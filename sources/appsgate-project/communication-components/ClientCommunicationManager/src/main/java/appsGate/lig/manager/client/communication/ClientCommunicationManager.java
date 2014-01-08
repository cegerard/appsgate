package appsGate.lig.manager.client.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsGate.lig.manager.client.communication.service.subscribe.ConfigListener;


/**
 * This class is the manager for all web sockets connections. All external
 * applications will communicate with ApAM components through an instance of this class.
 * 
 * @author Cédric Gérard
 * @since February 8, 2013
 * @version 1.1.0
 * 
 * @provide AddListenerService to manage the subscription for receiving command from
 * AppsGate client.
 * @provide SendWebsocketsService to send messages to AppsGate client application from OSGi
 * environment.
 * 
 * @see ListenerService
 * @see SendWebsocketsService
 *
 */
@Component(publicFactory=false)
@Instantiate(name="AppsgateClientCommunicationManager")
@Provides(specifications = { ListenerService.class, SendWebsocketsService.class })
public class ClientCommunicationManager extends WebSocketServer implements ListenerService, SendWebsocketsService {
	
	public final static int DEFAULT_WEBSOCKET_PORT = 8087;
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ClientCommunicationManager.class);
	
	/**
	 * HTTP service dependency resolve by iPOJO.
	 * Allow to register HTML resources to the Felix HTTP server 
	 */
	//private HttpService httpService;
	
	public ClientCommunicationManager() throws UnknownHostException {
		super(new InetSocketAddress(DEFAULT_WEBSOCKET_PORT));
	}
	
	/**
	 * Called by iPOJO when an instance of this implementation is created
	 */
	@Validate
	public void newInst() {
		logger.info("initiating the web communication manager...");
//		if (httpService != null) {
//			final HttpContext httpContext = httpService.createDefaultHttpContext();
//			final Dictionary<String, String> initParams = new Hashtable<String, String>();
//			initParams.put("from", "HttpService");
//			try {
//				// register HTML pages as resources
//				httpService.registerResources("/", "WEB/", httpContext);
//			} catch (NamespaceException ex) {
//				logger.error("NameSpace exception");
//			}
//		}
		// start the web socket server
		this.start();
		logger.info("The communication manager is ready.");
	}
	
	/**
	 * Called by iPOJO when an instance of this implementation is removed
	 */
	@Invalidate
	public void deleteInst() {
		logger.info("Releasing the communication manager...");
//		if (httpService != null) {
//			httpService.unregister("/");
			try {
				this.stop();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		}
		logger.info("The communication manager is now stopped.");
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		logger.info( "New client connected: " + conn.getRemoteSocketAddress() );
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		logger.info("A client has closed his connection: "+ conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		logger.debug("msg --> " + message);
		
		try {
			JSONTokener jsonParser = new JSONTokener(message);
			JSONObject jsObj = (JSONObject)jsonParser.nextValue();
			
			if(isConfiguration(jsObj)) {
				notifyConfigListeners(conn, jsObj);
			}else {
				notifyCommandListeners(conn, jsObj);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		logger.error( "An error occured on connection " + conn + ":" + ex );
	}
	
	/**
	 * Test if the JSON message is a configuration message
	 * @param msg the message a a JSON object
	 * @return true if the message is a configuration message, false otherwise
	 */
	private boolean isConfiguration(JSONObject msg) {
		try{
			msg.getString("CONFIGURATION");
			return true;
		}catch(JSONException e) {
			return false;
		}
	}
	
	/**
	 * Get the HTTP service form OSGi/iPOJO. This service is required.
	 * 
	 * @param httpService
	 *            , the HTTP OSGi service
	 */
//	@Bind(optional = false)
//	public void bindHttpService(HttpService httpService) {
//		this.httpService = httpService;
//		logger.debug("httpService service dependency resolved");
//	}

	/**
	 * Call when the communication manager release the required HTTP service.
	 * 
	 * @param httpService
	 *            , the released HTTP OSGi service
	 */
//	@Unbind(optional = false)
//	public void unbindHttpService(HttpService httpService) {
//		this.httpService = null;
//		logger.debug("httpService service dependency not available");
//	}
	
	/*****************************************/
	/** Send message service implementation **/
	/*****************************************/
	
	/**
	 * Send service for JSON object
	 * 
	 * @param key the command form AppsGate communication protocol
	 * @param msg the JSONObject message
	 */
	public void send(String key, JSONObject msg) {
		
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.send(jsonResponse.toString());
	}
	
	/**
	 * Send service for JSON Array
	 * 
	 * @param key the command form AppsGate communication protocol
	 * @param msg the JSONArray message
	 */
	public void send(String key, JSONArray msg) {
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
 			e.printStackTrace();
		}
		this.send(jsonResponse.toString());
	}
	
	/**
	 * The send method with a specific command and parameters to the specify client
	 * 
	 * @param clientId the targeted client identifier
	 * @param key the notification or command response
	 * @param msg parameters corresponding to the cmd command
	 */
	public void send(int clientId, String key, JSONObject msg) {
		
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.send(clientId, jsonResponse.toString());
	}
	
	/**
	 * The send method with a specific command and parameters to the specify client
	 * 
	 * @param clientId the targeted client identifier
	 * @param key the notification or command response
	 * @param msg parameters corresponding to the cmd command
	 */
	public void send(int clientId, String key, JSONArray msg) {
		
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.send(clientId, jsonResponse.toString());
	}
	
	/**
	 * Send a message to all connected clients
	 * 
	 * @param msg the string message to send
	 */
	public void send(String msg) {
		logger.debug("retreiving web sockets...");
		Collection<WebSocket> sockets = this.connections();
		Iterator<WebSocket> socketIt = sockets.iterator();
		WebSocket sock;
		while (socketIt.hasNext()) {
			sock = socketIt.next();
			sock.send(msg);
			logger.debug("message sent.");
		}
	}
	
	/**
	 * Send the a message to only one client identify by the web socket connection
	 * 
	 * @param clientId clientId the client identifier
	 * @param msg the string message to send
	 */
	public void send(int clientId, String msg) {
		Collection<WebSocket> sockets = this.connections();
		Iterator<WebSocket> socketIt = sockets.iterator();
		WebSocket sock = null;
		boolean found = false;
		
		while (socketIt.hasNext() && !found) {
			sock = socketIt.next();
			if (sock.hashCode() == clientId)
				found = true;
		}
		
		if(found) {
			sock.send(msg);
			logger.debug("message sent.");
		}
	}
	
	/*****************************************/
	/** Add listener service implementation **/
	/*****************************************/
	
	@Override
	public boolean addCommandListener(CommandListener cmdListener) {
		logger.debug("New all command listener: "+cmdListener.toString());
		return commandListeners.add(cmdListener);
	}


	@Override
	public boolean addConfigListener(String target, ConfigListener configCmdList) {
		logger.debug("New config listener: "+configCmdList.toString()+" for target: "+target);
		return configListeners.put(target, configCmdList) == null;
	}
	
	@Override
	public boolean removeConfigListener(String target) {
		logger.debug("removing config listener: "+target);
		return configListeners.remove(target) != null;
	}
	
	/**
	 * notify all command listeners that new command or event is received
	 * @param socket the client connection
	 * @param cmd the command.
	 * @throws ParseException 
	 */
	private void notifyCommandListeners(WebSocket socket, JSONObject cmd) {
		logger.debug("notify listeners for new command event");
		
		try {
			cmd.put("clientId", socket.hashCode());
			
			Iterator<CommandListener> it = commandListeners.iterator();
			CommandListener allcmdListener;
		
			while(it.hasNext()){
				allcmdListener = it.next();
				allcmdListener.onReceivedCommand(cmd);
			}
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * notify all configuration listener that new event is received
	 * @param socket the client connection
	 * @param cmd the command.
	 * @throws ParseException 
	 */
	private void notifyConfigListeners(WebSocket socket, JSONObject cmd) {
		logger.debug("notify listeners for new configuration event");
		try {
			String target = cmd.getString("TARGET");
			String command = cmd.getString("CONFIGURATION");
			JSONObject value = cmd.getJSONObject(command);
			
			value.put("clientId", socket.hashCode());
			
			ConfigListener listener = configListeners.get(target);
			listener.onReceivedConfig(command, value);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The lister list for components who subscribe for all client commands.
	 */
	ArrayList<CommandListener> commandListeners = new ArrayList<CommandListener>();
	
	/**
	 * The lister list for components who subscribe for configuration commands.
	 */
	HashMap<String, ConfigListener> configListeners = new HashMap<String, ConfigListener>();

	
}

