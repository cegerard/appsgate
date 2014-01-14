package appsgate.lig.proxy.PhilipsHUE;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.philips.lighting.hue.listener.PHBridgeConfigurationListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.exception.PHHueException;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHHueError;

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
	//private final static int AUTHENT_BRIDGE = 2;
	private int status;


	/**
	 * Java reference on the Philips HUE adapter that
	 * manage Philips HUE lights
	 */
	 private PhilipsHUEAdapter adapter;
	 
	/**
	 * Philips HUE access point thaht need an authorization
	 */
	private ArrayList<PHAccessPoint> unAuthorizedAccesPointList = new ArrayList<PHAccessPoint>();

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsBridgeUPnPFinder.class);

	/**
	 * Default constructor for Philips HUE finder
	 */
	public PhilipsBridgeUPnPFinder(PhilipsHUEAdapter philipsHUEAdapter, PHHueSDK phHueSDK) {
		logger.debug("new PhilipsBridgeUPnPFinder()");
		status = IDLE;
		adapter = philipsHUEAdapter;
		this.phHueSDK = phHueSDK;
	}

	public void start() {
		logger.debug("start()");
		phHueSDK.getNotificationManager().registerSDKListener(this);
		logger.debug("Listener registered !");

		sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.upnpSearch();
		status = SEARCHING_AP;
	}		

	public void stop() {
		logger.debug("stop()");
		phHueSDK.getNotificationManager().unregisterSDKListener(this);
		status = IDLE;
	}
	
//	/**
//	 * Get all available HUE bridge, all that AppsGate is associated
//	 * with.
//	 * @return a sub ArrayList<PHBridge> of all accessPoint.
//	 */
//	public ArrayList<PHBridge> getAvailableBridges() {
//		return phHueSDK.getAllBridges();
//	}

	/**
	 * Get all access points that need an authorization
	 * @return the unauthorized access points list as an ArrayList<PHAccessPoint>
	 */
	public ArrayList<PHAccessPoint> getUnauthorizedAccessPoints() {
		return unAuthorizedAccesPointList;
	}

	@Override
	public void onAccessPointsFound(List<PHAccessPoint> aps) {

		for (PHAccessPoint ap : aps) {
			
			String ipAddr = ap.getIpAddress();
			String macAddr = ap.getMacAddress();
			String userName =  ap.getUsername();
			
			logger.debug("Found Access Point with IP : " + ipAddr
					+ ", mac address : " + macAddr + ", and username : "
					+ userName);
			
			ap.setUsername(phHueSDK.getDeviceName());
			try{
				phHueSDK.connect(ap);
			}catch(PHHueException phe){phe.printStackTrace();}
		}
	}

	@Override
	public void onAuthenticationRequired(PHAccessPoint ap) {
		logger.warn("Authentication required for "+ap.getIpAddress()+", mac: "+ap.getMacAddress()+" username: "+ap.getUsername());
		unAuthorizedAccesPointList.add(ap);
	}

	@Override
	public void onBridgeConnected(PHBridge pb) {
		phHueSDK.enableHeartbeat(pb, 30000);
		PHBridgeConfiguration phbc = pb.getResourceCache().getBridgeConfiguration();
		logger.info("Bridge connected: "+phbc.getIpAddress());
		pb.getBridgeConfigurations(new BridgeConfListener(pb));
		adapter.notifyNewBridge(pb);
	}

	@Override
	public void onCacheUpdated(int arg0, PHBridge arg1) {
//		logger.debug("Cache updated: "+arg0+" for "+arg1.getResourceCache().getBridgeConfiguration().getIpAddress());
	}

	@Override
	public void onConnectionLost(PHAccessPoint ap) {
		logger.debug("Connexion lost with PhilipsHUE bridge: "+ap.getIpAddress());
		if(adapter != null ) {
			adapter.notifyOldBridge(ap);
		}
	}

	@Override
	public void onConnectionResumed(PHBridge pb) {
//		PHBridgeConfiguration phbc = pb.getResourceCache().getBridgeConfiguration();
//		logger.info("Bridge connexion resumed: "+phbc.getIpAddress());
	}

	@Override
	public void onError(int code, String message) {
		//27 error code stand for connection on already connected bridge
		if(code != 27) { //We just ignore the 27 error case
			logger.debug("onError(int code : " + code + ", String message : "+ message + ")");
			if (status == SEARCHING_AP) {
				sm.upnpSearch();
			}
		}
	}

	/**
	 * Remove a bridge from unauthorized bridge list
	 * @param ipAddr the bridge IP address
	 * @return true if the bridge is correctly removed.
	 */
	private boolean removeBridgeFromUnauthorizedList(String ipAddr) {
		PHAccessPoint paToRemove = null;
		for(PHAccessPoint pa : unAuthorizedAccesPointList) {
			if(pa.getIpAddress().contentEquals(ipAddr)){
				paToRemove = pa;
				break;
			}
		}
		return unAuthorizedAccesPointList.remove(paToRemove);
	}
	
	/***********************************************/
	/** 		    INNER CLASS					  **/
	/***********************************************/
	
	/**
	 * Inner class use to get update for the bridge configuration
	 * @author Cédric Gérard
	 * @since January 9, 2014
	 * @version 0.0.1
	 */
	private class BridgeConfListener extends PHBridgeConfigurationListener{

		/**
		 * The Philips HUE bridge reference
		 */
		private PHBridge phBridge;
		
		/**
		 * Build a Philips HUE bridge configuration listener
		 * @param phBridge the bridge reference
		 */
		public BridgeConfListener(PHBridge phBridge) {
			super();
			this.phBridge = phBridge;
		}

		@Override
		public void onError(int type, String description) {
			logger.debug("BridgeConfListener -- ON ERROR "+phBridge.getResourceCache().getBridgeConfiguration().getIpAddress());
		}

		@Override
		public void onStateUpdate(Hashtable<String, String> state, List<PHHueError> errors) {
			logger.debug("BridgeConfListener -- ON STATE UPDATE "+ phBridge.getResourceCache().getBridgeConfiguration().getIpAddress());
			for(String k : state.keySet()){
				String value = state.get(k);
				logger.debug(k+" --> "+value);
			}
		}

		@Override
		public void onSuccess() {
			logger.debug("BridgeConfListener -- ON SUCCESS" +phBridge.getResourceCache().getBridgeConfiguration().getIpAddress());
		}
	}
	
}
