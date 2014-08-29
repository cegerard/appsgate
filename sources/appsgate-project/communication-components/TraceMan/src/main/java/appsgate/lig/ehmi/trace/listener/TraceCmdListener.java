package appsgate.lig.ehmi.trace.listener;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.ehmi.trace.TraceMan;

/**
 * This class is use to get back command from debugguer web client.
 * 
 * @author Cedric Gerard
 * @since August 25, 2014
 * @version 1.0.0
 */
public class TraceCmdListener implements CommandListener{
	
	/**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceCmdListener.class);
    
    /**
     * Trace manager instance
     */
    private TraceMan traceMan;
    
    /**
     * Constructor
     */
    public TraceCmdListener(TraceMan traceMan) {
    	this.traceMan = traceMan;
    }

	@Override
	public void onReceivedCommand(JSONObject obj) {
		LOGGER.debug("debugger command received: "+obj.toString());
		
		if(obj.has("name")){
			String cmd;
			try {
				cmd = obj.getString("name");
				if(cmd.equalsIgnoreCase("historytrace")){
					JSONObject args = obj.getJSONObject("args");
					long from = args.getLong("from");
					long to = args.getLong("to");
					//int res = args.getInt("res");
					
					//TODO set the deltaTInMilis from resolution param
					
					traceMan.getTracesBetweenInterval(from, to);
					
				}else if(cmd.equalsIgnoreCase("livetrace")){
					traceMan.initLiveTracer();					
				}else if (cmd.equalsIgnoreCase("filetrace")){
					traceMan.initFileTracer();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
