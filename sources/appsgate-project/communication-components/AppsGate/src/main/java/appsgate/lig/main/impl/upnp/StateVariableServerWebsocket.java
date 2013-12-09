package appsgate.lig.main.impl.upnp;


import org.osgi.service.upnp.UPnPLocalStateVariable;

public class StateVariableServerWebsocket extends StringStateVariable implements UPnPLocalStateVariable {
	
	public static final String VAR_NAME="serverWebsocket";
	
	public StateVariableServerWebsocket() {
		NAME = VAR_NAME;
		DEFAULT_VALUE = "http://127.0.0.1:8087/";
		NAME = "serverWebsocket";
		stringValue = DEFAULT_VALUE.toString();
	}

}
