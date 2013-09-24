

package appsgate.lig.upnp.media.player.adapter;

import java.net.URI;

import org.apache.felix.upnp.devicegen.holder.IntegerHolder;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;

import fr.imag.adele.apam.Instance;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.player.MediaPlayer;
import appsgate.lig.upnp.media.proxy.MediaRendererProxyImpl;

public class MediaPlayerAdapter implements MediaPlayer,CoreObjectSpec {	

	private MediaRendererProxyImpl mediaRenderer;

	private String 		deviceId;

	private String 		userObjectName;
	private int 		locationId;
	private String 		pictureId;

	
	private String		currentMedia;
	
	@SuppressWarnings("unused")
	private void initialize(Instance instance) {
		deviceId 	= instance.getProperty(UPnPDevice.ID);
		mediaRenderer.getAVTransport();
		mediaRenderer.getConnectionManager();
		mediaRenderer.getRenderingControl();
	}

	@Override
	public String getAbstractObjectId() {
		return mediaRenderer.getAbstractObjectId()+"-Player";
	}

	@Override
	public String getUserType() {
		return null;
	}

	@Override
	public int getObjectStatus() {
		return 0;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
	}

	@Override
	public String getPictureId() {
		return pictureId;
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
				currentMedia = null;
			} catch (UPnPException ignored) {
				ignored.printStackTrace(System.err);
			}
		}
	}


}
