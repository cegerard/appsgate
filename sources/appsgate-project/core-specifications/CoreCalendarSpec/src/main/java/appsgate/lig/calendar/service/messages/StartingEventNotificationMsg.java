package appsgate.lig.calendar.service.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * instance of this class are starting agenda event notification.
 *
 * @author Cédric Gérard
 * @since May 16, 2013
 * @version 1.0.0
 *
 * @see NotificationMsg
 */
public class StartingEventNotificationMsg implements NotificationMsg {

    /**
     * The attach event name
     */
    private final String eventName;

    /**
     * build a new starting event notification object
     *
     * @param name the starting event name
     */
    public StartingEventNotificationMsg(String name) {
        eventName = name;
    }

    @Override
    public String getSource() {
        return null;
    }
    
    @Override
    public String getVarName() {
    	return "eventStarting";
    }

    @Override
    public String getNewValue() {
        return eventName;
    }

    @Override
    public String getOldValue() {
        return "";
    }

    @Override
    public JSONObject JSONize() {
        JSONObject obj = new JSONObject();
        try {
            //TODO: This one is weird because all event should have one parameter name and one parameter value
            obj.put("event", eventName);
        } catch (JSONException ex) {
            // Will never been raised
        }
        return obj;
    }

}
