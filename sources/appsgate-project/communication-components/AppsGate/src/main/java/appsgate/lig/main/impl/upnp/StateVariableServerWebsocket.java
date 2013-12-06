package appsgate.lig.main.impl.upnp;


import org.osgi.service.upnp.UPnPLocalStateVariable;

public class StateVariableServerWebsocket extends StringStateVariable implements UPnPLocalStateVariable {
	
	public StateVariableServerWebsocket() {
		NAME = "serverWebsocket";
		DEFAULT_VALUE = "http://127.0.0.1:8080";
		stringValue = DEFAULT_VALUE.toString();
	}

}
