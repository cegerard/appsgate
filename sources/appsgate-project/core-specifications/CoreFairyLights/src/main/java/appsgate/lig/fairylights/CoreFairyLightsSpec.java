package appsgate.lig.fairylights;

import org.json.JSONObject;


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
public interface CoreFairyLightsSpec {
	
	
	/**
	 * 
	 * @return the list and states of all lights as a JSONObject
	  {
    "leds": [
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
    }       
	 * 
	 */
	JSONObject getAllLights();
	
	/**
	 * 
	 * @param lightNumber
	 * @return
	 * 
	 */
	JSONObject getOneLight(int lightNumber);

	/**
	 * Only append one single light
	 * @param lightNumber
	 * @return
	 */
	JSONObject setOneColorLight(int lightNumber, String color);	
	
	/**
	 * Attempt to set all lights of the same color
	 * @return
	 */
	JSONObject setAllColorLight(String color);
	
	/**
	 * Attempt to set a patern (using a selected set of leds, with their colors) as a JSON Object
    "leds": [
        {
            "color": "#c93b3b", 
            "id": 3, 
        }, 
        {
            "color": "#2134e3", 
            "id": 9, 
        }, 
        {
            "color": "#438a4f", 
            "id": 22, 
        },
        ...
        {
            "color": "#438a4f", 
            "id": n, 
        }
    }        
	 * @return
	 */
	JSONObject setColorPattern(JSONObject pattern);	
	

	/**
	 * Small animation, beginning with starting led number to ending led number
	 * Between start and end each led change its color to the specified one, and the step after return to its original state
	 * @param start
	 * @param end
	 * @param color
	 */
	void singleChaserAnimation(int start, int end, String color);
	
	/**
	 * same as the single chaser but make several round trips
	 */
	void roundChaserAnimation(int start, int end, String color, int rounds);
	
}
