

package appsgate.lig.upnp.media.player.adapter;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.upnp.adapter.event.UPnPEvent;
import appsgate.lig.upnp.media.AVTransport;
import appsgate.lig.upnp.media.ConnectionManager;
import appsgate.lig.upnp.media.ContentDirectory;
import appsgate.lig.upnp.media.RenderingControl;
import appsgate.lig.upnp.media.player.MediaPlayer;
import appsgate.lig.upnp.media.proxy.MediaRendererProxyImpl;

import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.apache.felix.upnp.devicegen.holder.IntegerHolder;

import org.json.JSONException;
import org.json.JSONObject;

import fr.imag.adele.apam.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;

public class MediaPlayerAdapter extends CoreObjectBehavior implements MediaPlayer, CoreObjectSpec {	

	/**
	 * The associated UPnP device
	 */
	private String 		deviceId;

    private String deviceName;
	
	/**
	 * Core Object Spec properties
	 */
	private String appsgatePictureId;
	private String appsgateUserType;
	private String appsgateStatus;
	private String appsgateServiceName;

    private final static Logger logger = LoggerFactory.getLogger(MediaPlayerAdapter.class);

    private AVTransport aVTransport;
    private ConnectionManager connectionManager;
    private RenderingControl renderingControl;

	/**
	 * The currently playing media
	 */
	private String		currentMedia;


	@SuppressWarnings("unused")
	private void initialize(Instance instance) {
		deviceId 	= instance.getProperty(UPnPDevice.ID);

		appsgatePictureId = null;
		appsgateServiceName = "Appsgate UPnP Media player";
		appsgateUserType = "31";
		appsgateStatus = "2";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getAbstractObjectId()
	 */
	@Override
	public String getAbstractObjectId() {
		return "player:"+deviceId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getDescription()
	 */
	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		// mandatory appsgate properties
		descr.put("id", getAbstractObjectId());
		descr.put("type", getUserType());
		descr.put("status", appsgateStatus);
		descr.put("sysName", appsgateServiceName);
		descr.put("friendlyName", deviceName);
		
		return descr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getObjectStatus()
	 */
	@Override
	public int getObjectStatus() {
		return Integer.parseInt(appsgateStatus);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getPictureId()
	 */
	@Override
	public String getPictureId() {
		return appsgatePictureId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getUserType()
	 */
	@Override
	public String getUserType() {
		return appsgateUserType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.core.object.spec.CoreObjectSpec#setPictureId(java.lang.String
	 * )
	 */
	@Override
	public void setPictureId(String pictureId) {
		this.appsgatePictureId = pictureId;
	}


	@Override
	public void play(String media) {
		try {
            aVTransport.setAVTransportURI(0,media,"");
            aVTransport.play(0,"1");
			currentMedia = media;
		} catch (UPnPException ignored) {
			ignored.printStackTrace(System.err);
		}
	}

	@Override
	public void play() {
			try {
                aVTransport.play(0,"1");
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
	}

	@Override
	public void pause() {
			try {
                aVTransport.pause(0);
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
	}

	@Override
	public void stop() {
			try {
				aVTransport.stop(0);
				currentMedia = null;
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
	}

	@Override
	public int getVolume() {
		
			try {
				IntegerHolder result = new IntegerHolder();
				renderingControl.getVolume(0,"Master",result);
				return result.getValue();
				
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
		return 0;

	}

	@Override
	public void setVolume(int level) {
			try {
                renderingControl.setVolume(0, "Master", level);
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}



    private void onUpnPEvent( UPnPEvent event) {
        logger.debug("onUpnPEvent( UPnPEvent event)" +
                ", deviceId = "+event.getDeviceId() +
                ", serviceId = "+event.getServiceId() +
                ", events = "+event.getEvents());

        if(event != null) {
            Dictionary events = event.getEvents();
            Enumeration<String> variables = events.keys();
            while( variables.hasMoreElements()) {

                String variable = variables.nextElement();
                Object value = events.get(variable);

                stateChanged(variable, "", value.toString());
            }
        } else {
            logger.debug("No events to send");
        }

    }

    private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
        return new CoreNotificationMsg(varName, oldValue, newValue, this);
    }


}
