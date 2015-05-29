package appsgate.lig.fairylights.service;

import java.util.HashSet;
import java.util.Set;

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
import appsgate.lig.fairylights.adapter.LightManagement;


/**
 * Core interface for the Fairy Lights (LumiPixel Device)
 * @author thibaud
 * 
 */
public class FairyLightsImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreFairyLightsSpec{
	
	private static Logger logger = LoggerFactory.getLogger(FairyLightsImpl.class);


	String host;
	int port = -1;
	
	//Apsgate Properties for CoreObjectSpec
	public static final String UserType = CoreFairyLightsSpec.class.getSimpleName();	
	int coreObjectStatus = 2;
	String coreObjectId;
	String name;

	
	public static final String KEY_COLOR = "color";
	public static final String KEY_LEDS = "leds";
	public static final String KEY_ID = "id";
	
	public static final String IMPL_NAME = "FairyLightsImpl";
	
	Set<Integer> currentLights;
	
	Object lock = new Object();
	boolean configured = false;
	
	public FairyLightsImpl() {
		logger.trace("FairyLightsImpl(), default constructor");
		currentLights = new HashSet<Integer>();
	}
	
	LightManagement lightManager;
	
	public void configure(LightManagement lightManager, JSONArray lights) {
		logger.trace("configure(LightManagement lightManager : {}, JSONArray lights : {})", lightManager, lights);

		this.lightManager = lightManager;
		setAffectedLights(lights);
		configured = true;
	}
	
	public void setAffectedLights(JSONArray lights) {
		if(lights != null) {
			currentLights.clear();
			for(int i = 0 ; i < lights.length(); i++) {
				int lightNumber = lights.optInt(i, -1);
				if(lightNumber >=0 && lightManager.affect(getAbstractObjectId(), lightNumber)) {
					currentLights.add(lightNumber);
				}
			}
		}
	}
	
	@Override
	public JSONArray getLightsStatus() {
		logger.trace("getAllLights()");
		JSONArray response = lightManager.getAllLights();
		JSONArray results = new JSONArray();
		if(response!= null && response.length()>0) {
			for(int i = 0; i< response.length(); i++) {
				if(response.optJSONObject(i) != null
						&& response.optJSONObject(i).optInt(KEY_ID,-1)>0
						&& currentLights.contains(response.optJSONObject(i).optInt(KEY_ID,-1))) {
					results.put(response.optJSONObject(i));
				}
			}
		}

		logger.trace("getAllLights(), returning {}",results);
		return results;
	}

	@Override
	public String getOneLight(int lightNumber) {
		logger.trace("getOneLight(int lightNumber : {})", lightNumber);
		if(currentLights.contains(lightNumber)) {
			return lightManager.getOneLight(lightNumber);			
		} else {
			logger.warn("getOneLight(...), light number not in the group");
			return null;
		}
	}

	@Override
	public JSONObject setOneColorLight(int lightNumber, String color) {
		logger.trace("setColorLight(int lightNumber : {}, String color : {})", lightNumber, color);
		if(currentLights.contains(lightNumber)) {

		JSONObject response = lightManager.setOneColorLight(getAbstractObjectId(), lightNumber, color);
		logger.trace("setColorLight(...), returning {}",response);
		stateChanged("ledChanged", null, color, getAbstractObjectId());
		return response;
		} else {
			logger.warn("setOneColorLight(...), light number not in the group");
			return null;
		}
	}
	
	@Override
	public JSONArray setAllColorLight(String color) {
		logger.trace("setAllColorLight(String color : {})", color);
		JSONArray response = lightManager.setAllColorLight(getAbstractObjectId(), color);
		
		stateChanged(KEY_LEDS, null, response.toString(), getAbstractObjectId());
		return response;
	}
	
	@Override
	public JSONArray setColorAnimation(int start, int end, String color) {
		logger.trace("setColorAnimation(int start : {}, int end : {}, String color : {})",
				start, end, color);
				
		JSONArray response = lightManager.setColorAnimation(getAbstractObjectId(), start, end, color);
		
		stateChanged(KEY_LEDS, null, response.toString(), getAbstractObjectId());
		return response;
	}

	@Override
	public JSONArray setColorPattern(JSONArray pattern) {
		logger.trace("setColorPattern(JSONObject pattern : {})", pattern);
		JSONArray response = lightManager.setColorPattern(getAbstractObjectId(), pattern);

		stateChanged(KEY_LEDS, null, response.toString(), getAbstractObjectId());
		return response;

	}

	@Override
	public void singleChaserAnimation(int start, int end, String color) {
		logger.trace("singleChaserAnimation(int start : {}, int end : {}, String color : {})", start, end, color);
		lightManager.singleChaserAnimation(getAbstractObjectId(), start, end, color);
	}

	@Override
	public void roundChaserAnimation(int start, int end, String color, int rounds) {
		logger.trace("roundChaserAnimation(int start, int end, String color, int rounds : {})", start, end, color, rounds);

		for (int i = 0; i< rounds; i+=2) {
			singleChaserAnimation(start, end, color);
			singleChaserAnimation(end, start, color);
		}
	}	
	
	private boolean waitForConfiguration() {
		synchronized (lock) {
			while (!configured) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return configured;
		}
	}


	@Override
	public JSONObject getDescription() throws JSONException {
		logger.trace("getDescription()");

		JSONObject descr = new JSONObject();
		descr.put("id", getAbstractObjectId());
		descr.put("type", getUserType()); 
		descr.put("status", String.valueOf(getObjectStatus()));

		if(waitForConfiguration()) {		
			descr.put(KEY_LEDS, getLightsStatus());
		}
		logger.trace("getDescription(), returning "+descr);

		return descr;
	}
	
	@Override
	public String getAbstractObjectId() {
		return coreObjectId;
	}

	@Override
	public int getObjectStatus() {
		return coreObjectStatus;
	}

	@Override
	public String getUserType() {
		return UserType;
	}
	
	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}
	
	public NotificationMsg stateChanged(String varName, String oldValue, String newValue, String source) {
		return new CoreNotificationMsg(varName, oldValue, newValue, getAbstractObjectId());
	}
}
