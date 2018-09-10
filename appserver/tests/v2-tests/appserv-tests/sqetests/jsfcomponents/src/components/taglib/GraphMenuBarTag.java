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
 * $Id: GraphMenuBarTag.java,v 1.3 2004/11/14 07:33:16 tcfujii Exp $
 */

package components.taglib;


import components.components.GraphComponent;
import components.model.Graph;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.webapp.UIComponentTag;


/**
 * Tag Handler class for menu control.
 * This class creates a <code>Graph</code> instance if there is no
 * value attribute specified on the component, represented by this tag and
 * stores it against the attribute name "graph_menu" in session scope.
 */

public class GraphMenuBarTag extends UIComponentTag {

    protected String actionListener = null;
    protected String styleClass = null;
    protected String selectedClass = null;
    protected String unselectedClass = null;
    protected String value = null;
    protected String immediate = null;


    /**
     * Optional method reference to handle menu expansion and contraction
     * events.
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }


    /**
     * Value Binding reference expression that points to a Graph in scoped
     * namespace.
     */
    public void setValue(String newValue) {
        value = newValue;
    }


    /**
     * The CSS style <code>class</code> to be applied to the text
     * of selected nodes. This can be value or a value binding reference
     * expression.
     */
    public void setSelectedClass(String styleSelected) {
        this.selectedClass = styleSelected;
    }


    /**
     * The CSS style <code>class</code> to be applied to the text
     * of unselected nodes. This can be value or a value binding reference
     * expression.
     */
    public void setUnselectedClass(String styleUnselected) {
        this.unselectedClass = styleUnselected;
    }


    /**
     * The CSS style <code>class</code> to be applied to the entire menu.
     * This can be value or a value binding reference expression.
     */
    public void setStyleClass(String style) {
        this.styleClass = style;
    }


    /**
     * A flag indicating that the default ActionListener should execute
     * immediately (that is, during the Apply Request Values phase of the
     * request processing lifecycle, instead of waiting for Invoke
     * Application phase). The default value of this property must be false.
     * This can be value or a value binding reference expression.
     */
    public void setImmediate(java.lang.String immediate) {
        this.immediate = immediate;
    }


    public String getComponentType() {
        return ("Graph");
    }


    public String getRendererType() {
        return ("MenuBar");
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        FacesContext context = FacesContext.getCurrentInstance();
        ValueBinding vb = null;

        GraphComponent graphComponent = (GraphComponent) component;

        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                graphComponent.setActionListener(mb);
            } else {
                Object params [] = {actionListener};
                throw new javax.faces.FacesException();
            }
        }
        // if the attributes are values set them directly on the component, if
        // not set the ValueBinding reference so that the expressions can be
        // evaluated lazily.
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                vb = context.getApplication().createValueBinding(styleClass);
                graphComponent.setValueBinding("styleClass", vb);
            } else {
                graphComponent.getAttributes().put("styleClass", styleClass);
            }
        }
        if (selectedClass != null) {
            if (isValueReference(selectedClass)) {
                vb =
                    context.getApplication().createValueBinding(selectedClass);
                graphComponent.setValueBinding("selectedClass", vb);
            } else {
                graphComponent.getAttributes().put("selectedClass",
                                                   selectedClass);
            }
        }
        if (unselectedClass != null) {
            if (isValueReference(unselectedClass)) {
                vb =
                    context.getApplication().createValueBinding(
                        unselectedClass);
                graphComponent.setValueBinding("unselectedClass", vb);
            } else {
                graphComponent.getAttributes().put("unselectedClass",
                                                   unselectedClass);
            }
        }

        if (immediate != null) {
            if (isValueReference(immediate)) {
                vb = context.getApplication().createValueBinding(immediate);
                graphComponent.setValueBinding("immediate", vb);
            } else {
                boolean _immediate = new Boolean(immediate).booleanValue();
                graphComponent.setImmediate(_immediate);
            }
        }

        if (value != null) {
            // if the value is not value reference expression, we need
            // to build the graph using the nested node tags.
            if (isValueReference(value)) {
                vb = context.getApplication().createValueBinding(value);
                component.setValueBinding("value", vb);
            }
        }

        if (value == null) {
            vb =
                context.getApplication().createValueBinding(
                    "#{sessionScope.graph_menu}");
            component.setValueBinding("value", vb); 
           
            // In the postback case, graph exists already. So make sure
            // it doesn't created again.
            Graph graph = (Graph) ((GraphComponent) component).getValue();
            if (graph == null) {
                graph = new Graph();
                vb.setValue(context, graph);
            }
        }
    }
}
