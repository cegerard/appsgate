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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import appsgate.lig.yahoo.geoplanet.YahooGeoPlanet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author thibaud
 */
public class YahooGeoPlanetImpl implements YahooGeoPlanet {

	private static Logger logger = LoggerFactory
			.getLogger(YahooGeoPlanetImpl.class);

	private String geoPlanetURL = "http://where.yahooapis.com/v1/";
	private String queryFromPlaceName = "places.q(%s)?appid=%s";
	private String queryFromWOEID = "places.woeid(%s)?appid=%s";
	private String queryPlaceStartingWith = "places.q(%s);count=%s?appid=%s";

	/**
	 * Valid Yahoo application ID, registered by Thibaud Flury
	 * thibaud_lig@yahoo.com -> for the purpose of AppsGate
	 */
	private String appID = "LIolTLV34H_riBa5bYkTDWrCutm0j7Ta6Nvzfh1wOp5tBktKEXzSOsKznKPqFzE9Nw-";

	static final String xPathSep = "//";

	public static final String PLACE_ELT = "place";
	public static final String PLACES_ELT = "places";
	
	public static final String ROOT_ELT = "/";

	public static final String WOEID = "woeid";
	public static final String PLACETYPENAME = "placeTypeName";
	public static final String NAME = "name";
	public static final String COUNTRY = "country";
	public static final String TOWN = "locality1";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.weather.YahooGeoPlanet#GetWOEIDFromPlaceName(java.lang.String
	 * )
	 */
	public String getWOEIDFromPlaceName(String placeName) {
		logger.trace("GetWOEIDFromPlaceName(String placeName : " + placeName
				+ ")");
		String currentWOEID = null;

		try {
			URL url = new URL(String.format(geoPlanetURL + queryFromPlaceName,
					placeName, appID));

			logger.trace("GetWOEIDFromPlaceName(...), query URL : " + url);

			DocumentBuilder db = null;
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(url.openStream());
			XPath xPath = XPathFactory.newInstance().newXPath();
			currentWOEID = xPath.evaluate(xPathSep + WOEID, doc);
			logger.trace("GetWOEIDFromPlaceName(...), found WOEID : "
					+ currentWOEID);

		} catch (Exception exc) {
			logger.error("Error when fetching Yahoo GeoPlanet WebService : "
					+ exc.getMessage());
		}

		return currentWOEID;
	}

	@Override
	public JSONObject getDescriptionFromPlaceName(String placeName) {
		logger.trace("getDescriptionFromPlaceName(String placeName : "
				+ placeName + ")");
		JSONObject obj = new JSONObject();
		try {
			URL url = new URL(String.format(geoPlanetURL + queryFromPlaceName,
					placeName, appID));
			logger.trace("getDescriptionFromPlaceName(...), query URL : " + url);

			DocumentBuilder db = null;
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(url.openStream());
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate(xPathSep+PLACE_ELT, doc, XPathConstants.NODESET);
			
			
			obj = parseLocationResponse(nodeList.item(0));
			logger.trace("getDescriptionFromPlaceName(...), returning "
					+ obj.toString());
			return obj;

		} catch (Exception exc) {
			logger.error("Error when fetching Yahoo GeoPlanet WebService : "
					+ exc.getMessage());
		}
		logger.trace("getDescriptionFromPlaceName(...), returning "
				+ obj.toString());
		return obj;
	}



	@Override
	public JSONObject getDescriptionFromWOEID(String woeid) {
		logger.trace("getDescriptionFromWOEID(String woeid : " + woeid + ")");
		try {
			URL url = new URL(String.format(geoPlanetURL + queryFromWOEID,
					woeid, appID));
			logger.trace("getDescriptionFromWOEID(...), query URL : " + url);

			DocumentBuilder db = null;
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(url.openStream());
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate(PLACE_ELT, doc, XPathConstants.NODESET);
			
			
			JSONObject obj = parseLocationResponse(nodeList.item(0));
			logger.trace("getDescriptionFromWOEID(...), returning "
					+ obj.toString());
			return obj;

		} catch (Exception exc) {
			logger.error("Error when fetching Yahoo GeoPlanet WebService : "
					+ exc.getMessage());
			return new JSONObject();
		}

	}

	private JSONObject parseLocationResponse(Node doc) {
		logger.trace("parseLocationResponse(Node doc : "+doc+")");
				
		JSONObject obj = new JSONObject();

		XPath xPath = XPathFactory.newInstance().newXPath();

		try {

			String currentWOEID = xPath.evaluate(WOEID, doc);
			logger.trace("parseLocationResponse(...), found woeid : "
					+ currentWOEID);

			String currentPlaceTypeName = xPath.evaluate(PLACETYPENAME, doc);
			logger.trace("parseLocationResponse(...), found placeTypeName : "
					+ currentPlaceTypeName);

			String currentName = xPath.evaluate(NAME, doc);
			logger.trace("parseLocationResponse(...), found placeTypeName : "
					+ currentName);

			String currentCountry = xPath.evaluate(COUNTRY, doc);
			logger.trace("parseLocationResponse(...), found country : "
					+ currentCountry);

			String currentTown = xPath.evaluate(TOWN, doc);
			logger.trace("parseLocationResponse(...), found locality1 : "
					+ currentTown);

			if (currentWOEID != null && currentWOEID.length() > 0)
				obj.put(WOEID, currentWOEID);
			if (currentPlaceTypeName != null
					&& currentPlaceTypeName.length() > 0)
				obj.put(PLACETYPENAME, currentPlaceTypeName);
			if (currentCountry != null && currentCountry.length() > 0)
				obj.put(COUNTRY, currentCountry);
			if (currentName != null && currentName.length() > 0)
				obj.put(NAME, currentName);
			if (currentTown != null && currentTown.length() > 0)
				obj.put(TOWN, currentTown);
			
		} catch (XPathException exc) {
			logger.error("Error when parsing Yahoo GeoPlanet WebService response : "
					+ exc.getMessage());
		}
		return obj;

	}
	
	@Override
	public JSONArray getLocationsStartingWith(String firstLetters) {
		logger.trace("checkLocationStartingWith(String firstLetters : "
				+ firstLetters + ")");
		JSONArray array = new JSONArray();
		try {
			URL url = new URL(String.format(geoPlanetURL + queryPlaceStartingWith,
					firstLetters+"%2A","5", appID));
			logger.trace("checkLocationStartingWith(...), query URL : " + url);

			DocumentBuilder db = null;
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(url.openStream());
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate(xPathSep+PLACE_ELT, doc, XPathConstants.NODESET);
			
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
			    JSONObject obj =  parseLocationResponse(nodeList.item(i));
			    logger.trace("checkLocationStartingWith(...), adding entry : "+obj.toString());
			    array.put(obj);
			}

			logger.trace("checkLocationStartingWith(...), returning "
					+ array.toString());
			return array;

		} catch (Exception exc) {
			logger.error("Error when fetching Yahoo GeoPlanet WebService : "
					+ exc.getMessage());
		}
		return array;
	}	

}
