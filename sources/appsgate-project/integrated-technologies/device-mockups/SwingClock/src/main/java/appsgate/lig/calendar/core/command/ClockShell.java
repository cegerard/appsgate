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

import appsgate.lig.clock.sensor.impl.SwingClock;

/**
 * Universal shell command for the SwingClock 
 * @author thibaud
 *
 */
@Instantiate
@Component(public_factory = false, immediate = true, name = "appsgate.lig.calendar.core.command")
@Provides(specifications = ClockShell.class)
public class ClockShell {
    
    @Requires
    Apam apam;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "ClockShell")
	String universalShell_groupID;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "Swing Clock commands")
	String universalShell_groupName;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
	    String[] universalShell_groupCommands = new String[] {
	    "showClock#show Swing Interface of the clock",
	    "hideClock#hide the Swing Interface",
	    "getTime#hide the Swing Interface",
	    "resetTime#reset clock to current system date and time" };

	private SwingClock retrieveSwingClockInstance() {
		Implementation implementation = CST.apamResolver.findImplByName(null,
			"SwingClockImpl");
		return (SwingClock) implementation.getInst().getServiceObject();
	    
	}

	public void showClock(PrintWriter out, String... args) {
	    SwingClock myClock=retrieveSwingClockInstance();
	    if(myClock!= null) {
		myClock.show();
	    }
		
	}
	
	public void hideClock(PrintWriter out, String... args) {
	    SwingClock myClock=retrieveSwingClockInstance();
	    if(myClock!= null) {
		myClock.hide();
	    }
		
	}
	
	public void getTime(PrintWriter out, String... args) {
	    SwingClock myClock=retrieveSwingClockInstance();
	    if(myClock!= null) {
		out.println("Clock currently defined time : "+myClock.getCurrentDate().getTime());
	    }
		
	}
	
	public void resetTime(PrintWriter out, String... args) {
	    SwingClock myClock=retrieveSwingClockInstance();
	    if(myClock!= null) {
		myClock.resetClock();
	    }
		
	}



		
}
