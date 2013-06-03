package appsgate.lig.eude.interpreter.impl;

import java.util.Vector;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.follower.listeners.CoreListener;
import appsgate.lig.context.follower.spec.ContextFollowerSpec;
import appsgate.lig.router.spec.RouterApAMSpec;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.nodes.NodeExpBool;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.router.impl.RouterImpl;

/**
 * This class is the interpreter component for end user development environment. 
 * 
 * @author Cédric Gérard
 * @since  April 26, 2013
 * @version 1.0.0
 *
 */
public class EUDEInterpreterImpl implements StartEventListener, EndEventListener {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EUDEInterpreterImpl.class);
	
	private ContextFollowerSpec contextFollower;
	
	private coreEventListener listener;
	
	private RouterApAMSpec router;
	
	/**
	 * Vector that contains all the existing programs
	 */
	private Vector<NodeProgram> listPrograms;
	
	/**
	 * Reference to the ApAM router. Used to send action to the objects
	 */
	private RouterImpl router;
	
	/**
	 * Initialize the list of the programs
	 * 
	 * @constructor
	 */
	public EUDEInterpreterImpl() {
		listPrograms = new Vector<NodeProgram>();
		
		// for the tests - remove on production
		listPrograms.add(new NodeProgram());
		
		// deploy the first program
		listPrograms.get(0).addEndEventListener(this);
		listPrograms.get(0).call();
	}
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The interpreter component is initialized");
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
		System.out.println(p.getName() + " has ended");
		p.removeEndEventListener(this);
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
		
	}

}
