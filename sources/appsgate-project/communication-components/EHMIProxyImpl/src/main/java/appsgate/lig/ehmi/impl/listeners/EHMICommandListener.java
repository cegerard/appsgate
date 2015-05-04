package appsgate.lig.ehmi.impl.listeners;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.ehmi.exceptions.CoreDependencyException;
import appsgate.lig.ehmi.impl.EHMIProxyImpl;

/**
 * Listener for EHMI commands and events from clients.
 * 
 * @author Cédric Gérard
 * @since April 22, 2014
 * @version 1.0.0
 * 
 */
public class EHMICommandListener implements CommandListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EHMICommandListener.class);
	
	/**
	 * Service that launch method call in a dedicated thread pool
	 */
	private ScheduledExecutorService executorService;

	/**
	 * The parent proxy of this listener
	 */
	private EHMIProxyImpl ehmiProxy;

	/**
	 * Constructor to initialize the listener with its parent
	 * 
	 * @param router the parent of this listener
	 */
	public EHMICommandListener(EHMIProxyImpl ehmiProxy) {
		this.ehmiProxy = ehmiProxy;
		executorService = Executors.newScheduledThreadPool(10);
	}

	/**
	 * This is the handler for command events from client.
	 * 
	 * @param obj The JSON object description for the cmd parameter
	 */
	@Override
	public void onReceivedCommand(JSONObject obj) {
		logger.trace("onReceivedCommand(JSONObject obj : {})", obj.toString());
		try {
			int clientId=-1;
			String method = obj.getString("method");
			JSONArray args = obj.getJSONArray("args");
			String callId = null;

			
			try {
				if(obj.has("callId")) {
					callId = obj.getString("callId");
					logger.debug("method with return call");
				} else {
					logger.debug("method without return");
				}
				
				if(obj.has("clientId")) {
					clientId = obj.getInt("clientId");
					logger.debug("method with clientId");
				} else {
					logger.debug("method without clientId (won't return to a client using webSocket)");
				}
				
				if(!obj.has("objectId")){
					logger.trace("onReceivedCommand(), obj does not have object Id");

					if("EHMI".equals(obj.optString("TARGET")) ) {
						logger.trace("onReceivedCommand(), TARGET is EHMI ");
						executorService.execute(new GenericCommand(args, ehmiProxy, null, method,callId, clientId, ehmiProxy));
					} else if("CHMI".equals(obj.optString("TARGET")) ) {
						logger.trace("onReceivedCommand(), TARGET is CHMI ");
						executorService.execute(ehmiProxy.executeRemoteCommand("proxy", method, args, clientId, callId));
					} 
				}else{
					logger.trace("onReceivedCommand(), obj have objectId : "+obj.getString("objectId"));
					executorService.execute(ehmiProxy.executeRemoteCommand(obj.getString("objectId"), method, args, clientId, callId));
				}
			} catch (IllegalArgumentException e) {
				logger.debug("Inappropriate argument: " + e.getMessage());
			} catch(CoreDependencyException coreException) {
				String objId = obj.getString("objectId");
			}
		} catch (JSONException e1) {
			logger.error(e1.getMessage());
		}
		
	}
}
