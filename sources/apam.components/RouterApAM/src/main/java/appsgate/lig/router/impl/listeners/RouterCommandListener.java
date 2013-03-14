package appsgate.lig.router.impl.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
		logger.debug("Client send : " + obj.toString());
		try {
			String targetTypeTemp;
			targetTypeTemp = obj.getString("targetType");

			int targetType = Integer.parseInt(targetTypeTemp);

			switch (targetType) {

			case 0: // Router level
				logger.debug("Router level message");
				String cmd = obj.getString("commandName");

				if (cmd.equalsIgnoreCase("getDevices")) {
					router.getDevices("clientID");
				}

				break;

			case 1:// Abstract object level
				logger.debug("Abstract object level message");
				try {
					String method = obj.getString("method");
					String id = obj.getString("objectId");
					JSONArray args = obj.getJSONArray("args");

					ArrayList<Object> arguments = new ArrayList<Object>();

					// Get all arguments types and values
					int l = args.length();
					int cpt = 0;
					JSONObject JSONObj;
					String value, type;
					while (cpt < l) {
						JSONObj = args.getJSONObject(cpt);
						value = JSONObj.getString("value");
						type = JSONObj.getString("type");
						addArguments(type, value, arguments);
						cpt++;
					}

					// Null because the call does not need a client
					// identification for response.
					router.executeCommand(null, id, method, arguments);

				} catch (IllegalArgumentException e) {
					logger.debug("Inappropriate argument: " + e.getMessage());
				} catch (ClassNotFoundException e) {
					logger.debug("The argument type is unknown from \"java/lang\" package: "
							+ e.getMessage());
				}

				break;

			case 2: // Location level
				logger.debug("Location level message");
				String cmdName = obj.getString("commandName");

				if (cmdName.equalsIgnoreCase("getLocations")) {
					router.getLocations();

				} else if (cmdName.equalsIgnoreCase("newLocation")) {
					router.newLocation(obj.getJSONObject("location"));

				} else if (cmdName.equalsIgnoreCase("updateLocation")) {
					router.updateLocation(obj.getJSONObject("location"));

				} else if (cmdName.equalsIgnoreCase("moveDevice")) {
					String objId = obj.getString("deviceId");
					AbstractObjectSpec abObj = (AbstractObjectSpec) router
							.getObjectRefFromID(objId);
					router.moveObject(abObj, obj.getString("srcLocationId"),
							obj.getString("destLocationId"));
				}

				break;

			case 3: // Interpreter
				logger.debug("Interpreter level message");
				break;

			default:
				logger.debug("Not corresponding target type");
				break;
			}

		} catch (JSONException e1) {
			e1.printStackTrace();
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
