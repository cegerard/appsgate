package appsgate.lig.clock.sensor.impl;

import java.util.TimerTask;

class RearmingPeriodicAlarmTask extends TimerTask {
	
	public RearmingPeriodicAlarmTask(ConfigurableClockImpl clock) {
		super();
		this.clock = clock;
	}

	ConfigurableClockImpl clock;

	@Override
	public void run() {
		clock.rearm();
	}
}
