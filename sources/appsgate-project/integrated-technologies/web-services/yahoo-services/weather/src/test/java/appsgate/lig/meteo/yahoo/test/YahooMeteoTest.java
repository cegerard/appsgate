package appsgate.lig.meteo.yahoo.test;

import appsgate.lig.weather.spec.WeatherAdapterSpec;
import appsgate.lig.weather.yahoo.YahooWeatherImpl;

public class YahooMeteoTest {

    WeatherAdapterSpec meteo;
	
	    public void start() {
//		try {
//		    WeatherForecast meo = meteo.fetch();
//
//		    System.out.println(meteo.getCurrentLocation());
//		    for (DayForecast pre : meteo.getForecast()) {
//			System.out.println(pre.toString());
//		    }
//
//		} catch (WeatherForecastException exc) {
//		    exc.printStackTrace();
//		}
//
	    }

	    public void stop() {

	    }

	    /**
	     * Main method to test statically without displaying this instance
	     * 
	     * @param args
	     */

	    public static void main(String[] args) {

		YahooWeatherImpl meteo = new YahooWeatherImpl();// 12724717 "593720"

		meteo.start();

		System.out.println(meteo);
		try {
		    meteo.addLocation("Grenoble");
		    meteo.addLocation("New York");
		    meteo.fetch();
		    meteo.fetch();
		    
		    System.out.println(meteo);
		} catch (Exception exc) {
		    exc.printStackTrace();
		}

	    }

	
}
