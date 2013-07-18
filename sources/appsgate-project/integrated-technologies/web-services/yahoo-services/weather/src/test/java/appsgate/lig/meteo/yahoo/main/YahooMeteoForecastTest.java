package appsgate.lig.meteo.yahoo.main;

import appsgate.lig.weather.DayForecast;
import appsgate.lig.weather.WeatherForecast;
import appsgate.lig.weather.WeatherForecastException;
import appsgate.lig.weather.yahoo.YahooWeatherImpl;

public class YahooMeteoForecastTest {

    WeatherForecast meteo;

    public void start() {
	try {
	    WeatherForecast meo = meteo.fetch();

	    System.out.println(meteo.getCurrentLocation());
	    for (DayForecast pre : meteo.getForecast()) {
		System.out.println(pre.toString());
	    }

	} catch (WeatherForecastException exc) {
	    exc.printStackTrace();
	}

    }

    public void stop() {

    }

    /**
     * Main method to test statically without displaying this instance
     * 
     * @param args
     */

    public static void main(String[] args) {

	YahooWeatherImpl meteo = new YahooWeatherImpl("593720");// 12724717

	meteo.start();

	System.out.println(meteo);
	try {
	    System.out.println(meteo.getCurrentLocation());
	    for (DayForecast pre : meteo.getForecast()) {
		System.out.println(pre.toString());
	    }
	} catch (WeatherForecastException exc) {
	    exc.printStackTrace();
	}

    }

}
