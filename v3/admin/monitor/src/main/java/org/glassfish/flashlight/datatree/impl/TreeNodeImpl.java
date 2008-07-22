/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree.impl;

/**
 *
 * @author hsingh
 */
public class TreeNodeImpl extends AbstractTreeNode {
    
    public TreeNodeImpl (String name, Object instance, String category){
        setName (name);
        setValue (instance);
        setCategory (category);
        setEnabled (true);
    }

}
