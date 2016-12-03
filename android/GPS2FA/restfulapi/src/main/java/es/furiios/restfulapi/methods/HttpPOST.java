package es.furiios.restfulapi.methods;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import es.furiios.restfulapi.HttpMethod;
import es.furiios.restfulapi.HttpResponse;
import es.furiios.restfulapi.log.RESTfulAPILog;

public class HttpPOST extends HttpMethod {

    private static final String TAG = "HttpPOST";

    public HttpPOST(String path, String queryParameters) {
        super(path, queryParameters);
    }

    @Override
    protected HttpResponse execute(Context context, String host, String path, String queryParameters) throws IOException {
        URL url = new URL(parseURI(host, path, null));
        HttpResponse response = new HttpResponse(url.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        urlConnection.setRequestMethod("POST");
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
