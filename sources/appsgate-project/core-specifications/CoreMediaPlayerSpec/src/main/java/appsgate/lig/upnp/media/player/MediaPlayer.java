package appsgate.lig.upnp.media.player;


public interface MediaPlayer {	
	
	public void play(String mediaURL);
	public void play(String mediaURL, String mediaName);
	
	public void audioNotification(String message);
	public void audioNotification(String message, String voice, int speed);
	
	public void resume();
	public void pause();
	public void stop();
	
	public int getVolume();
	public void setVolume(int level);
	
	
	/**
	 * Increase the volume by step
	 * @param step the increase volume step as an integer
	 * @return the new volume value
	 */
	public int increaseVolume(int step);
	
	/**
	 * Decrease the volume by step
	 * @param step the decrease volume step as an integer
	 * @return the new volume value
	 */
	public int decreaseVolume(int step);	
	
	public String getPlayerStatus();
	public String getCurrentMediaName();
}
