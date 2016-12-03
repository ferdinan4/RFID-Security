package pro.vicente.gps2fa.location.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.furiios.restfulapi.RESTfulAPI;
import es.furiios.restfulapi.exceptions.NoSuchInstanceException;
import es.furiios.restfulapi.methods.HttpPUT;
import es.furiios.restfulapi.modules.RESTAddAuthBasicModule;
import es.furiios.restfulapi.modules.RESTAddDefaultHeadersModule;
import pro.vicente.gps2fa.RESTModules.RESTRestoreSessionModule;
import pro.vicente.gps2fa.Statics.RESTfulConsts;
import pro.vicente.gps2fa.Statics.SharedPreferencesHandler;
import pro.vicente.gps2fa.log.Logger;

public class RFIDLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback {

    private static final String TAG = "RFIDLocationService";

    private static boolean inited;

    private RESTfulAPI rest;
    private Geocoder geocoder;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private PendingIntent mGeofencePendingIntent;
    private static ArrayList<Geofence> mGeofenceList = new ArrayList<Geofence>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        inited = true;
        Logger.v(TAG, "RFIDLocationService started!");
        if (SharedPreferencesHandler.getCredentialsSharedPreferences(this).getString(SharedPreferencesHandler.CREDENTIALS_ID, null) != null) {
            try {
                rest = RESTfulAPI.getInstance(RESTfulConsts.REST_URL);
            } catch (NoSuchInstanceException e) {
                rest = RESTfulAPI.createInstance(this, RESTfulConsts.REST_URL);
                rest.addModule(new RESTAddDefaultHeadersModule());
                rest.addModule(new RESTRestoreSessionModule(getApplicationContext()));
                rest.addModule(new RESTAddAuthBasicModule(SharedPreferencesHandler.getCredentialsSharedPreferences(this).getString(SharedPreferencesHandler.CREDENTIALS_SESSION, null), "suchpassword"));

                e.printStackTrace();
            }

            geocoder = new Geocoder(this, Locale.getDefault());
            mGeofencePendingIntent = getGeofencePendingIntent();
            createLocationRequest(900000, 60000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            connectToGoogleApi();
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Logger.v(TAG, "GoogleApiClient connected!");
        getLastLocation();
        startLocationUpdates();
        addGeoference(null);
        startGeoferences();
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (location != null) {
            JSONObject params = new JSONObject();
            params.put("lat", location.getLatitude());
            params.put("lon", location.getLongitude());
            HttpPUT sendLocation = new HttpPUT("/users/", params.toJSONString());
            rest.executeAsync(sendLocation);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void stopGeoferences() {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private void startGeoferences() {
        if (!mGeofenceList.isEmpty()) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }
    }

    public static void addGeoference(Location loc) {
        //TODO user location
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("1")
                .setCircularRegion(42.6033412, -5.5789584, 100)
                .setExpirationDuration(604800000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private Location getLastLocation() {
        Logger.v(TAG, "Requesting last location...");
        Location current = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (current != null) {
            Logger.v(TAG, "Time: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(current.getTime())) + "\tProvider: " + current.getProvider() + "\tAcc: " + current.getAccuracy() + "\tLat: " + current.getLatitude() + "\tLng: " + current.getLongitude());
        }
        return current;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.v(TAG, "Connection to GoogleApiClient suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.v(TAG, "Connection failed on connect to GoogleApiClient: " + connectionResult.toString());
    }

    @Override
    public void onDestroy() {
        inited = false;
        stopLocationUpdates();
        Logger.v(TAG, "Stopping RFIDLocationService...");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void createLocationRequest(long interval, long fastest, int priority) {
        mLocationRequest = new LocationRequest();
        updateLocationRequest(interval, fastest, priority);
    }

    private void updateLocationRequest(long interval, long fastest, int priority) {
        if (mLocationRequest == null) {
            createLocationRequest(interval, fastest, priority);
        } else {
            mLocationRequest.setInterval(interval);
            mLocationRequest.setFastestInterval(fastest);
            mLocationRequest.setPriority(priority);
        }
    }

    private synchronized void buildGoogleApiClient() {
        Logger.v(TAG, "Initializing GoogleApiClient with LocationServices.API...");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void connectToGoogleApi() {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        if (mGoogleApiClient.isConnected()) {
            Logger.v(TAG, "Reconnecting to LocationServices.API...");
            mGoogleApiClient.reconnect();
        } else {
            Logger.v(TAG, "Connecting to LocationServices.API...");
            mGoogleApiClient.connect();
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

    public static boolean isInited() {
        return inited;
    }

    @Override
    public void onResult(Result result) {

    }
}
