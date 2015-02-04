package appsgate.lig.upnp.media.player.command;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.player.MediaPlayer;
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.Specification;


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
		Instance inst = CST.apamResolver.findInstByName(null,
				player);

		if(inst == null) {
			System.out.println("No Media Player Instance with name : "+player+" found");
			return null;
		}
		
		return (MediaPlayer) inst.getServiceObject();
	    
	}
    
    public Set<Instance> getInsts() {
		Specification spec = CST.apamResolver.findSpecByName(null,"CoreMediaPlayerSpec");
		if(spec == null
				|| spec.getImpls() != null
				|| spec.getImpls().isEmpty()) {
			System.out.println("no Implems of CoreMediaPlayerSpec not found");
		}
    	Set<Implementation> impls = spec.getImpls();
    	Set<Instance> insts = new HashSet<Instance>();
    	
    	for(Implementation impl : impls) {
    		insts.addAll(impl.getInsts());
    	}
    	return insts;
    }
    

    @Descriptor("list the available players")
	public void players(@Descriptor("none") String... args) {
    	Set<Instance> insts = getInsts();
		if(insts == null
				|| insts.isEmpty()) {
			System.out.println("no Instance of CoreMediaPlayerSpec not found");
			return;
		}		
		
		StringBuilder players = new StringBuilder();
		
		for (Instance playerInstance : insts) {
			CoreObjectSpec player = (CoreObjectSpec) playerInstance.getServiceObject();
			
			players.append(playerInstance.getName())
			.append(" appsgate id = ").append(player.getAbstractObjectId())
			.append(" name = ").append(playerInstance.getName())
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
