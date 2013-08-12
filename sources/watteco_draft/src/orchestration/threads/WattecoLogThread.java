package orchestration.threads;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import persistence.Persistence;
import watteco.border_router.BorderRouter;
import watteco.sensors.SmartPlug;
import watteco.sensors.SmartPlugValue;

/**
 * A class meant to log the values of all connected watteco sensors at regular 
 * intervals using a dedicated thread. 
 * 
 * @author thalgott
 */
public class WattecoLogThread extends Thread {
	
	/* ***********************************************************************
	 * 							    ATTRIBUTES                               *
	 *********************************************************************** */

	/** Main border router */
	private BorderRouter 	br;
	/** List of all the smart plugs in use */
	private List<SmartPlug> sp 		= new LinkedList<SmartPlug>();
	/** Default timer separating two data logging */
	private long 			timer 	= 1000;
	
	/* ***********************************************************************
	 * 							   CONSTRUCTORS                              *
	 *********************************************************************** */
	
	/**
	 * Initiates thread values.
	 * 
	 * @param timer time in milliseconds between two sensor information logging
	 */
	public WattecoLogThread(long timer) {
		this.timer = timer;
		// initiate the border router
		br = new BorderRouter();
		try {
			// add all connected smart plugs
			sp.add(new SmartPlug("aaaa::10:294"));
			sp.add(new SmartPlug("aaaa::10:2CB"));
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/* ***********************************************************************
	 * 							 PUBLIC FUNCTIONS                            *
	 *********************************************************************** */

	/**
	 * Runs the thread, initiating the database logging of the values for all
	 * the smart plugs.
	 */
	public void run() {
		while (true) {
			// get a common date for this particular logging session
			Date currentDate = new Date(System.currentTimeMillis());
			try {
				for (SmartPlug plug: sp) {
					// retrieve and store the values for each smart plug
					SmartPlugValue spv = br.readAttribute(plug);
					Persistence.insert(spv, currentDate);
//					TODO: remove: debug-purpose function
					printValues(spv);
				}
				Thread.sleep(timer);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/* ***********************************************************************
	 * 							PRIVATE FUNCTIONS                            *
	 *********************************************************************** */
	
	/**
	 * debug-purpose function
	 */
	private static void printValues(SmartPlugValue spv) {
		System.out.println("Active energy: " + spv.activeEnergy);
		System.out.println("Number of sample: " + spv.nbOfSamples);
		System.out.println("Active power: " + spv.activePower);
		System.out.println("-------------------------------------------------");
	}
}
