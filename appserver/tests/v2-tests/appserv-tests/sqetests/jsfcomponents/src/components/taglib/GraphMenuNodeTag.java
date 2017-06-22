/*
 * $Id: GraphMenuNodeTag.java,v 1.3 2004/11/14 07:33:16 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

// GraphMenuNodeTag.java

package components.taglib;

import components.model.Graph;
import components.model.Node;
import components.renderkit.Util;

import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentBodyTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <B>GraphMenuNodeTag</B> builds the graph as the nodes are processed.
 * This tag creates a node with specified properties. Locates the parent of
 * this node by using the node name from its immediate parent tag of the
 * type GraphTreeNodeTag. If the parent could not be located, then the created
 * node is assumed to be root.
 */

public class GraphMenuNodeTag extends UIComponentBodyTag {

    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    //
    // Instance Variables
    //

    // Attribute Instance Variables

    private String name = null;
    private String icon = null;
    private String label = null;
    private String action = null;
    private boolean expanded;
    private boolean enabled = true;
   
    // Relationship Instance Variables

    //
    // Constructors and Initializers    
    //

    public GraphMenuNodeTag() {
        super();
    }

    //
    // Class methods
    //

    //
    // General Methods
    //
   
    /**
     * Name of the node
     */
    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return this.name;
    }


    /**
     * Should the node appear expanded by default
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }


    /**
     * Icon representing the node.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }


    /**
     * Label for the node.
     */
    public void setLabel(String label) {
        this.label = label;
    }


    /**
     * Should the node be enabled by default
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /**
     * Link the node points to.
     */
    public void setAction(String action) {
        this.action = action;
    }


    public String getComponentType() {
        return null;
    }


    public String getRendererType() {
        return null;
    }


    //
    // Methods from FacesBodyTag
    //
    public int doStartTag() throws JspException {

        FacesContext context = FacesContext.getCurrentInstance();
        Graph graph = (Graph)
            ((Util.getValueBinding("#{sessionScope.graph_menu}").getValue(
                context)));
        // In the postback case, graph and the node exist already.So make sure
        // it doesn't created again.
        if (graph.findNodeByName(name) != null) {
            return BodyTag.EVAL_BODY_BUFFERED;
        }
        Node node = new Node(name, label, action, icon, enabled, expanded);

        // get the immediate ancestor/parent of this node.
        GraphMenuNodeTag parentNode = null;
        try {
            parentNode = (GraphMenuNodeTag) TagSupport.findAncestorWithClass(
                this,
                GraphMenuNodeTag.class);
        } catch (Exception e) {
            System.out.println(
                "Exception while locating GraphMenuNodeTag.class");
        }

        if (parentNode == null) {
            // then this should be root
            graph.setRoot(node);
        } else {
            Node nodeToAdd = graph.findNodeByName(parentNode.getName());
            // this node should exist
            if (nodeToAdd != null) {
                nodeToAdd.addChild(node);
            }
        }

        return BodyTag.EVAL_BODY_BUFFERED;
    }


    public int doEndTag() throws JspException {
        return (EVAL_PAGE);
    }

}
    

