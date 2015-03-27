package appsgate.lig.chmi.spec.listeners;

import appsgate.lig.core.object.messages.NotificationMsg;

/**
 * This interface is a listener template for core event subscription
 * @author Cédric Gérard
 * @since April 23, 2014
 * @version 1.0.0 
 */
public interface CoreEventsListener {
	
	/**
	 * Notify that a new event has come
	 */
	public void notifyEvent(NotificationMsg notification);

}
