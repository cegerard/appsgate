package appsgate.lig.media.player.jlayer;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javazoom.jl.player.Player;

public class PlayerWrapperThread extends Thread {
	

	private final static Logger logger = LoggerFactory
			.getLogger(PlayerWrapperThread.class);
	
	String playStatus = STOPPED;
	
	final static String STOPPED = "STOPPED";
	final static String PLAYING = "PLAYING";

	Player player;
	String mediaURL;
	boolean configured = false;

	public boolean configure(String mediaURL) {
		logger.trace("configure(String mediaURL : {}) ", mediaURL);
		if (mediaURL == null || mediaURL.isEmpty()) {
			logger.warn("configure(...), no URL provided ");
		} else {
			try {
				URL url = new URL(mediaURL);
				InputStream stream = null;
				if (mediaURL.startsWith("https")) {
					logger.trace("configure(...), https url, special case to avoid 403 error ");
					HttpsURLConnection httpscon = (HttpsURLConnection) url
							.openConnection();
					httpscon.addRequestProperty("user-agent", "Mozilla/5.0");
					stream = httpscon.getInputStream();
				} else {
					logger.trace("configure(...), regular (?) url, opening stream");
					stream = url.openStream();
				}
				player = new Player(stream);
				logger.trace("configure(...),"
						+ " player created successfully"
						+", now trying to play a single frame to check everything is ok");
				player.play(1);

				this.mediaURL = mediaURL;
				configured = true;

			} catch (Exception exc) {
				logger.error("Exception occured during creation of player:"
						+ exc.getMessage());
				player = null;
				configured = false;
			}
		}
		logger.trace("configure(...), returning "+configured);
		return configured;
	}

	@Override
	public void run() {
		if (configured && player != null) {
			logger.trace("run(), player should be ready to play");
			try {
				playStatus = PLAYING;
				player.play();
				
			} catch (Exception e) {
				logger.error("Exception occured during play"
						+ e.getMessage());
				player = null;
				configured = false;
			}
		} else {
			logger.trace("run(), player not configured properly, cannot play");
		}
		playStatus = STOPPED;
	}
	
	public void close() {
		if(player != null) {
			player.close();
			player = null;
			logger.trace("stop(), player closed and set to null");			
		}
		playStatus = STOPPED;
	}
	
	public String getPlayStatus() {
		logger.trace("getPlayStatus(), returning "+playStatus);
		return playStatus;
	}

}
