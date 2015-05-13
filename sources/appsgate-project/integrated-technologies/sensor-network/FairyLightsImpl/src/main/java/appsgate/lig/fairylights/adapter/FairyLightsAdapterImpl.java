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
import appsgate.lig.fairylights.utils.HttpUtils;


/**
 * Deux fonctions pour l'adapter, Discovery (mDNS) de la guirlande (pour créer supprimer des groupes)
 * ce module s'occupe de la "réservation" des LEDs
 *
 * Implémeentation de l'API REST (bas niveau)
 * 
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
public class FairyLightsAdapterImpl implements  FairyLightsAdapterSpec {
	
	private static Logger logger = LoggerFactory.getLogger(FairyLightsAdapterImpl.class);


	
	
	
	
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
	
	public FairyLightsAdapterImpl() {
		logger.trace("FairyLightsImpl(), default constructor");
		if(HttpUtils.testURLTimeout(DEFAULT_PROTOCOL+DEFAULT_HOST+LUMIPIXEL_API_URL, 30*1000) ) {		
			try {

				Inet4Address address = (Inet4Address) Inet4Address.getByName(DEFAULT_HOST);
				host=DEFAULT_PROTOCOL + address.getHostAddress();
				coreObjectId = FairyLightsAdapterImpl.class.getSimpleName()+"-"+host;
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
	

	
}
