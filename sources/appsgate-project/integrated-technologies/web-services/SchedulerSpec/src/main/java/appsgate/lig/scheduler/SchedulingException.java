package appsgate.lig.scheduler;

/**
 * This exception is thrown when the Scheduling service cannot work properly
 * (mostly because of missing dependencies with used services)
 * @author thibaud
 *
 */
public class SchedulingException extends Exception {

	/**
	 * Generated VersionID
	 */
	private static final long serialVersionUID = 7823360081298326863L;

	public SchedulingException(String message) {
		super(message);
	}
}
