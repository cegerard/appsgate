package appsgate.lig.upnp.media.browser.command;

import java.io.PrintStream;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;
import org.osgi.service.upnp.UPnPDevice;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.browser.MediaBrowser;
import appsgate.lig.upnp.media.player.MediaPlayer;
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

/**
 * Universal shell command for the Media browser
 *
 * @author thibaud
 *
 */
@Instantiate
@org.apache.felix.ipojo.annotations.Component(publicFactory = false, immediate = true, name = "mediabrowser.gogoshell")
@Provides(specifications = MediaBrowserShell.class)
public class MediaBrowserShell {

    @Requires
    Apam apam;

    @ServiceProperty(name = "osgi.command.scope", value = "media")
    String gogoShell_groupID;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] gogoShell_groupCommands = new String[]{
            "browsers",
            "browse",};

    PrintStream out = System.out;



    private MediaBrowser retrieveMediaBrowserInstance(String browser) {
		Implementation implementation = CST.apamResolver.findImplByName(null,
				"MediaBrowser");
			if(implementation == null) {
				System.out.println("Media Browser Implementation not found");
				return null;
			}
			Instance inst = implementation.getInst(browser);
			if(inst == null) {
				System.out.println("No Media Browser Instance with name : "+browser+" found");
				return null;
			}
			
			return (MediaBrowser) inst.getServiceObject();    	
    }


    @Descriptor("list the available browsers")
    public void browsers(@Descriptor("none") String... args) {

        Implementation implementation = CST.apamResolver.findImplByName(null, "MediaBrowser");
		if(implementation == null) {
			System.out.println("Media Browser Implementation not found");
			return ;
		}

        StringBuilder browsers = new StringBuilder();

        for (Instance browserInstance : implementation.getInsts()) {
            CoreObjectSpec browser = (CoreObjectSpec) browserInstance.getServiceObject();
            browsers.append(browserInstance.getName())
                    .append(" appsgate id = ").append(browser.getAbstractObjectId())
                    .append(" friendly name = ").append(browserInstance.getProperty(UPnPDevice.FRIENDLY_NAME))
                    .append(" \n");
        }

        System.out.println("Currently discovered browsers:");
        System.out.println(browsers);

    }

    @Descriptor("browse the server")
    public void browse(@Descriptor("none") String... args) {
        MediaBrowser myBrowser = retrieveMediaBrowserInstance(args[0]);
        if (myBrowser != null && args != null) {
            String objectId = args.length > 1 ? args[1] : "0";
            long start = args.length > 2 ? Long.valueOf(args[2]) : 0L;
            long count = args.length > 3 ? Long.valueOf(args[3]) : 0L;

            System.out.println(myBrowser.browse(objectId, MediaBrowser.BROWSE_CHILDREN, "*", start, count, ""));
        }
    }

}
