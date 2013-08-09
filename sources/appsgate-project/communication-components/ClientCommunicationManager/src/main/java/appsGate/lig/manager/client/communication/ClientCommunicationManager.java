package appsGate.lig.manager.client.communication;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.AddListenerService;
import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsGate.lig.manager.client.communication.service.subscribe.ConfigListener;


/**
 * This class is the manager for all web sockets connections. All external
 * applications will communicate with ApAM components through an instance of this class.
 * 
 * @author Cédric Gérard
 * @since February 8, 2013
 * @version 1.0.0
 * 
 * @provide AddListenerService to manage the subscription for receiving command from
 * AppsGate client.
 * @provide SendWebsocketsService to send messages to AppsGate client application from OSGi
 * environment.
 * 
 * @see AddListenerService
 * @see SendWebsocketsService
 *
 */
@Component(publicFactory=false)
@Instantiate(name="AppsgateClientCommunicationManager")
@Provides(specifications = { AddListenerService.class, SendWebsocketsService.class })
public class ClientCommunicationManager extends WebSocketApplication implements AddListenerService, SendWebsocketsService {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ClientCommunicationManager.class);
	
	/**
	 * HTTP service dependency resolve by iPOJO.
	 * Allow to register HTML resources to the Felix HTTP server 
	 */
	//private HttpService httpService;
	
	
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
//		// initialize web socket engine and register web socket java application
		WebSocketEngine.getEngine().register(this);
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
			WebSocketEngine.getEngine().unregister(this);
//		}
		logger.info("The communication manager is now stopped.");
	}

	@Override
	public boolean isApplicationRequest(HttpRequestPacket hrp) {
		logger.debug("isApplicationRequest: " + hrp);
		return true;
	}
	
	/**
	 * This method is call to create a new web socket.
	 * it is called by the web socket engine each time a client start a connection.
	 * 
	 * @param handler the Protocolhandler set by the web socket engine
	 * @param listeners the set of listeners for this sockets
	 */
	@Override
	public WebSocket createSocket(ProtocolHandler handler, WebSocketListener... listeners) {
		logger.debug("Create webSocket");
		return new DefaultWebSocket(handler, listeners);
	}
	
	/**
	 * Call back used to notify that a new message come from a connected client.
	 * 
	 * @param socket the socket that send the message
	 * @param cmd the message received
	 */
	@Override
	public void onMessage(WebSocket socket, String cmd) {
		logger.debug("msg --> " + cmd);
		
		try {
			JSONTokener jsonParser = new JSONTokener(cmd);
			JSONObject jsObj = (JSONObject)jsonParser.nextValue();
			@SuppressWarnings("rawtypes")
			Iterator keys = jsObj.keys();
			String command = keys.next().toString();

			if (command.contentEquals("setPairingMode")) {
				notifyConfigListeners(socket, cmd);
				
			} else if (command.contentEquals("sensorValidation")) {
				notifyConfigListeners(socket, cmd);
				
			} else if (command.contentEquals("getConfDevices")) {
				notifyConfigListeners(socket, cmd);
				
			} else if (command.contentEquals("createActuator")) { 
				notifyConfigListeners(socket, cmd);
				
			} else if (command.contentEquals("actuatorAction")) { 
				notifyConfigListeners(socket, cmd);
				
			} else {
				notifyCommandListeners(socket, cmd);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Call back called when a socket connection has closed.
	 * 
	 * @param socket the closed socket connection
	 * @param frame the dataFrame corresponding to the closed connection action
	 */
	@Override
	public void onClose(WebSocket socket, DataFrame frame) {
		super.onClose(socket, frame);
		logger.info("A client has closed his connection.");
	}

	/**
	 * Call back called when a new connection is opened
	 * 
	 * @param socket the new connected socket
	 * 
	 */
	@Override
	public void onConnect(WebSocket socket) {
		super.onConnect(socket);
		logger.info("new client connected.");
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
		Set<WebSocket> sockets = this.getWebSockets();
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
		Set<WebSocket> sockets = this.getWebSockets();
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
	
	/**
	 * Register a new command listener
	 * 
	 * @param cmdListener the command listener to add
	 */
	public boolean addCommandListener(CommandListener cmdListener) {
		logger.debug("New all command listener: "+cmdListener.toString());
		return commandListeners.add(cmdListener);
	}

	/**
	 * Register a new configuration listener
	 * 
	 * @param configCmdList the configuration listener to add
	 * 
	 */
	public boolean addConfigListener(ConfigListener configCmdList) {
		logger.debug("New config listener: "+configCmdList.toString());
		return configListeners.add(configCmdList);
	}
	
	/**
	 * notify all command listeners that new command or event is received
	 * @param socket the client connection
	 * @param cmd the command.
	 * @throws ParseException 
	 */
	private void notifyCommandListeners(WebSocket socket, String cmd) {
		logger.debug("notify listeners for new command event");
		
		try {
		
			JSONTokener jsonParser = new JSONTokener(cmd);
			JSONObject jsObj;
		
			jsObj = (JSONObject)jsonParser.nextValue();
			jsObj.put("clientId", socket.hashCode());
			
			Iterator<CommandListener> it = commandListeners.iterator();
			CommandListener allcmdListener;
		
			while(it.hasNext()){
				allcmdListener = it.next();
				allcmdListener.onReceivedCommand(jsObj);
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
	private void notifyConfigListeners(WebSocket socket, String cmd) {
		logger.debug("notify listeners for new configuration event");
		try {
			JSONTokener jsonParser = new JSONTokener(cmd);
			JSONObject jsObj = (JSONObject)jsonParser.nextValue();
			@SuppressWarnings("rawtypes")
			Iterator keys = jsObj.keys();
			String command = keys.next().toString();
			JSONObject value;
			value = jsObj.getJSONObject(command);
			value.put("clientId", socket.hashCode());
			
			Iterator<ConfigListener> it = configListeners.iterator();
			ConfigListener configCommandListener;
			
			while(it.hasNext()){
				configCommandListener = it.next();
				configCommandListener.onReceivedConfig(command, value);
			}
			
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
	ArrayList<ConfigListener> configListeners = new ArrayList<ConfigListener>();
	
}

