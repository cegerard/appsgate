package appsgate.lig.ehmi.impl.upnp;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.upnp.extra.util.UPnPEventNotifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPIcon;
import org.osgi.service.upnp.UPnPService;

public class AppsGateServerDevice implements UPnPDevice, UPnPEventListener,
		ServiceListener {

	private Dictionary<String, Object> dictionary;
	static final String DEVICE_ID = "uuid:898fff3a-a3d4-8ff5-c2e9-000e0c3e2d45";
	
	private final String devicesFilter = "(&("
				                        +Constants.OBJECTCLASS+"="+UPnPDevice.class.getName()+"))";

	private UPnPEventNotifier notifier;

	ServerInfoService serverInfoService;
	UPnPService[] services;

	ArrayList LinkedDevices = new ArrayList();

	private StateVariableServerIP serverIP;
	private StateVariableServerURL serverURL;
	private StateVariableServerWebsocket serverWebsocket;
	
	BundleContext context;

	public AppsGateServerDevice(BundleContext context) {

		this.context = context;
		
		serverInfoService = new ServerInfoService();
		services = new ServerInfoService[] { serverInfoService };

		serverIP = (StateVariableServerIP) serverInfoService
				.getStateVariable("serverIP");
		serverURL = (StateVariableServerURL) serverInfoService
				.getStateVariable("serverURL");
		serverWebsocket = (StateVariableServerWebsocket) serverInfoService
				.getStateVariable("serverWebsocket");

		setupDeviceProperties();
		buildEventNotifier();

		try {
			context.addServiceListener(this, devicesFilter);
		} catch (InvalidSyntaxException e) {
			System.out.println(e);
		}

	}

	private void setupDeviceProperties() {
		dictionary = new Hashtable<String, Object>();
		dictionary.put(UPnPDevice.UPNP_EXPORT, "");
		dictionary.put(org.osgi.service.device.Constants.DEVICE_CATEGORY,
				new String[] { UPnPDevice.DEVICE_CATEGORY });

		dictionary.put(UPnPDevice.FRIENDLY_NAME, "AppsGate set-top box");
		dictionary.put(UPnPDevice.MANUFACTURER, "LIG");
		dictionary.put(UPnPDevice.MANUFACTURER_URL, "http://www.liglab.fr");
		dictionary.put(UPnPDevice.MODEL_DESCRIPTION,
				"UPnP device for AppsGate server");
		dictionary.put(UPnPDevice.MODEL_NAME, "set top-box for AppsGate");
		dictionary.put(UPnPDevice.MODEL_NUMBER, "01");
		dictionary.put(UPnPDevice.MODEL_URL,
				"https://sites.google.com/site/liggates");
		dictionary.put(UPnPDevice.SERIAL_NUMBER, "00");

		dictionary.put(UPnPDevice.TYPE,
				"urn:schemas-upnp-org:device:appsgate-server:1");
		dictionary.put(UPnPDevice.UDN, DEVICE_ID);
		dictionary.put(UPnPDevice.UPC, "01");

		HashSet<String> types = new HashSet<String>(services.length);
		String[] ids = new String[services.length];
		ids[0] = services[0].getId();
		types.add(services[0].getType());
		dictionary.put(UPnPService.TYPE, types.toArray(new String[] {}));
		dictionary.put(UPnPService.ID, ids);
	}

	private void buildEventNotifier() {
		notifier = new UPnPEventNotifier(context, this,
				serverInfoService);
		serverIP.setNotifier(notifier);
		serverURL.setNotifier(notifier);
		serverWebsocket.setNotifier(notifier);
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
			break;

		case ServiceEvent.MODIFIED:
			break;

		case ServiceEvent.UNREGISTERING:
			ServiceReference<?> sr = event.getServiceReference();
			String UDN = (String) sr.getProperty(UPnPDevice.ID);
			if (UDN != null) {
				if (LinkedDevices.contains(UDN)) {
					// TODO ?

				}
			}
			break;
		}
	}

	@Override
	public void notifyUPnPEvent(String deviceId, String serviceId,
			Dictionary events) {
		if (!LinkedDevices.contains(deviceId)) {
			LinkedDevices.add(deviceId);
		}
		// TODO ?
	}

	@Override
	public UPnPService getService(String serviceId) {
		if (serviceId.equals(serverInfoService.getId())) {
			return serverInfoService;
		}
		return null;
	}

	@Override
	public UPnPService[] getServices() {
		return services;
	}

	@Override
	public UPnPIcon[] getIcons(String locale) {
		return new UPnPIcon[0];
	}

	@Override
	public Dictionary<String,Object> getDescriptions(String locale) {
		return dictionary;
	}

}
