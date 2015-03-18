/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.context.dependency.graph;

import appsgate.lig.context.dependency.spec.Dependencies;

/**
 *
 * @author jr
 */
public interface ProgramGraph {

    public String getProgramName();

    public Dependencies getReferences();

    public String getStateName();
}
