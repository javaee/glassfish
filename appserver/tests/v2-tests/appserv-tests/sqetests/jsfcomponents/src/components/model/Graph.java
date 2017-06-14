/*
 * $Id: Graph.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.model;

import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>Graph is a JavaBean representing a complete graph of {@link Node}s.</p>
 */

public class Graph {

    // ----------------------------------------------------------- Constructors


    // No-args constructor
    public Graph() {
        super();
    }


    // Constructor with specified root
    public Graph(Node root) {
        setRoot(root);
    }


    // ------------------------------------------------------------- Properties


    /**
     * The collection of nodes that represent this hierarchy, keyed by name.
     */
    protected HashMap registry = new HashMap();

    // The root node
    private Node root = null;


    public Node getRoot() {
        return (this.root);
    }


    public void setRoot(Node root) {
        setSelected(null);
        if (this.root != null) {
            removeNode(this.root);
        }
        if (root != null) {
            addNode(root);
        }
        root.setLast(true);
        this.root = root;
    }


    // The currently selected node (if any)
    private Node selected = null;


    public Node getSelected() {
        return (this.selected);
    }


    public void setSelected(Node selected) {
        if (this.selected != null) {
            this.selected.setSelected(false);
        }
        this.selected = selected;
        if (this.selected != null) {
            this.selected.setSelected(true);
        }
    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Find and return the named {@link Node} if it exists; otherwise,
     * return <code>null</code>.  The search expression must start with a
     * slash character ('/'), and the name of each intervening node is
     * separated by a slash.</p>
     *
     * @param path Absolute path to the requested node
     */
    public Node findNode(String path) {

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException(path);
        }
        Node node = getRoot();
        path = path.substring(1);
        while (path.length() > 0) {
            String name = null;
            int slash = path.indexOf("/");
            if (slash < 0) {
                name = path;
                path = "";
            } else {
                name = path.substring(0, slash);
                path = path.substring(slash + 1);
            }
            node = node.findChild(name);
            if (node == null) {
                return (null);
            }
        }
        return (node);

    }


    /**
     * Register the specified node in our registry of the complete tree.
     *
     * @param node The <code>Node</code> to be registered
     *
     * @throws IllegalArgumentException if the name of this node
     *                                  is not unique
     */
    protected void addNode(Node node) throws IllegalArgumentException {

        synchronized (registry) {
            String name = node.getName();
            if (registry.containsKey(name)) {
                throw new IllegalArgumentException("Name '" + name +
                                                   "' is not unique");
            }
            node.setGraph(this);
            registry.put(name, node);
        }

    }


    /**
     * Deregister the specified node, as well as all child nodes of this
     * node, from our registry of the complete tree.  If this node is not
     * present, no action is taken.
     *
     * @param node The <code>Node</code> to be deregistered
     */
    void removeNode(Node node) {

        synchronized (registry) {
            Iterator nodeItr = node.getChildren();
            while (nodeItr.hasNext()) {
                removeNode((Node) nodeItr.next());
            }
            node.setParent(null);
            node.setGraph(null);
            if (node == this.root) {
                this.root = null;
            }
        }

    }


    /**
     * Return <code>Node</code> by looking up the node registry.
     *
     * @param nodename Name of the <code>Node</code> to look up.
     */
    public Node findNodeByName(String nodename) {

        synchronized (registry) {
            return ((Node) registry.get(nodename));
        }
    }
}
