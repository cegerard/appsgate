package appsgate.lig.tts.yakitome.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.tts.CoreTTSService;
import appsgate.lig.tts.yakitome.DAOSpeechTextItems;
import appsgate.lig.tts.yakitome.SpeechTextItem;
import appsgate.lig.tts.yakitome.TTSItemsListener;
import appsgate.lig.tts.yakitome.YakitomeAPI;

/**
 * This class holds the CoreObjectSpec and TTS business functions
 * @author thibaud
 *
 */
public class TTSServiceImpl extends CoreObjectBehavior implements TTSItemsListener, CoreTTSService, CoreObjectSpec{
	
	public final static String TTS_IMPLEM_NAME = "TTSServiceImpl";
	
	/**
	 * These  should match those stored in the DB and in the YakiTomeAPI
	 */
	Map<Integer, SpeechTextItem> ttsItems = new HashMap<Integer, SpeechTextItem>();
	
	/**
	 * These corresponds to TTS running status (not in the DB)
	 */
	Map<Integer, SpeechTextItem> ttsItemsRunning = new HashMap<Integer, SpeechTextItem>();

	YakitomeAPI apiClient;
	
	int coreObjectStatus = 0;
	DAOSpeechTextItems dao;
	
	String defaultVoice = YakitomeAPIClient.DEFAULT_VOICE;
	int defaultSpeed = YakitomeAPIClient.DEFAULT_SPEED;
	
	
	/**
	 * This method should be accessible only by the adapter
	 * @param apiClient
	 */
	public void configure(YakitomeAPI apiClient,
			DAOSpeechTextItems dao) {
		logger.trace("configure(YakitomeAPI apiClient : {}"
				+ ", DAOSpeechTextItem dao : {})"
				, apiClient, dao);

		this.apiClient = apiClient;
		this.dao = dao;
		if ( testStatus()) {
			logger.trace("configure(...), test OK, populating from DB");
			for(SpeechTextItem item : dao.getSpeechItemsFromDB()) {
				logger.trace("configure(...), trying to add "+item.getBookId());
				if (item != null
						&& item.getBookId()>0
						&& getSpeechTextStatus(item.getBookId()) != null) {
					ttsItems.put(item.getBookId(), item);
					logger.trace("configure(...), item added, boook id : {} and text : {}",
							item.getBookId(), item.getText());
				}
			}		
		}
	}
	
	private boolean testStatus() {
		if(apiClient!= null
				&& apiClient.testService()
				&& dao!=null
				&& dao.testService()) {
			coreObjectStatus = 2;
			return true;
		} else {
			coreObjectStatus = 0;
			return false;
		}
	}
	
	private static Logger logger = LoggerFactory
			.getLogger(TTSServiceImpl.class);	
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#getTTSItemMatchingText(java.lang.String,java.lang.String, int)
	 */
	@Override
	public int getTTSItemMatchingText(String text, String voice, int speed) {
		logger.trace("getTTSItemMatchingText(String text : {}, String voice : {}, int speed : {})",text, voice, speed);
		
		
		if(text == null ||text.isEmpty()) {
			logger.warn("getTTSItemMatchingText(...), "
					+ "empty or null text, returning 0");
			return 0;
		}
		
		if(voice == null ||voice.isEmpty()) {
			logger.warn("getTTSItemMatchingText(...), "
					+ "empty or null voice, returning 0");
			return 0;
		}
		
		if(speed != apiClient.checkAndgetSpeed(speed)) {
			logger.warn("getTTSItemMatchingText(...), "
					+ "speed in in the limits, returning 0");
			return 0;			
		}
		
		logger.debug("getTTSItemMatchingText(...), Step One checking already generated TTS");
		for(int ttsId : ttsItems.keySet()) {
			SpeechTextItem tmp = ttsItems.get(ttsId);
			
			logger.trace("getTTSItemMatchingText(...), comparing {} with {}",text,tmp.getText());
			if(tmp!= null && text.equals(tmp.getText())
					&& voice.equals(tmp.getVoice())
					&& speed == tmp.getSpeed()) {
				logger.trace("getTTSItemMatchingText(...), found matching text, with id : "+tmp.getBookId());
				if(getSpeechTextStatus(ttsId) == null) {
					logger.warn("getTTSItemMatchingText(...),"
							+ " item unknown to Yakitome TTS, removing an returning book id = 0");
					return 0;
				} else {
					logger.trace("getTTSItemMatchingText(...),"
							+ " found book id in Yakitome TTS, returning "+tmp.getBookId());
					return tmp.getBookId();					
				}
			}
		}
		
		logger.debug("getTTSItemMatchingText(...), TTS Not found, Step Two checking TTS onGoing");
		for(int ttsId : ttsItemsRunning.keySet()) {
			SpeechTextItem tmp = ttsItemsRunning.get(ttsId);
			logger.trace("getTTSItemMatchingText(...), comparing {} with {}",text,tmp);
			if(tmp!= null && text.equals(tmp)
					&& voice.equals(tmp.getVoice())
					&& speed == tmp.getSpeed()) {
				logger.trace("getTTSItemMatchingText(...), found matching text, with id : "+ttsId);
				if(getSpeechTextStatus(ttsId) == null) {
					logger.warn("getTTSItemMatchingText(...),"
							+ " item unknown to Yakitome TTS, removing an returning book id = 0");
					return 0;
				} else {
					logger.trace("getTTSItemMatchingText(...),"
							+ " found book id in Yakitome TTS, returning "+ttsId);
					return ttsId;					
				}
			}
		}		
		logger.debug("getTTSItemMatchingText(...), no item matching the text found, returning 0");
		return 0;	
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#asynchronousTTSGeneration(java.lang.String)
	 */
	@Override
	public int asynchronousTTSGeneration(String text) {
		logger.trace("asynchronousTTSGeneration(String text : {})",text);
		return asynchronousTTSGeneration(text, defaultVoice, defaultSpeed);
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#waitForTTSGeneration(java.lang.String)
	 */
	@Override
	public int waitForTTSGeneration(String text) {
		logger.trace("waitForTTSGeneration(String text : {})",text);
		return waitForTTSGeneration(text, defaultVoice, defaultSpeed);
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#deleteSpeechText(int)
	 */
	@Override
	public JSONObject deleteSpeechText(int book_id) {
		logger.trace("deleteSpeechText(int book_id : {})",book_id);
		JSONObject response = null;
		try {
			response = apiClient.deleteSpeechText(book_id);
			logger.trace("deleteSpeechText(...), removed from the server");
		} catch (Exception e) {
			logger.warn("deleteSpeechText(...), Error occured when trying to delete TTS from server : "+e.getMessage());
			testStatus();
		}
		logger.trace("deleteSpeechText(...), trying to remove from local list");
		ttsItems.remove(book_id);
		dao.removeSpeechItem(book_id);
		return response;
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#getSpeechTextStatus(int)
	 */
	@Override
	public JSONObject getSpeechTextStatus(int book_id) {
		logger.trace("getSpeechTextStatus(int book_id : {})",book_id);
		JSONObject response = null;
		try {
			response = apiClient.getSpeechTextStatus(book_id);
			logger.trace("getSpeechTextStatus(...), response from server : "+response);
			if(response.has(YakitomeAPI.BOOK_ID_KEY)
					&& response.getInt(YakitomeAPI.BOOK_ID_KEY) == 0
					&& ttsItems.containsKey(book_id)) {
				logger.trace("getSpeechTextStatus(...), the book id does not exist on the server, removing on local list");
				ttsItems.remove(book_id);
				response = null;
			}

		} catch (Exception e) {
			logger.warn("getSpeechTextStatus(...), Error occured calling getStatus on server : "+e.getMessage());
			testStatus();
		}
		return response;
	}
	
	@Override
	public String getAudioURL(int book_id, int track) {
		logger.trace("getAudioURL(int book_id : {}, int track : {})",book_id, track);
		if(ttsItems.containsKey(book_id)
				&& track>=0
				&& ttsItems.get(book_id) != null
				&& ttsItems.get(book_id).getAudioURLs() != null
				&& ttsItems.get(book_id).getAudioURLs().size() > track) {
			logger.trace("getAudioURL(...), found book_id, audio urls are existing");
			return ttsItems.get(book_id).getAudioURLs().get(track);
		} else {
			logger.warn("getAudioURL(...), book_id does not exists");
		}
		return null;
	}
	
	public static final String userType = "104";
	
	@Override
	public void onTTSItemAdded(SpeechTextItem item) {
		logger.trace("onTTSItemAdded(SpeechTextItem item : {})",item.toJSON());
		if(ttsItemsRunning.containsKey(item.getBookId())) {
			ttsItemsRunning.remove(item.getBookId());
		}
		dao.addUpdateSpeechItem(item);
		ttsItems.put(item.getBookId(), item);
	}

	String serviceId;

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", getAbstractObjectId());
		descr.put("type", getUserType()); // 104 for TTS
		descr.put("status", getObjectStatus());
		descr.put("ttsItems", getSpeechTextItems());
		return descr;
	}

	@Override
	public JSONObject getBehaviorDescription() {
		return super.getBehaviorDescription();
	}
	
	@Override
	public String getAbstractObjectId() {
		return serviceId;
	}	
	
	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}	
	
	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return coreObjectStatus;
	}	
	
	/**
	 * @deprecated
	 */
	@Override
	public String getPictureId() {
		// Will be removed
		return null;
	}

	/**
	 * @deprecated
	 */
	@Override
	public void setPictureId(String pictureId) {
		// will be removed
	}

	@Override
	public JSONObject getSpeechTextItem(int book_id) {
		logger.trace("getSpeechTextItem(int book_id : {})",book_id);
		JSONObject response = getSpeechTextStatus(book_id);
		if(response!=null
				&& response.has(YakitomeAPI.BOOK_ID_KEY)
				&& response.getInt(YakitomeAPI.BOOK_ID_KEY) >0
				&& ttsItems.containsKey(book_id)) {
			logger.trace("getSpeechTextItem(...), book id found, returning "+ttsItems.get(book_id).toJSON());
			return ttsItems.get(book_id).toJSON();	
		}
		logger.warn("getSpeechTextItem(...), book id not found, returning null");
		return null;
	}

	@Override
	public JSONArray getSpeechTextItems() {
		logger.trace("getSpeechTextItems()");
		JSONArray response = new JSONArray();
		for(SpeechTextItem item : ttsItems.values()){
			logger.trace("getSpeechTextItems(), adding {} to the result", item.toJSON());
			response.put(item.toJSON());
		}
		return response;
	}


	@Override
	public int asynchronousTTSGeneration(String text, String voice, int speed) {
		logger.trace("asynchronousTTSGeneration(String text : {}, String voice :{}, int speed : {})",text,voice, speed);


		int book_id = getTTSItemMatchingText(text,voice, speed);
		if(book_id >0) {
			logger.trace("asynchronousTTSGeneration(...), Text to speech already generated : "+book_id);
			return book_id;
		}
		try {
			
			logger.trace("asynchronousTTSGeneration(), step one generation of Text : "+text);
			JSONObject response=new JSONObject();
			
			try{
				response = apiClient.textToSpeech(text, voice, speed);

			} catch (ServiceException e) {
				logger.trace("asynchronousTTSGeneration(), Service Exception : "+e.getMessage());
				response = new JSONObject();
			}
			
			if(!response.has(YakitomeAPIClient.BOOK_ID_KEY)) {
				logger.warn("asynchronousTTSGeneration(), Text To Speech not generated ");
				return 0;
			}
			book_id = response.getInt(YakitomeAPIClient.BOOK_ID_KEY);
			ttsItemsRunning.put(book_id, new SpeechTextItem(book_id, text, voice, speed, response.optInt(YakitomeAPIClient.WORD_CNT_KEY),null));

			logger.trace("asynchronousTTSGeneration(...), Launching asynchronous monitor...");
			TTSGenerationMonitor monitor = new TTSGenerationMonitor(book_id, text, voice, speed, this, apiClient);
			monitor.start();
			logger.trace("... asynchronousTTSGeneration(...), monitor launched, waiting for the callback");
		} catch (Exception e) {
			logger.trace("asynchronousTTSGeneration(...), Exception occured : "+e.getMessage());
			testStatus();
		}
		return book_id;
	}

	@Override
	public int waitForTTSGeneration(String text, String voice, int speed) {
		logger.trace("waitForTTSGeneration(String text : {}, String voice :{}, int speed : {})",text,voice, speed);
		int book_id = getTTSItemMatchingText(text, voice, speed);
		JSONObject response;
		if(book_id >0) {
			logger.trace("waitForTTSGeneration(...), found book_id: "+book_id);
			if(!ttsItemsRunning.containsKey(book_id)) {
				logger.trace("waitForTTSGeneration(...), Text to speech already generated : "+book_id);
				return book_id;
			} else {
				logger.trace("waitForTTSGeneration(...), Text to speech NOT generated");
			}
		}
		
		try {
			logger.trace("waitForTTSGeneration(), step one generation of Text : "+text);
			response=new JSONObject();
			try{
				response = apiClient.textToSpeech(text, voice, speed);
			} catch (ServiceException e) {
				logger.trace("waitForTTSGeneration(), Service Exception : "+e.getMessage());
				response = new JSONObject();
			}
			
			if(!response.has(YakitomeAPIClient.BOOK_ID_KEY)) {
				logger.warn("waitForTTSGeneration(), Text To Speech not generated ");
				return 0;
			}
			book_id = response.getInt(YakitomeAPIClient.BOOK_ID_KEY);
			ttsItemsRunning.put(book_id, new SpeechTextItem(book_id, text, voice, speed, response.optInt(YakitomeAPIClient.WORD_CNT_KEY),null));

			logger.trace("waitForTTSGeneration(...), Launching blocking method waiting for TTS...");
			response = apiClient.waitForTTS(book_id);
			response.put(YakitomeAPI.TEXT_KEY, text);
			response.put(YakitomeAPI.VOICE_KEY, voice);
			response.put(YakitomeAPI.SPEED_KEY, speed);
			SpeechTextItem item = new SpeechTextItem(response);
			if(item != null ) {
				logger.trace("waitForTTSGeneration(), adding it to the list and returning book id");
				onTTSItemAdded(item);
				return book_id;
			}
		} catch (Exception e) {
			logger.warn("waitForTTSGeneration(...), Exception occured : "+e.getMessage());
			testStatus();
		}
		return book_id;
	}

	@Override
	public int countAudioURLs(int book_id) {
		logger.trace("countAudioURLs(int book_id : {})",book_id);
		if(ttsItems.containsKey(book_id)
				&& ttsItems.get(book_id).getAudioURLs() != null) {
			logger.trace("countAudioURLs(...), returning "
				+ttsItems.get(book_id).getAudioURLs().size());
			return ttsItems.get(book_id).getAudioURLs().size();
		} else {
			logger.trace("countAudioURLs(...), no audio URLs found, returning 0");
			return 0;			
		}
	}

	@Override
	public String getDefaultVoice() {
		logger.trace("getDefaultVoice(), returning "+defaultVoice);
		return defaultVoice;
	}

	@Override
	public void setDefaultVoice(String voice) {
		logger.trace("setDefaultVoice(String voice : {})",voice);
		if(apiClient.checkVoice(voice)) {
			defaultVoice=voice;
		} else {
			logger.trace("setDefaultVoice(...), voice does not exists, keeping ",defaultVoice);			
		}
	}

	@Override
	public int getDefaultSpeed() {
		logger.trace("getDefaultSpeed(), returning "+defaultSpeed);
		return defaultSpeed;
	}

	@Override
	public void setDefaultSpeed(int speed) {
		logger.trace("setDefaultSpeed(int speed : {})",speed);
		defaultSpeed = apiClient.checkAndgetSpeed(speed);
	}

	@Override
	public JSONObject getAvailableVoices() {
		logger.trace("getAvailableVoices()");
		return apiClient.getVoices();
	}
}
