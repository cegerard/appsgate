package appsgate.lig.proxy.PhilipsHUE;

import java.util.ArrayList;
import java.util.List;

//import org.cybergarage.upnp.ControlPoint;
//import org.cybergarage.upnp.Device;
//import org.cybergarage.upnp.device.DeviceChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;

//import com.philips.lighting.hue.sdk.PHHueSDK;
//import com.philips.lighting.hue.sdk.PHSDKListener;

/**
 * This class is design to find with UPnP discovery protocol the Philips bridge
 * for HUE light
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 23, 2013
 * 
 */
public class PhilipsBridgeUPnPFinder implements PHSDKListener {

	private PHHueSDK phHueSDK;
	private PHBridgeSearchManager sm;
	private final static int IDLE = 0;
	private final static int SEARCHING_AP = 1;
	private final static int AUTHENT_BRIDGE = 2;

	private int status;

	/**
	 * UPnP device UDN
	 */
	private static String PHILIPS_BRIDGE_UPNP_TYPE = "urn:schemas-upnp-org:device:Basic:1";

	/**
	 * Philips HUE friendly device name
	 */
	private static String BRIDGE_NAME = "Philips hue";

	/**
	 * Philips HUE bridge IP address list
	 */
	private ArrayList<String> bridgeIp = new ArrayList<String>();

	/**
	 * Java reference on the Philips HUE adapter that manage Philips HUE lights
	 */
	private PhilipsHUEAdapter adapter;

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(PhilipsBridgeUPnPFinder.class);

	/**
	 * Default constructor for Philips HUE finder
	 */
	public PhilipsBridgeUPnPFinder() {
		logger.debug("new PhilipsBridgeUPnPFinder()");
		status = IDLE;
		adapter = null;
		phHueSDK = PHHueSDK.create();
	}

	public void start() {
		logger.debug("start()");
		phHueSDK.getNotificationManager().registerSDKListener(this);
		logger.debug("Listener registered !");

		sm = (PHBridgeSearchManager) phHueSDK
				.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.upnpSearch();
		status = SEARCHING_AP;
	}

	public void stop() {
		logger.debug("stop()");
		status = IDLE;

	}

	/**
	 * Register the Philips HUE adapter reference
	 * 
	 * @param philipsHUEAdapter
	 *            the Philips HUE reference to register
	 */
	public void registrer(PhilipsHUEAdapter philipsHUEAdapter) {
		adapter = philipsHUEAdapter;
	}

	/**
	 * Unregister the current Philips HUE reference
	 * 
	 * @param philipsHUEAdapter
	 *            the Philips HUE reference that have been deleted
	 */
	public void unregistrer(PhilipsHUEAdapter philipsHUEAdapter) {
		adapter = null;
	}

	public ArrayList<String> getBridgesIp() {
		return bridgeIp;
	}

	@Override
	public void onAccessPointsFound(List<PHAccessPoint> aps) {

		for (PHAccessPoint ap : aps) {
			String ipAddr = ap.getIpAddress();
			String macAddr = ap.getMacAddress();
			logger.debug("Found Access Point with IP : " + ipAddr
					+ ", mac address : " + macAddr + ", and username : "
					+ ap.getUsername());
			// TODO: this one not sufficient, should add only after
			// authentication
			if (adapter != null && !bridgeIp.contains(ipAddr)) {
				bridgeIp.add(ipAddr);
				adapter.notifyNewBridge(macAddr, ipAddr);
			}
		}
	}

	@Override
	public void onAuthenticationRequired(PHAccessPoint arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBridgeConnected(PHBridge arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCacheUpdated(int arg0, PHBridge arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionLost(PHAccessPoint arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionResumed(PHBridge arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int code, String message) {
		logger.debug("onError(int code : " + code + ", String message : "
				+ message + ")");
		if (status == SEARCHING_AP) {
			sm.upnpSearch();
		}

	}

}

// extends ControlPoint implements DeviceChangeListener {

//
// /**
// * Default constructor for Philips HUE finder
// */
// public PhilipsBridgeUPnPFinder() {
// super();
// addDeviceChangeListener(this);
// adapter = null;
// }
//
// @Override
// public void deviceAdded(Device device) {
// if(device.getDeviceType().contentEquals(PHILIPS_BRIDGE_UPNP_TYPE) &&
// device.getFriendlyName().contains(BRIDGE_NAME)) {
// logger.debug("Finder found a Philips bridge on local network");
// String philipsNameIP = device.getFriendlyName();
// CharSequence splitString = device.getFriendlyName().subSequence(13,
// philipsNameIP.length()-1);
// logger.debug("New bridge with IP: "+splitString);
//
// String ipAddr = splitString.toString();
// //TODO the contain test is used to deal with UPnP announce problem
// if(adapter != null && !bridgeIp.contains(ipAddr)) {
// String macAddr = adapter.getBridgeMacAddress(ipAddr);
// logger.debug(", and mac: "+macAddr);
// bridgeIp.add(ipAddr);
// adapter.notifyNewBridge(macAddr, ipAddr);
// }
// }
// }
//
// @Override
// public void deviceRemoved(Device device) {
// if(device.getDeviceType().contentEquals(PHILIPS_BRIDGE_UPNP_TYPE) &&
// device.getFriendlyName().contains(BRIDGE_NAME)) {
// String philipsNameIP = device.getFriendlyName();
// CharSequence splitString = device.getFriendlyName().subSequence(13,
// philipsNameIP.length()-1);
// logger.debug("A Philips HUE removed UPnP message wwas received.");
// //TODO this test is just because we have a problem with the UPnP stack
// if(adapter.getLightState((String) splitString, "1") == null) {
// logger.debug("A Philips bridge on local netork is not reachable, former IP was "+
// splitString);
// bridgeIp.remove(splitString.toString());
// if(adapter != null ) {
// adapter.notifyOldBridge(splitString.toString());
// }
// }
// }
// }

// }