package appsgate.lig.weather.extended.impl;

import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.util.Util;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by thibaud on 02/07/2014.
 */
public class WeatherObserverFactory {

    private static Logger logger = LoggerFactory
            .getLogger(WeatherObserverFactory.class);

    public static final String LOCATIONS="org.lig.appsgate.weather.locations";


    public static ExtendedWeatherObserver createObserver(final String location) {

        try {
            Implementation observerImpl = CST.componentBroker.getImpl(WeatherObserverImpl.IMPL_NAME);

            Map<String,String> configuration = new Hashtable<String,String>();
            configuration.put("currentLocation", location);


            WeatherObserverImpl impl = (WeatherObserverImpl) observerImpl.createInstance(null, configuration);

            return impl;

        } catch( Exception exc) {
            logger.warn("Exception when creating WeatherObserver for "+location+" : "+exc.getMessage());
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
            logger.error(" Exception occured when reading the locations : "+exc.getMessage());
        }
    }
}
