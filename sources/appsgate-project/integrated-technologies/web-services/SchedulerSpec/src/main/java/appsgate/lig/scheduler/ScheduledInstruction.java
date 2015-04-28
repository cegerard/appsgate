package appsgate.lig.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledInstruction {
	
	private static Logger logger = LoggerFactory.getLogger(ScheduledInstruction.class);
	
	public enum Commands {
		CALL_PROGRAM("callProgram"),  
		STOP_PROGRAM("stopProgram"),
		GENERAL_COMMAND("command");
		
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
	
	
	public ScheduledInstruction(String command, String target) {
		this.command=command;
		this.target=target;
	}
	
	public static ScheduledInstruction parseInstruction(String instruction) {
		if(instruction == null) {
			logger.warn("instruction is null");
			return null;
		}
				
		String command=parseCommand(instruction);
		if(command == null ) {
			return null;
		}
		String target=parseTarget(instruction,command);
		
		return new ScheduledInstruction(command, target);
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
		return command+SEPARATOR+target;
	}

}
