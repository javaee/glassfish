/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html  
$Id: PaneTabbedTag.java,v 1.5 2005/11/04 04:40:41 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.taglib;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents a the overall tabbed pane control.
 */
public class PaneTabbedTag extends UIComponentELTag {

    private ValueExpression contentClass = null;


    public void setContentClass(ValueExpression contentClass) {
        this.contentClass = contentClass;
    }

    private ValueExpression paneClass = null;


    public void setPaneClass(ValueExpression paneClass) {
        this.paneClass = paneClass;
    }


    private ValueExpression selectedClass = null;


    public void setSelectedClass(ValueExpression selectedClass) {
        this.selectedClass = selectedClass;
    }


    private ValueExpression unselectedClass = null;


    public void setUnselectedClass(ValueExpression unselectedClass) {
        this.unselectedClass = unselectedClass;
    }


    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("Tabbed");
    }

    public void release() {
        super.release();
        contentClass = null;
        paneClass = null;
        selectedClass = null;
        unselectedClass = null;
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        if (contentClass != null) {
            if (!contentClass.isLiteralText()) {
                component.setValueExpression("contentClass", contentClass);
            } else {
                component.getAttributes().put("contentClass", contentClass.getExpressionString());
            }
        }

        if (paneClass != null) {
            if (!paneClass.isLiteralText()) {
                component.setValueExpression("paneClass", paneClass);
            } else {
                component.getAttributes().put("paneClass", paneClass.getExpressionString());
            }
        }

        if (selectedClass != null) {
            if (!selectedClass.isLiteralText()) {
                component.setValueExpression("selectedClass", selectedClass);
            } else {
                component.getAttributes().put("selectedClass", selectedClass.getExpressionString());
            }
        }

        if (unselectedClass != null) {
            if (!unselectedClass.isLiteralText()) {
                component.setValueExpression("unselectedClass", unselectedClass);
            } else {
                component.getAttributes().put("unselectedClass", unselectedClass.getExpressionString());
            }
        }
    }

}
