package pro.vicente.gps2fa.location.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pro.vicente.gps2fa.location.services.RFIDLocationService;
import pro.vicente.gps2fa.log.Logger;

public class WakeUpRFID extends BroadcastReceiver {

    private static final String TAG = "WakeUpRFID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.v(TAG, "BOOT COMPLETED received. Starting RFIDLocationService...");
        Intent i = new Intent(context, RFIDLocationService.class);
        context.startService(i);
    }
}
