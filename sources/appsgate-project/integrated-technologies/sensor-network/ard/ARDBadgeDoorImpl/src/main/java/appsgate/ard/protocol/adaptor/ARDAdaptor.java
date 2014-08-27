package appsgate.ard.protocol.adaptor;


import appsgate.ard.protocol.controller.ARDController;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.ard.protocol.model.command.request.SubscriptionRequest;
import appsgate.ard.protocol.model.Constraint;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ARDAdaptor {

    private Logger logger = LoggerFactory.getLogger(ARDAdaptor.class);

    private ARDController ard;

    private Instance instance;

    private String server;

    Constraint constraintForARDContactSensor=new Constraint() {
        public boolean evaluate(JSONObject jsonObject) throws JSONException{
            return jsonObject.getJSONObject("event").getString("class").equals("card");
        }
    };

    public void validate() {

        try {

            logger.info("Instantiating ARD Controller (low level abstraction)");
            ard = ARDController.getInstance(server, 2001);
            logger.info("Connecting with ARD controller on IP {}", server);
            ard.connect();
            ard.monitoring();

            ard.sendRequest(new SubscriptionRequest());

            Implementation impl = CST.apamResolver.findImplByName(null, "ARDBadgeDoor");
            Map<String, String> properties = new HashMap<String, String>();

            properties.put("deviceName", "ARD-ContactSensor");
            properties.put("deviceId", "ARD-ContactSensor");
            properties.put("deviceType", "ARD_DEVICE");

            properties.put("pictureId", "");
            properties.put("userType", "");
            properties.put("status", "2");
            properties.put("currentStatus", "2");

            instance = impl.createInstance(null, properties);

            ard.getMapRouter().put(constraintForARDContactSensor, (ARDMessage) instance.getServiceObject());

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void invalidate() {
        System.out.println("Invalidate Protocol ARD");
        Thread t1=new Thread() {
            public void run() {
                String appsgateInstanceName = instance.getName();
                logger.info("ARD Door device removed, removing instance {} ..", appsgateInstanceName);
                ((ComponentBrokerImpl) CST.componentBroker).disappearedComponent(appsgateInstanceName);
                logger.info("Appsgate instance {} removed", appsgateInstanceName);
            }
        };
        t1.setDaemon(true);
        t1.start();

    }

}
