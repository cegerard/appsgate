/**
 * 
 */
package appsgate.lig.google.scheduler;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.scheduler.SchedulingException;

/**
 * @author thibaud
 *
 */
public class ScheduleAutoRefresh extends TimerTask {
	
	GoogleScheduler scheduler;
	
	private static Logger logger = LoggerFactory.getLogger(ScheduleAutoRefresh.class);
	
	
	public ScheduleAutoRefresh(GoogleScheduler scheduler) {
		this.scheduler = scheduler;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		try {
			// First, set the next timer task
			scheduler.refreshTask();
			
			// Then, refresh the calendar
			scheduler.refreshScheduler();
		} catch(SchedulingException exc) {
			logger.error("Error while refreshing the Scheduler : "+exc.getMessage());
		}
	}

}
