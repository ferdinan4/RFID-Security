package pro.vicente.gps2fa.log;

import android.util.Log;

public abstract class Logger {

    private static final String TAG = "Logger";
    private static final boolean DEBUG = true;

    public static void v(String tag, String msg) {
        if (Logger.DEBUG) {
            Log.v(tag, "\n" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (Logger.DEBUG) {
            Log.d(tag, "\n" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (Logger.DEBUG) {
            Log.e(tag, "\n" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (Logger.DEBUG) {
            Log.i(tag, "\n" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (Logger.DEBUG) {
            Log.w(tag, "\n" + msg);
        }
    }

    public static void wtf(String tag, String msg) {
        if (Logger.DEBUG) {
            Log.wtf(tag, "\n" + msg);
        }
    }
}
