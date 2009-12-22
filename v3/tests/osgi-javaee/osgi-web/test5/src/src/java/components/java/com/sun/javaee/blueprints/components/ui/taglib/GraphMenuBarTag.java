/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: GraphMenuBarTag.java,v 1.3 2005/11/01 21:59:11 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.taglib;


import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.webapp.UIComponentELTag;

import java.util.Map;

import com.sun.javaee.blueprints.components.ui.components.GraphComponent;
import com.sun.javaee.blueprints.components.ui.model.Graph;
import com.sun.javaee.blueprints.components.ui.util.Util;


/**
 * Tag Handler class for menu control.
 * This class creates a <code>Graph</code> instance if there is no
 * value attribute specified on the component, represented by this tag and
 * stores it against the attribute name "graph_menu" in session scope.
 */

public class GraphMenuBarTag extends UIComponentELTag {

    protected MethodExpression actionListener = null;
    protected ValueExpression styleClass = null;
    protected ValueExpression selectedClass = null;
    protected ValueExpression unselectedClass = null;
    protected ValueExpression value = null;
    protected ValueExpression immediate = null;


    /**
     * Optional method reference to handle menu expansion and contraction
     * events.
     */
    public void setActionListener(MethodExpression actionListener) {
        this.actionListener = actionListener;
    }


    /**
     * ValueExpression reference expression that points to a Graph in scoped
     * namespace.
     */
    public void setValue(ValueExpression value) {
        this.value = value;
    }


    /**
     * The CSS style <code>class</code> to be applied to the text
     * of selected nodes. 
     */
    public void setSelectedClass(ValueExpression selectedClass) {
        this.selectedClass = selectedClass;
    }


    /**
     * The CSS style <code>class</code> to be applied to the text
     * of unselected nodes. 
     */
    public void setUnselectedClass(ValueExpression unselectedClass) {
        this.unselectedClass = unselectedClass;
    }


    /**
     * The CSS style <code>class</code> to be applied to the entire menu.    
     */
    public void setStyleClass(ValueExpression styleClass) {
        this.styleClass = styleClass;
    }


    /**
     * A flag indicating that the default ActionListener should execute
     * immediately (that is, during the Apply Request Values phase of the
     * request processing lifecycle, instead of waiting for Invoke
     * Application phase). The default value of this property must be false.    
     */
    public void setImmediate(ValueExpression immediate) {
        this.immediate = immediate;
    }


    public String getComponentType() {
        return ("Graph");
    }


    public String getRendererType() {
        return ("MenuBar");
    }

    @Override
    public void release() {
        super.release();
        actionListener = null;
        styleClass = null;
        selectedClass = null;
        unselectedClass = null;
        value = null;
        immediate = null;
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        GraphComponent graphComponent = (GraphComponent) component;
        Map<String,Object> attributes = graphComponent.getAttributes();

        if (actionListener != null) {
            graphComponent.addActionListener(
                  new MethodExpressionActionListener(actionListener));
        }

        // if the attributes are values set them directly on the component, if
        // not set the ValueExpression reference so that the expressions can be
        // evaluated lazily.        
        if (styleClass != null) {
            if (!styleClass.isLiteralText()) {
                graphComponent.setValueExpression("styleClass", styleClass);
            } else {
                attributes.put("styleClass", styleClass.getExpressionString());
            }
        }
        if (selectedClass != null) {
            if (!selectedClass.isLiteralText()) {
                graphComponent.setValueExpression("selectedClass",
                                                  selectedClass);
            } else {
                attributes.put("selectedClass",
                               selectedClass.getExpressionString());
            }
        }
        if (unselectedClass != null) {
            if (!unselectedClass.isLiteralText()) {
                graphComponent.setValueExpression("unselectedClass",
                                                  unselectedClass);
            } else {
                attributes.put("unselectedClass",
                               unselectedClass.getExpressionString());
            }
        }

        if (immediate != null) {
            if (!immediate.isLiteralText()) {
                graphComponent.setValueExpression("immediate", immediate);
            } else {
                graphComponent.setImmediate(
                      Boolean.valueOf(immediate.getExpressionString()));
            }
        }

        if (value != null) {
            
            if (!value.isLiteralText()) {
                graphComponent.setValueExpression("value", value);
            }
        } else {
            // if the value is a literal, we need
            // to build the graph using the nested node tags.
            FacesContext context = FacesContext.getCurrentInstance();            
            ValueExpression ve =
                  Util.getValueExpression("#{sessionScope.graph_menu}",
                                          Graph.class,
                                          context);
            graphComponent.setValueExpression("value", ve);
            
            // In the postback case, Graph already exists.
            // Don't create again.
            Graph graph = (Graph) graphComponent.getValue();
            if (graph == null) {
                graph = new Graph();
                ve.setValue(context.getELContext(), graph);
            }
        }
    }
}
