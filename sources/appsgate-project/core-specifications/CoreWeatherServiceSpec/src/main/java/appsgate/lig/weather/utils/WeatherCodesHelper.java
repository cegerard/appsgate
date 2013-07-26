/**
 * Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * YahooMeteoCodesHelper.java - 15 juil. 2013
 */
package appsgate.lig.weather.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * set of codes used by Yahoo to describes weather and their meanings (English String) 
 * @author thibaud
 *
 */
public class WeatherCodesHelper {
    
	private static Map <Integer, String> yahooWeatherCodes;
	static
	{
	    yahooWeatherCodes = new HashMap<Integer, String>(50);
	    yahooWeatherCodes.put(0, "tornado");
	    yahooWeatherCodes.put(1,"tropical storm");
	    yahooWeatherCodes.put(2,"hurricane");
	    yahooWeatherCodes.put(3,"severe thunderstorms");
	    yahooWeatherCodes.put(4,"thunderstorms");
	    yahooWeatherCodes.put(5,"mixed rain and snow");
	    yahooWeatherCodes.put(6,"mixed rain and sleet");
	    yahooWeatherCodes.put(7,"mixed snow and sleet");
	    yahooWeatherCodes.put(8,"freezing drizzle");
	    yahooWeatherCodes.put(9,"drizzle");
	    yahooWeatherCodes.put(10,"freezing rain");
	    yahooWeatherCodes.put(11,"showers");
	    yahooWeatherCodes.put(12,"showers");
	    yahooWeatherCodes.put(13,"snow flurries");
	    yahooWeatherCodes.put(14,"light snow showers");
	    yahooWeatherCodes.put(15,"blowing snow");
	    yahooWeatherCodes.put(16,"snow");
	    yahooWeatherCodes.put(17,"hail");
	    yahooWeatherCodes.put(18,"sleet");
	    yahooWeatherCodes.put(19,"dust");
	    yahooWeatherCodes.put(20,"foggy");
	    yahooWeatherCodes.put(21,"haze");
	    yahooWeatherCodes.put(22,"smoky");
	    yahooWeatherCodes.put(23,"blustery");
	    yahooWeatherCodes.put(24,"windy");
	    yahooWeatherCodes.put(25,"cold");
	    yahooWeatherCodes.put(26,"cloudy");
	    yahooWeatherCodes.put(27,"mostly cloudy (night)");
	    yahooWeatherCodes.put(28,"mostly cloudy (day)");
	    yahooWeatherCodes.put(29,"partly cloudy (night)");
	    yahooWeatherCodes.put(30,"partly cloudy (day)");
	    yahooWeatherCodes.put(31,"clear (night)");
	    yahooWeatherCodes.put(32,"sunny");
	    yahooWeatherCodes.put(33,"fair (night)");
	    yahooWeatherCodes.put(34,"fair (day)");
	    yahooWeatherCodes.put(35,"mixed rain and hail");
	    yahooWeatherCodes.put(36,"hot");
	    yahooWeatherCodes.put(37,"isolated thunderstorms");
	    yahooWeatherCodes.put(38,"scattered thunderstorms");
	    yahooWeatherCodes.put(39,"scattered thunderstorms");
	    yahooWeatherCodes.put(40,"scattered showers");
	    yahooWeatherCodes.put(41,"heavy snow");
	    yahooWeatherCodes.put(42,"scattered snow showers");
	    yahooWeatherCodes.put(43,"heavy snow");
	    yahooWeatherCodes.put(44,"partly cloudy");
	    yahooWeatherCodes.put(45,"thundershowers");
	    yahooWeatherCodes.put(46,"snow showers");
	    yahooWeatherCodes.put(47,"isolated thundershowers");
	    yahooWeatherCodes.put(3200,"not available");	    
	}
	
	public static String getDescription(int weatherCode) {
	    if(yahooWeatherCodes.containsKey(new Integer(weatherCode))) {
		return (String)yahooWeatherCodes.get(new Integer(weatherCode));
	    }
	    else return null;
	}

/*
 * Yahoo WeatherForecast Codes
 * from http://developer.yahoo.com/weather/#req	
	    0 	tornado
	    1 	tropical storm
	    2 	hurricane
	    3 	severe thunderstorms
	    4 	thunderstorms
	    5 	mixed rain and snow
	    6 	mixed rain and sleet
	    7 	mixed snow and sleet
	    8 	freezing drizzle
	    9 	drizzle
	    10 	freezing rain
	    11 	showers
	    12 	showers
	    13 	snow flurries
	    14 	light snow showers
	    15 	blowing snow
	    16 	snow
	    17 	hail
	    18 	sleet
	    19 	dust
	    20 	foggy
	    21 	haze
	    22 	smoky
	    23 	blustery
	    24 	windy
	    25 	cold
	    26 	cloudy
	    27 	mostly cloudy (night)
	    28 	mostly cloudy (day)
	    29 	partly cloudy (night)
	    30 	partly cloudy (day)
	    31 	clear (night)
	    32 	sunny
	    33 	fair (night)
	    34 	fair (day)
	    35 	mixed rain and hail
	    36 	hot
	    37 	isolated thunderstorms
	    38 	scattered thunderstorms
	    39 	scattered thunderstorms
	    40 	scattered showers
	    41 	heavy snow
	    42 	scattered snow showers
	    43 	heavy snow
	    44 	partly cloudy
	    45 	thundershowers
	    46 	snow showers
	    47 	isolated thundershowers
	    3200 	not available
	    */

	
    

}
