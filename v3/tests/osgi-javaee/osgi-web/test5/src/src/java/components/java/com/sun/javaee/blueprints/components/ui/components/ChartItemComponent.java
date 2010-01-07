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

package com.sun.javaee.blueprints.components.ui.components;


import javax.el.ValueExpression;
import java.io.IOException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponentBase;


/**
 * <p><strong>ChartItemComponent</strong> is a component that may be nested
 * inside a <code>ChartItem</code>, and causes the addition of a 
 * <code>ChartItem</code> instance to the list of available options for the 
 * parent component.  The contents of the
 * <code>ChartItem</code> can be specified in one of the following ways:</p>
 * <ul>
 * <li>The <code>itemValue</code> attribute's value is an instance of
 *     <code>ChartItem</code>.</li>
 * <li>The associated {@link ValueExpression} points at a model data
 *     item of type <code>ChartItem</code>.</li>
 * <li>A new <code>ChartItem</code> instance is synthesized from the values
 *     of the <code>itemLabel</code>, <code>itemColor</code>, 
 * <code>itemValue</code></li>
 * </ul>
 */

public class ChartItemComponent extends UIComponentBase {


    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE = "ChartItem";


    /**
     * <p>The standard component family for this component.</p>
     */
    public static final String COMPONENT_FAMILY = "ChartItem";


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Create a new <code>ChartItem</code> instance with default property
     * values.</p>
     */
    public ChartItemComponent() {

        super();
        setRendererType(null);

    }


    // ------------------------------------------------------ Instance Variables


    private String itemLabel = null;
    private String itemColor = null;
    private Object itemValue = null;
    private Object value = null;


    // -------------------------------------------------------------- Properties


    public String getFamily() {

        return (COMPONENT_FAMILY);

    }


    /**
     * <p>Return the label for this chart item.
     */
    public String getItemLabel() {

	if (this.itemLabel != null) {
	    return (this.itemLabel);
	}
	ValueExpression _ve = getValueExpression("itemLabel");
	if (_ve != null) {
	    return ((String) _ve.getValue(getFacesContext().getELContext()));
	} else {
	    return (null);
	}

    }


    /**
     * <p>Set the label for this chart item.
     *
     * @param label The new label
     */
    public void setItemLabel(String label) {

        this.itemLabel = label;

    }

    /**
     * <p>Return the color for this chart item.
     */
    public String getItemColor() {

	if (this.itemColor != null) {
	    return (this.itemColor);
	}
	ValueExpression _ve = getValueExpression("itemColor");
	if (_ve != null) {
	    return ((String) _ve.getValue(getFacesContext().getELContext()));
	} else {
	    return (null);
	}

    }


    /**
     * <p>Set the color for this chart item.
     *
     * @param color The new color
     */
    public void setItemColor(String color) {

        this.itemColor = color;

    }
    
    /**
     * <p>Return the server value for this selection item.
     */
    public Object getItemValue() {

	if (this.itemValue != null) {
	    return (this.itemValue);
	}
	ValueExpression _ve = getValueExpression("itemValue");
	if (_ve != null) {
	    return ((Integer) (_ve.getValue(getFacesContext().getELContext())));
	} else {
	    return null;
	}
    }
    
    /**
     * <p>Set the server value for this selection item.
     *
     * @param itemValue The new server value
     */
    public void setItemValue(Object itemValue) {

        this.itemValue = itemValue;

    }

    /**
     * <p>Returns the <code>value</code> of this chart item
     */
    public Object getValue() {

	if (this.value != null) {
	    return (this.value);
	}
	ValueExpression _ve = getValueExpression("value");
	if (_ve != null) {
	    return (_ve.getValue(getFacesContext().getELContext()));
	} else {
	    return (null);
	}
    }


    /**
     * <p>Sets the <code>value</code> property of this chart item
     * 
     * @param value the new value
     */
    public void setValue(Object value) {

        this.value = value;

    }


    // ----------------------------------------------------- StateHolder Methods
    public Object saveState(FacesContext context) {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = itemLabel;
        values[2] = itemColor;
        values[3] = itemValue;
        values[4] = value;
        return (values);
    }

    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        itemLabel = (String) values[1];
        itemColor = (String) values[2];
        itemValue = values[3];
        value = values[4];
    }
}
