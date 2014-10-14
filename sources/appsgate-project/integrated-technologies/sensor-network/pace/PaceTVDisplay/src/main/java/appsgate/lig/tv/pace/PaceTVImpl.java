/**
 * 
 */
package appsgate.lig.tv.pace;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.tv.spec.CoreTVSpec;
import appsgate.lig.tv.spec.TVFactory;

/**
 * @author thibaud
 *
 */
public class PaceTVImpl extends CoreObjectBehavior implements CoreTVSpec, CoreObjectSpec{
	
	/*
	 * Object information
	 */
	private String serviceId;
	private String userType;
	private String status;
	private String pictureId;	
	

	public PaceTVImpl() {
	}
	
	@Override
	public String getAbstractObjectId() {
		return serviceId;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return Integer.parseInt(status);
	}

	@Override
	public String getPictureId() {
		return pictureId;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();

		descr.put("id", serviceId);
		descr.put("type", userType); // 124 for TV
		descr.put("status", status);

		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
		this.pictureId=pictureId;
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}
	
	
	
	/**
	 * WebService Name
	 */
	final static String VIDEO_SERVICE = "video?";
	
	/**
	 * TV Web service command, first parameter of the REST Command
	 */
	final static String COMMAND_PARAM_NAME = "command=";
	
	final static String ID_PARAM_NAME = "&id=";
	final static String SCREEN_PARAM_NAME = "&screen=";
	final static String SENDER_PARAM_NAME = "&sender=";
	final static String MESSAGE_PARAM_NAME = "&message=";
	
	final static String COMMAND_CHANNELUP = "channelUp";
	final static String COMMAND_CHANNELDOWN = "channelDown";
	final static String COMMAND_RESUME = "resume";
	final static String COMMAND_STOP = "stop";
	final static String COMMAND_PAUSE = "pause";
	final static String COMMAND_RESIZE = "resize";
	final static String COMMAND_NOTIFY = "notify";
	
	final static String COMMA_SEPARATOR = ",";

	public NotificationMsg fireNotificationMessage(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this);
	}

	@Override
	public void notify(int id, String sender, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelUp(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelDown(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int id, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfiguration(String hostname, int port, TVFactory factory) {
		// TODO Auto-generated method stub
		
	}
	

}
