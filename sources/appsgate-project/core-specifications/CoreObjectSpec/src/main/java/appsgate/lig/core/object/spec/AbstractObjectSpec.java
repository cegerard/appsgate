package appsgate.lig.core.object.spec;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class to inherit all the stuff of CoreObjects
 * Created by thibaud on 25/07/2014.
 */
public abstract class AbstractObjectSpec extends CoreObjectBehavior implements CoreObjectSpec {

    protected String appsgatePictureId;
    protected String appsgateUserType;
    protected String appsgateDeviceStatus;
    protected String appsgateObjectId;
    protected String appsgateServiceName;

    protected CORE_TYPE appsgateCoreType;

    @Override
    public String getAbstractObjectId() {
        return appsgateObjectId;
    }

    @Override
    public String getUserType() {
        return appsgateUserType;
    }

    @Override
    public int getObjectStatus() {
        return Integer.parseInt(appsgateDeviceStatus);
    }

    @Override
    public String getPictureId() {
        return appsgatePictureId;
    }

    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();

        // mandatory appsgate properties
        descr.put("id", appsgateObjectId);
        descr.put("type", appsgateUserType);
        descr.put("status", appsgateDeviceStatus);

        descr.put("pictureId", appsgatePictureId);
        descr.put("name", appsgateServiceName);

        return descr;
    }

    @Override
    public void setPictureId(String pictureId) {
        this.appsgatePictureId = pictureId;

    }

    @Override
    public CORE_TYPE getCoreType() {
        return appsgateCoreType;
    }

}
