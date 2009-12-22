/* Copyright 2005 Sun Microsystems, Inc. All rights reserved.
   You may not modify, use, reproduce, or distribute this software except in
   compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
   $Id: SlideshowTag.java,v 1.1 2005/11/19 00:53:51 inder Exp $  */

package com.sun.javaee.blueprints.components.ui.slider_navigator;

import javax.faces.component.*;
import javax.faces.webapp.UIComponentELTag;
import javax.el.ValueExpression;

public class SlideshowTag extends UIComponentELTag {

    private ValueExpression left;
    private ValueExpression top;
    private ValueExpression width;
    private ValueExpression height;
    private ValueExpression pause;
    private ValueExpression speed;
    private ValueExpression data;

    public String getComponentType() {
        return "bpcatalog.ajax.sliderNavigator";
    }

    public String getRendererType() {
        return "SliderNavigatorRenderer";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        Slideshow ss = (Slideshow)component;
        
        if (left != null) {
            if (!left.isLiteralText()) {
                component.setValueExpression("left", left);
            } else {
                ss.getAttributes().put("left", left.getExpressionString());
            }
        }
        if (top != null) {
            if (!top.isLiteralText()) {
                component.setValueExpression("top", top);
            } else {
                ss.getAttributes().put("top", top.getExpressionString());
            }
        }
        if (width != null) {
            if (!width.isLiteralText()) {
                component.setValueExpression("width", width);
            } else {
                ss.getAttributes().put("width", width.getExpressionString());
            }
        }
        if (height != null) {
            if (!height.isLiteralText()) {
                component.setValueExpression("height", height);
            } else {
                ss.getAttributes().put("height", height.getExpressionString());
            }
        }
        if (data != null) {
            if (!data.isLiteralText()) {
                component.setValueExpression("data", data);
            } else {
                ss.getAttributes().put("data", data.getExpressionString());
            }
        }
        if (pause != null) {
            if (!pause.isLiteralText()) {
                component.setValueExpression("pause", pause);
            } else {
                ss.getAttributes().put("pause", pause.getExpressionString());
            }
        }
        if (speed != null) {
            if (!speed.isLiteralText()) {
                component.setValueExpression("speed", speed);
            } else {
                ss.getAttributes().put("speed", speed.getExpressionString());
            }
        }
    }

    public ValueExpression getLeft() {
        return left;
    }
    public void setLeft(ValueExpression left) {
        this.left = left;
    }
    
    public ValueExpression getTop() {
        return top;
    }
    public void setTop(ValueExpression top) {
        this.top = top;
    }
    
    public ValueExpression getWidth() {
        return width;
    }
    public void setWidth(ValueExpression w) {
        this.width = w;
    }
    
    public ValueExpression getHeight() {
        return height;
    }
    public void setHeight(ValueExpression h) {
        this.height = h;
    }
    
    public ValueExpression getPause() {
        return pause;
    }
    public void setPause(ValueExpression p) {
        this.pause = p;
    }
    
    public ValueExpression getSpeed() {
        return speed;
    }
    public void setSpeed(ValueExpression s) {
        this.speed = s;
    }
    public ValueExpression getData() {
        return data;
    }
    public void setData(ValueExpression d) {
        this.data = d;
    }
    
    public void release() {
        left = null;
        top = null;
        width = null;
        height = null;
        pause = null;
        speed = null;
        data = null;
        super.release();
    }
}
