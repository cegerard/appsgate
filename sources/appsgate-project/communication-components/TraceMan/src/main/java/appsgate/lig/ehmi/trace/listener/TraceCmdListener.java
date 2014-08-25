package appsgate.lig.ehmi.trace.listener;

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
		LOGGER.trace("debbuguer commande received: "+obj.toString());
		
		//TODO remove test
		if(obj.has("deactivate")){
			traceMan.toggleLiveTrace();
		}
		
		//TODO add client debugguer communication protocl here
	}

}
