package appsgate.lig.proxy.PhilipsHUE;

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

	private static String PHILIPS_BRIDGE_UPNP_TYPE = "urn:schemas-upnp-org:device:Basic:1";
	private static String BRIDGE_NAME 			   = "Philips hue";

	private String bridgeIP = "127.0.0.0.1";

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsBridgeUPnPFinder.class);

	public PhilipsBridgeUPnPFinder() {
		super();
		addDeviceChangeListener(this);
	}

	@Override
	public void deviceAdded(Device device) {
		if(device.getDeviceType().contentEquals(PHILIPS_BRIDGE_UPNP_TYPE) &&
				device.getFriendlyName().contains(BRIDGE_NAME)) {
			logger.debug("Finder found a Philips bridge on local network");
			String philipsNameIP = device.getFriendlyName();
			CharSequence splitString = device.getFriendlyName().subSequence(13, philipsNameIP.length()-1);
			logger.debug("Current bridge IP: "+splitString);
			bridgeIP = splitString.toString();
		}
	}

	@Override
	public void deviceRemoved(Device device) {
		if(device.getDeviceType().contentEquals(PHILIPS_BRIDGE_UPNP_TYPE) &&
				device.getFriendlyName().contains(BRIDGE_NAME)) {
			logger.debug("The Philips bridge on local netork is not reachable, former IP was "+ bridgeIP);
			bridgeIP = "127.0.0.0.1";
		}
	}

	public String getBridgeIp() {
		return bridgeIP;
	}
}