package appsgate.lig.mobile.device.com;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Bidois Morgan on 23/04/15.
 */
public class SocketTasker {

    private final static Logger LOGGER = LoggerFactory.getLogger(SendMessageSocket.class);

    private Socket socket;

    private String host = Configuration.HOST;
    private String httpAddr;
    private String httpPort;

    private final JSONObject socketStateCreation = new JSONObject();
    private final JSONObject socketStateConnection = new JSONObject();
    private final JSONObject socketStateDisconnection = new JSONObject();

    private Timer timer;
    private PingSocketTask pingSocTask;

    /**
     *
     */
    public SocketTasker() {
        this.trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }
        }};
        try {
            // Initialize socket with the host
            this.socket = IO.socket("http://" + Configuration.HOST);
            this.configureSocket(socket);
            this.createSockeStateMsg();

        } catch (URISyntaxException e) {
            LOGGER.error("Unable to parse URI");

        }
    }

    private void createSockeStateMsg() {
        try {
            socketStateCreation.put("creation", true);
            socketStateCreation.put("socketstate", "creation");
            socketStateConnection.put("connection", true);
            socketStateConnection.put("socketstate", "connection");
            socketStateDisconnection.put("disconnection", true);
            socketStateDisconnection.put("socketstate", "disconnection");
        } catch (JSONException e) {
        }
    }

    public boolean setServer(String host) {
        /**
         * SSL *
         */
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("No such algorithm found");
            return false;
        }
        try {
            sc.init(null, trustAllCerts, new SecureRandom());
        } catch (KeyManagementException e) {
            LOGGER.error("Unable to init the ssl context");
            LOGGER.debug(e.getMessage());
            return false;
        }
        IO.setDefaultSSLContext(sc);
        HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

        // socket options
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;

        opts.secure = host.split(":")[0].equals("https");
        opts.sslContext = sc;

        if (this.host == null) {
            this.host = host;
            try {
                this.socket = IO.socket(this.host, opts);
                this.configureSocket(socket);
            } catch (URISyntaxException e) {
                LOGGER.error("URI is not well formed: {}", this.host);
                return false;
            }
        } else {
            if (this.socket.connected()) {
                this.socket.disconnect();
            }
            try {
                this.socket = IO.socket(this.host, opts);
                this.configureSocket(socket);
            } catch (URISyntaxException e) {
                LOGGER.error("URI is not well formed: {}", this.host);
                return false;
            }
        }
        return true;
    }

    public void resetServer() {
        this.setServer(this.host);
    }

    public void addHTTPInfo(String addr, String port) {
        this.httpAddr = addr;
        this.httpPort = port;
    }

    public void connect() {
        if (!this.socket.connected()) {
            this.socket.connect();
        }
    }

    public void identifyAndSubscribe() {
        this.connect();
        this.identify(Configuration.LOGIN, Configuration.PASSWORD);

        JSONObject subscribe = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            subscribe.put("id", "all");
            data.put("title", ".*");
            data.put("regexp", true);
            subscribe.put("data", data);
        } catch (JSONException e) {
        }
        this.socket.emit("subscribe", subscribe);
    }

    /**
     * TODO A mettre dans interface
     *
     * @param msg
     */
    private void sendMsgToTasker(JSONObject msg) {
        LOGGER.trace("Message receive from Tasker: {}", msg.toString());
        
    }

    public void disconnect() {
        this.socket.disconnect();
    }

    public void close() {
        this.socket.close();
    }

    public boolean isConnected() {
        return this.socket.connected();
    }

    private void identify(String login, String pass) {
        JSONObject loginObject = new JSONObject();
        try {
            loginObject.put("login", login);
            loginObject.put("pass", pass);
        } catch (JSONException e) {
        }

        this.socket.emit("login", loginObject);
    }

    private void configureSocket(final Socket socket) {
        socket.off();
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOGGER.debug("Socket IO " + socket.id() + " -> Connect "); //$NON-NLS-1$
                identifyAndSubscribe();                timer = new Timer();
                if (pingSocTask == null) {
                    pingSocTask = new PingSocketTask(socket);
                    timer.schedule(pingSocTask, 0, 20000);
                }
            }

        }).on("all", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOGGER.debug("Socket IO -> Event handle "); //$NON-NLS-1$
                JSONObject o;
                try {
                    o = (JSONObject) args[0];
                } catch (ClassCastException ex) {
                    o = new JSONObject();
                    try {
                        o.put("error", "malformated message");
                    } catch (JSONException e) {
                    }
                }
                // Adding HTTP Info
                try {
                    o.put("httpAddr", httpAddr);
                    o.put("httpPort", httpPort);
                } catch (JSONException e) {
                }

                LOGGER.debug("JSON reçu transféré to tasker : " + o.toString());
                sendMsgToTasker(o);
            }
        }).on("ping", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOGGER.debug("PONG");
                socket.emit("pong");
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOGGER.debug("Socket IO -> Connect Error "); //$NON-NLS-1$
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOGGER.debug("Socket IO -> Disconnect "); //$NON-NLS-1$
                // Disconnection Socket message
                LOGGER.debug("JSON disconnection socket : " + socketStateDisconnection.toString());

                if (timer != null) {
                    pingSocTask.cancel();
                    pingSocTask = null;
                    timer.cancel();
                    timer = null;
                }
            }

        }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOGGER.debug("Socket IO -> Reconnection "); //$NON-NLS-1$
                identifyAndSubscribe();

                timer = new Timer();
                pingSocTask = new PingSocketTask(socket);
                timer.schedule(pingSocTask, 0, 20000);
            }

        });

        // Creation Socket message
        LOGGER.debug("JSON creation socket : " + socketStateCreation.toString());
    }

    public void socketOff() {
        this.socket.off();
    }

    private class PingSocketTask extends TimerTask {

        private final Socket socket;

        public PingSocketTask(Socket soc) {
            this.socket = soc;
        }

        @Override
        public void run() {
            LOGGER.debug("Ping!");
            socket.emit("ping", true);
        }
    }

    /**
     * * SSL **
     */
    public static class RelaxedHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private TrustManager[] trustAllCerts;

}
