package appsgate.lig.weather.extended.spec;

import appsgate.lig.weather.exception.WeatherForecastException;

/**
 * Created by thibaud on 01/07/2014.
 */
public interface ExtendedWeatherObserver {

    /**
     * Get a string representation of the Location this Observer monitor
     * @return
     */
    public String getCurrentLocation();

    public final String SPEC_NAME = "ExtendedWeatherObserverSpec";

    /**
    *
    * @return the Where On Earth IDdentifier of the location from Yahoo
    */
    String getCurrentWOEID();

    /**
    *
    * @return a convenient Presentation URL for the weather
    */
    String getPresentationURL();    
    
    /**
     *
     * @return true is current Time is day for the location, false if night
     */
    boolean isCurrentlyDaylight() throws WeatherForecastException;


    /**
     * Check if weather forecast match a simplified  weather code
     *  for a a particular day
     * Note that the forecast for today may be different of the current weather condition
     * @param simpleWeatherCode is the expected weather forecast (not all yahoo weather codes, only the simplified ones)
     * @return true if expected weather is the code forecast, @see appsgate.lig.weather.spec.utils.SimplifiedWeatherCodesHelper,
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     */
    boolean isCurrentWeatherCode(int simpleWeatherCode) throws WeatherForecastException;

    /**
     *
     * @return the simplified code of current weather @see CoreWeatherServiceSpec
     */
    int getCurrentWeatherCode() throws WeatherForecastException;


    /**
     *
     * @return the current temperature @see CoreWeatherServiceSpec
     */
    int getCurrentTemperature() throws WeatherForecastException;

    /**
     * Check if weather forecast match a simplified  weather code
     *  for a a particular day
     * Note that the forecast for today may be different of the current weather condition
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @param simpleWeatherCode is the expected weather forecast (not all yahoo weather codes, only the simplified ones)
     * @return true if expected weather is the code forecast, @see appsgate.lig.weather.spec.utils.SimplifiedWeatherCodesHelper,
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     */
    boolean isForecastWeatherCode(int dayForecast, int simpleWeatherCode) throws WeatherForecastException;

    /**
     /**
     * Get Weather forecast for a particular place, and a particular day
     * Note that the forecast for today may be different of the current weather condition
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @throws WeatherForecastException if the place was not found, the forecast day is not supported,
     * the remote weather service not working ...
     *
     * @return the simplified weather code forecast
     */
    int getForecastWeatherCode(int dayForecast) throws WeatherForecastException;

    /**
     * Get temperature minimum
     * forecast for a particular day
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @return the minimum temperature as an int
     * @throws WeatherForecastException the forecast day is not supported,
     * the remote weather service not working ...
     */
    int getForecastMinTemperature(int dayForecast) throws WeatherForecastException;

    /**
     * Get temperature maximum
     * forecast for a particular day
     * @param dayForecast O is for today, 1 for tomorrow, 2 for the day after and so on
     * @return the maximum temperature as an int
     * @throws WeatherForecastException the forecast day is not supported,
     * the remote weather service not working ...
     */
    int getForecastMaxTemperature(int dayForecast) throws WeatherForecastException;

}
