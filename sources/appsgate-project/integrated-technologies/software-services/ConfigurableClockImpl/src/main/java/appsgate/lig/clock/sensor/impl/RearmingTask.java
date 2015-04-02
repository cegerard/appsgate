package appsgate.lig.clock.sensor.impl;

import java.util.TimerTask;

public class RearmingTask extends TimerTask {
	int alarmId;
	AlarmRegistry registry;

	/**
	 * 
	 */
	public RearmingTask(AlarmRegistry registry, int alarmId) {
		super();
		this.alarmId = alarmId;
		this.registry = registry;
	}

	@Override
	public void run() {
		registry.enableAlarm(alarmId);
	}

}
