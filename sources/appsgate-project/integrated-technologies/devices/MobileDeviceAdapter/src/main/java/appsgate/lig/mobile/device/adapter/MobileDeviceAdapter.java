package appsgate.lig.mobile.device.adapter;

import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.mobile.device.adapter.spec.MobileDeviceAdapterServices;
import appsgate.lig.mobile.device.MobileDeviceImpl;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class MobileDeviceAdapter extends CoreObjectBehavior implements MobileDeviceAdapterServices, CoreObjectSpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger logger = LoggerFactory.getLogger(MobileDeviceAdapter.class);

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public MobileDeviceAdapter() {
        logger.info("New color mobile device adapter");
        MobileDeviceImpl mob = createApamComponent();
        mob.setAdapter(this);
    }


    /**
     * Method that creates a MobileDeviceImpl
     *
     * @return a new ApamCompenent
     */
    private MobileDeviceImpl createApamComponent() {
        Implementation implem = CST.apamResolver.findImplByName(null, MobileDeviceImpl.IMPL_NAME);
        if (implem == null) {
            logger.error("createApamComponent(...) Unable to get APAM Implementation");
            return null;
        }
        logger.trace("createGroup(), implem found");
        Map<String, String> properties = new HashMap<String, String>();

        Instance inst = implem.createInstance(null, properties);

        if (inst == null) {
            logger.error("createApamComponent(...) Unable to create APAM Instance");
            return null;
        }

        return (MobileDeviceImpl) inst.getServiceObject();
    }

    @Override
    public String getAbstractObjectId() {
        return "MobileDeviceAdapter-0";
    }

    @Override
    public String getUserType() {
        return "MobileDeviceAdapter";
    }

    @Override
    public int getObjectStatus() {
        return 0;
    }

    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        descr.put("id", getAbstractObjectId());
        descr.put("type", getUserType());
        descr.put("coreType", getCoreType());
        descr.put("status", getObjectStatus());
        return descr;

    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.ADAPTER;
    }

    @Override
    public JSONObject getBehaviorDescription() {
        return null;
    }

    @Override
    public List<String> getDevicesId() {
        ArrayList<String> l = new ArrayList<String>();
        l.add("test");
        return l;
    }
}
