package appsgate.lig.chmi.exceptions;

/**
 * The Exception related to EHMI dependency resolution failure.
 * @author Cédric Gérard
 * @since 9 April, 2014
 *
 */
public class EHMIDependencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default exception constructor
	 */
	public EHMIDependencyException() {
		super();
	}
	
	/**
	 * Build an ehmiException
	 * @param reason the reason why this exception has been triggered
	 */
	public EHMIDependencyException(String reason) {
		super(reason);
	}

}
