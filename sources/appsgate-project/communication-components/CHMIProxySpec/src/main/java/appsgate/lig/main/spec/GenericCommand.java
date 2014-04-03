package appsgate.lig.main.spec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;

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
public class GenericCommand implements Runnable {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(GenericCommand.class);

	private ArrayList<Object> args;
	@SuppressWarnings("rawtypes")
	private ArrayList<Class> paramType;
	private Object obj;
	private String methodName;
	private String callId;
	private int clientId;
	private SendWebsocketsService sendToClientService;
	
	private Object returnObject;

	@SuppressWarnings("rawtypes")
	public GenericCommand(ArrayList<Object> args, ArrayList<Class> paramType,
			Object obj, String methodName, String callId, int clientId,
			SendWebsocketsService sendToClientService) {

		this.args = args;
		this.paramType = paramType;
		this.obj = obj;
		this.methodName = methodName;
		this.callId = callId;
		this.clientId = clientId;
		this.sendToClientService = sendToClientService;
		
		this.returnObject = null;
	}

	@SuppressWarnings("rawtypes")
	public GenericCommand(ArrayList<Object> args, ArrayList<Class> paramType,
			Object obj, String methodName) {
		
		this.args = args;
		this.paramType = paramType;
		this.obj = obj;
		this.methodName = methodName;
		this.callId = null;
		this.clientId = -1;
		this.sendToClientService = null;
		
		this.returnObject = null;
	}

	/**
	 * This method allow the router to invoke methods on an abstract java
	 * object.
	 * 
	 * @param obj
	 *            , the abstract object on which the method will be invoke
	 * @param args
	 *            , all arguments for the method call
	 * @param methodName
	 *            , the method to invoke
	 * @return the result of dispatching the method represented by this object
	 *         on obj with parameters args
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Object abstractInvoke(Object obj, Object[] args, ArrayList<Class> paramTypes, String methodName)
			throws Exception {
		
		Class[] tabTypes = new Class[paramTypes.size()];
		int i = 0;
		Iterator<Class> itc = paramTypes.iterator();
		
		while(itc.hasNext()) {
			tabTypes[i] = itc.next(); 
			i++;
		}
	
		Method m = obj.getClass().getMethod(methodName, tabTypes);
		return m.invoke(obj, args);
	}

	@Override
	public void run() {
		try {
			Object ret = abstractInvoke(obj, args.toArray(), paramType, methodName);
			
			if (ret != null) {
				logger.debug("remote call, " + methodName + " returns "
						+ ret.toString() + " / return type: "
						+ ret.getClass().getName());
				returnObject = ret;
				
				if(sendToClientService != null) {
					JSONObject msg = new JSONObject();
					msg.put("value", returnObject.toString());
					msg.put("callId", callId);
					sendToClientService.send(clientId, msg.toString());
				}
			}
			
		} catch (Exception e) {

                    
                    
                    logger.debug("The generic method invocation failed --> ");
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the return object of the last call
	 * @return the return type of the last call
	 */
	public Object getReturn() {
		return returnObject;
	}

}
