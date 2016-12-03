package pro.vicente.gps2fa.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import pro.vicente.gps2fa.R;
import pro.vicente.gps2fa.location.services.RFIDLocationService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_FINE_LOCATION = 0;

    private ImageView image;
    private Toolbar toolbar;

    /**
     * Este metodo inicializamos la vista y solicitamos los permisos al usuario para acceder a la
     * localizacion
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

        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION);
    }

    /**
     * Este metodo es el encargado de solicitar el permiso al usuario
     * @param perm Permiso que queremos solicitar
     * @param requestCode Codigo de peticion
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
}
