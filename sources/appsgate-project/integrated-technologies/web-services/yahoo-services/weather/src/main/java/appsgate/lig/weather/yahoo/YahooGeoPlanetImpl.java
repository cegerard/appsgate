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
 * YahooGeoPlanetImpl.java - 15 juil. 2013
 */
package appsgate.lig.weather.yahoo;

import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;


/**
 * @author thibaud
 * 
 */
public class YahooGeoPlanetImpl implements YahooGeoPlanet {
    
    private Logger logger = Logger.getLogger(YahooGeoPlanetImpl.class
		.getSimpleName());


    private String geoPlanetURL = "http://where.yahooapis.com/v1/";
    private String queryPlaceWOEID = "places.q(%s)?appid=%s";
    /**
     * Valid Yahoo application ID, registered by Thibaud Flury
     * thibaud_lig@yahoo.com -> for the purpose of AppsGate
     */
    private String appID = "LIolTLV34H_riBa5bYkTDWrCutm0j7Ta6Nvzfh1wOp5tBktKEXzSOsKznKPqFzE9Nw-";

    
    static final String xPathWOEID = "//woeid";

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.weather.YahooGeoPlanet#GetWOEIDFromPlaceName(java.lang.String)
     */
    public String getWOEIDFromPlaceName(String placeName) {
	System.out.println("GetWOEIDFromPlaceName(String placeName : "
		+ placeName + ")");
	String currentWOEID = null;

	try {
	    URL url = new URL(String.format(geoPlanetURL + queryPlaceWOEID,
		    placeName, appID));
	    System.out.println("GetWOEIDFromPlaceName(...), query URL : " + url);
	    
		DocumentBuilder db = null;
		db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(url.openStream());
		XPath xPath = XPathFactory.newInstance().newXPath();
		currentWOEID = xPath.evaluate(xPathWOEID, doc);
	    System.out.println("GetWOEIDFromPlaceName(...), found WOEID : "
		    + currentWOEID);

	} catch (Exception exc) {
	    exc.printStackTrace();
	}

	return currentWOEID;
    }
}
