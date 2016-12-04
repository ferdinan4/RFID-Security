package pro.vicente.gps2fa.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Locale;
import java.util.Map;

import pro.vicente.gps2fa.R;
import pro.vicente.gps2fa.RESTModules.RESTRestoreSessionModule;
import pro.vicente.gps2fa.Statics.SharedPreferencesHandler;
import pro.vicente.gps2fa.Statics.StaticView;
import restfulapi.HttpResponse;
import restfulapi.RESTfulAPI;
import restfulapi.callbacks.RESTCallback;
import restfulapi.enums.FallbackCode;
import restfulapi.handlers.RestAsyncHandler;
import restfulapi.methods.HttpGET;
import restfulapi.methods.HttpPOST;
import restfulapi.modules.RESTAddAuthBasicModule;
import restfulapi.modules.RESTAddDefaultHeadersModule;

public class LoginActivity extends FragmentActivity {

    private Button login;
    private EditText user, pwd, server;
    private SharedPreferences preferences;

    private RESTfulAPI rest;

    /**
     * This method is executed when the application is initialized. Then, check of the user's
     * credentials against the API and this will be returned by the session token.
     * Also, it initializes the components of the view and the associated events.
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = SharedPreferencesHandler.getCredentialsSharedPreferences(this);
        if (preferences.getString(SharedPreferencesHandler.CREDENTIALS_ID, null) != null) {
            startMainActivity();
        } else {
            if (preferences.getString(SharedPreferencesHandler.CREDENTIALS_USER, null) != null && preferences.getString(SharedPreferencesHandler.CREDENTIALS_PWD, null) != null) {
                if (preferences.getString(SharedPreferencesHandler.CREDENTIALS_SESSION, null) == null) {
                    loginWithCredentials(preferences.getString(SharedPreferencesHandler.CREDENTIALS_USER, null), preferences.getString(SharedPreferencesHandler.CREDENTIALS_PWD, null));
                } else {
                    loginWithSession();
                }
            }
        }

        setContentView(R.layout.activity_login);
        this.login = (Button) findViewById(R.id.sign_in);
        this.user = (EditText) findViewById(R.id.user);
        this.pwd = (EditText) findViewById(R.id.pwd);
        this.server = (EditText) findViewById(R.id.server);

        if (SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, null) != null) {
            this.server.setText(SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
        }

        this.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticView.disable(LoginActivity.this, login);
                if (!(user.getText().toString().isEmpty() || pwd.getText().toString().isEmpty() || server.toString().isEmpty())) {
                    StaticView.disable(LoginActivity.this, login);
                    SharedPreferencesHandler.getCredentialsSharedPreferencesEditor(getApplicationContext()).putString(SharedPreferencesHandler.CREDENTIALS_SERVER, server.getText().toString()).commit();
                    loginWithCredentials(user.getText().toString().toUpperCase(Locale.getDefault()), pwd.getText().toString());
                } else {
                    Snackbar.make(login, getString(R.string.provide_user_and_pass), Snackbar.LENGTH_LONG).show();
                    StaticView.enable(LoginActivity.this, login);
                }
            }
        });
    }

    /**
     * This method is responsible for making the POST request against the REST API to check the credentials of the user
     *
     * @param userCredential Username
     * @param pwdCredential  User's Password
     */

    private void loginWithCredentials(final String userCredential, final String pwdCredential) {
        JSONObject params = new JSONObject();
        params.put("user", userCredential);
        params.put("pwd", pwdCredential);
        HttpPOST loginRequest = new HttpPOST("/session/", params.toJSONString());
        loginRequest.addCallback(201, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                try {
                    JSONParser parser = new JSONParser();
                    Map<?, ?> data = (Map<?, ?>) parser.parse(response.getBody());
                    SharedPreferencesHandler.getCredentialsSharedPreferencesEditor(LoginActivity.this)
                            .putString(SharedPreferencesHandler.CREDENTIALS_USER, userCredential)
                            .putString(SharedPreferencesHandler.CREDENTIALS_PWD, pwdCredential)
                            .commit();

                    rest = RESTfulAPI.createInstance(LoginActivity.this, SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
                    rest.addModule(new RESTAddDefaultHeadersModule());
                    rest.addModule(new RESTRestoreSessionModule(getApplicationContext()));
                    rest.addModule(new RESTAddAuthBasicModule((String) data.get("session"), "suchpassword"));

                    loginWithSession();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        loginRequest.addCallback(403, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                Snackbar.make(login, getString(R.string.LoginActivity_user_or_pass_incorrect), Snackbar.LENGTH_LONG).show();
                SharedPreferencesHandler.getCredentialsSharedPreferencesEditor(LoginActivity.this)
                        .remove(SharedPreferencesHandler.CREDENTIALS_ID)
                        .remove(SharedPreferencesHandler.CREDENTIALS_USER)
                        .remove(SharedPreferencesHandler.CREDENTIALS_PWD)
                        .commit();
            }
        });
        loginRequest.addCallback(FallbackCode.OTHERWISE, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                Snackbar.make(login, getString(R.string.an_error_ocurred), Snackbar.LENGTH_LONG).show();

            }
        });
        loginRequest.addCallback(FallbackCode.CON_FAILED, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                Snackbar.make(login, getString(R.string.check_your_internet_connection), Snackbar.LENGTH_LONG).show();
            }
        });
        loginRequest.addRestAyncHandler(new RestAsyncHandler() {
            @Override
            public void onEndMethod() {
                StaticView.enable(LoginActivity.this, login);
            }
        });
        rest = RESTfulAPI.createInstance(LoginActivity.this, SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
        rest.executeAsync(loginRequest);
    }

    /**
     * This method do a GET request against API to obtain session's token and
     * save to do requests against API.
     */
    private void loginWithSession() {
        HttpGET loginRequest = new HttpGET("/session/");
        loginRequest.addCallback(200, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                try {
                    JSONParser parser = new JSONParser();
                    Map<?, ?> data = (Map<?, ?>) parser.parse(response.getBody());
                    SharedPreferencesHandler.getCredentialsSharedPreferencesEditor(LoginActivity.this).putString(SharedPreferencesHandler.CREDENTIALS_ID, (String) data.get("id")).commit();
                    startMainActivity();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        loginRequest.addCallback(FallbackCode.OTHERWISE, new RESTCallback() {
            @Override
            public void onResult(HttpResponse response) {
                SharedPreferencesHandler.getCredentialsSharedPreferencesEditor(LoginActivity.this).remove(SharedPreferencesHandler.CREDENTIALS_SESSION).commit();

                startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                Snackbar.make(login, getString(R.string.check_your_internet_connection), Snackbar.LENGTH_LONG).show();
                StaticView.enable(LoginActivity.this, login);
                finish();
            }
        });
        rest.execute(loginRequest);
    }

    /**
     * This method initializes the main activity
     */

    private void startMainActivity() {
        rest = RESTfulAPI.createInstance(LoginActivity.this, SharedPreferencesHandler.getCredentialsSharedPreferences(getApplicationContext()).getString(SharedPreferencesHandler.CREDENTIALS_SERVER, ""));
        rest.addModule(new RESTAddDefaultHeadersModule());
        rest.addModule(new RESTRestoreSessionModule(getApplicationContext()));
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}