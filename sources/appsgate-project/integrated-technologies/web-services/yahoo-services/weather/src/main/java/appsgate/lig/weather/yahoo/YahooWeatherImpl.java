package appsgate.lig.weather.yahoo;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;
import appsgate.lig.weather.utils.SimplifiedWeatherCodesHelper;
import appsgate.lig.weather.utils.WeatherCodesHelper;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;
import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.messages.WeatherUpdateNotificationMsg;

/**
 * Implementation of Yahoo forecast, allows to change unit (Celsius,Fahrenheit)
 * as input its required the WOEID
 * (http://developer.yahoo.com/geo/geoplanet/guide/concepts.html#hierarchy)
 * which indicates the location for the forecast. This class parses the
 * information obtained in from weather yahoo service: e.g.
 * http://weather.yahooapis.com/forecastrss?w=12724717&u=c
 * 
 * @author thibaud
 * 
 */
public class YahooWeatherImpl extends WeatherServiceCoreImpl implements CoreObjectSpec, CoreWeatherServiceSpec {

	private final Logger logger = Logger.getLogger(YahooWeatherImpl.class.getSimpleName());


	// *******
	// Overriding  appsgate properties  to allow injection:

	private String appsgatePictureId=super.appsgatePictureId;
	private String appsgateUserType=super.appsgateUserType;
	private String appsgateDeviceStatus=super.appsgateDeviceStatus;
	private String appsgateObjectId=super.appsgateObjectId;
	private String appsgateServiceName=super.appsgateServiceName;	
	

	URL url;

	boolean noFetch;
	private Timer refreshTimer = new Timer();
	private YahooGeoPlanet geoPlanet;


	// Set of map defining combination between a WOEID (Where on Earth
	// IDentifier)
	// and some information about this location WOEID always comes as key and
	// the information as value
	private Map<String, Calendar> lastPublicationDates;
	private Map<String, CurrentWeather> currentWeathers;
	private Map<String, List<DayForecast>> forecasts;
	
	int refreshRate;

	TimerTask refreshtask;

	public YahooWeatherImpl() {
		super.initAppsgateFields();

		woeidFromePlaceName = new HashMap<String, String>();
		lastPublicationDates = new HashMap<String, Calendar>();
		currentWeathers = new HashMap<String, CurrentWeather>();
		forecasts = new HashMap<String, List<DayForecast>>();

		lastFetchDate = null;
		currentUnit = 'c';
		noFetch = true;

		geoPlanet = new YahooGeoPlanetImpl();
		refreshRate=-1;
		refreshtask = new WeatherRefreshTask(this, refreshRate);
		
	}

	public Calendar getLastFetchDate() {
		return lastFetchDate;
	}

	private String getWOEID(String placeName) throws WeatherForecastException {
		if (!woeidFromePlaceName.containsKey(placeName))
			throw new WeatherForecastException(
					"Place Name is not monitored (Not spelled as previously added ?)");
		return woeidFromePlaceName.get(placeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.weather.WeatherForecast#getForecast(java.lang.String)
	 */
	@Override
	public List<DayForecast> getForecast(String placeName)
			throws WeatherForecastException {
		return forecasts.get(getWOEID(placeName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.weather.WeatherForecast#getPublicationDate(java.lang.String)
	 */
	@Override
	public Calendar getPublicationDate(String placeName)
			throws WeatherForecastException {
		return lastPublicationDates.get(getWOEID(placeName));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.weather.WeatherForecast#getCurrentWeather(java.lang.String)
	 */
	@Override
	public CurrentWeather getCurrentWeather(String placeName)
			throws WeatherForecastException {
		return currentWeathers.get(getWOEID(placeName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.weather.WeatherForecast#getLocations()
	 */
	@Override
	public String[] getLocations() {
		return woeidFromePlaceName.keySet().toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.weather.WeatherForecast#addLocation(java.lang.String)
	 */
	@Override
	public void addLocation(String placeName) throws WeatherForecastException {
		if (woeidFromePlaceName.containsKey(placeName))
			throw new WeatherForecastException(
					"Already monitoring this location");

		String okPlaceName = placeName.replace(" ", "%20");
		logger.info("addLocation(String placeName: " + okPlaceName	+ " )");

		String newWOEID = geoPlanet.getWOEIDFromPlaceName(okPlaceName);
		if (newWOEID != null) {
			woeidFromePlaceName.put(placeName, newWOEID);
			lastPublicationDates.put(newWOEID, null);
			currentWeathers.put(newWOEID, null);
			forecasts.put(newWOEID, null);
			fireWeatherUpdateMessage("location", placeName,
					WeatherUpdateNotificationMsg.EVENTTYPE_ADDLOCATION);
			fetch();
		} else
			throw new WeatherForecastException("Place was not found");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.weather.WeatherForecast#removeLocation(java.lang.String)
	 */
	@Override
	public boolean removeLocation(String placeName)
			throws WeatherForecastException {
		if (!woeidFromePlaceName.containsKey(placeName))
			throw new WeatherForecastException("Not monitoring this location");
		else {
			String woeid = woeidFromePlaceName.remove(placeName);
			if (woeid == null)
				return false;
			lastPublicationDates.remove(woeid);
			currentWeathers.remove(woeid);
			forecasts.remove(woeid);
			fireWeatherUpdateMessage("location", placeName,
					WeatherUpdateNotificationMsg.EVENTTYPE_REMOVELOCATION);
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.weather.WeatherForecast#containLocation(java.lang.String)
	 */
	@Override
	public boolean containLocation(String placeName)
			throws WeatherForecastException {
		return woeidFromePlaceName.containsKey(placeName);
	}

	public void start() {
		initAppsgateFields();

		try {
			fetch();
		} catch (WeatherForecastException exc) {
			exc.printStackTrace();
		}

		/**
		 * Configure auto-refresh meteo data
		 */
		if (refreshRate > 0) {
			logger.fine("Configuring auto-refresh for :" + refreshRate);
			refreshTimer.scheduleAtFixedRate(refreshtask, 0, refreshRate);
		}

	}

	public void stop() {
		logger.info("WeatherForecast stopped:"
				+ this.getClass().getSimpleName());
		refreshtask.cancel();
	}

	public Unit getUnit() {
		switch (currentUnit) {
		case 'c':
			return Unit.EU;
		case 'f':
			return Unit.US;
		default:
			currentUnit = 'c';
			return CoreWeatherServiceSpec.Unit.EU;
		}
	}

	@Override
	public String toString() {

		if (this.getLastFetchDate() == null) {
			return "No info retrieved, wait for the next fetch";
		}
		StringBuffer sb = new StringBuffer();
		for (String placeName : woeidFromePlaceName.keySet()) {
			sb.append(String.format("location %s \n", placeName));

			try {
				if (getCurrentWeather(placeName) != null)
					sb.append(this.getCurrentWeather(placeName));

				if (getPublicationDate(placeName) != null)
					sb.append(String
							.format("meteo report from %1$te/%1$tm/%1$tY %1$tH:%1$tM \n",
									this.getPublicationDate(placeName)));
				sb.append("-- forecasts --\n");
				if (getForecast(placeName) != null)
					for (DayForecast forecast : getForecast(placeName)) {
						sb.append(String
								.format("Date: %1$te/%1$tm/%1$tY (min:%2$s,max:%3$s, code:%4$s) \n",
										forecast.getDate(), forecast.getMin(),
										forecast.getMax(), forecast.getCode()));
					}
			} catch (WeatherForecastException e) {
				e.printStackTrace();
			}

			sb.append("--- /forecasts ---\n");
		}
		sb.append(String
				.format("osgi meteo polled report at %1$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS \n",
						this.getLastFetchDate().getTime()));

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.weather.WeatherForecast#fetch()
	 */
	public void fetch() throws WeatherForecastException {
		try {

			for (String placeName : woeidFromePlaceName.keySet()) {
				String woeid = woeidFromePlaceName.get(placeName);
				logger.fine("Fetching Weather for " + placeName);

				feedUrl = String.format(feedUrlTemplate, woeid, currentUnit);

				this.url = new URL(feedUrl);

				DocumentBuilder db = null;
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse(url.openStream());
				Calendar newPubDate = YahooWeatherParser
						.parsePublicationDate(doc);

				if (newPubDate != null
						&& (newPubDate.after(lastPublicationDates.get(woeid)) || lastPublicationDates
								.get(woeid) == null)) {
					logger.info("Publication date for " + placeName
							+ " is newer, parsing new Weather Data !");
					currentWeathers.put(woeid,
							YahooWeatherParser.parseCurrentConditions(doc));
					forecasts.put(woeid, YahooWeatherParser.parseForecast(doc));
					lastPublicationDates.put(woeid, newPubDate);
					fireWeatherUpdateMessage(
							"location",
							placeName,
							WeatherUpdateNotificationMsg.EVENTTYPE_FETCHLOCATION);

				} else
					logger.info("Publication date for " + placeName
							+ " is NOT newer, does nothing");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new WeatherForecastException(
					"Impossible to fetch to weather forecast service, "
							+ e.getMessage());
		}

		lastFetchDate = Calendar.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.weather.WeatherForecast#setUnit(appsgate.lig.weather.
	 * WeatherForecast.Unit)
	 */
	@Override
	public void setUnit(Unit unit) {
		switch (unit) {
		case EU:
			currentUnit = 'c';
		case US:
			currentUnit = 'f';
			break;
		}
		fireWeatherUpdateMessage("unit", String.valueOf(currentUnit),
				WeatherUpdateNotificationMsg.EVENTTYPE_SETUNIT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.weather.spec.CoreWeatherServiceSpec#fireWeatherUpdateMessage
	 * (java.lang.String)
	 */
	@Override
	public NotificationMsg fireWeatherUpdateMessage(String property,
			String value, String eventType) {
		// TODO this one is very basic
		return new WeatherUpdateNotificationMsg(this, property, value,
				eventType);
	}


	@Override
	public JSONArray getCurrentWeather() throws JSONException {
		Set<String> keys = currentWeathers.keySet();
		JSONArray weather = new JSONArray();
		for (String key : keys) {
			CurrentWeather cw = currentWeathers.get(key);

			JSONObject obj = new JSONObject();
			obj.put("location", getNameFromWOEID(key));
			obj.put("temperature", String.valueOf(cw.getTemperature()));
			obj.put("trend",
					WeatherCodesHelper.getDescription(cw.getWeatherCode()));

			weather.put(obj);
		}
		return weather;
	}

	@Override
	public JSONArray getForecast() throws JSONException {
		Set<String> keys = forecasts.keySet();
		JSONArray allForecasts = new JSONArray();

		for (String key : keys) {

			List<DayForecast> dfc = forecasts.get(key);
			JSONObject place = new JSONObject();
			JSONArray locDFC = new JSONArray();

			for (DayForecast d : dfc) {
				JSONObject obj = new JSONObject();
				obj.put("date", d.getDate().toString());
				obj.put("trend", WeatherCodesHelper.getDescription(d.getCode()));
				obj.put("max", d.getMax());
				obj.put("min", d.getMin());
				locDFC.put(obj);
			}

			place.put("location", getNameFromWOEID(key));
			place.put("forecast", locDFC);
			allForecasts.put(place);
		}

		return allForecasts;
	}

	private String getNameFromWOEID(String WOEID) {
		Set<String> nameList = woeidFromePlaceName.keySet();
		String nameLoc = "";
		for (String name : nameList) {
			String woeid = woeidFromePlaceName.get(name);
			if (WOEID.contentEquals(woeid)) {
				nameLoc = name;
				break;
			}
		}

		return nameLoc;
	}
    
    private void fetchAtLocation(String placeName) throws WeatherForecastException {
		if (!woeidFromePlaceName.containsKey(placeName))
			addLocation(placeName);
		fetch();
    }
    
    private DayForecast getDayForecast(String placeName, int dayForecast) throws WeatherForecastException {
		fetchAtLocation(placeName);

		List<DayForecast> frcsts = forecasts.get(getWOEID(placeName));
		try {
		return frcsts.get(dayForecast);
		} catch(IndexOutOfBoundsException exc) {
			throw new WeatherForecastException("dayForecast "+dayForecast+" wrong, "+exc.getMessage());
		}
    }
    

	@Override
	public int getWeatherCodeForecast(String placeName, int dayForecast)
			throws WeatherForecastException {
		if(dayForecast == 0) {
			fetchAtLocation(placeName);
			CurrentWeather weather = currentWeathers.get(getWOEID(placeName));
			return weather.getWeatherCode();
		} else {
			return getDayForecast(placeName,dayForecast).getCode();
		}
	}

	@Override
	public int getMaxTemperatureForecast(String placeName, int dayForecast)
			throws WeatherForecastException {
		return getDayForecast(placeName,dayForecast).getMax();
	}

	@Override
	public int getMinTemperatureForecast(String placeName, int dayForecast)
			throws WeatherForecastException {
		return getDayForecast(placeName,dayForecast).getMin();
	}
	
	@Override
	public int getAverageTemperatureForecast(String placeName, int dayForecast)
			throws WeatherForecastException {
		if(dayForecast == 0) {
			fetchAtLocation(placeName);
			CurrentWeather weather = currentWeathers.get(getWOEID(placeName));
			return weather.getTemperature();
		} else {
			DayForecast frct = getDayForecast(placeName,dayForecast);
			return (frct.getMin()+frct.getMax())/2;
		}
	}

	@Override
	public boolean isWeatherSimplifiedCodeForecast(String placeName, int dayForecast, int simpleWeatherCode)
			throws WeatherForecastException {
		int currentYahooForecast = getWeatherCodeForecast(placeName, dayForecast);
		
		return SimplifiedWeatherCodesHelper.contains(simpleWeatherCode, currentYahooForecast);
	}


}
