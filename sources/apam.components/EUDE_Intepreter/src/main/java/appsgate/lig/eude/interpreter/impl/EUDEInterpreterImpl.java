package appsgate.lig.eude.interpreter.impl;

import java.util.Vector;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	}
	
	/**
	 * Called by ApAM when Notification message comes
	 * and forward it to client part by calling the sendService
	 * 
	 * @param notif the notification message from ApAM
	 */
	public void gotNotification(NotificationMsg notif) {
			try {
				logger.debug("Interpreter message received, " + notif.JSONize());
			} catch (JSONException e) {
				logger.error("Notification format not recognized: ");
				e.printStackTrace();
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
