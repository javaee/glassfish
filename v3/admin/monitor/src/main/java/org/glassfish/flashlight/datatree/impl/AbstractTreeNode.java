/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.flashlight.datatree.impl;

import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.Monitorable;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.*;

/**
 *
 * @author Harpreet Singh
 */
public abstract class AbstractTreeNode implements TreeNode {

    protected Map<String, TreeNode> children =
            new ConcurrentHashMap<String, TreeNode>();
    protected String name;    // The node object itself
    protected Object instance;
    
    protected String category;
    protected boolean enabled = false;
    
    private static char NAME_SEPARATOR = '.';

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new RuntimeException("Flashlight-utils: Tree Node needs a" +
                    " non-null name");
        }
        this.name = name;
    }

    // should be implemented at the sub-class level
    public Object getValue() {
        if (enabled) {
            return this.instance;
        } 
        return null;
    }

    public void setValue(Object value) {
        if (value == null) {
            throw new RuntimeException("Flashlight-utils: Tree Node" +
                    " needs a non-null value");
        }
        this.instance = value;
    }
    /*    public Object getValue(){
    
    Class clazz = instance.getClass();
    clazz.getAnnotation(Monitorable.class);
    }
     */

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TreeNode addChild(TreeNode newChild) {
        return children.put(newChild.getName(), newChild);
    }

    /**
     * Returns a mutable view of the children
     * @return
     */
    public Collection<TreeNode> getChildNodes() {
        return children.values();
    }

    public Enumeration<TreeNode> getChildNodesImmutable() {

        return ((ConcurrentHashMap) children).elements();
    }

    public boolean hasChildNodes() {
        return !children.isEmpty();

    }

    public void removeChild(TreeNode oldChild) {
        String child = oldChild.getName();
        if (child != null)
            children.remove(child);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TreeNode getChild(String childName) {
        if (childName == null) {
            return null;
        } else {
            return children.get(childName);
        }
    }

    public TreeNode getNode(String completeName) {
        
        if (completeName == null)
            return null;
        
        char regex = NAME_SEPARATOR;
        // dots mean any character in regex. Replacing them to make 
        // final regex easier
        String split = completeName.replace(regex, ':');
        String[] tokens = split.split(":");
        TreeNode n = null;
        if (tokens.length == 1) {
            if (this.getName().equals(tokens[0])) {
                n = this;
            }
        } else {

            n = findNodeInTree(dropFirstStringToken(tokens));

        }
        return n;
    }

    public TreeNode findNodeInTree(String[] tokens) {
        if (tokens == null)
            return null;
        
        TreeNode child = getChild(tokens[0]);
        if (child == null) {
            return null;
        }
        if (tokens.length > 1) {
            child = ((AbstractTreeNode)child).
                    findNodeInTree(dropFirstStringToken(tokens));
        }
        
        return child;

    }

    private String[] dropFirstStringToken(String[] token) {
        if (token.length == 0) {
            return null;
        }
        if (token.length == 1) {
            return null;
        }
        String[] newToken = new String[token.length - 1];
        for (int i = 0; i < newToken.length; i++) {
            newToken[i] = token[i + 1];
        }
        return newToken;
    }
}

  
