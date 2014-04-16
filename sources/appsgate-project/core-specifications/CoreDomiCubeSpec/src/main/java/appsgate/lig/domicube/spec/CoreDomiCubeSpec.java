package appsgate.lig.domicube.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle DomiCube events.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since April 16, 2014
 *
 */
public interface CoreDomiCubeSpec {
	
	/**
	 * Check if the DomiCube instance has received a message
	 * @return true if the DomiCube instance has been synchronized once with real DomiCube.
	 */
	public boolean hasReceived();
	
	/**
	 * Get the current face number of the DomiCube
	 * @return the number of the face as an integer
	 */
	public int getCurrentFaceNumber();
	
}
