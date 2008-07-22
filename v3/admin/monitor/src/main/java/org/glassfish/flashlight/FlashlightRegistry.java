/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight;

import org.glassfish.flashlight.datatree.TreeNode;

/**
 * A registry that has all the nodes registered. It is rooted at "/"
 * TBD: Convert this to a Contract
 * @author Harpreet Singh
 */
public interface FlashlightRegistry {
    
    public void add (String name, TreeNode node);
    public void remove (String name);
    
    /**
     * @return TreeNode
     */
    public TreeNode getNodeFromRegistry (String name);

}
