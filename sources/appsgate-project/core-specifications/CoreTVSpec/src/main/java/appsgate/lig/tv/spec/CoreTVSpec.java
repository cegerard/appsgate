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
	 * KeyCodes for the radiocontrol
	 * (these value are used as String litterals)
	 */
	public enum KeyCode {
		VK_CHANNEL_DOWN,
		VK_CHANNEL_UP,
		VK_MENU,
		VK_GUIDE,
		VK_INFO,
		VK_UP,
		VK_DOWN,
		VK_LEFT,
		VK_RIGHT, 
		VK_SELECT,
		VK_LAST,
		VK_COLORED_KEY_0,// (red)
		VK_COLORED_KEY_1,// (green)
		VK_COLORED_KEY_2,// (blue)
		VK_COLORED_KEY_3,// (yellow)
		VK_0,
		VK_1,
		VK_2,
		VK_3,
		VK_4,
		VK_5,
		VK_6,
		VK_7,
		VK_8,
		VK_9,
		VK_EXIT, 
		VK_ENTER
	};
	
	/**
	 * command: rc, send a radiocontrol command (see enum KeyCode for available commands)
	 */
	public void sendRCCommand(String keycode);
	
	/**
	 * command: notify, notify a message on the STB
	 * @param id  0/1 screen identification
	 * @param sender URL encoded string of the sender name
	 * @param message URL encoded notification message
	 * @parm
	 */
	public void notify(int id, String sender, String message, boolean ack, int duration, String icon);
	
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
	 * @param path (optional)
	 * @param factory
	 */
	public void setConfiguration(String hostname, int port, String path, TVFactory factory);	
	
}
