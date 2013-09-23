/**
 * Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * WeatherUpdateNotificationMsg.java - 25 juil. 2013
 */
package appsgate.lig.weather.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * ApAM message for Weather Update Notification
 * 
 * @author Thibaud Flury version 1.0.0
 * @since 25 juil. 2013
 */
public class WeatherUpdateNotificationMsg implements NotificationMsg {

    public static final String EVENTTYPE_SETUNIT = "unitSetted";
    public static final String EVENTTYPE_FETCHLOCATION = "weatherUpdated";
    public static final String EVENTTYPE_ADDLOCATION = "locationAdded";
    public static final String EVENTTYPE_REMOVELOCATION = "locationAdded";

    /**
     * The source sensor of this notification
     */
    private CoreObjectSpec source;

    /**
     * The name of the change variable
     */
    private String varName;

    /**
     * The value corresponding to the varName variable
     */
    private String value;

    private String eventType;

    /**
     * Constructor of Weather Update ApAM message
     * 
     * @param source
     *            the abstract object source of this message
     * @param varName
     *            the variable that changed
     * @param value
     *            the new variable value
     */
    public WeatherUpdateNotificationMsg(CoreObjectSpec source, String varName,
	    String value, String eventType) {
	this.source = source;
	this.varName = varName;
	this.value = value;
	this.eventType = eventType;
    }

    @Override
    public CoreObjectSpec getSource() {
	return source;
    }

    @Override
    public String getNewValue() {
	return value;
    }

    @Override
    public JSONObject JSONize() throws JSONException {
	JSONObject notif = new JSONObject();
	notif.put("objectId", source.getAbstractObjectId());
	notif.put("varName", varName);
	notif.put("value", value);
	if (eventType != null && eventType.length() > 0){
	    notif.put("eventType", eventType);
	}
	
	return notif;
    }

}
