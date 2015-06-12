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
	 * Retrieve the latest/current Light number that was lighted (or turned off)
	 * @return the number (or index or id) of the current light, by default: 0
	 */
	int getCurrentLightNumber();
	
	/**
	 * Retrieve the latest/current color that was used (might be  "#000000" for défault value or if lights were turned off)
	 * @return the number (or index or id) of the current light
	 */
	String getCurrentColor();	
	

	/**
	 * Set the current light number but DOES NOT light it up
	 * (designed to be used along with method changeNextPreviousLights)
	 * @param lightNumber
	 */
	void setCurrentLightNumber(int lightNumber);	

	/**
	 * Set the current color, but does not light up any light number
	 * (designed to be used along with method changeNextPreviousLights) 
	 * @param lightNumber
	 */
	void setCurrentColor(String color);
	
	/**
	 * Starting from the 'current' light number illuminate (or turn off) nb lights contiguous with the currentColor
	 * Used for relative illumination of the fairy lights
	 * change the currentLightNumber accordingly (for example current+2 or current -1)
	 * @param nb
	 */
	void changeContiguousLights(int nb,String color);
	
	/**
	 * Only append one single light if the number is in the group
	 * also set the currentColor and the currentLightNumber
	 * @param lightNumber
	 * @return
	 */
	JSONObject setOneColorLight(int lightNumber, String color);	
	
	/**
	 * Attempt to set all in the group lights of the same color
	 * also set the currentColor and the currentLightNumber (the last light of the group)
	 * @return
	 */
	JSONArray setAllColorLight(String color);
	
	/**
	 * Attempt to set a pattern (using a selected set of leds, with their colors) as a JSON array
	 * also set the currentColor and the currentLightNumber (the last light index and color in the array)
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
	 * Apply a previously added color pattern upon its name
	 * @param patternName
	 * @return
	 */
	JSONArray setColorPattern(String patternName);	


	/**
	 * Add or update an existing pattern (patternName cannot be null)
	 */
	void addUpdateColorPattern(String patternName, JSONArray pattern);	
	void removeColorPattern(String patternName);	
	
	/**
	 * gets All the color patten currently saved
	 * @return
	 */
	JSONObject getColorPatterns();	


	/**
	 * Small animation, beginning with starting led number to ending led number
	 * Between start and end each led change its color to the specified one, and the step after return to its original state
	 * also set the currentColor and the currentLightNumber (the 'end' light number)
	 * @param start
	 * @param end
	 * @param color
	 * @param tail is the ending trail of the animation (how many light are coloured at the same time, befor animation ends)
	 */
	void singleChaserAnimation(int start, int end, String color, int tail);
	
	/**
	 * same as the single chaser but make several round trips
	 * also set the currentColor and the currentLightNumber (the 'start' light number) 
	 * @param tail is the ending trail of the animation (how many light are coloured at the same time, befor animation ends)
	 */
	void roundChaserAnimation(int start, int end, String color, int tail, int rounds);
	
	/**
	 * same as the single chaser but the color remains (led does not returns to their original states)
	 * also set the currentColor and the currentLightNumber (the 'end' light number)
	 * retruns the now status of the group
	 */
	JSONArray setColorAnimation(int start, int end, String color);
	
}
