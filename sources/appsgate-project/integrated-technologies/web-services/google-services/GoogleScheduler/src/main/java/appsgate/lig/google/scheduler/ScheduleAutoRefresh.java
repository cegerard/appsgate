/**
 * 
 */
package appsgate.lig.google.scheduler;

import java.util.TimerTask;

/**
 * @author thibaud
 *
 */
public class ScheduleAutoRefresh extends TimerTask {
	
	GoogleScheduler scheduler;
	
	public ScheduleAutoRefresh(GoogleScheduler scheduler) {
		this.scheduler = scheduler;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		scheduler.refreshScheduler();
	}

}
