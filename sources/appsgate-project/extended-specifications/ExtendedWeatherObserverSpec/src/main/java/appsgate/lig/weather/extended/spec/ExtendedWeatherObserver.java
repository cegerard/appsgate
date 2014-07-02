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
     * @return true is current Time is day for the location, false if night
     */
    public boolean isCurrentlyDaylight() throws WeatherForecastException;


    /**
     *
     * @return the simplified code of current weather @see CoreWeatherServiceSpec
     */
    public int getCurrentWeatherCode() throws WeatherForecastException;


    /**
     *
     * @return the current temperature @see CoreWeatherServiceSpec
     */
    public int getCurrentTemperature() throws WeatherForecastException;

    /**
     *
     * @return the simplified weather code forecast for tomorrow @see CoreWeatherServiceSpec
     */
    public int getTomorrowWeatherCode() throws WeatherForecastException;

    /**
     *
     * @return the min temperature forecast for tomorrow @see CoreWeatherServiceSpec
     */
    public int getTomorrowMinTemperature() throws WeatherForecastException;

    /**
     *
     * @return the max temperature forecast for tomorrow @see CoreWeatherServiceSpec
     */
    public int getTomorrowMaxTemperature() throws WeatherForecastException;

}
