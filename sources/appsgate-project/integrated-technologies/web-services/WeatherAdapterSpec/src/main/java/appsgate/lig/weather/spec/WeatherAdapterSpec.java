package appsgate.lig.weather.spec;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.utils.CurrentWeather;
import appsgate.lig.weather.utils.DayForecast;

/**
 * Interface for weather forecast service
 * 
 * @author thibaud
 * 
 */
public interface WeatherAdapterSpec {
    
    /**
     * Try add the place with an human friendly name to fetch Weather conditions
     * 
     * @param location
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     */
    public void addLocationObserver(String location);
    
    /**
     * Try to remove a place previously added
     * 
     * @param location the placeName as it was previously added
     * @return true if the place was found and was successfully removed
     */
    public void removeLocationObserver(String location);

    public Set<String> getActiveLocationsObservers();

    public Set<String> getAllLocationsObservers();





} 