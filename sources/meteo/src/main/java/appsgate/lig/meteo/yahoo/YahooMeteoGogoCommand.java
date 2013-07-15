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
/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author jnascimento
 */
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
		
		for(Instance instance:CST.componentBroker.getInsts()){
			
			//Only those services that implement this spec are acceptable
			if(!instance.getSpec().getName().equals("meteo-service-specification")) continue;
			
			out.print(String.format("Apam-Instance: %s\n", instance.getName()));
			
			Meteo meteo=(Meteo)instance.getServiceObject();
		
			out.println(meteo);
			
			out.print(String.format("/Apam-Instance: %s \n", instance.getName()));
			
		}
		
	}
	
	public void meteoFetch(PrintWriter out, String... args){
	
		for(Instance instance:CST.componentBroker.getInsts()){
			
			//Only those services that implement this spec are acceptable
			if(!instance.getSpec().getName().equals("meteo-service-specification")) continue;
			
			Meteo meteo=(Meteo)instance.getServiceObject();
			
			meteo.fetch();
			
		}
		
	}
	
}
