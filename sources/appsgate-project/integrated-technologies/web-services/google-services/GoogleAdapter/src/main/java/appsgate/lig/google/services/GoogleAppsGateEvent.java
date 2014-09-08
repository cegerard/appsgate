package appsgate.lig.google.services;

import org.json.JSONObject;


/**
 * Subclass of GoogleEvent
 * Created only if description contains appsgate keyword
 * @author thibaud
 */
public class GoogleAppsGateEvent extends GoogleEvent {

	public GoogleAppsGateEvent(JSONObject jsonEvent)
			throws InstantiationException {
		super(jsonEvent);
		// TODO Auto-generated constructor stub
	}

}
