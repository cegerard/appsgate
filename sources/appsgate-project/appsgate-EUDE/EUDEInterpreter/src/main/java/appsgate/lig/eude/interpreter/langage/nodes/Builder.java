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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class Builder {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

    /**
     *
     */
    private static enum NODE_TYPE {

        NODE_ACTION, NODE_BOOLEAN_EXPRESSION,
        NODE_EVENT, NODE_EVENTS_OR, NODE_EVENTS_AND, NODE_EVENTS_SEQUENCE,
        NODE_FUNCTION,  NODE_FUNCTION_DEFINITION, NODE_IF,
        NODE_KEEP_STATE, NODE_LISTS, NODE_PROGRAM, NODE_RETURN,
        NODE_SELECT, NODE_SELECT_STATE, NODE_STATE, NODE_SEQ_RULES, NODE_SET_OF_RULES, NODE_VALUE,
        NODE_VARIABLE_ASSIGNATION, NODE_VARIABLE_DEFINITION, NODE_WHEN, NODE_WHILE;
    }

    /**
     * Return the type for a given string
     *
     * @param type
     * @return NODE_TYPE
     * @throws SpokException
     */
    private static NODE_TYPE getType(String type) throws SpokTypeException {
        if (type.equalsIgnoreCase("action")) {
            return NODE_TYPE.NODE_ACTION;
        }
        if (type.equalsIgnoreCase("booleanExpression")) {
            return NODE_TYPE.NODE_BOOLEAN_EXPRESSION;
        }
        if (type.equalsIgnoreCase("event")) {
            return NODE_TYPE.NODE_EVENT;
        }
        if (type.equalsIgnoreCase("eventsOr")) {
            return NODE_TYPE.NODE_EVENTS_OR;
        }
        if (type.equalsIgnoreCase("eventsSequence")) {
            return NODE_TYPE.NODE_EVENTS_SEQUENCE;
        }
        if (type.equalsIgnoreCase("eventsAnd")) {
            return NODE_TYPE.NODE_EVENTS_AND;
        }
        if (type.equalsIgnoreCase("function")) {
            return NODE_TYPE.NODE_FUNCTION;
        }
        if (type.equalsIgnoreCase("functionDefinition")) {
            return NODE_TYPE.NODE_FUNCTION_DEFINITION;
        }
        if (type.equalsIgnoreCase("variableDefinition")) {
            return NODE_TYPE.NODE_VARIABLE_DEFINITION;
        }
        if (type.equalsIgnoreCase("if")) {
            return NODE_TYPE.NODE_IF;
        }
        if (type.equalsIgnoreCase("keepstate")) {
            return NODE_TYPE.NODE_KEEP_STATE;
        }
        if (type.equalsIgnoreCase("lists")) {
            return NODE_TYPE.NODE_LISTS;
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
        if (type.equalsIgnoreCase("selectState")) {
            return NODE_TYPE.NODE_SELECT_STATE;
        }
        if (type.equalsIgnoreCase("state")) {
            return NODE_TYPE.NODE_STATE;
        }
        if (type.equalsIgnoreCase("number")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("list")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("device")) {
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
        if (type.equalsIgnoreCase("programCall")) {
            return NODE_TYPE.NODE_VALUE;
        }
        if (type.equalsIgnoreCase("assignation")) {
            return NODE_TYPE.NODE_VARIABLE_ASSIGNATION;
        }
        if (type.equalsIgnoreCase("when")) {
            return NODE_TYPE.NODE_WHEN;
        }
        if (type.equalsIgnoreCase("while")) {
            return NODE_TYPE.NODE_WHILE;
        }
        if (type.equalsIgnoreCase("seqRules")) {
            return NODE_TYPE.NODE_SEQ_RULES;
        }
        if (type.equalsIgnoreCase("setOfRules")) {
            return NODE_TYPE.NODE_SET_OF_RULES;
        }
        LOGGER.debug("The type [{}] does not exists", type);
        throw new SpokTypeException(type);
    }

    /**
     *
     * @param o
     * @param parent
     * @return
     * @throws SpokTypeException
     */
    public static Node buildFromJSON(JSONObject o, Node parent)
            throws SpokTypeException {
        if (o == null) {
            LOGGER.warn("No node to build");
            throw new SpokTypeException("no node");

        }
        if (!o.has("type")) {
            LOGGER.debug("No type: {}", o.toString());
            throw new SpokTypeException("no type");
        }
        try {
            switch (getType(o.optString("type"))) {
                case NODE_ACTION:
                    return new NodeAction(o, parent);
                case NODE_BOOLEAN_EXPRESSION:
                    return new NodeBooleanExpression(o, parent);
                case NODE_EVENT:
                    return new NodeEvent(o, parent);
                case NODE_EVENTS_OR:
                    return new NodeEventsOr(o, parent);
                case NODE_EVENTS_SEQUENCE:
                    return new NodeEventsSequence(o, parent);
                case NODE_EVENTS_AND:
                    return new NodeEventsAnd(o, parent);
                case NODE_FUNCTION:
                    return new NodeFunction(o, parent);
                case NODE_FUNCTION_DEFINITION:
                    return new NodeFunctionDefinition(o, parent);
                case NODE_IF:
                    return new NodeIf(o, parent);
                case NODE_KEEP_STATE:
                    return new NodeKeepState(o, parent);
                case NODE_LISTS:
                    return new NodeLists(o, parent);
                case NODE_RETURN:
                    return new NodeReturn(o, parent);
                case NODE_SELECT:
                    return new NodeSelect(o, parent);
                case NODE_SELECT_STATE:
                    return new NodeSelectState(o, parent);
                case NODE_STATE:
                    return new NodeState(o, parent);
                case NODE_VALUE:
                    return new NodeValue(o, parent);
                case NODE_VARIABLE_ASSIGNATION:
                    return new NodeVariableAssignation(o, parent);
                case NODE_WHEN:
                    return new NodeWhen(o, parent);
                case NODE_WHILE:
                    return new NodeWhile(o, parent);
                case NODE_PROGRAM:
                    throw new SpokTypeException("Unable to build program node inside other programs", null);
                case NODE_SEQ_RULES:
                    return new NodeSeqRules(o, parent);
                case NODE_SET_OF_RULES:
                    return new NodeSetOfRules(o, parent);
                case NODE_VARIABLE_DEFINITION:
                    return new NodeVariableDefinition(o, parent);
                default:
                    LOGGER.debug("No such type found : {}", o.toString());
                    throw new SpokNodeException("NodeBuilder", "type", null);
            }
        } catch (SpokNodeException ex) {
            LOGGER.debug("Unable to build node: {}", o.optString("type"));
            throw new SpokTypeException("Node not built: " + o.toString(), ex);
        } catch (SpokException ex) {
            throw new SpokTypeException("Variable node definition can not be built", ex);
        }
    }

    /**
     *
     * @param o
     * @param parent
     * @return
     * @throws SpokTypeException
     */
    public static Node nodeOrNull(JSONObject o, Node parent)
            throws SpokTypeException {
        if (o == null) {
            return null;
        }
        return buildFromJSON(o, parent);
    }
}
