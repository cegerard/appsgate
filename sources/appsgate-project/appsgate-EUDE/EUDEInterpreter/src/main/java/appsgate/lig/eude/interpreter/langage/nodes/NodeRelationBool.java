package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * Node for the boolean relations
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 * 
 * @since June 19, 2013
 * @version 1.0.0
 */
public class NodeRelationBool extends Node {
	// <relationBool> ::= <term> <opComparison> <term>
	// <term> ::= NodeAction | number | string | boolean
	// <term> can be a NodeAction in the case where the value is provided by a device. In this case, a method call has to be performed

	private String operator;
	private Object leftValue;
	private NodeAction leftNodeAction;
	private String leftReturnType;
	private Object rightValue;
	private NodeAction rightNodeAction;
	private String rightReturnType;
	private Boolean result;

	/**
	 * Getter for the result of the boolean relation
	 *
	 * @return true or false according to the result of the boolean relation
	 */
	public boolean getResult() throws Exception {
		if (result != null) {
			return result;
		}
		throw new Exception("result has not been computed yet");
	}

	/**
	 * Default constructor
	 *
	 * @param interpreter Pointer on the interpreter
	 * @param relationBoolJSON JSON representation of the node
	 * @throws JSONException
	 */
	public NodeRelationBool(EUDEInterpreterImpl interpreter, JSONObject relationBoolJSON) throws JSONException {
		super(interpreter);

		// operator
		operator = relationBoolJSON.getString("operator");

		JSONObject operand;

		// left operand
		operand = relationBoolJSON.getJSONObject("leftOperand");
		if (operand.has("deviceId")) {
			leftNodeAction = new NodeAction(interpreter, operand);
			leftReturnType = operand.getString("returnType");
			leftValue = null;
		} else {
			String type = operand.getString("type");
			String valueJSON = operand.getString("value");
			
			if (type.equals("number")) {
				leftValue = Long.parseLong(valueJSON);
			} else if (type.equals("boolean")) {
				leftValue = Boolean.parseBoolean(valueJSON);
			} else if (type.equals("string")) {
				leftValue = valueJSON;
			}
		}

		// right operand
		operand = relationBoolJSON.getJSONObject("rightOperand");
		if (operand.has("deviceId")) {
			rightNodeAction = new NodeAction(interpreter, operand);
			rightReturnType = operand.getString("returnType");
			rightValue = null;
		} else {
			String type = operand.getString("type");
			String valueJSON = operand.getString("value");
			
			if (type.equals("number")) {
				rightValue = Long.parseLong(valueJSON);
			} else if (type.equals("boolean")) {
				rightValue = Boolean.parseBoolean(valueJSON);
			} else if (type.equals("string")) {
				rightValue = valueJSON;
			}
		}

		// two thread - one for each operand
		//pool = Executors.newFixedThreadPool(2);

		// nothing has been computed yet
		result = null;
	}

	/**
	 * Parse a JSON operand
	 *
	 * @param operand JSON object representing an operand of a boolean relation
	 * @param value
	 * @throws JSONException
	 */
	private void parseOperand(JSONObject operand, NodeAction nodeAction, Object value) throws JSONException {
		if (operand.has("deviceId")) {
			nodeAction = new NodeAction(interpreter, operand);
			value = null;
		} else {
			String type = operand.getString("type");
			String valueJSON = operand.getString("value");

			if (type.equals("number")) {
				value = Long.parseLong(valueJSON);
			} else if (type.equals("boolean")) {
				value = Boolean.parseBoolean(valueJSON);
			} else if (type.equals("string")) {
				value = valueJSON;
			}
			nodeAction = null;
		}
	}

	@Override
	public void undeploy() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void stop() {
		if(started) {
			stopping = true;
			if (leftNodeAction != null) {
				leftNodeAction.removeEndEventListener(this);
				leftNodeAction.stop();
			} else {
				rightNodeAction.removeEndEventListener(this);
				rightNodeAction.stop();
			}
			started = false;
			stopping = false;
		}
	}

	@Override
	public void resume() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void getState() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * Launch the interpretation of the node.
	 * 
	 * @return 
	 */
	@Override
	public Integer call() {
		// fire the start event
		fireStartEvent(new StartEvent((this)));
		started = true;
		
		// if the both operands are direct value, compute the final result and fire the end event
		if (leftNodeAction == null && rightNodeAction == null) {
			computeResult();
			started = false;
			fireEndEvent(new EndEvent(this));
		}

		// interpret the left operand first if possible
		if (leftNodeAction != null) {
			leftNodeAction.addEndEventListener(this);
			//pool.submit(leftNodeAction);
			leftNodeAction.call();
		} else {
			rightNodeAction.addEndEventListener(this);
			//pool.submit(rightNodeAction);
			rightNodeAction.call();
		}
		
		return null;
	}
	
	/**
	 * Compute the final result according to the operator
	 */
	private void computeResult() {
		if (operator.equals("==")) {
			result = (leftValue.equals(rightValue));
		} else if (operator.equals("!=")) {
			result = !(leftValue.equals(rightValue));
		}
	}

	@Override
	public void startEventFired(StartEvent e) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * Called when an operand is done.
	 * @param e 
	 */
	@Override
	public void endEventFired(EndEvent e) {
		NodeAction n = (NodeAction) e.getSource();
		n.removeEndEventListener(this);
		
		if (n == leftNodeAction) {
			// cast the value to the correct type
			if (leftReturnType.equals("number")) {
				leftValue = (Long)n.getResult();
			} else if (leftReturnType.equals("boolean")) {
				leftValue = (Boolean)n.getResult();
			} else if (leftReturnType.equals("string")) {
				leftValue = (String)n.getResult();
			}
			
			// if the right operand is not a direct value, launch its interpretation...
			if (rightNodeAction != null) {
				rightNodeAction.addEndEventListener(this);
				rightNodeAction.call();
				//pool.submit(rightNodeAction);
			// ... compute the final result and fire the end event otherwise
			} else {
				computeResult();
				started = false;
				fireEndEvent(new EndEvent(this));
			}
		} else {
			// cast the value to the correct type
			if (rightReturnType.equals("number")) {
				rightValue = (Long)n.getResult();
			} else if (rightReturnType.equals("boolean")) {
				rightValue = (Boolean)n.getResult();
			} else if (rightReturnType.equals("string")) {
				rightValue = (String)n.getResult();
			}
			
			// compute the final result and fire the end result
			computeResult();
			started = false;
			fireEndEvent(new EndEvent(this));
		}
	}
}
