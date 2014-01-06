package appsgate.lig.weather.yahoo;

import java.util.TimerTask;
import java.util.logging.Logger;

import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;

public class WeatherRefreshTask extends TimerTask {
	
	int refreshRate;
	
	CoreWeatherServiceSpec service;
	private Logger logger = Logger.getLogger(WeatherRefreshTask.class
			.getSimpleName());
	

	public WeatherRefreshTask(CoreWeatherServiceSpec service, int refreshRate) {
		super();
		this.refreshRate=refreshRate;
		this.service = service;
	}

	@Override
	public void run() {

		if (refreshRate != -1) {
				logger.fine("Refreshing meteo data");
			try {
				service.fetch();
			} catch (WeatherForecastException exc) {
				exc.printStackTrace();
			}

		}
	}

}
