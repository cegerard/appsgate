package appsgate.validation.context;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;

/**
 * This class is use to validate the context history.
 * @author Cédric Gérard
 *
 */
public class ContextHistoryTester {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ContextHistoryTester.class);
	
	private DataBasePullService contextHistory_pull;
	private DataBasePushService contextHistory_push;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	@SuppressWarnings("unchecked")
	public void newInst() {
		logger.debug("ContextHistoryTester has been initialized");
		
		//Try to push something in the database
		logger.debug("#########  Try to push data into data base");
		ArrayList<Entry<String, Object>> state = new ArrayList<Entry<String, Object>>();
		state.add(new AbstractMap.SimpleEntry<String, Object>("plop",new Integer(474745)));
		state.add(new AbstractMap.SimpleEntry<String, Object>("plup",new Integer(474734)));
		state.add(new AbstractMap.SimpleEntry<String, Object>("plip",new Integer(476543)));
		state.add(new AbstractMap.SimpleEntry<String, Object>("plap",new Integer(474535)));
		if( contextHistory_push.pushData_add("baseTest", "plop", "090398494", "nameTEST", state)) {
			logger.debug("#########  Data correctly pushed");
		} else {
			logger.debug("#########  ERROR DATA NOT PUSHED");
		}
		
		//Try to pull previous push data from the data base
		logger.debug("#########  Try to pull data from data base");
		JSONObject obj = contextHistory_pull.pullLastObjectVersion("baseTest");
		if(obj != null){
			logger.debug("#########  An object has been retreived");
			Iterator<String> keyIT = obj.keys();
			while(keyIT.hasNext()) {
				String key = keyIT.next();
				try {
					Object value = obj.get(key);
					logger.debug("<"+key+" , "+value.toString()+">");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}else {
			logger.debug("#########  No object found with the current request");
		}
		
		//Try to push update off an object
		logger.debug("#########  Try to update data into data base");
		if( contextHistory_push.pushData_change("baseTest", "plop", "090398494", "nameTEST", "newTestName", new ArrayList<Entry<String, Object>>())) {
			logger.debug("#########  Data correctly udpated");
		} else {
			logger.debug("#########  ERROR DATA NOT UPDATED");
		}
		
		//Try to pull previous push data from the data base
		logger.debug("#########  Try to pull data from data base");
		obj = contextHistory_pull.pullLastObjectVersion("baseTest");
		if(obj != null){
			logger.debug("#########  An object has been retreived");
			Iterator<String> keyIT = obj.keys();
			while(keyIT.hasNext()) {
				String key = keyIT.next();
				try {
					Object value = obj.get(key);
					logger.debug("<"+key+" , "+value.toString()+">");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}else {
			logger.debug("#########  No object found with the current request");
		}
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("ContextHistoryTester has been stopped");
	}

}
