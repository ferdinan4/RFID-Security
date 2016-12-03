package es.furiios.restfulapi.methods;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import es.furiios.restfulapi.HttpMethod;
import es.furiios.restfulapi.HttpResponse;
import es.furiios.restfulapi.log.RESTfulAPILog;

public class HttpGET extends HttpMethod {

    private static final String TAG = "HttpGET";

    public HttpGET(String path) {
        super(path, null);
    }

    public HttpGET(String path, String query) {
        super(path, query);
    }

    @Override
    protected HttpResponse execute(Context context, String host, String path, String queryParameters) throws IOException {
        URL url = new URL(parseURI(host, path, queryParameters));
        HttpResponse response = new HttpResponse(url.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        setHeaders(urlConnection);

        try {
            RESTfulAPILog.v(TAG, "Request: " + url.toString());
            response.setCode(urlConnection.getResponseCode());
            setBody(urlConnection, response);
        } finally {
            urlConnection.disconnect();
        }
        return response;
    }
}
