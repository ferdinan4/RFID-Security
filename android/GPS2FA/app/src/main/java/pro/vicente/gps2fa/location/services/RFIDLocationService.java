package pro.vicente.gps2fa.location.services;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Map;

import pro.vicente.gps2fa.RESTModules.RESTRestoreSessionModule;
import pro.vicente.gps2fa.Statics.SharedPreferencesHandler;
import restfulapi.HttpResponse;
import restfulapi.RESTfulAPI;
import restfulapi.callbacks.RESTCallback;
import restfulapi.exceptions.NoSuchInstanceException;
import restfulapi.methods.HttpGET;
import restfulapi.methods.HttpPUT;
import restfulapi.modules.RESTAddAuthBasicModule;
import restfulapi.modules.RESTAddDefaultHeadersModule;

public class RFIDLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback {

    private static boolean inited;

    private RESTfulAPI rest;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private PendingIntent mGeofencePendingIntent;

    private ArrayList<Geofence> mGeofenceList = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        inited = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && SharedPreferencesHandler.getCredentialsSharedPreferences(this).getString(SharedPreferencesHandler.CREDENTIALS_ID, null) != null) {
            try {
                rest = RESTfulAPI.getInstance(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
            } catch (NoSuchInstanceException e) {
                rest = RESTfulAPI.createInstance(this, SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
                rest.addModule(new RESTAddDefaultHeadersModule());
                rest.addModule(new RESTRestoreSessionModule(getApplicationContext()));
                rest.addModule(new RESTAddAuthBasicModule(SharedPreferencesHandler.getCredentialsSharedPreferences(this).getString(SharedPreferencesHandler.CREDENTIALS_SESSION, null), "suchpassword"));

                e.printStackTrace();
            }

            connectToGoogleApi();
            mGeofencePendingIntent = getGeofencePendingIntent();
            createLocationRequest(150000, 20000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    private void stopGeofences() {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private void startForGeofences() {
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private void addGeofence() {
        HttpGET getLocation = new HttpGET("/achievement/");
        getLocation.addCallback(200, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                try {
                    JSONParser parser = new JSONParser();
                    Map<?, ?> data = (Map<?, ?>) parser.parse(response.getBody());

                    LatLng latlng = new GsonBuilder().create().fromJson(response.getBody(), LatLng.class);
                    SharedPreferencesHandler.getCredentialsSharedPreferencesEditor(getApplicationContext())
                            .putString(SharedPreferencesHandler.CREDENTIALS_LAT, (String) data.get("lat"))
                            .putString(SharedPreferencesHandler.CREDENTIALS_LNG, (String) data.get("lon"))
                            .commit();
                    mGeofenceList.add(new Geofence.Builder()
                            .setRequestId("RFID")
                            .setCircularRegion(
                                    latlng.latitude,
                                    latlng.longitude,
                                    50
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        rest.execute(getLocation);
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private void connectToGoogleApi() {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.reconnect();
        } else {
            mGoogleApiClient.connect();
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private synchronized void createLocationRequest(long interval, long fastest, int priority) {
        mLocationRequest = new LocationRequest();
        if (mLocationRequest == null) {
            createLocationRequest(interval, fastest, priority);
        } else {
            mLocationRequest.setInterval(interval);
            mLocationRequest.setFastestInterval(fastest);
            mLocationRequest.setPriority(priority);
        }
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public static void init(Activity activity) {
        if (!inited) {
            activity.startService(new Intent(activity, RFIDLocationService.class));
        }
    }

    private Location getLastLocation() {
        Location current = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return current;
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (/*GeofenceTransitionsIntentService.IS_TRANSITION_ENTER && */location != null) {
            JSONObject params = new JSONObject();
            params.put("lat", location.getLatitude());
            params.put("lon", location.getLongitude());
            HttpPUT sendLocation = new HttpPUT("/users/", params.toJSONString());
            rest.executeAsync(sendLocation);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLastLocation();
        startLocationUpdates();
        addGeofence();
        startForGeofences();

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onDestroy() {
        inited = false;
        stopLocationUpdates();
        stopGeofences();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onResult(Result result) {

    }
}
