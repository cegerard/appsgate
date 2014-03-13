package appsgate.lig.proxy.PhilipsHUE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.philips.lighting.hue.listener.PHBridgeConfigurationListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.exception.PHHueException;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;

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
	// private final static int AUTHENT_BRIDGE = 2;
	private int status;
	
	Timer timer;
	
	// By default, launch one research each 20 secs
	public final long APSEARCH_DELAY = 20000;

	/**
	 * Java reference on the Philips HUE adapter that manage Philips HUE lights
	 */
	private PhilipsHUEAdapter adapter;

	/**
	 * Philips HUE access point thaht need an authorization
	 */
	private ArrayList<PHAccessPoint> unAuthorizedAccesPointList = new ArrayList<PHAccessPoint>();

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(PhilipsBridgeUPnPFinder.class);

	/**
	 * Default constructor for Philips HUE finder
	 */
	public PhilipsBridgeUPnPFinder(PhilipsHUEAdapter philipsHUEAdapter,
			PHHueSDK phHueSDK) {
		logger.debug("new PhilipsBridgeUPnPFinder()");
		status = IDLE;
		adapter = philipsHUEAdapter;
		this.phHueSDK = phHueSDK;
	}

	public void start() {
		logger.debug("start()");
		phHueSDK.getNotificationManager().registerSDKListener(this);
		logger.debug("Listener registered !");

		sm = (PHBridgeSearchManager) phHueSDK
				.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		

		APSearchTask nextAPSearch = new APSearchTask();
		if (timer != null)
			timer.cancel();
		timer = new Timer();
		timer.schedule(nextAPSearch, 1000);

		status = SEARCHING_AP;
	}

	public void stop() {
		logger.debug("stop()");
		phHueSDK.getNotificationManager().unregisterSDKListener(this);
		status = IDLE;
	}

	/**
	 * Get all access points that need an authorization
	 * 
	 * @return the unauthorized access points list as an
	 *         ArrayList<PHAccessPoint>
	 */
	public ArrayList<PHAccessPoint> getUnauthorizedAccessPoints() {
		return unAuthorizedAccesPointList;
	}

	/**
	 * Get an unauthorized access point from its IP
	 * 
	 * @param bridgeIP
	 *            the bridge IP address
	 * @return the corresponding PHAccessPoint instance
	 */
	public PHAccessPoint getUnauthorizedAccessPoint(String bridgeIP) {
		PHAccessPoint returnedAp = null;
		for (PHAccessPoint ap : unAuthorizedAccesPointList) {
			if (ap.getIpAddress().contentEquals(bridgeIP)) {
				returnedAp = ap;
				break;
			}
		}
		return returnedAp;
	}

	@Override
	public void onAccessPointsFound(List<PHAccessPoint> aps) {

		for (PHAccessPoint ap : aps) {

			String ipAddr = ap.getIpAddress();
			String macAddr = ap.getMacAddress();
			String userName = ap.getUsername();

			logger.debug("Found Access Point with IP : " + ipAddr
					+ ", mac address : " + macAddr + ", and username : "
					+ userName);

			ap.setUsername(phHueSDK.getDeviceName());
			try {
				phHueSDK.connect(ap);
			} catch (PHHueException phe) {
				phe.printStackTrace();
			}
		}
	}

	@Override
	public void onAuthenticationRequired(PHAccessPoint ap) {
		logger.warn("Authentication required for " + ap.getIpAddress()
				+ ", mac: " + ap.getMacAddress() + " username: "
				+ ap.getUsername());
		ap.setUsername(PhilipsHUEAdapter.APP_NAME);
		unAuthorizedAccesPointList.add(ap);
	}

	@Override
	public void onBridgeConnected(PHBridge pb) {
		phHueSDK.enableHeartbeat(pb, 30000);
		PHBridgeConfiguration phbc = pb.getResourceCache()
				.getBridgeConfiguration();
		logger.info("Bridge connected: " + phbc.getIpAddress());
		pb.getBridgeConfigurations(new BridgeConfListener(pb));
		adapter.notifyNewBridge(pb);
		removeBridgeFromUnauthorizedList(phbc.getIpAddress());
		status = IDLE;
	}

	@Override
	public void onCacheUpdated(int flag, PHBridge bridge) {
		PHBridgeConfiguration bc = bridge.getResourceCache()
				.getBridgeConfiguration();
		logger.debug("Cache updated: " + flag + " for " + bc.getIpAddress());

		for (PHLight light : bridge.getResourceCache().getAllLights()) {
			Instance inst = adapter.getSensorInstance(bc.getMacAddress() + "-"
					+ light.getIdentifier());
			PHLightState lightState = light.getLastKnownLightState();
			Map<String, String> properties = new HashMap<String, String>();
			String deviceID = bc.getMacAddress() + "-" + light.getIdentifier();

			if (inst != null) {
				if (!light.isReachable()) {
					inst.setProperty("reachable", "false");
					((ComponentBrokerImpl) CST.componentBroker)
							.disappearedComponent(inst.getName());
					adapter.removeInSensorInstance(deviceID);
				} else {
					adapter.initiateLightStateProperties(properties, lightState);
					inst.setAllProperties(properties);
				}
			} else { // no ApAM instance
				if (light.isReachable()) {
					// Instantiate the light
					adapter.instanciateHUELight(bridge, light);
				}
			}
		}
	}

	@Override
	public void onConnectionLost(PHAccessPoint ap) {
		logger.debug("Connexion lost with PhilipsHUE bridge: "
				+ ap.getIpAddress());
		if (adapter != null) {
			adapter.notifyOldBridge(ap);
		}
	}

	@Override
	public void onConnectionResumed(PHBridge pb) {
		// PHBridgeConfiguration phbc =
		// pb.getResourceCache().getBridgeConfiguration();
		// logger.info("Bridge connection resumed: "+phbc.getIpAddress());
	}

	@Override
	public void onError(int code, String message) {
		try {
			JSONObject resp = new JSONObject();
			if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
				logger.error("BRIDGE NOT RESPONDING");

			} else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
				logger.warn("Bridge pushlink button not pressed");
				resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
				JSONObject content = new JSONObject();
				content.put("header", "Push link button not pressed");
				content.put("text", "you must to push the link button !");
				resp.put("hueToastAlert", content);
				adapter.getCommunicationService().send(resp.toString());

			} else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
				logger.error("BRIDGE AUTHENTICATION FAILED");
				resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
				JSONObject content = new JSONObject();
				content.put("header", "Authentication failed");
				content.put("text", message);
				resp.put("hueToastAlert", content);
				adapter.getCommunicationService().send(resp.toString());

			} else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
				logger.error("BRIDGE NOT FOUND");
				if (status == SEARCHING_AP) {
					APSearchTask nextAPSearch = new APSearchTask();
					if (timer != null)
						timer.cancel();
					timer = new Timer();
					timer.schedule(nextAPSearch, APSEARCH_DELAY);

				}

			} else if (code != PHHueError.BRIDGE_ALREADY_CONNECTED) { // We just
																		// ignore
																		// the
																		// bridge
																		// already
																		// connected
																		// error
																		// case
				logger.debug("onError(int code : " + code
						+ ", String message : " + message + ")");
				// if (status == SEARCHING_AP) {
				// sm.upnpSearch();
				// }
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove a bridge from unauthorized bridge list
	 * 
	 * @param ipAddr
	 *            the bridge IP address
	 */
	private void removeBridgeFromUnauthorizedList(String ipAddr) {
		PHAccessPoint paToRemove = null;
		for (PHAccessPoint pa : unAuthorizedAccesPointList) {
			if (pa.getIpAddress().contentEquals(ipAddr)) {
				paToRemove = pa;
				break;
			}
		}

		if (paToRemove != null) {
			try {
				unAuthorizedAccesPointList.remove(paToRemove);
				JSONObject resp = new JSONObject();
				resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
				resp.put("bridgeConnected", ipAddr);
				adapter.getCommunicationService().send(resp.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/***********************************************/
	/** INNER CLASS **/
	/***********************************************/

	/**
	 * Inner class use to get update for the bridge configuration
	 * 
	 * @author Cédric Gérard
	 * @since January 9, 2014
	 * @version 0.0.1
	 */
	private class BridgeConfListener extends PHBridgeConfigurationListener {

		/**
		 * The Philips HUE bridge reference
		 */
		private PHBridge phBridge;

		/**
		 * Build a Philips HUE bridge configuration listener
		 * 
		 * @param phBridge
		 *            the bridge reference
		 */
		public BridgeConfListener(PHBridge phBridge) {
			super();
			this.phBridge = phBridge;
		}

		@Override
		public void onError(int type, String description) {
			logger.debug("BridgeConfListener -- ON ERROR "
					+ phBridge.getResourceCache().getBridgeConfiguration()
							.getIpAddress());
		}

		@Override
		public void onStateUpdate(Hashtable<String, String> state,
				List<PHHueError> errors) {
			logger.debug("BridgeConfListener -- ON STATE UPDATE "
					+ phBridge.getResourceCache().getBridgeConfiguration()
							.getIpAddress());
			for (String k : state.keySet()) {
				String value = state.get(k);
				logger.debug(k + " --> " + value);
			}
		}

		@Override
		public void onSuccess() {
			logger.debug("BridgeConfListener -- ON SUCCESS"
					+ phBridge.getResourceCache().getBridgeConfiguration()
							.getIpAddress());
		}
	}

	// Dedicated task to launch AP research (allowing to launch research in the
	// future)
	class APSearchTask extends TimerTask {
		@Override
		public void run() {
			logger.debug("Lauching AP Search");
			sm.upnpSearch();
		}
	}

}
