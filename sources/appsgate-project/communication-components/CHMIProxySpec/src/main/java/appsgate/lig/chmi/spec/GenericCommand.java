package appsgate.lig.chmi.spec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

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

	private ArrayList<Object> args;
	@SuppressWarnings("rawtypes")
	private ArrayList<Class> paramType;
	private Object obj;
	private String objId;
	private String methodName;
	private String callId;
	private int clientId;
	AsynchronousCommandResponseListener listener;
	
	private Object returnObject;

	@SuppressWarnings("rawtypes")
	public GenericCommand(ArrayList<Object> args, ArrayList<Class> paramType,
			Object obj, String objId, String methodName, String callId, int clientId,
			AsynchronousCommandResponseListener listener) {
		logger.trace("new GenericCommand(ArrayList<Object> args : {}, ArrayList<Class> paramType : {},"
				+ " Object obj: {}, String objId : {}, String methodName : {}, String callId : {}, int clientId : {},"
				+ " AsynchronousCommandResponseListener listener : {}) ",
				args, paramType, obj,objId, methodName, callId, clientId, listener);

		this.args = args;
		this.paramType = paramType;
		this.obj = obj;
		this.objId = objId;
		this.methodName = methodName;
		this.callId = callId;
		this.clientId = clientId;
		this.listener = listener;
		
		this.returnObject = null;
	}
	
	@SuppressWarnings("rawtypes")
	public GenericCommand(ArrayList<Object> args, ArrayList<Class> paramType,
			Object obj, String objId, String methodName, String callId, int clientId) {
		this(args, paramType, obj, objId, methodName, callId, clientId, null);
	}

	@SuppressWarnings("rawtypes")
	public GenericCommand(ArrayList<Object> args, ArrayList<Class> paramType,
			Object obj, String methodName) {
		this(args, paramType, obj, null, methodName, null, -1, null);
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
	@Override
	public Object abstractInvoke()
			throws Exception {
		logger.trace("abstractInvoke()");

		
		Class[] tabTypes = new Class[paramType.size()];
		int i = 0;
		Iterator<Class> itc = paramType.iterator();
		
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

		return m.invoke(obj, args.toArray());
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
                    logger.error(e.getMessage());
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

}
