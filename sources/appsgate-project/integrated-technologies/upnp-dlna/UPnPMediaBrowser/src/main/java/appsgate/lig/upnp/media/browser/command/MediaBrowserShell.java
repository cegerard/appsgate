package appsgate.lig.upnp.media.browser.command;

import java.io.PrintWriter;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.browser.MediaBrowser;
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
@org.apache.felix.ipojo.annotations.Component(publicFactory = false, immediate = true, name = "appsgate.universal.shell")
@Provides(specifications = MediaBrowserShell.class)
public class MediaBrowserShell {

    @Requires
    Apam apam;

    @ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "media")
    String universalShell_groupID;

    @ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "Media player commands")
    String universalShell_groupName;

    @ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
    String[] universalShell_groupCommands = new String[]{
        "listMedias#list the media available on this network",
        "browsers#list the available browsers",
        "browse#browse the server",};

    private MediaBrowser retrieveMediaBrowserInstance(String player) {
        Implementation implementation = CST.apamResolver.findImplByName(null,
                "MediaBrowser");
        return (MediaBrowser) implementation.getInst(player).getServiceObject();

    }
    
    public void browsers(PrintWriter out, String... args) {
        
         Implementation implementation = CST.apamResolver.findImplByName(null, "MediaBrowser");

         StringBuilder browsers = new StringBuilder();

         for (Instance playerInstance : implementation.getInsts()) {
         CoreObjectSpec player = (CoreObjectSpec) playerInstance.getServiceObject();
         browsers.append(playerInstance.getName()).append("(appsgate id = ").append(player.getAbstractObjectId()).append(") \n");
         }

         System.out.println("Currently discovered browsers:");
         System.out.println(browsers);
         
    }

    public void browse(PrintWriter out, String... args) {
        MediaBrowser myBrowser = retrieveMediaBrowserInstance(args[0]);
        if (myBrowser != null && args != null) {
            String objectId = args.length > 1 ? args[1] : "0";
            long start = args.length > 2 ? Long.valueOf(args[2]) : 0L;
            long count = args.length > 3 ? Long.valueOf(args[3]) : 0L;

            System.out.println(myBrowser.browse(objectId, MediaBrowser.BROWSE_CHILDREN, "*", start, count, ""));
        }
    }

}
