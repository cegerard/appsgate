package appsgate.lig.fairylights;

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
import appsgate.lig.fairylights.utils.HttpUtils;


/**
 * Core interface for the Fairy Lights (LumiPixel Device)
 * @author thibaud
 * 

Draft specifications, portage des interfaces REST (api Amiqual v1.0)
1. Avec son interface web:
http://guirlande.local/ <— il faut être sur le meme réseau local

2. Avec son API REST :
tout les retour sont en JSON
curl -X GET http://guirlande.local/lumipixel/api/v1.0/leds <— liste des leds
curl -X GET http://guirlande.local/lumipixel/api/v1.0/leds/0 <— retourne la valeur de la premiere LED en partant du RPi
curl -X PUT -H "Content-Type: application/json" -d '{"color": "#ffffff"}'  http://guirlande.local/lumipixel/api/v1.0/leds/3 <— change la couleur de la let no 4 en blanc
Couleurs doivent entre en hexa avec le format #RRGGBB.

Si la guirlande est lente à répondre, c’est probablement à cause de la résolution du nom par mDNS.
Pour une réactivité optimale il faut mieux utiliser l’adresse IP directement.

 */
public class FairyLightsImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreFairyLightsSpec{
	
	private static Logger logger = LoggerFactory.getLogger(FairyLightsImpl.class);


	String host;
	int port = -1;
	
	//Apsgate Properties for CoreObjectSpec
	public static final String UserType = "12";	
	int coreObjectStatus = 0;
	String coreObjectId;
	
	public static final String DEFAULT_HOST = "guirlande.local";	
	public static final String DEFAULT_PROTOCOL = "http://";
	public static final String LUMIPIXEL_API_URL = "/lumipixel/api/v1.0/leds";
	public static final String URL_SEP = "/";

	public static final String KEY_COLOR = "color";
	public static final String KEY_LEDS = "leds";
	public static final String KEY_ID = "id";
	
	public FairyLightsImpl() {
		logger.trace("FairyLightsImpl(), default constructor");
		if(HttpUtils.testURLTimeout(DEFAULT_PROTOCOL+DEFAULT_HOST+LUMIPIXEL_API_URL, 30*1000) ) {		
			try {

				Inet4Address address = (Inet4Address) Inet4Address.getByName(DEFAULT_HOST);
				host=DEFAULT_PROTOCOL + address.getHostAddress();
				coreObjectId = FairyLightsImpl.class.getSimpleName()+"-"+host;
				logger.trace("FairyLightsImpl(), the device is available, instanciation success");

			} catch (UnknownHostException e) {
				logger.info("FairyLightsImpl(), the device is NOT available,"+ e.getMessage());
				throw new RuntimeException("FairyLights unavailable, must destroy the instance");
			}
		} else {
			logger.info("FairyLightsImpl(), timeout when calling the lumipixel device, it is NOT available");
			throw new RuntimeException("FairyLights unavailable, must destroy the instance");
		}
	}
	
	@Override
	public JSONObject getAllLights() {
		logger.trace("getAllLights()");
		JSONObject response = new JSONObject(HttpUtils.sendHttpGet(host+LUMIPIXEL_API_URL)); 

		logger.trace("getAllLights(), returning {}",response);
		return response;
	}

	@Override
	public JSONObject getOneLight(int lightNumber) {
		logger.trace("getOneLight(int lightNumber : {})", lightNumber);
		JSONObject response = new JSONObject(HttpUtils.sendHttpGet(host+LUMIPIXEL_API_URL+URL_SEP+lightNumber)); 

		logger.trace("getOneLight(...), returning {}",response);
		return response;
	}

	@Override
	public JSONObject setOneColorLight(int lightNumber, String color) {
		logger.trace("setColorLight(int lightNumber : {}, String color : {})", lightNumber, color);

		JSONObject response = setOneColorLightPrivate(lightNumber, color);
		logger.trace("setColorLight(...), returning {}",response);
		stateChanged("ledChanged", null, color, getAbstractObjectId());
		return response;
	}
	
	private JSONObject setOneColorLightPrivate(int lightNumber, String color) {
		JSONObject jsonColor = new JSONObject().put(KEY_COLOR, color);

		JSONObject response = new JSONObject(
				HttpUtils.sendHttpsPut(host+LUMIPIXEL_API_URL+URL_SEP+lightNumber, jsonColor.toString().getBytes())); 
		return response;
	}
	
	
	@Override
	public JSONObject setAllColorLight(String color) {
		logger.trace("setAllColorLight(String color : {})", color);

		
		JSONObject response = getAllLights();
		JSONArray cache = response.getJSONArray("leds");
		int length = cache.length();
		
		for(int i = 0; i< length; i++) {
			setOneColorLightPrivate(i, color);
		}
		response = getAllLights();

		stateChanged(KEY_LEDS, null, response.getJSONArray(KEY_LEDS).toString(), getAbstractObjectId());
		return response;
	}

	@Override
	public JSONObject setColorPattern(JSONObject pattern) {
		logger.trace("setColorPattern(JSONObject pattern : {})", pattern);

		
		JSONArray array = pattern.getJSONArray(KEY_LEDS);
		int length = array.length();		
		
		for(int i = 0; i< length; i++) {
			JSONObject obj = array.getJSONObject(i);
			setOneColorLightPrivate(obj.getInt(KEY_ID), obj.getString(KEY_COLOR));
		}
		JSONObject response = getAllLights();

		stateChanged(KEY_LEDS, null, response.getJSONArray(KEY_LEDS).toString(), getAbstractObjectId());
		return response;

	}

	@Override
	public void singleChaserAnimation(int start, int end, String color) {
		logger.trace("singleChaserAnimation(int start : {}, int end : {}, String color : {})", start, end, color);
		
		JSONObject response = getAllLights();
		JSONArray cache = response.getJSONArray("leds");
		
		if(start < end) {
			for(int i = start; i<= end; i++) {
				if (i> start) {
					setOneColorLightPrivate(i-1, cache.getJSONObject(i-1).getString(KEY_COLOR));
				}
				setOneColorLightPrivate(i, color);
			}
			setOneColorLightPrivate(end, cache.getJSONObject(end).getString(KEY_COLOR));
		} else {
			for(int i = start; i>= end; i--) {
				if (i<start) {
					setOneColorLightPrivate(i+1, cache.getJSONObject(i+1).getString(KEY_COLOR));
				}
				setOneColorLightPrivate(i, color);
			}
			setOneColorLightPrivate(end, cache.getJSONObject(end).getString(KEY_COLOR));
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
		descr.put("type", getUserType()); // 12 for fairy lights
		descr.put("status", getObjectStatus());
		descr.put(KEY_LEDS, getAllLights().getJSONArray(KEY_LEDS));

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
