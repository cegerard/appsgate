package appsgate.lig.eude.interpreter.spec;

import java.util.HashMap;
import org.json.JSONArray;

import org.json.JSONObject;

/**
 * Interpreter component for end-user development
 * 
 * @author Rémy Dautriche
 * @author Cédric Gérard
 * @version 1.0.0
 */
public interface EUDE_InterpreterSpec {

	/**
	 * Initialize a program from its JSON representation
	 * 
	 * @param programJSON Abstract tree of the program in JSON
	 * @return true when succeeded, false when failed a JSON error has been detected
	 */
	public boolean addProgram(JSONObject programJSON);
	
	/**
	 * Remove an existing program.
	 * 
	 * @param programId the identifier of the program to remove
	 * @return true if the program has been removed, false otherwise
	 */
	public boolean removeProgram(String programId);
	
	/**
	 * Update the program name or sources
	 * 
	 * @param jsonProgram the updated JSON program
	 * @return true if the program has been updated, false otherwise
	 */
	public boolean update(JSONObject jsonProgram);
	
	/**
	 * Launch the interpretation of a program
	 * 
	 * @param programId identifier of the program to launch
	 * @return true if the program has been successfully launched, false otherwise
	 */
	public boolean callProgram(String programId);
	/**
	 * Launch the interpretation of a program
	 * 
	 * @param programId identifier of the program to launch
         * @param args the args of the program
	 * @return true if the program has been successfully launched, false otherwise
	 */
	public boolean callProgram(String programId, JSONArray args);
	
	/**
	 * Stop the program interpretation
	 * 
	 * @param progamId identifier of the program
	 * @return true if the program has been stopped, false otherwise
	 */
	public boolean stopProgram(String progamId);
	
	/**
	 * Return a hash map containing all the programs known by the interpreter.
	 * The keys are the programs' name and the value are the programs under their
	 * JSON format
	 * 
	 * @return HashMap with all the existing programs
	 */
	public HashMap<String, JSONObject> getListPrograms();
	
	/**
	 * Check if a program is active or not
	 * 
	 * @param programId the identifier of the program
	 * @return true if the program is active, false otherwise
	 */
	public boolean isProgramActive(String programId);
        
        /**
         * warn the Interpreter that a status has a new status
         * 
         * @param deviceId the id of the device or service
         * @param statusOK true if the status is OK
         */
        public void newDeviceStatus(String deviceId, Boolean statusOK);

    public void checkReferences();

}
