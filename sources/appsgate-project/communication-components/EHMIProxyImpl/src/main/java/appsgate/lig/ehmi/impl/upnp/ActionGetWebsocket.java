package appsgate.lig.ehmi.impl.upnp;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPStateVariable;

public class ActionGetWebsocket implements UPnPAction {

	String NAME = "getWebsocket";
	String RESULT_STATUS = "serverWebsocket";
	String[] OUT_ARG_NAMES = new String[] { RESULT_STATUS };
	StateVariableServerWebsocket serverWebsocket;

	public ActionGetWebsocket(StateVariableServerWebsocket serverWebsocket) {
		this.serverWebsocket = serverWebsocket;
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
		return serverWebsocket;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Dictionary invoke(Dictionary args) throws Exception {
		String value = serverWebsocket.getCurrentStringValue();
		Hashtable result = new Hashtable();
		result.put(RESULT_STATUS, value);
		return result;
	}

}
