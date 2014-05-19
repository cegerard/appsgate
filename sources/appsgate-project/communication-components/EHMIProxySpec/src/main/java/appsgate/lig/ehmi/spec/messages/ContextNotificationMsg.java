/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.ehmi.spec.messages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Cedric Gerard
 */
public class ContextNotificationMsg implements NotificationMsg{
        /**
     * The source sensor of this notification
     */
    private final String source;


    /**
     * The name of the change variable
     */
    private final String varName;

    /**
     * The value corresponding to the varName variable
     */
    private final String value;

    /**
     * Constructor for this ApAM message
     *
     * @param varName the property name that change
     * @param value the new property value
     * @param source the source instance of this notification
     */
    public ContextNotificationMsg(String varName, String value, String source) {
        this.source = source;
        this.varName = varName;
        this.value = value;
    }


    @Override
    public String getSource() {
        return source;
    }
    
    @Override
	public String getVarName() {
		return varName;
	}

    @Override
    public String getNewValue() {
        return value;
    }

    @Override
    public JSONObject JSONize() {
        JSONObject notif = new JSONObject();
        try {
            notif.put("source", source);
            notif.put("varName", varName);
            notif.put("value", value);
        } catch (JSONException ex) {
                    // Will never be thrown
        }

        return notif;
    }
    
}
