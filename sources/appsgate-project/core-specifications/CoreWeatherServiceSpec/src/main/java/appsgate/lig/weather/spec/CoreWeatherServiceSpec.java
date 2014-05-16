package appsgate.lig.weather.spec;

import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;

/**
 * Interface for weather forecast service
 * 
 * @author thibaud
 * 
 */
public interface CoreWeatherServiceSpec {
    
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
     * @throws WeatherForecastException If place name in incorrect, misspelled, cannot be found or if weather cannot be retrieved for this place
     */
    void addLocation(String placeName) throws WeatherForecastException;
    
    /**
     * Try to remove a place previously added
     * 
     * @param the placeName as it was previously added
     * @return true if the place was found and was successfully removed
     * @throws WeatherForecastException If place name in incorrect, misspelled, or cannot be found
     */
    boolean removeLocation(String placeName) throws WeatherForecastException;
    
    /**
     * Check if a location is monitored
     *  
     * @param placeName
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @return true if the place is currently monitored, false otherwise
     * @throws WeatherForecastException If place name in incorrect, misspelled, or cannot be found (as a valid location)
     */
    boolean containLocation(String placeName) throws WeatherForecastException;
    
    /**
     * @param unit US or EU
     */
    void setUnit(Unit unit);
        
    /**
     * @return The Forecast for the next days (number of days might vary),
     * including min and max temperature weather code, for a particular location
     * @param the placeName as it was previously added
     * @throws WeatherForecastException if no weather forecast have been fetch or if place cannot be found
     */
    List<DayForecast> getForecast(String placeName) throws WeatherForecastException;

    /**
     * @return The current weather conditions for a particular location
     * , for a particular location
     * @param the placeName as it was previously added
     * @throws WeatherForecastException if no weather forecast have been fetch or if place cannot be found
     */    
    CurrentWeather getCurrentWeather(String placeName) throws WeatherForecastException;
    
    /**
     * @return The Date at which weather forecast data were initially published
     * (do not confuse with getLastFetch date) , for a particular location
     * @param the placeName as it was previously added
     * @throws WeatherForecastException if no weather forecast have been fetch or if place cannot be found
     */
    Calendar getPublicationDate(String placeName) throws WeatherForecastException;

    /**
     * @return The Date at which the Weather web-service was requested for the last time
     * or null if no data been fetched 
     */
    public Calendar getLastFetchDate();

    /**
     * Try to connect to a weather web-service and fetch the latest weather forecast for all location monitored
     * @throws WeatherForecastException if impossible to retrieve weather forecast (web-service unavailable, ...)
     */
    void fetch() throws WeatherForecastException;
    
    /**
     * @return The corresponding notification message
     */
    NotificationMsg fireWeatherUpdateMessage(String property, String value, String eventType);
    
    /**
     * Check if weather forecast match a simplified  weather code 
     *  for a particular place, and a particular day 
     * @param placeName the place to monitor (should exists according to GeoPlanet ID)
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @param simpleWeatherCodeis the expected weather forecast (not all yahoo weather codes, only the simplified ones)
     * @return true if expected weather is the code forecast, @see appsgate.lig.weather.spec.utils.SimplifiedWeatherCodesHelper,
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     */
    boolean isWeatherSimplifiedCodeForecast(String placeName, int dayForecast, int simpleWeatherCode) throws WeatherForecastException;    
    
    /**
     * Get Weather 
     * forecast for a particular place, and a particular day 
     * @param placeName the place to monitor (should exists according to GeoPlanet ID)
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @return the weather code as an int, @see appsgate.lig.weather.spec.utils.WeatherCodesHelper,
     * for today, we use the current weather code (not a forecast)
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     */
    int getWeatherCodeForecast(String placeName, int dayForecast) throws WeatherForecastException;
    
    /**
     * Get temperature 
     * forecast for a particular place, and a particular day 
     * @param placeName the place to monitor (should exists according to GeoPlanet ID)
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @return the maximum temperature as an int
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     */
    int getMaxTemperatureForecast(String placeName, int dayForecast) throws WeatherForecastException;
    
    /**
     * Get temperature 
     * forecast for a particular place, and a particular day 
     * @param placeName the place to monitor (should exists according to GeoPlanet ID)
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @return the minimum temperature as an int
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     */
    int getMinTemperatureForecast(String placeName, int dayForecast) throws WeatherForecastException;
    
    /**
     * Get temperature 
     * forecast for a particular place, and a particular day 
     * @param placeName the place to monitor (should exists according to GeoPlanet ID)
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @return a temperature forecast as an int (average value between maximum and minimum),
     * for today, we use the current temperature (not a forecast)
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     */
    int getAverageTemperatureForecast(String placeName, int dayForecast) throws WeatherForecastException;    
    
    
    /**
     * Method GUI uses
     */
    public JSONArray getCurrentWeather() throws JSONException;
    public JSONArray getForecast() throws JSONException;
} 