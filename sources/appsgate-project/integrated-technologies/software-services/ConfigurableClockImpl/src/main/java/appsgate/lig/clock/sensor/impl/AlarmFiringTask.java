package appsgate.lig.clock.sensor.impl;

import java.util.TimerTask;

class AlarmFiringTask extends TimerTask {
	
	public AlarmFiringTask(ConfigurableClockImpl clock) {
		super();
		this.clock = clock;
	}

	ConfigurableClockImpl clock;
	
	@Override
	public void run() {
		clock.fireAlarms();
	}
}
