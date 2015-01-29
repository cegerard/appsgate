package appsgate.lig.upnp.media.player.command;

import java.io.PrintStream;


import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;
import org.osgi.service.upnp.UPnPDevice;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.player.MediaPlayer;

import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;


/**
 * Gogo shell command for the Media player
 * @author thibaud
 *
 */

@Instantiate
@org.apache.felix.ipojo.annotations.Component(publicFactory = false, immediate = true, name = "mediaplayer.gogoshell")
@Provides(specifications = MediaPlayerShell.class)
public class MediaPlayerShell {
    
    @Requires
    Apam apam;

	@ServiceProperty(name = "osgi.command.scope", value = "media")
	String gogoShell_groupID;


	@ServiceProperty(name = "osgi.command.function", value = "{}")
	    String[] gogoShell_groupCommands = new String[] {
	    "players",
	    "playMedia",
	    "resume",
	    "pause",
	    "stop",
	    "getVolume",
	    "setVolume",
	    "audioNotification"
	    };

    PrintStream out = System.out;

    private MediaPlayer retrieveMediaPlayerInstance(String player) {
		Implementation implementation = CST.apamResolver.findImplByName(null,
			"MediaPlayer");
		if(implementation == null) {
			System.out.println("Media Player Implementation not found");
			return null;
		}
		Instance inst = implementation.getInst(player);
		if(inst == null) {
			System.out.println("No Media Player Instance with name : "+player+" found");
			return null;
		}
		
		return (MediaPlayer) inst.getServiceObject();
	    
	}

    @Descriptor("list the available players")
	public void players(@Descriptor("none") String... args) {
		Implementation implementation = CST.apamResolver.findImplByName(null,"MediaPlayer");
		if(implementation == null) {
			System.out.println("Media Player Implementation not found");
			return;
		}		
		
		StringBuilder players = new StringBuilder();
		
		for (Instance playerInstance : implementation.getInsts()) {
			CoreObjectSpec player = (CoreObjectSpec) playerInstance.getServiceObject();
			
			players.append(playerInstance.getName())
			.append(" appsgate id = ").append(player.getAbstractObjectId())
			.append(" friendly name = ").append(playerInstance.getProperty(UPnPDevice.FRIENDLY_NAME))
			.append(" \n");
			
		}
		
		System.out.println("Currently discovered players :");
		System.out.println(players);
	}

    @Descriptor("Play the Media URL given as parameter (served by a valid upnp media server")
	public void playMedia(@Descriptor("none") String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null && args!=null && args.length>1) {
	    	myPlayer.play(args[1]);
	    }
	}
    
    @Descriptor("Plays an audio notification of the message in parameter (Text To Speech)")
	public void audioNotification(@Descriptor("none") String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null && args!=null && args.length>1) {
	    	myPlayer.audioNotification(args[1]);
	    }
	}

    @Descriptor("resume if currently paused")
	public void resume(@Descriptor("none") String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	myPlayer.resume();
	    }
		
	}

    @Descriptor("Pause the currently running media")
	public void pause(@Descriptor("none") String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	myPlayer.pause();
	    }
		
	}

    @Descriptor("Stop the currently running media")
	public void stop(@Descriptor("none") String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	myPlayer.stop();
	    }
		
	}

    @Descriptor("Get the currently setted media volume")
	public void getVolume(@Descriptor("none") String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null) {
	    	out.println(((CoreObjectSpec) myPlayer).getAbstractObjectId()+", current volume : "+myPlayer.getVolume());
	    }
		
	}

    @Descriptor("Set the volume")
	public void setVolume(@Descriptor("none") String... args) {
	    MediaPlayer myPlayer=retrieveMediaPlayerInstance(args[0]);
	    if(myPlayer!= null && args!=null && args.length>1) {
	    	myPlayer.setVolume(Integer.parseInt(args[1]));
	    }
		
	}

		
}
