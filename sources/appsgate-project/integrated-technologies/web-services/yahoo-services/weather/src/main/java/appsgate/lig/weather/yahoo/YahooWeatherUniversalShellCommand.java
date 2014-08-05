package appsgate.lig.weather.yahoo;

import java.io.PrintWriter;

import appsgate.lig.weather.spec.WeatherAdapterSpec;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import appsgate.lig.weather.exception.WeatherForecastException;
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;

@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "appsgate.universal.shell")
@Provides(specifications = YahooWeatherUniversalShellCommand.class)
/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author jnascimento
 */
public class YahooWeatherUniversalShellCommand {

    @Requires
    Apam apam;

    @ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "weather")
    String universalShell_groupID;

    @ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "weather commands")
    String universalShell_groupName;

    @ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
    String[] universalShell_groupCommands = new String[] {
	    "weatherShow#show last collected meteo data",
	    "weatherFetch#show data to be fetched from the meteo provider",
	    "weatherLoc#create, remove or show current locations monitored by weather service" };

    public void weatherShow(PrintWriter out, String... args) {

	for (Instance instance : CST.componentBroker.getInsts()) {

	    // Only those services that implement this spec are acceptable
	    if (!instance.getSpec().getName().equals("WeatherAdapterSpec"))
		continue;

	    out.print(String.format("Apam-Instance: %s\n", instance.getName()));

	    WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
		    .getServiceObject();

	    out.println(meteo);

	    out.print(String.format("/Apam-Instance: %s \n", instance.getName()));

	}

    }

    public void weatherFetch(PrintWriter out, String... args) {

	for (Instance instance : CST.componentBroker.getInsts()) {

	    // Only those services that implement this spec are acceptable
	    if (!instance.getSpec().getName().equals("WeatherAdapterSpec"))
		continue;

        WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
		    .getServiceObject();
	    try {
		meteo.fetch();
	    } catch (WeatherForecastException exc) {
		exc.printStackTrace();
	    }

	}

    }

    public void weatherLoc(PrintWriter out, String... args) {

	for (Instance instance : CST.componentBroker.getInsts()) {

	    // Only those services that implement this spec are acceptable
	    if (!instance.getSpec().getName().equals("WeatherAdapterSpec"))
		continue;

        WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
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
			out.println("syntax: meteoLoc with no arguments shows all location currently monitored by weather service"
				+ "\nsyntax: meteoLoc [location] try to add the location if not currently monitored OR remove it il already monitored");
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
