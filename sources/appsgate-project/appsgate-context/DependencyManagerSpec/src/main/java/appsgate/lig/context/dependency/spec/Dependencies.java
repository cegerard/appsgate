package appsgate.lig.context.dependency.spec;

import java.util.ArrayList;
import appsgate.lig.ehmi.spec.SpokObject;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class Dependencies implements SpokObject{
    public ArrayList<String> actsOn_ids = new ArrayList<String>();
    public ArrayList<String> readedDeviceId = new ArrayList<String>();
//    public ArrayList<String> referencedBy =

    @Override
    public JSONObject getJSONDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getType() {
        return "dependencyTree";
    }

    @Override
    public String getValue() {
        return null;
    }
}
