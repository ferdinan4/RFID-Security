package es.furiios.restfulapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

import es.furiios.restfulapi.callbacks.RESTCallback;
import es.furiios.restfulapi.enums.CacheType;
import es.furiios.restfulapi.enums.FallbackCode;
import es.furiios.restfulapi.exceptions.NoSuchInstanceException;
import es.furiios.restfulapi.handlers.RestAsyncHandler;
import es.furiios.restfulapi.log.RESTfulAPILog;
import es.furiios.restfulapi.modules.RESTfulAPIModule;
import es.furiios.restfulapi.sqlite.RESTfulAPISQLite;

public abstract class HttpMethod {

    private static final String TAG = "HttpMethod";

    private static final int BBDD_VERSION = 1;
    private static final String BBDD_NAME = "RESTfulAPICache";

    protected HttpResponse response;
    protected String path, queryParameters;

    private boolean isCacheEnabled;
    private CacheType cacheRestoreType;

    private RestAsyncHandler mRestAsyncHandler;

    private HashMap<String, String> headers = new HashMap<String, String>();
    private HashMap<Integer, RESTCallback> callbacks = new HashMap<Integer, RESTCallback>();

    protected HttpMethod(String path, String queryParameters) {
        this.path = path;
        this.queryParameters = queryParameters;
        this.isCacheEnabled = false;
        this.cacheRestoreType = CacheType.ON_FAIL_RESTORE;
    }

    protected void setHeaders(HttpURLConnection urlConnection) {
        if (!headers.isEmpty()) {
            for (String key : headers.keySet()) {
                urlConnection.setRequestProperty(key, headers.get(key));
            }
        }
    }

    public void setCacheRestoreType(CacheType cacheRestoreType) {
        this.cacheRestoreType = cacheRestoreType;
    }

    public void addHeader(String field, String value) {
        headers.put(field, value);
    }

    public void setCacheStore(boolean enabled) {
        isCacheEnabled = enabled;
    }

    public void removeHeader(String field) {
        headers.remove(field);
    }

    public void clearHeaders() {
        headers.clear();
    }

    public String getPath() {
        return this.path;
    }

    public String getQueryParameters() {
        return this.queryParameters;
    }

    public void addRestAyncHandler(RestAsyncHandler restAsyncHandler) {
        mRestAsyncHandler = restAsyncHandler;
    }

    public void addCallback(int code, RESTCallback callback) {
        callbacks.put(code, callback);
    }

    public void addCallback(FallbackCode code, RESTCallback callback) {
        callbacks.put(code.getCode(), callback);
    }

    public void removeCallback(int code, RESTCallback callback) {
        callbacks.remove(code);
    }

    protected HttpResponse getResponse() {
        return this.response;
    }

    protected static String parseURI(String host, String path, String query) {
        return ((host.endsWith("/")) ? host.substring(0, host.length() - 1) : host) + ((path.startsWith("/")) ? path : "/" + path) + ((query != null) ? query : "");
    }

    protected HttpResponse execute(Context context, String host, boolean sync) {
        if(mRestAsyncHandler != null) {
            mRestAsyncHandler.onExecMethod();
        }

        RESTfulAPI rest = null;
        try {
            rest = RESTfulAPI.getInstance(host);
        } catch (NoSuchInstanceException e) {
            e.printStackTrace();
        }

        for (RESTfulAPIModule module : rest.getModules()) {
            module.onPreExecute(rest, this, sync);
        }

        if (cacheRestoreType == CacheType.NEVER_RESTORE ||
                (cacheRestoreType != CacheType.FORCE_RESTORE &&
                        !(isCacheEnabled && isSpam(context, parseURI(host, path, queryParameters))))) {
            try {
                response = execute(context, host, path, queryParameters);
                if (isCacheEnabled) {
                    storeData(context, response);
                }
            } catch (IOException e) {
                response = new HttpResponse(parseURI(host, path, queryParameters), FallbackCode.CON_FAILED.getCode(), "");
                if (cacheRestoreType == CacheType.ON_FAIL_RESTORE) {
                    restoreData(context, response);
                }
                e.printStackTrace();
            }
        } else {
            RESTfulAPILog.v(TAG, "FORCE_RESTORE: " + (cacheRestoreType == CacheType.FORCE_RESTORE));
            response = new HttpResponse(parseURI(host, path, queryParameters));
            restoreData(context, response);
        }

        if (callbacks.containsKey(response.getCode())) {
            callbacks.get(response.getCode()).onResult(response);
        } else if (callbacks.containsKey(FallbackCode.OTHERWISE.getCode())) {
            callbacks.get(FallbackCode.OTHERWISE.getCode()).onResult(response);
        }

        for (RESTfulAPIModule module : rest.getModules()) {
            module.onPostExecute(rest, this, response, sync);
        }

        if(mRestAsyncHandler != null) {
            mRestAsyncHandler.onEndMethod();
        }

        return response;
    }

    protected void setBody(HttpURLConnection urlConnection, String body) throws IOException {
        RESTfulAPILog.v(TAG, "Body: " + body);
        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
        out.write(queryParameters.getBytes());
        out.flush();
        out.close();
    }

    protected void setBody(HttpURLConnection urlConnection, HttpResponse response) throws IOException {
        int character;
        InputStream in;
        byte[] data = new byte[16384];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        if (urlConnection.getResponseCode() >= 400) {
            in = urlConnection.getErrorStream();
        } else {
            in = urlConnection.getInputStream();
        }

        while ((character = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, character);
        }

        buffer.flush();
        in.close();

        RESTfulAPILog.v(TAG, "Result Code: " + urlConnection.getResponseCode() + "\nBody: " + buffer.toString());
        response.setBody(buffer.toString());
        response.setRawData(buffer.toByteArray());
    }

    private void storeData(Context context, HttpResponse response) {
        RESTfulAPISQLite sqlite = new RESTfulAPISQLite(context, BBDD_NAME, null, BBDD_VERSION);
        SQLiteDatabase db = sqlite.getWritableDatabase();
        synchronized (db) {
            RESTfulAPILog.v(TAG, "Storing Data...");
            db.delete("cached", "`url` = '" + response.getUrl() + "'", null);
            ContentValues values = new ContentValues();
            values.put("url", response.getUrl());
            values.put("timestamp", (System.currentTimeMillis() / 1000));
            values.put("resultCode", response.getCode());
            values.put("html", response.getBody());
            db.insert("cached", null, values);
            RESTfulAPILog.v(TAG, "Successfull!");
            db.close();
        }
    }

    private void restoreData(Context context, HttpResponse response) {
        RESTfulAPISQLite sqlite = new RESTfulAPISQLite(context, BBDD_NAME, null, BBDD_VERSION);
        SQLiteDatabase db = sqlite.getReadableDatabase();
        synchronized (db) {
            RESTfulAPILog.v(TAG, "Getting Data...");
            Cursor c = db.query("cached", new String[]{"url", "resultCode", "html"}, "`url`='" + response.getUrl() + "'", null, null, null, null);
            if (c.moveToFirst()) {
                RESTfulAPILog.v(TAG, "Found Data...");
                response.setCode(c.getInt(c.getColumnIndex("resultCode")));
                response.setBody(c.getString(c.getColumnIndex("html")));
                response.setCached(true);
            } else {
                RESTfulAPILog.v(TAG, "Data not found...");
            }
            db.close();
        }
    }

    private boolean isSpam(Context context, String url) {
        boolean spam = false;
        RESTfulAPISQLite sqlite = new RESTfulAPISQLite(context, BBDD_NAME, null, BBDD_VERSION);
        SQLiteDatabase db = sqlite.getReadableDatabase();
        synchronized (db) {
            RESTfulAPILog.v(TAG, "Getting Data...");
            Cursor c = db.query("cached", new String[]{"timestamp"}, "`url`='" + url + "'", null, null, null, null);
            if (c.moveToFirst() && (System.currentTimeMillis() / 1000) - c.getInt(c.getColumnIndex("timestamp")) < 2) {
                spam = true;
            }
            db.close();
            RESTfulAPILog.v(TAG, "Spam Detected: " + spam);
            return spam;
        }
    }

    protected abstract HttpResponse execute(Context context, String host, String path, String queryParameters) throws IOException;
}
