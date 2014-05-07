package appsgate.lig.weather.yahoo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;
import appsgate.lig.weather.utils.WeatherCodesHelper;

/**
 * Implementation of Yahoo forecast, allows to change unit (Celsius,Fahrenheit)
 * (http://developer.yahoo.com/geo/geoplanet/guide/concepts.html#hierarchy)
 * which indicates the location for the forecast. This class parses the
 * information obtained in from weather yahoo service: e.g.
 * http://weather.yahooapis.com/forecastrss?w=12724717&u=c
 * 
 * @author thibaud
 * 
 */
public class YahooWeatherParser {

    static private Logger logger = Logger.getLogger(YahooWeatherParser.class
	    .getSimpleName());

    static final String xPathCondition = "//condition";
    static final String xPathWind = "//wind";
    static final String xPathForecast = "/rss/channel/item/forecast";
    static final String xPathPubDate = "//pubDate";
    static final String xPathTitle = "/rss/channel/item/title";

    static public CurrentWeather parseCurrentConditions(Document xmlDocument)
	    throws Exception {
	XPath xPath = XPathFactory.newInstance().newXPath();

	int temperature = Integer.valueOf(xPath.evaluate(xPathCondition
		+ "/@temp", xmlDocument));
	logger.info("Current temperature : " + temperature);

	int code = Integer.valueOf(xPath.evaluate(xPathCondition + "/@code",
		xmlDocument));
	logger.info("Current code : " + code + ", "
		+ WeatherCodesHelper.getDescription(code));

	int windDirection = Integer.valueOf(xPath.evaluate(xPathWind
		+ "/@direction", xmlDocument));
	logger.info("Current wind direction : " + windDirection);

	float windSpeed = Float.valueOf(xPath.evaluate(xPathWind + "/@speed",
		xmlDocument));
	logger.info("Current wind speed : " + windSpeed);

	return new CurrentWeather(temperature, code, windDirection, windSpeed);
    }

    static public List<DayForecast> parseForecast(Document xmlDocument)
	    throws Exception {
	List<DayForecast> forecasts = new ArrayList<DayForecast>();

	XPath xPath = XPathFactory.newInstance().newXPath();
	NodeList listForecast = (NodeList) xPath.evaluate(xPathForecast,
		xmlDocument, XPathConstants.NODESET);
	logger.info(listForecast.getLength() + " days of forecast");
	Date date = null;
	int min;
	int max;
	int code;
	for (int i = 0; i < listForecast.getLength(); i++) {
	    NamedNodeMap map = listForecast.item(i).getAttributes();

	    DateFormat yahooForecastDateAttributeParser = new SimpleDateFormat(
		    "dd MMM yyyy", Locale.ENGLISH);
	    String dateString = map.getNamedItem("date").getNodeValue();

	    date = yahooForecastDateAttributeParser.parse(dateString);

	    min = Integer.parseInt(map.getNamedItem("low").getNodeValue());
	    max = Integer.parseInt(map.getNamedItem("high").getNodeValue());
	    code = Integer.parseInt(map.getNamedItem("code").getNodeValue());

	    logger.info("Forecast, day : " + map.getNamedItem("day")
		    + ", date : " + date + ", min : " + min + ", max : " + max
		    + ", code :" + code);

	    DayForecast df = new DayForecast(date, min, max, code);
	    forecasts.add(df);
	}
	return forecasts;
    }

    static public String parseLocation(Document xmlDocument) throws Exception {
	XPath xPath = XPathFactory.newInstance().newXPath();

	String location = xPath.evaluate(xPathTitle, xmlDocument);
	logger.info("Current location : " + location);

	return location;
    }

    static public Calendar parsePublicationDate(Document xmlDocument)
	    throws Exception {
	XPath xPath = XPathFactory.newInstance().newXPath();

	String date = xPath.evaluate(xPathPubDate, xmlDocument);
	logger.info("Current publication date : " + date);

	DateFormat yahooForecastDateAttributeParser = new SimpleDateFormat(
		"EEE, d MMM yyyy h:m a zzz", Locale.ENGLISH);

	Date pubdate = yahooForecastDateAttributeParser.parse(date);
	Calendar cal=Calendar.getInstance();
	cal.setTime(pubdate);

	return cal;
    }

}
