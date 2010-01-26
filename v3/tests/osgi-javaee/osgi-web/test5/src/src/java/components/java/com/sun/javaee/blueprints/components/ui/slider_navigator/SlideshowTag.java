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
