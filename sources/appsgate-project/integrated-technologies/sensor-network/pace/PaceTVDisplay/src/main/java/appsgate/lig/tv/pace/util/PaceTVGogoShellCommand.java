package appsgate.lig.tv.pace.util;

import java.io.PrintStream;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;

import appsgate.lig.tv.pace.PaceTVImpl;
import appsgate.lig.tv.spec.CoreTVSpec;
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;

@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "appsgate.gogo.shell")
@Provides(specifications = PaceTVGogoShellCommand.class)
/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author Thibaud
 */
public class PaceTVGogoShellCommand {

	@ServiceProperty(name = "osgi.command.scope", value = "pace")
	String gogoShell_groupName;

	@ServiceProperty(name = "osgi.command.function", value = "{}")
	String[] gogoShell_groupCommands = new String[] { "channelUp",
			"channelDown", "resume", "pause", "stop",
			"resize", "notify", "checkConfiguration", };

	@Requires
	Apam apam;

	PrintStream out = System.out;
	
	/**
	 * Helper method that retrieves the first CoreTVSpec instance
	 * @return
	 */
	private CoreTVSpec getCoreTVSpec() {
		for (Instance instance : CST.componentBroker.getInsts()) {

			// Only those services that implement this spec are acceptable
			if (!instance.getSpec().getName().equals("CoreTVSpec"))
				continue;

			out.print(String.format("Apam-Instance: %s\n", instance.getName()));

            return (CoreTVSpec) instance.getServiceObject();
		}
		return null;
		
	}

	@Descriptor("command: channelUp, switch to the next channel")
	public void channelUp(@Descriptor("screen id (0/1) - (optional, by default id=0) ") String... args) {
		
		CoreTVSpec service = getCoreTVSpec();
		if(service != null) {
			if(args.length == 1 ) {
				service.channelUp(Integer.parseInt(args[0]));
			} else {
				service.channelUp(0);
			}
		} else {
			out.println("no Apam-Instance for CoreTVSpec");
		}
	}
	
	@Descriptor("command: channelDown, switch to the previous channel")
	public void channelDown(@Descriptor("screen id (0/1) - (optional, by default id=0) ") String... args) {
		
		CoreTVSpec service = getCoreTVSpec();
		if(service != null) {
			if(args.length == 1 ) {
				service.channelDown(Integer.parseInt(args[0]));
			} else {
				service.channelDown(0);
			}
		} else {
			out.println("no Apam-Instance for CoreTVSpec");
		}
	}
	
	@Descriptor("command: resume, resume video")
	public void resume(@Descriptor("screen id (0/1) - (optional, by default id=0) ") String... args) {
		
		CoreTVSpec service = getCoreTVSpec();
		if(service != null) {
			if(args.length == 1 ) {
				service.resume(Integer.parseInt(args[0]));
			} else {
				service.resume(0);
			}
		} else {
			out.println("no Apam-Instance for CoreTVSpec");
		}
	}
	
	@Descriptor("command: stop, stop video playback")
	public void stop(@Descriptor("screen id (0/1) - (optional, by default id=0) ") String... args) {
		
		CoreTVSpec service = getCoreTVSpec();
		if(service != null) {
			if(args.length == 1 ) {
				service.stop(Integer.parseInt(args[0]));
			} else {
				service.stop(0);
			}
		} else {
			out.println("no Apam-Instance for CoreTVSpec");
		}
	}	
	
	@Descriptor("command: pause, pause video")
	public void pause(@Descriptor("screen id (0/1) - (optional, by default id=0) ") String... args) {
		
		CoreTVSpec service = getCoreTVSpec();
		if(service != null) {
			if(args.length == 1 ) {
				service.pause(Integer.parseInt(args[0]));
			} else {
				service.pause(0);
			}
		} else {
			out.println("no Apam-Instance for CoreTVSpec");
		}
	}		
	
	@Descriptor("command: resize, resize video")
	public void resize(@Descriptor("id x y width height ") String... args) {
		
		CoreTVSpec service = getCoreTVSpec();
		if(service != null) {
			if(args.length == 5 ) {
				service.resize(Integer.parseInt(args[0]),
						Integer.parseInt(args[1]),
						Integer.parseInt(args[2]),
						Integer.parseInt(args[3]),
						Integer.parseInt(args[4]));
			} else {
				out.println("wrong number of arguments : id x y width height");
			}
		} else {
			out.println("no Apam-Instance for CoreTVSpec");
		}
	}
	
	@Descriptor("command: notify, notify a message on the STB")
	public void notify(@Descriptor("id sender message ") String... args) {
		
		CoreTVSpec service = getCoreTVSpec();
		if(service != null) {
			if(args.length == 3 ) {
				service.notify(Integer.parseInt(args[0]),
						args[1],
						args[2]);
			} else {
				out.println("wrong number of arguments : id id sender message");
			}
		} else {
			out.println("no Apam-Instance for CoreTVSpec");
		}
	}
	
	@Descriptor("command: checkConfiguration, check if TV service is responding")
	public void checkConfiguration(@Descriptor("hostname port path (optional, will use default hostname : localhost, port : 80 and no path) ") String... args) {
		String hostname = PaceTVImpl.DEFAULT_HOSTNAME;
		int port = PaceTVImpl.DEFAULT_HTTPPORT;
		String path = null;
		
		if(args.length >= 2 ) {
			hostname = args[0];
			port = Integer.parseInt(args[1]);
			if (args.length == 3) {
				path = args[2];
			}
		}
		
		if (PaceTVImpl.checkConfiguration(hostname, port, path)) {
			out.println("checkConfiguration returned true");
		} else {
			out.println("checkConfiguration returned false");
		}
	}	
	
}
