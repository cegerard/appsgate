package appsgate.lig.context.proxy.spec;

import java.util.ArrayList;
import appsgate.lig.context.proxy.listeners.CoreListener;

/**
 * This interface describe services offered by context follower implementation
 * 
 * @author Cédric Gérard
 * @since  May 28, 2013
 * @version 1.0.0
 */
public interface ContextProxySpec {

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
	
	/**
	 * Return the devices of a list of type presents in the places
	 * 
	 * @param typeList
	 *            the list of types to look for (if empty, return all objects)
	 * @param places
	 *            the spaces where to find the objects (if empty return all
	 *            places)
	 * @return a list of objects contained in these spaces
	 */
	public ArrayList<String> getDevicesInSpaces(ArrayList<String> typeList, ArrayList<String> spaces);

}
