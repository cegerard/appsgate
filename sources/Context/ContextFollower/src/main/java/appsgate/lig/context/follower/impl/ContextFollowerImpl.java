package appsgate.lig.context.follower.impl;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.logical.object.messages.NotificationMsg;

/**
 * This class is use to allow other components to subscribe for specific triggering
 * event or request for system or devices state
 * 
 * @author Cédric Gérard
 * @since  May 28, 2013
 * @version 0.0.1-SNAPSHOT
 */
public class ContextFollowerImpl {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ContextFollowerImpl.class);
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The context follower ApAM component has been initialized");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The context follower ApAM component has been stopped");
	}
	
	/**
	 * Called by ApAM when Notification message comes
	 * and forward it to client part by calling the sendService
	 * 
	 * @param notif the notification message from ApAM
	 */
	public void gotNotification(NotificationMsg notif) {
		try {
			logger.debug("Notification message receive, " + notif.JSONize());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
