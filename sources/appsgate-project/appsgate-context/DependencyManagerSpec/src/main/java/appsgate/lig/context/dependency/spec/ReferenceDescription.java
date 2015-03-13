package appsgate.lig.context.dependency.spec;

import appsgate.lig.context.dependency.spec.Reference.REFERENCE_TYPE;
import appsgate.lig.ehmi.spec.SpokObject;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class ReferenceDescription implements SpokObject {

    private final REFERENCE_TYPE referenceType;
    private final String method;

    /**
     * Constructor
     *
     * @param reference_type
     * @param eventName
     */
    public ReferenceDescription(REFERENCE_TYPE reference_type, String eventName) {
        referenceType = reference_type;
        method = eventName;
    }

    /**
     *
     * @return the method name
     */
    public String getMethod() {
        return method;
    }

    /**
     *
     * @return the reference type
     */
    public String getReferenceType() {
        return referenceType.toString();
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("method", this.method);
            o.put("referenceType", referenceType);
        } catch (JSONException ex) {
        }
        return o;
    }

    @Override
    public String getType() {
        return "referenceDescription";
    }

    @Override
    public String getValue() {
        return getReferenceType() + ":" + getMethod();
    }

    boolean isReferenceType(REFERENCE_TYPE refType) {
        return refType == referenceType;
    }
}
