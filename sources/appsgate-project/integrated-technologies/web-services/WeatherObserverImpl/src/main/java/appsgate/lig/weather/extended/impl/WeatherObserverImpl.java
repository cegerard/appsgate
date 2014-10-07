package appsgate.lig.weather.extended.impl;

import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.AbstractObjectSpec;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;
import appsgate.lig.weather.utils.SimplifiedWeatherCodesHelper;
import appsgate.lig.yahoo.weather.YahooWeather;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;

/**
 * Created by thibaud on 01/07/2014.
 */
public class WeatherObserverImpl extends AbstractObjectSpec implements ExtendedWeatherObserver, AlarmEventObserver, CoreObjectSpec{

    protected String appsgatePictureId;

    protected String appsgateUserType;
    protected String appsgateDeviceStatus;
    protected String appsgateObjectId;
    protected String appsgateServiceName;

    public final static String OBJECTID_PREFIX= "WeatherObserver-";

    protected CORE_TYPE appsgateCoreType;



    private String currentLocation;
    private boolean currentlyDaylight;
    private Calendar sunrise = Calendar.getInstance();
    private Calendar sunset = Calendar.getInstance();

    private CurrentWeather currentWeather;
    private List<DayForecast> forecasts;

    private Calendar lastPublicationdate;
    
    private String currentPresentationURL;
    private String currentWoeid;


    private long lastFetch = -1;
    private Timer timer;

    private YahooWeather weatherService;
    private CoreClockSpec clock;

    private int alarmSunset=-1;
    private int alarmSunrise=-1;

    public static final String IMPL_NAME = "WeatherObserverImpl";

    private Object lock;

    public WeatherObserverImpl() {
        lock = new Object();
        super.appsgateUserType = "103"; // 103 stands for weather forecast service
        super.appsgateDeviceStatus = "2"; // 2 means device paired (for a device, not
        // relevant for service)

        super.appsgateServiceName = "Yahoo Weather Forecast";
        super.appsgateCoreType = CORE_TYPE.SERVICE;
    }

    public void start() throws WeatherForecastException {
        if(currentLocation != null
                && currentLocation.length()> 0) {
        	if( weatherService == null) {
        		throw new WeatherForecastException("No weather service available, aborting");
        	}
            weatherService.addLocation(currentLocation);
        } else {
            throw new WeatherForecastException("Trying to create a WeatherObserver with an empty or null location");
        }


    }

    @Override
    public String getAbstractObjectId() {
        return appsgateObjectId;
    }

    /**
     * Helper class to simulate a WeatherObserver if not created using apam
     * @param location
     * @return
     */
    public static String getFakeObjectId(String location) {
        return OBJECTID_PREFIX+location;
    }

    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();

        // mandatory appsgate properties
        descr.put("id", appsgateObjectId);
        // mandatory appsgate properties
        descr.put("id", appsgateObjectId);
        descr.put("type", appsgateUserType);
        descr.put("status", appsgateDeviceStatus);
        descr.put("woeid", currentWoeid);
        descr.put("presentationURL", currentPresentationURL);

        descr.put("pictureId", appsgatePictureId);
        try {

            descr.put("location", getCurrentLocation());
            descr.put("temperature", getCurrentTemperature());
            descr.put("trend", getCurrentWeatherCode());


        } catch(WeatherForecastException exc ) {
            logger.warn("Error during get description" + exc.getMessage());
            return descr;
        }
        return descr;
    }

    private static Logger logger = LoggerFactory
            .getLogger(WeatherObserverImpl.class);

    private NotificationMsg fireDaylightNotificationMsg(boolean isDayLight) {
        if(isDayLight) {
            return fireWeatherNotificationMsg("daylightEvent", "sunset", "sunrise");
        } else {
            return fireWeatherNotificationMsg("daylightEvent", "sunrise", "sunset");
        }
    }

    /**
     * Check associated Simplified code and if they are different fire the NotificationMsg
     * @param varName
     * @param oldCode
     * @param newCode
     * @return
     */
    private NotificationMsg fireWeatherCodeNotificationMsgIfNew(String varName, int oldCode, int newCode) {
        if(SimplifiedWeatherCodesHelper.getSimplified(oldCode) != SimplifiedWeatherCodesHelper.getSimplified(newCode)) {
            return fireWeatherNotificationMsg(varName, String.valueOf(SimplifiedWeatherCodesHelper.getSimplified(oldCode)),
                    String.valueOf(SimplifiedWeatherCodesHelper.getSimplified(newCode)));
        }
        return null;
    }

    private NotificationMsg fireWeatherTempNotificationMsgIfNew(String varName, int oldTemp, int newTemp) {
        if(oldTemp != newTemp) {
            return fireWeatherNotificationMsg(varName, String.valueOf(oldTemp),
                    String.valueOf(newTemp));
        }
        return null;
    }

    private NotificationMsg fireWeatherNotificationMsg(String varName, String oldValue, String newValue) {
        return new CoreNotificationMsg(varName, oldValue, newValue, this);
    }
    @Override
    public String getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public boolean isCurrentlyDaylight()throws WeatherForecastException  {
    	if(clock == null) {
    		throw new WeatherForecastException("No clock service bound, cannot determine daylight");
    	}
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
    public boolean isCurrentWeatherCode(int simpleWeatherCode) throws WeatherForecastException {
        refresh();
        return (SimplifiedWeatherCodesHelper.getSimplified(currentWeather.getWeatherCode()) == simpleWeatherCode);

    }

    @Override
    public int getCurrentWeatherCode() throws WeatherForecastException{
        refresh();
        return SimplifiedWeatherCodesHelper.getSimplified(currentWeather.getWeatherCode());
    }

    public String getCurrentWeatherString() throws WeatherForecastException{
        refresh();
        return SimplifiedWeatherCodesHelper.getDescription(getCurrentWeatherCode());
    }
    
    @Override
    public int getCurrentTemperature()throws WeatherForecastException {
        refresh();
        return currentWeather.getTemperature();
    }

    @Override
    public boolean isForecastWeatherCode(int dayForecast, int simpleWeatherCode) throws WeatherForecastException {
        return (getForecastWeatherCode(dayForecast)==simpleWeatherCode);
    }

    private void testDayForecast(int dayForecast) throws WeatherForecastException {
        if(dayForecast< 0 || forecasts == null || dayForecast >= forecasts.size()) {
            throw new WeatherForecastException("dayForecast not available : " + dayForecast);
        }
    }

    @Override
    public int getForecastWeatherCode(int dayForecast) throws WeatherForecastException {
        refresh();
        testDayForecast(dayForecast); // might throw exception
        return forecasts.get(dayForecast).getCode();
    }
    
    public String getForecastWeatherString(int dayForecast) throws WeatherForecastException{
        refresh();
        return SimplifiedWeatherCodesHelper.getDescription(SimplifiedWeatherCodesHelper.getSimplified(getForecastWeatherCode(dayForecast)));
    }

    @Override
    public int getForecastMinTemperature(int dayForecast) throws WeatherForecastException {
        refresh();
        testDayForecast(dayForecast); // might throw exception
        return forecasts.get(dayForecast).getMin();
    }

    @Override
    public int getForecastMaxTemperature(int dayForecast) throws WeatherForecastException {
        refresh();
        testDayForecast(dayForecast); // might throw exception
        return forecasts.get(dayForecast).getMax();
    }

    public void refresh() throws WeatherForecastException {
        synchronized (lock) {
        	
        	if(clock == null) {
        		throw new WeatherForecastException("No clock service bound, cannot refresh");
        	}


            if (weatherService != null
                    && (lastFetch < 0 || System.currentTimeMillis() - lastFetch > 10000)
                    && (lastPublicationdate == null || !lastPublicationdate.equals(weatherService.getPublicationDate(currentLocation))
            )) {
                logger.trace("refreshing weather data for "+currentLocation);


                checkAndUpdateCurrentWeather(weatherService.getCurrentWeather(currentLocation));
                checkAndUpdateForecasts(weatherService.getForecast(currentLocation));

                sunrise.setTime(currentWeather.getSunrise());
                sunset.setTime(currentWeather.getSunset());


                if (alarmSunrise >= 0) {
                    clock.unregisterAlarm(alarmSunrise);
                }

                if (alarmSunset >= 0) {
                    clock.unregisterAlarm(alarmSunset);
                }

                alarmSunrise = clock.registerAlarm(sunrise, this);
                alarmSunset = clock.registerAlarm(sunset, this);

                lastFetch = System.currentTimeMillis();

                lastPublicationdate = weatherService.getPublicationDate(currentLocation);
                if(currentWoeid == null ) { // should not change, so we get it only once
                	currentWoeid = weatherService.getWOEID(currentLocation);
                }
                
                currentPresentationURL = weatherService.getPresentationURL(currentLocation);
                
            }

            WeatherRefreshTask nextRefresh = new WeatherRefreshTask(this);
            if (timer != null)
                timer.cancel();
            timer = new Timer();
            timer.schedule(nextRefresh, 10 * 60 * 1000); // One auto refresh each 10 minutes
        }
    }

    private void checkAndUpdateCurrentWeather(CurrentWeather newWeather) {
        if (newWeather != null) {
            fireWeatherCodeNotificationMsgIfNew(CurrentWeather.CODE,
                        (currentWeather == null ? -1 : currentWeather.getWeatherCode()),
                        newWeather.getWeatherCode());

            fireWeatherTempNotificationMsgIfNew(CurrentWeather.TEMPERATURE,
                        (currentWeather == null ? -999 : currentWeather.getTemperature()),
                        newWeather.getTemperature());
            currentWeather = newWeather;
        }
    }

    private void checkAndUpdateForecasts(List<DayForecast> newForecasts) {
        if (newForecasts != null && newForecasts.size()>0) {
            int i = 0;
            DayForecast oldForecast;
            for(DayForecast newForecast: newForecasts) {
                if(forecasts !=null && i<forecasts.size()) {
                    oldForecast = forecasts.get(i);
                } else {
                    oldForecast = null;
                }

                fireWeatherCodeNotificationMsgIfNew(DayForecast.FORECAST+CurrentWeather.CODE+i,
                            (oldForecast == null ? -1 : oldForecast.getCode()),
                            newForecast.getCode());

                fireWeatherTempNotificationMsgIfNew(DayForecast.FORECAST+DayForecast.MIN+i,
                        (oldForecast == null ? -1 : oldForecast.getMin()),
                        newForecast.getMin());

                fireWeatherTempNotificationMsgIfNew(DayForecast.FORECAST+DayForecast.MAX+i,
                        (oldForecast == null ? -1 : oldForecast.getMax()),
                        newForecast.getMax());
                i++;
            }
            forecasts=newForecasts;
        }
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

	@Override
	public String getCurrentWOEID() {
        logger.trace("getCurrentWOEID()"+currentWoeid);
		return currentWoeid;
	}

	@Override
	public String getPresentationURL() {
        logger.trace("getPresentationURL(), returning "+currentPresentationURL);

		return currentPresentationURL;
	}
}
