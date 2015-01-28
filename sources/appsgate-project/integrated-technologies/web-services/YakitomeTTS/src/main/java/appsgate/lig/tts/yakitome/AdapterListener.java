package appsgate.lig.tts.yakitome;

public interface AdapterListener {
	
	/**
	 * Called when a call to the YakitomeAPI fail (maybe the service is unreachable)
	 */
	public void serviceUnavailable();

}
