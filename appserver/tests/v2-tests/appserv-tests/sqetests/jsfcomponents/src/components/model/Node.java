/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * $Id: Node.java,v 1.4 2004/11/14 07:33:14 tcfujii Exp $
 */

package components.model;


import java.util.ArrayList;
import java.util.Iterator;


/**
 * <p>Node is a JavaBean representing a node in a tree control or menu.</p>
 */

public class Node {


    // ----------------------------------------------------------- Constructors


    // No-args constructor
    public Node() {
        super();
    }


    // Full-up constructor
    public Node(String name, String label, String action, String icon,
                boolean enabled, boolean expanded) {
        setName(name);
        setLabel(label);
        setAction(action);
        setIcon(icon);
        setEnabled(enabled);
        setExpanded(expanded);
    }


    // ----------------------------------------------------- Instance Variables

    /**
     * Maintains a list of all the child nodes of this node.
     */
    private ArrayList children = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * The <code>Graph</code> instance representing the
     * entire tree.
     */
    protected Graph graph = null;


    void setGraph(Graph graph) {
        this.graph = graph;
    }


    public Graph getGraph() {
        return graph;
    }


    /*
     * Node action (context-relative URL triggered when node selected)
     */
    private String action = null;


    public String getAction() {
        return (this.action);
    }


    public void setAction(String action) {
        this.action = action;
    }


    private String icon = null;


    /*
     * Icon for this node if any.
     */
    public String getIcon() {
        return (this.icon);
    }


    public void setIcon(String icon) {
        this.icon = icon;
    }


    /*
     * Returns the number of children of this Node.
     */
    public int getChildCount() {
        return (children.size());
    }


    /*
     * The nesting depth of this Node
     */
    private int depth = 1;


    public int getDepth() {
        return (this.depth);
    }


    /*
     * Is this node currently enabled (available for use by the user)?
     */
    private boolean enabled = false;


    public boolean isEnabled() {
        return (this.enabled);
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /*
     * Is this node currently expanded (in a tree control) or open (in a menu)?
     */
    private boolean expanded = false;


    public boolean isExpanded() {
        return (this.expanded);
    }


    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }


    /*
     * Node label (visible representation)
     */
    private String label = null;


    public String getLabel() {
        return (this.label);
    }


    public void setLabel(String label) {
        this.label = label;
    }


    /*
     * Node name
     */
    private String name = null;


    public String getName() {
        return (this.name);
    }


    public void setName(String name) {
        this.name = name;
    }


    /*
     * The parent Node
     */
    private Node parent = null;


    public Node getParent() {
        return (this.parent);
    }


    void setParent(Node parent) {
        this.parent = parent;
        if (parent == null) {
            depth = 1;
        } else {
            depth = parent.getDepth() + 1;
        }
    }


    /*
     * Returns the absolute path of this node
     */
    public String getPath() {

        Node parent = getParent();
        if (parent == null) {
            return ("/");
        }

        ArrayList list = new ArrayList();
        list.add(getName());
        while (parent != null) {
            list.add(0, parent.getPath());
            parent = parent.getParent();
        }

        StringBuffer sb = new StringBuffer();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            if (i != 1) {
                sb.append("/");
            }
            if (i > 0) {
                sb.append((String) list.get(i));
            }
        }
        return (sb.toString());

    }


    /*
     * Is this node the currently selected one in the entire tree?
     */
    private boolean selected = false;


    public boolean isSelected() {
        return (this.selected);
    }


    void setSelected(boolean selected) {
        this.selected = selected;
    }


    /**
     * Is this the last node in the set of children for our parent node?
     */
    protected boolean last = false;


    public boolean isLast() {
        return (this.last);
    }


    void setLast(boolean last) {
        this.last = last;
    }


    /**
     * <p> Is this a "leaf" node that is with no children?
     */
    public boolean isLeaf() {
        synchronized (children) {
            return (children.size() < 1);
        }
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Adds the specified node as a child of this node and sets this node
     * as its parent.
     */
    public void addChild(Node child) {
        if (child.getParent() != null) {
            throw new IllegalArgumentException("Child already has a parent");
        }
        // if graph is created after all the nodes are created, then
        // graph will be null.
        if (graph != null) {
            graph.addNode(child);
        }
        synchronized (children) {
            children.add(child);
        }
        child.setParent(this);

        int n = children.size();
        if (n > 0) {
            Node node = (Node) children.get(n - 1);
            node.setLast(false);
        }
        child.setLast(true);
    }


    /**
     * Adds the specified node as a child of this node at the
     * specifed offset and sets this node as its parent.
     */
    public void addChild(int offset, Node child) {
        if (child.getParent() != null) {
            throw new IllegalArgumentException("Child already has a parent");
        }
        if (graph != null) {
            graph.addNode(child);
        }
        synchronized (children) {
            children.add(offset, child);
        }
        child.setParent(this);
    }


    /**
     * Returns the node with the specified name by looking up
     * by child list. If node is not found returns <code>null</code>
     */
    public Node findChild(String name) {
        int n = children.size();
        for (int i = 0; i < n; i++) {
            Node kid = (Node) children.get(i);
            if (name.equals(kid.getName())) {
                return (kid);
            }
        }
        return (null);
    }


    /**
     * Returns and <code>iterator</code> over the children of this node.
     */
    public Iterator getChildren() {
        return (children.iterator());
    }


    /**
     * Removes the specified node from the child list of this node.
     */
    public void removeChild(Node child) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException(
                "Child not related to this node");
        }
        synchronized (children) {
            children.remove(child);
        }
        graph.removeNode(child);
        child.setParent(null);
    }


    /**
     * Returns a string representing a description of this node.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Node[name=");
        sb.append(name);
        if (label != null) {
            sb.append(",label=");
            sb.append(label);
        }
        if (action != null) {
            sb.append(",action=");
            sb.append(action);
        }
        sb.append(",enabled=");
        sb.append(enabled);
        sb.append(",expanded=");
        sb.append(expanded);
        sb.append("]");
        return sb.toString();
    }


}
