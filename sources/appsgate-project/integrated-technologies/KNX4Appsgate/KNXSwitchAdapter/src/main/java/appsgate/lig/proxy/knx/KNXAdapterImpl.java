package appsgate.lig.proxy.knx;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KNXAdapterImpl {

    private static final String APAM_SWITCH_IMPL="KNXSwitchImpl";
    private static final Logger logger = LoggerFactory.getLogger(KNXAdapterImpl.class);
    private SendWebsocketsService sendToClientService;
    private ListenerService listenerService;

    private Set<Switch> lights;

    public void on(String id){
        try {
            getDevice(id).on();
            logger.info("method On from instance {} was invoked",id);
        } catch (Exception e) {
            logger.error("instance {} not found, impossible to invoke method On",id,e);
        }
    }

    private Switch getDevice(String id) throws Exception{

        for(Switch light:lights){

            if(light.getId().equals(id)){
                return light;
            }
        }

        throw new Exception(String.format("Device %s not found",id));

    }

    private String generateAppsgateDeviceInstanceName(String deviceId){
        return "appsgate-"+deviceId;
    }

    public void off(String id){
        try {
            getDevice(id).off();
            logger.info("method Off from instance {} was invoked",id);
        } catch (Exception e) {
            logger.error("instance {} not found, impossible to invoke method Off",id,e);
        }
    }

    public Boolean isOn(String id){
        try {
            return getDevice(id).isOn();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void switchRemove(final Switch fuchsiaSwitch){

        Thread t1=new Thread(){

            public void run(){

                String appsgateInstanceName=generateAppsgateDeviceInstanceName(fuchsiaSwitch.getId());
                logger.info("KNX device removed, removing instance {} ..",appsgateInstanceName);
                ((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(appsgateInstanceName);
                logger.info("Appsgate instance {} removed",appsgateInstanceName);
            }

        };

        t1.setDaemon(true);
        t1.start();


    }



    public void switchAdd(final Switch fuchsiaSwitch){

        final String appsgateDeviceInstance=generateAppsgateDeviceInstanceName(fuchsiaSwitch.getId());

        logger.info("Found new KNX instance, creating Appsgate instance to represent the device {} ..",appsgateDeviceInstance);

        Thread t1=new Thread(){

            public void run() {

                Implementation apamImpl = CST.componentBroker.getImpl(APAM_SWITCH_IMPL);

                Map properties = new HashMap<String, String>();

                properties.put("instance.name", appsgateDeviceInstance);
                properties.put("deviceName", fuchsiaSwitch.getId());
                properties.put("deviceId", fuchsiaSwitch.getId());
                properties.put("deviceType", "7");

                Instance apamInstance = apamImpl.createInstance(null, properties);

                logger.info("Appsgate instance {} created", appsgateDeviceInstance);
            }
        };

        t1.setDaemon(true);
        t1.start();
    }


}
