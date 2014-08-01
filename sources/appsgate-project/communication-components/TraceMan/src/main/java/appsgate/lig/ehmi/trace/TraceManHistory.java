/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.ehmi.trace;

import appsgate.lig.persistence.MongoDBConfiguration;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class TraceManHistory {

    private static final String DBNAME_DEFAULT = "TraceHistory";
    /**
     * The collection containing symbol table
     */
    private static final String CONTEXT_COLLECTION = "traces";

    public static boolean add(MongoDBConfiguration conf, Long timestamp, JSONObject o) {
        if (conf != null && conf.isValid()) {
            try {
                DBCollection context = conf.getDB(DBNAME_DEFAULT).getCollection(CONTEXT_COLLECTION);

                BasicDBObject newVal = new BasicDBObject("name", "")
                        .append("time", timestamp)
                        .append("trace", o.toString());

                context.insert(newVal);
                return true;

            } catch (MongoException e) {

            }
        }
        return false;
    }

    /**
     *
     * @param conf
     * @param timestamp
     * @param count
     * @return
     */
    public static JSONArray get(MongoDBConfiguration conf, Long timestamp, Integer count) throws JSONException {
        if (conf != null && conf.isValid()) {

            DBCollection context = conf.getDB(DBNAME_DEFAULT).getCollection(CONTEXT_COLLECTION);
            DBCursor cursor = context
                    .find(new BasicDBObject("time", new BasicDBObject("$lte", timestamp)))                   
                    //.find()
                    .limit(count)
                    .sort(new BasicDBObject("time", 1));

            JSONArray a = new JSONArray();
            for (DBObject cur : cursor) {
                String o = cur.get("trace").toString();
                a.put(new JSONObject(o));
            }
            return a;
        }

        return null;

    }
}
