package appsgate.lig.context.device.properties.table.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.ehmi.spec.messages.NotificationMsg;

/**
 * This class is an ApAM message for object properties notification
 *
 * @author Cédric Gérard version 1.0.0
 * @since June 1, 2013
 */
public class PropertiesTableNotificationMsg implements NotificationMsg {

    /**
     * The objectId
     */
    private String objectId;

    /**
     * The user concern for the object property
     */
    private String userId;

    /**
     * The object property name for the user
     */
    private String objectPropertyName;
    
    /**
     * The object property value for the user
     */
    private String objectPropertyValue;

    /**
     * Constructor for an object property notification
     *
     * @param objectID the object identifier
     * @param userId the user identifier
     * @param objectPropertyValue the property of this object for this user
     */
    public PropertiesTableNotificationMsg(String source, String userId,
            String objectPropertyName, String objectPropertyValue) {
        super();
        this.objectId = source;
        this.userId = userId;
        this.objectPropertyName = objectPropertyName;
        this.objectPropertyValue = objectPropertyValue;
    }

    @Override
    public String getSource() {
        return objectId;
    }
    
    @Override
    public String getVarName() {
    	return objectPropertyName;
    }

    @Override
    public String getNewValue() {
        return objectPropertyValue;
    }

    @Override
    public JSONObject JSONize() {
        JSONObject notif = new JSONObject();
        try {
            notif.put("objectId", objectId);
            notif.put("userId", userId);
            notif.put("varName", objectPropertyName);
            notif.put("value", objectPropertyValue);
            
        } catch (JSONException e) {
            // Exception will never been thrown
        }

        return notif;
    }

}
