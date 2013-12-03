

package appsgate.lig.upnp.media.browser.adapter;


import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.browser.MediaBrowser;
import appsgate.lig.upnp.media.proxy.MediaServerProxyImpl;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.apache.felix.upnp.devicegen.holder.LongHolder;
import org.apache.felix.upnp.devicegen.holder.StringHolder;

import org.json.JSONException;
import org.json.JSONObject;

import fr.imag.adele.apam.Instance;

public class MediaBrowserAdapter implements MediaBrowser, CoreObjectSpec {	

	private MediaServerProxyImpl mediaServer;

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


	@SuppressWarnings("unused")
	private void initialize(Instance instance) {
		deviceId 	= instance.getProperty(UPnPDevice.ID);

		appsgatePictureId = null;
		appsgateServiceName = "Appsgate UPnP Media browser";
		appsgateUserType = "36";
		appsgateStatus = "2";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getAbstractObjectId()
	 */
	@Override
	public String getAbstractObjectId() {
		return "browser:"+deviceId;
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
	public String browse(String objectID, String browseFlag, String filter,
			long startingIndex, long requestedCount, String sortCriteria) {
		try {
			StringHolder result = new StringHolder();
			LongHolder number = new LongHolder();
			LongHolder totalMatches = new LongHolder();
			LongHolder updateId = new LongHolder();
			
			mediaServer.getContentDirectory().browse(objectID,browseFlag,filter,startingIndex,requestedCount,sortCriteria,
					result,number,totalMatches,updateId);
			
			return result.getObject();
			
		} catch (UPnPException ignored) {
			ignored.printStackTrace(System.err);
			return "";
		}
	}


}
