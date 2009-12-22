/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: DataRepeaterTag.java,v 1.3 2005/11/01 21:59:11 jenniferb Exp $ */

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
