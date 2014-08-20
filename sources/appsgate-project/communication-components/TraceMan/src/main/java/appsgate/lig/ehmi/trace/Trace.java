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
    		} else if (type.equalsIgnoreCase("deconnection")){
    			pictoID = PICTO_TABLE.DECONNECTION.stringify();
    		}
    	}
    	
    	return pictoID;
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
    	DECONNECTION;
    	
    	/** Get the lower case string of enumerate value*/
    	public String stringify() {
    		return this.toString().toLowerCase();
    	}
    }
}
