package appsgate.lig.meteo;

import java.util.Calendar;
import java.util.List;


/**
 * Represents a Metheorological data containing a set of DayForecast
 * @author jnascimento
 *
 */
public interface Meteo {
	
	String getLocation();
	
	Integer getCurrentTemperature();
	
	Calendar getDatePublication();
	
	Calendar getLastFetch();
	
	Meteo fetch();
	
	List<DayForecast> getForecast();
	
}
