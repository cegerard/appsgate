package appsgate.lig.eude.interpreter.langage.components;

import org.json.JSONObject;

/**
 * interface that defines an object in AppsGate
 * an object will have a JSONDescroption
 * @author jr
 */
public interface SpokObject {
    
    public JSONObject getJSONDescription();
    
    public String getType();
    
    public String getValue();
}
