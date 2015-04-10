package appsgate.lig.chmi.spec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A generic command is generic representation of a method call.
 * An instance of this class i a runnable object that contain all information
 * of a method call.
 * 
 * The run method when it execute make a java introspection method call and return, if any,
 * the response to the client who made the call.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 *
 */
public class GenericCommand implements AsynchronousCommandRunner {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(GenericCommand.class);

	private ArrayList<Object> argumentsValue;
	@SuppressWarnings("rawtypes")
	private ArrayList<Class> argumentsType;
	private Object obj;
	private String objId;
	private String methodName;
	private String callId;
	private int clientId;
	AsynchronousCommandResponseListener listener;
	
	private Object returnObject;
	
	public GenericCommand(JSONArray jsonArgs,
			Object obj, String objId, String methodName, String callId, int clientId,
			AsynchronousCommandResponseListener listener) {
		logger.trace("new GenericCommand(JSONArray jsonArgs : {},"
				+ " Object obj: {}, String objId : {}, String methodName : {}, String callId : {}, int clientId : {},"
				+ " AsynchronousCommandResponseListener listener : {}) ",
				jsonArgs, obj,objId, methodName, callId, clientId, listener);

        loadArguments(jsonArgs);

		this.obj = obj;
		this.objId = objId;
		this.methodName = methodName;
		this.callId = callId;
		this.clientId = clientId;
		this.listener = listener;
		
		this.returnObject = null;
	}	

	/**
	 * This method allow the router to invoke methods on an abstract java
	 * object.
	 * 
	 * @param obj
	 *            , the abstract object on which the method will be invoke
	 * @param argumentsValue
	 *            , all arguments for the method call
	 * @param methodName
	 *            , the method to invoke
	 * @return the result of dispatching the method represented by this object
	 *         on obj with parameters args
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object abstractInvoke()
			throws Exception {
		logger.trace("abstractInvoke()");

		
		Class[] tabTypes = new Class[argumentsType.size()];
		int i = 0;
		Iterator<Class> itc = argumentsType.iterator();
		
		while(itc.hasNext()) {
			tabTypes[i] = itc.next(); 
			i++;
		}
        if (obj == null) {
            logger.error("Null object");
            return null;
        }
        if (obj.getClass() == null) {
            logger.error("Unable to find the class of this object {}", obj);
            return null;
        }
		Method m = obj.getClass().getMethod(methodName, tabTypes);
		logger.trace("abstractInvoke(), real method found : "+m);

		return m.invoke(obj, argumentsValue.toArray());
	}

	@Override
	public void run() {
		logger.trace("run()");
		try {
			Object ret = abstractInvoke();
			logger.trace("Invocation successfull");
			
			if (ret != null) {
				logger.trace("remote call (Generic Command), " + methodName + " returns "
						+ ret.toString() + " / return type: "
						+ ret.getClass().getName());
				returnObject = ret;
				
				if(listener != null) {
					listener.notifyResponse(objId, returnObject.toString(),callId, clientId);
				}
			} else {
				logger.debug("remote call (Generic Command), for method  " + methodName + " returns null");				
			}
			
		} catch (Exception e) {
                    logger.error("The method {} is not found for {}", methodName, this.objId);
                    logger.error("run(), ", e);
		}
	}
	
	/**
	 * Get the return object of the last call
	 * @return the return type of the last call
	 */
	@Override
	public Object getReturn() {
		return returnObject;
	}

	/**
	 * Load argument describe in a JSONArray with their value and java type to two 
	 * ArrayList one for values and the other for types
	 * @param args the arguments JSONArray
	 */
	@SuppressWarnings("rawtypes")
	public void loadArguments(JSONArray args) {
		try {
			argumentsValue = new ArrayList<Object>();
			argumentsType = new ArrayList<Class>();
			
			// Get all arguments types and values
			int l = args.length();
			int cpt = 0;
			JSONObject JSONObj;
			String value, type;
		
			while (cpt < l) {
				JSONObj = args.getJSONObject(cpt);
				// This one has been modified : JSONObj.getString("value") not working properly with JSONObjects
				value = JSONObj.get("value").toString();
				type = JSONObj.getString("type");
				addArguments(type, value);
				cpt++;
			}
		}catch (ClassNotFoundException e) {
			logger.debug("The argument type is unknown from \"java/lang\" package: "
					+ e.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load the Class java object from the String type, instantiate it and add
	 * the generate java object to the parameters list.
	 * 
	 * @param type the parameter java type (e.g.: java.lang.String)
	 * @param value the value of the parameter
	 * @param arguments the arguments list
	 * 
	 * @throws ClassNotFoundException throw if no java Class corresponding to type parameter is  found.
	 * @throws IllegalArgumentException throw if the argument does not corresponding to the constructor type.
	 */
	@SuppressWarnings("unchecked")
	private void addArguments(String type, String value) throws ClassNotFoundException,
			IllegalArgumentException {

		// Type referred to a basic java type
		try {
			if (type.matches("\\p{javaUpperCase}.*")) {
				// Java wrapper for basic type
				logger.debug("Wrapper type detected");
				@SuppressWarnings("rawtypes")
				Class argClass;
				if (type.contains("JSON")) {
					argClass = Class.forName("org.json." + type);
				} else {
					argClass = Class.forName("java.lang." + type);
				}
				Object param = argClass.getConstructor(String.class)
						.newInstance(value);
				argumentsValue.add(param);
				argumentsType.add(param.getClass());

			} else {
				// Java primitive type
				logger.debug("Full primitive type detected");
				if (type.contentEquals("int")) {
					int intParam = new Integer(value).intValue();
					argumentsValue.add(intParam);
					argumentsType.add(int.class);
				} else if (type.contentEquals("float")) {
					float floatParam = new Float(value).floatValue();
					argumentsValue.add(floatParam);
					argumentsType.add(float.class);
				} else if (type.contentEquals("long")) {
					long longParam = new Long(value).longValue();
					argumentsValue.add(longParam);
					argumentsType.add(long.class);
				} else if (type.contentEquals("double")) {
					double doubleParam = new Double(value).doubleValue();
					argumentsValue.add(doubleParam);
					argumentsType.add(double.class);
				} else if (type.contentEquals("boolean")) {
					boolean boolParam = new Boolean(value).booleanValue();
					argumentsValue.add(boolParam);
					argumentsType.add(boolean.class);
				} else {
					throw new ClassNotFoundException("Primitive type (" + type+ ") not defined");
				}
			}

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
	}		
	
	
	
}
