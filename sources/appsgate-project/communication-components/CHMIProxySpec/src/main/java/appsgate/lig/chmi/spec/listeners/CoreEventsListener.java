package appsgate.lig.chmi.spec.listeners;

/**
 * This interface is a listener template for core event subscription
 * @author Cédric Gérard
 * @since April 23, 2014
 * @version 1.0.0 
 */
public interface CoreEventsListener {
	
	/**
	 * Get the source core identifier of the event 
	 * @return the core object identifier as a String 
	 */
	public String getSourceId();
	
	/**
	 * Get the variable name of the event
	 * @return the variable that change as a String
	 */
	public String varName();
	
	/**
	 * Get the new value of the variable
	 * @return the new value as String
	 */
	public String getValue();
	
	/**
	 * Notify that a new event has come
	 */
	public void notifyEvent(String srcId, String varName, String value);

}
