package appsgate.lig.weather.extended.impl;

import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Specification;

import java.util.HashMap;

/**
 * Created by thibaud on 02/07/2014.
 */
public class WeatherObserverFactory {


    public ExtendedWeatherObserver createObserver(final String location) {
        Implementation observerImpl = CST.componentBroker.getImpl(WeatherObserverImpl.IMPL_NAME);
        WeatherObserverImpl impl = (WeatherObserverImpl)observerImpl.createInstance(null, new HashMap<String,String>(){{put("currentLocation", location);}} ).getServiceObject();
        return impl;
    }

    public void start() {
        createObserver("Grenoble");
        System.out.println("################# CREATED WEATHER OBSERVER ####################");
    }
}
