package appsgate.lig.weather.extended.impl;

import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.AbstractObjectSpec;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import appsgate.lig.weather.extended.spec.messages.DaylightNotificationMsg;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;
import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by thibaud on 01/07/2014.
 */
public class WeatherObserverImpl extends AbstractObjectSpec implements ExtendedWeatherObserver, AlarmEventObserver, CoreObjectSpec{

    private String currentLocation;
    private boolean currentlyDaylight;
    private Calendar sunrise = Calendar.getInstance();
    private Calendar sunset = Calendar.getInstance();
    private int currentWeatherCode;
    private int currentTemperature;
    private int tomorrowWeatherCode;
    private int tomorrowMinTemperature;
    private int tomorrowMaxTemperature;

    private CoreWeatherServiceSpec weatherService;
    private CoreClockSpec clock;

    private int alarmSunset=-1;
    private int alarmSunrise=-1;

    public static final String IMPL_NAME = "WeatherObserverImpl";

    public WeatherObserverImpl() {
        appsgatePictureId = null;
        appsgateUserType = "103"; // 103 stands for weather forecast service
        appsgateDeviceStatus = "2"; // 2 means device paired (for a device, not
        // relevant for service)
        appsgateObjectId = String.valueOf( this.hashCode()); // Object id
        // prefixed by
        // the user type
        appsgateServiceName = "Yahoo Weather Forecast";
        appsgateCoreType = CORE_TYPE.SERVICE;
    }

    private static Logger logger = LoggerFactory
            .getLogger(WeatherObserverImpl.class);

    private DaylightNotificationMsg fireDaylightNotificationMsg(boolean dayLight) {

        return new DaylightNotificationMsg(dayLight,null,null,null);
    }

    @Override
    public String getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public boolean isCurrentlyDaylight()throws WeatherForecastException  {
        Calendar current = clock.getCurrentDate();
        refresh();

        if(current.compareTo(sunrise)>=0
                && current.compareTo(sunset)<0) {
            logger.debug("isCurrentlyDaylight() return true, clock indicates a time" +
                    " between sunrise and sunset time");
            return true;
        } else {
            logger.debug("isCurrentlyDaylight() return false, clock indicates a time" +
                    " before sunrise or after sunset (or error during date comparison)");

            return false;
        }
    }

    @Override
    public int getCurrentWeatherCode() throws WeatherForecastException{
        refresh();
        return currentWeatherCode;
    }

    @Override
    public int getCurrentTemperature()throws WeatherForecastException {
        refresh();
        return currentTemperature;
    }

    @Override
    public int getTomorrowWeatherCode() throws WeatherForecastException{
        refresh();
        return tomorrowWeatherCode;
    }

    @Override
    public int getTomorrowMinTemperature() throws WeatherForecastException{
        refresh();
        return tomorrowMinTemperature;
    }

    @Override
    public int getTomorrowMaxTemperature() throws WeatherForecastException{
        refresh();
        return tomorrowMaxTemperature;
    }

    private void refresh() throws WeatherForecastException {
        logger.debug("refreshing weather data for "+currentLocation);

        CurrentWeather weather = weatherService.getCurrentWeather(currentLocation);
        DayForecast forecast = weatherService.getForecast(currentLocation).get(1);

        currentWeatherCode = weather.getWeatherCode();
        currentTemperature = weather.getTemperature();
        sunrise.setTime(weather.getSunrise());
        sunset.setTime(weather.getSunset());

        tomorrowWeatherCode = forecast.getCode();
        tomorrowMinTemperature = forecast.getMin();
        tomorrowMaxTemperature = forecast.getMax();

        if(alarmSunrise>=0) {
            clock.unregisterAlarm(alarmSunrise);
        }

        if(alarmSunset>=0) {
            clock.unregisterAlarm(alarmSunset);
        }

        alarmSunrise = clock.registerAlarm(sunrise,this);
        alarmSunset = clock.registerAlarm(sunset,this);

    }

    @Override
    public void alarmEventFired(int alarmEventId) {
        if(alarmEventId == alarmSunrise) {
            logger.debug("Alarm (clock) event : sunrise, eventId = "+alarmEventId);
            alarmSunrise = -1;
            fireDaylightNotificationMsg(true);
        } else if(alarmEventId == alarmSunset) {
            logger.debug("Alarm (clock) event : sunset, eventId = "+alarmEventId);
            alarmSunset = -1;
            fireDaylightNotificationMsg(false);
        } else {
            logger.warn("Unknown clock event (not sunrise nor sunset) : "+alarmEventId);
        }
        try {
            refresh();
        } catch (WeatherForecastException exc) {
            logger.warn("Exception occured following alarm event "+exc.getStackTrace());
        }
    }
}
