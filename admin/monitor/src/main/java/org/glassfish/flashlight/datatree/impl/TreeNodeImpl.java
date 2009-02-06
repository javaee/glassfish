/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree.impl;

/**
 *
 * @author Harpreet Singh
 */
public class TreeNodeImpl extends AbstractTreeNode {
    
    public TreeNodeImpl(String name, String category){
        setName (name);
        setCategory (category);
        setEnabled (true);
    }

}
