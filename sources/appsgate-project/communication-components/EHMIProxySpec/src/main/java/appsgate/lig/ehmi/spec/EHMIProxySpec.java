package appsgate.lig.ehmi.spec;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.ehmi.spec.listeners.CoreListener;

/**
 * EHMI specification that define all method that a client can remote call
 * to interact with the AppsGate system.
 * 
 * @author Cedric Gérard
 * @since June 19, 2013
 * @version 1.0.0
 *
 */
public interface EHMIProxySpec {
	
	/***************************/
	/**   Device management    */
	/***************************/
	
	/**
	 * Get all the devices description
         * @return 
	 */
	public JSONArray getDevices();
	
	/**
	 * Get device details
	 * @param deviceId the targeted device identifier
	 * @return the device description as a JSONObject
	 */
	public JSONObject getDevice(String deviceId);
	
	/**
	 * Get all the devices of a specify user type
	 * @param type the type to filter devices
	 * @return the device list as a JSONArray
	 */
	public JSONArray getDevices(String type);
	
    /**
     * Get the state representation for a device type in the grammar
     * @param objectId the device from which to get the state representation
     * @param stateName the specific state to extract
     * @return the state description
     */
    public StateDescription getEventsFromState(String objectId, String stateName);

	
	/***************************/
	/** Device properties management */
	/***************************/
	
	/**
	 * Call AppsGate to add a user object name 
	 * @param objectId the object
	 * @param user the user that name this object
	 * @param name the new name of this object
	 */
	public void setUserObjectName(String objectId, String user, String name);
	
	/**
	 * Get the name of an object for a specific user
	 * @param objectId the object
	 * @param user the user who ask
	 * @return the name of the object named by user
	 */
	public String getUserObjectName(String objectId, String user);
	
	/**
	 * Delete an name for an object set by a user
	 * @param objectId the object
	 * @param user the user who give the name to this object
	 */
	public void deleteUserObjectName(String objectId, String user);
	
	/**
	 * Add grammar in the context properties manager for a new device type
         * @param deviceId the id if the device
	 * @param deviceType the type of device
	 * @param grammarDescription the grammar associated to the device type
	 * @return true if the grammar is really new, false if the grammar has been replaced
	 */
	public boolean addGrammar(String deviceId, String deviceType, GrammarDescription grammarDescription);
	
	/**
	 * Remove grammar associated to a device type
	 * @param deviceType the device type from which to remove the grammar
	 * @return true if the grammar has been removed, false otherwise
	 */
	public boolean removeGrammar(String deviceType);

	/**
	 * Get the grammar associated to a device type
	 * @param deviceType the device type from which to get the grammar
	 * @return the grammar as a JSONObject
	 */
	public GrammarDescription getGrammarFromType(String deviceType);
	/**
	 * Get the grammar associated to a device 
	 * @param deviceId the device id from which to get the grammar
	 * @return the grammar as a JSONObject
	 */
	public GrammarDescription getGrammarFromDevice(String deviceId);
	
	
	/***************************/
	/**    Place management    */
	/***************************/

	/**
	 * Call AppsGate to get all existing place definition.
	 * @return a JSON array that describe each place.
	 */
	public JSONArray getPlaces();

	/**
	 * Add a new place and move object in it.
	 * @param place the new place description and the list of object to move in
	 */
	public void newPlace(JSONObject place);

	/**
	 * Update a place on the smart place
	 * @param place the new place description
	 */
	public void updatePlace(JSONObject place);

	/**
	 * Remove a place from the smart place
	 * @param id the place identifier
	 */
	public void removePlace(String id);

	/**
	 * Move a device in a specified place
	 * @param objId the object to move
	 * @param srcPlaceId the previous place of this object
	 * @param destPlaceId the destination of this object
	 */
	
	public void moveDevice(String objId, String srcPlaceId, String destPlaceId);
	
	/**
	 * Move a service in a specified place
	 * @param serviceId the service to move
	 * @param srcPlaceId the previous place of this object
	 * @param destPlaceId the destination of this object
	 */
	public void moveService(String serviceId, String srcPlaceId, String destPlaceId);

	/**
	 * Get the place identifier of a core object
	 * @param objId the core object identifier 
	 * @return the identifier of the place where the core object is placed.
	 */
	public String getCoreObjectPlaceId(String objId);
	
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
	 * Call AppsGate to get all the places that match a specific name
	 * @param name the name to match
	 * @return the places with the name <name> as a JSONArray
	 */
	public JSONArray getPlacesByName(String name);
	
	/**
	 * Get places that have been tagged with all tags
	 * give in parameter.
	 * @param tags the tags list that places have to match
	 * @return places as a JSONArray
	 */
	public JSONArray gePlacesWithTags(JSONArray tags);
	
	/**
	 * Get places that contains the properties keys in parameters
	 * @param keys all properties that places have to be set
	 * @return places list as a JSONArray
	 */
	public JSONArray getPlacesWithProperties(JSONArray keys);
	
	/**
	 * Get places that contains the properties keys in parameters
	 * and with the corresponding values
	 * @param properties all properties that places have to be set with
	 * the corresponding value
	 * @return places list as a JSONArray
	 */
	public JSONArray getPlacesWithPropertiesValue(JSONArray properties);
	
	/** Get the root places description
	 * @return all root places as a JSONArray
	 */
	public JSONArray getRootPlaces();
	
	/**
	 * Add a tag to the tag of list of the specified place
	 * @param placeId the place where to add the tag
	 * @param tag the tag to add
	 * @return true if the tag has been added, false otherwise
	 */
	public boolean addTag(String placeId, String tag);
	
	/**
	 * Remove a tag from a place
	 * @param placeId the place from where to remove the tag
	 * @param tag the tag to remove
	 * @return true if the tag has been removed, false otherwise
	 */
	public boolean removeTag(String placeId, String tag);
	
	/**
	 * Add a property to a specified place
	 * @param placeId the place where to add the property
	 * @param key the key of the property to add
	 * @param value the value of the property to add
	 * @return true f the property has been added, false otherwise
	 */
	public boolean addProperty(String placeId, String key, String value);
	
	/**	
	 * Remove a property from a specified place
	 * @param placeId the place from where to remove the property	 
	 * @param key the key of the property that have to be removed
	 * @return true if the property is removed, false otherwise
	 */
	public boolean removeProperty(String placeId, String key);
	
	
	/***************************/
	/**  End User management   */
	/***************************/
	
	/**
	 * Get the end user list
	 * @return the user list as a JSONArray
	 */
	public JSONArray getUsers();

	/**
	 * Create a new end user
	 * @param id the user identifier
	 * @param password the user password
	 * @param lastName the user last name
	 * @param firstName the user first name
	 * @param role the user role
	 * @return true if the user is created, false otherwise
	 */
	public boolean createUser(String id, String password, String lastName, String firstName, String role);

	/**
	 * Delete an existing end user
	 * @param id the identifier of the user to be deleted
	 * @param password the corresponding password
	 * @return true if the user has been deleted, false otherwise
	 */
	public boolean deleteUser(String id, String password);

	/**
	 * Get details on a specify user
	 * @param id the identifier of the user
	 * @return user details as a JSONObject
	 */
	public JSONObject getUserDetails(String id);

	/**
	 * Get all information on a specify user
	 * @param id the identifier of the user
	 * @return user information as a JSONObject
	 */
	public JSONObject getUserFullDetails(String id);

	/**
	 * Check if the wanted identifier already existing.
	 * @param id the identifier to check
	 * @return true if the identifier is not use, false otherwise
	 */
	public boolean checkIfIdIsFree(String id);

	/**
	 * Synchronize a web service account with an end user profile
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param accountDetails all service account needed to be connected
	 * @return true if the service account has been synchronized, false otherwise
	 */
	public boolean synchronizeAccount(String id, String password, JSONObject accountDetails);

	/**
	 * delete service account synchronization
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param accountDetails all information needed to removed connection
	 * @return true it the synchronization has been canceled, false otherwise.
	 */
	public boolean desynchronizedAccount(String id, String password, JSONObject accountDetails);

	/**
	 * Associate a device to an end user
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param deviceId the device identifier
	 * @return true if the association has been completed, false otherwise
	 */
	public boolean associateDevice(String id, String password, String deviceId);

	/**
	 * Remove end user and device association
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param deviceId the device identifier
	 * @return true if the association has been deleted, false otherwise
	 */
	public boolean separateDevice(String id, String password, String deviceId);

    /************************************/
    /**  Weather Observers management   */
    /************************************/

    /**
     * Check a location upon it place Name
     *
     * @param location
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @return A JSON object describing the location
     * {"locality1":"Grenoble","woeid":"593720","name":"Grenoble","placeTypeName":"Town","country":"France"}
     */
    public JSONObject checkLocation(String location);	
    
    /**
     * Check a location upon the first letters of its place name
     *
     * @param location
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @return A JSONArray with 0..5  objects describing the location (formatted as follow), example starting with Gre
     * [
     * {"locality1":"Grenoble","woeid":"593720","name":"Grenoble","placeTypeName":"Town","country":"France"},
     * {"locality1":"Green Bay","woeid":"2413753","name":"Green Bay","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greenville","woeid":"2414583","name":"Greenville","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greensboro","woeid":"2414469","name":"Greensboro","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greifswald","woeid":"654035","name":"Greifswald","placeTypeName":"Town","country":"Allemagne"}
     * ]
     */
    public JSONArray checkLocationsStartingWith(String firstLetters);	    

    /**
     * Try to create an Observer with an human friendly name to fetch Weather conditions
     *
     * @param location
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     */
    public JSONArray addLocationObserver(String location);
    
    /**
     * Try to create an Observer with the explicit Yahoo WOEID (Where On Earth IDentifier)
     *
     */
    public JSONArray addLocationObserverFromWOEID(String woeid);
    

    /**
     * Try to remove a weather observer previously added
     *
     * @param location the placeName as it was previously added
     * @return true if the place was found and was successfully removed
     */
    public JSONArray removeLocationObserver(String location);

    /**
     * Retrieves the Location Observers currently running
     * @return
     */
    public JSONArray getActiveLocationsObservers();

    /**
     * Retrieves all Location Observers created (but maybe not running)
     * @return
     */
    public JSONArray getAllLocationsObservers();

    
    /************************************/
    /**  Scheduling service management  */
    /************************************/


	/**
	 * Try to schedule the start or stop of a program
	 * The Event is created just one hour before current Time, and last for 30 minutes
	 * (it up to the end user to modify this event according to its need)
	 * @param eventName is the name as it will appear in the Calendar
	 * @param programId should be a VALID program ID referenced by EUDE Interpreter
	 * @param startOnBegin if program should start when Google Event begin
	 * @param stopOnEnd if program should start when Google Event end
	 */
	public void scheduleProgram(String eventName, String programId, boolean startOnBegin, boolean stopOnEnd);
    
	/**
	 * List a set of Events referencing the programId (might be on begin/end or start/stop)
	 * @param programId the program-id (not the program name)
	 *  (we do not check if the program-id really exist as we parse Google Calendar events)
	 * @param startPeriod the starting period to observe, if -1 we start from the beginning of the calendar
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @param endPeriod the ending of the period to observe, if -1 we parse until no more events left
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @return The set of Events (events format depends on a particular implementation, such as google event)
	 */
	public Set<?> listEventsSchedulingProgramId(String programId, String startPeriod, String endPeriod);
	
	/**
	 * Check if a particular program Id is scheduled in the future (on start or on ending)
	 * @param progamId the program-id (not the program name)
	 * @return true if the program is scheduled in the future
	 *  (we do not check if the program-id really exist as we parse Google Calendar events)
	 */
	public boolean checkProgramIdScheduled(String programId);
        
        /**
	 * Check programs that are scheduled in the future (on start or on ending)
	 * @return a JSON Array containing all program scheduled in the future
	 *  (we do not check if the program-id really exist as we only parse Google Calendar events)
	 */
	public JSONArray checkProgramsScheduled();

	/************************************/
	/**   End User programs management  */
	/************************************/
	
	/**
	 * Deploy a new end user program in AppsGate system
	 * @param jsonProgram the JSONtree of the program
	 * @return true if the program has been deployed, false otherwise
	 */
	public boolean addProgram(JSONObject jsonProgram);
	
	/**
	 * Remove a currently deployed program.
	 * Stop it, if it is running.
	 * @param programId the identifier of the program to remove.
	 * @return true if the program has been removed, false otherwise.
	 */
	public boolean removeProgram(String programId);
	
	/**
	 * Update an existing program
	 * @param jsonProgram the JSONtree of the program
	 * @return true if the program has been updated, false otherwise
	 */
	public boolean updateProgram(JSONObject jsonProgram);
	
	/**
	 * Run a deployed end user program 
	 * @param programId the identifier of the program to run
	 * @return true if the program has been launched, false otherwise
	 */
	public boolean callProgram(String programId);
	
	/**
	 * Stop a deployed program execution
	 * @param programId identifier of the program
	 * @return true if the program has been stopped, false otherwise
	 */
	public boolean stopProgram(String programId);
	
	
	/**
	 * Get the list of current deployed programs
	 * @return the programs list as a JSONArray
	 */
	public JSONArray getPrograms();
	        public JSONObject getGraph() ;

	/**
	 * Check if a program is active or not
	 * 
	 * @param programId the identifier of the program
	 * @return true if the program is active (STARTED), false otherwise
	 */
	public boolean isProgramActive(String programId);
	
	
	/************************************/
	/**    Core update Subscription     */
	/************************************/
	
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
    
	/************************************/
	/**    General AppsGate commands    */
	/************************************/
	
	/**
	 * Shutdown the AppsGate system
	 * (Shutdown the OSGi distribution)
	 */
	public void shutdown();
	
	/**
	 * restart the AppsGate system
	 * (Restart the system bundle from OSGi)
	 */
	public void restart();
	
	/************************************/
	/**    Generic AppsGate commands    */
	/************************************/
    
    /**
	 * Get a runnable object that can execute command from a remote device manager asynchronously
	 * 
	 * @param objIdentifier the identifier of the object on the remote system
	 * @param method the method name to call
	 * @param args the arguments list with their types
	 * @return a runnable object that can be execute and manage.
	 */
	public appsgate.lig.chmi.spec.GenericCommand executeRemoteCommand(String objIdentifier, String method, JSONArray args);
	
	/**
	 * Get the current system time from the local EHMI clock
	 * @return the current time in milliseconds as a long
	 */
	public long getCurrentTimeInMillis() ;
	
	/************************************/
	/**       Client comm commands      */
	/************************************/
    
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
     * @return true if the connexion has been closed and removed, false otherwise
     */
    public boolean removeClientConnexion(String name);
    
    /**
     * Send message to all clients behind a dedicated connection
     * @param name the connection identifier
     * @param msg the message to send as a String
     */
    public void sendFromConnection(String name, String msg);
    
    /**
     * Send message to on client throught a dedicated connection
     * @param name the connection name
     * @param clientId the targeted client identifier
     * @param msg the message to send
     */
    public void sendFromConnection(String name, int clientId, String msg);
    
	/************************************/
	/**    Trace mananager commands     */
	/************************************/
   
    /**
     * Start the debugger on a new client connexion
     * @return the port number on which the connexion is open, 0 if connexion error
     */
    public int startDebugger();
    
    /**
     * Close the debugger client connexion
     * @return true if the connexion is closed, flase otherwise
     */
    public boolean stopDebugger();
    
    /**
     * Get trace man status
     * @return the complete status of trace man as a JSONObject
     */
    public JSONObject getTraceManStatus();
}