package appsgate.lig.weather.extended.impl;

import appsgate.lig.weather.exception.WeatherForecastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class SynchroObserverTask extends TimerTask {

    WeatherObserverFactory service;
    private static Logger logger = LoggerFactory
            .getLogger(SynchroObserverTask.class);


    public SynchroObserverTask(WeatherObserverFactory service) {
        super();
        this.service = service;
    }

    @Override
    public void run() {

        logger.debug("Auto refreshing observer list");
        service.synchronizeObservers();
    }

}
