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
package appsgate.lig.meteo.yahoo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;


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

    private String WOEID = "woeid";

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.meteo.YahooGeoPlanet#GetWOEIDFromPlaceName(java.lang.String)
     */
    public String getWOEIDFromPlaceName(String placeName) {
	logger.fine("GetWOEIDFromPlaceName(String placeName : "
		+ placeName + ")");
	String currentWOEID = null;

	try {
	    URL url = new URL(String.format(geoPlanetURL + queryPlaceWOEID,
		    placeName, appID));
	    logger.fine("GetWOEIDFromPlaceName(...), query URL : " + url);

	    XMLEventReader reader = XMLInputFactory.newInstance()
		    .createXMLEventReader(url.openStream());

	    while (currentWOEID == null && reader.hasNext()) {
		XMLEvent evt = reader.nextEvent();
		if (evt.isStartElement()
			&& evt.asStartElement().getName().getLocalPart()
				.equals(WOEID))
		    currentWOEID = reader.nextEvent().asCharacters().toString();
	    }
	    logger.info("GetWOEIDFromPlaceName(...), found WOEID : "
		    + currentWOEID);
	    reader.close();

	} catch (Exception exc) {
	    logger.warning(exc.getMessage());
	}

	return currentWOEID;
    }
}
