package appsgate.lig.ehmi.spec.trace;

import org.json.JSONObject;

/**
 * This component is the Trace manager ApAM specification.
 * is use to share trace service to other components in the
 * EHMI layer.
 * 
 * @author Cedric Gerard
 * @since July 13, 2014
 * @version 1.0.0
 */
public interface TraceManSpec {
	
	/**
	 * callback use by EHMIProxyImpl to notify the trace
	 * manager that a new core event notification has been received.
	 * 
	 * @param timeStamp 
	 * @param srcId 
	 * @param varName
	 * @param value
	 */
	public void coreEventNotify(long timeStamp, String srcId, String varName, String value);

	/**
	 * callback use by EHMIProxyImpl to notify the trace
	 * manager that a new core update notification has been received.
	 * 
	 * @param eventType "new" or "remove"
	 * @param description 
	 * @param name 
	 * @param userType 
	 * @param coreType 
	 * @param srcId 
	 * @param timeStamp
	 */
	public void coreUpdateNotify(long timeStamp, String srcId, String coreType,
			String userType, String name, JSONObject description, String eventType);
        
        /**
         * 
         * @param objectID
         * @param command
         * @param caller 
         */
        public void commandHasBeenPassed(String objectID, String command, String caller);

}
