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
	int coreObjectStatus = 0;
	String coreObjectId;
	String name;

	
	public static final String KEY_COLOR = "color";
	public static final String KEY_LEDS = "leds";
	public static final String KEY_ID = "id";
	
	public static final String IMPL_NAME = "FairyLightsImpl";
	
	Set<String> currentLights;
	
	public FairyLightsImpl() {
		logger.trace("FairyLightsImpl(), default constructor");
		currentLights = new HashSet<String>();
	}
	
	public void configure(String host, JSONArray lights) {
		LumiPixelImpl.setHost(host);
		setAffectedLights(lights);
	}
	
	public void setAffectedLights(JSONArray lights) {
		if(lights != null) {
			currentLights.clear();
			for(int i = 0 ; i < lights.length(); i++) {
				currentLights.add(lights.optString(i));
			}
		}
	}
	
	@Override
	public JSONArray getLightsStatus() {
		logger.trace("getAllLights()");
		JSONArray response = LumiPixelImpl.getAllLights(); 

		logger.trace("getAllLights(), returning {}",response);
		return response;
	}

	@Override
	public String getOneLight(int lightNumber) {
		logger.trace("getOneLight(int lightNumber : {})", lightNumber);
		// TODO test if the light is in the group
		
		return LumiPixelImpl.getOneLight(lightNumber);
	}

	@Override
	public JSONObject setOneColorLight(int lightNumber, String color) {
		logger.trace("setColorLight(int lightNumber : {}, String color : {})", lightNumber, color);
		// TODO test if the light is in th group

		JSONObject response = LumiPixelImpl.setOneColorLight(lightNumber, color);
		logger.trace("setColorLight(...), returning {}",response);
		stateChanged("ledChanged", null, color, getAbstractObjectId());
		return response;
	}

	
	@Override
	public JSONArray setAllColorLight(String color) {
		logger.trace("setAllColorLight(String color : {})", color);

		
		JSONArray cache = getLightsStatus();
		int length = cache.length();
		
		for(int i = 0; i< length; i++) {
			LumiPixelImpl.setOneColorLight(i, color);
		}
		JSONArray response = getLightsStatus();

		stateChanged(KEY_LEDS, null, response.toString(), getAbstractObjectId());
		return response;
	}

	@Override
	public JSONArray setColorPattern(JSONArray pattern) {
		logger.trace("setColorPattern(JSONObject pattern : {})", pattern);
		JSONArray response = LumiPixelImpl.setColorPattern(pattern);

		stateChanged(KEY_LEDS, null, response.toString(), getAbstractObjectId());
		return response;

	}

	@Override
	public void singleChaserAnimation(int start, int end, String color) {
		logger.trace("singleChaserAnimation(int start : {}, int end : {}, String color : {})", start, end, color);
		
		JSONArray cache = getLightsStatus();
		
		if(start < end) {
			for(int i = start; i<= end; i++) {
				if (i> start) {
					LumiPixelImpl.setOneColorLight(i-1, cache.getJSONObject(i-1).getString(KEY_COLOR));
				}
				LumiPixelImpl.setOneColorLight(i, color);
			}
			LumiPixelImpl.setOneColorLight(end, cache.getJSONObject(end).getString(KEY_COLOR));
		} else {
			for(int i = start; i>= end; i--) {
				if (i<start) {
					LumiPixelImpl.setOneColorLight(i+1, cache.getJSONObject(i+1).getString(KEY_COLOR));
				}
				LumiPixelImpl.setOneColorLight(i, color);
			}
			LumiPixelImpl.setOneColorLight(end, cache.getJSONObject(end).getString(KEY_COLOR));
		}
	}

	@Override
	public void roundChaserAnimation(int start, int end, String color, int rounds) {
		logger.trace("roundChaserAnimation(int start, int end, String color, int rounds : {})", start, end, color, rounds);

		for (int i = 0; i< rounds; i+=2) {
			singleChaserAnimation(start, end, color);
			singleChaserAnimation(end, start, color);
		}
	}	


	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", getAbstractObjectId());
		descr.put("type", getUserType()); 
		descr.put("status", getObjectStatus());
		descr.put(KEY_LEDS, getLightsStatus());

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

	@Override
	public JSONArray getLightsIndexes() {
		// TODO Auto-generated method stub
		return null;
	}


	
}
