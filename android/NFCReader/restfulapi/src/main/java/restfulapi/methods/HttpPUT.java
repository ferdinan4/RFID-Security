package restfulapi.methods;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;

import restfulapi.HttpMethod;
import restfulapi.HttpResponse;
import restfulapi.log.RESTfulAPILog;

public class HttpPUT extends HttpMethod {

    private static final String TAG = "HttpPUT";

    public HttpPUT(String path, String queryParameters) {
        super(path, queryParameters);
    }

    @Override
    protected HttpResponse execute(Context context, String host, String path, String queryParameters) throws IOException {
        URL url = new URL(parseURI(host, path, null));
        HttpResponse response = new HttpResponse(url.toString());

        HttpURLConnection urlConnection;
        if (host.startsWith("https")) {
            urlConnection = (HttpsURLConnection) url.openConnection();
            try {
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(context));
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        urlConnection.setRequestMethod("PUT");
        setHeaders(urlConnection);
        setCacheStore(false);

        try {
            RESTfulAPILog.v(TAG, "Request: " + url.toString());
            setBody(urlConnection, queryParameters);
            response.setCode(urlConnection.getResponseCode());
            setBody(urlConnection, response);
        } finally {
            urlConnection.disconnect();
        }
        return response;
    }
}
