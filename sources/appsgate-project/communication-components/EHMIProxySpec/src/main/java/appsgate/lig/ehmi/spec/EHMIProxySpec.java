package appsgate.lig.ehmi.spec;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.ehmi.spec.listeners.CoreListener;

/**
 * EHMI specification that define all method that a client can remote call to
 * interact with the AppsGate system.
 *
 * @author Cedric Gérard
 * @since June 19, 2013
 * @version 1.0.0
 *
 */
public interface EHMIProxySpec {

    /**
     * ************************
     */
    /**
     * Device management
     */
    /**
     * ************************
     */
    /**
     * Get all the devices description
     *
     * @return
     */
    public JSONArray getDevices();

    /**
     * Get device details
     *
     * @param deviceId the targeted device identifier
     * @return the device description as a JSONObject
     */
    public JSONObject getDevice(String deviceId);

    /**
     * Get all the devices of a specify user type
     *
     * @param type the type to filter devices
     * @return the device list as a JSONArray
     */
    public JSONArray getDevices(String type);

    /**
     * Get the state representation for a device type in the grammar
     *
     * @param objectId the device from which to get the state representation
     * @param stateName the specific state to extract
     * @return the state description
     */
    public StateDescription getEventsFromState(String objectId, String stateName);

    /**
     * ************************
     */
    /**
     * Device properties management
     */
    /**
     * ************************
     */
 
    /**
     * Get the grammar associated to a device type
     *
     * @param deviceType the device type from which to get the grammar
     * @return the grammar as a JSONObject
     */
    public GrammarDescription getGrammarFromType(String deviceType);

    /**
     * Get the grammar associated to a device
     *
     * @param deviceId the device id from which to get the grammar
     * @return the grammar as a JSONObject
     */
    public GrammarDescription getGrammarFromDevice(String deviceId);

    /**
     * ************************
     */
    /**
     * Place management
     */
    /**
     * ************************
     */

    
    public JSONArray getPlaces();
    
	/**
	 * Get the place identifier of a core object
	 * @param objId the core object identifier 
	 * @return the identifier of the place where the core object is placed.
	 */
	public String getCoreObjectPlaceId(String objId);    
    
    /**
     * Remove a property from a specified place
     *
     * @param placeId the place from where to remove the property
     * @param key the key of the property that have to be removed
     * @return true if the property is removed, false otherwise
     */
    public boolean removeProperty(String placeId, String key);    

    /**
     * Return the devices of a list of type presents in the places
     *
     * @param typeList the list of types to look for (if empty, return all
     * objects)
     * @param spaces the spaces where to find the objects (if empty return all
     * places)
     * @return a list of objects contained in these spaces
     */
    public ArrayList<String> getDevicesInSpaces(ArrayList<String> typeList, ArrayList<String> spaces);
    
    /**
     * Move a device in a specified place
     *
     * @param objId the object to move
     * @param srcPlaceId the previous place of this object
     * @param destPlaceId the destination of this object
     */
    public void moveDevice(String objId, String srcPlaceId, String destPlaceId);

    /**
     * *********************************
     */
    /**
     * Scheduling service management
     */
    /**
     * *********************************
     */
    /**
     * Try to schedule the start or stop of a program The Event is created just
     * one hour before current Time, and last for 30 minutes (it up to the end
     * user to modify this event according to its need)
     *
     * @param eventName is the name as it will appear in the Calendar
     * @param programId should be a VALID program ID referenced by EUDE
     * Interpreter
     * @param startOnBegin if program should start when Google Event begin
     * @param stopOnEnd if program should start when Google Event end
     */
    public void scheduleProgram(String eventName, String programId, boolean startOnBegin, boolean stopOnEnd);

    /**
     * List a set of Events referencing the programId (might be on begin/end or
     * start/stop)
     *
     * @param programId the program-id (not the program name) (we do not check
     * if the program-id really exist as we parse Google Calendar events)
     * @param startPeriod the starting period to observe, if -1 we start from
     * the beginning of the calendar (formatted according to RFC 3339 :
     * 2014-09-16T12:45:23+0200)
     * @param endPeriod the ending of the period to observe, if -1 we parse
     * until no more events left (formatted according to RFC 3339 :
     * 2014-09-16T12:45:23+0200)
     * @return The set of Events (events format depends on a particular
     * implementation, such as google event)
     */
    public Set<?> listEventsSchedulingProgramId(String programId, String startPeriod, String endPeriod);

    /**
     * Check if a particular program Id is scheduled in the future (on start or
     * on ending)
     *
     * @param programId the program-id (not the program name)
     * @return true if the program is scheduled in the future (we do not check
     * if the program-id really exist as we parse Google Calendar events)
     */
    public boolean checkProgramIdScheduled(String programId);

    /**
     * Check programs that are scheduled in the future (on start or on ending)
     *
     * @return a JSON Array containing all program scheduled in the future (we
     * do not check if the program-id really exist as we only parse Google
     * Calendar events)
     */
    public JSONArray checkProgramsScheduled();

    /**
     * *********************************
     */
    /**
     * End User programs management
     */
    /**
     * *********************************
     */
    /**
     * Deploy a new end user program in AppsGate system
     *
     * @param jsonProgram the JSONtree of the program
     * @return true if the program has been deployed, false otherwise
     */
    public boolean addProgram(JSONObject jsonProgram);

    /**
     * Remove a currently deployed program. Stop it, if it is running.
     *
     * @param programId the identifier of the program to remove.
     * @return true if the program has been removed, false otherwise.
     */
    public boolean removeProgram(String programId);

    /**
     * Update an existing program
     *
     * @param jsonProgram the JSONtree of the program
     * @return true if the program has been updated, false otherwise
     */
    public boolean updateProgram(JSONObject jsonProgram);

    /**
     * Run a deployed end user program
     *
     * @param programId the identifier of the program to run
     * @return true if the program has been launched, false otherwise
     */
    public boolean callProgram(String programId);

    /**
     * Stop a deployed program execution
     *
     * @param programId identifier of the program
     * @return true if the program has been stopped, false otherwise
     */
    public boolean stopProgram(String programId);

    /**
     * Get the list of current deployed programs
     *
     * @return the programs list as a JSONArray
     */
    public JSONArray getPrograms();

    public JSONObject getGraph(Boolean buildGraph);

    /**
     * Check if a program is active or not
     *
     * @param programId the identifier of the program
     * @return true if the program is active (STARTED), false otherwise
     */
    public boolean isProgramActive(String programId);

    /**
     * *********************************
     */
    /**
     * Core update Subscription
     */
    /**
     * *********************************
     */
    /**
     * This method allow the caller to add a specific coreListener to follow
     * core components state change.
     *
     * @param coreListener the listener for subscription
     */
    public void addCoreListener(CoreListener coreListener);

    /**
     * This method allow the caller to unsubscribe itself from core components
     * state change.
     *
     * @param coreListener
     */
    public void deleteCoreListener(CoreListener coreListener);

    /**
     * *********************************
     */
    /**
     * General AppsGate commands
     */
    /**
     * *********************************
     */
    /**
     * Shutdown the AppsGate system (Shutdown the OSGi distribution)
     */
    public void shutdown();

    /**
     * restart the AppsGate system (Restart the system bundle from OSGi)
     */
    public void restart();

    /**
     * *********************************
     */
    /**
     * Generic AppsGate commands
     */
    /**
     * *********************************
     */
    /**
     * Get a runnable object that can execute command from a remote device
     * manager asynchronously
     *
     * @param objIdentifier the identifier of the object on the remote system
     * @param method the method name to call
     * @param args the arguments list with their types
     * @return a runnable object that can be execute and manage.
     */
    public appsgate.lig.chmi.spec.GenericCommand executeRemoteCommand(String objIdentifier, String method, JSONArray args);

    /**
     * Get the current system time from the local EHMI clock
     *
     * @return the current time in milliseconds as a long
     */
    public long getCurrentTimeInMillis();

    /**
     * *********************************
     */
    /**
     * Client comm commands
     */
    /**
     * *********************************
     */
    /**
     * Add a new client connexion.
     *
     * @param cmdListener the callback for input messages
     * @param name the name of the connexion, muste be unique
     * @param port the port for the connexion
     * @return true if the connexion is opened, false otherwise
     */
    public boolean addClientConnexion(CommandListener cmdListener, String name, int port);

    /**
     * Remove an existing client connexion
     *
     * @param name the name of the connexion to remove
     * @return true if the connexion has been closed and removed, false
     * otherwise
     */
    public boolean removeClientConnexion(String name);

    /**
     * Send message to all clients behind a dedicated connection
     *
     * @param name the connection identifier
     * @param msg the message to send as a String
     */
    public void sendFromConnection(String name, String msg);

    /**
     * Send message to on client throught a dedicated connection
     *
     * @param name the connection name
     * @param clientId the targeted client identifier
     * @param msg the message to send
     */
    public void sendFromConnection(String name, int clientId, String msg);

    /**
     * *********************************
     */
    /**
     * Trace mananager commands
     */
    /**
     * *********************************
     */
    /**
     * Start the debugger on a new client connexion
     *
     * @return the port number on which the connexion is open, 0 if connexion
     * error
     */
    public int startDebugger();

    /**
     * Close the debugger client connexion
     *
     * @return true if the connexion is closed, flase otherwise
     */
    public boolean stopDebugger();

    /**
     * Get trace man status
     *
     * @return the complete status of trace man as a JSONObject
     */
    public JSONObject getTraceManStatus();
    
    /**
     * Return a list of 
     * @param programid
     * @return 
     */
    public SpokObject getProgramDependencies(String programid);
    /**
     * Return a list of 
     * @param programid
     * @return 
     */
    public SpokObject getProgram(String programid);
}
