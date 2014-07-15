package appsgate.lig.ehmi.trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.ehmi.spec.trace.TraceManSpec;
import appsgate.lig.eude.interpreter.spec.ProgramNotification;
import appsgate.lig.eude.interpreter.spec.ProgramStateNotificationMsg;


/**
 * This component get CHMI from the EHMI proxy and got notifications for each event in the
 *  EHMI layer to merge them into a JSON stream.
 * 
 * @author Cedric Gerard
 * @since July 13, 2014
 * @version 1.0.0
 */
public class TraceMan implements TraceManSpec {
	
    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceMan.class);
    
    /**
     * Dependence to the main EHMI component
     */
    private EHMIProxySpec EHMIProxy;
    
    /**
     * Dependence to the device property table
     */
    private DevicePropertiesTableSpec devicePropTable;
    
    /**
     * The printWriter for the trace file on the hard drive
     */
    private PrintWriter traceFileWriter;
    
    /**
     * Map for device id to their user friendly name
     */
    private HashMap<String, String> deviceTypeName = new HashMap<String, String>();

    /**
     * number of trace counter
     */
	private int cptTrace = 0;
    
    /**
     * Called by APAM when an instance of this implementation is created
     */
	public void newInst() {
		try {
			Calendar date = Calendar.getInstance();
			this.traceFileWriter = new PrintWriter("traceMan-"+ date.getTimeInMillis() + ".json");
			traceFileWriter.println("[");
		} catch (FileNotFoundException ex) {
			this.traceFileWriter = null;
			LOGGER.error("Unable to open trace file");
		}
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		if (this.traceFileWriter != null) {
			this.traceFileWriter.println();
			this.traceFileWriter.print("]");
			this.traceFileWriter.flush();
			this.traceFileWriter.close();
		}
	}
    
	@Override
    public synchronized void coreEventNotify(long timeStamp, String srcId, String varName, String value){
    	try {
    		//Create the notification JSON object
    		JSONObject coreNotif = new JSONObject();
    		{
    			coreNotif.put("timestamp", timeStamp);
    			
    			//Create the device tab JSON entry
    			JSONArray deviceTab = new JSONArray();
    			{
    				//Create a device trace entry
    				JSONObject objectNotif = new JSONObject();
    				{
    					objectNotif.put("id", srcId);
    					objectNotif.put("name", devicePropTable.getName(srcId, ""));
    					objectNotif.put("type", deviceTypeName.get(srcId));
        			
    					//Create the event description device entry
    					JSONObject event = new JSONObject();
    					{
    						event.put("type", "update");
        				
    						//Create causality event entry
    						JSONObject causality = new JSONObject();
    						{
    							causality.put("type", "technical");
    							causality.put("description", "something happened");
    							event.put("causality", causality);
    						}
    						
    						objectNotif.put("event", event);
    					}
    					
    					addDeviceState(objectNotif, srcId, varName, value);
    					deviceTab.put(objectNotif);
    				}
    				coreNotif.put("devices", deviceTab);
    			}
    				
    			//Create the device tab JSON entry
        		JSONArray pgmTab = new JSONArray();
        		{
        			//Nothing here cause it is device notification
        			coreNotif.put("programs", pgmTab);
        		}
    		}
  
    		//Trace the notification JSON object in the trace file
    		trace(coreNotif);
    		
    	} catch (JSONException jsonEx) {
			LOGGER.error("JSONException thrown: " + jsonEx.getMessage());
		}
    }

	@Override
	public synchronized void coreUpdateNotify(long timeStamp, String srcId, String coreType,
			String userType, String name, JSONObject description, String eventType) {
		try {
			
			//Put the user friendly core type in the map
			addUserType(srcId, userType);
			
			JSONObject coreNotif = new JSONObject();
			coreNotif.put("timestamp", timeStamp);
			coreNotif.put("id", srcId);
			coreNotif.put("name", name);
			coreNotif.put("availibility", eventType);
			
			// TODO complete the notification JSON entry
			
			trace(coreNotif);

		} catch (JSONException jsonEx) {
			LOGGER.error("JSONException thrown: " + jsonEx.getMessage());
		}
	}

	/**
    *
    * @param notif
    */
	public synchronized void gotNotification(NotificationMsg notif) {
		try {
			// Add trace for programs notification only
			if (notif instanceof ProgramNotification) {
				JSONObject pgmNotif = new JSONObject();
				pgmNotif.put("timestamp", EHMIProxy.getCurrentTimeInMillis());
				pgmNotif.put("id", notif.getSource());
				pgmNotif.put("name", "test");
				pgmNotif.put("state", notif.JSONize().get("runningState"));

				// TODO complete the notification JSON entry
				
				trace(pgmNotif);

			} else if (notif instanceof ProgramStateNotificationMsg) {
				JSONObject pgmStateNotif = new JSONObject();
				pgmStateNotif.put("timestamp", EHMIProxy.getCurrentTimeInMillis());
				pgmStateNotif.put("id", notif.getSource());
				pgmStateNotif.put("name", "test");
				pgmStateNotif.put("state", notif.getNewValue());

				// TODO complete the notification JSON entry

				trace(pgmStateNotif);
				
			} else {
				LOGGER.debug("EHMI Notification message not traced.");
			}

		} catch (JSONException jsonEx) {
			LOGGER.error("JSONException thrown: " + jsonEx.getMessage());
		}
	}
	
	/**
	 * Trace the JSON object into the opened trace file
	 * 
	 * @param traceObj the trace to add into the trace file
	 */
	private void trace (JSONObject traceObj) {
		
		if(cptTrace > 0){ //For all trace after the first 
			traceFileWriter.println(",");
			traceFileWriter.print(traceObj.toString());
		}else { //For the first trace
			traceFileWriter.print(traceObj.toString());
		}
		cptTrace++;
	}

	private void addDeviceState(JSONObject objectNotif, String srcId, String varName, String value) {
		try{
			JSONObject deviceState = new JSONObject();
		
			String deviceFriendlyType = deviceTypeName.get(srcId);
			
			if (deviceFriendlyType.contentEquals("Temperature")) {
				if(varName.contentEquals("currentTemperature")) {
					deviceState.put("value", value);
					deviceState.put("status", "2");
				}else if(varName.contentEquals("status")) {
					deviceState.put("status", value);
					deviceState.put("value", ""); //TODO get last sensor value
				}
				
			} else if (deviceFriendlyType.contentEquals("Illumination") ||
					deviceFriendlyType.contentEquals("Co2")) {
				if(varName.contains("current")) {
					deviceState.put("value", value);
					deviceState.put("status", "2");
				}else if(varName.contentEquals("status")) {
					deviceState.put("status", value);
					deviceState.put("value", ""); //TODO get last sensor value
				}
				
			} else if (deviceFriendlyType.contentEquals("Switch")) {
				if(varName.contains("switchNumber")) {
					deviceState.put("switchNumber", value);
					deviceState.put("buttonStatus", ""); //TODO get last sensor value
					deviceState.put("status", "2");
				} else if(varName.contentEquals("buttonStatus")) {
					deviceState.put("status", "2");
					deviceState.put("switchNumber", "");//TODO get last sensor value
					deviceState.put("buttonStatus", value); 
				} else if(varName.contentEquals("status")) {
					deviceState.put("status", value);
					deviceState.put("switchNumber", ""); //TODO get last sensor value
					deviceState.put("buttonStatus", ""); //TODO get last sensor value
				}
				
			} else if (deviceFriendlyType.contentEquals("Contact")    ||
					deviceFriendlyType.contentEquals("KeyCardSwitch") ||
					deviceFriendlyType.contentEquals("Occupancy")     ||
					deviceFriendlyType.contentEquals("OnOffActuator")) {
				
				if(varName.contains("current") || varName.contains("occupied")
												|| varName.contains("isOn")) {
					deviceState.put("status", "2");
					deviceState.put("value", value);
				} else if(varName.contentEquals("status")) {
					deviceState.put("status", value);
					deviceState.put("value", ""); //TODO get last sensor value
				}
				
				
			} else if (deviceFriendlyType.contentEquals("SmartPlug")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("Colorlight")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("SystemClock")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("MediaPlayer")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("MediaBrowser")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("GoogleCalendar")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("Mail")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("Weather")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("MobileDevice")) {
				//TODO Add state profile
			} else if (deviceFriendlyType.contentEquals("DomiCube")) {
				//TODO Add state profile
			} else {
				if(varName.contentEquals("status")) {
					deviceState.put("status", value);
				}
			}
			
			objectNotif.put("state", deviceState);
			
		}catch(JSONException ex){
			LOGGER.error("JSONException thrown: " + ex.getMessage());
		}
	}
	
    private void addUserType(String srcId, String userType) {
    	
    	String typeFriendlyName;
    	int i_userType = Integer.valueOf(userType);
    
    	switch(i_userType){
    		case 0:
    			typeFriendlyName = "Temperature";
				break;
    		case 1:
    			typeFriendlyName = "Illumination";
				break;
    		case 2:
    			typeFriendlyName = "Switch";
				break;
    		case 3:
    			typeFriendlyName = "Contact";
				break;
    		case 4:
    			typeFriendlyName = "KeyCardSwitch";
				break;
    		case 5:
    			typeFriendlyName = "Occupancy";
				break;
    		case 6:
    			typeFriendlyName = "SmartPlug";
				break;
    		case 7:
    			typeFriendlyName = "Colorlight";
				break;
    		case 8:
    			typeFriendlyName = "OnOffActuator";
				break;
    		case 9:
    			typeFriendlyName = "Co2";
				break;
    		case 21:
    			typeFriendlyName = "SystemClock";
				break;
    		case 31:
    			typeFriendlyName = "MediaPlayer";
				break;
    		case 36:
    			typeFriendlyName = "MediaBrowser";
				break;
    		case 101:
    			typeFriendlyName = "GoogleCalendar";
				break;
    		case 102:
    			typeFriendlyName = "Mail";
				break;
    		case 103:
    			typeFriendlyName = "Weather";
				break;
    		case 200:
    			typeFriendlyName = "MobileDevice";
				break;
    		case 210:
    			typeFriendlyName = "DomiCube";
				break;
    		case 794225618:
    			typeFriendlyName = "ContentDirectory";
				break;
//    		case -532540516:
//    			typeFriendlyName = "Unknown";
//				break;
    		case 2052964255:
    			typeFriendlyName = "ConnectionManager";
    			break;
    		case 415992004:
    			typeFriendlyName = "AvTransport";
				break;
    		case -164696113:
    			typeFriendlyName = "RenderingControl";
				break;
//    		case -1943939940:
//    			typeFriendlyName = "Unknown";
//				break;
			default:
				typeFriendlyName = "Undefined";
				break;
    	}
    	
    	deviceTypeName.put(srcId, typeFriendlyName);
	}
}
