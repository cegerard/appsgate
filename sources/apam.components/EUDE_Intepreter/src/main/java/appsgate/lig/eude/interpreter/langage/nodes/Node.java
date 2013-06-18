package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;

/**
 * Abstract class for all the nodes of the interpreter
 * 
 * @author Rémy Dautriche
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public abstract class Node implements Callable<Integer>, StartEventGenerator, StartEventListener, EndEventGenerator, EndEventListener {

	/**
	 * List of the listeners that listen to the StartEvent of the node
	 */
	private ArrayList<StartEventListener> startEventListeners = new ArrayList<StartEventListener>();

	/**
	 * List of the listeners that listen to the EndEvent of the node
	 */
	private ArrayList<EndEventListener> endEventListeners = new ArrayList<EndEventListener>();
	
	/**
	 * Pool to execute the children. Possibly a single thread
	 */
	protected ExecutorService pool;
	
	protected EUDEInterpreterImpl interpreter;

	/**
	 * Symbol table of the node containing the local symbols
	 */
	protected SymbolTable symbolTable;

	/**
	 * Node parent in the abstract tree of a program
	 */
	protected Node parent;
	
	/**
	 * Default constructor
	 */
	public Node(EUDEInterpreterImpl interpreter) {
		this.interpreter = interpreter;
	}

	//Abstract Methods

	/**
	 * Stop the interpretation of the node. The current state is saved
	 */
	public abstract void undeploy();
	public abstract void stop();

	/**
	 * Relaunch the node from its last previous state
	 */
	public abstract void resume();

	/**
	 * Return the current state of the node i.e. which branch is being executed
	 */
	public abstract void getState();

	/**
	 * Fire a start event to all the listeners
	 * 
	 * @param e The start event to fire for all the listeners
	 */
	protected void fireStartEvent(StartEvent e) {
		for (int i = 0; i < startEventListeners.size(); i++) {
			startEventListeners.get(i).startEventFired(e);
		}
	}

	/**
	 * Fire an end event to all the listeners
	 * 
	 * @param e The end event to fire for all the listeners
	 */
	protected void fireEndEvent(EndEvent e) {
		for (int i = 0; i < endEventListeners.size(); i++) {
			endEventListeners.get(i).endEventFired(e);
		}
	}

	/**
	 * Add a new listener to the start event of the node
	 * 
	 * @param listener Listener to add
	 */
	public void addStartEventListener(StartEventListener listener) {
		startEventListeners.add(listener);
	}

	/**
	 * Remove a listener to the start event of the node
	 * 
	 * @param listener Listener to remove
	 */
	public void removeStartEventListener(StartEventListener listener) {
		startEventListeners.remove(listener);
	}

	/**
	 * Add a new listener to the end event of the node
	 * 
	 * @param listener Listener to add
	 */
	public void addEndEventListener(EndEventListener listener) {
		endEventListeners.add(listener);
	}

	/**
	 * Remove a listener to the end event of the node
	 * 
	 * @param listener Listener to remove
	 */
	public void removeEndEventListener(EndEventListener listener) {
		endEventListeners.remove(listener);
	}

	/**
	 * Getter for the local symbol table
	 * 
	 * @return the symbol table of the node that contains the symbols defined by the node
	 */
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	/**
	 * Getter for the symbol table of the parent node
	 * 
	 * @return the symbol table of the parent node if the node has a parent, null otherwise
	 */
	public SymbolTable getParentSymbolTable() {
		if (parent != null) {
			return parent.getSymbolTable();
		} else {
			return null;
		}
	}

}