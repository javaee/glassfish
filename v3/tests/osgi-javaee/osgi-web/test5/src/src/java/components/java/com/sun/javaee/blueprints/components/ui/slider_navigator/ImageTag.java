/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
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
