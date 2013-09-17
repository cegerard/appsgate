package appsgate.validation.router.command;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.router.spec.RouterApAMSpec;

/**
 * This class is use to validate the router command execution
 * @author Cédric Gérard
 *
 */
public class ExecuteCommandTester {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ExecuteCommandTester.class);
	
	private RouterApAMSpec router;


	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	@SuppressWarnings("rawtypes")
	public void newInst() {
		logger.debug("ExecuteCommandTester has been initialized");  
		
		router.executeCommand("194.199.23.135-1", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-2", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-3", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-4", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-5", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-6", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		
		router.executeCommand("194.199.23.135-1", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-2", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-3", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-4", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-5", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-6", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		
		router.executeCommand("194.199.23.135-1", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-2", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-3", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-4", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-5", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-6", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		
		router.executeCommand("194.199.23.135-1", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-2", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-3", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-4", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-5", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-6", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		
		router.executeCommand("194.199.23.135-1", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-2", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-3", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-4", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-5", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-6", "On", new ArrayList<Object>(), new ArrayList<Class>()).run();
		
		router.executeCommand("194.199.23.135-1", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-2", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-3", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-4", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-5", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.135-6", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("ExecuteCommandTester has been stopped");
	}

}
