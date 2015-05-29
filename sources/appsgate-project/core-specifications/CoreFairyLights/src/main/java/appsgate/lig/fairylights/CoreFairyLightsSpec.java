package appsgate.lig.fairylights;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Core interface for the Fairy Lights (LumiPixel Device)
 * A CoreFairyLights service manage a Subset of all the lights in the device (referenced by their absolute id/index/number),
 * will change color if the specified light number of id/index/number is in the current group
 * @author thibaud
 */
public interface CoreFairyLightsSpec {
	
	/**
	 * 
	 * @return the list and states of the lights in the group as a JSON Array
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
	JSONArray getLightsStatus();
	
	/**
	 * Try to get the color of one light if it is in the group (otherwise will return null)
	 * @param lightNumber
	 * @return
	 * 
	 */
	String getOneLight(int lightNumber);

	/**
	 * Only append one single light if the number is in the group
	 * @param lightNumber
	 * @return
	 */
	JSONObject setOneColorLight(int lightNumber, String color);	
	
	/**
	 * Attempt to set all in the group lights of the same color
	 * @return
	 */
	JSONArray setAllColorLight(String color);
	
	/**
	 * Attempt to set a pattern (using a selected set of leds, with their colors) as a JSON array
    [
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
	JSONArray setColorPattern(JSONArray pattern);	
	

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
	
	/**
	 * same as the single chaser but the color remains (led does not returns to their original states)
	 * retruns the now status of the group
	 */
	JSONArray setColorAnimation(int start, int end, String color);
	
}
