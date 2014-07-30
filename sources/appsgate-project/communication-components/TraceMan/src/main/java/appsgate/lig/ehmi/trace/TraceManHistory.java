/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.ehmi.trace;

import appsgate.lig.persistence.MongoDBConfiguration;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import java.util.Map;
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
                        .append("trace", o);

                ArrayList<BasicDBObject> stateArray = new ArrayList<BasicDBObject>();


                context.insert(newVal);
                return true;

            } catch (MongoException e) {
                
            }
        }
        return false;
    }

}
