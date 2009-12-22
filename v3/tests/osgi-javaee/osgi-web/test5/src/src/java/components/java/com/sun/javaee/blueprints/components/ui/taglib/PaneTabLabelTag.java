/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html  
$Id: PaneTabLabelTag.java,v 1.5 2005/11/04 04:40:40 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.taglib;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents a tab button control on the tab pane.
 */
public class PaneTabLabelTag extends UIComponentELTag {

    private ValueExpression commandName = null;


    public void setCommandName(ValueExpression newCommandName) {
        commandName = newCommandName;
    }

    private ValueExpression image = null;

    public void setImage(ValueExpression newImage) {
        image = newImage;
    }

    private ValueExpression label = null;


    public void setLabel(ValueExpression newLabel) {
        label = newLabel;
    }

    public String getComponentType() {
        return ("Pane");
    }

    public String getRendererType() {
        return ("TabLabel");
    }

    protected ValueExpression paneTabLabelClass;
    public ValueExpression getPaneTabLabelClass() {
	return paneTabLabelClass;
    }

    public void setPaneTabLabelClass(ValueExpression newPaneTabLabelClass) {
	paneTabLabelClass = newPaneTabLabelClass;
    }

    public void release() {
        super.release();
        this.commandName = null;
        this.image = null;
        this.label = null;
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        if (commandName != null) {
            if (!commandName.isLiteralText()) {
                component.setValueExpression("commandName", commandName);
            } else {
                component.getAttributes().put("commandName", commandName.getExpressionString());
            }
        }

        if (image != null) {
            if (!image.isLiteralText()) {
                component.setValueExpression("image", image);
            } else {
                component.getAttributes().put("image", image.getExpressionString());
            }
        }

        if (label != null) {
            if (!label.isLiteralText()) {
                component.setValueExpression("label", label);
            } else {
                component.getAttributes().put("label", label.getExpressionString());
            }
        }

        if (paneTabLabelClass != null) {
            if (!paneTabLabelClass.isLiteralText()) {
                component.setValueExpression("paneTabLabelClass", paneTabLabelClass);
            } else {
                component.getAttributes().put("paneTabLabelClass", paneTabLabelClass.getExpressionString());
            }
        }
    }
}
