package appsgate.lig.weather.extended.impl;

import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Specification;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by thibaud on 02/07/2014.
 */
public class WeatherObserverFactory {


    public static ExtendedWeatherObserver createObserver(final String location) {

        try {
            Implementation observerImpl = CST.componentBroker.getImpl(WeatherObserverImpl.IMPL_NAME);

            Map<String,Object> configuration = new Hashtable<String,Object>();
            configuration.put("currentLocation", location);


            WeatherObserverImpl impl = (WeatherObserverImpl) observerImpl.getApformImpl().addDiscoveredInstance(configuration).getServiceObject();

//            WeatherObserverImpl impl = (WeatherObserverImpl) observerImpl.createInstance(null, new HashMap<String, String>() {{
//                put("currentLocation", location);
//            }}).getServiceObject();

            return impl;

        } catch( Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    public void start() {

        try {
            System.out.println("\n\n Starting WeatherObserverFactory \n");

            ExtendedWeatherObserver gre = createObserver("Grenoble");
            ExtendedWeatherObserver nice = createObserver("Nice");

            System.out.println("Temperature in " + gre.getCurrentLocation() + " is " + gre.getCurrentTemperature());
        } catch (Exception exc) {
            System.out.println(" Exception occured");
            exc.printStackTrace();
        }
    }
}
