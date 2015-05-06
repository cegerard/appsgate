package appsgate.lig.mobile.device.adapter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsgate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.mobile.device.adapter.spec.MobileDeviceAdapterServices;
import appsgate.lig.mobile.device.impl.MobileDeviceImpl;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import java.util.HashMap;
import java.util.Map;

public class MobileDeviceAdapter implements MobileDeviceAdapterServices {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger logger = LoggerFactory.getLogger(MobileDeviceAdapter.class);

    private ListenerService listenerService;
    private SendWebsocketsService sendToClientService;

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        logger.info("New color mobile device adapter");
        MobileDeviceImpl mob = createApamComponent();
        mob.setAdapter(this);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        logger.info("A color light device adapter");
    }

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
}
