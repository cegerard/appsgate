package appsgate.validation.configuration.eude;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;

public class DeployPrograms {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	//private static Logger logger = LoggerFactory.getLogger(DeployPrograms.class);

	/**
	 * EUDE reference
	 */
	private EUDE_InterpreterSpec eude;
	
	/**
	 * Built the auto configuration instance
	 */
	public DeployPrograms() {
		super();
	}
	
	/**
	 * Callback called when the ApAm instance has been created
	 */
	public void newInst() {}
	
	public void deleteInst() {removeAllProgram();}
	
	private void deployProgram(String newDay) {
		// TODO Auto-generated method stub
		
	}
	
	private void removeAllProgram() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Called by APAM when the day property is changed
	 * @param newDay the new day
	 */
	public void dayChanged (String newDay) {
		deployProgram(newDay);
	}

	
}
