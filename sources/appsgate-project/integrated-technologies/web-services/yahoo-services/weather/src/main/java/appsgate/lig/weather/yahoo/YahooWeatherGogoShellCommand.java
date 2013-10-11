package appsgate.lig.weather.yahoo;

import java.io.PrintStream;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;

import appsgate.lig.weather.spec.CoreWeatherServiceSpec;
import appsgate.lig.weather.exception.WeatherForecastException;
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;


@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "appsgate.universal.shell")
@Provides(specifications = YahooWeatherGogoShellCommand.class)
/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author Thibaud
 */
public class YahooWeatherGogoShellCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "weather")
    String gogoShell_groupName;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] gogoShell_groupCommands = new String[] {
	    "weatherShow",
	    "weatherFetch",
	    "weatherLoc",
	    };
    
    @Requires
    Apam apam;

    PrintStream out = System.out;
    
    
    @Descriptor("show last collected weather data")
    public void weatherShow(@Descriptor("none") String... args) {

	for (Instance instance : CST.componentBroker.getInsts()) {

	    // Only those services that implement this spec are acceptable
	    if (!instance.getSpec().getName().equals("CoreWeatherServiceSpec"))
		continue;

	    out.print(String.format("Apam-Instance: %s\n", instance.getName()));

	    CoreWeatherServiceSpec meteo = (CoreWeatherServiceSpec) instance
		    .getServiceObject();

	    out.println(meteo);

	    out.print(String.format("/Apam-Instance: %s \n", instance.getName()));

	}

    }

    @Descriptor("show data to be fetched from the meteo provider")
    public void weatherFetch(@Descriptor("none") String... args) {

	for (Instance instance : CST.componentBroker.getInsts()) {

	    // Only those services that implement this spec are acceptable
	    if (!instance.getSpec().getName().equals("CoreWeatherServiceSpec"))
		continue;

	    CoreWeatherServiceSpec meteo = (CoreWeatherServiceSpec) instance
		    .getServiceObject();
	    try {
		meteo.fetch();
	    } catch (WeatherForecastException exc) {
		exc.printStackTrace();
	    }

	}

    }

    @Descriptor("create, remove or show current locations monitored by weather service")
    public void weatherLoc(@Descriptor("target location") String... args) {

	for (Instance instance : CST.componentBroker.getInsts()) {

	    // Only those services that implement this spec are acceptable
	    if (!instance.getSpec().getName().equals("CoreWeatherServiceSpec"))
		continue;

	    CoreWeatherServiceSpec meteo = (CoreWeatherServiceSpec) instance
		    .getServiceObject();
	    try {
		switch (args.length) {
		case 0:
		    String[] locations = meteo.getLocations();
		    if (locations != null && locations.length > 0)
			for (String loc : locations)
			    out.println(loc);
		    break;
		case 1:
		    if (args[0].equals("--help"))
			out.println("syntax: weatherLoc with no arguments shows all location currently monitored by weather service"
				+ "\nsyntax: weatherLoc [location] try to add the location if not currently monitored OR remove it il already monitored");
		    else if (meteo.containLocation(args[0]))
			meteo.removeLocation(args[0]);
		    else
			meteo.addLocation(args[0]);
		    break;
		default:
		    out.println("invalid number of arguments, type --help to obtain more information about the syntax");
		}
	    } catch (WeatherForecastException exc) {
		exc.printStackTrace();
	    }

	}

    }

}
