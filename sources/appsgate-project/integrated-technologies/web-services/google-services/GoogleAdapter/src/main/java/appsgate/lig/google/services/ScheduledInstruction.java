package appsgate.lig.google.services;

import org.omg.CORBA.COMM_FAILURE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledInstruction {
	
	private static Logger logger = LoggerFactory.getLogger(ScheduledInstruction.class);

	
	public final static String CALL_PROGRAM = "callProgram";  
	public final static String STOP_PROGRAM = "stopProgram";
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
				
		String cmd, tgt;
		if(instruction.startsWith(CALL_PROGRAM))  {
			cmd=CALL_PROGRAM;
			tgt=instruction.substring(CALL_PROGRAM.length()+SEPARATOR.length());
			return new ScheduledInstruction(cmd, tgt);
		} else if (instruction.startsWith(STOP_PROGRAM)) {
			cmd=STOP_PROGRAM;
			tgt=instruction.substring(STOP_PROGRAM.length()+SEPARATOR.length());
			return new ScheduledInstruction(cmd, tgt);
		} else {
			logger.info("Unrecognized instruction : "+instruction);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Command:"+command+", target: "+target;
	}

}
