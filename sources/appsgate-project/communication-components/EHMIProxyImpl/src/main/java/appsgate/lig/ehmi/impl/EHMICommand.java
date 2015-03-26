package appsgate.lig.ehmi.impl;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.chmi.spec.AsynchronousCommandResponseListener;
import appsgate.lig.chmi.spec.AsynchronousCommandRunner;
import appsgate.lig.chmi.spec.GenericCommand;

/**
 * 
 * A generic command is generic representation of a method call.
 * An instance of this class i a runnable object that contain all information
 * of a method call.
 * 
 * This method is the same as the GenericCommand of CHMI, mostly used for type and retro compatibility
 * 
 * The run method when it execute make a java introspection method call and return, if any,
 * the response to the client who made the call.
 * 
 * @author Cédric Gérard
 * @since April 23, 2014
 * @version 1.0.0
 *
 */
public class EHMICommand extends GenericCommand implements AsynchronousCommandRunner {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EHMICommand.class);

	@SuppressWarnings("rawtypes")
	public EHMICommand(Object serviceObject, String methodName, ArrayList<Object> args, ArrayList<Class> paramType,
                       String callId, int clientId, AsynchronousCommandResponseListener listener) {
		super(args, paramType, serviceObject, null, methodName, callId, clientId, listener);
		logger.trace("new EHMICommand(Object serviceObject : {},"
				+ " String methodName : {}, ArrayList<Object> args : {}, ArrayList<Class> paramType : {}, String callId : {},"
				+ " int clientId : {}, AsynchronousCommandResponseListener listener : {})",
				serviceObject, methodName, args, paramType, callId, clientId, listener);
	}

}
