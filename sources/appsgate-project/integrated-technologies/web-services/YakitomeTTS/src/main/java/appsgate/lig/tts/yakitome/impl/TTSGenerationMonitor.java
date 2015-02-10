package appsgate.lig.tts.yakitome.impl;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.tts.yakitome.SpeechTextItem;
import appsgate.lig.tts.yakitome.TTSItemsListener;
import appsgate.lig.tts.yakitome.YakitomeAPI;

public class TTSGenerationMonitor extends Thread {
	/**
	 * @param listener
	 */
	public TTSGenerationMonitor(int book_id, String text, String voice, int speed, TTSItemsListener listener, YakitomeAPI ttsAPI) {
		logger.trace("TTSGenerationMonitor(int book_id : {}"
				+ ", String text : {}"
				+ ", String voice : {}"
				+ ", int speed : {}"
				+ ", TTSItemsListener listener : {}"
				+ ", YakitomeAPI ttsAPI: {})", book_id, text, voice, speed, listener, ttsAPI);
		this.listener = listener;
		this.ttsAPI = ttsAPI;
		this.book_id = book_id;
		this.text = text;
		this.voice = voice;
		this.speed = speed;
	}
	
	@Override
	public void run() {
		logger.trace("run()");
		try {
			JSONObject response = ttsAPI.waitForTTS(book_id);
			response.put(YakitomeAPI.TEXT_KEY, text);
			response.put(YakitomeAPI.VOICE_KEY, voice);
			response.put(YakitomeAPI.SPEED_KEY, speed);
			SpeechTextItem item = new SpeechTextItem(response);
			if(item != null ) {
				logger.trace("run(), item found, sending callback message to listener");
				listener.onTTSItemAdded(item);
			}
		} catch (Exception e) {
			logger.warn("run(), exception occured : "+e.getMessage());
		}		
	}
	
	private static Logger logger = LoggerFactory
			.getLogger(TTSGenerationMonitor.class);
	
	TTSItemsListener listener;
	YakitomeAPI ttsAPI;
	int book_id;
	String text;
	String voice;
	int speed;
}
