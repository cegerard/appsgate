package appsgate.lig.proxy.PhilipsHUE.importer;

import com.philips.lighting.hue.sdk.bridge.impl.PHBridgeImpl;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.philipshue.util.PhilipsHueImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component
@Provides(specifications = {ImporterService.class, ImporterIntrospection.class})
public class PhilipsHueImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueImporter.class);

    private final BundleContext context;

    private ServiceReference serviceReference;

    private Map<String,ServiceRegistration> lamps=new HashMap<String, ServiceRegistration>();

    @ServiceProperty(name = "target", value = "(discovery.philips.device.name=*)")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    public PhilipsHueImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void validate() {
        LOG.info("Appsgate philips hue importer is up and running");
    }

    @Invalidate
    public void invalidate() {

        LOG.info("Cleaning up instances into Appsgate philips hue importer ");

        cleanup();

    }

    private void cleanup(){

        for(Map.Entry<String,ServiceRegistration> lampEntry:lamps.entrySet()){
            lamps.remove(lampEntry.getKey()).unregister();
        }

        for(ImportDeclaration id:super.getImportDeclarations()){
            super.unhandleImportDeclaration(id);
        }

    }

    @Requires(specification = PHBridgeImpl.class,optional = true)
    List<PHBridgeImpl> bridges;

    @Override
    protected void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("Appsgate philips hue importer was invoked");

        PhilipsHueImportDeclarationWrapper pojo= PhilipsHueImportDeclarationWrapper.create(importDeclaration);

        try {

            FuchsiaUtils.loadClass(context, pojo.getType());

            Dictionary<String, Object> props = new Hashtable<String, Object>();

            ServiceRegistration lampService=context.registerService(pojo.getType(),pojo.getObject(),props);

            lamps.put(pojo.getId(),lampService);

            try {
                instanciateHUELight(bridges.get(0), (PHLight) pojo.getObject());
                super.handleImportDeclaration(importDeclaration);
            } catch (Exception e) {
                LOG.error("Failed to create appsgate instance for the light.",e);
            }

        } catch (ClassNotFoundException e) {
            LOG.error("Failed to load type {}, importing process aborted.", pojo.getType(), e);
        }


    }

    public static String ApAMIMPL = "PhilipsHUEImpl";

    public void instanciateHUELight(PHBridge bridge, PHLight light) throws Exception {
        PHBridgeConfiguration bc  = bridge.getResourceCache().getBridgeConfiguration();
        String deviceID = bc.getMacAddress()+"-"+light.getIdentifier();
        Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
        Map<String, String> properties = new HashMap<String, String>();

        properties.put("deviceName", 	light.getName());
        properties.put("deviceId", 		deviceID);
        properties.put("lightBridgeId", light.getIdentifier());
        properties.put("lightBridgeIP", bc.getIpAddress());
        properties.put("reachable", Boolean.toString(light.isReachable()));
        initiateLightStateProperties(properties, light.getLastKnownLightState());

        if(impl != null) {
            try {
                Instance inst = impl.createInstance(null, properties);
            }catch(Exception e){
                LOG.error("Failed to instantiate apam implementation {} (lamp proxy for appsgate)",ApAMIMPL,e);
                throw new Exception(e);
            }
        }else {
            System.out.println("No "+ApAMIMPL+" found !");
        }
    }

    public void initiateLightStateProperties(Map<String, String> properties, PHLightState lightState) {
        properties.put("state", String.valueOf(lightState.isOn()));
        properties.put("hue", String.valueOf(lightState.getHue()));
        properties.put("sat", String.valueOf(lightState.getSaturation()));
        properties.put("bri", String.valueOf(lightState.getBrightness()));
        properties.put("x", String.valueOf(lightState.getX()));
        properties.put("y", String.valueOf(lightState.getY()));
        properties.put("ct", String.valueOf(lightState.getCt()));
        properties.put("speed", String.valueOf(lightState.getTransitionTime()));

        if(lightState.getAlertMode().name().contentEquals(PHLight.PHLightAlertMode.ALERT_SELECT.name())) {
            properties.put("alert", "select");
        }else if(lightState.getAlertMode().name().contentEquals(PHLight.PHLightAlertMode.ALERT_LSELECT.name())){
            properties.put("alert", "lselect");
        }else if(lightState.getAlertMode().name().contentEquals(PHLight.PHLightAlertMode.ALERT_NONE.name())){
            properties.put("alert", "none");
        }else if(lightState.getAlertMode().name().contentEquals(PHLight.PHLightAlertMode.ALERT_UNKNOWN.name())){
            properties.put("alert", "unknown");
        }else {
            System.out.println("Error when initiating the HUE light alert value!");
        }

        if(lightState.getEffectMode().name().contentEquals(PHLight.PHLightEffectMode.EFFECT_COLORLOOP.name())) {
            properties.put("effect",  "colorloop");
        }else if(lightState.getEffectMode().name().contentEquals(PHLight.PHLightEffectMode.EFFECT_NONE.name())){
            properties.put("effect",  "none");
        }else if(lightState.getEffectMode().name().contentEquals(PHLight.PHLightEffectMode.EFFECT_UNKNOWN.name())){
            properties.put("effect",  "unknown");
        }else {
            System.out.println("Error when initiating the HUE light alert value!");
        }

        if(lightState.getColorMode().name().contentEquals(PHLight.PHLightColorMode.COLORMODE_HUE_SATURATION.name())) {
            properties.put("mode",  "hs");
        }else if(lightState.getColorMode().name().contentEquals(PHLight.PHLightColorMode.COLORMODE_CT.name())){
            properties.put("mode",  "ct");
        }else if(lightState.getColorMode().name().contentEquals(PHLight.PHLightColorMode.COLORMODE_XY.name())){
            properties.put("mode",  "xy");
        }else if(lightState.getColorMode().name().contentEquals(PHLight.PHLightColorMode.COLORMODE_NONE.name())){
            properties.put("mode",  "none");
        }else if(lightState.getColorMode().name().contentEquals(PHLight.PHLightColorMode.COLORMODE_UNKNOWN.name())){
            properties.put("mode",  "unknown");
        }else {
            System.out.println("Error when initiating the HUE light alert value!");
        }
    }

    @Override
    protected void denyImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        PhilipsHueImportDeclarationWrapper pojo= PhilipsHueImportDeclarationWrapper.create(importDeclaration);

        try {
            lamps.remove(pojo.getId()).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering lamp", e);
        }

        unhandleImportDeclaration(importDeclaration);

    }


    public String getName() {
        return name;
    }
}