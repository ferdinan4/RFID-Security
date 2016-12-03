package es.furiios.restfulapi.modules;

import android.util.Base64;

import es.furiios.restfulapi.HttpMethod;
import es.furiios.restfulapi.RESTfulAPI;
import es.furiios.restfulapi.log.RESTfulAPILog;

public class RESTAddAuthBasicModule extends RESTfulAPIModule {

    private static final String TAG = "RESTAddAuthBasicModule";
    private String username, password;

    public RESTAddAuthBasicModule(String username, String password) {
        super(TAG);
        RESTfulAPILog.v(TAG, "Setting new session with value: " + username);
        this.username = username;
        this.password = password;
    }

    @Override
    public void onPreExecute(RESTfulAPI restApi, HttpMethod request, boolean sync) {
        RESTfulAPILog.v(TAG, "Pre execute: Session: " + username);
        request.addHeader("Authorization", "Basic " + new String(Base64.encode((username + ":" + password).getBytes(), Base64.DEFAULT)));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
