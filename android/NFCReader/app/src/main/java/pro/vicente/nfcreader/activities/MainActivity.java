package pro.vicente.nfcreader.activities;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import pro.vicente.nfcreader.R;
import pro.vicente.nfcreader.RESTModules.RESTRestoreSessionModule;
import pro.vicente.nfcreader.Statics.SharedPreferencesHandler;
import restfulapi.HttpResponse;
import restfulapi.RESTfulAPI;
import restfulapi.callbacks.RESTCallback;
import restfulapi.enums.CacheType;
import restfulapi.exceptions.NoSuchInstanceException;
import restfulapi.methods.HttpGET;
import restfulapi.modules.RESTAddAuthBasicModule;
import restfulapi.modules.RESTAddDefaultHeadersModule;

/**
 * In this main class
 **/
public class MainActivity extends ArduinoADK {

    private RESTfulAPI rest;
    private NfcAdapter mNfcAdapter;
    private Toolbar toolbar;


    /**
     * Override 'onCreate' and associate the layout activity_main and create an instace from our APIRest
     *
     * @param savedInstanceState
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        try {
            rest = RESTfulAPI.getInstance(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
        } catch (NoSuchInstanceException e) {
            rest = RESTfulAPI.createInstance(this, SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
            rest.addModule(new RESTAddDefaultHeadersModule());
            rest.addModule(new RESTRestoreSessionModule(getApplicationContext()));
            rest.addModule(new RESTAddAuthBasicModule(SharedPreferencesHandler.getCredentialsSharedPreferences(this).getString(SharedPreferencesHandler.CREDENTIALS_SESSION, null), "suchpassword"));

            e.printStackTrace();
        }
        rest.executeAsync(new HttpGET("/session/"));
    }

    /**
     * Override 'onNewIntent' that is called for the mobile when a NFC Card is read
     * After that, we extract data from the NFC card, and we make the GET request,if this request return 403, so the ID dont exist, so we turn on red led on arduino.
     * On the other hand if we obtain 200 we make again other GET request, to check if our geoposition is in an allowed range where we turn on green led or if it isn't
     * we will turn on yellow led and return 403.
     *
     * @param intent
     **/
    @Override
    public void onNewIntent(Intent intent) {
        final Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        HttpGET checkCard = new HttpGET("/business/check/" + bytesToHex(tagFromIntent.getId()) + "/");
        checkCard.addCallback(403, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                if (mOutputStream != null) {
                    try {
                        mOutputStream.write(3);
                        Thread.sleep(1000);
                        mOutputStream.write(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        checkCard.addCallback(200, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                HttpGET check = new HttpGET("/business/check/gps/" + bytesToHex(tagFromIntent.getId()) + "/");
                check.addCallback(200, new RESTCallback() {
                    @Override
                    public void onResult(HttpResponse response) {
                        if (mOutputStream != null) {
                            try {
                                mOutputStream.write(2);
                                Thread.sleep(1000);
                                mOutputStream.write(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                check.addCallback(403, new RESTCallback() {
                    @Override
                    public void onResult(HttpResponse response) {
                        if (mOutputStream != null) {
                            try {
                                mOutputStream.write(1);
                                Thread.sleep(1000);
                                mOutputStream.write(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                check.setCacheRestoreType(CacheType.NEVER_RESTORE);
                rest.executeAsync(check);
            }
        });
        rest.executeAsync(checkCard);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.enable_nfc_title)
                    .setMessage(R.string.please_activate_nfc)
                    .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                        }
                    })
                    .show();
        }

        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0), null, null);
    }


    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
