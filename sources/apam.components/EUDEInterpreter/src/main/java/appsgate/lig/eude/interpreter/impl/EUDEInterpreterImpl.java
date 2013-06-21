package appsgate.lig.eude.interpreter.impl;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.follower.listeners.CoreListener;
import appsgate.lig.context.follower.spec.ContextFollowerSpec;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;
import appsgate.lig.router.spec.GenericCommand;
import appsgate.lig.router.spec.RouterApAMSpec;

/**
 * This class is the interpreter component for end user development environment. 
 * 
 * @author Cédric Gérard
 * @since  April 26, 2013
 * @version 1.0.0
 *
 */
public class EUDEInterpreterImpl implements EUDE_InterpreterSpec, StartEventListener, EndEventListener {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EUDEInterpreterImpl.class);

	private ContextFollowerSpec contextFollower;
	
	private coreEventListener listener;
	
	/**
	 * HashMap that contains all the existing programs under a JSON format
	 */
	private HashMap<String, NodeProgram> mapPrograms;
	public HashMap<String, JSONObject> getListPrograms() {
		HashMap <String, JSONObject> mapProgramJSON = new HashMap<String, JSONObject>();
		for (NodeProgram p : mapPrograms.values()) {
			mapProgramJSON.put(p.getName(), p.getProgramJSON());
		}
		
		return mapProgramJSON;
	}

	/**
	 * Reference to the ApAM router. Used to send action to the objects
	 */
	private RouterApAMSpec router;
	
	/**
	 * Initialize the list of the programs
	 * 
	 * @constructor
	 */
	public EUDEInterpreterImpl() {
		mapPrograms = new HashMap<String, NodeProgram>();
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The interpreter component is initialized");;
		contextFollower.addListener(listener);
	}

	/**
	 * Initialize a program from its JSON representation
	 * 
	 * @param programJSON Abstract tree of the program in JSON
	 * @return true when succeeded, false when failed a JSON error has been detected
	 */
	public boolean addProgram(JSONObject programJSON) {
		NodeProgram p;
		
		// initialize a program node from the JSON
		try {
			p = new NodeProgram(this, programJSON);
		} catch (JSONException e) {
			logger.debug("JSON error detected while loading a program");
			return false;
		}
		
		mapPrograms.put(p.getName(), p);

		return true;
	}
	
	/**
	 * Launch the interpretation of a program
	 * 
	 * @param programName Name of the program to launch
	 * @return true if the program has been successfully launched, false otherwise
	 */
	public boolean callProgram(String programName) {
		NodeProgram p = mapPrograms.get(programName);
		
		if (p != null) {
			p.addEndEventListener(this);
			p.call();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Execute a method call on the router
	 * 
	 * @param objectId
	 * @param methodName
	 * @param args
	 * @param paramType
	 * @return
	 */
	public GenericCommand executeCommand(String objectId, String methodName, JSONArray args) {
		return router.executeCommand(objectId, methodName, args);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The router interpreter components has been stopped");
		contextFollower.deleteListener(listener);
	}
	
	public class coreEventListener implements CoreListener {
		
		private String objectId;
		private String varName;
		private String varValue;
		
	
		public coreEventListener(String objectId, String varName,
				String varValue) {
			this.objectId = objectId;
			this.varName = varName;
			this.varValue = varValue;
		}

		@Override
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		@Override
		public void setEvent(String eventVarName) {
			this.varName = eventVarName;
		}

		@Override
		public void setValue(String eventVarValue) {
			this.varValue = eventVarValue;
		}

		@Override
		public String getObjectId() {
			return objectId;
		}

		@Override
		public String getEvent() {
			return varName;
		}

		@Override
		public String getValue() {
			return varValue;
		}

		@Override
		public void notifyEvent() {
			logger.debug("The event is catch by the EUDE");
		}

		@Override
		public void notifyEvent(CoreListener listener) {
			logger.debug("The event is catch by the EUDE "+listener);
		}
		
	}

	@Override
	public void endEventFired(EndEvent e) {
		NodeProgram p = (NodeProgram)e.getSource();
		p.removeEndEventListener(this);
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
		
	}

}
