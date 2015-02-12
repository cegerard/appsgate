package appsgate.ard.protocol.fuchsia;

import appsgate.ard.protocol.controller.ARDController;
import org.apache.felix.ipojo.*;
import org.json.JSONException;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This thread is invoked by the importer in order not to block the main thread of ipojo
 */
public class ARDSideTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ARDController.ARD_LOGNAME);

    ARDBrigdeImporter importer;

    ImportDeclaration importDeclaration;

    private final Map<String,InstanceManager> controllers;

    private final Map<ARDController,Set<String>> declarationController;

    private Factory factory;


    ARDSideTask(ARDBrigdeImporter importer,final Map<String,InstanceManager> controllers,Map<ARDController,Set<String>> declarationController, ImportDeclaration importDeclaration,Factory factory){
        this.importer=importer;
        this.importDeclaration=importDeclaration;
        this.controllers=controllers;
        this.declarationController=declarationController;
        this.factory=factory;
    }

    private ARDController initController(String ip,Integer port){

        InstanceManager controller=controllers.get(ip);
        ARDController ardController=null;
        if(controller==null){

            logger.info("Initializing ARD Controller on IP {} port {}",ip,port);

            try {
                Dictionary<String,String> properties= new Hashtable();
                properties.put("host",ip);
                properties.put("port",port.toString());
                controller = (InstanceManager) factory.createComponentInstance(properties);
                controllers.put(ip,controller);
                declarationController.put((ARDController)controller.getPojoObject(),new HashSet<String>());
                ardController=(ARDController)controller.getPojoObject();
                ardController.validate(importer,importDeclaration,declarationController);
            } catch (UnacceptableConfiguration unacceptableConfiguration) {
                unacceptableConfiguration.printStackTrace();
            } catch (MissingHandlerException e) {
                e.printStackTrace();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else {
            ardController= (ARDController) controller.getPojoObject();
            logger.info("Reusing existing socket for ARD Controller on IP {} port {}",ip,port);
        }


        return ardController;

    }

    @Override
    public void run() {
        String ip=importDeclaration.getMetadata().get("ard.bridge.ip").toString();
        Integer port=Integer.parseInt(importDeclaration.getMetadata().get("ard.bridge.port").toString());


        initController(ip,port);



    }
}
