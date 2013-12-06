package appsgate.lig.main.impl.upnp;

import java.util.HashMap;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPService;
import org.osgi.service.upnp.UPnPStateVariable;

public class ServerInfoService implements UPnPService {

	final private String SERVICE_ID = "urn:upnp-org:serviceId:serverInfo:1";
	final private String SERVICE_TYPE = "urn:schemas-upnp-org:service:serverInfo:1";
	final private String VERSION = "1";

	private StateVariableServerIP serverIP;
	private StateVariableServerURL serverURL;
	private StateVariableServerWebsocket serverWebsocket;
	private UPnPStateVariable[] states;
	private HashMap<String, UPnPAction> actions = new HashMap<String, UPnPAction>();

	public ServerInfoService() {
		serverIP = new StateVariableServerIP();
		serverURL = new StateVariableServerURL();
		serverWebsocket = new StateVariableServerWebsocket();
		states = new UPnPStateVariable[] { serverIP, serverURL, serverWebsocket };

		UPnPAction getServerIP = new ActionGetIP(serverIP);
		UPnPAction getServerURL = new ActionGetURL(serverURL);
		UPnPAction getWebsocket = new ActionGetWebsocket(serverWebsocket);

		actions.put(getServerIP.getName(), getServerIP);
		actions.put(getServerURL.getName(), getServerURL);
		actions.put(getWebsocket.getName(), getWebsocket);
	}

	@Override
	public String getId() {
		return SERVICE_ID;
	}

	@Override
	public String getType() {
		return SERVICE_TYPE;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public UPnPAction getAction(String name) {
		return (UPnPAction) actions.get(name);
	}

	@Override
	public UPnPAction[] getActions() {
		return (UPnPAction[]) (actions.values()).toArray(new UPnPAction[] {});
	}

	@Override
	public UPnPStateVariable[] getStateVariables() {
		return states;
	}

	@Override
	public UPnPStateVariable getStateVariable(String name) {
		if (name != null) {
			if (name.equals(serverIP.getName()))
				return serverIP;
			else if (name.equals(serverURL.getName()))
				return serverURL;
			else if (name.equals(serverWebsocket.getName()))
				return serverWebsocket;
		}
		return null;
	}

}
