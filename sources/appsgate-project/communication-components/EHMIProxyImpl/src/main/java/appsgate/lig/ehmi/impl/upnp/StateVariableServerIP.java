package appsgate.lig.ehmi.impl.upnp;


import org.osgi.service.upnp.UPnPLocalStateVariable;

public class StateVariableServerIP extends StringStateVariable implements UPnPLocalStateVariable {
	
	public static final String VAR_NAME="serverIP";
	
	public StateVariableServerIP() {
		NAME = VAR_NAME;
		DEFAULT_VALUE = "127.0.0.1";
		stringValue = DEFAULT_VALUE.toString();
	}

}
