/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.ehmi.trace;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class
 * 
 * @author jr
 */
public class Trace {
    
    /**
     * Method to format a causality JSON object.
     *
     * @param type
     * @param cause
     * @param source
     * @param target
     * @param description
     * @return the JSON object
     */
    public static JSONObject getJSONDecoration(String type, String cause, String source, String target, String description) {
        JSONObject causality = new JSONObject();
        try {
        	causality.put("order", 0);
            causality.put("type", getPictoFromType(type, cause));
            causality.put("causality", cause);
            causality.put("source", source);
            causality.put("target", target);
            causality.put("description", description);

        } catch (JSONException ex) {
            // Never happens
        }
        return causality;

    }
    
    /**
     * Return the pictoID that match the decoration type
     * @param type the decoration type
     * @param cause the decoration cause
     * @return the corresponding pictoID as a String
     */
    public static String getPictoFromType(String type, String cause) {
    	
    	String pictoID = PICTO_TABLE.DEFAULT.stringify();

    	if(cause.equalsIgnoreCase("user")){
    		pictoID = PICTO_TABLE.USER.stringify();
    	}else {
    		if(type.equalsIgnoreCase("read")){
    			pictoID = PICTO_TABLE.READ.stringify();
    		} else if (type.equalsIgnoreCase("write")){
    			pictoID = PICTO_TABLE.WRITE.stringify();
    		} else if (type.equalsIgnoreCase("connection")){
    			pictoID = PICTO_TABLE.CONNECTION.stringify();
    		} else if (type.equalsIgnoreCase("disconnection")){
    			pictoID = PICTO_TABLE.DISCONNECTION.stringify();
    		}
    	}
    	
    	return pictoID;
    }
    
    /**
     * Return the pictoID for a device state
     * @param type the device type
     * @param varName the variable that changed
     * @param value the new value of this variable
     * @param fullState the current state of the device as a JSONObject
     * @return the pictoID as a String
     * @throws JSONException 
     */
    public static String getPictoState(String type, String varName, String value, JSONObject fullState) throws JSONException {
    	
		String picto = PICTO_TABLE.DEFAULT.stringify();
		
    	if(type.equalsIgnoreCase("Temperature")){
    		picto = PICTO_TABLE.TEMPERATURE_TYPE.stringify();
    		
    	} else if (type.equalsIgnoreCase("Illumination")){
    		picto = PICTO_TABLE.ILLUMINATION_TYPE.stringify();
    	
    	} else if (type.equalsIgnoreCase("Switch")){
    		if(varName.equalsIgnoreCase("switchNumber")){
    			picto = PICTO_TABLE.SINGLE_SWITCH_STATE_.stringify()+value;
    		} else {
    			picto = PICTO_TABLE.SWITCH_TYPE.stringify();
    		}
    		
    	} else if (type.equalsIgnoreCase("Contact")){
    		if(varName.equalsIgnoreCase("contact")){
    			if(value.equalsIgnoreCase("true")){
    				picto = PICTO_TABLE.CONTACT_STATE_ON.stringify();
    			} else {
    				picto = PICTO_TABLE.CONTACT_STATE_OFF.stringify();
    			}
    		} else {
    			picto = PICTO_TABLE.CONTACT_TYPE.stringify();
    		}
    		
    	} else if (type.equalsIgnoreCase("KeyCardSwitch")){
    		if(varName.equalsIgnoreCase("inserted")){
    			if(value.equalsIgnoreCase("true")){
    				picto = PICTO_TABLE.KEYCARDSWITCH_STATE_IN.stringify();
    			} else {
    				picto = PICTO_TABLE.KEYCARDSWITCH_STATE_OUT.stringify();
    			}
    		} else {
    			picto = PICTO_TABLE.KEYCARDSWITCH_TYPE.stringify();
    		}
    		
    	} else if (type.equalsIgnoreCase("ColorLight")){
    		if(varName.equalsIgnoreCase("state") ||
    			varName.equalsIgnoreCase("color") ||
  				varName.equalsIgnoreCase("bri") ||
  				varName.equalsIgnoreCase("sat") ||
  				varName.equalsIgnoreCase("hue") ||
  				varName.equalsIgnoreCase("value")){
    			
    			if(fullState.getBoolean("state")){
    				picto = PICTO_TABLE.COLORLIGHT_STATE_ON.stringify();
    			}else{
    				picto = PICTO_TABLE.COLORLIGHT_STATE_OFF.stringify();
    			}
    		}  else {
    			picto = PICTO_TABLE.COLORLIGHT_TYPE.stringify();
    		}
    		
    	} else if (type.equalsIgnoreCase("SmartPlug")){
    		if(fullState.getString("plugState").equalsIgnoreCase("true")){
    			picto = PICTO_TABLE.SMARTPLUG_STATE_ON.stringify();
    		}else{
    			picto = PICTO_TABLE.SMARTPLUG_STATE_OFF.stringify();
    		}
    		
    	} else if (type.equalsIgnoreCase("DomiCube")){
    		if(varName.equalsIgnoreCase("activeFace")){
    			picto = PICTO_TABLE.DOMICUBE_STATE_.stringify()+value;
    		} else {
    			picto = PICTO_TABLE.DOMICUBE_TYPE.stringify();
    		}
    		
    	} else if (type.equalsIgnoreCase("MediaPlayer")){
    		picto = PICTO_TABLE.MEDIAPLAYER_TYPE.stringify();
    	}
    	
		return picto;
	}
    
    /**
     * Icon type name table
     * @author Cedric Gerard
     * @version spec_v4
     */
    private enum PICTO_TABLE {
    	DEFAULT,
    	READ,
    	WRITE,
    	//TODO add write maintain 
    	USER,
    	CONNECTION,
    	DISCONNECTION,
    	
    	//Device state icon identifier
    	SWITCH_STATE_,
    	SWITCH_STATE_1,
    	SWITCH_STATE_3,
    	SWITCH_STATE_5,
    	SWITCH_STATE_7,
    	SINGLE_SWITCH_STATE_,
    	SINGLE_SWITCH_STATE_5,
    	SINGLE_SWITCH_STATE_7,
    	CONTACT_STATE_ON,
    	CONTACT_STATE_OFF,
    	KEYCARDSWITCH_STATE_IN,
    	KEYCARDSWITCH_STATE_OUT,
    	OCCUPANCY_STATE_ON,
    	OCCUPANCY_STATE_OFF,
    	SMARTPLUG_STATE_ON,
    	SMARTPLUG_STATE_OFF,
    	COLORLIGHT_STATE_ON,
    	COLORLIGHT_STATE_OFF,
    	DOMICUBE_STATE_,
    	DOMICUBE_STATE_1,
    	DOMICUBE_STATE_2,
    	DOMICUBE_STATE_3,
    	DOMICUBE_STATE_4,
    	DOMICUBE_STATE_5,
    	DOMICUBE_STATE_6,
    	
    	//Fall back icon identifier
    	TEMPERATURE_TYPE,
    	ILLUMINATION_TYPE,
    	CO2_TYPE,
    	SWITCH_TYPE,
    	SINGLE_SWITCH_TYPE,
    	CONTACT_TYPE,
    	KEYCARDSWITCH_TYPE,
    	OCCUPANCY_TYPE,
    	SMARTPLUG_TYPE,
    	COLORLIGHT_TYPE,
    	DOMICUBE_TYPE,
    	MEDIAPLAYER_TYPE
    	;
    	
    	/** Get the lower case string of enumerate value*/
    	public String stringify() {
    		return this.toString().toLowerCase();
    	}
    }

	public static String getConnectionPicto() {
		return PICTO_TABLE.CONNECTION.stringify();
	}

	public static String getDisconnectionPicto() {
		return PICTO_TABLE.DISCONNECTION.stringify();
	}
}
