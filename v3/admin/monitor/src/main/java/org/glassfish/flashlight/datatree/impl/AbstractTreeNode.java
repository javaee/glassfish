/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.flashlight.datatree.impl;

import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.annotations.Monitorable;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static String NAME_SEPARATOR = ".";
    private static String REGEX =
            NAME_SEPARATOR.equals(".") ? "\\." : NAME_SEPARATOR;
    private TreeNode parent = null;

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
        if (newChild == null) {
            return null;
        } else if (newChild.getName() == null) {
            // log it and return null
            return null;
        }
        newChild.setParent(this);
        return children.put(newChild.getName(), newChild);
    }

    public String getCompletePathName() {

        if (getParent() != null) {
            return getParent().getCompletePathName() +
                    this.NAME_SEPARATOR + getName();
        } else {
            return getName();
        }
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getParent() {
        return this.parent;
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
        if (child != null) {
            children.remove(child);
        }
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

        if (completeName == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(this.REGEX);
        String[] tokens = pattern.split(completeName);
        TreeNode n = findNodeInTree(tokens);
        return n;
    }

    private TreeNode findNodeInTree(String[] tokens) {
        if (tokens == null) {
            return null;
        }
        TreeNode child = getChild(tokens[0]);
        if (child == null) {
            return null;
        }
        if (tokens.length > 1) {
            child = ((AbstractTreeNode) child).findNodeInTree(dropFirstStringToken(tokens));
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

    public List<TreeNode> traverse() {
        // System.out.println ("Node: " + this.getName ());
        List<TreeNode> list = new ArrayList<TreeNode>();
        list.add(this);

        if (!hasChildNodes()) {
            return list;
        }

        Collection<TreeNode> childList = children.values();
        for (TreeNode node : childList) {
            list.addAll(node.traverse());
        }
        return list;
    }

    public List<TreeNode> getNodes(String regex) {
        List<TreeNode> regexMatchedTree = new ArrayList<TreeNode>();

        try {
            Pattern pattern = Pattern.compile(regex);
            List<TreeNode> completeTree = traverse();

            for (TreeNode node : completeTree) {
                Matcher matcher = pattern.matcher(node.getName());

                if (matcher.matches()) {
                    regexMatchedTree.add(node);
                }
            }
        } catch (java.util.regex.PatternSyntaxException e) {
            // log this
            e.printStackTrace ();
        }
        return regexMatchedTree;
    }
}

