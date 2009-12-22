/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html  
$Id: PaneTabTag.java,v 1.4 2005/11/04 04:40:41 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.taglib;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents an individual tab on the overall control.
 */
public class PaneTabTag extends UIComponentELTag {

    public String getComponentType() {
        return ("Pane");
    }

    public String getRendererType() {
        return ("Tab");
    }

    public void release() {
        super.release();
    }

    protected ValueExpression paneClass;
    public ValueExpression getPaneClass() {
        return paneClass;
   }

    public void setPaneClass(ValueExpression newPaneClass) {
	paneClass = newPaneClass;
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        if (paneClass != null) {
            if (!paneClass.isLiteralText()) {
                component.setValueExpression("paneClass", paneClass);
            } else {
                component.getAttributes().put("paneClass", paneClass.getExpressionString());
            }
        }
    }
}
