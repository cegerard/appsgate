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
 * CurrentWeather.java - 25 juil. 2013
 */
package appsgate.lig.weather.utils;


/**
 * Helper class to represents current weather information
 * @author thibaud
 *
 */
public class CurrentWeather {
    
    int currentTemperature;
    int currentCode;
    int currentWindDirection;
    float currentWindSpeed;
    
    /**
     * @param currentTemperature
     * @param currentCode
     * @param currentWindDirection
     * @param currentWindSpeed
     * @param currentUnit
     */
    public CurrentWeather(int currentTemperature, int currentCode,
	    int currentWindDirection, float currentWindSpeed) {
	super();
	this.currentTemperature = currentTemperature;
	this.currentCode = currentCode;
	this.currentWindDirection = currentWindDirection;
	this.currentWindSpeed = currentWindSpeed;
    }    
    
        
    /**
     * @return The current temperature (accordingly to current temperature unit °C or °F)s
     */
    public int getTemperature() {
	return currentTemperature;
    }

    /**
     * @return The code describing current weather condition 
     * @see appsgate.lig.weather.utils.WeatherCodesHelper
     */
    public int getWeatherCode() {
	return currentCode;
    }

    /**
     * @return The current wind speed (accordingly to current unit mph or km/h)
     */
    public float getWindSpeed() {
	return currentWindSpeed;
    }

    /**
     * @return The current wind direction (in degree)
     */
    public int getWindDirection() {
	return currentWindDirection;
    }
    
    @Override
    public String toString() {

	StringBuffer sb = new StringBuffer();
	sb.append(String.format("current temperature %s \n",
		currentTemperature));
	sb.append(String.format("current code %s \n",
		currentCode));
	sb.append(String.format("current wind direction %d \n",
		currentWindDirection));
	sb.append(String.format("current wind speed %f \n",
		currentWindSpeed));
	return sb.toString();
    }


    

}
