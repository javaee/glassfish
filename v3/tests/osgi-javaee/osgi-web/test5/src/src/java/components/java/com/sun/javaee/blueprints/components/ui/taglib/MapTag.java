/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: MapTag.java,v 1.3 2005/11/01 21:59:12 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.taglib;


import com.sun.javaee.blueprints.components.ui.components.MapComponent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.el.*;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.webapp.UIComponentELTag;


/**
 * <p>{@link UIComponentTag} for an image map.</p>
 */

public class MapTag extends UIComponentELTag {


    private javax.el.ValueExpression current = null;


    public void setCurrent(javax.el.ValueExpression current) {
        this.current = current;
    }


    private javax.el.MethodExpression actionListener = null;


    public void setActionListener(javax.el.MethodExpression actionListener) {
        this.actionListener = actionListener;
    }


    private javax.el.MethodExpression action = null;


    public void setAction(javax.el.MethodExpression action) {
        this.action = action;
    }


    private javax.el.ValueExpression immediate = null;


    public void setImmediate(javax.el.ValueExpression immediate) {
        this.immediate = immediate;
    }


    private javax.el.ValueExpression styleClass = null;


    public void setStyleClass(javax.el.ValueExpression styleClass) {
        this.styleClass = styleClass;
    }
    
    public String getComponentType() {
        return ("DemoMap");
    }


    public String getRendererType() {
        return ("DemoMap");
    }


    public void release() {
        super.release();
        current = null;
        styleClass = null;
        actionListener = null;
        action = null;
        immediate = null;
        styleClass = null;
    }


   protected void setProperties(UIComponent component) {
        super.setProperties(component);
        MapComponent map = (MapComponent) component;
        if (styleClass != null) {
            if (!styleClass.isLiteralText()) {
                map.setValueExpression("styleClass", styleClass);
            } else {
                map.getAttributes().put("styleClass", styleClass.getExpressionString());
            }
        }
        if (actionListener != null) {
            map.addActionListener(new MethodExpressionActionListener(actionListener));
        }

        if (action != null) {	    
	    map.setActionExpression(action);
        }
        if (immediate != null) {
            if (!immediate.isLiteralText()) {
                map.setValueExpression("immediate", immediate);
            } else {
                map.setImmediate(new Boolean(immediate.getExpressionString()).booleanValue());
            }
        }

    }


}
