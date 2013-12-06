package appsgate.lig.main.impl.upnp;

import org.osgi.service.upnp.UPnPLocalStateVariable;

public class StateVariableServerURL extends StringStateVariable implements UPnPLocalStateVariable {

	
	public StateVariableServerURL() {
		NAME = "serverURL";
		DEFAULT_VALUE = "http://127.0.0.1/index.html";
		stringValue = DEFAULT_VALUE.toString();
	}


}
