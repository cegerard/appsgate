package appsgate.lig.meteo.yahoo.main;

import appsgate.lig.meteo.DayForecast;
import appsgate.lig.meteo.Meteo;
import appsgate.lig.meteo.yahoo.YahooMeteoImplementation;

public class YahooMeteoTest {

	Meteo meteo;
	
	public void start(){
		
		Meteo meo=meteo.fetch();
		
		System.out.println(meteo.getLocation());
		for (DayForecast pre : meteo.getForecast()) {
			System.out.println(pre.toString());
		}
		
	}
	
	public void stop(){
		
	}	

	/**
	 * Main method to test statically without displaying this instance
	 * @param args
	 */
	
	public static void main(String[] args) {

		YahooMeteoImplementation meteo = new YahooMeteoImplementation("593720");//12724717

		meteo.start();
		
		System.out.println(meteo);
		
		System.out.println(meteo.getLocation());
		for (DayForecast pre : meteo.getForecast()) {
			System.out.println(pre.toString());
		}

	}
	
}
