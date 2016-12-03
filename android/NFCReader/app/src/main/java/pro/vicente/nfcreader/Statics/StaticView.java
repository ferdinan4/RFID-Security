package pro.vicente.nfcreader.Statics;

import android.app.Activity;
import android.view.View;

public class StaticView {
    public static void enable(Activity activity, final View v) {
        if (v != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.setEnabled(true);
                }
            });
        }
    }

    public static void disable(Activity activity, final View v) {
        if (v != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.setEnabled(false);
                }
            });
        }
    }
}
