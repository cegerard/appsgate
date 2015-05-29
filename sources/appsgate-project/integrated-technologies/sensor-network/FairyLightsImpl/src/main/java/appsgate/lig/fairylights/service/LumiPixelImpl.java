package appsgate.lig.fairylights.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.fairylights.utils.HttpUtils;

/**
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
 * @author thibaud
 *
 */
public class LumiPixelImpl {
	
	private static Logger logger = LoggerFactory.getLogger(LumiPixelImpl.class);
	
	public static final String URL_SEP = "/";

	public static final String DEFAULT_PROTOCOL = "http://";

	public static final String DEFAULT_HOST = "guirlande.local";

	public static final String LUMIPIXEL_API_URL = "/lumipixel/api/v1.0/leds";
	
	public static final String KEY_COLOR = "color";
	public static final String KEY_LED = "led";
	public static final String KEY_LEDS = "leds";
	public static final String KEY_ID = "id";	
	
	/**
	 * 
	 * @return the whole list and states of the lights as a JSON Array
	  [
        {
            "color": "#c93b3b", 
            "id": 0, 
            "name": "premiere", 
            "uri": "/lumipixel/api/v1.0/leds/0"
        }, 
        {
            "color": "#2134e3", 
            "id": 1, 
            "name": "1", 
            "uri": "/lumipixel/api/v1.0/leds/1"
        }, 
        {
            "color": "#438a4f", 
            "id": 2, 
            "name": "2", 
            "uri": "/lumipixel/api/v1.0/leds/2"
        },
        ...
        {
            "color": "#438a4f", 
            "id": n, 
            "name": "n", 
            "uri": "/lumipixel/api/v1.0/leds/n"
        }
      ]
	 * 
	 */
	public static JSONArray getAllLights(String host) {
		logger.trace("getAllLights()");
		JSONObject response = new JSONObject(HttpUtils.sendHttpGet(host+LUMIPIXEL_API_URL)); 

		logger.trace("getAllLights(), returning {}",response.optJSONArray(KEY_LEDS));
		return response.optJSONArray(KEY_LEDS);
	}

	public static String getOneLight(String host, int lightNumber) {
		logger.trace("getOneLight(int lightNumber : {})", lightNumber);
		JSONObject response = new JSONObject(HttpUtils.sendHttpGet(host+LUMIPIXEL_API_URL+URL_SEP+lightNumber)); 
		if(response.optJSONObject(KEY_LED)!= null ) {
			logger.trace("getOneLight(...), returning {}",response);
			return response.getJSONObject(KEY_LED).optString(KEY_COLOR);
		} else {
			logger.warn("getOneLight(...), no led response");
			return null;
		}
	}

	
	public static JSONObject setOneColorLight(String host, int lightNumber, String color) {
		logger.trace("setOneColorLight(int lightNumber : {}, String color : {})", lightNumber, color);
		
		JSONObject jsonColor = new JSONObject().put(KEY_COLOR, color);

		JSONObject response = new JSONObject(
				HttpUtils.sendHttpsPut(host+LUMIPIXEL_API_URL+URL_SEP+lightNumber, jsonColor.toString().getBytes())); 
		return response;
	}
}
