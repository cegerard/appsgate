package appsgate.validation.context.follower;


import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.follower.listeners.CoreListener;
import appsgate.lig.context.follower.spec.ContextFollowerSpec;

/**
 * This class is use to validate the context follower
 * @author Cédric Gérard
 *
 */
public class ContextFollowerTester {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ContextFollowerTester.class);
	
	/**
	 * The context follower member instance
	 */
	private ContextFollowerSpec contextFollower;
	
	/**
	 * our listener for test
	 */
	TestListener test;
	
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("ContextFollowerTester has been initialized");
		
		
		logger.debug("Try to register a time listener to 10 minutes");
		test = new TestListener();
		contextFollower.addListener(test);
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("ContextFollowerTester has been stopped");
		if(test != null) {
			contextFollower.deleteListener(test);
		}
	}

	private class TestListener implements CoreListener {

		private String objectId;
		private String eventVarName;
		private String eventVarValue;
		
		
		public TestListener() {
			objectId = "21106637055";
			eventVarName = "flowRate";
			eventVarValue= "";
		}
		
		
		@Override
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		@Override
		public void setEvent(String eventVarName) {
			this.eventVarName = eventVarName;
		}

		@Override
		public void setValue(String eventVarValue) {
			this.eventVarValue = eventVarValue;
		}

		@Override
		public String getObjectId() {
			return objectId;
		}

		@Override
		public String getEvent() {
			return eventVarName;
		}

		@Override
		public String getValue() {
			return eventVarValue;
		}

		@Override
		public void notifyEvent() {
			//Calendar cal = Calendar.getInstance();
			//Calendar oldCal = (Calendar) cal.clone();
			//cal.setTimeInMillis(Long.valueOf(eventVarValue));
			logger.debug("####### The context instance notify the CONTEXT FOLLOWER TESTER");
		}

		@Override
		public void notifyEvent(CoreListener listener) {
			logger.debug("####### The context follower tester has received a notification");
		}
		
	}
	
	
}
