package appsgate.lig.assembly;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import appsgate.lig.mail.Mail;
import appsgate.lig.weather.*;;

public class Main {

	Integer refreshRate=5000;
	WeatherForecast meteo;
	Mail mail;
	Integer lastNotifiedValue=-1;
	
	private Logger logger=Logger.getLogger(Main.class.getSimpleName());
	
	private Timer refreshTimer = new Timer();
	
	TimerTask refreshtask = new TimerTask(){
		@Override
		public void run() {
			logger.info("Checking conditions..");
			Main.this.check();
		}
	};
	
	public void start(){
		System.out.println("test app started");
		refreshTimer.scheduleAtFixedRate(refreshtask, 0, refreshRate);
		
	}
	
	public void stop(){
		System.out.println("test app stopped");
		refreshtask.cancel();
	}
	
	public void check(){
		
		try {
			Integer  temperature = meteo.getCurrentTemperature();
			if(temperature.intValue()>35 && lastNotifiedValue!=temperature){
				System.out.println(String.format("Temperature is too right %s, sending alert!",  temperature));
				lastNotifiedValue=meteo.getCurrentTemperature();
				mail.sendMailSimple("jbotnascimento@gmail.com", "alert!", "temperature is too high!");
			}
		} catch (WeatherForecastException e) {
			e.printStackTrace();
		}
		
		
	}
	
}