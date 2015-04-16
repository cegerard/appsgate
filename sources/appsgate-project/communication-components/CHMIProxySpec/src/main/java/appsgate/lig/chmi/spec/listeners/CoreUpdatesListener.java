package appsgate.lig.chmi.spec.listeners;

import org.json.JSONObject;

import appsgate.lig.core.object.spec.CoreObjectSpec.CORE_TYPE;

/**
 * This interface is a listener template for core Updates notifications
 *
 * @author Cédric Gérard
 * @since April 23, 2014
 * @version 1.0.0
 */
public interface CoreUpdatesListener {

    /**
     * Notify that a new update has come
     *
     * @param coreType
     * @param objectId
     * @param userType
     * @param behavior
     * @param descirption
     */
    public void notifyUpdate(UPDATE_TYPE updateType, CORE_TYPE coreType, String objectId, String userType, JSONObject descirption, JSONObject behavior);

    /** 
     * Adding some strong type to the available updates
     * @author thibaud
     *
     */
    public enum UPDATE_TYPE {
    	NEW("new"),
    	REMOVE("remove");
    	private String name;
    	private UPDATE_TYPE(String name) {
			this.name = name;
		}
    	public String getName() {
    		return name;
    	}
    }
}
