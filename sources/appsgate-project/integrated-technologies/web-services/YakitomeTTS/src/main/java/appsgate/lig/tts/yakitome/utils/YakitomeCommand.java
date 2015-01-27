package appsgate.lig.tts.yakitome.utils;

import java.io.PrintStream;



import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;

import org.apache.felix.service.command.Descriptor;

/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author thibaud
 */
@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true)
@Provides(specifications = YakitomeCommand.class)
public class YakitomeCommand {

	@Requires
	Apam apam;

	@ServiceProperty(name = "osgi.command.scope", value = "TTS commands")
	String universalShell_groupName;

	@ServiceProperty(name = "osgi.command.function", value = "{}")
	String[] universalShell_groupCommands = new String[] {
    };

    PrintStream out = System.out;
    
    
}
