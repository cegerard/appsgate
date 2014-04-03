package appsgate.lig.main.impl.upnp;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPStateVariable;

public class ActionGetIP implements UPnPAction {

	String NAME = "getIP";
	String RESULT_STATUS = "serverIP";
	String[] OUT_ARG_NAMES = new String[] { RESULT_STATUS };
	StateVariableServerIP serverIP;

	public ActionGetIP(StateVariableServerIP serverIP) {
		this.serverIP = serverIP;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getReturnArgumentName() {
		return null;
	}

	@Override
	public String[] getInputArgumentNames() {
		return null;
	}

	@Override
	public String[] getOutputArgumentNames() {
		return OUT_ARG_NAMES;
	}

	@Override
	public UPnPStateVariable getStateVariable(String argumentName) {
		return serverIP;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Dictionary invoke(Dictionary args) throws Exception {
		String value = serverIP.getCurrentStringValue();
		Hashtable result = new Hashtable();
		result.put(RESULT_STATUS, value);
		return result;
	}

}
