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
	public static final String KEY_CURRENT_LIGHT = "currentLight";
	public static final String KEY_CURRENT_COLOR = "currentColor";
	
	public static final String IMPL_NAME = "FairyLightsImpl";
	
	Set<Integer> currentLights;
	
	Object lock = new Object();
	boolean configured = false;
	
	int currentLight = 0;
	String currentColor = "#000000";
	
	
	public FairyLightsImpl() {
		logger.trace("FairyLightsImpl(), default constructor");
		currentLights = new HashSet<Integer>();
	}
	
	LightManagement lightManager;
	
	public void configure(LightManagement lightManager, JSONObject configuration) {
		logger.trace("configure(LightManagement lightManager : {}, JSONObject configuration : {})", lightManager, configuration);

		this.lightManager = lightManager;
		setAffectedLights(configuration.getJSONArray(KEY_LEDS));
		this.currentColor = configuration.optString(KEY_CURRENT_COLOR, "#000000");
		this.currentLight = configuration.optInt(KEY_CURRENT_LIGHT, 0);
		configured = true;
	}
	
	public void setAffectedLights(JSONArray lights) {
		if(lights != null) {
			currentLights.clear();
			for(int i = 0 ; i < lights.length(); i++) {
				if(lights.getJSONObject(i) != null
						&& lights.getJSONObject(i).optInt(KEY_ID,-1)>=0) {
					int lightNumber = lights.getJSONObject(i).getInt(KEY_ID);
					if(lightNumber >=0 && lightManager.affect(getAbstractObjectId(), lightNumber)) {
						currentLights.add(lightNumber);
					}
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
		setCurrentColor(color);
		setCurrentLightNumber(lightNumber);
		stateChanged(KEY_LEDS, null, getLightsStatus().toString(), getAbstractObjectId());
		
		stateChanged("ledChanged", null, String.valueOf(currentLight), getAbstractObjectId());
		
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
		setCurrentColor(color);
		setCurrentLightNumber(LightManagement.FAIRYLIGHT_SIZE-1);
		return response;
	}
	
	@Override
	public JSONArray setColorAnimation(int start, int end, String color) {
		logger.trace("setColorAnimation(int start : {}, int end : {}, String color : {})",
				start, end, color);
				
		JSONArray response = lightManager.setColorAnimation(getAbstractObjectId(), start, end, color);
		
		stateChanged(KEY_LEDS, null, response.toString(), getAbstractObjectId());
		setCurrentColor(color);
		setCurrentLightNumber(end);		
		return response;
	}

	@Override
	public JSONArray setColorPattern(JSONArray pattern) {
		logger.trace("setColorPattern(JSONObject pattern : {})", pattern);
		JSONArray response = lightManager.setColorPattern(getAbstractObjectId(), pattern);
		
		if(response != null
				&& response.length()>0
				&& response.getJSONObject(response.length()-1) != null
				&& response.getJSONObject(response.length()-1).optString(KEY_COLOR) != null
				&& response.getJSONObject(response.length()-1).optInt(KEY_ID, -1) >= 0) {

			logger.trace("setColorPattern(....), pattern successfully applied");
			stateChanged(KEY_LEDS, null, response.toString(), getAbstractObjectId());
			setCurrentColor(response.getJSONObject(response.length()-1).getString(KEY_COLOR));
			setCurrentLightNumber(response.getJSONObject(response.length()-1).getInt(KEY_ID));	
			return response; 
		} else {
			logger.warn("setColorPattern(....), problem applying pattern");
			return null;
		}

	}

	@Override
	public void singleChaserAnimation(int start, int end, String color, int tail) {
		logger.trace("singleChaserAnimation(int start : {}, int end : {}, String color : {}, int tail: {})", start, end, color, tail);
		lightManager.singleChaserAnimation(getAbstractObjectId(), start, end, color, tail);
		setCurrentColor(color);
		setCurrentLightNumber(end);	
	}

	@Override
	public void roundChaserAnimation(int start, int end, String color, int tail, int rounds) {
		logger.trace("roundChaserAnimation(int start, int end, String color, int tail : {},  int rounds : {})", start, end, color, tail, rounds);
		lightManager.roundChaserAnimation(getAbstractObjectId(), start, end, color, tail, rounds);
		setCurrentColor(color);
		setCurrentLightNumber(start);	
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
			descr.put(KEY_CURRENT_LIGHT, getCurrentLightNumber());
			descr.put(KEY_CURRENT_COLOR, getCurrentColor());	
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

	@Override
	public int getCurrentLightNumber() {
		return currentLight;
	}

	@Override
	public String getCurrentColor() {
		return currentColor;
	}

	@Override
	public void setCurrentLightNumber(int lightNumber) {
		logger.trace("setCurrentLightNumber(int lightNumber : {})", lightNumber);
		int oldValue = this.currentLight;
		if(lightNumber < 0) {
			this.currentLight= 0;
		} else if (lightNumber>= LightManagement.FAIRYLIGHT_SIZE) {
			this.currentLight= LightManagement.FAIRYLIGHT_SIZE-1;			
		} else {
			this.currentLight = lightNumber;
		}
		stateChanged(KEY_CURRENT_LIGHT, String.valueOf(oldValue), String.valueOf(currentLight), getAbstractObjectId());
	}

	@Override
	public void setCurrentColor(String color) {
		logger.trace("setCurrentColor(String color : {})", color);
		
		String oldValue = this.currentColor;
		this.currentColor = color;
		stateChanged(KEY_CURRENT_LIGHT, oldValue, currentColor, getAbstractObjectId());
	}

	@Override
	public void changeContiguousLights(int nb, String color) {
		logger.trace("changeNextPreviousLights(int nb : {}, String color : {})", nb, color);
		currentColor= color;
		synchronized (lock) {
			int i = 0;
			while(i < Math.abs(nb)) {
				logger.trace("changeNextPreviousLights(...), i : {}, Math.abs(nb): {}", i, Math.abs(nb));
				setOneColorLight(currentLight, color);
				if(nb > 0 && (currentLight+1) >= LightManagement.FAIRYLIGHT_SIZE) {
					currentLight = LightManagement.FAIRYLIGHT_SIZE;
					i = nb;
				} else if (nb < 0 && currentLight <= 0 ) {
					currentLight = 0;
					i = Math.abs(nb);				
				} else if (nb > 0) {
					currentLight++;
				} else {
					currentLight--;
				}
				i++;
			}	
		}
	}
}
