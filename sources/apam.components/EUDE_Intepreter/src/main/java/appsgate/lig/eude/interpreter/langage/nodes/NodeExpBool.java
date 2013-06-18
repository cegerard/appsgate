package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

public class NodeExpBool extends Node {
	// <expBool> ::= <seqAndBool> { <opOrBool> <seqAndBool> }

	private String expBool;
	
	public NodeExpBool(EUDEInterpreterImpl interpreter, String expBool) {
		super(interpreter);
		this.expBool = expBool;
	}
	
	public Boolean getResult() {
		return true;
	}
	
	@Override
	public void undeploy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endEventFired(EndEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer call() {
		fireStartEvent(new StartEvent(this));
		fireEndEvent(new EndEvent(this));
		
		return null;
	}

}
