/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight;

import org.glassfish.flashlight.datatree.TreeNode;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Harpreet Singh
 */
@Contract
public interface MonitoringRuntimeDataRegistry {
    
    public void add (String name, TreeNode node);
    /*
    public void remove (String name);
    */
    
    /**
     * @param name of the top node in the registry
     * @return TreeNode
     */
    public TreeNode get (String name);

}
