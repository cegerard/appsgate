package appsgate.lig.yahoo.impl;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import appsgate.lig.yahoo.geoplanet.YahooGeoPlanet;
import appsgate.lig.yahoo.weather.YahooWeather;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;
import appsgate.lig.weather.exception.WeatherForecastException;

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
public class YahooWeatherImpl implements YahooWeather {

	private static Logger logger = LoggerFactory
			.getLogger(YahooWeatherImpl.class);

	URL url;

	protected String feedUrl;
	protected char currentUnit;

	private Object lock = new Object();

	/**
	 * In order to avoid massive pooling of the webService, two request must be
	 * separated by this minimal interval (5 minutes)
	 */
	private long intervalBetweenRefresh = 1000 * 60 * 5;
	private long timeStamp = 0;

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

	boolean noFetch;
	private YahooGeoPlanet geoPlanet;

	// Set of map defining combination between a WOEID (Where on Earth
	// IDentifier)
	// and some information about this location WOEID always comes as key and
	// the information as value
	private Map<String, Calendar> lastPublicationDates;
	private Map<String, String> presentationURLs;
	private Map<String, CurrentWeather> currentWeathers;
	private Map<String, List<DayForecast>> forecasts;

	public YahooWeatherImpl() {

		woeidFromePlaceName = new HashMap<String, String>();
		lastPublicationDates = new HashMap<String, Calendar>();
		presentationURLs = new HashMap<String, String>();
		currentWeathers = new HashMap<String, CurrentWeather>();
		forecasts = new HashMap<String, List<DayForecast>>();

		lastFetchDate = null;
		currentUnit = 'c';
		noFetch = true;

		geoPlanet = new YahooGeoPlanetImpl();
		timeStamp = System.currentTimeMillis();
		logger.debug("YahooWeatherImpl() is OK");
	}

	public Calendar getLastFetchDate() {
		return lastFetchDate;
	}

	@Override
	public String getWOEID(String placeName) throws WeatherForecastException {
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
		fetchAtLocation(placeName);

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
		fetchAtLocation(placeName);

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

		synchronized (lock) {
			String okPlaceName = placeName.replace(" ", "%20");
			logger.info("addLocation(String placeName: " + placeName + " )");

			String newWOEID = geoPlanet.getWOEIDFromPlaceName(okPlaceName);
			if (newWOEID != null && newWOEID.length() > 0) {
				woeidFromePlaceName.put(placeName, newWOEID);
				lastPublicationDates.put(newWOEID, null);
				presentationURLs.put(newWOEID, null);

				currentWeathers.put(newWOEID, null);
				forecasts.put(newWOEID, null);
				// Special case of fetch that override the timeStamp (because a
				// new location must be polled one time)
				fetch(placeName);
			} else
				throw new WeatherForecastException("Place was not found");
		}
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
			presentationURLs.remove(woeid);

			currentWeathers.remove(woeid);
			forecasts.remove(woeid);

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

	}

	public void stop() {
		logger.info("WeatherForecast stopped:"
				+ this.getClass().getSimpleName());
	}

	public Unit getUnit() {
		switch (currentUnit) {
		case 'c':
			return Unit.EU;
		case 'f':
			return Unit.US;
		default:
			currentUnit = 'c';
			return YahooWeather.Unit.EU;
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
		if (System.currentTimeMillis() > (timeStamp + intervalBetweenRefresh)) {
			timeStamp = System.currentTimeMillis();

			for (String placeName : woeidFromePlaceName.keySet()) {
				fetch(placeName);
			}

			lastFetchDate = Calendar.getInstance();
		}

	}

	/**
	 * This method is private because only the YahooWeatherImpl service can
	 * choose to send a request to the web service (to avoid pooling)
	 * 
	 * @param placeName
	 * @throws WeatherForecastException
	 */
	private void fetch(String placeName) throws WeatherForecastException {
		try {
			synchronized (lock) {
				String woeid = woeidFromePlaceName.get(placeName);
				logger.trace("Fetching Weather for " + placeName + ", WOEID : "
						+ woeid);

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
					presentationURLs.put(woeid,
							YahooWeatherParser.parsePresentationURL(doc));
				} else
					logger.info("Publication date for " + placeName
							+ " is NOT newer, does nothing");
			}
		} catch (Exception e) {
			throw new WeatherForecastException(
					"Impossible to fetch to weather forecast service, "
							+ e.getMessage());
		}
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

	private void fetchAtLocation(String placeName)
			throws WeatherForecastException {
		if (!woeidFromePlaceName.containsKey(placeName))
			addLocation(placeName);
	}

	@Override
	public JSONObject checkLocation(String location) {
		String okLocation = location.replace(" ", "%20");

		return geoPlanet.getDescriptionFromPlaceName(okLocation);
	}

	@Override
	public JSONArray checkLocationsStartingWith(String firstLetters) {
		String okLocation = firstLetters.replace(" ", "%20");

		return geoPlanet.getLocationsStartingWith(okLocation);

	}

	@Override
	public String getPresentationURL(String placeName)
			throws WeatherForecastException {
		return presentationURLs.get(getWOEID(placeName));
	}

	@Override
	public String addWOEID(String woeid) throws WeatherForecastException {
		logger.trace("addWOEID(String woeid : " + woeid + ")");
		if (woeidFromePlaceName.containsValue(woeid))
			throw new WeatherForecastException(
					"Already monitoring this location");

		synchronized (lock) {
			JSONObject obj = geoPlanet.getDescriptionFromWOEID(woeid);
			if (obj == null)
				throw new WeatherForecastException(
						"Cannot retrieve the location from woeid");
			String placeName = obj.optString("name");
			if (placeName == null)
				throw new WeatherForecastException(
						"no place name associated with woeid");

			addLocation(placeName);
			return placeName;
		}
		
	}

	@Override
	public String getPlaceName(String woeid) throws WeatherForecastException {
		JSONObject obj = geoPlanet.getDescriptionFromWOEID(woeid);
		if(obj!=null && obj.has("name")) {
			return obj.getString("name");
		}
		return null;	
	}

}
