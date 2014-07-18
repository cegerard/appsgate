package appsgate.ard.adaptor;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

/**
 * This entity is instantiated in Apam level, and all messages received in the
 * Queue fuchsia/ard/locker (EventAdmin message), are addressed to a
 * ApamMessage, so it can be captured in upper levels (e.g. IHM)
 */
public class ARDLockerFuchsiaApamAdapter implements EventHandler {

    BundleContext context;

    EventAdmin eventAdmin;

    public ARDLockerFuchsiaApamAdapter(BundleContext bc) {
        context = bc;
    }

    public void start() {

        ServiceReference ref = context.getServiceReference(EventAdmin.class.getName());

        if (ref != null) {
            /**
             * Here we subscribe to the topic in which the door messages are
             * published
             */

            eventAdmin = (EventAdmin) context.getService(ref);

            String[] topics = new String[]{
                "fuchsia/ard/locker/*"
            };

            Dictionary props = new Hashtable();
            props.put(EventConstants.EVENT_TOPIC, topics);
            context.registerService(EventHandler.class.getName(), this, props);

        }

    }

    public void stop() {

    }

    private NotificationMsg triggerApamMessage(final Event event) { //final Message msg
        return new NotificationMsg() {

            public CoreObjectSpec getSource() {
                return null;
            }
            
            public String getVarName() {
            	return null;
            }

            public String getNewValue() {
                return null;
            }
            public String getOldValue() {
                return null;
            }

            public JSONObject JSONize() {

                JSONObject result = new JSONObject();
                try {
                    //Those a mandatory fields, case they are not defined an exception is raised in upper layers
                    result.put("objectId", String.format("ard-door-%s", event.getProperty("door")));

                //result.put(event.getProperty("authorization_result").toString(),event.getProperty("card-int"));
                    result.put("varName", event.getProperty("authorization_result"));
                    result.put("value", event.getProperty("card-int"));

                    //Add all other informations just to make sure
                    for (String name : event.getPropertyNames()) {
                        result.put(name, event.getProperty(name));
                    }
                } catch (JSONException ex) {
                    // Will never be thrown
                }

                return result;

            }
        };
    }

    public void handleEvent(Event event) {

        /**
         * Here we trigger an Apam message (they will be catch by somebody in
         * upper layers)
         */
        triggerApamMessage(event);

    }
}
