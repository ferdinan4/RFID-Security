package pro.vicente.nfcreader.activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

import pro.vicente.nfcreader.R;

/**
 * This class is used to communicate with arduino by USB
 **/
public abstract class ArduinoADK extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "USB_PERMISSION";


    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;
    private PendingIntent mPermissionIntent;

    protected FileOutputStream mOutputStream;
    private ParcelFileDescriptor mFileDescriptor;

    private boolean mPermissionRequestPending;

    /**
     * Is called when a USB connection is detected and try to initializate the communication with arduino
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Toast.makeText(ArduinoADK.this, getString(R.string.permission_denied) + accessory, Toast.LENGTH_LONG).show();
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(mAccessory))
                    closeAccessory();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    /**
     * Started the communcation with arduino
     *
     * @param accessory is the USB device
     **/
    private void openAccessory(UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            mOutputStream = new FileOutputStream(mFileDescriptor.getFileDescriptor());

            setConnectionStatus(true);
            Toast.makeText(ArduinoADK.this, R.string.accesory_opened, Toast.LENGTH_LONG).show();
        } else {
            setConnectionStatus(false);
            Toast.makeText(ArduinoADK.this, R.string.accesory_open_failed, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * finish the communication with arduino
     **/
    private void closeAccessory() {
        setConnectionStatus(false);

        try {
            if (mOutputStream != null)
                mOutputStream.close();
        } catch (Exception ignored) {
        } finally {
            mOutputStream = null;
        }

        try {
            if (mFileDescriptor != null)
                mFileDescriptor.close();
        } catch (IOException ignored) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    /**
     * Show if is connected or not by USB
     *
     * @param connected return True or false depends of the connection
     **/
    private void setConnectionStatus(boolean connected) {
        Toast.makeText(ArduinoADK.this, connected ? getString(R.string.connected) : getString(R.string.disconnected), Toast.LENGTH_LONG).show();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * If the communication have been started, ask us if really  we want to keen on the app
     **/
    @Override
    public void onBackPressed() {
        if (mAccessory != null) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.close_Activity)
                    .setMessage(R.string.are_you_sure_exit)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else {
            finish();
        }
    }

    /**
     * Try to communicate when the app is onResume()
     **/
    @Override
    public void onResume() {
        super.onResume();
        if (mAccessory != null) {
            setConnectionStatus(true);
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory))
                openAccessory(accessory);
            else {
                setConnectionStatus(false);
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            setConnectionStatus(false);
        }
    }

    /**
     * Override onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        closeAccessory();
        unregisterReceiver(mUsbReceiver);
    }
}
