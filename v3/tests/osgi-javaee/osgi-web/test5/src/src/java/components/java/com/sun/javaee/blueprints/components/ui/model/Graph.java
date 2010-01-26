/*
 * Copyright 2005-2010 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.javaee.blueprints.components.ui.model;

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
