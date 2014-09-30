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
 * YahooGeoPlanet.java - 15 juil. 2013
 */
package appsgate.lig.yahoo.geoplanet;

import org.json.JSONObject;

/**
 * Java Interface to use Yahoo GeoPlanet WebService API
 * @see <a href="http://developer.yahoo.com/geo/geoplanet/">Geo Planet</a>
 * @author thibaud
 *
 */
public interface YahooGeoPlanet {
    
    /**
     * Retrieving the Most Likely Place for a Given Place Name :
     * "http://where.yahooapis.com/v1/places.q(placeName)?appid=[YahooAppID]"
     * @see <a href="http://developer.yahoo.com/geo/geoplanet/guide/api-reference.html">GeoPlanet API</a>
     * @param placeName is a Human friendly place name
     * @return the most likely valid WOEID (Where On Earth ID) - to be used by Yahoo services 
     */
    public String getWOEIDFromPlaceName(String placeName);
    
    /**
     * Retrieving Information about the Most Likely Place for a Given Place Name :
     * "http://where.yahooapis.com/v1/places.q(placeName)?format=json&appid=[YahooAppID]"
     * @see <a href="http://developer.yahoo.com/geo/geoplanet/guide/api-reference.html">GeoPlanet API</a>
     * @param placeName is a Human friendly place name
     * @return JSON Description of the place name 
     */
    public JSONObject getDescriptionFromPlaceName(String placeName);
    

}
