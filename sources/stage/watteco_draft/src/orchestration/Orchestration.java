package orchestration;

import java.net.UnknownHostException;

import orchestration.threads.WattecoLogThread;

/**
 * A class meant to handle the overall data flow and database logging of all 
 * sensors' values.
 * 
 * @author thalgott
 */
public class Orchestration {
	
	/** Number of minutes between two data logs, change if necessary */
	private static final double TIME_IN_MINUTES = 1.0/12.0;
	
	/** Time between two data logs (in milliseconds) */
	private static final long TIME = (long) (TIME_IN_MINUTES * 60 * 1000);

	public static void main(String[] args) throws UnknownHostException {
		
		// create and start the thread handling watteco auto-logging
		WattecoLogThread w = new WattecoLogThread(TIME);
		w.start();
	}
}
