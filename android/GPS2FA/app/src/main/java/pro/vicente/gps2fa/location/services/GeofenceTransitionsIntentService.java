package pro.vicente.gps2fa.location.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import org.json.simple.JSONObject;

import pro.vicente.gps2fa.R;
import pro.vicente.gps2fa.RESTModules.RESTRestoreSessionModule;
import pro.vicente.gps2fa.Statics.SharedPreferencesHandler;
import restfulapi.RESTfulAPI;
import restfulapi.exceptions.NoSuchInstanceException;
import restfulapi.methods.HttpPUT;
import restfulapi.modules.RESTAddAuthBasicModule;
import restfulapi.modules.RESTAddDefaultHeadersModule;

public class GeofenceTransitionsIntentService extends IntentService {

    private RESTfulAPI rest;
    public static boolean IS_TRANSITION_ENTER = false;

    public GeofenceTransitionsIntentService() {
        super("");
    }

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            return;
        }

        try {
            rest = RESTfulAPI.getInstance(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
        } catch (NoSuchInstanceException e) {
            rest = RESTfulAPI.createInstance(this, SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
            rest.addModule(new RESTAddDefaultHeadersModule());
            rest.addModule(new RESTRestoreSessionModule(getApplicationContext()));
            rest.addModule(new RESTAddAuthBasicModule(SharedPreferencesHandler.getCredentialsSharedPreferences(this).getString(SharedPreferencesHandler.CREDENTIALS_SESSION, null), "suchpassword"));
            e.printStackTrace();
        }

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
            IS_TRANSITION_ENTER = true;

            Location current = LocationServices.FusedLocationApi.getLastLocation(new GoogleApiClient.Builder(this).addApi(LocationServices.API).build());
            if (current != null) {
                JSONObject params = new JSONObject();
                params.put("lat", current.getLatitude());
                params.put("lon", current.getLongitude());
                HttpPUT sendLocation = new HttpPUT("/users/", params.toJSONString());
                rest.executeAsync(sendLocation);
            }

            mNotifyMgr.notify(42, new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Geofence Transition Enter")
                    .setContentText("Now you can use your card!").build());
        } else {
            IS_TRANSITION_ENTER = false;

            JSONObject params = new JSONObject();
            params.put("lat", 0);
            params.put("lon", 0);
            HttpPUT sendLocation = new HttpPUT("/users/", params.toJSONString());
            rest.executeAsync(sendLocation);

            mNotifyMgr.notify(42, new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Geofence Transition Enter")
                    .setContentText("You don't use your card!").build());
        }
    }
}
