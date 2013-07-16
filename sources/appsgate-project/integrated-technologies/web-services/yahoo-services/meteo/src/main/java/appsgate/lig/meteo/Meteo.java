package appsgate.lig.meteo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Metheorological data containing a set of DayForecast
 * 
 * @author jnascimento
 * 
 */
public interface Meteo {

    /**
     * @return The current defined location for the weather (Human Readable)
     */
    String getLocation();

    /**
     * Try to resolve the place with an human friendly name
     * 
     * @param placeName
     *            A human place name (a town, a country, a particular place)
     */
    void setLocation(String placeName);

    int getCurrentTemperature();

    int getCurrentWeatherCode();

    Calendar getDatePublication();

    Calendar getLastFetch();

    Meteo fetch();

    List<DayForecast> getForecast();

}
