package HUESdkTest;

import static org.junit.Assert.*;

import java.util.List;

import javax.swing.JFrame;

import org.junit.Test;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.connection.impl.PHBridgeInternal;
import com.philips.lighting.hue.sdk.utilities.impl.PHLog;
import com.philips.lighting.model.PHBridge;

public class FindBridgeTest implements PHSDKListener {

	private PHHueSDK phHueSDK;
	private int countdown;

	PHBridgeSearchManager sm;
	String myDeviceId;

	private static final int DEFAULT_TIMER = 30;

	private int status = NONE;
	private static final int NONE = 0;
	private static final int AP_SEARCH = 1;
	private static final int AUTHENTICATE = 2;
	private static final int CONNECTING = 3;

	PHAccessPoint lastAP;

	public static void main(String arg[]) {
		FindBridgeTest bridgeTest = new FindBridgeTest();
//		JFrame frame = new JFrame();
//		frame.setVisible(true);
		
		bridgeTest.test();
	}
	
	
	public void test() {
		phHueSDK = PHHueSDK.create();
		PHLog log = (PHLog) phHueSDK
				.getSDKService(PHHueSDK.LOG);
		log.setSdkLogLevel(PHLog.DEBUG);
		
		myDeviceId = new String(PHBridgeInternal.generateUniqueKey());
		System.out.println("SDK created, " + myDeviceId);

		phHueSDK.getNotificationManager().registerSDKListener(
				this);
		System.out.println("Listener registered !");

		sm = (PHBridgeSearchManager) phHueSDK
				.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.upnpSearch();
		System.out.println("search begin, 30 sec max !");

		countdown = DEFAULT_TIMER;
		status = AP_SEARCH;
		lastAP = null;

		try {
			while (countdown > 0 && status != NONE) {
				Thread.sleep(1000);
				System.out.println(countdown);
				countdown--;
//				if (lastAP != null) {
//					System.out.println("connecting");
//					phHueSDK.connect(lastAP);
//					lastAP = null;
//				}

			}

		} catch (InterruptedException exc) {
			System.out.println("Interrupted " + exc.getMessage());
		}

		PHBridge bridge = phHueSDK.getSelectedBridge();
		System.out.println("Bridge retrieved : " + bridge);

	}


		public void onAccessPointsFound(List<PHAccessPoint> aps) {
			System.out.println("onAccessPointsFound : " + aps);

			for (PHAccessPoint ap : aps) {

				System.out.println(myDeviceId);
				ap.setUsername(myDeviceId);//new String(myDeviceId.getBytes()));//"rE3AWo7D9QeUS9Vs");
				System.out.println("try to connect " + ap.getIpAddress() + ", "
						+ ap.getMacAddress() + ", " + ap.getUsername());
				phHueSDK.connect(ap);
				//lastAP = new PHAccessPoint(ap);
			}

		//	System.out.println("last AP "+lastAP.getUsername());
		}

		public void onAuthenticationRequired(PHAccessPoint ap) {
			System.out
					.println("onAuthenticationRequired : " + ap.getUsername());
			//phHueSDK.startPushlinkAuthentication(ap);

			System.out.println("waiting to press button");
			status = AUTHENTICATE;
			countdown = DEFAULT_TIMER;

		}

		public void onBridgeConnected(PHBridge bridge) {
			System.out.println("onBridgeConnected : " + bridge);
			status = NONE;

		}

		public void onCacheUpdated(int val, PHBridge bridge) {
			System.out.println("onCacheUpdated : " + bridge);

		}

		public void onConnectionLost(PHAccessPoint ap) {
			System.out.println("onConnectionLost : " + ap);

		}

		public void onConnectionResumed(PHBridge bridge) {
			System.out.println("onConnectionResumed : " + bridge);

		}

		public void onError(int val, String message) {
			System.out.println("onError (listen AP) n° " + val + " : "
					+ message);
			if (status == AP_SEARCH && countdown > 0)
				sm.upnpSearch();

		}


}
