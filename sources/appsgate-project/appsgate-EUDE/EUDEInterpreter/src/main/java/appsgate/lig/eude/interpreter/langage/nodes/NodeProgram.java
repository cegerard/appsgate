package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * Node program for the interpreter. Contains the metadatas of the program, the parameters, the variables and the rules
 * 
 * @author Rémy Dautriche
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeProgram extends Node {

	/**
	 * Program's name given by the user
	 */
	private String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	/**
	 * User's name who wrote the program
	 */
	private String author;
	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }

	/**
	 * Target user
	 */
	private String target;
	public String getTarget() { return target; }
	public void setTarget(String target) { this.target = target; }
	
	/**
	 * Deamon attribute
	 */
	private String deamon;
	public String getDeamon() {return deamon;}
	public void setDeamon(String deamon) { this.deamon = deamon;}
	public boolean isDeamon() { return deamon.contentEquals("true");}


	/**
	 * Sequence of rules to interpret
	 */
	private NodeSeqRules seqRules;
	
	/**
	 * JSON representation of the program
	 */
	private JSONObject programJSON;
	
	public JSONObject getProgramJSON() { return programJSON; }
	
	/**
	 * the current running state of this program
	 * 	- STARTED
	 *  - STOPPED
	 *  - PAUSED
	 */
	private String runningState = "STOPPED";

	/**
	 * Default constructor
	 * 
	 * @constructor
	 */
	public NodeProgram(EUDEInterpreterImpl interpreter) {
		super(interpreter);
		// initialize the thread pool
		pool = Executors.newSingleThreadExecutor();
	}

	/**
	 * Initialize the program from a JSON object
	 * 
	 * @param interpreter 
	 * @param programJSON Abstract tree of the program in JSON
	 * 
	 * @throws JSONException Thrown when the JSON file contains an error
	 */
	public NodeProgram(EUDEInterpreterImpl interpreter, JSONObject programJSON) throws JSONException {
		this(interpreter);
		
		this.programJSON = programJSON;
		
		// initialize the program with the JSON
		name = programJSON.getString("programName");
		author = programJSON.getString("author");
		target = programJSON.getString("target");
		deamon = programJSON.getString("deamon");
		seqRules = new NodeSeqRules(interpreter, programJSON.getJSONArray("seqRules"));
	}
	
	/**
	 * Return a JSON object with all the necessary information for the user
	 * 
	 * @return
	 */
	public JSONObject getInformation() {
		return programJSON;
	}

	/**
	 * Launch the interpretation of the rules
	 */
	@Override
	public Integer call() {
		fireStartEvent(new StartEvent(this));
		
		seqRules.addStartEventListener(this);
		seqRules.addEndEventListener(this);
		
		pool.submit(seqRules);
		
		runningState = "STARTED";
		
		return null;
	}

	@Override
	public void undeploy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		runningState = "STOPPED";
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		runningState = "STARTED";
	}

	@Override
	public void getState() {
		// TODO Auto-generated method stub
		
	}
	
	public String getRunningState() {
		return runningState;
	}

	@Override
	public void startEventFired(StartEvent e) {
		seqRules.removeStartEventListener(this);
		
	}

	@Override
	public void endEventFired(EndEvent e) {
		seqRules.removeEndEventListener(this);
		fireEndEvent(new EndEvent(this));
		runningState = "STOPPED";
	}

}
