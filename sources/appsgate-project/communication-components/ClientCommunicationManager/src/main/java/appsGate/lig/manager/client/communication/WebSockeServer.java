package appsGate.lig.manager.client.communication;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;

/*****************************************/
/**    Web socket server management    **/
/*****************************************/

/**
 * Inner class that hold web socket handling 
 * 
 * @author Cedric Gerard
 * @since August 5, 2014
 * @version 1.0.0
 */
public class WebSockeServer extends WebSocketServer {
	
	private static Logger logger = LoggerFactory.getLogger(WebSockeServer.class);

	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private final Logger socketLogger = LoggerFactory.getLogger(WebSockeServer.class);
	
	/**
	 * Default web socket port for default constructor call
	 */
	public final static int DEFAULT_WEBSOCKET_PORT = 8087;
	
	/**
	 * Default target for dedicated server
	 */
	public final static String DEFAULT_TARGET = "default";
	
	/**
	 * A human readable server name
	 */
	private String name;
	
	/**
	 * The lister list for components who subscribe for all client commands.
	 */
	private HashMap<String, CommandListener> commandListeners = new HashMap<String, CommandListener>();
	
	/**
	 * The default constructor initialize a web socket server on the
	 * default web socket port
	 * 
	 * @param name the name of the connection
	 * @throws UnknownHostException
	 * @see DEFAULT_WEBSOCKET_PORT
	 */
	public WebSockeServer(String name) throws UnknownHostException {
		super(new InetSocketAddress(DEFAULT_WEBSOCKET_PORT));
		this.name = name;
	}

	/**
	 * Manager named web socket connection on specified port
	 * 
	 * @param name the name of the connection
	 * @param port the port to listen 
	 * @throws UnknownHostException
	 */
	public WebSockeServer(String name, int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
		this.name = name;
	}

	@Override
	public void onClose(WebSocket sock, int code, String reason, boolean remote) {
		socketLogger.info("A client has closed his connection: "+ sock.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onError(WebSocket sock, Exception ex) {
		socketLogger.error( "An error occured on connection " + sock + ":" + ex.getMessage() );
	}

	@Override
	public void onMessage(WebSocket sock, String message) {
		socketLogger.debug("msg --> " + message);
		
		try {
			JSONTokener jsonParser = new JSONTokener(message);
			JSONObject jsObj = (JSONObject)jsonParser.nextValue();
			notifyCommandListeners(sock, jsObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onOpen(WebSocket sock, ClientHandshake cltHs) {
		socketLogger.info( "New client connected: " + sock.getRemoteSocketAddress() );
	}
	
	/**
	 * Add a listener to this socket
	 * @param cmdListener the new listener
	 * @param target a specified target if any, null for default (no target)
	 * @return true if the command listener is new, false if it has been replaced
	 */
	public boolean addListener(CommandListener cmdListener, String target) {
		return commandListeners.put(target, cmdListener) == null;
	}
	
	/**
	 * remove an existing target listener
	 * @param target targeted listener
	 * @return true if the listener has been removed, false otherwise
	 */
	public boolean removeListener(String target) {
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
			
			switch (this.getPort()) {
			
				case WebSockeServer.DEFAULT_WEBSOCKET_PORT:
					logger.debug("retrieving command listener for "+cmd.getString("TARGET")+" target.");
					cmd.put("clientId", socket.hashCode());
					CommandListener cmdListener = commandListeners.get(cmd.getString("TARGET"));
					logger.debug("Command listener {} found, invoking onReceivedCommand",cmdListener);

					if(cmdListener!=null){
						cmdListener.onReceivedCommand(cmd);
					}else {
						logger.debug("skipping command listener invocation duo to null listener");
					}

					logger.debug("Finished invoking listener {} for command {}.",cmdListener,cmd.toString());
					break;
					
				default:
					cmd.put("clientId", socket.hashCode());
					CommandListener listener = commandListeners.get(WebSockeServer.DEFAULT_TARGET);
					listener.onReceivedCommand(cmd);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a human readable name for this server
	 * @return the name as a String
	 */
	public String getName() {
		return name;
	}
	
}
