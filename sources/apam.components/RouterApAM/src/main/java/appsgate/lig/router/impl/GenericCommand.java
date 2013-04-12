package appsgate.lig.router.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.communication.service.send.SendWebsocketsService;

public class GenericCommand implements Runnable {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(GenericCommand.class);
	
	
	private ArrayList<Object> args;
	private Object obj;
	private String methodName;
	private String callId;
	private int clientId;
	private SendWebsocketsService sendToClientService;

	public GenericCommand(ArrayList<Object> args, Object obj,
			String methodName, String callId, int clientId,
			SendWebsocketsService sendToClientService) {
		
		this.args = args;
		this.obj = obj;
		this.methodName = methodName;
		this.callId = callId;
		this.clientId = clientId;
		this.sendToClientService = sendToClientService;
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
	public Object abstractInvoke(Object obj, Object[] args, String methodName)
			throws Exception {
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = null;
		if (args != null) {
			paramTypes = new Class[args.length];
			for (int i = 0; i < args.length; ++i) {
				paramTypes[i] = args[i].getClass();
			}
		}
		Method m = obj.getClass().getMethod(methodName, paramTypes);
		return m.invoke(obj, args);
	}
	
	@Override
	public void run() {
		try{
			Object ret = abstractInvoke(obj, args.toArray(), methodName);
			if(ret != null)
				logger.debug("remote call, "+ methodName + " returns " + ret.toString()+" / return type: "+ret.getClass().getName());
				JSONObject msg = new JSONObject();
				msg.put("value", ret.toString());
				msg.put("callId", callId);
				sendToClientService.send(clientId, msg.toString());
		} catch (Exception e) {
			logger.debug("The generic method invocation failed --> ");
			e.printStackTrace();
		}
	}

}
