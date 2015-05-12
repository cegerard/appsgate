package appsgate.lig.scheduler;

import java.util.Set;

import org.json.JSONObject;

import appsgate.lig.scheduler.ScheduledInstruction;

/**
 * Functionnal representation of a scheduler Event
 * @author thibaud
 */
public interface SchedulerEvent {
	
	public String getId() ;

	public String getStartTime();

	public String getEndTime();

	public String getUpdated();

	public String getName();

	public Set<ScheduledInstruction> getOnBeginInstructions();

	public Set<ScheduledInstruction> getOnEndInstructions();
	
	/**
	 * @return a JSON representation of this Event, it contains additionnal information from th underlying implementaiton
	 * (for instance google calendar event) 
	 */
	public JSONObject toJSON() ;
	
	public boolean isSchedulingProgram(String programId);
	
	/**
	 * Return all instructions that matches the pattern 
	 * @param pattern
	 * @return
	 */
	public Set<ScheduledInstruction> instructionsMatchingPattern(String pattern);
	
	/**
	 * @return true if this event is reccurent, if false, this is a single time event
	 */
	public BasicRecurrencePattern getRecurrencePattern();

	/**
	 * Limited set of Patterns for reccurence
	 * (complex pattern with multiples reccurences rules, specific dates and exceptions are considerered as OTHER) 
	 * @author thibaud
	 */
	public enum BasicRecurrencePattern {
		NONE("NONE"),
		EACH_DAY("FREQ=DAILY"),
		EACH_WEEK("FREQ=WEEKLY"),
		EACH_MONDAY("FREQ=WEEKLY;BYDAY=MO"),
		EACH_TUESDAY("FREQ=WEEKLY;BYDAY=TU"),
		EACH_WEDNESDAY("FREQ=WEEKLY;BYDAY=WE"),
		EACH_THURSDAY("WEEKLY;BYDAY=TH"),
		EACH_FRIDAY("WEEKLY;BYDAY=FR"),
		EACH_SATURDAY("WEEKLY;BYDAY=SA"),
		EACH_SUNDAY("WEEKLY;BYDAY=SU"),
		EACH_MONTH("FREQ=MONTHLY"),
		EACH_YEAR("FREQ=YEARLY"),
		OTHER("OTHER");
		
		String rruleFreq;
		BasicRecurrencePattern(String rruleFreq) {
			this.rruleFreq = rruleFreq;
		}
		public String getRRuleFreq() {
			return rruleFreq;
		}
		
		public static BasicRecurrencePattern fromRRuleFreq(String rruleFreq) throws IllegalArgumentException {
			if(rruleFreq != null && rruleFreq.length()>0) {
				for(BasicRecurrencePattern pattern : BasicRecurrencePattern.values()) {
					if(rruleFreq.equals(pattern.getRRuleFreq())) {
						return pattern;
					}
				}
				throw new IllegalArgumentException("Unrecognized rrule frequency : "+rruleFreq);
			} else {
				throw new IllegalArgumentException("Provided rrule frequency is empty");
			}
		}
		
		public static BasicRecurrencePattern fromName(String name) throws IllegalArgumentException {
			if(name != null && name.length()>0) {
				for(BasicRecurrencePattern pattern : BasicRecurrencePattern.values()) {
					if(name.equals(pattern.name())) {
						return pattern;
					}
				}
				throw new IllegalArgumentException("Unrecognized name : "+name);
			} else {
				throw new IllegalArgumentException("Provided name is empty");
			}
		}
	}
}
