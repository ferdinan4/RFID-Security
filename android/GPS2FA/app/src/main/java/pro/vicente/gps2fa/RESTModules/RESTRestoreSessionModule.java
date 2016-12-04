package pro.vicente.gps2fa.RESTModules;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;
import java.util.concurrent.Semaphore;

import pro.vicente.gps2fa.Statics.SharedPreferencesHandler;
import restfulapi.HttpMethod;
import restfulapi.HttpResponse;
import restfulapi.RESTfulAPI;
import restfulapi.callbacks.RESTCallback;
import restfulapi.enums.CacheType;
import restfulapi.log.RESTfulAPILog;
import restfulapi.methods.HttpPOST;
import restfulapi.modules.RESTAddAuthBasicModule;
import restfulapi.modules.RESTfulAPIModule;

public class RESTRestoreSessionModule extends RESTfulAPIModule {

    private static final String TAG = "RESTRestoreSessionModule";
    private static Semaphore restoring = new Semaphore(1, true);
    private static long restoredTime;

    private final String user, pwd;
    private SharedPreferences preferences;

    /**
     * Class constructor, that initializes the variables, will get them from the shared preferences.
     * @param context
     */
    public RESTRestoreSessionModule(Context context) {
        super(TAG);
        preferences = SharedPreferencesHandler.getCredentialsSharedPreferences(context);
        user = preferences.getString(SharedPreferencesHandler.CREDENTIALS_USER, null);
        pwd = preferences.getString(SharedPreferencesHandler.CREDENTIALS_PWD, null);
    }

    /**
     * Restore a user session if it has expired
     *
     * @param restApi
     * @param request
     * @param response
     * @param sync
     */
    @Override
    public void onPostExecute(final RESTfulAPI restApi, final HttpMethod request, HttpResponse response, final boolean sync) {
        if (response != null && response.getCode() == 401 && user != null && pwd != null) {
            try {
                restoring.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (System.currentTimeMillis() - restoredTime > 5000) {
                RESTfulAPILog.v(TAG, "Restoring session...");
                JSONObject params = new JSONObject();
                params.put("user", user);
                params.put("pwd", pwd);
                HttpPOST session = new HttpPOST("/session/", params.toJSONString());
                session.addCallback(201, new RESTCallback() {
                    @Override
                    public void onResult(HttpResponse response) {
                        try {
                            boolean found = false;
                            String mSession = (String) ((Map<?, ?>) new JSONParser().parse(response.getBody())).get("session");

                            RESTfulAPILog.v(TAG, "Cleaning previous RESTAddAuthBasicModule...");
                            for (RESTfulAPIModule module : restApi.getModules()) {
                                if (module.getTag().equals("RESTAddAuthBasicModule")) {
                                    found = true;
                                    RESTfulAPILog.v(TAG, "Changing RESTAddAuthBasicModule...");
                                    ((RESTAddAuthBasicModule) module).setUsername(mSession);
                                }
                            }
                            if (!found) {
                                RESTfulAPILog.v(TAG, "Creating RESTAddAuthBasicModule...");
                                restApi.addModule(new RESTAddAuthBasicModule(mSession, "suchpassword"));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        RESTfulAPILog.v(TAG, response.getBody());
                    }
                });
                restApi.execute(session);
                restoredTime = System.currentTimeMillis();
            }
            restoring.release();

            RESTfulAPILog.v(TAG, "New session. Executing failed request...");
            request.setCacheRestoreType(CacheType.NEVER_RESTORE);
            if (sync) {
                restApi.execute(request);
            } else {
                restApi.executeAsync(request);
            }
        }
    }
}
