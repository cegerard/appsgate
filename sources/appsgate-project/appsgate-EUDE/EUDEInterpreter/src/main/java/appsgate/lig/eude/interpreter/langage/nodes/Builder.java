/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class Builder {

    private static enum NODE_TYPE {

        NODE_ACTION, NODE_BINARY_EXPRESSION, NODE_EVENT, NODE_EVENTS, NODE_FUNCTION,
        NODE_FUNCTION_DEFINITION, NODE_IF, NODE_PROGRAM, NODE_RETURN,
        NODE_SELECT, NODE_SEQ_RULES, NODE_VALUE, NODE_VARIABLE_ASSIGNATION, NODE_WHEN;
    }

    private static NODE_TYPE getType(String type) throws SpokException {
        if (type.equalsIgnoreCase("action")) {
            return NODE_TYPE.NODE_ACTION;
        }
        if (type.equalsIgnoreCase("binaryExpression")) {
            return NODE_TYPE.NODE_BINARY_EXPRESSION;
        }
        if (type.equalsIgnoreCase("event")) {
            return NODE_TYPE.NODE_EVENT;
        }
        if (type.equalsIgnoreCase("events")) {
            return NODE_TYPE.NODE_EVENTS;
        }
        if (type.equalsIgnoreCase("function")) {
            return NODE_TYPE.NODE_FUNCTION;
        }
        if (type.equalsIgnoreCase("functionDefinition")) {
            return NODE_TYPE.NODE_FUNCTION_DEFINITION;
        }
        if (type.equalsIgnoreCase("if")) {
            return NODE_TYPE.NODE_IF;
        }
        if (type.equalsIgnoreCase("program")) {
            return NODE_TYPE.NODE_PROGRAM;
        }
        if (type.equalsIgnoreCase("return")) {
            return NODE_TYPE.NODE_RETURN;
        }
        if (type.equalsIgnoreCase("select")) {
            return NODE_TYPE.NODE_SELECT;
        }
        if (type.equalsIgnoreCase("number")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("string")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("variable")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("boolean")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("assignation")) {
            return NODE_TYPE.NODE_VARIABLE_ASSIGNATION;
        }
        if (type.equalsIgnoreCase("when")) {
            return NODE_TYPE.NODE_WHEN;
        }
        if (type.equalsIgnoreCase("instructions")) {
            return NODE_TYPE.NODE_SEQ_RULES;
        }
        throw new SpokTypeException(type);
    }

    /**
     *
     * @param o
     * @param parent
     * @return
     * @throws SpokNodeException
     */
    public static Node BuildNodeFromJSON(JSONObject o, Node parent) throws SpokException {
        if (o == null || !o.has("type")) {
            throw new SpokNodeException("NodeBuilder", "type", null);
        }
        switch (getType(o.optString("type"))) {
            case NODE_ACTION:
                return new NodeAction(o, parent);
            case NODE_BINARY_EXPRESSION:
                return new NodeBinaryExpression(o, parent);
            case NODE_EVENT:
                return new NodeEvent(o, parent);
            case NODE_EVENTS:
                return new NodeEvents(o, parent);
            case NODE_FUNCTION:
                return new NodeFunction(o, parent);
            case NODE_FUNCTION_DEFINITION:
                return new NodeFunctionDefinition(o, parent);
            case NODE_IF:
                return new NodeIf(o, parent);
            case NODE_RETURN:
                return new NodeReturn(o, parent);
            case NODE_SELECT:
                return new NodeSelect(o, parent);
            case NODE_VALUE:
                return new NodeValue(o, parent);
            case NODE_VARIABLE_ASSIGNATION:
                return new NodeVariableAssignation(o, parent);
            case NODE_WHEN:
                return new NodeWhen(o, parent);
            case NODE_PROGRAM:
                throw new SpokException("Unable to build program node inside other programs", null);
            case NODE_SEQ_RULES:
                return new NodeSeqRules(o, parent);
            default:
                throw new SpokNodeException("NodeBuilder", "type", null);
        }
    }
}
