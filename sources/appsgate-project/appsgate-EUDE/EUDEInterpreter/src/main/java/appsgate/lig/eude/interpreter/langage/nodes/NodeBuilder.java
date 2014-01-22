/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeBuilder {

    private static enum NODE_TYPE {

        NODE_ACTION, NODE_BINARY_EXPRESSION, NODE_EVENT, NODE_FUNCTION,
        NODE_FUNCTION_DEFINITION, NODE_IF, NODE_PROGRAM, NODE_RELATION_BOOL,
        NODE_RETURN, NODE_SELECT, NODE_VALUE, NODE_VARIABLE_ASSIGNATION, NODE_WHEN;
    }

    private static NODE_TYPE getType(String type) {
        if (type.equalsIgnoreCase("NodeAction")) {
            return NODE_TYPE.NODE_ACTION;
        }
        if (type.equalsIgnoreCase("NodeBinaryExpression")) {
            return NODE_TYPE.NODE_BINARY_EXPRESSION;
        }
        if (type.equalsIgnoreCase("NodeEvent")) {
            return NODE_TYPE.NODE_EVENT;
        }
        if (type.equalsIgnoreCase("NodeFunction")) {
            return NODE_TYPE.NODE_FUNCTION;
        }
        if (type.equalsIgnoreCase("NodeFunctionDefinition")) {
            return NODE_TYPE.NODE_FUNCTION_DEFINITION;
        }
        if (type.equalsIgnoreCase("NodeIf")) {
            return NODE_TYPE.NODE_IF;
        }
        if (type.equalsIgnoreCase("NodeProgram")) {
            return NODE_TYPE.NODE_PROGRAM;
        }
        if (type.equalsIgnoreCase("NodeRelationBool")) {
            return NODE_TYPE.NODE_RELATION_BOOL;
        }
        if (type.equalsIgnoreCase("NodeReturn")) {
            return NODE_TYPE.NODE_RETURN;
        }
        if (type.equalsIgnoreCase("NodeSelect")) {
            return NODE_TYPE.NODE_SELECT;
        }
        if (type.equalsIgnoreCase("number")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("boolean")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("NodeVariableAssignation")) {
            return NODE_TYPE.NODE_VARIABLE_ASSIGNATION;
        }
        if (type.equalsIgnoreCase("NodeWhen")) {
            return NODE_TYPE.NODE_WHEN;
        }
        return null;
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
            case NODE_FUNCTION:
                return new NodeFunction(o, parent);
            case NODE_FUNCTION_DEFINITION:
                return new NodeFunctionDefinition(o, parent);
            case NODE_IF:
                return new NodeIf(o, parent);
            case NODE_RELATION_BOOL:
                return new NodeRelationBool(o, parent);
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
            default:
                throw new SpokNodeException("NodeBuilder", "type", null);
        }
    }
}
