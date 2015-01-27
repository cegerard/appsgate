package appsgate.lig.tts.yakitome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TTSGenerationMonitor extends Thread {
	/**
	 * @param listener
	 */
	public TTSGenerationMonitor(String book_id, TTSItemsListener listener, YakitomeAPI ttsAPI) {
		logger.trace("TTSGenerationMonitor(String book_id : {}"
				+ ", TTSItemsListener listener : {}"
				+ ", YakitomeAPI ttsAPI: {})", book_id, listener, ttsAPI);
		this.listener = listener;
		this.ttsAPI = ttsAPI;
		this.book_id = book_id;
	}
	
	@Override
	public void run() {
		logger.trace("run()");
		try {
			SpeechTextItem item = ttsAPI.waitForTTS(this.book_id);
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
	String book_id;
}
