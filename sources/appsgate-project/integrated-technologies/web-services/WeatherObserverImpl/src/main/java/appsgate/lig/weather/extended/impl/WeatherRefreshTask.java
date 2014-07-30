package appsgate.lig.weather.extended.impl;

import java.util.TimerTask;

import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.spec.WeatherAdapterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherRefreshTask extends TimerTask {

    WeatherObserverImpl service;
    private static Logger logger = LoggerFactory
            .getLogger(WeatherRefreshTask.class);


    public WeatherRefreshTask(WeatherObserverImpl service) {
        super();
        this.service = service;
    }

    @Override
    public void run() {

        logger.debug("Auto refreshing Weather data");
        try {
            service.refresh();
        } catch (WeatherForecastException exc) {
            exc.printStackTrace();
        }
    }

}
