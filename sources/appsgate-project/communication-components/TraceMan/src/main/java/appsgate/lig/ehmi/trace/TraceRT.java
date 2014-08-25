package appsgate.lig.ehmi.trace;

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
		ehmi.sendFromConnection(connectionName, o.toString());
	}

	@Override
	public JSONArray get(Long timestamp, Integer count) {
		 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public JSONArray getInterval(Long start, Long end) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}