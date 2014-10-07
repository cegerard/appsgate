package appsgate.lig.yahoo.weather;

import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;

import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Interface for weather forecast service
 * 
 * @author thibaud
 * 
 */
public interface YahooWeather {

    /**
     * Units defined for Weather Conditions
     * <ul>
     * <li>EU for European (temperature °C, distance km, pressure mb, speed km/h) - Metric format</li>
     * <li>US for English (temperature °F, distance mi, pressure in, speed mph)</li>
     * </ul>
     */
    static enum Unit {
	/**
	 * European measure units (temperature °C, distance km, pressure mb, speed km/h) - Metric Format
	 */
	EU,
	/**
	 * English measure units (temperature °F, distance mi, pressure in, speed mph)
	 */
	US;
    }

    /**
     * @return The currently defined locations for Weather Forecast as they were entered
     */
    String[] getLocations();


    /**
     * Try add the place with an human friendly name to fetch Weather conditions
     *
     * @param placeName
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @throws appsgate.lig.weather.exception.WeatherForecastException If place name in incorrect, misspelled, cannot be found or if weather cannot be retrieved for this place
     */
    void addLocation(String placeName) throws WeatherForecastException;

    /**
     * Try to remove a place previously added
     *
     * @param placeName the placeName as it was previously added
     * @return true if the place was found and was successfully removed
     * @throws appsgate.lig.weather.exception.WeatherForecastException If place name in incorrect, misspelled, or cannot be found
     */
    boolean removeLocation(String placeName) throws WeatherForecastException;

    /**
     * Check if a location is monitored
     *
     * @param placeName
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @return true if the place is currently monitored, false otherwise
     * @throws appsgate.lig.weather.exception.WeatherForecastException If place name in incorrect, misspelled, or cannot be found (as a valid location)
     */
    boolean containLocation(String placeName) throws WeatherForecastException;
    
    /**
    *
    * @return a convenient Yahoo Presentation URL for the weather
    */
    String getPresentationURL(String placeName) throws WeatherForecastException;    
    
    /**
    *
    * @return a the WOEID (Where On Earth Identifier) associated with a placeName
    */
    String getWOEID(String placeName) throws WeatherForecastException;     

    /**
     * @param unit US or EU
     */
    void setUnit(Unit unit);

    /**
     * @return The Forecast for the next days (number of days might vary),
     * including min and max temperature weather code, for a particular location
     * @param placeName the placeName as it was previously added
     * @throws appsgate.lig.weather.exception.WeatherForecastException if no weather forecast have been fetch or if place cannot be found
     */
    List<DayForecast> getForecast(String placeName) throws WeatherForecastException;

    /**
     * @return The current weather conditions for a particular location
     * , for a particular location
     * @param placeName the placeName as it was previously added
     * @throws appsgate.lig.weather.exception.WeatherForecastException if no weather forecast have been fetch or if place cannot be found
     */
    CurrentWeather getCurrentWeather(String placeName) throws WeatherForecastException;

    /**
     * @return The Date at which weather forecast data were initially published
     * (do not confuse with getLastFetch date) , for a particular location
     * @param placeName the placeName as it was previously added
     * @throws appsgate.lig.weather.exception.WeatherForecastException if no weather forecast have been fetch or if place cannot be found
     */
    Calendar getPublicationDate(String placeName) throws WeatherForecastException;

    /**
     * @return The Date at which the Weather web-service was requested for the last time
     * or null if no data been fetched
     */
    public Calendar getLastFetchDate();

    /**
     * Try to connect to a weather web-service and fetch the latest weather forecast for all location monitored
     * @throws appsgate.lig.weather.exception.WeatherForecastException if impossible to retrieve weather forecast (web-service unavailable, ...)
     */
    void fetch() throws WeatherForecastException;
    
    /**
     * Check a location upon it place Name
     *
     * @param location
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @return A JSON object describing the location
     * {"locality1":"Grenoble","woeid":"593720","name":"Grenoble","placeTypeName":"Town","country":"France"}
     */
    public JSONObject checkLocation(String location);	    
    
    /**
     * Check a location upon the first letters of its place name
     *
     * @param location
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @return A JSONArray with 0..5  objects describing the location (formatted as follow), example starting with Gre
     * [
     * {"locality1":"Grenoble","woeid":"593720","name":"Grenoble","placeTypeName":"Town","country":"France"},
     * {"locality1":"Green Bay","woeid":"2413753","name":"Green Bay","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greenville","woeid":"2414583","name":"Greenville","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greensboro","woeid":"2414469","name":"Greensboro","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greifswald","woeid":"654035","name":"Greifswald","placeTypeName":"Town","country":"Allemagne"}
     * ]
     */
    public JSONArray checkLocationsStartingWith(String firstLetters);    


} 