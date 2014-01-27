package appsgate.ard;

import appsgate.ard.base.callback.DoorMonitorExceptionHandler;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;

import java.util.*;

import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

@Component(name = "ARDSwitchImporterFactory")
@Provides
public class ARDSwitchImporter extends AbstractImporterComponent implements DoorMonitorExceptionHandler {

    @ServiceProperty(name = "target")
    private String filter;

    @Requires(filter = "(factory.name=SwitchMonitorFactory)")
    Factory switchFactory;

    @Requires(filter = "(factory.name="+FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME+")")
    Factory linkerFactory;

    private Map<ImportDeclaration,InstanceManager> monitors=new HashMap<ImportDeclaration,InstanceManager>();

    public List<String> getConfigPrefix() {
        return null;
    }

    public String getName() {
        return "ard-switch-importer";
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        String id=importDeclaration.getMetadata().get("id").toString();
        String address=importDeclaration.getMetadata().get("ard.switch.ip").toString();
        String port=importDeclaration.getMetadata().get("ard.switch.port").toString();

        Dictionary<String,Object> switchProperty=new Hashtable<String,Object>() ;

        switchProperty.put("instance.name", String.format("%s-door-importer", id).toString());
        switchProperty.put("ard.switch.ip", address);
        switchProperty.put("target", String.format("(&(ard.door.id=*)(ard.door.switch.ip=%s))", address));
        switchProperty.put("port", port);
        switchProperty.put("ard.switch.authorized_cards",importDeclaration.getMetadata().get("ard.switch.authorized_cards"));

        try {

            Dictionary<String,Object> linkerProperty=new Hashtable<String,Object>() ;

            linkerProperty.put(FILTER_IMPORTDECLARATION_PROPERTY,String.format("(&(ard.door.id=*)(ard.door.switch.ip=%s))", address));
            linkerProperty.put(FILTER_IMPORTERSERVICE_PROPERTY,String.format("(ard.switch.ip=%s)", address));

            linkerFactory.createComponentInstance(linkerProperty);

            InstanceManager im=(InstanceManager)switchFactory.createComponentInstance(switchProperty);

            ARDSwitchMonitor monitor=(ARDSwitchMonitor)im.getPojoObject();

            Thread thread=new Thread(monitor);
            thread.start();

            monitors.put(importDeclaration,im);

        } catch (Exception e) {
            throw new ImporterException(e);
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        InstanceManager im=monitors.get(importDeclaration);
        ARDSwitchMonitor lm=(ARDSwitchMonitor)im.getPojoObject();
        lm.stopMonitor();
        im.dispose();

    }

    public void handleException(Throwable exception, ARDSwitchMonitor monitor) {
        System.out.println("failed loading switch");
    }

    public boolean getCardState() {
        return false;
    }

    public int getLastCardNumber() {
        return 0;
    }


    //core object spec methods

    public String getAbstractObjectId() {
        return "door-sensor-reader";
    }

    public String getUserType() {
        return "5";  //4 is the type dedicated to the SensorReader
    }

    public int getObjectStatus() {
        return 0;
    }

    public String getPictureId() {
        return null;
    }
        /*
    public JSONObject getDescription() throws JSONException {

        JSONObject descr = new JSONObject();
        descr.put("id", "ard-door-switch01");
        descr.put("type", "5"); //4 for keyCard sensor
        descr.put("status", "0");
        descr.put("inserted", "true");

        return descr;
    }

    public void setPictureId(String pictureId) {
        // Ignore
    }       */
}
