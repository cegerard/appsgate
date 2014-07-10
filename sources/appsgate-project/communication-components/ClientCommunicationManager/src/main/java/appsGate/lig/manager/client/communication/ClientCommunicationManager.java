package appsGate.lig.manager.client.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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
public class ClientCommunicationManager extends WebSocketServer implements ListenerService, SendWebsocketsService {
	
	public final static int DEFAULT_WEBSOCKET_PORT = 8087;
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ClientCommunicationManager.class);
	
	public ClientCommunicationManager() throws UnknownHostException {
		super(new InetSocketAddress(DEFAULT_WEBSOCKET_PORT));
	}
	
	/**
	 * Called by ApAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("initiating the web communication manager...");
		// start the web socket server
		this.start();
		logger.info("The communication manager is ready.");
	}
	
	/**
	 * Called by ApAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Releasing the communication manager...");
		try {
			this.stop();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
			notifyCommandListeners(conn, jsObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		logger.error( "An error occured on connection " + conn + ":" + ex );
	}
	
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
	public boolean addCommandListener(CommandListener cmdListener, String target) {
		logger.debug("New "+target+" command listener: "+cmdListener.toString());
		return commandListeners.put(target, cmdListener) == null;
	}
	
	@Override
	public boolean removeCommandListener(String target) {
		return commandListeners.remove(target) != null;
	}

	/**
	 * notify command listeners that new command or event is received
	 * @param socket the client connection
	 * @param cmd the command.
	 * @throws ParseException 
	 */
	private void notifyCommandListeners(WebSocket socket, JSONObject cmd) {
		logger.debug("notify listeners for new command event");
		try {
			logger.debug("retrieving command listener for "+cmd.getString("TARGET")+" target.");
			cmd.put("clientId", socket.hashCode());
			CommandListener cmdListener = commandListeners.get(cmd.getString("TARGET"));
            logger.debug("Command listener {} found, invoking onReceivedCommand",cmdListener);

            if(cmdListener!=null){
                logger.debug("skipping command listener invocation");
                cmdListener.onReceivedCommand(cmd);
            }


            logger.debug("Finished invoking listener {} for command {}.",cmdListener,cmd.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The lister list for components who subscribe for all client commands.
	 */
	HashMap<String, CommandListener> commandListeners = new HashMap<String, CommandListener>();
}

