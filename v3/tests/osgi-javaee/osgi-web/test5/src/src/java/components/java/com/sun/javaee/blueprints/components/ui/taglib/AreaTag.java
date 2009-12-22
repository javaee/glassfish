/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: AreaTag.java,v 1.3 2005/11/01 21:59:11 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.taglib;

import com.sun.javaee.blueprints.components.ui.components.AreaComponent;
import javax.el.ELException;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.webapp.UIComponentELTag;


/**
 * <p>{@link UIComponentTag} for an image map hotspot.</p>
 */

public class AreaTag extends UIComponentELTag {


    private javax.el.ValueExpression alt;


    public void setAlt(javax.el.ValueExpression alt) {
        this.alt = alt;
    }


    private javax.el.ValueExpression targetImage;


    public void setTargetImage(javax.el.ValueExpression targetImage) {
        this.targetImage = targetImage;
    }


    private javax.el.ValueExpression coords;


    public void setCoords(javax.el.ValueExpression coords) {
        this.coords = coords;
    }


    private javax.el.ValueExpression onmouseout;


    public void setOnmouseout(javax.el.ValueExpression newonmouseout) {
        onmouseout = newonmouseout;
    }


    private javax.el.ValueExpression onmouseover = null;


    public void setOnmouseover(javax.el.ValueExpression newonmouseover) {
        onmouseover = newonmouseover;
    }


    private javax.el.ValueExpression shape = null;


    public void setShape(javax.el.ValueExpression shape) {
        this.shape = shape;
    }


    private javax.el.ValueExpression styleClass = null;


    public void setStyleClass(javax.el.ValueExpression styleClass) {
        this.styleClass = styleClass;
    }


    private javax.el.ValueExpression value = null;


    public void setValue(javax.el.ValueExpression newValue) {
        value = newValue;
    }


    public String getComponentType() {
        return ("DemoArea");
    }


    public String getRendererType() {
        return ("DemoArea");
    }


    public void release() {
        super.release();
        this.alt = null;
        this.coords = null;
        this.onmouseout = null;
        this.onmouseover = null;
        this.shape = null;
        this.styleClass = null;
        this.value = null;
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        AreaComponent area = (AreaComponent) component;
        if (alt != null) {
            if (!alt.isLiteralText()) {
                area.setValueExpression("alt", alt);
            } else {
                area.setAlt(alt.getExpressionString());
            }
        }
        if (coords != null) {
            if (!coords.isLiteralText()) {
                area.setValueExpression("coords", coords);
            } else {
                area.setCoords(coords.getExpressionString());
            }
        }
        if (onmouseout != null) {
            if (!onmouseout.isLiteralText()) {
                area.setValueExpression("onmouseout", onmouseout);
            } else {
                area.getAttributes().put("onmouseout", onmouseout.getExpressionString());
            }
        }
        if (onmouseover != null) {
            if (!onmouseover.isLiteralText()) {
                area.setValueExpression("onmouseover", onmouseover);
            } else {
                area.getAttributes().put("onmouseover", onmouseover.getExpressionString());
           }
        }
        if (shape != null) {
            if (!shape.isLiteralText()) {
                area.setValueExpression("shape", shape);
            } else {
                area.setShape(shape.getExpressionString());
            }
        }
        if (styleClass != null) {
            if (!styleClass.isLiteralText()) {
                area.setValueExpression("styleClass", styleClass);
            } else {
                area.getAttributes().put("styleClass", styleClass.getExpressionString());
            }
        }
        if (area instanceof ValueHolder) {
            ValueHolder valueHolder = (ValueHolder) component;
            if (value != null) {
                if (!value.isLiteralText()) {
                    area.setValueExpression("value", value);
                } else {
                    valueHolder.setValue(value.getExpressionString());
                }
            }
        }
        // target image is required
        area.setValueExpression("targetImage", targetImage);
    }
}
