/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;
/**
 *
 * @author jr
 */
public interface ICanBeEvaluated {

    /**
     * Method that return the value associated to a node Must be overridden
     *
     * @return 
     */
    public NodeValue getResult();

    /**
     * 
     * @return the type of the result
     */
    public String getResultType();
}
