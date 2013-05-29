package appsgate.lig.context.follower.listeners;

/**
 * This interface describe a core listener to by notified when a specific
 * event from a specific component happen.
 * 
 * @author Cédric Gérard
 * @since  May 28, 2013
 * @version 0.0.1
 */
public interface CoreListener {
	
	/**
	 * Set the object Id corresponding to the core component
	 * that trigger the event.
	 * @param objectId
	 */
	public void setObjectId(String objectId);
	
	/**
	 * Set the event variable name that is followed 
	 * @param eventVarName
	 */
	public void setEvent(String eventVarName);
	
	/**
	 * Set the threshold value that trigger the notification
	 * @param eventVarValue
	 */
	public void setValue(String eventVarValue);
	
	/**
	 * Get the id of the source of the event
	 * @return the object id
	 */
	public String getObjectId();
	
	/**
	 * Get the event variable name
	 * @return the variable name
	 */
	public String getEvent();
	
	/**
	 * Get the threshold value for notification
	 * @return the threshold value
	 */
	public String getValue();
	
	/**
	 * Method call when the event is catch
	 */
	public void notifyEvent();
	
	/**
	 * Method call when the event is catch
	 */
	public void notifyEvent(CoreListener listener);

}
