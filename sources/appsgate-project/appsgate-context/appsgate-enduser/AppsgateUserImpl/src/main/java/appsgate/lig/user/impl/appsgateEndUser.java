package appsgate.lig.user.impl;

import java.util.ArrayList;

import org.mindrot.jbcrypt.BCrypt;

/**
 * This class is Appsgate end user implementation.
 * It describe how a end user is represent in the Appsgate context.
 * 
 * @author Cédric Gérard
 * @since July 19, 2013
 * @version 1.0.0
 */
public class appsgateEndUser {

	/**
	 * The end user identifier use in in
	 * all other context components
	 */
	String id;
	
	/**
	 * The password finger print keep for authentication
	 */
	String hashPSWD;
	
	/**
	 * The user last name
	 */
	String lastName;
	
	/**
	 * The user first name
	 */
	String firstName;
	
	/**
	 * The user role in his family.
	 * (father, mother, child, ant, friend, etc.)
	 */
	String role;
	
	/**
	 * List of synchronized services
	 */
	ArrayList<?> serviceAccountList;
	
	/**
	 * List of owned devices
	 */
	ArrayList<?> deviceOwnedList;
	
	/**
	 * Build a new end user instance
	 * 
	 * @param id the end user unique identifier
	 * @param pswd the end user password 
	 * @param lastName the end user last name
	 * @param firstName the end user first name
	 * @param role the end user role in the smart home
	 */
	public appsgateEndUser(String id, String pswd, String lastName, String firstName, String role) {
		super();
		
		this.id	   	   = id;
		this.hashPSWD  = BCrypt.hashpw(pswd, BCrypt.gensalt(11));;
		this.lastName  = lastName;
		this.firstName = firstName;
		this.role 	   = role;
	}
	
	/**
	 * Authenticate the end user password 
	 * @param candidatepswd the password to test
	 * @return true if the password correspond to this end user password, false otherwise
	 */
	public boolean authenticate(String candidatepswd) {
		return BCrypt.checkpw(candidatepswd, hashPSWD);
	}
	
	
	
}
