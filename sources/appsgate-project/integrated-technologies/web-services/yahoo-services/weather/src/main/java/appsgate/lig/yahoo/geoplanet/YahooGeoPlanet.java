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

import org.json.JSONArray;
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
    
    /**
     * Retrieving Information a particular WOEID (Where On Earth Identifier from Yahoo) :
     * "http://where.yahooapis.com/v1/places.woeid(WOEID)?format=json&appid=[YahooAppID]"
     * @see <a href="http://developer.yahoo.com/geo/geoplanet/guide/api-reference.html">GeoPlanet API</a>
     * @param WOEID  is the Where On Earth IDentifier from Yahoo
     * @return JSON Description of the place name 
     */
    public JSONObject getDescriptionFromWOEID(String woeid);
        
    /**
     * Check a location upon the first letters of its place name
     *
     * @param location
     *            A human place name : a town, a country, a particular place or point of interest (poi)
     * @return A JSONArray with 0..5  objects describing the location (formatted as follow), example starting with Gre
     * [
     * {"locality1":"Grenoble","woeid":"593720","name":"Grenoble","placeTypeName":"Town","country":"France"},
     * {"locality1":"Green Bay","woeid":"2413753","name":"Green Bay","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greenville","woeid":"2414583","name":"Greenville","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greensboro","woeid":"2414469","name":"Greensboro","placeTypeName":"Town","country":"États-Unis"},
     * {"locality1":"Greifswald","woeid":"654035","name":"Greifswald","placeTypeName":"Town","country":"Allemagne"}
     * ]
     */
    public JSONArray getLocationsStartingWith(String firstLetters);    
    

}
