package appsgate.lig.proxy.PhilipsHUE;

import java.util.ArrayList;
import java.util.HashMap;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.json.JSONException;
import org.json.JSONObject;
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
	 * Philips HUE bridge with error map
	 * key is the bridge current IP address and
	 * the value is the JSON description of the related error
	 */
	private HashMap<String, JSONObject> errorBridgeMap = new HashMap<String, JSONObject>();
	
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
			
			String ipAddr = splitString.toString();
			//TODO the contain test is used to deal with UPnP stack announce problem
			if(adapter != null && !bridgeIp.contains(ipAddr)) {
				bridgeIp.add(ipAddr);
				JSONObject[] error = new JSONObject[1];
				boolean associated = adapter.isAssociated(ipAddr, error);
				
				if(associated) {
					String macAddr = adapter.getBridgeMacAddress(ipAddr);
					logger.debug(", and mac: "+macAddr);
					adapter.notifyNewBridge(macAddr, ipAddr);
				}else{
					logger.debug(error[0].toString());
					try {
						JSONObject errorDetail = error[0].getJSONObject("error");
						errorBridgeMap.put(ipAddr, errorDetail);
					} catch (JSONException e) {e.printStackTrace();} 
				}
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

	/**
	 * Get the discovered bridge IP address list
	 * @return the IP address list as an ArrayList<String>
	 */
	public ArrayList<String> getBridgesIp() {
		return bridgeIp;
	}
	
	/**
	 * get all available HUE bridge IP, all that AppsGate is associated
	 * with.
	 * @return a sub ArrayList<String> of bridgeIp.
	 */
	public ArrayList<String> getAvailableBridgesIp() {
		ArrayList<String> availableBridgeIp = new ArrayList<String>();
		
		for(String ip : bridgeIp){
			if(!isStatusError(ip)){
				availableBridgeIp.add(ip);
			}
		}
		
		return availableBridgeIp;
	}
	
	/**
	 * get the error detail for a specify bridge
	 * @param bridgeIP the IP address of the HUE bridge
	 * @return the error details as a JSONObject
	 */
	public JSONObject getErrorDetails(String bridgeIP) {
		return errorBridgeMap.get(bridgeIP);
	}
	
	/**
	 * Check if the bridge is correctly set up
	 * @param ip the bridge IP to check
	 * @return true if the bridge is correctly set up, false otherwise
	 */
	public boolean isStatusError(String ip) {
		return errorBridgeMap.containsKey(ip);
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
	
	/**
	 * Remove a bridge from error bridge list
	 * @param ipAddr the bridge IP address
	 * @return true if the bridge is correctly removed.
	 */
	public boolean removeBridgeFromErrorMap(String ipAddr) {
		return errorBridgeMap.remove(ipAddr) != null;
	}

	
}