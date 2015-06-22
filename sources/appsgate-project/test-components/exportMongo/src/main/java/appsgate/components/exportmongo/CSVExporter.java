package appsgate.components.exportmongo;

import org.json.JSONObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

public class CSVExporter {

    /**
     * The collection containing symbol table
     */
    private static final String COLLECTION = "traces";

    /**
     * The database
     */
    private static final String DB = "TraceHistory";

    /**
     * The client
     */
    private final MongoClient mongoClient;
    private final PrintWriter progBuffer;
    private final PrintWriter devsBuffer;

    /**
     * Constructor.
     *
     * @param progs
     * @param devs
     * @throws UnknownHostException
     */
    public CSVExporter(PrintWriter progs, PrintWriter devs) throws UnknownHostException {
        mongoClient = new MongoClient();
        progBuffer = progs;
        devsBuffer = devs;
    }

    public void parseTraces(String ObjectName) throws JSONException {
        DBCollection context = mongoClient.getDB(DB).getCollection(COLLECTION);
        DBCursor cursor = context.find();
        parseResult(cursor);

    }

    private void parseResult(DBCursor cursor) throws JSONException {
        Map toMap;

        for (DBObject cur : cursor) {
            toMap = cur.toMap();
            JSONObject o = new JSONObject((String) toMap.get("trace"));
            parseProgArray(o.optJSONArray("programs"), o.optString("timestamp"));
            parseDevsArray(o.optJSONArray("devices"), o.optString("timestamp"));

        }

    }

    private void parseProgArray(JSONArray array, String timestamp) {
        if (array == null) {
            return;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                progBuffer.println(
                        timestamp + ","
                        + o.optString("id") + ","
                        + quote(o.optString("name")) + ','
                        + getEvent(o.optJSONObject("event")) + ","
                        + getDevice(o.optJSONArray("decorations")) + ","
                //        + quote( o.toString())
                );
            }
        }
    }

    private void parseDevsArray(JSONArray array, String timestamp) {
        if (array == null) {
            return;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                devsBuffer.println(
                        timestamp + ','
                        + o.optString("id") + ","
                        + o.optString("type") + ","
                        + quote(o.optString("name")) + ','
                        + getLocation(o.optJSONObject("location")) + ","
                        + getDeviceEvent(o.optJSONObject("event")) + ","
                     //   + quote(o.toString())
                );
            }
        }
    }

    private String getEvent(JSONObject event) {
        if (event != null) {
            JSONObject state = event.optJSONObject("state");
            if (state != null) {
                return state.optString("name");
            }
        }
        return "";
    }

    private String getDevice(JSONArray deco) {
        if (deco != null && deco.length() > 0) {
            JSONObject first = deco.optJSONObject(0);
            if (first != null) {
                return first.optString("picto") + "," + first.optString("device") + "," + first.optString("deviceName") + ",";
            }
        }
        return "";
    }

    private String getLocation(JSONObject loc) {
        if (loc == null) {
            return ",";
        }
        return loc.optString("id") + "," + quote(loc.optString("place"));
    }

    private String quote(String str) {
        if (str == null) {
            return "";
        }
        return '"' + str.replace('"', '\'') + '"';
    }

    private String getDeviceEvent(JSONObject deviceEvent) {
        if (deviceEvent != null) {
            return deviceEvent.optString("picto") + "," + quote(deviceEvent.optString("state"));
        }
        return ",";
    }

}
