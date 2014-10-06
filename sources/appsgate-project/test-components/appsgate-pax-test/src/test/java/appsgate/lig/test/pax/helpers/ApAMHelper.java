package appsgate.lig.test.pax.helpers;
/**
 * 
 */

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.Specification;


/**
 * @author thibaud
 *
 */


public class ApAMHelper {
	
		
	public static long waitPeriod = 200;    
    public static long RESOLVE_TIMEOUT = 3000;
    
    
    public static void waitForIt(long timeout) {
		long sleep = 0;
		while (sleep < timeout) {

				try {
					Thread.sleep(waitPeriod);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			sleep += waitPeriod;
		}

    }

    
	public static void waitForApam(long timeout) {
		boolean apamReady = false;
		long sleep = 0;
		while (!apamReady && sleep < timeout) {
			if (CST.componentBroker != null && CST.apamResolver != null
					&& CST.apam != null) {

				apamReady = true;
			} else {
				try {
					Thread.sleep(waitPeriod);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			sleep += waitPeriod;
		}
		boolean foundAPAM = false;
		while (sleep < timeout && !foundAPAM) {
			try {
				Thread.sleep(waitPeriod);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			sleep += waitPeriod;
			if (CST.apamResolver != null) {
				if (CST.apamResolver.findInstByName(null, "APAM-Instance") != null) {
					// && CST.apamResolver.findInstByName(null,
					// "OSGiMan-Instance") != null
					// && CST.apamResolver.findInstByName(null,
					// "ConflictManager-Instance") != null)
					foundAPAM = true;
				}
			}
		}
	}
	
	public static Component waitForComponentByName(Component client,
			String componentName, long timeout) {
		waitForApam(RESOLVE_TIMEOUT);

		Component comp = CST.apamResolver.findComponentByName(client,
				componentName);
		long sleep = 0;

		while (sleep < timeout && comp == null) {
			try {
				Thread.sleep(waitPeriod);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			sleep += waitPeriod;
			if (CST.apamResolver != null) {
				comp = CST.apamResolver.findComponentByName(client,
						componentName);
			}
		}

		return comp;
	}
	
	public static Instance waitForInstanceByImplemName(Component client,
			String implemName, long timeout) {
		waitForApam(RESOLVE_TIMEOUT);
		
		Implementation implem = (Implementation)waitForComponentByName(client,
				implemName, timeout);
		long sleep = 0;
		Instance inst = null;

		while (sleep < timeout && inst == null) {
			try {
				Thread.sleep(waitPeriod);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			sleep += waitPeriod;
			if (CST.apamResolver != null) {
				inst = implem.getInst();				
			}
		}

		return inst;
	}		

	public static void listSpecs() {
		System.out.println(String.format(
				"------------ Specifications (Total:%d) -------------", 
				CST.componentBroker.getSpecs().size()));
		for (Specification  i : CST.componentBroker.getSpecs()) {

			System.out.println(String.format(" Specification name %s ",
					i.getName()));

		}
		System.out.println(String.format(
				"------------ /Specifications -------------"));
	}
	public static void listImplems() {
		System.out.println(String.format(
				"------------ Implementations (Total:%d) -------------", 
				CST.componentBroker.getImpls().size()));
		for (Implementation i : CST.componentBroker.getImpls()) {

			System.out.println(String.format(" Implementation name %s ",
					i.getName()));

		}
		System.out.println(String.format(
				"------------ /Specifications -------------"));
	}
	
	public static void listInstances() {
		System.out.println(String.format(
				"------------ Instances (Total:%d) -------------", 
				CST.componentBroker.getInsts().size()));
		for (Instance i : CST.componentBroker.getInsts()) {

			System.out.println(String.format(" Instance name %s ( oid: %s ) ",
					i.getName(), i.getServiceObject()));

		}
		System.out.println(String.format(
				"------------ /Instances -------------"));
	}
	
	public static void listComponents() {
		listSpecs();
		listImplems();
		listInstances();
	}

}
