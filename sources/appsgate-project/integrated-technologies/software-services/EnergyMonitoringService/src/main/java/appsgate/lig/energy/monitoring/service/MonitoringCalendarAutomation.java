package appsgate.lig.energy.monitoring.service;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.energy.monitoring.group.CoreEnergyMonitoringGroup;
import appsgate.lig.scheduler.ScheduledInstruction;
import appsgate.lig.scheduler.SchedulerSpec;
import appsgate.lig.scheduler.SchedulingException;
import appsgate.lig.scheduler.ScheduledInstruction.Commands;
import appsgate.lig.scheduler.SchedulerEvent.BasicRecurrencePattern;
import appsgate.lig.scheduler.utils.DateFormatter;

/**
 * Helper class to simplify the energy monitoring group with calendar Event
 * @author thibaud
 *
 */
public class MonitoringCalendarAutomation {
	
	private final static Logger logger = LoggerFactory.getLogger(MonitoringCalendarAutomation.class);
	
	public enum AutomationType{
		RESET(0),
		START(1),
		STOP(2);
		int index;
		private AutomationType(int index) {
			this.index = index;
		}
		public int getIndex(){
			return index;
		}
	}
	
	private String serviceId = null;
	
	/**
	 * This constructor creates an empty automation object (no automation defined)
	 * @param serviceId
	 */
	public MonitoringCalendarAutomation(String serviceId) {
		this.serviceId = serviceId;
		json = computeJSON();
	}
	
	/**
	 * This constructor creates an automation object from its JSON representation (see toJSON() method)
	 * @param serviceId
	 */
	public MonitoringCalendarAutomation(JSONObject automation, String serviceId) {
		this.serviceId = serviceId;
		for(AutomationType type : AutomationType.values()) {
			if(automation.has(type.name())) {
				eventId[type.getIndex()] = automation.optString(CoreEnergyMonitoringGroup.PERIOD_ID, null);
				date[type.getIndex()] = automation.optLong(CoreEnergyMonitoringGroup.PERIOD_START, -1);
				recurrence[type.getIndex()] = BasicRecurrencePattern.fromName(
						automation.optString(CoreEnergyMonitoringGroup.PERIOD_RECURRENCE, BasicRecurrencePattern.NONE.name()));
			}
		}
		json = computeJSON();
	}

	
	private long[] date = {-1, -1, -1};
	private BasicRecurrencePattern[] recurrence = {BasicRecurrencePattern.NONE,
			BasicRecurrencePattern.NONE,
			BasicRecurrencePattern.NONE};
	private String[] eventId = {null, null, null};
	
	private JSONObject json;
	
	// by default events last only for 5 minutes (because the ending date of the event means nothing 
	private static final long EVENT_DURATION = 5*60*1000;

	public void configure(AutomationType automationType, long date, String recurrence, SchedulerSpec scheduler) {
		logger.trace("configure(AutomationType automationType : {}, long startDate : {}, String recurrence: {}, SchedulerSpec scheduler : {})",
				automationType.name(), date, recurrence, scheduler);
		
		if(scheduler == null) {
			logger.error("No scheduler found, won't configure "+automationType.name());
			return;
		}
		
		this.date[automationType.getIndex()] = date;
		try {
			this.recurrence[automationType.getIndex()] = BasicRecurrencePattern.fromName(recurrence);
		} catch (IllegalArgumentException e) {
			logger.warn("configure(...), unable to parse recurrence. Assuming NONE : ", e);
			this.recurrence[automationType.getIndex()] = BasicRecurrencePattern.NONE;
		}
		
		if(this.eventId[automationType.getIndex()] != null) {
			logger.trace("configure(...), An event was already registered with id : {}, removing it", this.eventId[automationType.getIndex()]);
			scheduler.removeEvent(this.eventId[automationType.getIndex()]);
		}
		if(this.date[automationType.getIndex()] <=0 && this.recurrence[automationType.getIndex()] == BasicRecurrencePattern.NONE) {
			logger.trace("configure(...), No valid date provided and no recurrence. Won't be automated (no event created)");
			this.eventId[automationType.getIndex()] = null;
		} else {
			Set<ScheduledInstruction> onBeginInstructions= new HashSet<ScheduledInstruction>();
			onBeginInstructions.add(formatInstruction("startMonitoring", null, ScheduledInstruction.ON_BEGIN));
			try {
				this.eventId[automationType.getIndex()] = scheduler.createEvent("AppsGate Energy Monitoring", onBeginInstructions, null,
						DateFormatter.format(date), DateFormatter.format(date+EVENT_DURATION), this.recurrence[automationType.getIndex()] );
				logger.trace("configureStart(...), Successfully registered start event : {}", this.eventId[automationType.getIndex()]);
			} catch (SchedulingException e) {
				logger.error("configureStart(...), unable to parse register start monitoring : ", e);
			}
		}
		json = computeJSON();
	}
	
	private ScheduledInstruction formatInstruction(String methodName, JSONArray args, String trigger) {
		JSONObject target = new JSONObject();
		target.put("objectId", serviceId);
		target.put("method", methodName);
		target.put("args", (args==null?new JSONArray():args));
		target.put("TARGET", "EHMI");
		
		return new ScheduledInstruction(Commands.GENERAL_COMMAND.getName(), target.toString(), trigger);
	}
	
	public JSONObject toJSON() {
		return json;
	}
	
	private JSONObject computeJSON() {
		JSONObject result = new JSONObject();
		for(AutomationType type : AutomationType.values()) {
			if(eventId[type.getIndex()] != null) {
				JSONObject obj = new JSONObject();
				obj.put(CoreEnergyMonitoringGroup.PERIOD_ID, eventId[type.getIndex()]);
				obj.put(CoreEnergyMonitoringGroup.PERIOD_START, date[type.getIndex()]);
				obj.put(CoreEnergyMonitoringGroup.PERIOD_RECURRENCE, recurrence[type.getIndex()].name());
				result.put(type.name(), obj);
			}
		}
		return result;
	}
	
	

}
