package appsgate.lig.clock.sensor.impl;

import java.util.TimerTask;

class AlarmFiringTask extends TimerTask {
	
	long timeStamp;
	
	public AlarmFiringTask(ConfigurableClockImpl clock, long timeStamp) {
		super();
		this.clock = clock;
		this.timeStamp = timeStamp;
	}

	ConfigurableClockImpl clock;
	
	@Override
	public void run() {
		clock.fireAlarms(timeStamp);
	}
}
