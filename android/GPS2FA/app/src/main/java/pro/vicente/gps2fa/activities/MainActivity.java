package pro.vicente.gps2fa.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import pro.vicente.gps2fa.R;
import pro.vicente.gps2fa.Statics.SharedPreferencesHandler;
import pro.vicente.gps2fa.location.services.RFIDLocationService;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_FINE_LOCATION = 0;

    private ImageView image;
    private Toolbar toolbar;
    private GoogleMap mMap;

    private Circle mCircle;
    private Marker mMarker;

    /**
     * This method initializes the view and request user's permissions to access the location
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        image = (ImageView) findViewById(R.id.image);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION);
    }

    /**
     * This method is in charge of requesting user's permission.
     *
     * @param perm        Permission we want to request
     * @param requestCode Request Code
     */
    private void loadPermissions(String perm, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, requestCode);
            }
        } else {
            RFIDLocationService.init(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RFIDLocationService.init(this);
                } else {
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(4);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (mCircle != null)
                    mCircle.remove();
                if (mMarker != null)
                    mMarker.remove();

                mCircle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(Double.parseDouble(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_LAT, "0")), Double.parseDouble(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_LNG, "0"))))
                        .radius(50)
                        .strokeWidth(0f)
                        .zIndex(5)
                        .fillColor(0x55FFFFFF)
                        .visible(true));

                mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_LAT, "0")), Double.parseDouble(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_LNG, "0")))));

            }
        });
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_settings:
                return true;
            case R.id.action_menu_log_out:
                SharedPreferencesHandler.getCredentialsSharedPreferencesEditor(this).clear().commit();
                startActivity(new Intent(this, LoginActivity.class));
                RFIDLocationService.init(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
