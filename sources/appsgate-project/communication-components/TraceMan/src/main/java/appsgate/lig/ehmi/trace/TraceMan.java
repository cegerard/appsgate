package appsgate.lig.ehmi.trace;

import java.util.ArrayList;
import java.util.Collection;
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
import appsgate.lig.eude.interpreter.spec.ProgramLineNotification;
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
     * Dependencies to the place manager
     */
    private PlaceManagerSpec placeManager;

    /*
     * The collection containing the links (wires) created, and deleted
     */
    private MongoDBConfiguration myConfiguration;


    /**
     *
     */
    private TraceHistory fileTracer;
    /**
     *
     */
    private TraceHistory dbTracer;
    
    /**
     * Define the trace time width in milliseconds
     * value to 0 means no aggregation interval and each change
     * is trace when it appear
     */
    private long deltaTinMillis = 0;
    
    /**
     * The buffer queue for AppsGate traces
     */
    private TraceQueue<JSONObject> traceQueue;
    
    /**
     * Thread to manage trace writing and aggregation
     */
    private TraceExecutor traceExec;

    /**
     * Last trace time stamp
     * use to avoid collisions
     */
	private long lastTimeStamp;

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        fileTracer = new TraceFile();
        if (!fileTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
        }
        dbTracer = new TraceMongo(myConfiguration);
        if (!dbTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
        }
        
        traceQueue = new TraceQueue<JSONObject>();
        traceExec = new TraceExecutor();
        new Thread(traceExec).start();
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
    	fileTracer.close();
        dbTracer.close();
    	traceExec.stop();
    }

    /**
     * Request the trace man instance to trace event.
     * Add the time stamp to the trace and put it in the queue
     * @param o the event to trace
     */
    private void trace(JSONObject o) {
    	synchronized(traceQueue) {
    		try {
    			o.put("timestamp", EHMIProxy.getCurrentTimeInMillis());
    			traceQueue.offer(o);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    	}
    	synchronized(traceExec) {
    		if (traceExec.isSleeping()) {
    			traceExec.notify();
    		}
    	}
    }
    
    /**
     * Write the trace into destination
     * @param trace the event to write
     * @exception trace time stamp must be greater than
     * one deltaTinMilis + previous written time stamp value
     */
    private synchronized void writeTrace(JSONObject trace){
		try {
			long timeStamp = trace.getLong("timestamp");
			
			if(timeStamp >/*=*/ lastTimeStamp + deltaTinMillis){
				
	    		lastTimeStamp = timeStamp;
	    		dbTracer.trace(trace);
	    		fileTracer.trace(trace);
	    		//liveTracer.trace(trace);
	    		
	    	}else {
	    		LOGGER.error("Multiple trace request with the same time stamp value: "+timeStamp+". Entry are skipped.");
	    		throw new Error("Multiple trace request with the same time stamp value. Entry with time stamp "+timeStamp+" are skipped.");
	    	}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Write an array of traces into the destination
     * @param traceArray the event array to write
     */
    private synchronized void writeTraces(JSONArray traceArray) {
    	int nbTraces = traceArray.length();
    	
    	for(int i=0; i < nbTraces; i++ ){
    		try {
				writeTrace(traceArray.getJSONObject(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    }

    @Override
    public synchronized void commandHasBeenPassed(String objectID, String command, String caller) {
        if (EHMIProxy.getGrammarFromDevice(objectID) != null) { //if the equipment has been instantiated from ApAM spec before
            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject deviceJson = getJSONDevice(objectID, event, Trace.getJSONDecoration("write", "user", null, objectID, "User trigger this action using HMI"));
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
            try {
                event.put("type", "update");
                event.put("state", getDeviceState(srcId, varName, value));
            } catch (JSONException e) {
            }
            JSONObject deviceJson = getJSONDevice(srcId, event, null);
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
            objectNotif.put("decoration", cause);
            objectNotif.put("event", event);

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
                cause = Trace.getJSONDecoration("", "technical", null, null, "Equipment appear");
                event.put("state", getDeviceState(srcId, "", ""));

            } else if (eventType.contentEquals("remove")) {
                event.put("type", "disappear");
                cause = Trace.getJSONDecoration("", "technical", null, null, "Equipment disappear");
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
        if (n instanceof ProgramLineNotification) {
            JSONObject o = getDecorationNotification((ProgramLineNotification) n);
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
        return dbTracer.get(timestamp, number);
    }

    @Override
    public JSONArray getTracesBetweenInterval(Long start, Long end) {
        return dbTracer.getInterval(start, end);
    }

    private JSONObject getJSONProgram(String id, String name, String change, String state, String iid) {
        JSONObject progNotif = new JSONObject();
        try {
            progNotif.put("id", id);
            progNotif.put("name", name);

            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject cause = new JSONObject();
            {
                JSONObject s = new JSONObject();
                s.put("name", state);
                s.put("instruction_id", iid);
                event.put("state", s);
                if (change != null) {
                    if (change.contentEquals("newProgram")) {
                        event.put("type", "appear");
                        cause = Trace.getJSONDecoration("", "user", null, null, "Program has been added");
                    } else if (change.contentEquals("removeProgram")) {
                        event.put("type", "disappaear");
                        cause = Trace.getJSONDecoration("", "user", null, null, "Program has been deleted");

                    } else { //change == "updateProgram"
                        event.put("type", "update");
                    }
                    //Create causality event entry
                    if (change.contentEquals("updateProgram")) {
                        cause = Trace.getJSONDecoration("", "user", null, null, "Program has been updated");
                    }
                }

            }
            progNotif.put("event", event);
            progNotif.put("decoration", cause);
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
            deviceState.put("status", "2");
        //    deviceState.put(varName, value); // Not sure this is really necessary
        } catch (JSONException ex) {
        }
        return deviceState;
    }

    private JSONObject getDecorationNotification(ProgramLineNotification n) {
        JSONObject p = getJSONProgram(n.getProgramId(), n.getProgramName(), null, n.getRunningState(), n.getInstructionId());
        JSONObject d = getJSONDevice(n.getTargetId(), null, Trace.getJSONDecoration(n.getType(), "Program", n.getSourceId(), null, n.getDescription()));
        try {
            p.put("decoration", Trace.getJSONDecoration(n.getType(), "Program", null, n.getTargetId(), n.getDescription()));
        } catch (JSONException ex) {
        }
        return getCoreNotif(d, p);
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
		this.deltaTinMillis = deltaTinMillis;
		traceExec.reScheduledTraceTimer(deltaTinMillis);
	}
    
    /******************************/
    /** Inner class for tracing **/
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
    	 * Default TraceQueue constructor with max capacity
    	 * set to 1000 elements
    	 */
		public TraceQueue() {
			super(1000);
			
		}
    	
    	/**
    	 * TraceQueue constructor with max capacity
    	 * @param capacity the max queue capacity
    	 */
		public TraceQueue(int capacity) {
			super(capacity);
			
		}
		
		/**
		 * Load the Queue with a collection of traces
		 * @param traces the collection to load
		 */
		public synchronized void loadTraces (Collection<E> traces) {
			this.clear();
			this.addAll(traces);
		}
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
    	 * Timer to wake up the thread each deltaT time interval
    	 */
    	private Timer timer;
    	
    	/**
    	 * Default constructor
    	 */
    	public TraceExecutor() {
    		
    		start = true;
    		sleeping = false;
    		timer = new Timer();
    		if(deltaTinMillis > 0) {
    			timer.scheduleAtFixedRate(new TimerTask() {
    				@Override
    				public void run() {traceExec.notify();}
    					}, 0, deltaTinMillis);
    		}
    	}
    	
		@Override
		public void run() {
			
			while(start) {
				try {
					if(traceQueue.isEmpty()){
						synchronized(this) {
							sleeping = true;
							wait();
							sleeping = false;
						}
					}
					writeTraces(apply(null));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Apply a policy on the queue to aggregate traces
		 * The default policy is the deltaTInMilis attribute
		 * 
		 * @param policy other policies to apply (e.g. type of device)
		 * @return the aggregated traces as a JSONArray
		 */
		private JSONArray apply(JSONObject policy) {
			synchronized(traceQueue) {
				
				JSONArray aggregateTraces = new JSONArray();
				
				//No aggregation
				if(policy == null && deltaTinMillis == 0) {
					
					while(!traceQueue.isEmpty()) {
						JSONObject trace = traceQueue.poll();
						aggregateTraces.put(trace);
					}
				
				}
				
				return aggregateTraces;
			}
		}
		
		/**
		 * Schedule the trace timer for aggregation
		 * @param time the time interval
		 */
		public void reScheduledTraceTimer(long time){
			timer.cancel();
			timer.purge();
			
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {traceExec.notify();}
					}, 0, time);
		}

		/**
		 * Is this thread infinitely sleeping
		 * @return true if the thread is waiting till the end of time, false otherwise
		 */
		public synchronized boolean isSleeping() {
			return sleeping;
		}
		
		/**
		 * Stop the current thread by ending its execution
		 * cleanly 
		 */
		public void stop(){
			timer.cancel();
			timer.purge();
			start = false;
			traceExec.notify();
		}
    }

}
