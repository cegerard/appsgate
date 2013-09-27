

package appsgate.lig.upnp.media.player.adapter;

import appsgate.lig.upnp.media.player.MediaPlayer;
import appsgate.lig.upnp.media.proxy.MediaRendererProxyImpl;

import appsgate.lig.core.object.spec.CoreObjectSpec;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.apache.felix.upnp.devicegen.holder.IntegerHolder;

import org.json.JSONException;
import org.json.JSONObject;

import fr.imag.adele.apam.Instance;

public class MediaPlayerAdapter implements MediaPlayer, CoreObjectSpec {	

	private MediaRendererProxyImpl mediaRenderer;

	/**
	 * The associated UPnP device
	 */
	private String 		deviceId;
	
	/**
	 * Core Object Spec properties
	 */
	private String appsgatePictureId;
	private String appsgateUserType;
	private String appsgateStatus;
	private String appsgateServiceName;

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
		descr.put("id", deviceId);
		descr.put("type", appsgateUserType);
		descr.put("status", appsgateStatus);
		descr.put("sysName", appsgateServiceName);
		
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
			mediaRenderer.getAVTransport().setAVTransportURI(0,media,"");
			mediaRenderer.getAVTransport().play(0,"1");
			currentMedia = media;
		} catch (UPnPException ignored) {
			ignored.printStackTrace(System.err);
		}
	}

	@Override
	public void play() {
		if (currentMedia != null){
			try {
				mediaRenderer.getAVTransport().play(0,"1");
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
		}
	}

	@Override
	public void pause() {
		if (currentMedia != null) {
			try {
				mediaRenderer.getAVTransport().pause(0);
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
		}
	}

	@Override
	public void stop() {
		if (currentMedia != null) {
			try {
				mediaRenderer.getAVTransport().stop(0);
				currentMedia = null;
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
		}
	}

	@Override
	public int getVolume() {
		
		if (currentMedia != null) {
			try {
				IntegerHolder result = new IntegerHolder();
				mediaRenderer.getRenderingControl().getVolume(0,"Master",result);
				return result.getValue();
				
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
		}	
		
		return 0;

	}

	@Override
	public void setVolume(int level) {
		if (currentMedia != null) {
			try {
				mediaRenderer.getRenderingControl().setVolume(0, "Master", level);
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
		}
	}


}
