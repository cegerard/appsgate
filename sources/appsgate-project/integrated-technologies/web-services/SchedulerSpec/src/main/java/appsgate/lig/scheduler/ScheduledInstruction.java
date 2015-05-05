package appsgate.lig.scheduler;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledInstruction {
	
	/**
	 * Reserved keyword for AppsGate in the description,
	 * to map appsgate instruction (such as appsgate schedules)
	 * To the beginning of the event
	 * (one instruction per line, must start with the keyword and the '=' separator
	 * Ex: begin=start.program-5524
	 */
	public static final String ON_BEGIN="begin";
	
	/**
	 * Reserved keyword for AppsGate in the description,
	 * to map appsgate instruction (such as appsgate schedules)
	 * To the end of the event
	 * (one instruction per line, must start with the keyword and the '=' separator
	 * Ex: end=stop.program-5524
	 */	
	public static final String ON_END="end";
	
	private static Logger logger = LoggerFactory.getLogger(ScheduledInstruction.class);
	
	public enum Commands {
		CALL_PROGRAM("callProgram"),  
		STOP_PROGRAM("stopProgram"),
		GENERAL_COMMAND("command");
		
		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return getName();
		}

		String name;
		Commands(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}	
	}
	
	
	public final static String SEPARATOR=".";
	
	public String getCommand() {
		return command;
	}

	public String getTarget() {
		return target;
	}

	String command;
	String target;
	String trigger;
	
	
	public ScheduledInstruction(String command, String target, String trigger) {
		this.command=command;
		this.target=target;
		this.trigger = trigger;
	}
	
	public static ScheduledInstruction parseInstruction(String instruction, String trigger) {
		if(instruction == null) {
			logger.warn("instruction is null");
			return null;
		}
				
		String command=parseCommand(instruction);
		if(command == null ) {
			return null;
		}
		String target=parseTarget(instruction,command);
		
		return new ScheduledInstruction(command, target, trigger);
	}
	
	private static String parseCommand(String instruction) {
		for(Commands command : Commands.values()) {
			if(instruction.startsWith(command.getName()))  {
				return command.getName();	
			}
		}

		logger.info("Unrecognized instruction : "+instruction);
		return null;
	}
	
	
	private static String parseTarget(String instruction, String commandName) {
		return instruction.substring(commandName.length()+SEPARATOR.length());
	}
	
	@Override
	public String toString() {
		return trigger+SEPARATOR+command+SEPARATOR+target;
	}
	
	public boolean matchesPattern(String pattern) {
		logger.trace("matchesPattern(String pattern : {})", pattern);			

		if(pattern==null ||pattern.length()==0) {
			logger.warn("matchesPattern(...), no pattern specified");
			return false ; //false by default
		}
		
		Pattern regExp = null;
		try {
			regExp = Pattern.compile(pattern);
			if (regExp == null) throw new NullPointerException("compiled regexp is null");
		} catch (Exception e) {
			logger.warn("matchesPattern(...), invalid regexp pattern : ",e);
			return false;
		}
		
		if(regExp.matcher(this.toString()).matches()) {
			logger.trace("matchesPattern(...), return true");			
			return true;
		} else {
			logger.trace("matchesPattern(...), return true");			
			return false;
		}
	}

}
