package appsgate.lig.mobile.device.adapter;

import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.mobile.device.adapter.spec.MobileDeviceAdapterServices;
import appsgate.lig.mobile.device.MobileDeviceImpl;
import appsgate.lig.mobile.device.com.SendMessageSocket;
import appsgate.lig.mobile.device.com.SocketTasker;
import appsgate.lig.mobile.device.spec.CoreMobileDeviceSpec;
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

    private final SendMessageSocket socket;

    private final SocketTasker tasker;

    private List<CoreMobileDeviceSpec> mobiles;

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public MobileDeviceAdapter() {
        logger.info("New mobile device adapter");
        mobiles = new ArrayList<>();
        socket = new SendMessageSocket();
        tasker = new SocketTasker(this);
        tasker.identifyAndSubscribe();
        createDefaultApamComponent();
    }

    /**
     * Method that creates a MobileDeviceImpl
     *
     */
    private void createDefaultApamComponent() {
        Implementation implem = CST.apamResolver.findImplByName(null, MobileDeviceImpl.IMPL_NAME);
        if (implem == null) {
            logger.error("createApamComponent(...) Unable to get APAM Implementation");
            return;
        }
        logger.trace("createGroup(), implem found");
        Map<String, String> properties = new HashMap<>();

        Instance inst = implem.createInstance(null, properties);

        if (inst == null) {
            logger.error("createApamComponent(...) Unable to create APAM Instance");
            return;
        }
        MobileDeviceImpl mobile = (MobileDeviceImpl) inst.getServiceObject();
        mobile.setAdapter(this);
        mobiles.add(mobile);

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
        ArrayList<String> l = new ArrayList<>();
        l.add("test");
        return l;
    }

    @Override
    public Boolean sendMessage(String title, String msg) {
        return socket.sendPost(title, msg);
    }

    public void MessageReceived(JSONObject msg) {
        if (! msg.has("title")) {
            return;
        }
        if (!msg.has("message")) {
            return;
        }
        for (CoreMobileDeviceSpec m : mobiles) {
            m.emitTaskerMessage(msg);
        }
    }
}
