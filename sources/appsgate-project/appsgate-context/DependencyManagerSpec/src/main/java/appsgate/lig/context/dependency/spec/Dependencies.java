package appsgate.lig.context.dependency.spec;

import java.util.ArrayList;
import appsgate.lig.ehmi.spec.SpokObject;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public abstract class Dependencies implements SpokObject {

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
    
    public abstract Set<String> getActsOnEntities();
    public abstract Set<String> getReadedEntities();
    public abstract Set<DeviceReference> getDevicesReferences();
    public abstract Set<ProgramReference> getProgramsReferences();
    public abstract Set<SelectReference> getSelectors();
}
