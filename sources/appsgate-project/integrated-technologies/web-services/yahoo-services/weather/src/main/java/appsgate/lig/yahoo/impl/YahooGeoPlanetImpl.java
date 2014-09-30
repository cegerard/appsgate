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
package appsgate.lig.yahoo.impl;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import appsgate.lig.yahoo.geoplanet.YahooGeoPlanet;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * @author thibaud
 * 
 */
public class YahooGeoPlanetImpl implements YahooGeoPlanet {

	private static Logger logger = LoggerFactory
			.getLogger(YahooGeoPlanetImpl.class);	

	private String geoPlanetURL = "http://where.yahooapis.com/v1/";
	private String queryPlaceWOEID = "places.q(%s)?appid=%s";

	/**
	 * Valid Yahoo application ID, registered by Thibaud Flury
	 * thibaud_lig@yahoo.com -> for the purpose of AppsGate
	 */
	private String appID = "LIolTLV34H_riBa5bYkTDWrCutm0j7Ta6Nvzfh1wOp5tBktKEXzSOsKznKPqFzE9Nw-";

	static final String xPathSep = "//";

	public static final String WOEID = "woeid";
	public static final String PLACETYPENAME = "placeTypeName";
	public static final String NAME = "name";
	public static final String COUNTRY = "country";
	public static final String TOWN = "locality1";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.weather.YahooGeoPlanet#GetWOEIDFromPlaceName(java.lang.String)
	 */
	public String getWOEIDFromPlaceName(String placeName) {
		logger.trace("GetWOEIDFromPlaceName(String placeName : "
				+ placeName + ")");
		String currentWOEID = null;

		try {
			URL url = new URL(String.format(geoPlanetURL + queryPlaceWOEID,
					placeName, appID));

			logger.trace("GetWOEIDFromPlaceName(...), query URL : " + url);

			DocumentBuilder db = null;
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(url.openStream());
			XPath xPath = XPathFactory.newInstance().newXPath();
			currentWOEID = xPath.evaluate(xPathSep+WOEID, doc);
			logger.trace("GetWOEIDFromPlaceName(...), found WOEID : "
					+ currentWOEID);

		} catch (Exception exc) {
			logger.error("Error when fetching Yahoo GeoPlanet WebService : "+exc.getMessage());
		}

		return currentWOEID;
	}

	@Override
	public JSONObject getDescriptionFromPlaceName(String placeName) {
		logger.trace("getDescriptionFromPlaceName(String placeName : "
				+ placeName + ")");		
		JSONObject obj = new JSONObject();
		try {
			URL url = new URL(String.format(geoPlanetURL + queryPlaceWOEID,
					placeName, appID));
			logger.trace("getDescriptionFromPlaceName(...), query URL : " + url);


			DocumentBuilder db = null;
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(url.openStream());
			XPath xPath = XPathFactory.newInstance().newXPath();

			String currentWOEID = xPath.evaluate(xPathSep+WOEID, doc);
			logger.trace("getDescriptionFromPlaceName(...), found woeid : "
					+ currentWOEID);

			String currentPlaceTypeName = xPath.evaluate(xPathSep+PLACETYPENAME, doc);
			logger.trace("getDescriptionFromPlaceName(...), found placeTypeName : "
					+ currentPlaceTypeName);

			String currentName = xPath.evaluate(xPathSep+NAME, doc);
			logger.trace("getDescriptionFromPlaceName(...), found placeTypeName : "
					+ currentName);

			String currentCountry = xPath.evaluate(xPathSep+COUNTRY, doc);
			logger.trace("getDescriptionFromPlaceName(...), found placeTypeName : "
					+ currentCountry);

			String currentTown = xPath.evaluate(xPathSep+TOWN, doc);
			logger.trace("getDescriptionFromPlaceName(...), found locality1 : "
					+ currentTown);

			if(currentWOEID!= null && currentWOEID.length()>0)
				obj.put(WOEID, currentWOEID);
			if(currentPlaceTypeName!= null && currentPlaceTypeName.length()>0)
				obj.put(PLACETYPENAME, currentPlaceTypeName);
			if(currentCountry!= null && currentCountry.length()>0)
				obj.put(COUNTRY, currentCountry);
			if(currentName!= null && currentName.length()>0)			
				obj.put(NAME, currentName);
			if(currentTown!= null && currentTown.length()>0)			
				obj.put(TOWN, currentTown);			

		} catch (Exception exc) {
			logger.error("Error when fetching Yahoo GeoPlanet WebService : "+exc.getMessage());
		}
		logger.trace("getDescriptionFromPlaceName(...), returning "+obj.toString());
		return obj;
	}


}
