package appsgate.lig.ehmi.exceptions;

/**
 * The Exception related to core dependency resolution failure.
 * @author Cédric Gérard
 * @since 22 April, 2014
 *
 */
public class CoreDependencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default exception constructor
	 */
	public CoreDependencyException() {
		super();
	}
	
	/**
	 * Build an ehmiException
	 * @param reason the reason why this exception has been triggered
	 */
	public CoreDependencyException(String reason) {
		super(reason);
	}

}
