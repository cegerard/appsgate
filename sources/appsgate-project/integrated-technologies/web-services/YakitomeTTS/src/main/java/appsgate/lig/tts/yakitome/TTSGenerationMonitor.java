package appsgate.lig.tts.yakitome;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TTSGenerationMonitor extends Thread {
	/**
	 * @param listener
	 */
	public TTSGenerationMonitor(int book_id, String text, TTSItemsListener listener, YakitomeAPI ttsAPI) {
		logger.trace("TTSGenerationMonitor(int book_id : {}"
				+ ", String text : {}"
				+ ", TTSItemsListener listener : {}"
				+ ", YakitomeAPI ttsAPI: {})", book_id, text, listener, ttsAPI);
		this.listener = listener;
		this.ttsAPI = ttsAPI;
		this.book_id = book_id;
		this.text = text;
	}
	
	@Override
	public void run() {
		logger.trace("run()");
		try {
			JSONObject response = ttsAPI.waitForTTS(book_id);
			response.put(YakitomeAPI.TEXT_KEY, text);
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
}
