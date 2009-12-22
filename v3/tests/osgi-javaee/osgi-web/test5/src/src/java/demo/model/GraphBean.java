/*
 * $Id: GraphBean.java,v 1.6 2006/03/08 01:52:30 rlubke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package demo.model;

import javax.faces.event.ActionEvent;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.javaee.blueprints.components.ui.components.GraphComponent;
import com.sun.javaee.blueprints.components.ui.model.Graph;
import com.sun.javaee.blueprints.components.ui.model.Node;

/** <p>Backing file bean for TreeControl demo.</p> */

public class GraphBean {

    private static Logger LOGGER = Logger.getLogger("demo.model");    
    
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
                  new Node("File-Open",
                           "Open 2",
                           "/demo-test.faces",
                           null,
                           true,
                           false));
            Node close = new Node("File-Close", "Close 2", "/demo-test.faces",
                                  null, false, false);
            file.addChild(close);
            file.addChild(
                  new Node("File-Exit",
                           "Exit 2",
                           "/demo-test.faces",
                           null,
                           true,
                           false));

            Node edit = new Node("Edit", "Edit 2", "/demo-test.faces", null,
                                 true, false);
            root.addChild(edit);
            edit.addChild(
                  new Node("Edit-Cut", "Cut 2", "/demo-test.faces", null, true,
                           false));
            edit.addChild(
                  new Node("Edit-Copy",
                           "Copy 2",
                           "/demo-test.faces",
                           null,
                           true,
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
                  new Node("File-Open",
                           "Open 4",
                           "/demo-test.faces",
                           null,
                           true,
                           false));
            Node close = new Node("File-Close", "Close 4", "/demo-test.faces",
                                  null, false, false);
            file.addChild(close);
            file.addChild(
                  new Node("File-Exit",
                           "Exit 4",
                           "/demo-test.faces",
                           null,
                           true,
                           false));

            Node edit = new Node("Edit", "Edit 4", "/demo-test.faces", null,
                                 true, false);
            root.addChild(edit);
            edit.addChild(
                  new Node("Edit-Cut", "Cut 4", "/demo-test.faces", null, true,
                           false));
            edit.addChild(
                  new Node("Edit-Copy",
                           "Copy 4",
                           "/demo-test.faces",
                           null,
                           true,
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
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("TRACE: GraphBean.processGraphEvent ");
        }
        Graph graph = null;
        GraphComponent component = (GraphComponent) event.getSource();
        String path = (String) component.getAttributes().get("path");

        // Acquire the root node of the graph representing the menu
        graph = (Graph) component.getValue();
        if (graph == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.severe("ERROR: Graph could not located in scope ");
            }
        }
        // Toggle the expanded state of this node
        Node node = graph.findNode(path);
        if (node == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.severe("ERROR: Node " + path + "could not be located. ");
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
