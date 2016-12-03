package restfulapi.methods;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import restfulapi.HttpResponse;
import restfulapi.log.RESTfulAPILog;

public class HttpPOSTBitmap extends HttpPOST {

    private static final String TAG = "HttpPOSTFile";

    private String crlf = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "*****";

    private Bitmap bitmap;
    private String attachmentName;
    private String attachmentFileName;

    public HttpPOSTBitmap(String path, Bitmap bitmap, String attachmentName, String attachmentFileName) {
        super(path, "");
        this.bitmap = bitmap;
        this.attachmentName = attachmentName;
        this.attachmentFileName = attachmentFileName;
    }

    @Override
    protected HttpResponse execute(Context context, String host, String path, String queryParameters) throws IOException {
        URL url = new URL(parseURI(host, path, null));
        HttpResponse response = new HttpResponse(url.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setUseCaches(false);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setChunkedStreamingMode(0);

        removeHeader("Content-Type");
        addHeader("Connection", "Keep-Alive");
        addHeader("Cache-Control", "no-cache");
        addHeader("Content-Type", "multipart/form-data;boundary=" + boundary);
        setHeaders(urlConnection);
        setCacheStore(false);

        DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
        request.writeBytes(crlf);

        request.write(getBytesFromBitmap(bitmap));
        request.writeBytes(crlf);
        request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

        request.flush();
        request.close();

        try {
            RESTfulAPILog.v(TAG, "Request: " + url.toString());
            response.setCode(urlConnection.getResponseCode());
            setBody(urlConnection, response);
        } finally {
            urlConnection.disconnect();
        }
        return response;
    }

    private byte[] getBytesFromBitmap(Bitmap b) {
        //b = Bitmap.createScaledBitmap(b, 128, 128, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
