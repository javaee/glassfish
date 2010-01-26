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
 * This class creates a <code>Graph</code> instance if there is no modelReference
 * attribute specified on the component, represented by this tag and
 * stores it against the attribute name "graph_tree" in session scope.
 */
public class GraphMenuTreeTag extends UIComponentELTag {

    protected MethodExpression actionListener = null;
    protected ValueExpression styleClass = null;
    protected ValueExpression selectedClass = null;
    protected ValueExpression unselectedClass = null;
    protected ValueExpression value = null;
    protected ValueExpression immediate = null;


    /**
     * <code>MethodExpression</code> to handle menu expansion and 
     * contraction events
     */
    public void setActionListener(MethodExpression actionListener) {
        this.actionListener = actionListener;
    }


    /**
     * <code>ValueExpression</code> that points to a Graph in scoped
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
        return ("MenuTree");
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
            // if the value is not value reference expression, we need
            // to build the graph using the nested node tags.
            if (!value.isLiteralText()) {
                graphComponent.setValueExpression("value", value);
            }
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            ValueExpression ve =
                  Util.getValueExpression("#{sessionScope.graph_tree}",
                                          Graph.class,
                                          context);
            component.setValueExpression("value", ve);
            Graph graph = (Graph) graphComponent.getValue();
            if (graph == null) {
                graph = new Graph();
                ve.setValue(context.getELContext(), graph);
            }
        }
    }

}
