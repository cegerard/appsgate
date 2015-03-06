/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.core.object.messages;

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
    private final String source;


    /**
     * The name of the change variable
     */
    private final String varName;

    /**
     * The newValue corresponding to the varName variable
     */
    private final String newValue;

    /**
     * The old value to the varName variable if existing
     */
    private final String oldValue;

    /**
     * Constructor for this ApAM message
     *
     * @param varName the property name that change
     * @param newValue the new property newValue
     * @param source the source instance of this notification
     */
    public CoreNotificationMsg(String varName, String oldValue, String newValue, String source) {
        this.source = source;
        this.varName = varName;
        this.newValue = newValue;
        this.oldValue = oldValue;

    }

    /**
     * Constructor for this ApAM message
     *
     * @param varName the property name that change
     * @param newValue the new property newValue
     * @param source the source instance of this notification
     */
    public CoreNotificationMsg(String varName, String newValue, String source) {
        this(varName, "", newValue, source);
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
        return newValue;
    }

    @Override
    public String getOldValue() {
        return oldValue;
    }

    @Override
    public JSONObject JSONize() {
        JSONObject notif = new JSONObject();
        try {
            notif.put("objectId", source);
            notif.put("varName", varName);
            notif.put("value", newValue);
            notif.put("oldValue", oldValue);

        } catch (JSONException ex) {
            // Will never be thrown
        }

        return notif;
    }

}