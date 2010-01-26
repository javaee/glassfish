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
