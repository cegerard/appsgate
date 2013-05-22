package appsgate.lig.meteo.yahoo;

import appsgate.lig.meteo.DayForecast;
import appsgate.lig.meteo.Meteo;

public class RSSReader {

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
	 * Main method to test statically without deplaying this instance
	 * @param args
	 */
	
	public static void main(String[] args) {

		YahooMeteoImplementation meteo = new YahooMeteoImplementation("593720");//12724717
		meteo.start();
		
		System.out.println(meteo.getLocation());
		for (DayForecast pre : meteo.getForecast()) {
			System.out.println(pre.toString());
		}

	}
	
}
