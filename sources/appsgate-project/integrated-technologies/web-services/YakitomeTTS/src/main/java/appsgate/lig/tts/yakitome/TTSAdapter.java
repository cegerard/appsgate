package appsgate.lig.tts.yakitome;

public interface TTSAdapter {
	
	/**
	 * Called when a call to the YakitomeAPI fail (maybe the service is unreachable)
	 */
	public void serviceUnavailable();
	
	public DAOSpeechTextItems getDAO();
	
	public YakitomeAPI getAPI();
}
