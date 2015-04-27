package appsgate.lig.ehmi.trace;

import appsgate.lig.persistence.MongoDBConfiguration;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class TraceMongo implements TraceHistory {

    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceMongo.class);

    /**
     * The db name
     */
    private static final String DBNAME = "TraceHistory";
    /**
     * The collection containing trace table
     */
    private static final String TRACES = "traces";
    /**
     * The collection containing execution table
     */
    private static final String EXECUTION_TRACES = "execution";

    /**
     * The connection to the mongo db
     */
    private final MongoDBConfiguration conf;

    /**
     * Constructor
     *
     * @param c
     */
    public TraceMongo(MongoDBConfiguration c) {
        this.conf = c;
    }

    /**
     *
     * @param timestamp
     * @param o
     * @return
     */
    private boolean add(Long timestamp, String id, String trace) {
        if (isValidConf()) {
            try {
                DBCollection context = conf.getDB(DBNAME).getCollection(TRACES);

                BasicDBObject newVal = new BasicDBObject("name", id)
                        .append("time", timestamp)
                        .append("trace", trace);

                context.insert(newVal);
                return true;

            } catch (MongoException e) {
                LOGGER.error("A Database Excepion has been raised: " + e);
            }
        }
        return false;
    }

    @Override
    public void addExecutionTrace(Long timestamp, String pid, String node) {
        if (isValidConf()) {
            try {
                DBCollection context = conf.getDB(DBNAME).getCollection(EXECUTION_TRACES);

                BasicDBObject newVal = new BasicDBObject("pid", pid)
                        .append("time", timestamp)
                        .append("node", node);

                context.insert(newVal);

            } catch (MongoException e) {
                LOGGER.error("A Database Excepion has been raised: " + e);
            }
        }
    }

    @Override
    public JSONArray get(Long timestamp, Integer count) {
        if (isValidConf()) {

            DBCollection context = conf.getDB(DBNAME).getCollection(TRACES);
            DBCursor cursor = context
                    .find(new BasicDBObject("time", new BasicDBObject("$lte", timestamp)))
                    //.find()
                    .limit(count)
                    .sort(new BasicDBObject("time", 1));

            return formatData(cursor);
        }

        return null;

    }

    @Override
    public JSONArray getInterval(Long start, Long end) {
        if (isValidConf()) {

            DBCollection context = conf.getDB(DBNAME).getCollection(TRACES);
            DBCursor cursor = context
                    .find(new BasicDBObject("time", BasicDBObjectBuilder.start("$gte", start).add("$lte", end).get()))
                    .sort(new BasicDBObject("time", 1));

            return formatData(cursor);
        }
        return null;
    }

    @Override
    public JSONArray getLastState(JSONArray ids, Long timestamp) {
        JSONArray arr = new JSONArray();
        if (isValidConf() && ids != null) {
            DBCollection collection = conf.getDB(DBNAME).getCollection(TRACES);
            for (int i = 0; i < ids.length(); i++) {
                String id = ids.optString(i);
                DBCursor cursor = collection
                        .find(BasicDBObjectBuilder.start().add("time", BasicDBObjectBuilder.start("$lte", timestamp).get()).add("name", id).get())
                        .sort(new BasicDBObject("time", -1)).limit(1);
                try {
                    if (!cursor.hasNext()) {
                        LOGGER.debug("No logs for {} before the start of window", id);
                        continue;
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to parse cursor" + e.getMessage());
                    continue;
                }
                String trace = cursor.next().get("trace").toString();
                try {
                    JSONObject o = new JSONObject(trace);
                    o.put("timestamp", timestamp);
                    JSONArray array = o.optJSONArray("devices");
                    for (int a = 0; a < array.length(); a++) {
                        array.getJSONObject(i).put("decorations", new JSONArray());
                    }
                    array = o.optJSONArray("programs");
                    for (int a = 0; a < array.length(); a++) {
                        array.getJSONObject(i).put("decorations", new JSONArray());
                    }
                    arr.put(o);
                } catch (JSONException ex) {

                }
            }

        }
        return arr;
    }

    /**
     *
     * @param a
     * @param from
     * @param to
     * @return
     */
    public JSONArray appendTraces(JSONArray a, Long from, Long to) {
        JSONArray traces = getInterval(from, to);
        for (int i = 0; i < traces.length(); i++) {
            try {
                a.put(traces.get(i));
            } catch (JSONException ex) {
                // Never happens
            }
        }
        return a;
    }

    @Override
    public void close() {
    }

    @Override
    public void trace(JSONObject o) {
        try {
            JSONArray programs = o.getJSONArray("programs");
            if (programs.length() > 0) {
                String id = programs.getJSONObject(0).getString("id");
                add(o.getLong("timestamp"), id, o.toString());
            } else {
                JSONArray devices = o.getJSONArray("devices");
                String id = devices.getJSONObject(0).getString("id");
                add(o.getLong("timestamp"), id, o.toString());
            }
        } catch (JSONException ex) {
            // if there is no timestamp, just don't log the trace
        }
    }

    @Override
    public Boolean init() {
        return isValidConf();
    }

    /**
     * Method to format data
     *
     * @param cursor the cursor on the database
     * @return a JSON array containing the trace
     */
    private JSONArray formatData(DBCursor cursor) {
        JSONArray a = new JSONArray();
        for (DBObject cur : cursor) {
            String o;
            try {
                o = cur.get("trace").toString();
                a.put(new JSONObject(o));
            } catch (JSONException ex) {

            } catch (NullPointerException e) {
                // Traces database not clean
            }
        }
        return a;

    }

    private boolean isValidConf() {
        if (conf == null) {
            LOGGER.error("Unable to init TraceMongo, no MongoDB configuration");
            return false;
        }
        if (!conf.isValid()) {
            LOGGER.error("Unable to init TraceMongo, configuration not valid");
            return false;
        }
        return true;
    }

    public JSONArray getLastNodesId(Long timestamp) {
        JSONArray arr = new JSONArray();
        if (isValidConf()) {
            DBCollection collection = conf.getDB(DBNAME).getCollection(EXECUTION_TRACES);
            for (Object o : collection.distinct("pid")) {
                String id = o.toString();
                DBCursor cursor = collection
                        .find(BasicDBObjectBuilder.start().add("time", BasicDBObjectBuilder.start("$lte", timestamp).get()).add("name", id).get())
                        .sort(new BasicDBObject("time", -1)).limit(1);
                try {
                    if (!cursor.hasNext()) {
                        LOGGER.debug("No logs for {} before the start of window", id);
                        continue;
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to parse cursor" + e.getMessage());
                    continue;
                }
                String node = cursor.next().get("node").toString();
                try {
                    JSONObject json = new JSONObject();
                    json.put("pid", id);
                    json.put("node", node);
                    arr.put(json);
                } catch (JSONException ex) {

                }
            }
        }
        return arr;

    }

}
