package appsgate.lig.tts.yakitome;

public interface TTSItemsListener {
	
	/**
	 * Useful callback interface when TTS item is generated asynchronously
	 * (TTS generation and creation of audio files may takes some times) 
	 * @param item
	 */
	public void onTTSItemAdded(SpeechTextItem item);
	
	/**
	 * This one should be called when TTS generation takes to much time
	 * @param book_id
	 */
	public void onTTSItemAddedTimeout(int book_id);

}
