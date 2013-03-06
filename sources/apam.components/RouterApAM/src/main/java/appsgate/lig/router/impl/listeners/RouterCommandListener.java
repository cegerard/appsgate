package appsgate.lig.router.impl.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.communication.service.subscribe.CommandListener;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
import appsgate.lig.router.impl.RouterImpl;

/**
 * Listener for all commands and events from clients.
 * 
 * @author Cédric Gérard
 * @since February 15, 2013
 * @version 1.0.0
 * 
 */
public class RouterCommandListener implements CommandListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(RouterCommandListener.class);

	/**
	 * The parent proxy of this listener
	 */
	private RouterImpl router;

	/**
	 * Constructor to initialize the listener with its parent
	 * 
	 * @param enoceanProxy
	 *            , the parent of this listener
	 */
	public RouterCommandListener(RouterImpl router) {
		this.router = router;
	}

	/**
	 * This is the handler for command events from client.
	 * 
	 * @param obj
	 *            , The JSON object description for the cmd parameter
	 */
	@Override
	public void onReceivedCommand(JSONObject obj) {
		logger.debug("Client send : " + obj.toJSONString());
		String targetTypeTemp = (String)obj.get("targetType");
		int targetType = Integer.parseInt(targetTypeTemp);
		
		switch (targetType) {
		
		case 0: // Router level
			logger.debug("Router level message");
			String cmd = (String)obj.get("commandName");
			
			if (cmd.equalsIgnoreCase("getDevices")) {
				router.getDevices("clientID");	
			}
			
			break;

		case 1:// Abstract object level
			logger.debug("Abstract object level message");
			String method = (String) obj.get("method");
			String id = (String) obj.get("objectId");
			JSONArray args = (JSONArray) obj.get("args");
			
			ArrayList<Object> arguments = new ArrayList<Object>();

			try {
				// Get all arguments types and values
				@SuppressWarnings("unchecked")
				Iterator<JSONObject> it = args.iterator();
				JSONObject JSONObj;
				String value, type;
				while (it.hasNext()) {
					JSONObj = it.next();
					value = (String) JSONObj.get("value");
					type = (String) JSONObj.get("type");
					addArguments(type, value, arguments);
				}
			} catch (IllegalArgumentException e) {
				logger.debug("Inappropriate argument: " + e.getMessage());
			} catch (ClassNotFoundException e) {
				logger.debug("The argument type is unknown from \"java/lang\" package: "+ e.getMessage());
			}
			// Null because the call does not need a client identification for response.
			router.executeCommand(null, id, method, arguments);
			
			break; 
			
		case 2: // Location level
			logger.debug("Location level message");
			String cmdName = (String)obj.get("commandName");
			
			 if(cmdName.equalsIgnoreCase("getLocations")) {
				 router.getLocations();
				 
			 } else if(cmdName.equalsIgnoreCase("newLocation")) {
				 router.newLocation((JSONObject)obj.get("location"));
				 
			 } else if(cmdName.equalsIgnoreCase("updateLocation")) {
				 router.updateLocation((JSONObject)obj.get("location"));
				 
			 } else if(cmdName.equalsIgnoreCase("moveDevice")) {
				 String objId = (String)obj.get("deviceId");
				 AbstractObjectSpec abObj = (AbstractObjectSpec) router.getObjectRefFromID(objId);
				 router.moveObject(abObj, (String)obj.get("srcLocationId"), (String)obj.get("destLocationId"));
			 }
			
			break; 
			
		case 3: // Interpreter
			logger.debug("Interpreter level message");
			break;
			
		default: 
			logger.debug("Not corresponding target type");
			break;
		}
	}

	/**
	 * Load the Class java object from the String type, instanciate it and add
	 * the generate java object to the parameters list.
	 * 
	 * @param type
	 *            , the parameter java type (ex: java.lang.String)
	 * @param value
	 *            , the value of the parameter
	 * @param arguments
	 *            , the arguments list
	 * 
	 * @throws ClassNotFoundException
	 *             , throw if no java Class corresponding to type parameter is
	 *             found.
	 * @throws IllegalArgumentException
	 *             , throw if the argument does not corresponding to the
	 *             constructor type.
	 */
	@SuppressWarnings("unchecked")
	private void addArguments(String type, String value,
			ArrayList<Object> arguments) throws ClassNotFoundException,
			IllegalArgumentException {

		if (!type.equalsIgnoreCase("ref")) {
			// Type referred to a basic java type
			@SuppressWarnings("rawtypes")
			Class argClass = Class.forName("java.lang." + type);
			Object param;
			try {
				param = argClass.getConstructor(String.class)
						.newInstance(value);
				arguments.add(param);
			} catch (SecurityException e) {
				logger.debug("Security violation: " + e.getMessage());
			} catch (InstantiationException e) {
				logger.debug("Instanciation error, wrong type description: "
						+ e.getMessage());
			} catch (IllegalAccessException e) {
				logger.debug("illegal access detected:  " + e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Probleme with the targeted object : "
						+ e.getMessage());
			} catch (NoSuchMethodException e) {
				logger.debug("No constructor with \"String\" parameter : "
						+ e.getMessage());
			}
		} else {
			// Type is a reference to a complex java object
			// and value correspond to the id of the java AbstractObjectSpec
			Object paramRef = router.getObjectRefFromID(value);
			arguments.add(paramRef);
		}
	}

}
