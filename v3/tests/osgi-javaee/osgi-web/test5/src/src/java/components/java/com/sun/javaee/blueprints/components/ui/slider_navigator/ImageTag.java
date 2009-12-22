/* Copyright 2005 Sun Microsystems, Inc. All rights reserved.
   You may not modify, use, reproduce, or distribute this software except in
   compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
   $Id: ImageTag.java,v 1.1 2005/11/19 00:53:49 inder Exp $  */

package com.sun.javaee.blueprints.components.ui.slider_navigator;

import javax.faces.component.*;
import javax.faces.webapp.UIComponentELTag;
import javax.el.ValueExpression;

public class ImageTag extends UIComponentELTag {

    private ValueExpression link;
    private ValueExpression src;
    private ValueExpression name;

    public String getComponentType() {
        return "bpcatalog.ajax.sliderImage";
    }

    public String getRendererType() {
        return "SliderImageRenderer";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        Image ss = (Image)component;
        
        if (link != null) {
            if (!link.isLiteralText()) {
                component.setValueExpression("link", link);
            } else {
                ss.getAttributes().put("link", link.getExpressionString());
            }
        }
        if (src != null) {
            if (!src.isLiteralText()) {
                component.setValueExpression("src", src);
            } else {
                ss.getAttributes().put("src", src.getExpressionString());
            }
        }
        if (name != null) {
            if (!name.isLiteralText()) {
                component.setValueExpression("name", name);
            } else {
                ss.getAttributes().put("name", name.getExpressionString());
            }
        }
    }

    public ValueExpression getLink() {
        return link;
    }
    public void setLink(ValueExpression l) {
        this.link = l;
    }
    
    public ValueExpression getSrc() {
        return src;
    }
    public void setSrc(ValueExpression s) {
        this.src = s;
    }
    public ValueExpression getName() {
        return name;
    }
    public void setName(ValueExpression s) {
        this.name = s;
    }
    
    public void release() {
        link = null;
        src = null;
        name = null;
        super.release();
    }
}
