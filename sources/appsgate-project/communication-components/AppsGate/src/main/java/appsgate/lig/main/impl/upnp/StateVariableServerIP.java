package appsgate.lig.main.impl.upnp;


import org.osgi.service.upnp.UPnPLocalStateVariable;

public class StateVariableServerIP extends StringStateVariable implements UPnPLocalStateVariable {
	
	public StateVariableServerIP() {
		NAME = "serverIP";
		DEFAULT_VALUE = "127.0.0.1";
		stringValue = DEFAULT_VALUE.toString();
	}

}
