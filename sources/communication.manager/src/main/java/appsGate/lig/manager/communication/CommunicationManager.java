package appsGate.lig.manager.communication;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

import appsGate.lig.manager.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.communication.service.subscribe.AddListenerService;
import appsGate.lig.manager.communication.service.subscribe.CommandListener;
import appsGate.lig.manager.communication.service.subscribe.ConfigListener;


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
@Component
@Instantiate
@Provides(specifications = { AddListenerService.class, SendWebsocketsService.class })
public class CommunicationManager extends WebSocketApplication implements AddListenerService, SendWebsocketsService {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(CommunicationManager.class);
	
	
	/**
	 * A JSON parser, for interpret received messages.
	 */
	JSONParser jsonParser = new JSONParser();
	
	/**
	 * HTTP service dependency resolve by iPOJO.
	 * Allow to register HTML resources to the Felix HTTP server 
	 */
	private HttpService httpService;
	
	
	/**
	 * Called by iPOJO when an instance of this implementation is created
	 */
	@Validate
	public void newInst() {
		logger.info("initiating the web communication manager...");
		if (httpService != null) {
			final HttpContext httpContext = httpService.createDefaultHttpContext();
			final Dictionary<String, String> initParams = new Hashtable<String, String>();
			initParams.put("from", "HttpService");
			try {
				// register HTML pages as resources
				httpService.registerResources("/", "WEB/", httpContext);
			} catch (NamespaceException ex) {
				logger.error("NameSpace exception");
			}
		}
		// initialize web socket engine and register web socket java application
		WebSocketEngine.getEngine().register(this);
		logger.info("The communication manager is ready.");
	}
	
	/**
	 * Called by iPOJO when an instance of this implementation is removed
	 */
	@Invalidate
	public void deleteInst() {
		logger.info("Releasing the communication manager...");
		if (httpService != null) {
			httpService.unregister("/");
			WebSocketEngine.getEngine().unregister(this);
		}
		logger.info("The communication manager is now stopped.");
	}

	@Override
	public boolean isApplicationRequest(HttpRequestPacket hrp) {
		logger.debug("isApplicationRequest: " + hrp);
		return true;
	}
	
	@Override
	public WebSocket createSocket(ProtocolHandler handler, WebSocketListener... listeners) {
		logger.debug("Create webSocket");
		return new DefaultWebSocket(handler, listeners);
	}
	
	@Override
	public void onMessage(WebSocket socket, String cmd) {
		logger.debug("msg --> " + cmd);
		
		try {
			
			JSONObject jsObj = (JSONObject)jsonParser.parse(cmd);
			Set<?> keys = jsObj.keySet();
			String command = keys.iterator().next().toString();

			if (command.contentEquals("setPairingMode")) {
				notifyConfigListeners(socket, cmd);
				
			} else if (command.contentEquals("sensorValidation")) {
				notifyConfigListeners(socket, cmd);
				
			} else if (command.contentEquals("getConfDevices")) {
				notifyConfigListeners(socket, cmd);
				
			} else {
				notifyCommandListeners(socket, cmd);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(WebSocket socket, DataFrame frame) {
		super.onClose(socket, frame);
		logger.info("A client has closed his connection.");
	}

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
	@Bind(optional = false)
	public void bindHttpService(HttpService httpService) {
		this.httpService = httpService;
		logger.debug("httpService service dependency resolved");
	}

	/**
	 * Call when the communication manager release the required HTTP service.
	 * 
	 * @param httpService
	 *            , the released HTTP OSGi service
	 */
	@Unbind(optional = false)
	public void unbindHttpService(HttpService httpService) {
		this.httpService = null;
		logger.debug("httpService service dependency not available");
	}
	
	/*****************************************/
	/** Send message service implementation **/
	/*****************************************/
	
	/**
	 * send service
	 */
	@SuppressWarnings("unchecked")
	public void send(String cmd, JSONObject msg) {
		
		JSONObject jsonResponse =  new JSONObject();
		jsonResponse.put(cmd, msg);
		this.send(jsonResponse.toJSONString());
	}
	
	@SuppressWarnings("unchecked")
	public void send(String cmd, JSONArray msg) {
		JSONObject jsonResponse =  new JSONObject();
		jsonResponse.put(cmd, msg);
		this.send(jsonResponse.toJSONString());
	}
	
	/**
	 * Send the msg message to all connected clients
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
	 * Send the msg message to only one client identify by the sock connection
	 * 
	 * @param sock the client socket
	 * @param msg the string message to send
	 */
	public void send(WebSocket sock, String msg) {
			sock.send(msg);
			logger.debug("message sent.");
	}
	
	/*****************************************/
	/** Add listener service implementation **/
	/*****************************************/
	public boolean addCommandListener(CommandListener cmdListener) {
		logger.debug("New all command listener: "+cmdListener.toString());
		return commandListeners.add(cmdListener);
	}

	public boolean addConfigListener(ConfigListener configcmdList) {
		logger.debug("New config listener: "+configcmdList.toString());
		return configListeners.add(configcmdList);
	}
	
	/**
	 * notify all command listeners that new command or event is received
	 * @param socket, the client connection
	 * @param cmd, the command.
	 * @throws ParseException 
	 */
	private void notifyCommandListeners(WebSocket socket, String cmd) throws ParseException {
		logger.debug("notify listeners for new command event");
		JSONObject jsObj = (JSONObject)jsonParser.parse(cmd);
		
		Iterator<CommandListener> it = commandListeners.iterator();
		CommandListener allcmdListener;
		
		while(it.hasNext()){
			allcmdListener = it.next();
			allcmdListener.onReceivedCommand(jsObj);
		}
	}
	
	/**
	 * notify all configuration listener that new event is received
	 * @param socket, the client connection
	 * @param cmd, the command.
	 * @throws ParseException 
	 */
	private void notifyConfigListeners(WebSocket socket, String cmd) throws ParseException {
		logger.debug("notify listeners for new configuration event");
		JSONObject jsObj = (JSONObject)jsonParser.parse(cmd);
		Set<?> keys = jsObj.keySet();
		String command = keys.iterator().next().toString();
		JSONObject value = (JSONObject)jsObj.get(command);
		
		Iterator<ConfigListener> it = configListeners.iterator();
		ConfigListener configCommandListener;
		
		while(it.hasNext()){
			configCommandListener = it.next();
			configCommandListener.onReceivedConfig(command, value);
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

