package appsgate.ard.protocol.fuchsia;

import appsgate.ard.protocol.controller.ARDController;
import appsgate.ard.protocol.model.Constraint;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.json.JSONException;
import org.json.JSONObject;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ARDSideTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ARDSideTask.class);

    ARDBrigdeImporter importer;

    ImportDeclaration importDeclaration;

    private final Map<String,ARDController> controllers;

    private final Map<ARDController,Set<String>> declarationController;


    ARDSideTask(ARDBrigdeImporter importer,final Map<String,ARDController> controllers,Map<ARDController,Set<String>> declarationController, ImportDeclaration importDeclaration){
        this.importer=importer;
        this.importDeclaration=importDeclaration;
        this.controllers=controllers;
        this.declarationController=declarationController;
    }

    private ARDController initController(String ip,Integer port){

        ARDController controller=controllers.get(ip);

        if(controller==null){

            logger.info("Initializing ARD Controller on IP {} port {}",ip,port);

            controller=new ARDController(ip,port);
            controllers.put(ip,controller);
            declarationController.put(controller,new HashSet<String>());
            try {
                controller.validate();
            } catch (JSONException e) {
                logger.error("Failed on starting up ARD controller",e);
            }
        }else {
            logger.info("Reusing existing socket for ARD Controller on IP {} port {}",ip,port);
        }

        return controller;

    }

    private ARDMessage initDoor(String id,Integer doorIdx,String doorName){

        Implementation impl = CST.componentBroker.getImpl("ARDBadgeDoor");// CST.apamResolver.findImplByName(null, "");
        Map<String, String> properties = new HashMap<String, String>();

        properties.put("deviceName", "ARD-Door-"+doorName);
        properties.put("deviceId", "ARD-DoorIdx"+doorIdx);
        properties.put(Factory.INSTANCE_NAME_PROPERTY, id);

        Instance instance = impl.createInstance(null, properties);

        return (ARDMessage)instance.getServiceObject();

    }

    @Override
    public void run() {
        String id=importDeclaration.getMetadata().get("id").toString();
        String ip=importDeclaration.getMetadata().get("ard.bridge.ip").toString();
        Integer port=Integer.parseInt(importDeclaration.getMetadata().get("ard.bridge.port").toString());
        final Integer doorIdx=Integer.parseInt(importDeclaration.getMetadata().get("ard.door_idx").toString());
        String doorName=importDeclaration.getMetadata().get("ard.door_name").toString();

        ARDController controller=initController(ip,port);

        Set<String> controllersDeclaration=declarationController.get(controller);

        controllersDeclaration.add(id);

        controller.getMapRouter().put(new Constraint() {
            @Override
            public boolean evaluate(JSONObject jsonObject) throws JSONException {
                return jsonObject.getJSONObject("event").getString("class").equals("card") && jsonObject.getJSONObject("event").getInt("door_idx") == doorIdx;
            }
        }, initDoor(id,doorIdx, doorName));

    }
}
