package appsgate.lig.proxy.PhilipsHUE.importer;


import java.util.HashMap;
import java.util.Map;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhilipsHueFactoryExecutor implements Runnable {

    public static String ApAMIMPL = "PhilipsHUEImpl";
    final ImportDeclaration importDeclaration;
    private final PHBridge bridge;
    private final PHBridgeConfiguration bc;
    private final PHLight light;
    private final PhilipsHueImporter importer;
    private String deviceID;
    private Logger LOG= LoggerFactory.getLogger(PhilipsHueFactoryExecutor.class);

    public PhilipsHueFactoryExecutor(PHBridge bridge, PHLight light, PhilipsHueImporter importer, ImportDeclaration importDeclaration){
        this.bridge=bridge;
        this.light=light;
        this.importer=importer;
        this.importDeclaration=importDeclaration;
        bc  = bridge.getResourceCache().getBridgeConfiguration();
        deviceID = bc.getMacAddress()+"-"+light.getIdentifier();
    }

    @Override
    public void run()  {

        try {

            Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
            Map<String, String> properties = new HashMap<String, String>();

            properties.put("instance.name", deviceID);
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
                    LOG.error("Failed to instantiate apam implementation {} (lamp proxy for appsgate)",e);
                }
            }else {
                System.out.println("No "+ApAMIMPL+" found !");
            }

            importer.handleImportDeclaration(importDeclaration);

        } catch (Exception e) {
            LOG.error("Failed to create appsgate instance for the light.",e);
        }
    }

    public static void initiateLightStateProperties(Map<String, String> properties, PHLightState lightState) {
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

    public String getDeviceID() {
        return deviceID;
    }
}
