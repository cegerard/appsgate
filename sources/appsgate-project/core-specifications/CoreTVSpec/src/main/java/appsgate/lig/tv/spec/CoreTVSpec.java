/**
 * 
 */
package appsgate.lig.tv.spec;

/**
 * @author thibaud
 *
 */
public interface CoreTVSpec {
	
	
	/**
	 * command: notify, notify a message on the STB
	 * @param id  0/1 screen identification
	 * @param sender URL encoded string of the sender name
	 * @param message URL encoded notification message 
	 */
	public void notify(int id, String sender, String message);
	
	/**
	 * command: channelUp, switch to the next channel
	 * @param id  0/1 screen identification
	 */
	public void channelUp(int id);
	
	/**
	 * command: channelDown, switch to the previous channel
	 * @param id  0/1 screen identification
	 */
	public void channelDown(int id);
	
	/**
	 * command: resume, resume video
	 * @param id  0/1 screen identification
	 */
	public void resume(int id);
	
	/**
	 * command: stop, stop video playback
	 * @param id  0/1 screen identification
	 */
	public void stop(int id);
	
	/**
	 * command: pause, pause video
	 * @param id  0/1 screen identification
	 */
	public void pause(int id);

	/**
	 * command: resize, resize video
	 * @param id 0/1 screen identification
	 * @param x position of the video frame
	 * @param y position of the video frame
	 * @param width of the video frame
	 * @param height of the video frame
	 */
	public void resize(int id, int x, int y, int width, int height);	
	
	/**
	 * Used to set the configuration of the service, if unavailable (request timeout or bad response from the tv)
	 * calls the factory to destroy itself
	 * @param hostname
	 * @param port
	 * @param factory
	 */
	public void setConfiguration(String hostname, int port, TVFactory factory);	
	
}
