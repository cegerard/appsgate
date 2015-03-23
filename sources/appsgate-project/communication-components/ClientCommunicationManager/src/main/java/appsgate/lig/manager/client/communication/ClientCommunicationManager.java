package appsGate.lig.manager.client.communication;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
		for(WebSocketServer server : socketMap.values()){
			server.start();
			logger.info(server.getAddress()+ "web socket server started on port "+server.getPort());
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
			server.start();
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
	
	
}

