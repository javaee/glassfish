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

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.webapp.UIComponentELTag;
import javax.el.*;

/**
 * <p>DataRepeaterTag is the tag handler class for a <code>UIData</code>
 * component associated with a <code>RepeaterRenderer</code>.</p>
 */

public class DataRepeaterTag extends UIComponentELTag {


    // -------------------------------------------------------------- Attributes


    private ValueExpression first = null;


    public void setFirst(ValueExpression first) {
        this.first = first;
    }


    private ValueExpression rows = null;


    public void setRows(ValueExpression rows) {
        this.rows = rows;
    }


    private ValueExpression styleClass = null;


    public void setStyleClass(ValueExpression styleClass) {
        this.styleClass = styleClass;
    }


    private ValueExpression value = null;


    public void setValue(ValueExpression value) {
        this.value = value;
    }


    private ValueExpression var = null;


    public void setVar(ValueExpression var) {
        this.var = var;
    }


    // -------------------------------------------------- UIComponentTag Methods


    public String getComponentType() {
        return ("javax.faces.Data");
    }


    public String getRendererType() {
        return ("Repeater");
    }


    public void release() {
        super.release();
        first = null;
        rows = null;
        styleClass = null;
        value = null;
        var = null;
    }


    protected void setProperties(UIComponent component) {

        super.setProperties(component);
	UIData repeater = (UIData) component;
	
        if (first != null) {
            if (!first.isLiteralText()) {
		    repeater.setValueExpression("first", first);
            } else {
		    repeater.setFirst(new Integer(first.getExpressionString()));
            }
        }

        if (rows != null) {
            if (!rows.isLiteralText()) {
                repeater.setValueExpression("rows", rows);
            } else {
                repeater.setRows(new Integer(rows.getExpressionString()));
            }
        }

        if (styleClass != null) {
            if (!styleClass.isLiteralText()) {
                repeater.setValueExpression("styleClass", styleClass);
            } else {
                repeater.getAttributes().put("styleClass", styleClass.getExpressionString());
            }
        }

       if (value != null) {
            if (!value.isLiteralText()) {
                repeater.setValueExpression("value", value);
            } else {
                repeater.setValue(value.getExpressionString());
            }
        }

        if (var != null) {
             if (!var.isLiteralText()) {
                repeater.setValueExpression("var", var);
            } else {
                repeater.setVar(var.getExpressionString());
            }
        }

    }


}
