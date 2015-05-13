package appsgate.lig.fairylights.adapter;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.fairylights.CoreFairyLightsSpec;
import appsgate.lig.fairylights.utils.HttpUtils;


/**
 * Base implementation of the FairyLightsAdapter,
 * responsible for discovery of the FairyLightDevices and creation/update/removal of FairyLights groups
 */
public class FairyLightsAdapterImpl extends CoreObjectBehavior implements CoreObjectSpec,
FairyLightsAdapterSpec, FairyLightsDiscoveryListener {
	
	private static Logger logger = LoggerFactory.getLogger(FairyLightsAdapterImpl.class);

	String host;
	int port = -1;
	
	//Apsgate Properties for CoreObjectSpec
	public static final String UserType = FairyLightsAdapterSpec.class.getSimpleName();	
	int coreObjectStatus = 0;
	String coreObjectId;
	
	public static final String DEFAULT_HOST = "guirlande.local";	
	public static final String DEFAULT_PROTOCOL = "http://";
	public static final String LUMIPIXEL_API_URL = "/lumipixel/api/v1.0/leds";
	public static final String URL_SEP = "/";

	public static final String KEY_COLOR = "color";
	public static final String KEY_LEDS = "leds";
	public static final String KEY_ID = "id";
	
	public FairyLightsAdapterImpl() {
		logger.trace("FairyLightsAdapterImpl(), default constructor");
	}

	@Override
	public void createFreeformLightsGroup(JSONArray selectedLights) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createContiguousLightsGroup(int startingIndex, int endingIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLightsGroup(String groupId, JSONArray selectedLights) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void releaseLight(int lightIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeLightsGroup(String groupId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONArray getAvailableLights() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAbstractObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CORE_TYPE getCoreType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getObjectStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getUserType() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public void deviceAvailable(String host) {
		// Should (re)create all the configured instances
		// TODO Auto-generated method stub
	}

	@Override
	public void deviceUnavailable() {
		// Should remove the instances
		// TODO Auto-generated method stub
	}
	

	
}
