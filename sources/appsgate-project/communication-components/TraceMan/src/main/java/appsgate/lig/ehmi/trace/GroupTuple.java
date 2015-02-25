package appsgate.lig.ehmi.trace;

import org.json.JSONArray;

/**
 * A class that contains JSON arrays
 * @author jr
 */
public class GroupTuple {

    private final int order;
    private JSONArray members;

    public GroupTuple(int order, JSONArray members) {
        this.order = order;
        this.members = members;
    }

    public int getOrder() {
        return order;
    }

    public JSONArray getMembers() {
        return members;
    }

    public void setMembers(JSONArray members) {
        this.members = null;
        this.members = members;
    }
}
