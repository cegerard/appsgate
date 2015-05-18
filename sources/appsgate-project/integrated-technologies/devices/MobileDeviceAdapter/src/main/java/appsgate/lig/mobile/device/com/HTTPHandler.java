package appsgate.lig.mobile.device.com;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Bidois Morgan on 23/04/15.
 */
public class HTTPHandler {
    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger logger = LoggerFactory.getLogger(HTTPHandler.class);

    private static String addr;
    private static String port;
    private MyHTTPD HTTPDServer;

    public HTTPHandler(String address) {
        // Initialize with defaut port, will be set after
        this.addr = address;
    }

    public void setInformation(String addr, String port) {
        this.addr = addr;
        this.port = port;
    }

    public void initializeHTTPServer() {
//        this.HTTPDServer = new MyHTTPD(this.addr, this.port);
        this.HTTPDServer = new MyHTTPD(null, this.port);
    }

    public void start() {
        try {
            this.HTTPDServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.HTTPDServer.closeAllConnections();
        this.HTTPDServer.stop();
    }

    private static final class MyHTTPD extends NanoHTTPD {

        public MyHTTPD(String addr, String port) {
            super(addr, Integer.parseInt(port));
        }

        @Override
        public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method
                method, Map<String, String> headers, Map<String, String> parms,
                                        Map<String, String> files) {
            final StringBuilder buf = new StringBuilder();

            buf.append("Header<br>");
            for (Map.Entry<String, String> kv : headers.entrySet()) {
                buf.append(kv.getKey() + " : " + kv.getValue() + "\n");
            }

            buf.append("<br>----<br>");

            buf.append("method = " + method + "<br>");
            buf.append("uri = " + uri + "<br>");

            buf.append("Params<br>");
            for (Map.Entry<String, String> p : parms.entrySet()) {
                buf.append(p.getKey() + " : " + p.getValue() + "<br>");
            }

            final String html = "<html><head><head><body><h1>Hello, World</h1></body>" + buf + "</html>";

//            TaskerPlugin.Event.addPassThroughMessageID(BackgroundService.INTENT_REQUEST_REQUERY);

            HashMap<String, String> mapBis = new HashMap<>(parms);
            mapBis.remove("NanoHttpd.QUERY_STRING");

            JSONObject o = new JSONObject();
            for (Map.Entry<String, String> param : mapBis.entrySet()) {
                try {
                    o.put(param.getKey(), param.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Adding HTTP Info
            try {
                o.put("httpAddr", addr);
                o.put("httpPort", port);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            logger.debug("JSON envoyé : " + o.toString());
//            Bundle dataBundle = PluginBundleManager.generateURLBundle(context, o.toString());
//            TaskerPlugin.Event.addPassThroughData(BackgroundService.INTENT_REQUEST_REQUERY, dataBundle);
//            context.sendBroadcast(BackgroundService.INTENT_REQUEST_REQUERY);

            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, MIME_HTML, html);
        }

    }
}



