package appsgate.lig.weather.yahoo;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import appsgate.lig.weather.DayForecast;
import appsgate.lig.weather.WeatherCodesHelper;
import appsgate.lig.weather.WeatherForecast;
import appsgate.lig.weather.WeatherForecastException;

/**
 * Implementation of Yahoo forecast, allows to change unit (Celsius,Fahrenheit)
 * but gives maximum 2 days forecast, as input its required the WOEID
 * (http://developer.yahoo.com/geo/geoplanet/guide/concepts.html#hierarchy)
 * which indicates the location for the forecast. This class parses the
 * information obtained in from weather yahoo service: e.g.
 * http://weather.yahooapis.com/forecastrss?w=12724717&u=c
 * 
 * @author jnascimento
 * 
 */
public class YahooWeatherImpl implements WeatherForecast {

    static final String FORECAST = "forecast";
    static final String TITLE = "title";
    static final String ITEM = "item";
    static final String DATEPUBLICATION = "pubDate";

    static final String xPathCondition = "//condition";
    static final String xPathWind = "//wind";
    static final String xPathForecast = "/rss/channel/item/forecast";
    static final String xPathPubDate = "//pubDate";
    static final String xPathTitle = "/rss/channel/item/title";

    private Logger logger = Logger.getLogger(YahooWeatherImpl.class
	    .getSimpleName());

    public Integer refreshRate;

    char currentUnit;

    URL url;
    String WOEID;
    int currentTemperature;
    int currentCode;
    int currentWindDirection;
    float currentWindSpeed;

    boolean noFetch;

    private String location;
    private Timer refreshTimer = new Timer();
    private List<DayForecast> forecasts;
    private Calendar datePublication;
    private Calendar lastFetch;

    /**
     * Feed URL that returns an XML with the forecast (e.g.
     * http://weather.yahooapis.com/forecastrss?w=12724717&u=c)
     */
    private String feedUrlTemplate = "http://weather.yahooapis.com/forecastrss?w=%s&u=%c";

    private String feedUrl;

    TimerTask refreshtask = new TimerTask() {
	@Override
	public void run() {

	    if (YahooWeatherImpl.this.refreshRate != -1) {

		logger.fine("Refreshing meteo data");
		try {
		    YahooWeatherImpl.this.fetch();
		} catch (WeatherForecastException exc) {
		    exc.printStackTrace();
		}

	    }
	}
    };

    public YahooWeatherImpl() {
	// Setting default location to Grenoble
	this("593720");
	// default unit is celsius -> corresponding to Unit.EU

    }

    public YahooWeatherImpl(String woeid) {
	forecasts = new ArrayList<DayForecast>();
	this.WOEID = woeid;
	currentUnit = 'c';
	noFetch = true;
    }

    public Calendar getDateForecast() {
	return null;
    }

    public Calendar getPublicationDate() {
	return this.datePublication;
    }

    public List<DayForecast> getForecast() throws WeatherForecastException {
	if (noFetch)
	    throw new WeatherForecastException(
		    "No weather data retrieved (make a fetch())");
	return forecasts;
    }

    public Calendar getLastFetch() {
	return lastFetch;
    }

    public String getCurrentLocation() throws WeatherForecastException {
	if (noFetch)
	    throw new WeatherForecastException(
		    "No weather data retrieved (make a fetch())");

	return location;
    }

    public void start() {

	try {
	    fetch();
	} catch (WeatherForecastException exc) {
	    exc.printStackTrace();
	}

	/**
	 * Configure auto-refresh meteo data
	 */
	if (refreshRate != null && refreshRate != -1) {
	    logger.fine("Configuring auto-refresh for :" + refreshRate);
	    refreshTimer.scheduleAtFixedRate(refreshtask, 0,
		    refreshRate.longValue());
	}

    }

    public void stop() {
	logger.info("WeatherForecast stopped:"
		+ this.getClass().getSimpleName());
	refreshtask.cancel();
    }

    public int getCurrentTemperature() throws WeatherForecastException {
	return currentTemperature;
    }

    @Override
    public String toString() {

	if (this.getLastFetch() == null) {
	    return "No info retrieved, wait for the next fetch";
	}
	StringBuffer sb = new StringBuffer();
	sb.append(String.format("location %s \n", this.location));
	sb.append(String.format("current temperature %s \n",
		currentTemperature));
	sb.append(String.format("current code %s \n",
		currentCode));
	sb.append(String.format("current wind direction %d \n",
		currentWindDirection));
	sb.append(String.format("current wind speed %f \n",
		currentWindSpeed));
	
	if (this.getPublicationDate() != null)
	    sb.append(String.format(
		    "meteo report from %1$te/%1$tm/%1$tY %1$tH:%1$tM \n",
		    this.getPublicationDate()));
	sb.append(String
		.format("osgi meteo polled report at %1$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS \n",
			this.getLastFetch().getTime()));
	sb.append("-- forecasts --\n");
	for (DayForecast forecast : forecasts) {
	    sb.append(String
		    .format("Date: %1$te/%1$tm/%1$tY (min:%2$s,max:%3$s, code:%4$s) \n",
			    forecast.getDate(), forecast.getMin(),
			    forecast.getMax(), forecast.getCode()));
	}

	sb.append("--- /forecasts ---\n");

	return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.weather.WeatherForecast#setLocation(java.lang.String)
     */
    public void setLocation(String placeName) throws WeatherForecastException {
	YahooGeoPlanet geoPlanet = new YahooGeoPlanetImpl();
	String newWOEID = geoPlanet.getWOEIDFromPlaceName(placeName);
	if (newWOEID != null) {
	    WOEID = newWOEID;
	    location = placeName;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.weather.WeatherForecast#getCurrentWeatherCode()
     */
    public int getCurrentWeatherCode() throws WeatherForecastException {
	return currentCode;
    }

    /**
     * Update all meteo information stored here
     */
    public WeatherForecast fetch() throws WeatherForecastException {
	try {

	    feedUrl = String.format(feedUrlTemplate, WOEID, currentUnit);

	    this.url = new URL(feedUrl);

	    // First create a new XMLInputFactory
	    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	    // Setup a new eventReader
	    InputStream in = read();
	    DocumentBuilder db = null;
	    db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document doc = db.parse(in);

	    parseCurrentConditions(doc);
	    forecasts = new ArrayList<DayForecast>();
	    parseForecast(doc);
	    noFetch = false;

	} catch (Exception e) {
	    e.printStackTrace();
	    throw new WeatherForecastException(
		    "Impossible to fetch to weather forecast service, "
			    + e.getMessage());
	}

	lastFetch = Calendar.getInstance();

	return this;
    }

    void parseCurrentConditions(Document xmlDocument) throws Exception {
	XPath xPath = XPathFactory.newInstance().newXPath();

	currentTemperature = Integer.valueOf(xPath.evaluate(xPathCondition
		+ "/@temp", xmlDocument));
	logger.info("Current temperature : " + currentTemperature);

	currentCode = Integer.valueOf(xPath.evaluate(xPathCondition + "/@code",
		xmlDocument));
	logger.info("Current code : " + currentCode + ", "
		+ WeatherCodesHelper.getDescription(currentCode));

	currentWindDirection = Integer.valueOf(xPath.evaluate(xPathWind
		+ "/@direction", xmlDocument));
	logger.info("Current wind direction : " + currentWindDirection);

	currentWindSpeed = Float.valueOf(xPath.evaluate(
		xPathWind + "/@speed", xmlDocument));
	logger.info("Current wind speed : " + currentWindDirection);

	location = xPath.evaluate(xPathTitle, xmlDocument);
	logger.info("Current location : " + location);
    }

    public void parseForecast(Document xmlDocument) throws Exception {
	XPath xPath = XPathFactory.newInstance().newXPath();
	NodeList listForecast = (NodeList) xPath.evaluate(xPathForecast,
		xmlDocument, XPathConstants.NODESET);
	logger.info(listForecast.getLength() + " days of forecast");
	Date date = null;
	float min;
	float max;
	int code;
	for (int i = 0; i < listForecast.getLength(); i++) {
	    NamedNodeMap map = listForecast.item(i).getAttributes();

	    DateFormat yahooForecastDateAttributeParser = new SimpleDateFormat(
		    "dd MMM yyyy", Locale.ENGLISH);
	    String dateString = map.getNamedItem("date").getNodeValue();

	    date = yahooForecastDateAttributeParser.parse(dateString);

	    min = Float.parseFloat(map.getNamedItem("low").getNodeValue());
	    max = Float.parseFloat(map.getNamedItem("high").getNodeValue());
	    code = Integer.parseInt(map.getNamedItem("code").getNodeValue());

	    logger.info("Forecast, day : " + map.getNamedItem("day")
		    + ", date : " + date + ", min : " + min + ", max : " + max
		    + ", code :" + code);

	    DayForecast df = new DayForecast(date, min, max, code);
	    forecasts.add(df);
	}
    }

    private InputStream read() throws Exception {
	return url.openStream();
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

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.weather.WeatherForecast#getUnit()
     */
    @Override
    public Unit getUnit() {
	switch (currentUnit) {
	case 'c':
	    return Unit.EU;
	case 'f':
	    return Unit.US;
	default:
	    currentUnit = 'c';
	    return WeatherForecast.Unit.EU;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.weather.WeatherForecast#getCurrentWindSpeed()
     */
    @Override
    public float getCurrentWindSpeed() throws WeatherForecastException {
	if (noFetch)
	    throw new WeatherForecastException(
		    "No weather data retrieved (make a fetch())");

	return currentWindSpeed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.weather.WeatherForecast#getCurrentWindDirection()
     */
    @Override
    public int getCurrentWindDirection() throws WeatherForecastException {
	if (noFetch)
	    throw new WeatherForecastException(
		    "No weather data retrieved (make a fetch())");

	return currentWindDirection;
    }

}
