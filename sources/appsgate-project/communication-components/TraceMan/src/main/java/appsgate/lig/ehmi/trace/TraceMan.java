package appsgate.lig.ehmi.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.GrammarDescription;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.ehmi.spec.trace.TraceManSpec;
import appsgate.lig.ehmi.trace.listener.TraceCmdListener;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;
import appsgate.lig.eude.interpreter.spec.ProgramNotification;
import appsgate.lig.manager.place.spec.PlaceManagerSpec;
import appsgate.lig.manager.place.spec.SymbolicPlace;
import appsgate.lig.persistence.MongoDBConfiguration;

/**
 * This component get CHMI from the EHMI proxy and got notifications for each
 * event in the EHMI layer to merge them into a JSON stream.
 *
 * @author Cedric Gerard
 * @since July 13, 2014
 * @version 1.1.0
 * 
 * Compliant with the version 4 of trace specification
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
     * Dependencies to the place manager
     */
    private PlaceManagerSpec placeManager;

    /**
     * The collection containing the links (wires) created, and deleted
     */
    private MongoDBConfiguration myConfiguration;


   /**
    * Default tracer use to have complete trace history
    * Only simple trace (no aggregation) are log in.
    */
   private TraceHistory dbTracer;
    
   /**
    * Boolean for file tracer activation
    */
   private boolean fileTraceActivated = false;
   
   /**
    * Trace log in file use to inspect trace
    */
   private TraceHistory fileTracer;
    
    /**
     *Boolean for live tracer activation
     */
    private boolean liveTraceActivated = false;
    
    /**
     * Trace log in real time
     */
    private TraceRT liveTracer;
    
    /**
     * The buffer queue for AppsGate simple traces
     */
    private TraceQueue<JSONObject> traceQueue;

    /**
     * Last trace time stamp
     * use to avoid collisions
     */
	private long lastTimeStamp;
	
	/**
	 * Debugger connection name
	 */
	private final String DEBUGGER_COX_NAME = "debugger";
	
	/**
	 * Debugger default port connection
	 */
	private final int DEBUGGER_DEFAULT_PORT = 8090;
	
    /**
     * The grouping policy
     */
	private String grouping = "type";

	/**
	 * The focus identifier
	 */
	private String focus = NOFOCUS;

	/**
	 * The focus type
	 */
	private String focusType;

	/**
	 * time line delta value for aggregation
	 */
	private long timeLineDelta;
	
	/**
	 * No filtering for traces
	 * (i.e. all trace are returned)
	 */
	public static final String NOFOCUS = "NONE";

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        
        dbTracer = new TraceMongo(myConfiguration);
        if (!dbTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
        }

        //TraceQueue initialization with no aggregation
        traceQueue = new TraceQueue<JSONObject>(0);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
    	
    	if(traceQueue != null)
    		traceQueue.stop();
    	if(fileTracer != null)
    		fileTracer.close();
    	
    	EHMIProxy.removeClientConnexion(DEBUGGER_COX_NAME);
        dbTracer.close();
    }

    /**
     * Request the trace man instance to trace event.
     * Add the time stamp to the trace and put it in the queue
     * 
     * @param o the event to trace
     */
    private void trace(JSONObject o) {
    	synchronized(traceQueue) {
    		try {
    			o.put("timestamp", EHMIProxy.getCurrentTimeInMillis());
    			
    			//Delayed in queue to by aggregate by policy if real time tracing is actived
    			if(liveTraceActivated || fileTraceActivated) {
    				traceQueue.offer(o);
    			}
    	    	//Simple trace always save in data base
    	    	dbTracer.trace(o);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    /**
     * Send the trace into destinations
     * Destination can not be dbTracer
     * 
     * @param trace the event to write
     * exception trace time stamp must be greater than
     * one deltaTinMilis + previous written time stamp value
     */
    private synchronized void sendTrace(JSONObject trace){
		try {
			long timeStamp = trace.getLong("timestamp");
			
			if(timeStamp > lastTimeStamp){
	    		lastTimeStamp = timeStamp;
	   
	    		if(liveTraceActivated) {
	    			liveTracer.trace(trace); //Send trace packet to client side
	    		}
	    		
	    		if(fileTraceActivated) {
	    			fileTracer.trace(trace); //Save into local file
	    		}
	    		
	    	} else {
	    		LOGGER.error("Multiple trace request with the same time stamp value: "+timeStamp+". Entry are skipped.");
	    		throw new Error("Multiple trace request with the same time stamp value. Entry with time stamp "+timeStamp+" are skipped.");
	    	}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Send an array of traces into the destinations
     * using sendTrace method
     * 
     * @param traceArray the event array to write
     */
    private synchronized void sendTraces(JSONArray traceArray) {
    	int nbTraces = traceArray.length();
    	
    	for(int i=0; i < nbTraces; i++ ){
    		try {
				sendTrace(traceArray.getJSONObject(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    }

    @Override
    public synchronized void commandHasBeenPassed(String objectID, String command, String caller) {
        if (EHMIProxy.getGrammarFromDevice(objectID) != null) { //if the equipment has been instantiated from ApAM spec before
            JSONObject deviceJson = getJSONDevice(objectID, null, Trace.getJSONDecoration("write", "user", null, objectID, "User trigger this action ("+command+") using HMI"));
            //Create the notification JSON object
            JSONObject coreNotif = getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif);
        }
    }

    @Override
    public synchronized void coreEventNotify(long timeStamp, String srcId, String varName, String value) {
        if (EHMIProxy.getGrammarFromDevice(srcId) != null) { //if the equipment has been instantiated from ApAM spec before
            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject JDecoration = null;
            try {
            	
            	if(varName.equalsIgnoreCase("status")) {
            		if(value.equalsIgnoreCase("2")){
            			event.put("type", "connection");
            			event.put("picto", Trace.getConnectionPicto());
                		JDecoration = Trace.getJSONDecoration("connection", "technical", srcId, null, "Connection");
                	}else if (value.equalsIgnoreCase("0")) {
                		event.put("type", "deconnection");
                		event.put("picto", Trace.getDeconnectionPicto());
                		JDecoration = Trace.getJSONDecoration("deconnection", "technical", srcId, null, "Deconnection");
                	} else {
                		event.put("type", "update");
                		JDecoration = Trace.getJSONDecoration("error", "technical", srcId, null, "Error dectected");
                		event.put("picto", Trace.getPictoState(EHMIProxy.getGrammarFromDevice(srcId).getType(), varName, value));
                	}
            	}else{
            		 event.put("type", "update");
            		 JDecoration = Trace.getJSONDecoration("update", "technical", srcId, null, "update of "+varName+" to "+ value);
            		 event.put("picto", Trace.getPictoState(EHMIProxy.getGrammarFromDevice(srcId).getType(), varName, value));
            	}
                event.put("state", getDeviceState(srcId, varName, value));
               
            } catch (JSONException e) {
            }
            
            JSONObject deviceJson = getJSONDevice(srcId, event, JDecoration);
            //Create the notification JSON object
            JSONObject coreNotif = getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif);
        }
    }

	private JSONObject getCoreNotif(JSONObject device, JSONObject program) {
        JSONObject coreNotif = new JSONObject();
        try {
            //Create the device tab JSON entry
            JSONArray deviceTab = new JSONArray();
            {
                if (device != null) {
                    deviceTab.put(device);
                }
                coreNotif.put("devices", deviceTab);
            }
            //Create the device tab JSON entry
            JSONArray pgmTab = new JSONArray();
            {
                if (program != null) {
                    pgmTab.put(program);
                }
                coreNotif.put("programs", pgmTab);
            }
        } catch (JSONException e) {

        }
        return coreNotif;
    }

    private JSONObject getJSONDevice(String srcId, JSONObject event, JSONObject cause) {
        JSONObject objectNotif = new JSONObject();
        try {
            objectNotif.put("id", srcId);
            objectNotif.put("name", devicePropTable.getName(srcId, ""));
            GrammarDescription g =  EHMIProxy.getGrammarFromDevice(srcId);
            if (g != null) {
            objectNotif.put("type", g.getType());
            }
            JSONObject location = new JSONObject();
            location.put("id", placeManager.getCoreObjectPlaceId(srcId));
            SymbolicPlace place = placeManager.getPlaceWithDevice(srcId);
            if (place != null) {
                location.put("name", place.getName());
            }
            objectNotif.put("location", location);
            objectNotif.put("decorations", new JSONArray().put(cause));

            if(event != null) {
            	objectNotif.put("event", event);
            }
            
        } catch (JSONException e) {

        }
        return objectNotif;

    }

    @Override
    public synchronized void coreUpdateNotify(long timeStamp, String srcId, String coreType,
            String userType, String name, JSONObject description, String eventType) {

        JSONObject event = new JSONObject();
        JSONObject cause = new JSONObject();
        try {
            if (eventType.contentEquals("new")) {
                event.put("type", "appear");
                cause = Trace.getJSONDecoration("appear", "technical", srcId, null, "Equipment ("+ name +") appear");
                event.put("state", getDeviceState(srcId, "", ""));

            } else if (eventType.contentEquals("remove")) {
                event.put("type", "disappear");
                cause = Trace.getJSONDecoration("disappear", "technical", srcId, null, "Equipment ("+name+") disappear");
            }

        } catch (JSONException e) {

        }

        JSONObject jsonDevice = getJSONDevice(srcId, event, cause);
        JSONObject coreNotif = getCoreNotif(jsonDevice, null);
        //Trace the notification JSON object in the trace file
        trace(coreNotif);

    }

    /**
     *
     * @param n
     */
    public synchronized void gotNotification(NotificationMsg n) {
        if (!(n instanceof ProgramNotification)) {
            return;
        }
        if (n instanceof ProgramCommandNotification) {
            JSONObject o = getDecorationNotification((ProgramCommandNotification) n);
            trace(o);
            return;
        }
        ProgramNotification notif = (ProgramNotification) n;
        //Create the notification JSON object
        //Create a device trace entry
        //Trace the notification JSON object in the trace file
        JSONObject jsonProgram = getJSONProgram(notif.getProgramId(), notif.getProgramName(), notif.getVarName(), notif.getRunningState(), null);

        trace(getCoreNotif(null, jsonProgram));
    }

    @Override
    public JSONArray getTraces(Long timestamp, Integer number) {
    	JSONArray tracesTab = dbTracer.get(timestamp, number);
    	if(traceQueue.getDeltaTinMillis() == 0){ //No aggregation
    		return tracesTab;
    	} else { // Apply aggregation policy
    		try {
    			//filteringOnFocus(tracesTab);
    			traceQueue.stop();
    			traceQueue.loadTraces(tracesTab);
				return traceQueue.applyAggregationPolicy(timestamp, null); //Call with default aggregation policy (id and time)
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	
    	return tracesTab;
    }

    @Override
    public void getTracesBetweenInterval(Long from, Long to, boolean withEventLine, JSONObject request) {
    	JSONObject requestResult = new JSONObject();
    	JSONArray tracesTab = dbTracer.getInterval(from, to);
    	JSONObject result = new JSONObject();
		try {
			if(traceQueue.getDeltaTinMillis() == 0){ //No aggregation
    		
				result.put("data", tracesTab);
    			result.put("groups", computeGroupsFromPolicy(tracesTab)); 
    			if(withEventLine){
    				result.put("eventline", eventLineComputation(tracesTab, from));
    			}
				requestResult.put("result", result);
				requestResult.put("request", request);
    		
				EHMIProxy.sendFromConnection(DEBUGGER_COX_NAME, requestResult.toString());
				
			} else { // Apply aggregation policy

    			//filteringOnFocus(tracesTab);
    			result.put("groups", computeGroupsFromPolicy(tracesTab));
    			if(withEventLine){
    				result.put("eventline", eventLineComputation(tracesTab, from));
    			}
    			traceQueue.stop();
    			traceQueue.loadTraces(tracesTab);
    			tracesTab = traceQueue.applyAggregationPolicy(from, null); //Call with default aggregation policy (id and time)

    			result.put("data", tracesTab);
    			
    			requestResult.put("result", result);
    			requestResult.put("request", request);
    			
    			EHMIProxy.sendFromConnection(DEBUGGER_COX_NAME, requestResult.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

	/**
     * Compute groups to display
     * By default the type is to make group.
     * If a focus is define, the gourping policy can be type of dep
     *  
     * @param tracesTab the trace tab use to compute group from
     * @return a JSONArray containing each group
     * @throws JSONException
     */
    private JSONArray computeGroupsFromPolicy(JSONArray tracesTab) throws JSONException {
    	
    	JSONArray groups = new JSONArray();
    	HashMap<String, JSONArray> groupFollower = new HashMap<String, JSONArray>();
    	int l = tracesTab.length();
    	
    	if(focus.equalsIgnoreCase(TraceMan.NOFOCUS)){ //No specific focus required
    		
    		if(grouping.equalsIgnoreCase("type")){ //One group for each type	
    			for(int i=0; i<l; i++) {
    	    		JSONObject superTrace = tracesTab.getJSONObject(i);
    	    		ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);
    	    		
    	    		for(JSONObject trace : innerTraces){
    	    			String type = "program"; //Defaut is a program
    	    			if (trace.has("type")){ //in fact is an equipment
    	    				type = trace.getString("type");
    	    			}        			
    	    			if(!groupFollower.containsKey(type)){
    	    				JSONArray objs = new JSONArray();
    	    				objs.put(trace.get("id"));
    	    				groupFollower.put(type, objs);
    	    			}else{
    	    				JSONArray objs = groupFollower.get(type);
    	    				if(!objs.toString().contains(trace.getString("id"))){
    	    					objs.put(trace.get("id"));
    	    				}
    	    			}
    	    		}
    			}	
    		}else{ //just the all group
    			groupFollower.put("all", new JSONArray());
    			for(int i=0; i<l; i++) {
        	    	JSONObject superTrace = tracesTab.getJSONObject(i);
        	    	ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);
        	    	
        	    	for(JSONObject trace : innerTraces){
        	    		JSONArray objs = groupFollower.get("all");
        	    		if(!objs.toString().contains(trace.getString("id"))){
        	    			objs.put(trace.get("id"));
        	    		}
        	    	}
    			}
    		}
    			
    	}else { //Focus required check the kind of focus
    		
    		if(focusType.equalsIgnoreCase("id")){ //Focus on something (equipment or program)
    			groupFollower.put("focus", new JSONArray().put(focus));
    			groupFollower.put("others", new JSONArray());
    				
    			if(grouping.equalsIgnoreCase("dep")){//Group based on id dependency (focus, dependencies, others)
    				groupFollower.put("dependencies", new JSONArray());

    				for(int i=0; i<l; i++) {
            	    	JSONObject superTrace = tracesTab.getJSONObject(i);
            	    	ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);
            	    	
            	    	for(JSONObject trace : innerTraces){
            	    		JSONArray objs = null;
            	    		
            	    		if(!trace.getString("id").equalsIgnoreCase(focus) && trace.toString().contains(focus)) { //dep
            	    			objs = groupFollower.get("dependencies");
            	    		} else { //others
            	    			objs = groupFollower.get("others");
            	    		}
            	    		
            	    		if(!objs.toString().contains(trace.getString("id"))){
            	    			objs.put(trace.get("id"));
            	    		}
            	    	}
        			}
    					
    			} else { //One group focus and all in other
    				JSONArray objs = groupFollower.get("others");
    				for(int i=0; i<l; i++) {
            	    	JSONObject superTrace = tracesTab.getJSONObject(i);
            	    	ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);
            	    	
            	    	for(JSONObject trace : innerTraces){
            	    		if(!objs.toString().contains(trace.getString("id"))){
            	    			objs.put(trace.get("id"));
            	    		}
            	    	}
    				}
    			}
    			
    		} else if (focusType.equalsIgnoreCase("location")){ //focus on location name (location name, others)
    				
    			groupFollower.put(focus, new JSONArray());
    			groupFollower.put("others", new JSONArray());
    				
    			for(int i=0; i<l; i++) {
        	    	JSONObject superTrace = tracesTab.getJSONObject(i);
        	    	ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);
        	    	
        	    	for(JSONObject trace : innerTraces){
        	    		JSONArray objs = null;
        	    	
        	    		if(trace.has("location")){ //Equipment
        	    			JSONObject loc = trace.getJSONObject("location");
        	    			
        	    			if(loc.getString("id").equalsIgnoreCase("-1")){
        	    				objs = groupFollower.get("others");
        	    			}else{
        	    				if(loc.getString("name").equalsIgnoreCase(focus)){
        	    					objs = groupFollower.get(focus);
        	    				}else{
        	    					objs = groupFollower.get("others");
        	    				}	
        	    			}
        	    		}else{ //Program
        	    			objs = groupFollower.get("others");
        	    		}
        	    		
        	    		if(!objs.toString().contains(trace.getString("id"))){
        	    			objs.put(trace.get("id"));
        	    		}
        	    	}
				}
    				
    		} else if (focusType.equalsIgnoreCase("type")){ //focus on type (type, others)
    			groupFollower.put(focus, new JSONArray());
    			groupFollower.put("others", new JSONArray());
    				
    			for(int i=0; i<l; i++) {
        	    	JSONObject superTrace = tracesTab.getJSONObject(i);
        	    	ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);
        	    	
        	    	for(JSONObject trace : innerTraces){
        	    		JSONArray objs = null;
        	    	
        	    		String type = "program"; //Defaut is a program
        	    		if (trace.has("type")){ //in fact is an equipment
        	    			type = trace.getString("type");
        	    		}
            			
        	    		if(type.equalsIgnoreCase(focus)){
        	    			objs = groupFollower.get(focus);
        	    		} else {
        	    			objs = groupFollower.get("others");
        	    		}
            			
        	    		if(!objs.toString().contains(trace.getString("id"))){
        	    			objs.put(trace.get("id"));
        	    		}
        	    	}
        		}
    		}
    	}
    	
    	//Fill the JSONArray with HashMap
    	for(String key : groupFollower.keySet()){
    		JSONObject obj = new JSONObject();
    		obj.put("name", getDiplayableName(key));
    		obj.put("members", groupFollower.get(key));
    		groups.put(obj);
    	}
    	
		return groups;
	}
    
    /**
     * Compute the event line for debugger
     * @param traces default traces tab
     * @param from start time stamp
     * @return the event line as a JSONArray
     * @throws JSONException 
     */
    private JSONArray eventLineComputation(JSONArray traces, long from) throws JSONException {
    	
    	JSONArray eventLine = new JSONArray();
    	int size = traces.length();
    	JSONObject trace;
    	long beg = from;
    	long end = from+timeLineDelta;
    	ArrayList<JSONObject> interval = new ArrayList<JSONObject>();
    	
    	for(int i=0; i<size; i++){
    		
    		trace = traces.getJSONObject(i);
    		long ts = trace.getLong("timestamp");
    		
    		if(ts >= beg && ts < end){
    			interval.add(trace);
    		}else{
    			if(!interval.isEmpty()){
    				JSONObject entry = new JSONObject();
    				entry.put("timestamp", beg);
    				int nbEvent = 0;
    				for(JSONObject tr : interval){
    					nbEvent += tr.getJSONArray("programs").length()+tr.getJSONArray("devices").length();
    				}
    				entry.put("value", nbEvent);
    				eventLine.put(entry);
    				interval.clear();
    			}
    			i--; //Ensure that all trace are placed in time stamp interval
    			beg = end;
    			end += timeLineDelta;
    		}
    	}
    	
		if(!interval.isEmpty()){
			JSONObject entry = new JSONObject();
			entry.put("timestamp", beg);
			int nbEvent = 0;
			for(JSONObject tr : interval){
				nbEvent += tr.getJSONArray("programs").length()+tr.getJSONArray("devices").length();
			}
			entry.put("value", nbEvent);
			eventLine.put(entry);
		}
    	
    	return eventLine;
	}

//	/**
//     * Filter trace on focus identifier
//     * @param tracesTab the focuses equipment identifier
//     * @throws JSONException 
//     */
//    private void filteringOnFocus(JSONArray tracesTab) throws JSONException {
//    	if(!focus.equalsIgnoreCase(TraceMan.NOFOCUS)) {
//    		int l = tracesTab.length();
//    		int i = 0;
//    	
//    		JSONArray filteredArray = new JSONArray();
//    	
//    		while(i < l) {
//    			JSONObject obj = tracesTab.getJSONObject(i);
//    			if(obj.toString().contains(focus)){
//    				filteredArray.put(obj);
//    			}
//    			i++;
//    		}
//    	
//    		tracesTab = filteredArray;
//    	}
//	}

    /**
     * Moph name with more diplayable string
     * @param key the group name
     * @return the morph name from key name
     */
    private String getDiplayableName(String key) {
		String displayableName;
    	
		String firstChar = key.substring(0, 1).toUpperCase();
		
		displayableName = firstChar + key.substring(1) + "s";
		
		return displayableName;
	}

	/**
     * Merge programs and equipment traces from a super traces into
     * a simple arraylist of JSONbject
     * @param superTrace the super traces from any sources
     * @return an ArrayList<JSONObject> of all inner traces
     * @throws JSONException 
     */
	private ArrayList<JSONObject> mergeInnerTraces(JSONObject superTrace) throws JSONException {
		ArrayList<JSONObject> innerTraces = new ArrayList<JSONObject>(); 
		
		JSONArray pgms    = superTrace.getJSONArray("programs");
		JSONArray devices = superTrace.getJSONArray("devices");
		
		int nbPgms = pgms.length();
		int nbDev  = devices.length();
		
		for(int i=0; i<nbPgms; i++ ){
			innerTraces.add(pgms.getJSONObject(i));
		}
		
		for(int j=0; j<nbDev; j++){
			innerTraces.add(devices.getJSONObject(j));
		}
		
		return innerTraces;
	}

	private JSONObject getJSONProgram(String id, String name, String change, String state, String iid) {
        JSONObject progNotif = new JSONObject();
        try {
            progNotif.put("id", id);
            progNotif.put("name", name);

            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject cause = null;
            {
                JSONObject s = new JSONObject();
                
                if(state.equalsIgnoreCase("deployed")){
                	 s.put("name", "disabled");
                } else if (state.equalsIgnoreCase("invalid")) {
                	 s.put("name", state.toLowerCase());
                } else {
                	 s.put("name", "enabled");
                }
                
               
                s.put("instruction_id", iid);
                event.put("state", s);
                if (change != null) {
                    if (change.contentEquals("newProgram")) {
                        event.put("type", "appear");
                        cause = Trace.getJSONDecoration("newProgram", "user", name, null, "Program "+name+" has been added");
                    } else if (change.contentEquals("removeProgram")) {
                        event.put("type", "disappear");
                        cause = Trace.getJSONDecoration("removeProgram", "user", name, null, "Program "+name+" has been deleted");

                    } else { //change == "updateProgram"
                        event.put("type", "update");
                        cause = Trace.getJSONDecoration("updateProgram", "user", name, null, "Program "+name+" has been updated");
                    }
                }

            }
            progNotif.put("event", event);
            progNotif.put("decorations", new JSONArray().put(cause));

        } catch (JSONException e) {

        }
        return progNotif;
    }

    private JSONObject getDeviceState(String srcId, String varName, String value) {
        JSONObject deviceState = new JSONObject();
        GrammarDescription g = EHMIProxy.getGrammarFromDevice(srcId);
        // If the state of a device is complex

        JSONObject deviceProxyState = EHMIProxy.getDevice(srcId);
        ArrayList<String> props = g.getProperties();
        for (String k : props) {
            if (k != null && !k.isEmpty()) {
                try {
                    deviceState.put(g.getValueVarName(k), deviceProxyState.get(k));
                } catch (JSONException ex) {
                    LOGGER.error("Unable to retrieve key[{}] from {} for {}", k, srcId, g.getType());
                    LOGGER.error("DeviceState: " + deviceProxyState.toString());
                }
            }
        }
        try {
        	if(varName.equalsIgnoreCase("status")){
        		 deviceState.put("status", value);
        	} else {
        		deviceState.put("status", "2");
        	}
          
        } catch (JSONException ex) {
        }
        return deviceState;
    }

    private JSONObject getDecorationNotification(ProgramCommandNotification n) {
        JSONObject p = getJSONProgram(n.getProgramId(), n.getProgramName(), null, n.getRunningState(), n.getInstructionId());
        JSONObject d = getJSONDevice(n.getTargetId(), null, Trace.getJSONDecoration(n.getType(), "Program", n.getSourceId(), null, n.getDescription()));
        try {
            p.put("decorations", new JSONArray().put(Trace.getJSONDecoration(n.getType(), "Program", null, n.getTargetId(), n.getDescription())));
        } catch (JSONException ex) {
        }
        return getCoreNotif(d, p);
    }
    
    /**
     * Get the current delta time for trace aggregation
     * @return the delta time in milliseconds
     */
    public long getDeltaT() {
		return traceQueue.getDeltaTinMillis();
	}

    /**
     * Set the delta time for traces aggregation
     * @param deltaTinMillis the new delta time value
     */
	public void setDeltaT(long deltaTinMillis) {
		traceQueue.setDeltaTinMillis(deltaTinMillis);
	}
	
	/**
	 * Set the current time line delta value
	 * @param timeLineDelta the new time line delta value
	 */
	public void setTimeLineDelta(long timeLineDelta) {
		this.timeLineDelta = timeLineDelta;
	}
	
    /**
     * Get the current time line delta time for trace aggregation
     * @return the delta time in milliseconds
     */
    public long getTimeLineDelta() {
		return timeLineDelta;
	}
	
	
    /**
     * set the grouping policy
     * @param order the policy to make group from
     */
	public void setGroupingOrder(String order) {
		this.grouping = order;
	}

	/**
	 * Set the filtering identifier for trace
	 * @param focus the identifier use to filter trace
	 * @param focusType the type of focus (location, type, equipment)
	 */
	public void setFocusEquipment(String focus, String focusType) {
		this.focus = focus;
		this.focusType = focusType;
	}
		
	@Override
	public int startDebugger(){
		//Socket and live trace initialization
	    if(EHMIProxy.addClientConnexion(new TraceCmdListener(this), DEBUGGER_COX_NAME, DEBUGGER_DEFAULT_PORT)){
	        return DEBUGGER_DEFAULT_PORT;
	    }else {
	    	return 0;
		}
	}

	@Override
	public boolean stopDebugger() {
		if(liveTracer != null){
			liveTraceActivated = false;
			liveTracer.close();
			liveTracer = null;
		}
		
		if(fileTracer != null){
			fileTraceActivated = false;
			fileTracer.close();
			fileTracer = null;
		}
		traceQueue.stop();
		return EHMIProxy.removeClientConnexion(DEBUGGER_COX_NAME);
	}
	
	/**
	 * Initiate the live tracer
	 * @return true if the live tracer is ready, false otherwise
	 */
	public boolean initLiveTracer() {
		liveTracer = new TraceRT(DEBUGGER_COX_NAME, EHMIProxy);
        liveTraceActivated = true;
        
        if(!traceQueue.isInitiated()){
        	traceQueue.initTraceExec();
        }
        
        return liveTraceActivated;
	}
	
	/**
	 * Initiate the file tracer
	 * @return true if the file tracer is initiated, false otherwise
	 */
	public boolean initFileTracer() {
		fileTracer = new TraceFile();
        if (!fileTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
            fileTraceActivated = false;
        }else{
        	fileTraceActivated = true;
        }
        
        if(!traceQueue.isInitiated()){
        	traceQueue.initTraceExec();
        }
        
        return fileTraceActivated;
	}
	
    
    /*****************************/
    /** Trace Queue inner class **/
    /*****************************/

	/**
     * TraceQueue is a dedicated queue for AppsGate
     * JSON formatted traces.
     * 
     * @author Cedric Gerard
     * @since August 06, 2014
     * @version 0.5.0
     */
    private class TraceQueue<E> extends ArrayBlockingQueue<E>{

    	
		private static final long serialVersionUID = 1L;
		
	    /**
	     * Thread to manage trace writing and aggregation
	     */
	    private TraceExecutor traceExec;

        /**
         * Timer to wake up the thread each deltaT time interval
         */
        private Timer timer;

        /**
         * The next time to log
         */
        private long logTime;
	    
	    /**
	     * Define the trace time width in milliseconds
	     * value to 0 means no aggregation interval and each change
	     * is trace when it appear
	     */
	    private long deltaTinMillis;
	    
	    /**
	     * Is the trace executor thread initiated or not
	     */
	    private boolean initiated = false;

		/**
    	 * Default TraceQueue constructor with max capacity
    	 * set to 50000 elements
    	 * @param deltaT time interval size of packet
    	 */
		public TraceQueue(long deltaT) {
			super(50000);
			
			this.deltaTinMillis = deltaT;
			traceExec = new TraceExecutor();
		}
		
		/**
    	 * TraceQueue constructor with max capacity
    	 * @param capacity the max queue capacity
    	 * @param deltaT time interval size of packet
    	 */
		public TraceQueue(int capacity, long deltaT) {
			super(capacity);
			
			this.deltaTinMillis = deltaT;
			traceExec = new TraceExecutor();
		}

        /**
         * Initiate the trace executor thread/service
         */
        public void initTraceExec() {
            new Thread(traceExec).start();
            timer = new Timer();
            if(deltaTinMillis > 0) {
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized(traceExec) {
                            if(traceQueue != null && !traceQueue.isEmpty() && traceExec.isSleeping()){
                                logTime = EHMIProxy.getCurrentTimeInMillis();
                                traceExec.notify();
                            }
                        }
                    }
                }, deltaTinMillis, deltaTinMillis);
            }
            initiated = true;
        }
		
    	@Override
		public boolean offer(E e) {
			boolean res = super.offer(e);
			
			synchronized(traceExec) {
				if(deltaTinMillis == 0 && traceExec.isSleeping()) {
	    			traceExec.notify();
	    		}
	    	}
			
			return res;
		}

		/**
    	 * Stop the trace queue thread execution
    	 */
    	public void stop() {
    		if(timer != null) {
    			timer.cancel();
    			timer.purge();
    		}
			traceExec.stop();
			synchronized(traceExec) {
				traceExec.notify();
			}
		}
		
		/**
		 * Load the Queue with a collection of traces
		 * @param traces the collection to load
		 * @throws JSONException 
		 */
		@SuppressWarnings("unchecked")
		public synchronized void loadTraces (JSONArray traces) throws JSONException {
			
			ArrayList<E> traceCollection = new ArrayList<E>();
			int nbTrace = traces.length();
			
			for(int i =0; i < nbTrace; i++){
				traceCollection.add((E) traces.getJSONObject(i));
			}

			this.clear();
			this.addAll(traceCollection);
		}
		
		/**
	     * Get the current delta time for trace aggregation
	     * @return the delta time in milliseconds
	     */
	    public long getDeltaTinMillis() {
			return deltaTinMillis;
		}
	    
	    /**
	     * Set the delta time for traces aggregation
	     * @param deltaTinMillis the new delta time value
	     */
		public void setDeltaTinMillis(long deltaTinMillis) {
			if(deltaTinMillis != this.deltaTinMillis) {
				this.deltaTinMillis = deltaTinMillis;
				reScheduledTraceTimer(deltaTinMillis);
			}
		}

        /**
         * Schedule the trace timer for aggregation
         * @param time the time interval
         */
        public void reScheduledTraceTimer(long time){
        	if(isInitiated()){
        		timer.cancel();
        		timer.purge();

        		timer = new Timer();
        		if(deltaTinMillis > 0) {
        			timer.scheduleAtFixedRate(new TimerTask() {
        				@Override
        				public void run() {
        					synchronized (traceExec) {
        						if (!traceQueue.isEmpty() && traceExec.isSleeping()) {
        							logTime = EHMIProxy.getCurrentTimeInMillis();
        							traceExec.notify();
        						}
        					}
        				}
        			}, deltaTinMillis, deltaTinMillis);
        		}
        	}
        }
		
		/**
		 * Aggregate traces with the specified policy
		 * @param from the starting date
		 * @param policy the policy to apply to traces
		 * @return a JSONArray of aggregate traces
		 * @throws JSONException 
		 */
		public synchronized JSONArray applyAggregationPolicy(long from, JSONObject policy) throws JSONException{
			
			JSONArray aggregateTraces = new JSONArray();
			
			if(policy == null){ //default aggregation (time and identifiers)

				long beginInt = from;
				long  endInt = from+deltaTinMillis;
                JSONArray tracesPacket = new JSONArray();
                
                while(!traceQueue.isEmpty()) {

                	JSONObject latestTrace = traceQueue.peek();
                	long latestTraceTS = latestTrace.getLong("timestamp");
                	
                	if(latestTraceTS >= beginInt && latestTraceTS < endInt ){
                		tracesPacket.put(traceQueue.poll());
                	}else{
                		beginInt = endInt;
                		endInt +=  deltaTinMillis;
                		
                		if(tracesPacket.length() > 0){ //A packet is complete
                			traceExec.aggregation(aggregateTraces, tracesPacket, beginInt);
                			tracesPacket = new JSONArray();
                		}
                	}
                }
                
                if(tracesPacket.length() > 0){ //A packet is complete
        			traceExec.aggregation(aggregateTraces, tracesPacket, beginInt);
                }
                
			} else { //Apply specific aggregation policy
				//generic aggregation mechanism
				//aggregationWithPolicy(aggregateTraces, tracesPacket, logTime, policy)
			}
			return aggregateTraces;
		}
		
		/**
		 * Is the trace executor thread initiated
		 * @return true if the trace executor thread is started, false otherwise
		 */
		public boolean isInitiated() {
			return initiated;
		}

		/**
	     * A thread class to trace all elements in the trace
	     * queue
	     * 
	     * @author Cedric Gerard
	     * @since August 06, 2014
	     * @version 0.5.0
	     */
	    private class TraceExecutor implements Runnable {
	    	
	    	/**
	    	 * Indicates if the thread is in infinite
	    	 * waiting 
	    	 */
	    	private boolean sleeping;

	    	/**
	    	 * Use to manage thread loop execution
	    	 */
	    	private boolean start;
	    	
	    	/**
	    	 * Default constructor
	    	 */
	    	public TraceExecutor() {
	    		
	    		start = false;
	    		sleeping = false;
	    	}
	    	
			public void stop() {
				start = false;
				initiated = false;
			}

			@Override
			public void run() {
				start = true;
				while(start) {
					try {
						if( deltaTinMillis > 0 || traceQueue.isEmpty() ){
							synchronized(this) {
								sleeping = true;
								wait();
								sleeping = false;
							}
						}
						if(start){
							sendTraces(apply(null));
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}  catch (JSONException ex) {
                        ex.printStackTrace();
                    }
				}
			}
			
			/**
			 * Apply a policy on the queue to aggregate traces
			 * The default policy is the deltaTInMillis attribute
			 * 
			 * @param policy other policies to apply (e.g. type of device)
			 * @return the aggregated traces as a JSONArray
			 */
			public JSONArray apply(JSONObject policy) throws JSONException {
				synchronized(traceQueue) {
					
					JSONArray aggregateTraces = new JSONArray();
					
					//No aggregation
					if(policy == null && deltaTinMillis == 0) {
						
						while(!traceQueue.isEmpty()) {
							JSONObject trace = traceQueue.poll();
							aggregateTraces.put(trace);
						}
					
					}else {
						if(policy == null){ //default aggregation (time and identifiers)
                            //Get all traces from trace queue
                            JSONArray tempTraces = new JSONArray();
                            while(!traceQueue.isEmpty()) {
                            	tempTraces.put(traceQueue.poll());
                            }
                            aggregation(aggregateTraces, tempTraces, logTime);

						} else { //Apply specific aggregation policy
							//generic aggregation mechanism
							//aggregationWithPolicy(aggregateTraces, tracesPacket, logTime, policy)
						}
					}
					
					return aggregateTraces;
				}
			}
			
			/**
			 * Aggregates traces from a packet and add the aggregate trace to an array
			 * @param aggregateTraces result of all aggregations
			 * @param tracesPacket traces to aggregates
			 * @param logTime timestamp for this trace
			 * @throws JSONException 
			 */
			public void aggregation(JSONArray aggregateTraces, JSONArray tracesPacket, long logTime) throws JSONException {
				//Create new aggregate trace instance
	            JSONObject jsonTrace = new JSONObject();
	            jsonTrace.put("timestamp", logTime);
	            HashMap<String,JSONObject> devicesToAgg = new HashMap<String, JSONObject>();
	            HashMap<String,JSONObject> programsToAgg = new HashMap<String, JSONObject>();

	            int nbTraces = tracesPacket.length();
	            int i = 0;
	            while(i < nbTraces){
	                //Get a trace to aggregate from the array
	                JSONObject tempObj = tracesPacket.getJSONObject(i);
	                JSONArray tempDevices = tempObj.getJSONArray("devices");
	                JSONArray tempPgms = tempObj.getJSONArray("programs");

	                int tempDevicesSize = tempDevices.length();
	                int tempPgmsSize = tempPgms.length();

	                //If there is some device trace to merge
	                if(tempDevicesSize > 0){
	                    int x = 0;
	                    while(x < tempDevicesSize){
	                        //Merge the device trace
	                        JSONObject tempDev = tempDevices.getJSONObject(x);
	                        //tempDev.put("timestamp", tempObj.get("timestamp"));
	                        String id = tempDev.getString("id");
	                        
	                        if(!devicesToAgg.containsKey(id)){ //No aggregation for now
	      
	                            devicesToAgg.put(id, tempDev);
	                            
	                        }else{ //Device id exist for this time stamp --> aggregation
	                        	JSONObject existingDev = devicesToAgg.get(id);
	                        	if(tempDev.has("event")){//replace the state by the last known state
	                        		existingDev.put("event", tempDev.get("event")); 
	                        	}
	                        	//Aggregates the device trace has a decoration
	                        	JSONArray existingDecorations = existingDev.getJSONArray("decorations");
	                        	JSONArray tempDecs = tempDev.getJSONArray("decorations");
	                        	int decSize = tempDecs.length();
	                        	int x1 = 0;
	                        	while(x1 < decSize){
	                        		JSONObject tempDec = tempDecs.getJSONObject(x1);
	                        		tempDec.put("order", existingDecorations.length());
	                        		existingDecorations.put(tempDec);
	                        		x1++;
	                        	}
	                        }
	                        x++;
	                    }
	                }

	                //If there is some program traces to merge
	                if( tempPgmsSize > 0){
	                    int y = 0;
	                    while(y < tempPgmsSize){
	                        //Merge program traces
	                        JSONObject tempPgm = tempPgms.getJSONObject(y);
	                        //tempPgm.put("timestamp", tempObj.get("timestamp"));
	                        String id = tempPgm.getString("id");
	                        
	                        if(!programsToAgg.containsKey(id)){//No aggregation for now
	                        	
	                            programsToAgg.put(id, tempPgm);
	                            
	                        }else{ //program id exist for this time stamp --> aggregation
	                        	
	                        	JSONObject existingPgm = programsToAgg.get(id);
	                        	if(tempPgm.has("event")){//replace the state by the last known state
	                        		existingPgm.put("event", tempPgm.get("event")); 
	                        	}
	                        	
	                        	//Aggregates the device trace has a decoration
	                        	JSONArray existingDecorations = existingPgm.getJSONArray("decorations");
	                        	JSONArray tempDecs = tempPgm.getJSONArray("decorations");
	                        	int decSize = tempDecs.length();
	                        	int y1 = 0;
	                        	while(y1 < decSize){
	                        		JSONObject tempDec = tempDecs.getJSONObject(y1);
	                        		tempDec.put("order", existingDecorations.length());
	                        		existingDecorations.put(tempDec);
	                        		y1++;
	                        	}
	                        }
	                        y++;
	                    }
	                }
	                i++;
	            }

	            jsonTrace.put("devices", new JSONArray(devicesToAgg.values()));
	            jsonTrace.put("programs", new JSONArray(programsToAgg.values()));
				aggregateTraces.put(jsonTrace);
			}

			/**
			 * Is this thread infinitely sleeping
			 * @return true if the thread is waiting till the end of time, false otherwise
			 */
			public synchronized boolean isSleeping() {
				return sleeping;
			}
			
	    }
		
    }

}
