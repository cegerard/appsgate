package appsgate.lig.mobile.device.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

public final class SendMessageSocket {

    private final static Logger LOGGER = LoggerFactory.getLogger(SendMessageSocket.class);

    private final String USER_AGENT = "Mozilla/5.0";

    private final String url = "http://" + Configuration.HOST + "/broadcast";

    // HTTP POST request
    public boolean sendPost(String title, String msg) {

        URL obj;
        try {
            obj = new URL(url);
        } catch (MalformedURLException ex) {
            return false;
        }
        HttpURLConnection con;
        String urlParameters = getMessage(title, msg);

        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            //add reuqest header

            LOGGER.debug("\nSending 'POST' request to URL : " + url);
            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }
            int responseCode = con.getResponseCode();
            LOGGER.debug("Post parameters : " + urlParameters);
            LOGGER.debug("Response Code : " + responseCode);

        } catch (IOException ex) {
            return false;
        }

        return true;

    }

    /**
     *
     * @param title
     * @param msg
     * @return
     */
    private String getMessage(String title, String msg) {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("login", Configuration.LOGIN);
        urlParameters.put("pass", Configuration.PASSWORD);
        urlParameters.put("title", title);
        urlParameters.put("message", msg);

        StringBuilder result = new StringBuilder();
        for (String key : urlParameters.keySet()) {
            result.append(key).append("=").append(urlParameters.get(key)).append("&");
        }
        return result.toString();
    }

}
