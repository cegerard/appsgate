package appsgate.lig.weather.yahoo;

import java.io.PrintStream;

import appsgate.lig.weather.spec.WeatherAdapterSpec;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;

import appsgate.lig.weather.utils.WeatherCodesHelper;
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
	String[] gogoShell_groupCommands = new String[] { "weatherShow",
			"weatherFetch", "weatherLoc", "weatherCode", "weatherMin",
			"weatherMax", "weatherAvg", };

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

            WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
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

            WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
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

	@Descriptor("show weather code forecast for given location in x days")
	public void weatherCode(@Descriptor("location dayForecast") String... args) {

		for (Instance instance : CST.componentBroker.getInsts()) {

			// Only those services that implement this spec are acceptable
			if (!instance.getSpec().getName().equals("CoreWeatherServiceSpec"))
				continue;

            WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
					.getServiceObject();
			try {
				switch (args.length) {
				case 1:
					if (args[0].equals("--help"))
						out.println("syntax: weatherCode location (for today forecast)"
								+ "\nsyntax: weatherCode location number of day forecast");
					else {
						int code = meteo.getCurrentWeather(args[0]).getWeatherCode();
						out.println(" Code : "+code+", "+WeatherCodesHelper.getDescription(code));
					}
					break;
				case 2: 
					int code = meteo.getForecast(args[0]).get(Integer.parseInt(args[1])).getCode();
					out.println(" Code : "+code+", "+WeatherCodesHelper.getDescription(code));
					break;
				
				default:
					out.println("invalid number of arguments, type --help to obtain more information about the syntax");
				}
			} catch (WeatherForecastException exc) {
				exc.printStackTrace();
			}

		}
	}
	
	@Descriptor("show weather min temperature forecast for given location in x days")
	public void weatherMin(@Descriptor("location dayForecast") String... args) {

		for (Instance instance : CST.componentBroker.getInsts()) {

			// Only those services that implement this spec are acceptable
			if (!instance.getSpec().getName().equals("CoreWeatherServiceSpec"))
				continue;

            WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
					.getServiceObject();
			try {
				switch (args.length) {
				case 1:
					if (args[0].equals("--help"))
						out.println("syntax: weatherMin location (for today forecast)"
								+ "\nsyntax: weatherMin location number of day forecast");
					else {
                        int temp = meteo.getForecast(args[0]).get(0).getMin();
						out.println(" Temperature : "+temp);
					}
					break;
				case 2: 
					int temp = meteo.getForecast(args[0]).get(Integer.parseInt(args[1])).getMin();
					out.println(" Temperature : "+temp);
					break;				
				default:
					out.println("invalid number of arguments, type --help to obtain more information about the syntax");
				}
			} catch (WeatherForecastException exc) {
				exc.printStackTrace();
			}

		}
	}
	
	@Descriptor("show weather max temperature forecast for given location in x days")
	public void weatherMax(@Descriptor("location dayForecast") String... args) {

		for (Instance instance : CST.componentBroker.getInsts()) {

			// Only those services that implement this spec are acceptable
			if (!instance.getSpec().getName().equals("CoreWeatherServiceSpec"))
				continue;

            WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
					.getServiceObject();
			try {
				switch (args.length) {
				case 1:
					if (args[0].equals("--help"))
						out.println("syntax: weatherMax location (for today forecast)"
								+ "\nsyntax: weatherMax location number of day forecast");
					else {
						int temp = meteo.getForecast(args[0]).get(0).getMax();
						out.println(" Temperature : "+temp);
					}
					break;
				case 2: 
					int temp = meteo.getForecast(args[0]).get(Integer.parseInt(args[1])).getMax();
					out.println(" Temperature : "+temp);
					break;				
				default:
					out.println("invalid number of arguments, type --help to obtain more information about the syntax");
				}
			} catch (WeatherForecastException exc) {
				exc.printStackTrace();
			}

		}
	}	
	
	@Descriptor("show weather average temperature forecast for given location ")
	public void weatherAvg(@Descriptor("location dayForecast") String... args) {

		for (Instance instance : CST.componentBroker.getInsts()) {

			// Only those services that implement this spec are acceptable
			if (!instance.getSpec().getName().equals("CoreWeatherServiceSpec"))
				continue;

            WeatherAdapterSpec meteo = (WeatherAdapterSpec) instance
					.getServiceObject();
			try {
				switch (args.length) {
				case 1:
					if (args[0].equals("--help"))
						out.println("syntax: weatherAvg location (for today forecast)");
					else {
						int temp = meteo.getCurrentWeather(args[0]).getTemperature();
						out.println(" Temperature : "+temp);
					}
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
