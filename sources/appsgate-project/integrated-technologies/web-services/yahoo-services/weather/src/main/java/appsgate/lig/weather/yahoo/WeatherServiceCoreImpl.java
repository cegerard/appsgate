package appsgate.lig.weather.yahoo;

import java.util.Calendar;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.core.object.spec.CoreObjectSpec.CORE_TYPE;

/**
 * This one implements all the appsgate CoreObject behavior
 * @author thibaud
 *
 */
public class WeatherServiceCoreImpl implements CoreObjectSpec {

	// *******
	// Specific fields for appsgate properties :

	protected String appsgatePictureId;
	protected String appsgateUserType;
	protected String appsgateDeviceStatus;
	protected String appsgateObjectId;
	protected String appsgateServiceName;
	
	protected String feedUrl;
	protected char currentUnit;
	
	
	/*
	 * For each human place Name, as the user entered it (the key), associate a
	 * WOEID (the value)
	 */
	protected Map<String, String> woeidFromePlaceName;
	
	protected Calendar lastFetchDate;
	
	
	/**
	 * Feed URL that returns an XML with the forecast (e.g.
	 * http://weather.yahooapis.com/forecastrss?w=12724717&u=c)
	 */
	protected String feedUrlTemplate = "http://weather.yahooapis.com/forecastrss?w=%s&u=%c";
	

	protected void initAppsgateFields() {
		appsgatePictureId = null;
		appsgateUserType = "103"; // 103 stands for weather forecast service
		appsgateDeviceStatus = "2"; // 2 means device paired (for a device, not
									// relevant for service)
		appsgateObjectId = appsgateUserType
				+ String.valueOf(feedUrlTemplate.hashCode()); // Object id
																// prefixed by
																// the user type
		appsgateServiceName = "Yahoo Weather Forecast";
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
		descr.put("id", appsgateObjectId);
		descr.put("type", appsgateUserType); // 20 for weather service
		descr.put("status", appsgateDeviceStatus); // always 2 (because this is
													// not a service)
		descr.put("pictureId", appsgatePictureId);
		descr.put("name", appsgateServiceName);

		// specific weather service properties
		descr.put("unit", currentUnit);
		if (lastFetchDate != null)
			descr.put("lastFetchDate", String.format(
					"$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS", lastFetchDate.getTime()));
		else
			descr.put("lastFetchDate", "null");

		descr.put("locations", JSONObject.wrap(woeidFromePlaceName.keySet()));

		return descr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getAbstractObjectId()
	 */
	@Override
	public String getAbstractObjectId() {
		return appsgateObjectId;
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
	 * @see
	 * appsgate.lig.core.object.spec.CoreObjectSpec#setPictureId(java.lang.String
	 * )
	 */
	@Override
	public void setPictureId(String pictureId) {
		this.appsgatePictureId = pictureId;
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
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getObjectStatus()
	 */
	@Override
	public int getObjectStatus() {
		return Integer.parseInt(appsgateDeviceStatus);
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}

	@Override
	public JSONObject getBehaviorDescription() {
		// TODO Auto-generated method stub
		return null;
	}	

}
