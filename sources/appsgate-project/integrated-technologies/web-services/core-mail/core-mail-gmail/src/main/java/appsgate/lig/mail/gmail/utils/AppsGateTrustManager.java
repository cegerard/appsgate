package appsgate.lig.mail.gmail.utils;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


/**
 * Created by thibaud on 04/07/2014.
 */
public class AppsGateTrustManager  implements X509TrustManager {

    public void checkClientTrusted(X509Certificate[] cert, String authType) {
        // everything is trusted
    }

    public void checkServerTrusted(X509Certificate[] cert, String authType) {
        // everything is trusted
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
