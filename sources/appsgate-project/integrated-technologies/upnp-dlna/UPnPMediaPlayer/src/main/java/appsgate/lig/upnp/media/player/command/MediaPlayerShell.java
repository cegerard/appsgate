package appsgate.lig.upnp.media.player.command;

import java.io.PrintWriter;


import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import appsgate.lig.upnp.media.player.adapter.MediaPlayerAdapter;

import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;


/**
 * Universal shell command for the Media player 
 * @author thibaud
 *
 */

@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "appsgate.universal.shell")
@Provides(specifications = MediaPlayerShell.class)
public class MediaPlayerShell {
    
    @Requires
    Apam apam;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "media")
	String universalShell_groupID;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "Media player commands")
	String universalShell_groupName;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
	    String[] universalShell_groupCommands = new String[] {
	    "playMedia#Play the Media URL given as parameter (served by a valid upnp media server)",
	    "resume#resume if currently paused",
	    "pause#Pause the currently running media",
	    "stop#Stop the currently running media",
	    "getVolume#Get the currently setted media volume",
	    "setVolume#Set the volume"
	    };

	private MediaPlayerAdapter retrieveMediaPlayerInstance() {
		Implementation implementation = CST.apamResolver.findImplByName(null,
			"MediaPlayerAdapter");
		return (MediaPlayerAdapter) implementation.getInst().getServiceObject();
	    
	}

	public void playMedia(PrintWriter out, String... args) {
	    MediaPlayerAdapter myPlayer=retrieveMediaPlayerInstance();
	    if(myPlayer!= null && args!=null && args.length>1) {
		myPlayer.play(args[0]);
	    }
		
	}
	
	public void resume(PrintWriter out, String... args) {
	    MediaPlayerAdapter myPlayer=retrieveMediaPlayerInstance();
	    if(myPlayer!= null) {
		myPlayer.play();
	    }
		
	}
	
	public void pause(PrintWriter out, String... args) {
	    MediaPlayerAdapter myPlayer=retrieveMediaPlayerInstance();
	    if(myPlayer!= null) {
		myPlayer.pause();
	    }
		
	}
	
	public void stop(PrintWriter out, String... args) {
	    MediaPlayerAdapter myPlayer=retrieveMediaPlayerInstance();
	    if(myPlayer!= null) {
		myPlayer.stop();
	    }
		
	}
	
	public void getVolume(PrintWriter out, String... args) {
	    MediaPlayerAdapter myPlayer=retrieveMediaPlayerInstance();
	    if(myPlayer!= null) {
		out.println("MediaPlayer device ID : "+myPlayer.getAbstractObjectId()+", current volume : "+myPlayer.getVolume());
	    }
		
	}
	
	public void setVolume(PrintWriter out, String... args) {
	    MediaPlayerAdapter myPlayer=retrieveMediaPlayerInstance();
	    if(myPlayer!= null && args!=null && args.length>1) {
		myPlayer.setVolume(Integer.parseInt(args[0]));
	    }
		
	}


		
}
