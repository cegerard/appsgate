package appsgate.lig.ehmi.trace;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.ehmi.spec.EHMIProxySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceRT implements TraceHistory {

    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceRT.class);

    /**
     * The name of the connection to send trace
     */
    private final String connectionName;

    /**
     * the port of opened connection
     */
    private final EHMIProxySpec ehmi;

    /**
     * The list of all live trace subscribers
     */
    private final ArrayList<Integer> liveTraceSubscribers = new ArrayList<Integer>();

    /**
     * Constructor
     *
     * @param connectionName
     * @param ehmi
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
        LOGGER.error("Trying to get information from TraceRT");
        return null;

    }

    @Override
    public JSONArray getInterval(Long start, Long end) {
        LOGGER.error("Trying to get information from TraceRT");
        return null;
    }

    /**
     * Method that remove all subscribers
     */
    public void clearSubscribers() {
        LOGGER.trace("All subscribers have been cleared");
        liveTraceSubscribers.clear();
    }

    /**
     * Method to add a subscriber
     * @param clientId 
     */
    public void addSubscriber(int clientId) {
        if (!liveTraceSubscribers.contains(clientId)) {
            LOGGER.trace("Added subscriber: {}", clientId);
            liveTraceSubscribers.add(clientId);
        } else {
            LOGGER.trace("Already subscriber: {}", clientId);
        }
    }

    /**
     * Method to remove a subscriber
     * @param clientId 
     */
    public void removeSubscriber(int clientId) {
        if (liveTraceSubscribers.remove(clientId) > 0) {
            LOGGER.trace("Removing subscriber: {}", clientId);
        } else {
            LOGGER.trace("Subscriber was not present: {}", clientId);
        }
    }
}
