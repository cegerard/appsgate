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
 * @version 1.9.0
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
public class ClientCommunicationManager implements ListenerService, SendWebsocketsService {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ClientCommunicationManager.class);
	
	/**
	 * Web socket servers map. the key is a human readable name 
	 */
	private HashMap<String, WebSockeServer> socketMap;
	
	public final static String DEFAULT_SERVER_NAME = "default";
	
	public ClientCommunicationManager() {
		socketMap = new HashMap<String, WebSockeServer>();
		try {
			socketMap.put(DEFAULT_SERVER_NAME, new WebSockeServer(DEFAULT_SERVER_NAME));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			logger.error(e.getMessage()+" default web socket on port "+WebSockeServer.DEFAULT_WEBSOCKET_PORT+" NOT initialized");
		}
	}
	
	/**
	 * Called by ApAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("initiating the web communication manager...");
		// start all web socket servers
		for(WebSockeServer server : socketMap.values()){
			server.start();
			logger.info(server.getName()+ "web socket server started on port "+server.getPort());
		}
		logger.info("The communication manager is ready.");
	}
	
	/**
	 * Called by ApAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Releasing the communication manager...");
		try {
			for(WebSockeServer server : socketMap.values()){
				server.stop();
				logger.info(server.getName()+ "web socket server stopped");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("The communication manager is now stopped.");
	}

	/*****************************************/
	/** Send message service implementation **/
	/*****************************************/
	
	@Override
	public void send(String key, JSONObject msg) {
		
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.send(jsonResponse.toString());
	}
	
	@Override
	public void send(String key, JSONArray msg) {
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
 			e.printStackTrace();
		}
		this.send(jsonResponse.toString());
	}
	
	@Override
	public void send(int clientId, String key, JSONObject msg) {
		
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.send(clientId, jsonResponse.toString());
	}
	
	@Override
	public void send(int clientId, String key, JSONArray msg) {
		
		JSONObject jsonResponse =  new JSONObject();
		try {
			jsonResponse.put(key, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.send(clientId, jsonResponse.toString());
	}
	
	@Override
	public void send(String msg) {
		logger.debug("retreiving web sockets...");
		WebSocketServer server = socketMap.get(DEFAULT_SERVER_NAME);
		Collection<WebSocket> sockets = server.connections();
		Iterator<WebSocket> socketIt = sockets.iterator();
		WebSocket sock;
		while (socketIt.hasNext()) {
			sock = socketIt.next();
			sock.send(msg);
			logger.debug("message sent.");
		}
	}
	

	@Override
	public void send(int clientId, String msg) {
		WebSocketServer server = socketMap.get(DEFAULT_SERVER_NAME);
		Collection<WebSocket> sockets = server.connections();
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
	
	@Override
	public void sendTo(String name, String msg) {
		logger.debug("retreiving web socket server...");
		WebSocketServer server = socketMap.get(name);
		for(WebSocket sock : server.connections()) {
			sock.send(msg);
			logger.debug("message sent.");
		}
	}

	@Override
	public void sendTo(String name, int clientId, String msg) {
		logger.debug("retreiving web socket server...");
		WebSocketServer server = socketMap.get(name);
		for(WebSocket sock : server.connections()) {
			if (sock.hashCode() == clientId) {
				sock.send(msg);
				logger.debug("message sent.");
				break;
			}
		}
	}
	
	/*****************************************/
	/** Add listener service implementation **/
	/*****************************************/
	
	@Override
	public boolean addCommandListener(CommandListener cmdListener, String target) {
		logger.debug("New "+target+" command listener: "+cmdListener.toString());
		WebSockeServer server = socketMap.get(DEFAULT_SERVER_NAME);
		return server.addListener(cmdListener, target);
	}
	
	@Override
	public boolean removeCommandListener(String target) {
		WebSockeServer server = socketMap.get(DEFAULT_SERVER_NAME);
		return server.removeListener(target);
	}

	@Override
	public boolean createDedicatedServer(CommandListener cmdListener, String name, int port) {
		logger.info("Create a new dedicated connection, "+name+" on  port "+port);
		
		if(socketMap.containsKey(name)){
			logger.error("The connection name, "+name+", is already used");
			return false;
		}
		
		try {
			WebSockeServer server = new WebSockeServer(name, port);
			socketMap.put(name, server);
			return server.addListener(cmdListener, WebSockeServer.DEFAULT_TARGET);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			logger.error(e.getMessage()+" web socket on port "+port+" NOT initialized");
			return false;
		}
	}

	@Override
	public boolean removeDedicatedServer(String name) {
		
		WebSockeServer server = socketMap.get(name);
		server.removeListener(WebSockeServer.DEFAULT_TARGET);
		
		try {
			
			server.stop();
			return socketMap.remove(name) != null;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
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
	private class WebSockeServer extends WebSocketServer {
		
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
}

