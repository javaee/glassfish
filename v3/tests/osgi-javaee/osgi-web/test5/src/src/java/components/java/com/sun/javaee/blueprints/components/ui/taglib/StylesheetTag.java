/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: StylesheetTag.java,v 1.3 2005/11/04 04:40:41 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.taglib;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;
import javax.el.ValueExpression;


/**
 * This class is the tag handler that evaluates the <code>stylesheet</code>
 * custom tag.
 */

public class StylesheetTag extends UIComponentELTag {


    private ValueExpression path = null;


    public void setPath(ValueExpression path) {
        this.path = path;
    }


    public String getComponentType() {
        return ("javax.faces.Output");
    }


    public String getRendererType() {
        return "Stylesheet";
    }


    protected void setProperties(UIComponent component) {

        super.setProperties(component);

        if (path != null) {
             if (!path.isLiteralText()) {
                component.setValueExpression("path", path);
            } else {
                component.getAttributes().put("path", path.getExpressionString());
            }
        }

    }


}
