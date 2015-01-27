package appsgate.lig.tts.yakitome;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.tts.yakitome.utils.HttpUtils;

public class YakitomeAdapter implements TTSItemsListener {
	
	private static Logger logger = LoggerFactory
			.getLogger(YakitomeAdapter.class);	
	
	Map<String, SpeechTextItem> ttsItems;
	YakitomeAPI apiClient;

	/**
	 * Check if a TTS item matching the desired sentence already exists
	 * (used for optimization as TTS generation can takes long time)
	 */
	public String getTTSItemMatchingSentence(String sentence) {
		logger.trace("getTTSItemMatchingSentence(String sentence : {})",sentence);
		
		String encodedSentence;
		try {
			encodedSentence = URLEncoder.encode(sentence, HttpUtils.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.warn("getTTSItemMatchingSentence(...), "
					+ "error while getting encoded sentence, returning null, exception : "+e.getMessage());
			return null;
		}
		if(encodedSentence == null ||encodedSentence.isEmpty()) {
			logger.warn("getTTSItemMatchingSentence(...), "
					+ "empty or null sentence, returning null");
			return null;
		}
		
		for(String ttsId : ttsItems.keySet()) {
			SpeechTextItem tmp = ttsItems.get(ttsId);
			if(tmp!= null && encodedSentence.equals(tmp.getEncodedSentence())) {
				logger.trace("getTTSItemMatchingSentence(...), found matching sentence, with id : "+tmp.getSpeechTextId());
				return tmp.getSpeechTextId();
			}
		}
		logger.debug("getTTSItemMatchingSentence(...), no item matchnig the sentence found, returning null");
		return null;	
	}
	
	@Override
	public void onTTSItemAdded(SpeechTextItem item) {
		logger.trace("onTTSItemAdded(SpeechTextItem item : {})",item);
		ttsItems.put(item.getSpeechTextId(), item);
	}
	
	public String asynchronousTTSGeneration(String text) {
		logger.trace("asynchronousTTSGeneration(String text : {})",text);
		String id = getTTSItemMatchingSentence(text);
		if(id != null) {
			logger.trace("asynchronousTTSGeneration(...), Text to speech already generated : "+id);
			return id;
		}
		
		try {
			logger.trace("asynchronousTTSGeneration(...), Launching TTS...");
			JSONObject responseTTS = apiClient.textToSpeech(text);
			String book_id = String.valueOf(responseTTS.getInt(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY));
			logger.trace("... asynchronousTTSGeneration(...), TTS id :"+book_id);

			
		
		} catch (Exception e) {
		}	
		return null;
	}
}
