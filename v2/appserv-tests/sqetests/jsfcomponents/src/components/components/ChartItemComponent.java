/*
 * $Id: ChartItemComponent.java,v 1.4 2004/11/14 07:33:12 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.components;


import java.io.IOException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
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
 * <li>The associated {@link ValueBinding} points at a model data
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
	ValueBinding vb = getValueBinding("itemLabel");
	if (vb != null) {
	    return ((String) vb.getValue(getFacesContext()));
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
	ValueBinding vb = getValueBinding("itemColor");
	if (vb != null) {
	    return ((String) vb.getValue(getFacesContext()));
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
	ValueBinding vb = getValueBinding("itemValue");
	if (vb != null) {
	    return ((Integer) (vb.getValue(getFacesContext())));
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
	ValueBinding vb = getValueBinding("value");
	if (vb != null) {
	    return (vb.getValue(getFacesContext()));
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
