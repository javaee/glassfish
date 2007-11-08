/*
 * $Id: GraphBean.java,v 1.1 2005/11/03 03:00:17 SherryShen Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package demo.model;

import components.components.GraphComponent;
import components.model.Graph;
import components.model.Node;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import javax.faces.event.ActionEvent;

import java.util.Iterator;

/**
 * <p>Backing file bean for TreeControl demo.</p>
 */

public class GraphBean {

    private static Log log = LogFactory.getLog(GraphBean.class);
    Graph menuGraph = null;
    Graph treeGraph = null;


    public GraphBean() {
    }


    public Graph getMenuGraph() {
        // Construct a preconfigured customer list lazily.
        if (menuGraph == null) {
            Node root = new Node("Menu 2", "Menu", null, null, false, true);
            menuGraph = new Graph(root);

            Node file = new Node("File", "File 2", "/demo-test.faces", null,
                                 true, true);
            root.addChild(file);
            file.addChild(
                new Node("File-New", "New 2", "/demo-test.faces", null, true,
                         false));
            file.addChild(
                new Node("File-Open", "Open 2", "/demo-test.faces", null, true,
                         false));
            Node close = new Node("File-Close", "Close 2", "/demo-test.faces",
                                  null, false, false);
            file.addChild(close);
            file.addChild(
                new Node("File-Exit", "Exit 2", "/demo-test.faces", null, true,
                         false));

            Node edit = new Node("Edit", "Edit 2", "/demo-test.faces", null,
                                 true, false);
            root.addChild(edit);
            edit.addChild(
                new Node("Edit-Cut", "Cut 2", "/demo-test.faces", null, true,
                         false));
            edit.addChild(
                new Node("Edit-Copy", "Copy 2", "/demo-test.faces", null, true,
                         false));
            edit.addChild(
                new Node("Edit-Paste", "Paste 2", "/demo-test.faces", null,
                         false, false));

            menuGraph.setSelected(close);
        }
        return menuGraph;
    }


    public void setMenuGraph(Graph newMenuGraph) {
        this.menuGraph = newMenuGraph;
    }


    public Graph getTreeGraph() {
        // Construct a preconfigured Graph lazily.
        if (treeGraph == null) {
            Node root = new Node("Menu 4", "Menu 4", null, null, false, true);
            treeGraph = new Graph(root);

            Node file = new Node("File", "File 4", "/demo-test.faces", null,
                                 true, true);
            root.addChild(file);
            file.addChild(
                new Node("File-New", "New 4", "/demo-test.faces", null, true,
                         false));
            file.addChild(
                new Node("File-Open", "Open 4", "/demo-test.faces", null, true,
                         false));
            Node close = new Node("File-Close", "Close 4", "/demo-test.faces",
                                  null, false, false);
            file.addChild(close);
            file.addChild(
                new Node("File-Exit", "Exit 4", "/demo-test.faces", null, true,
                         false));

            Node edit = new Node("Edit", "Edit 4", "/demo-test.faces", null,
                                 true, false);
            root.addChild(edit);
            edit.addChild(
                new Node("Edit-Cut", "Cut 4", "/demo-test.faces", null, true,
                         false));
            edit.addChild(
                new Node("Edit-Copy", "Copy 4", "/demo-test.faces", null, true,
                         false));
            edit.addChild(
                new Node("Edit-Paste", "Paste 4", "/demo-test.faces", null,
                         false, false));

            treeGraph.setSelected(close);
        }
        return treeGraph;
    }


    public void setTreeGraph(Graph newTreeGraph) {
        this.treeGraph = newTreeGraph;
    }


    /*
     * Processes the event queued on the graph component when a particular
     * node in the tree control is to be expanded or collapsed.
     */
    public void processGraphEvent(ActionEvent event) {
        if (log.isTraceEnabled()) {
            log.trace("TRACE: GraphBean.processGraphEvent ");
        }
        Graph graph = null;
        GraphComponent component = (GraphComponent) event.getSource();
        String path = (String) component.getAttributes().get("path");

        // Acquire the root node of the graph representing the menu
        graph = (Graph) component.getValue();
        if (graph == null) {
            if (log.isErrorEnabled()) {
                log.error("ERROR: Graph could not located in scope ");
            }
        }
        // Toggle the expanded state of this node
        Node node = graph.findNode(path);
        if (node == null) {
            if (log.isErrorEnabled()) {
                log.error("ERROR: Node " + path + "could not be located. ");
            }
            return;
        }
        boolean current = node.isExpanded();
        node.setExpanded(!current);
        if (!current) {
            Node parent = node.getParent();
            if (parent != null) {
                Iterator kids = parent.getChildren();
                while (kids.hasNext()) {
                    Node kid = (Node) kids.next();
                    if (kid != node) {
                        kid.setExpanded(false);
                    }
                }
            }
        }
    }

}
