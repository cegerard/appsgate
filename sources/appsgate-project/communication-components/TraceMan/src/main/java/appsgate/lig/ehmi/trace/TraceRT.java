package appsgate.lig.ehmi.trace;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.ehmi.spec.EHMIProxySpec;

public class TraceRT implements TraceHistory {
	
    /**
     * The name of the connection to send trace
     */
    private String connectionName;
    
    /**
     * the port of opened connection
     */
    private EHMIProxySpec ehmi;
    
    /**
     * The list of all live trace subscribers
     */
    private ArrayList<Integer> liveTraceSubscribers = new ArrayList<Integer>();

    /**
     * Constructor
     */
    public TraceRT(String connectionName, EHMIProxySpec ehmi) {
    	this.connectionName = connectionName;
    	this.ehmi = ehmi;
    }
    
	@Override
	public Boolean init() {
		return true;
	}

	@Override
	public void close() {
	}

	@Override
	public void trace(JSONObject o) {
		for (int subscriberId : liveTraceSubscribers) {
			ehmi.sendFromConnection(connectionName, subscriberId, o.toString());
		}
		
	}

	@Override
	public JSONArray get(Long timestamp, Integer count) {
		 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public JSONArray getInterval(Long start, Long end) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void clearSubscribers() {
		liveTraceSubscribers.clear();
	}

	public void addSubscriber(int clientId) {
		if(!liveTraceSubscribers.contains(clientId))
			liveTraceSubscribers.add(clientId);
	}

	public void removeSubscriber(int clientId) {
		liveTraceSubscribers.remove(clientId);
	}
}