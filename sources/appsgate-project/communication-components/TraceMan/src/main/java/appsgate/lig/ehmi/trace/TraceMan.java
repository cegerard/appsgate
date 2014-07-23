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
import appsgate.lig.ehmi.spec.GrammarDescription;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.ehmi.spec.trace.TraceManSpec;
import appsgate.lig.eude.interpreter.spec.ProgramNotification;
import appsgate.lig.eude.interpreter.spec.ProgramStateNotificationMsg;
import java.text.SimpleDateFormat;

/**
 * This component get CHMI from the EHMI proxy and got notifications for each
 * event in the EHMI layer to merge them into a JSON stream.
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
    private final HashMap<String, String> deviceTypeName = new HashMap<String, String>();
    /**
     * Map for device id to their user friendly name
     */
    private final HashMap<String, GrammarDescription> deviceGrammar = new HashMap<String, GrammarDescription>();

    /**
     * number of trace counter
     */
    private int cptTrace = 0;

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().getTime());
            this.traceFileWriter = new PrintWriter("traceMan-" + date + ".json");
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
    public synchronized void commandHasBeenPassed(String objectID, String command, String caller) {
        LOGGER.trace("A command has been passed {} on {} by {}", command, objectID, caller);
    }

    @Override
    public synchronized void coreEventNotify(long timeStamp, String srcId, String varName, String value) {
        try {
            if (deviceTypeName.get(srcId) != null) { //if the equipment has been instantiated from ApAM spec before

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
                                event.put("causality", getCausalityJSON(srcId, varName));

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
            }

        } catch (JSONException jsonEx) {
            LOGGER.error("JSONException thrown: " + jsonEx.getMessage());
        }
    }

    @Override
    public synchronized void coreUpdateNotify(long timeStamp, String srcId, String coreType,
            String userType, String name, JSONObject description, String eventType) {
        try {

            //Put the user friendly core type in the map
            GrammarDescription grammar = EHMIProxy.getGrammarFromType(userType);

            addUserType(srcId, userType);
            deviceGrammar.put(srcId, grammar);

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
                        objectNotif.put("name", name);
                        objectNotif.put("type", deviceTypeName.get(srcId));

                        //Create the event description device entry
                        JSONObject event = new JSONObject();
                        {
                            if (eventType.contentEquals("new ")) {
                                event.put("type", "appear");
                            } else if (eventType.contentEquals("remove")) {
                                event.put("type", "disappear");
                            }

                            //Create causality event entry
                            if (eventType.contains("new")) {
                                event.put("causality", getCausalityJSON("technical", "equipement appear"));
                                addDeviceState(objectNotif, srcId, "", "");

                            } else if (eventType.contentEquals("remove")) {
                                event.put("causality", getCausalityJSON("technical", "equipement disappear"));

                            }
                            objectNotif.put("event", event);
                        }

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

    /**
     *
     * @param n
     */
    public synchronized void gotNotification(NotificationMsg n) {
        try {
            if (!(n instanceof ProgramNotification)) {
                return;
            }
            ProgramNotification notif = (ProgramNotification) n;
            //Create the notification JSON object
            JSONObject pgmNotif = new JSONObject();
            pgmNotif.put("timestamp", EHMIProxy.getCurrentTimeInMillis());

            //Create the device tab JSON entry
            JSONArray deviceTab = new JSONArray();
            //Nothing here cause it is program notification
            pgmNotif.put("devices", deviceTab);

            //Create the device tab JSON entry
            JSONArray pgmTab = new JSONArray();
            {

                //Create a device trace entry
                JSONObject progNotif = new JSONObject();
                {
                    progNotif.put("id", notif.getProgramId());
                    progNotif.put("name", notif.getProgramName());

                    //Create the event description device entry
                    JSONObject event = new JSONObject();
                    {
                        String change = notif.getVarName();
                        if (change.contentEquals("newProgram")) {
                            event.put("type", "appear");
                            event.put("causality", getCausalityJSON("user", "Program has been added"));
                        } else if (change.contentEquals("removeProgram")) {
                            event.put("type", "disappaear");
                            event.put("causality", getCausalityJSON("user", "Program has been deleted"));
                        } else { //change == "updateProgram"
                            event.put("type", "update");
                        }
                        //Create causality event entry
                        if (change.contentEquals("updateProgram")) {
                            event.put("causality", getCausalityJSON("user", "Program has been updated"));
                        }

                        progNotif.put("event", event);
                    }

                    progNotif.put("state", notif.getRunningState());
                    pgmTab.put(progNotif);
                }

                pgmNotif.put("programs", pgmTab);

                //Trace the notification JSON object in the trace file
                trace(pgmNotif);
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
    private synchronized void trace(JSONObject traceObj) {

        if (cptTrace > 0) { //For all trace after the first 
            traceFileWriter.println(",");
            traceFileWriter.print(traceObj.toString());
        } else { //For the first trace
            traceFileWriter.print(traceObj.toString());
        }
        this.traceFileWriter.flush();

        cptTrace++;
    }

    private void addDeviceState(JSONObject objectNotif, String srcId, String varName, String value) {
        try {
            JSONObject deviceState = new JSONObject();

            String deviceFriendlyType = deviceTypeName.get(srcId);

            if (deviceFriendlyType.contentEquals("Temperature")) {
                if (varName.contentEquals("currentTemperature")) {
                    deviceState.put("value", value);
                    deviceState.put("status", "2");
                } else if (varName.contentEquals("status")) {
                    deviceState.put("status", value);
                    deviceState.put("value", EHMIProxy.getDevice(srcId).getString("value"));
                } else {
                    deviceState.put("status", "2");
                    deviceState.put("value", EHMIProxy.getDevice(srcId).getString("value"));
                }

            } else if (deviceFriendlyType.contentEquals("Illumination")
                    || deviceFriendlyType.contentEquals("Co2")) {
                if (varName.contains("current")) {
                    deviceState.put("value", value);
                    deviceState.put("status", "2");
                } else if (varName.contentEquals("status")) {
                    deviceState.put("status", value);
                    deviceState.put("value", EHMIProxy.getDevice(srcId).getString("value"));
                } else {
                    deviceState.put("status", "2");
                    deviceState.put("value", EHMIProxy.getDevice(srcId).getString("value"));
                }

            } else if (deviceFriendlyType.contentEquals("Switch")) {
                if (varName.contains("switchNumber")) {
                    deviceState.put("switchNumber", value);
                    deviceState.put("buttonStatus", EHMIProxy.getDevice(srcId).getInt("buttonStatus"));
                    deviceState.put("status", "2");
                } else if (varName.contentEquals("buttonStatus")) {
                    deviceState.put("status", "2");
                    deviceState.put("switchNumber", EHMIProxy.getDevice(srcId).getString("switchNumber"));
                    deviceState.put("buttonStatus", value);
                } else if (varName.contentEquals("status")) {
                    deviceState.put("status", value);
                    JSONObject descr = EHMIProxy.getDevice(srcId);
                    deviceState.put("switchNumber", descr.getString("switchNumber"));
                    deviceState.put("buttonStatus", descr.getInt("buttonStatus"));
                } else {
                    JSONObject descr = EHMIProxy.getDevice(srcId);
                    deviceState.put("status", descr.getString("status"));
                    deviceState.put("switchNumber", descr.getString("switchNumber"));
                    deviceState.put("buttonStatus", descr.getInt("buttonStatus"));
                }

            } else if (deviceFriendlyType.contentEquals("Contact")
                    || deviceFriendlyType.contentEquals("KeyCardSwitch")
                    || deviceFriendlyType.contentEquals("Occupancy")
                    || deviceFriendlyType.contentEquals("OnOffActuator")) {

                if (varName.contains("current") || varName.contains("occupied")
                        || varName.contains("isOn")) {

                    deviceState.put("status", "2");
                    deviceState.put("value", value);

                } else if (varName.contentEquals("status")) {

                    deviceState.put("status", value);
                    if (deviceFriendlyType.contentEquals("Contact")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("contact"));
                    } else if (deviceFriendlyType.contentEquals("KeyCardSwitch")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("inserted"));
                    } else if (deviceFriendlyType.contentEquals("Occupancy")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("occupied"));
                    } else if (deviceFriendlyType.contentEquals("OnOffActuator")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("isOn"));
                    }
                } else {
                    deviceState.put("status", "2");
                    if (deviceFriendlyType.contentEquals("Contact")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("contact"));
                    } else if (deviceFriendlyType.contentEquals("KeyCardSwitch")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("inserted"));
                    } else if (deviceFriendlyType.contentEquals("Occupancy")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("occupied"));
                    } else if (deviceFriendlyType.contentEquals("OnOffActuator")) {
                        deviceState.put("value", EHMIProxy.getDevice(srcId).getString("isOn"));
                    }
                }

            } else if (deviceFriendlyType.contentEquals("SmartPlug")) {
                if (varName.contains("plugState")) {
                    deviceState.put("plugState", value);
                    deviceState.put("consumption", EHMIProxy.getDevice(srcId).getString("consumption"));
                    deviceState.put("status", "2");
                } else if (varName.contentEquals("consumption")) {
                    deviceState.put("status", "2");
                    deviceState.put("plugState", EHMIProxy.getDevice(srcId).getString("plugState"));
                    deviceState.put("consumption", value);
                } else if (varName.contentEquals("status")) {
                    deviceState.put("status", value);
                    JSONObject descr = EHMIProxy.getDevice(srcId);
                    deviceState.put("plugState", descr.getString("plugState"));
                    deviceState.put("consumption", descr.getString("consumption"));
                } else {
                    JSONObject descr = EHMIProxy.getDevice(srcId);
                    deviceState.put("status", descr.getString("status"));
                    deviceState.put("plugState", descr.getString("plugState"));
                    deviceState.put("consumption", descr.getString("consumption"));
                }
            } else if (deviceFriendlyType.contentEquals("Colorlight")) {
                JSONObject descr = EHMIProxy.getDevice(srcId);
                if (varName.contains("state")) {
                    deviceState.put("state", value);
                    deviceState.put("color", descr.getInt("color"));
                    deviceState.put("saturation", descr.getInt("saturation"));
                    deviceState.put("brightness", descr.getInt("brightness"));
                    deviceState.put("status", descr.getString("status"));

                } else if (varName.contentEquals("hue")) {
                    deviceState.put("state", descr.getBoolean("value"));
                    deviceState.put("color", value);
                    deviceState.put("saturation", descr.getInt("saturation"));
                    deviceState.put("brightness", descr.getInt("brightness"));
                    deviceState.put("status", descr.getString("status"));

                } else if (varName.contentEquals("sat")) {
                    deviceState.put("state", descr.getBoolean("value"));
                    deviceState.put("color", descr.getInt("color"));
                    deviceState.put("saturation", value);
                    deviceState.put("brightness", descr.getInt("brightness"));
                    deviceState.put("status", descr.getString("status"));

                } else if (varName.contentEquals("bri")) {
                    deviceState.put("state", descr.getBoolean("value"));
                    deviceState.put("color", descr.getInt("color"));
                    deviceState.put("saturation", descr.getInt("saturation"));
                    deviceState.put("brightness", value);
                    deviceState.put("status", descr.getString("status"));

                } else if (varName.contentEquals("reachable")) {
                    deviceState.put("state", descr.getBoolean("value"));
                    deviceState.put("color", descr.getInt("color"));
                    deviceState.put("saturation", descr.getInt("saturation"));
                    deviceState.put("brightness", descr.getInt("brightness"));
                    if (value.contentEquals("true")) {
                        deviceState.put("status", "2");
                    } else {
                        deviceState.put("status", "0");
                    }
                } else {
                    deviceState.put("state", descr.getBoolean("value"));
                    deviceState.put("color", descr.getInt("color"));
                    deviceState.put("saturation", descr.getInt("saturation"));
                    deviceState.put("brightness", descr.getInt("brightness"));
                    deviceState.put("status", descr.getString("status"));
                }
            } else if (deviceFriendlyType.contentEquals("SystemClock")) {
                //TODO Add state profile in next version

            } else if (deviceFriendlyType.contentEquals("MediaPlayer")) {
                //TODO Add state profile in next version

            } else if (deviceFriendlyType.contentEquals("MediaBrowser")) {
                //TODO Add state profile in next version

            } else if (deviceFriendlyType.contentEquals("GoogleCalendar")) {
                //TODO Add state profile in next version

            } else if (deviceFriendlyType.contentEquals("Mail")) {
                //TODO Add state profile in next version

            } else if (deviceFriendlyType.contentEquals("Weather")) {
                //TODO Add state profile in next version

            } else if (deviceFriendlyType.contentEquals("MobileDevice")) {
                //TODO Add state profile in next version

            } else if (deviceFriendlyType.contentEquals("DomiCube")) {

                JSONObject descr = EHMIProxy.getDevice(srcId);
                if (varName.contains("newFace")) {
                    deviceState.put("activeFace", value);
                    deviceState.put("batteryLevel", descr.getString("batteryLevel"));
                    deviceState.put("dimValue", descr.getString("dimValue"));
                    deviceState.put("status", descr.getString("status"));

                } else if (varName.contentEquals("newBatteryLevel")) {
                    deviceState.put("activeFace", descr.getString("activeFace"));
                    deviceState.put("batteryLevel", value);
                    deviceState.put("dimValue", descr.getString("dimValue"));
                    deviceState.put("status", descr.getString("status"));

                } else if (varName.contentEquals("newDimValue")) {
                    deviceState.put("activeFace", descr.getString("activeFace"));
                    deviceState.put("batteryLevel", descr.getString("batteryLevel"));
                    deviceState.put("dimValue", value);
                    deviceState.put("status", descr.getString("status"));

                } else if (varName.contentEquals("status")) {
                    deviceState.put("activeFace", descr.getString("activeFace"));
                    deviceState.put("batteryLevel", descr.getString("batteryLevel"));
                    deviceState.put("dimValue", descr.getString("dimValue"));
                    deviceState.put("status", value);
                } else {
                    deviceState.put("activeFace", descr.getString("activeFace"));
                    deviceState.put("batteryLevel", descr.getString("batteryLevel"));
                    deviceState.put("dimValue", descr.getString("dimValue"));
                    deviceState.put("status", descr.getString("status"));
                }

            } else {
                if (varName.contentEquals("status")) {
                    deviceState.put("status", value);
                }
            }

            objectNotif.put("state", deviceState);

        } catch (JSONException ex) {
            LOGGER.error("JSONException thrown: " + ex.getMessage());
        }
    }

    private void addUserType(String srcId, String userType) {

        String typeFriendlyName;
        int i_userType = Integer.valueOf(userType);

        switch (i_userType) {
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

    /**
     * Method to format a causality JSON object.
     *
     * @param type
     * @param description
     * @return the json object
     */
    private JSONObject getCausalityJSON(String type, String description) {
        JSONObject causality = new JSONObject();
        try {
            causality.put("type", type);
            causality.put("description", description);

        } catch (JSONException ex) {
            // Never happens
        }
        return causality;

    }

}
