package appsgate.lig.upnp.adapter.event;

import java.util.Dictionary;

/**
 * Helper class to be sent by ApAM Message
 * (@see org.osgi.service.upnp.UPnPEventListener)
 * Created by thibaud on 18/07/2014.
 */
public class UPnPEvent {

    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public Dictionary getEvents() {
        return events;
    }

    private String serviceId;
    private Dictionary events;

    public UPnPEvent(String deviceId, String serviceId,	@SuppressWarnings("rawtypes") Dictionary events) {
        this.deviceId=deviceId;
        this.serviceId=serviceId;
        this.events=events;
    }
}
