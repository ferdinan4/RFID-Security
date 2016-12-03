package es.furiios.restfulapi.log;

import android.util.Log;

public abstract class RESTfulAPILog {

    private static final boolean DEBUG = true;

    public static void v(String tag, String msg) {
        if (RESTfulAPILog.DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (RESTfulAPILog.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (RESTfulAPILog.DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (RESTfulAPILog.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (RESTfulAPILog.DEBUG) {
            Log.w(tag, msg);
        }
    }
}
