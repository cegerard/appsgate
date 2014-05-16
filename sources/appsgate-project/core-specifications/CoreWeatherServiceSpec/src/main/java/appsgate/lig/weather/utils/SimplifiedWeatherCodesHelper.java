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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * set of codes used by appsgate to describes weather and their meanings
 * (English String, simplified version of yahoo weather codes)
 * 
 * @author thibaud
 * 
 */
public class SimplifiedWeatherCodesHelper {

	private static Map<Integer, Set<Integer>> simplifiedWeatherCodeSets;
	private static Map<Integer, String> simplifiedWeatherCodeMeanings;

	static {
		simplifiedWeatherCodeMeanings = new HashMap<Integer, String>(8);
		simplifiedWeatherCodeSets = new HashMap<Integer, Set<Integer>>(8);

		simplifiedWeatherCodeMeanings.put(7, "special");
		simplifiedWeatherCodeSets.put(7,
				new HashSet<Integer>(Arrays.asList(0, 1, 2,19, 3200)));

		simplifiedWeatherCodeMeanings.put(0, "sunny");
		simplifiedWeatherCodeSets.put(0,
				new HashSet<Integer>(Arrays.asList(31, 32, 33, 34)));

		simplifiedWeatherCodeMeanings.put(1, "cloudy");
		simplifiedWeatherCodeSets.put(1,
				new HashSet<Integer>(Arrays.asList(26, 27, 28, 29, 30, 44)));

		simplifiedWeatherCodeMeanings.put(2, "rainy");
		simplifiedWeatherCodeSets.put(2,
				new HashSet<Integer>(Arrays.asList(5, 6, 7, 8 , 9, 10, 11, 12, 17, 18, 35, 40)));

		simplifiedWeatherCodeMeanings.put(3, "snowy");
		simplifiedWeatherCodeSets.put(
				3,
				new HashSet<Integer>(Arrays.asList(13, 14, 15, 16, 41, 42, 43,
						46)));

		simplifiedWeatherCodeMeanings.put(4, "thunder");
		simplifiedWeatherCodeSets.put(4,
				new HashSet<Integer>(Arrays.asList(3, 4, 37, 38, 39, 45, 47)));
		
		simplifiedWeatherCodeMeanings.put(5, "foggy");
		simplifiedWeatherCodeSets.put(5,
				new HashSet<Integer>(Arrays.asList(20, 21, 22)));		

		simplifiedWeatherCodeMeanings.put(6, "other");
		simplifiedWeatherCodeSets.put(6,
				new HashSet<Integer>(Arrays.asList(23, 24, 15, 36)));	

	}

	public static String getDescription(int simplifiedWeatherCode) {
		if (simplifiedWeatherCodeMeanings.containsKey(new Integer(simplifiedWeatherCode))) {
			return (String) simplifiedWeatherCodeMeanings.get(new Integer(simplifiedWeatherCode));
		} else
			return null;
	}
	
	public static boolean contains(int simplifiedWeatherCode, int yahooWeatherCode) {
		if (simplifiedWeatherCodeSets.containsKey(new Integer(simplifiedWeatherCode))) {
			return simplifiedWeatherCodeSets.get(new Integer(simplifiedWeatherCode)).contains(new Integer(yahooWeatherCode));
		} 		
		return false;
	}		
	
	public static int getSimplified(int yahooWeatherCode) {
		for(Integer simpleCode : simplifiedWeatherCodeMeanings.keySet() ) {
			if (SimplifiedWeatherCodesHelper.contains(simpleCode.intValue(), yahooWeatherCode)) {
				return simpleCode.intValue();
			}
		}
		return -1;
	}	


}
