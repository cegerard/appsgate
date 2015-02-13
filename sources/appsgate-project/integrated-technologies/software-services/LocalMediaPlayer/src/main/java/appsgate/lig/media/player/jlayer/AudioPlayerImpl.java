package appsgate.lig.media.player.jlayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.tts.CoreTTSService;
import appsgate.lig.upnp.media.player.MediaPlayer;

public class AudioPlayerImpl extends CoreObjectBehavior implements MediaPlayer, CoreObjectSpec {
		
	PlayerWrapperThread playerWrapper;
	
	CoreTTSService ttsService;

	/**
	 * Core Object Spec properties
	 */
	private static final String appsgateUserType= "31";
	private static final String appsgateStatus = "2";
	private static final String appsgateServiceName = "Local Audio player";
	private static final String deviceId="LocalAudioPlayer";
	
	/**
	 * The volume cannot be changed on the player, we assume always the max value
	 */
	private static final String currentVolume = "100";


	private final static Logger logger = LoggerFactory.getLogger(AudioPlayerImpl.class);
	/**
	 * The currently playing media
	 */
	private String	currentMediaURL;
	private String	currentMediaName;
	
	String mediaURL;
	
	public void play(String mediaURL) {
		logger.trace("play(String mediaURL :{}) ", mediaURL);
		play(mediaURL, null);

	}


	@Override
	public String getAbstractObjectId() {
		return deviceId;
	}

	@Override
	public String getUserType() {
		return appsgateUserType;
	}


	@Override
	public int getObjectStatus() {
		return Integer.parseInt(appsgateStatus);
	}


	@Override
	public String getPictureId() {
		// useless an to be deprecated
		return null;
	}


	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		// mandatory appsgate properties
		descr.put("id", getAbstractObjectId());
		descr.put("type", getUserType());
		descr.put("status", appsgateStatus);
		descr.put("sysName", appsgateServiceName);
		descr.put("friendlyName", appsgateServiceName);
		descr.put("volume", getVolume());
		descr.put("playerStatus", getPlayerStatus());
		descr.put("mediaName", currentMediaName);
		descr.put("mediaURL", currentMediaURL);

		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
		// useless and to be deprecated		
	}


	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}


	@Override
	public void play(String mediaURL, String mediaName) {
		logger.trace("play(String mediaURL : {}, String mediaName : {})");
		this.currentMediaURL = mediaURL;
		this.currentMediaName = mediaName;

		playerWrapper = new PlayerWrapperThread();
		if(playerWrapper.configure(mediaURL)) {
			playerWrapper.start();
		} else {
			logger.trace("play(), not properly configured");			
		}
	}

	
	@Override	
	public void audioNotification(String message) {
		logger.trace("audioNotification(String message: {})", message);
		audioNotification(message, null, -1);
	}
		
	@Override
	public void audioNotification(String message, String voice, int speed) {
		logger.trace("audioNotification(String message: {}, "
				+ "String voice : {}, int speed : {})", message, voice, speed);
		if(ttsService != null) {
			logger.trace("audioNotification(...), tts service found");
			

			int book_id;
			if(voice == null || speed == -1) {
				book_id = ttsService.waitForTTSGeneration(message);
			} else {
				book_id = ttsService.waitForTTSGeneration(message, voice, speed);
			}
			if(book_id>0) {
				logger.trace("audioNotification(...), tts generation done, book_id: "+book_id);
				String url = ttsService.getAudioURL(book_id, 0);
				if(url != null && !url.isEmpty()) {
					logger.trace("audioNotification(...), found url : "+url);
					play(url, "Audio Notification: "+message);
				} else {
					logger.warn("audioNotification(...),"
							+ " url is null");					
				}
			} else {
				logger.warn("audioNotification(...),"
						+ " problem during tts generation will not play audio file");				
			}
		} else {
			logger.warn("audioNotification(...),"
					+ " no tts service found, will not play audio file");
		}
	}
	

	@Override
	public void resume() {
		logger.trace("resume(), unsupported for the moment, calling play");	
		play(currentMediaURL, currentMediaName);
	}

	@Override
	public void pause() {
		logger.trace("pause(), unsupported for the moment, calling stop");
		stop();
	}

	@Override
	public void stop() {
		logger.trace("stop()");
		if(playerWrapper != null) {
			playerWrapper.close();
			playerWrapper = null;
			logger.trace("stop(), player closed and set to null");			
		} else {
			logger.trace("stop(), nothing to stop");			
		}
	}

	@Override
	public int getVolume() {
		// as we cannot get/set the volume, we assume 100
		return Integer.parseInt(currentVolume);
	}


	@Override
	public void setVolume(int level) {
		// Do nothing, we cannot set the volume on this kind of player, assuming 100
		logger.trace("setVolume(int level),"
				+ " unsupported for the moment, always setting "+currentVolume);
		stateChanged("volume", currentVolume, currentVolume);
	}

	@Override
	public String getPlayerStatus() {
		logger.trace("getPlayerStatus()");
		if(playerWrapper != null) {
			logger.trace("getPlayerStatus(), returning "+playerWrapper.getPlayStatus());
			return playerWrapper.getPlayStatus();
		}
		logger.trace("getPlayerStatus(), returning "+PlayerWrapperThread.STOPPED);
		return PlayerWrapperThread.STOPPED;	}

	@Override
	public String getCurrentMediaName() {
		return mediaURL;
	}
	
	@SuppressWarnings("unused")
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this);
	}	

}
