package appsgate.lig.upnp.media.player.command;

import java.io.PrintWriter;


import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.player.MediaPlayer;

import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;


/**
 * Universal shell command for the Media player 
 * @author thibaud
 *
 */

@Instantiate
@org.apache.felix.ipojo.annotations.Component(publicFactory = false, immediate = true, name = "appsgate.universal.shell")
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
	    "players#list the available players",
	    "playMedia#Play the Media URL given as parameter (served by a valid upnp media server)",
	    "resume#resume if currently paused",
	    "pause#Pause the currently running media",
	    "stop#Stop the currently running media",
	    "getVolume#Get the currently setted media volume",
	    "setVolume#Set the volume"
	    };

	private MediaPlayer retrieveMediaPlayerInstance(String player) {
		Implementation implementation = CST.apamResolver.findImplByName(null,
			"MediaPlayer");
		return (MediaPlayer) implementation.getInst(player).getServiceObject();
	    
	}

	public void players(PrintWriter out, String... args) {
		Implementation implementation = CST.apamResolver.findImplByName(null,"MediaPlayer");
		
		StringBuilder players = new StringBuilder();
		
		for (Instance playerInstance : implementation.getInsts()) {
			CoreObjectSpec player = (CoreObjectSpec) playerInstance.getServiceObject();
			players.append(playerInstance.getName()).append("(appsgate id = ").append(player.getAbstractObjectId()).append(") \n");
		}
		
		System.out.println("Currently discovered players :");
		System.out.println(players);
	}
	
	public void playMedia(PrintWriter out, String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null && args!=null && args.length>1) {
	    	myPlayer.play(args[1]);
	    }
	}
	
	public void resume(PrintWriter out, String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	myPlayer.play();
	    }
		
	}
	
	public void pause(PrintWriter out, String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	myPlayer.pause();
	    }
		
	}
	
	public void stop(PrintWriter out, String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	myPlayer.stop();
	    }
		
	}
	
	public void getVolume(PrintWriter out, String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	out.println(((CoreObjectSpec) myPlayer).getAbstractObjectId()+", current volume : "+myPlayer.getVolume());
	    }
		
	}
	
	public void setVolume(PrintWriter out, String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null && args!=null && args.length>1) {
	    	myPlayer.setVolume(Integer.parseInt(args[1]));
	    }
		
	}

		
}
