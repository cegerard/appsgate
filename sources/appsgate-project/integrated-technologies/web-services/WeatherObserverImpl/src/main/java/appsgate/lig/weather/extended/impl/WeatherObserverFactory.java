package appsgate.lig.weather.extended.impl;

import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.util.Util;
import org.osgi.framework.BundleContext;
import sun.jkernel.BundleCheck;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by thibaud on 02/07/2014.
 */
public class WeatherObserverFactory {

    public static final String LOCATIONS="org.lig.appsgate.weather.locations";


    public static ExtendedWeatherObserver createObserver(final String location) {

        try {
            Implementation observerImpl = CST.componentBroker.getImpl(WeatherObserverImpl.IMPL_NAME);

            Map<String,Object> configuration = new Hashtable<String,Object>();
            configuration.put("currentLocation", location);


            WeatherObserverImpl impl = (WeatherObserverImpl) observerImpl.getApformImpl().addDiscoveredInstance(configuration).getServiceObject();

            return impl;

        } catch( Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    BundleContext context;

    public WeatherObserverFactory(BundleContext context) {
        this.context = context;
    }

    public void start() {

        try {
            for(String location : Util.split(context.getProperty(WeatherObserverFactory.LOCATIONS))) {
                createObserver(location);
            }
        } catch (Exception exc) {
            System.out.println(" Exception occured");
            exc.printStackTrace();
        }
    }
}
