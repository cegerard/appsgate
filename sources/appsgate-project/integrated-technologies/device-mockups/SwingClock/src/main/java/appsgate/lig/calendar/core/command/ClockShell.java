package appsgate.lig.calendar.core.command;

import java.io.PrintWriter;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

import appsgate.lig.clock.sensor.impl.SwingClock;

/**
 * Universal shell command for the SwingClock 
 * @author thibaud
 *
 */
@Instantiate
@Component(public_factory = false, immediate = true, name = "apam.universal.shell")
@Provides(specifications = ClockShell.class)
public class ClockShell {
    
    @Requires
    Apam apam;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "clock")
	String universalShell_groupID;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "Swing Clock commands")
	String universalShell_groupName;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
	    String[] universalShell_groupCommands = new String[] {
	    "show#show Swing Interface of the clock",
	    "hide#hide the Swing Interface",
	    "reset#reset clock to current system date and time" };

	private SwingClock retrieveSwingClockInstance() {
		Implementation implementation = CST.apamResolver.findImplByName(null,
			"SwingClockImpl");
		return (SwingClock) implementation.getInst().getServiceObject();
	    
	}

	public void show(PrintWriter out, String... args) {
	    SwingClock myClock=retrieveSwingClockInstance();
	    if(myClock!= null) {
		myClock.show();
	    }
		
	}
	
	public void hide(PrintWriter out, String... args) {
	    SwingClock myClock=retrieveSwingClockInstance();
	    if(myClock!= null) {
		myClock.hide();
	    }
		
	}
	
	public void reset(PrintWriter out, String... args) {
	    SwingClock myClock=retrieveSwingClockInstance();
	    if(myClock!= null) {
		myClock.resetClock();
	    }
		
	}


		
}
