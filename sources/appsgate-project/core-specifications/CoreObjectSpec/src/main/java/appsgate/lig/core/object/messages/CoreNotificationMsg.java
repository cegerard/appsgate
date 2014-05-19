/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.core.object.messages;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class CoreNotificationMsg implements NotificationMsg{
        /**
     * The source sensor of this notification
     */
    private final CoreObjectSpec source;


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
    public CoreNotificationMsg(String varName, String value, CoreObjectSpec source) {
        this.source = source;
        this.varName = varName;
        this.value = value;
    }


    @Override
    public CoreObjectSpec getSource() {
        return source;
    }
    
    @Override
	public String getVarName() {
		return varName;
	}

	public String getValue() {
		return value;
	}

    @Override
    public String getNewValue() {
        return value;
    }

    @Override
    public JSONObject JSONize() {
        JSONObject notif = new JSONObject();
        try {
            notif.put("objectId", source.getAbstractObjectId());
            notif.put("varName", varName);
            notif.put("value", value);
        } catch (JSONException ex) {
                    // Will never be thrown
        }

        return notif;
    }
    
}
