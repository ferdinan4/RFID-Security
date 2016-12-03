package pro.vicente.nfcreader;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import es.furiios.restfulapi.HttpResponse;
import es.furiios.restfulapi.RESTfulAPI;
import es.furiios.restfulapi.callbacks.RESTCallback;
import es.furiios.restfulapi.enums.CacheType;
import es.furiios.restfulapi.methods.HttpGET;

public class MainActivity extends ArduinoADK {

    private RESTfulAPI rest;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        rest = RESTfulAPI.createInstance(this, "http://rfid.furiios.es");
    }

    @Override
    public void onNewIntent(Intent intent) {
        final Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        HttpGET checkCard = new HttpGET("/business/1/check/" + bytesToHex(tagFromIntent.getId()) + "/");
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
                HttpGET check = new HttpGET("/business/1/check/gps/" + bytesToHex(tagFromIntent.getId()) + "/");
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

        if (!mNfcAdapter.isEnabled()) {
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
