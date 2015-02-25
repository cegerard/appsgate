package appsgate.lig.ehmi.trace;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public interface TraceHistory {
    Boolean init();
    void close();
    void trace(JSONObject o);
    JSONArray get(Long timestamp, Integer count);
    JSONArray getInterval(Long start, Long end);
}
