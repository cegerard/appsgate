package appsgate.validation.interpreter.command;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;
import java.util.logging.Level;

public class InterpreterProgramTester {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(InterpreterProgramTester.class);
	
	private EUDE_InterpreterSpec interpreter;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		try {
			//interpreter.addProgram(loadFileJSON("test/simpleRuleJSON.js"));
			//interpreter.addProgram(loadFileJSON("test/testCallProgramJSON.js"));
			interpreter.addProgram(loadFileJSON("test/demo220713JSON_1.js"));
			interpreter.addProgram(loadFileJSON("test/demo220713JSON_2.js"));
			interpreter.addProgram(loadFileJSON("test/demo220713JSON_3.js"));
			/* interpreter.addProgram(loadFileJSON("test/philipsActionRuleJSON.js"));
			interpreter.addProgram(loadFileJSON("test/testIfJSON.js")); */
			// interpreter.addProgram(loadFileJSON("test/testWhenJSON.js"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		interpreter.callProgram("Demo220713_1");
		interpreter.callProgram("Demo220713_2");
		interpreter.callProgram("Demo220713_3");
		/* try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			java.util.logging.Logger.getLogger(InterpreterProgramTester.class.getName()).log(Level.SEVERE, null, ex);
		}
		interpreter.callProgram("SimpleRule"); */
	}
	
	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("InterpreterProgramTester has been stopped");
	}
	
	/**
	 * Load a file and return its content
	 * 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JSONException 
	 */
	private JSONObject loadFileJSON(String filename) throws FileNotFoundException, IOException, JSONException {
		FileInputStream fis = new FileInputStream(filename);
		DataInputStream dis = new DataInputStream(fis);
		
		byte[] buf = new byte[dis.available()];
		dis.readFully(buf);
		
		String fileContent = "";
		for (byte b:buf) {
			fileContent += (char)b;
		}
		
		dis.close();
		fis.close();
		
		return new JSONObject(fileContent);
	}
	
}
