package appsgate.lig.weather;

import java.util.Calendar;
import java.util.List;

/**
 * Interface for weather forecast service
 * 
 * @author jnascimento
 * 
 */
public interface WeatherForecast {
    
    
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
     * Try to resolve the place with an human friendly name
     * 
     * @param placeName
     *            A human place name (a town, a country, a particular place)
     * @throws WeatherForecastException If place name in incorrect, misspelled or cannot be found
     */
    void setLocation(String placeName) throws WeatherForecastException;
    
    /**
     * @param unit US or EU
     */
    void setUnit(Unit unit);
    
    /**
     * @return the currently defined unit (EU by default)
     */
    Unit getUnit();
    

    /**
     * @return The current defined location for Weather Forecast (Human Readable),
     * by default location is set to Grenoble
     * @throws WeatherForecastException if no weather forecast have been fetch
     */
    String getCurrentLocation() throws WeatherForecastException;

    /**
     * @return The current temperature (accordingly to current temperature unit °C or °F)
     * @throws WeatherForecastException if no weather forecast have been fetch
     */
    int getCurrentTemperature() throws WeatherForecastException;

    /**
     * @return The code describing current weather condition 
     * @see appsgate.lig.weather.WeatherCodesHelper
     * @throws WeatherForecastException if no weather forecast have been fetch
     */
    int getCurrentWeatherCode() throws WeatherForecastException;
    
    /**
     * @return The current wind speed (accordingly to current unit mph or km/h)
     * @throws WeatherForecastException if no weather forecast have been fetch
     */
    float getCurrentWindSpeed() throws WeatherForecastException;
    
    
    /**
     * @return The current wind direction (in degree)
     * @throws WeatherForecastException if no weather forecast have been fetch
     */
    int getCurrentWindDirection() throws WeatherForecastException;
    
    
    /**
     * @return The Forecast for the next days (number of days might vary),
     * including min and max temperature weather code
     * @throws WeatherForecastException if no weather forecast have been fetch
     */
    List<DayForecast> getForecast() throws WeatherForecastException;
    
    /**
     * @return The Date at which weather forecast data were initially published
     * (do not confuse with getLastFetch date)
     * @throws WeatherForecastException if no weather forecast have been fetch
     */
    Calendar getPublicationDate() throws WeatherForecastException;

    /**
     * @return The Date at which the Weather web-service was requested for the last time
     * or null if no data been fetched 
     */
    Calendar getLastFetch();

    /**
     * Try to connect to a weather web-service and fetch the latest weather forecast
     * @return a forecast object
     * @throws WeatherForecastException if impossible to retrieve weather forecast (web-service unavailable, ...)
     */
    WeatherForecast fetch() throws WeatherForecastException;
} 