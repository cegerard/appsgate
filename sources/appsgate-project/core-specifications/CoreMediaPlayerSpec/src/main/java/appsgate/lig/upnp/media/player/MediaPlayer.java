package appsgate.lig.upnp.media.player;


public interface MediaPlayer {	
	
	public void play(String mediaURL);
	public void play(String mediaURL, String mediaName);
	
	public void audioNotification(String message);
	
	public void resume();
	public void pause();
	public void stop();
	
	public int getVolume();
	public void setVolume(int level);
	
	public String getPlayerStatus();
	public String getCurrentMediaName();
}
