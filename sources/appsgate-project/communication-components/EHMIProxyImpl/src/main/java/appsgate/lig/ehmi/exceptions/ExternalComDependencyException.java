package appsgate.lig.ehmi.exceptions;

/**
 * The Exception related to communication dependency resolution failure.
 * @author Cédric Gérard
 * @since 22 April, 2014
 *
 */
public class ExternalComDependencyException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Default exception constructor
	 */
	public ExternalComDependencyException() {
		super();
	}
	
	/**
	 * Build an ExternalComDependencyException
	 * @param reason the reason why this exception has been triggered
	 */
	public ExternalComDependencyException(String reason) {
		super(reason);
		
	}
	
}
