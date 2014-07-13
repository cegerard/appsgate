package appsgate.lig.ehmi.trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * The printWriter for the trace file on the hard drive
     */
    private PrintWriter traceFileWriter;

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
    		JSONObject coreNotif = new JSONObject();
    		coreNotif.put("timestamp", timeStamp);
    		coreNotif.put("id", srcId);
    		coreNotif.put("name", varName);
    		coreNotif.put("state", value);
    		
    		// TODO complete the notification JSON entry
    		
    		trace(coreNotif);
    		
    	} catch (JSONException jsonEx) {
			LOGGER.error("JSONException thrown: " + jsonEx.getMessage());
		}
    }
    
	@Override
	public synchronized void coreUpdateNotify(long timeStamp, String srcId, String coreType,
			String userType, String name, JSONObject description, String eventType) {
		try {
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

}
