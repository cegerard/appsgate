package appsgate.lig.yahoo.impl;

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
import org.w3c.dom.Node;
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
    static final String xPathAstronomy = "//astronomy";
    static final String xPathForecast = "/rss/channel/item/forecast";
    static final String xPathPubDate = "//pubDate";
    static final String xPathPresentationURL = "//link";
    static final String xPathLocationURL = "/rss/channel/location";


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

        DateFormat sdf = new SimpleDateFormat("h:mm a");
        Calendar sunrise = parsePublicationDate(xmlDocument);
        sunrise.set(Calendar.HOUR_OF_DAY, 0);
        sunrise.set(Calendar.MINUTE, 0);

        Calendar sunset = (Calendar)sunrise.clone();


        Calendar tmp = Calendar.getInstance();
        tmp.setTime(sdf.parse(xPath.evaluate(xPathAstronomy + "/@sunrise",
                xmlDocument)));
        sunrise.set(Calendar.HOUR_OF_DAY, tmp.get(Calendar.HOUR_OF_DAY));
        sunrise.set(Calendar.MINUTE, tmp.get(Calendar.MINUTE));
        logger.info("Sunrise : " + sunrise.getTime());


        tmp.setTime(sdf.parse(xPath.evaluate(xPathAstronomy + "/@sunset",
                xmlDocument)));
        sunset.set(Calendar.HOUR_OF_DAY, tmp.get(Calendar.HOUR_OF_DAY));
        sunset.set(Calendar.MINUTE, tmp.get(Calendar.MINUTE));
        logger.info("Sunset : " + sunset.getTime());
        return new CurrentWeather(temperature, code, windDirection, windSpeed, sunrise.getTime(), sunset.getTime());
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
    
    static public String parsePresentationURL(Document xmlDocument, String woeid) throws Exception {
    	// PresentationURL (old "link" xml elements does not work anymore)
    	// -> we build our own URL, as it is done when fetching a location on a browser
        
        //String location = xPath.evaluate(xPathPresentationURL, xmlDocument);
    	
    	XPath xPath = XPathFactory.newInstance().newXPath();
    	Node location = (Node)xPath.evaluate(xPathLocationURL, xmlDocument,
    			XPathConstants.NODE);
    	NamedNodeMap attributes = location.getAttributes();
    	
    	
    	String country = attributes.getNamedItem("country").getNodeValue();
    	String region = attributes.getNamedItem("region").getNodeValue();
    	String city = attributes.getNamedItem("city").getNodeValue();
    	
    	String presentationURL = "";
    	
    	if(country != null) {
    		country=country.replace(' ', '_');
    		
    		if(country.equals("France")) {
    			// Ugly hack to use the yahoo french website when fetching french location
    			presentationURL+="https://fr.meteo.yahoo.com/";
    		} else {
    			presentationURL+="https://weather.yahoo.com/";
    		}
    		
    		// Using country
			presentationURL+=country;    			

    		
    		// We should use the region and the city, but it does not work alwaysn
    		// especially with unusual characters, we replace the region with x
    		// and city with x, and we end by the woeid
    		
    		// Using region
    		presentationURL+="/x";
    		
    		
    		// Using city
    		presentationURL+="/x";
    		
    		// adding woeid
    		presentationURL+="-"+woeid;
    		
    	} else {
    		logger.warning("parsePresentationURL(...), no country found for current location");
    	}
    	logger.info("returning presentation URL : "+presentationURL);
		
    	return presentationURL;
    }

    static public Calendar parsePublicationDate(Document xmlDocument)
            throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();

        String date = xPath.evaluate(xPathPubDate, xmlDocument);
        logger.info("Current publication date : " + date);

        DateFormat yahooForecastDateAttributeParser = new SimpleDateFormat(
                "EEE, d MMM yyyy h:m a z", Locale.US);

        Date pubdate = yahooForecastDateAttributeParser.parse(date);
        Calendar cal=Calendar.getInstance();
        cal.setTime(pubdate);

        return cal;
    }

}
