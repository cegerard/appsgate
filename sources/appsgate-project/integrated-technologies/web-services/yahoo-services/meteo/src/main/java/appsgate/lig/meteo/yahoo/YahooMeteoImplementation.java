package appsgate.lig.meteo.yahoo;

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

import appsgate.lig.meteo.DayForecast;
import appsgate.lig.meteo.Meteo;
import appsgate.lig.meteo.WeatherCodesHelper;

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
public class YahooMeteoImplementation implements Meteo {

    static final String FORECAST = "forecast";
    static final String TITLE = "title";
    static final String ITEM = "item";
    static final String DATEPUBLICATION = "pubDate";

    static final String xPathCondition = "//condition";
    static final String xPathForecast = "/rss/channel/item/forecast";
    static final String xPathPubDate = "//pubDate";
    static final String xPathTitle = "/rss/channel/item/title";

    private Logger logger = Logger.getLogger(YahooMeteoImplementation.class
	    .getSimpleName());

    public Integer refreshRate;

    URL url;
    String WOEID;
    int currentTemperature;
    int currentCode;

    private String location;
    private Timer refreshTimer = new Timer();
    private List<DayForecast> forecasts;
    private Calendar datePublication;
    private Calendar lastFetch;

    /**
     * Feed URL that returns an XML with the forecast (e.g.
     * http://weather.yahooapis.com/forecastrss?w=12724717&u=c)
     */
    private String feedUrlTemplate = "http://weather.yahooapis.com/forecastrss?w=%s&u=c";

    private String feedUrl;

    TimerTask refreshtask = new TimerTask() {
	@Override
	public void run() {

	    if (YahooMeteoImplementation.this.refreshRate != -1) {

		logger.fine("Refreshing meteo data");
		YahooMeteoImplementation.this.fetch();

	    }
	}
    };

    public YahooMeteoImplementation() {
	// Setting default location to Grenoble
	this("593720");

    }

    public YahooMeteoImplementation(String woeid) {
	forecasts = new ArrayList<DayForecast>();
	this.WOEID = woeid;
    }

    public Calendar getDateForecast() {
	return null;
    }

    public Calendar getDatePublication() {
	return this.datePublication;
    }

    public List<DayForecast> getForecast() {
	return forecasts;
    }

    public Calendar getLastFetch() {
	return lastFetch;
    }

    public String getLocation() {
	return location;
    }

    public void start() {

	fetch();

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
	logger.info("Meteo stopped:" + this.getClass().getSimpleName());
	refreshtask.cancel();
    }

    public int getCurrentTemperature() {
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
		this.getCurrentTemperature()));
	sb.append(String.format("current code %s \n",
		this.getCurrentWeatherCode()));
	
	if (this.getDatePublication() != null)
	    sb.append(String.format(
		    "meteo report from %1$te/%1$tm/%1$tY %1$tH:%1$tM \n",
		    this.getDatePublication()));
	sb.append(String
		.format("osgi meteo polled report at %1$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS \n",
			this.getLastFetch().getTime()));
	sb.append("-- forecasts --\n");
	for (DayForecast forecast : this.getForecast()) {
	    sb.append(String.format(
		    "Date: %1$te/%1$tm/%1$tY (min:%2$s,max:%3$s, code:%4$s) \n",
		    forecast.getDate(), forecast.getMin(), forecast.getMax(),forecast.getCode()));
	}
	
	sb.append("--- /forecasts ---\n");

	return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.meteo.Meteo#setLocation(java.lang.String)
     */
    public void setLocation(String placeName) {
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
     * @see appsgate.lig.meteo.Meteo#getCurrentWeatherCode()
     */
    public int getCurrentWeatherCode() {
	return currentCode;
    }

    /**
     * Update all meteo information stored here
     */
    public Meteo fetch() {
	try {

	    feedUrl = String.format(feedUrlTemplate, WOEID);

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

	} catch (Exception e) {
	    e.printStackTrace();
	}

	lastFetch = Calendar.getInstance();

	return this;
    }

    public void parseCurrentConditions(Document xmlDocument) {
	try {
	    
	    XPath xPath = XPathFactory.newInstance().newXPath();

	    currentTemperature = Integer.valueOf(xPath.evaluate(xPathCondition
		    + "/@temp", xmlDocument));
	    logger.info("Current temperature : " + currentTemperature);

	    currentCode = Integer.valueOf(xPath.evaluate(xPathCondition
		    + "/@code", xmlDocument));
	    logger.info("Current code : " + currentCode + ", "
		    + WeatherCodesHelper.getDescription(currentCode));

	    location = xPath.evaluate(xPathTitle, xmlDocument);
	    logger.info("Current location : " + location);

	} catch (Exception exc) {
	    logger.warning(exc.getMessage());
	    exc.printStackTrace();
	}
    }
    
    public void parseForecast(Document xmlDocument) {
	try {
	    
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
		
		min= Float.parseFloat(map.getNamedItem("low").getNodeValue());
		max= Float.parseFloat(map.getNamedItem("high").getNodeValue());
		code= Integer.parseInt(map.getNamedItem("code").getNodeValue());

		logger.info("Forecast, day : " + map.getNamedItem("day")
			+ ", date : " + date
			+ ", min : "+min
			+ ", max : "+max
			+ ", code :"+code);

		DayForecast df = new DayForecast(date, min, max, code);
		forecasts.add(df);
	    }

	} catch (Exception exc) {
	    logger.warning(exc.getMessage());
	    exc.printStackTrace();
	}
    }    

    private InputStream read() {
	try {
	    return url.openStream();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

}
