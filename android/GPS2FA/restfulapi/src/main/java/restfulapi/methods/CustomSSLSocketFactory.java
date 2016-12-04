package restfulapi.methods;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Devuelve un SSLSocketFactory que acepta el certificado ubicado en assets/FICHERO_CERT como
 * certificado de confianza
 */
public class CustomSSLSocketFactory {

    private CustomSSLSocketFactory() {
        super();
    }

    private static SSLSocketFactory sslSocketFactory;

    private static final String FICHERO_CERT = "cert.crt";

    public static SSLSocketFactory getSSLSocketFactory(Context context)
            throws CertificateException, IOException, GeneralSecurityException {
        //sólo se instancia la primera vez que se necesite
        if (sslSocketFactory == null) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getAssets().open(FICHERO_CERT));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            //se añaden todos los certificados obtenidos desde el assets. En este caso
            //sólo tenemos uno.
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            sslSocketFactory = sslContext.getSocketFactory();
        }
        return sslSocketFactory;
    }
}