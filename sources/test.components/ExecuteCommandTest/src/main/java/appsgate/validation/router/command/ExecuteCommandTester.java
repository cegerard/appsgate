package appsgate.validation.router.command;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.router.spec.RouterApAMSpec;

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
		
		router.executeCommand("194.199.23.136-1", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.136-2", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.136-3", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.136-4", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
		router.executeCommand("194.199.23.136-5", "Off", new ArrayList<Object>(), new ArrayList<Class>()).run();
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("ExecuteCommandTester has been stopped");
	}

}
