package appsgate.lig.proxy.PhilipsHUE.dao;


import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;

public class LightWrapper {

    PHLight light;
    PHBridge bridge;

    public LightWrapper(PHLight light,PHBridge bridge){
        this.light=light;
        this.bridge=bridge;
    }

    public PHLight getLight() {
        return light;
    }

    public void setLight(PHLight light) {
        this.light = light;
    }

    public PHBridge getBridge() {
        return bridge;
    }

    public void setBridge(PHBridge bridge) {
        this.bridge = bridge;
    }
}
