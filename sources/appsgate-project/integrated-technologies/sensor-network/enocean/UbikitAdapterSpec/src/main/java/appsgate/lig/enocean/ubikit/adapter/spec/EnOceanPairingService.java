package appsgate.lig.enocean.ubikit.adapter.spec;

/**
 * 
 * This class is a service use to manipulate pairing mode to Ubikit software layer. 
 * 
 * @author Cédric Gérard
 * @since February 7, 2013
 * @version 1.0.0
 *
 */
public interface EnOceanPairingService {

	/**
	 * Set the pairing mode to Ubikit layer
	 * @param pair true to set the pairing mode, false to shutdown the pairing mode.
	 */
	public void setPairingMode (boolean pair); 
}
