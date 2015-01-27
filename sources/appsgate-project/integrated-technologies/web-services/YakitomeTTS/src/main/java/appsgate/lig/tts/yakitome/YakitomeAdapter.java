package appsgate.lig.tts.yakitome;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.tts.yakitome.utils.HttpUtils;

public class YakitomeAdapter implements TTSItemsListener, CoreTTSService, CoreObjectSpec {
	
	private static Logger logger = LoggerFactory
			.getLogger(YakitomeAdapter.class);	
	
	Map<Integer, SpeechTextItem> ttsItems = new HashMap<Integer, SpeechTextItem>();
	YakitomeAPI apiClient;
	
	
	public void configure(YakitomeAPI apiClient) {
		this.apiClient = apiClient;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#getTTSItemMatchingSentence(java.lang.String)
	 */
	@Override
	public int getTTSItemMatchingSentence(String sentence) {
		logger.trace("getTTSItemMatchingSentence(String sentence : {})",sentence);
		
		String encodedSentence;
		try {
			encodedSentence = URLEncoder.encode(sentence, HttpUtils.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.warn("getTTSItemMatchingSentence(...), "
					+ "error while getting encoded sentence, returning 0, exception : "+e.getMessage());
			return 0;
		}
		if(encodedSentence == null ||encodedSentence.isEmpty()) {
			logger.warn("getTTSItemMatchingSentence(...), "
					+ "empty or null sentence, returning 0");
			return 0;
		}
		
		for(int ttsId : ttsItems.keySet()) {
			SpeechTextItem tmp = ttsItems.get(ttsId);
			if(tmp!= null && encodedSentence.equals(tmp.getEncodedSentence())) {
				logger.trace("getTTSItemMatchingSentence(...), found matching sentence, with id : "+tmp.getSpeechTextId());
				return tmp.getSpeechTextId();
			}
		}
		logger.debug("getTTSItemMatchingSentence(...), no item matching the sentence found, returning 0");
		return 0;	
	}
	
	
	@Override
	public void onTTSItemAdded(SpeechTextItem item) {
		logger.trace("onTTSItemAdded(SpeechTextItem item : {})",item.toJSON());
		ttsItems.put(item.getSpeechTextId(), item);
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#asynchronousTTSGeneration(java.lang.String)
	 */
	@Override
	public int asynchronousTTSGeneration(String text) {
		logger.trace("asynchronousTTSGeneration(String text : {})",text);
		int book_id = getTTSItemMatchingSentence(text);
		if(book_id >0) {
			logger.trace("asynchronousTTSGeneration(...), Text to speech already generated : "+book_id);
			return book_id;
		}
		try {
			
			logger.trace("asynchronousTTSGeneration(), step one generation of Text : "+text);
			JSONObject response=new JSONObject();
			
			try{
				response = apiClient.textToSpeech(text);

			} catch (ServiceException e) {
				logger.trace("asynchronousTTSGeneration(), Service Exception : "+e.getMessage());
				response = new JSONObject();
			}
			
			if(!response.has(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY)) {
				logger.warn("asynchronousTTSGeneration(), Text To Speech not generated ");
				return 0;
			}
			book_id = response.getInt(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY);

			logger.trace("asynchronousTTSGeneration(...), Launching asynchronous monitor...");
			TTSGenerationMonitor monitor = new TTSGenerationMonitor(book_id, text, this, apiClient);
			monitor.start();
			logger.trace("... asynchronousTTSGeneration(...), monitor launched, waiting for the callback");
		} catch (Exception e) {
			logger.trace("asynchronousTTSGeneration(...), Exception occured : "+e.getMessage());
		}
		return book_id;
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.CoreTTSService#waitForTTSGeneration(java.lang.String)
	 */
	@Override
	public int waitForTTSGeneration(String text) {
		logger.trace("waitForTTSGeneration(String text : {})",text);
		int book_id = getTTSItemMatchingSentence(text);
		if(book_id >0) {
			logger.trace("waitForTTSGeneration(...), Text to speech already generated : "+book_id);
			return book_id;
		}
		try {
			logger.trace("waitForTTSGeneration(), step one generation of Text : "+text);
			JSONObject response=new JSONObject();
			try{
				response = apiClient.textToSpeech(text);
			} catch (ServiceException e) {
				logger.trace("waitForTTSGeneration(), Service Exception : "+e.getMessage());
				response = new JSONObject();
			}
			
			if(!response.has(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY)) {
				logger.warn("waitForTTSGeneration(), Text To Speech not generated ");
				return 0;
			}
			book_id = response.getInt(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY);
			
			logger.trace("waitForTTSGeneration(...), Launching blocking method waiting for TTS...");
			response = apiClient.waitForTTS(book_id);
			response.put(YakitomeAPI.TEXT_KEY, text);
			SpeechTextItem item = new SpeechTextItem(response);
			if(item != null ) {
				logger.trace("waitForTTSGeneration(), adding it to the list and returning book id");
				onTTSItemAdded(item);
				return book_id;
			}
		} catch (Exception e) {
			logger.warn("waitForTTSGeneration(...), Exception occured : "+e.getMessage());
		}
		return book_id;
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
		} catch (ServiceException e) {
			logger.warn("deleteSpeechText(...), Error occured when trying to delete TTS from server : "+e.getMessage());
		}
		logger.trace("deleteSpeechText(...), trying to remove from local list");
		ttsItems.remove(book_id);
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
			if(response.has(YakitomeAPI.BOOK_ID_RESPONSE_KEY)
					&& response.getInt(YakitomeAPI.BOOK_ID_RESPONSE_KEY) == 0
					&& ttsItems.containsKey(book_id)) {
				logger.trace("getSpeechTextStatus(...), the book id does not exist on the server, removing on local list");
				ttsItems.remove(book_id);
			}

		} catch (ServiceException e) {
			logger.warn("getSpeechTextStatus(...), Error occured calling getStatus on server : "+e.getMessage());
		}
		
		
		return response;
	}

	@Override
	public String getAbstractObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getObjectStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPictureId() {
		// Deprecated
		return null;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPictureId(String pictureId) {
		// Deprecated
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}

	@Override
	public JSONObject getBehaviorDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
