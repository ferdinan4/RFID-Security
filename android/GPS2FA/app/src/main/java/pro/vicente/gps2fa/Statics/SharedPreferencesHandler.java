package pro.vicente.gps2fa.Statics;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHandler {
    private static final String TAG = "SharedPreferencesHandler";

    /**
     * Store data in user's preferences
     */
    public static final String CREDENTIALS_ID = "LOGIN_CREDENTIALS_ID";
    public static final String CREDENTIALS_SESSION = "LOGIN_CREDENTIALS_SESSION";
    public static final String CREDENTIALS_USER = "LOGIN_CREDENTIALS_USER";
    public static final String CREDENTIALS_PWD = "LOGIN_CREDENTIALS_PWD";
    public static final String CREDENTIALS_NAME = "LOGIN_CREDENTIALS_NAME";
    public static final String CREDENTIALS_SURNAME = "LOGIN_CREDENTIALS_SURNAME";
    public static final String CREDENTIALS_SERVER = "LOGIN_CREDENTIALS_SERVER";

    public static SharedPreferences getCredentialsSharedPreferences(Context context) {
        return context.getSharedPreferences("RFIDSecurityLoginPreferences", Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getCredentialsSharedPreferencesEditor(Context context) {
        return getCredentialsSharedPreferences(context).edit();
    }
}
