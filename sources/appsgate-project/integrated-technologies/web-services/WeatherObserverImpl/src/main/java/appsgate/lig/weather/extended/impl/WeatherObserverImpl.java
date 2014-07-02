package appsgate.lig.weather.extended.impl;

import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import appsgate.lig.weather.extended.spec.messages.DaylightNotificationMsg;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;

import java.util.Date;

/**
 * Created by thibaud on 01/07/2014.
 */
public class WeatherObserverImpl implements ExtendedWeatherObserver{

    private String currentLocation;
    private boolean currentlyDaylight;
    private int currentWeatherCode;
    private int currentTemperature;
    private int tomorrowWeatherCode;
    private int tomorrowMinTemperature;
    private int tomorrowMaxTemperature;

    private CoreWeatherServiceSpec weatherService;
    private CoreClockSpec clock;

    public static final String IMPL_NAME = "WeatherObserverImpl";


    private DaylightNotificationMsg fireDaylightNotificationMsg() {
        return new DaylightNotificationMsg(false,null,null,null);
    }

    @Override
    public String getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public boolean isCurrentlyDaylight()throws WeatherForecastException  {
        Date current = clock.getCurrentDate().getTime();

        if(current.compareTo(weatherService.getCurrentWeather(currentLocation).getSunrise())>=0
                && current.compareTo(weatherService.getCurrentWeather(currentLocation).getSunset())<0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getCurrentWeatherCode() throws WeatherForecastException{
        currentWeatherCode = weatherService.getWeatherCodeForecast(currentLocation,0);
        return currentWeatherCode;
    }

    @Override
    public int getCurrentTemperature()throws WeatherForecastException {
        currentTemperature = weatherService.getAverageTemperatureForecast(currentLocation,0);
        return currentTemperature;
    }

    @Override
    public int getTomorrowWeatherCode() throws WeatherForecastException{
        tomorrowWeatherCode = weatherService.getWeatherCodeForecast(currentLocation,1);
        return tomorrowWeatherCode;
    }

    @Override
    public int getTomorrowMinTemperature() throws WeatherForecastException{
        tomorrowMinTemperature = weatherService.getMinTemperatureForecast(currentLocation,1);
        return tomorrowMinTemperature;
    }

    @Override
    public int getTomorrowMaxTemperature() throws WeatherForecastException{
        tomorrowMaxTemperature = weatherService.getMaxTemperatureForecast(currentLocation,1);
        return tomorrowMaxTemperature;
    }

}
