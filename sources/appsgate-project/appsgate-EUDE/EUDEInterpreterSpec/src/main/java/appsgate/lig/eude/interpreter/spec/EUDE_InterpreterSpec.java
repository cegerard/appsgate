package appsgate.lig.eude.interpreter.spec;

import java.util.HashMap;

import org.json.JSONObject;

/**
 * Interpreter component for end-user development
 * 
 * @author Rémy Dautriche
 * @version 1.0.0
 */
public interface EUDE_InterpreterSpec {

	/**
	 * Add a new program to the interpreter. Instantiate the program from its JSON representation
	 * 
	 * @param programJSON
	 * @return
	 */
	public boolean addProgram(JSONObject programJSON);
	
	/**
	 * Remove an existing program.
	 * @param programName the name of the program to remove
	 * @return true if the program has been removed, false otherwise
	 */
	public boolean removeProgram(String programName);
	
	/**
	 * Launch the interpretation of a program
	 * 
	 * @param programName Name of the program to launch
	 * @return true if the program has been successfully launched, false otherwise
	 */
	public boolean callProgram(String programName);
	
	/**
	 * Return a hash map containing all the programs known by the interpreter.
	 * The keys are the programs' name and the value are the programs under their
	 * JSON format
	 * 
	 * @return HashMap with all the existing programs
	 */
	public HashMap<String, JSONObject> getListPrograms();
	
}
