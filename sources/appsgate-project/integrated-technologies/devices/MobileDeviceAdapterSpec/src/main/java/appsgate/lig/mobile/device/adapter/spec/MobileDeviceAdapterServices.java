package appsgate.lig.mobile.device.adapter.spec;

import java.util.List;

public interface MobileDeviceAdapterServices {
    
    /**
     *
     * @return
     */
    public List<String> getDevicesId();
    
    /**
     *
     * @param title
     * @param msg
     * @return
     */
    public Boolean sendMessage(String title, String msg);

}
