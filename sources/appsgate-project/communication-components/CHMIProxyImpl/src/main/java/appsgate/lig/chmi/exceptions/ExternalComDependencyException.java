package appsgate.lig.chmi.exceptions;

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
