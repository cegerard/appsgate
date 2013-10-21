package appsgate.lig.proxy.PhilipsHUE;

import java.util.ArrayList;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is design to find with UPnP discovery protocol the Philips bridge for
 * HUE light
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 23, 2013
 *
 */
public class PhilipsBridgeUPnPFinder extends ControlPoint implements  DeviceChangeListener {

	/**
	 * UPnP device UDN
	 */
	private static String PHILIPS_BRIDGE_UPNP_TYPE = "urn:schemas-upnp-org:device:Basic:1";
	
	/**
	 * Philips HUE friendly device name
	 */
	private static String BRIDGE_NAME 			   = "Philips hue";

	/**
	 * Philips HUE bridge IP address list
	 */
	private ArrayList<String> bridgeIp = new ArrayList<String>();
	
	/**
	 * Java reference on the Philips HUE adapter that
	 * manage Philips HUE lights
	 */
	private PhilipsHUEAdapter adapter;
	

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsBridgeUPnPFinder.class);

	/**
	 * Default constructor for Philips HUE finder
	 */
	public PhilipsBridgeUPnPFinder() {
		super();
		addDeviceChangeListener(this);
		adapter = null;
	}

	@Override
	public void deviceAdded(Device device) {
		if(device.getDeviceType().contentEquals(PHILIPS_BRIDGE_UPNP_TYPE) &&
				device.getFriendlyName().contains(BRIDGE_NAME)) {
			logger.debug("Finder found a Philips bridge on local network");
			String philipsNameIP = device.getFriendlyName();
			CharSequence splitString = device.getFriendlyName().subSequence(13, philipsNameIP.length()-1);
			logger.debug("New bridge with IP: "+splitString);
			
			if(adapter != null) {
				String ipAddr = splitString.toString();
				String macAddr = adapter.getBridgeMacAddress(ipAddr);
				logger.debug(", and mac: "+macAddr);
				bridgeIp.add(ipAddr);
				adapter.notifyNewBridge(macAddr, ipAddr);
			}
		}
	}

	@Override
	public void deviceRemoved(Device device) {
		if(device.getDeviceType().contentEquals(PHILIPS_BRIDGE_UPNP_TYPE) &&
				device.getFriendlyName().contains(BRIDGE_NAME)) {
			String philipsNameIP = device.getFriendlyName();
			CharSequence splitString = device.getFriendlyName().subSequence(13, philipsNameIP.length()-1);	
			logger.debug("A Philips HUE removed UPnP message wwas received.");
			//TODO this test is just because we have a problem with the UPnP stack
			if(adapter.getLightState((String) splitString, "1") == null) {
				logger.debug("A Philips bridge on local netork is not reachable, former IP was "+ splitString);
				bridgeIp.remove(splitString.toString());
				if(adapter != null ) {
					adapter.notifyOldBridge(splitString.toString());
				}
			}
		}
	}

	public ArrayList<String> getBridgesIp() {
		return bridgeIp;
	}

	/**
	 * Register the Philips HUE adapter reference
	 * @param philipsHUEAdapter the Philips HUE reference to register
	 */
	public void registrer(PhilipsHUEAdapter philipsHUEAdapter) {
		adapter = philipsHUEAdapter;
	}

	/**
	 * Unregister the current Philips HUE reference
	 * @param philipsHUEAdapter the Philips HUE reference that have been deleted
	 */
	public void unregistrer(PhilipsHUEAdapter philipsHUEAdapter) {
		adapter = null;
	}
}