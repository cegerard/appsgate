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

}
