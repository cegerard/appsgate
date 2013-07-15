package appsgate.lig.meteo.yahoo;

import java.io.PrintWriter;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import appsgate.lig.meteo.Meteo;
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;

@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "apam.universal.shell")
@Provides(specifications = YahooMeteoGogoCommand.class)
public class YahooMeteoGogoCommand {

	@Requires
	Apam apam;
	
	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "meteo")
	String universalShell_groupID;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "meteo commands")
	String universalShell_groupName;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
	String[] universalShell_groupCommands = new String[] {
			"meteoShow#show last collected meteo data",
			"meteoFetch#show data to be fetched from the meteo provider"
	};

	public void meteoShow(PrintWriter out, String... args){
		
		Instance instance=CST.apamResolver.findInstByName(null, "meteo-0");
		Meteo meteo=(Meteo)instance.getServiceObject();
		
		out.println(meteo);
		
	}
	
	public void meteoFetch(PrintWriter out, String... args){
		
		Instance instance=CST.apamResolver.findInstByName(null, "meteo-0");
		Meteo meteo=(Meteo)instance.getServiceObject();
		
		meteo.fetch();
		
	}
	
}
