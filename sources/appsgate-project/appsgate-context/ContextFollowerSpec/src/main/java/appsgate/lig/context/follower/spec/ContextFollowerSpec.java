package appsgate.lig.context.follower.spec;

import appsgate.lig.context.follower.listeners.CoreListener;

/**
 * This interface describe services offered by context follower implementation
 * 
 * @author Cédric Gérard
 * @since  May 28, 2013
 * @version 0.0.1
 */
public interface ContextFollowerSpec {

	/**
	 * This method allow the caller to add a specific coreListener
	 * to follow core components state change.
	 * @param coreListener the listener for subscription
	 */
	public void addListener(CoreListener coreListener);
	
	/**
	 * This method allow the caller to unsubscribe itself from
	 * core components state change.
	 * @param coreListener
	 */
	public void deleteListener(CoreListener coreListener);

}
