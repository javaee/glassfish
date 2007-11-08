/*
 * $Id: ChartItem.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.model;

/**
 * This class represents an individual graphable item for the chart conmponent.
 */
public class ChartItem {

    public ChartItem() {
       super();
    }
    
    public ChartItem(String label, int value, String color) {
        setLabel(label);
	setValue(value);
	setColor(color);
    }

    /**
     * <p>The label for this item.</p>
     */
    private String label = null;
    /**
     *<p>Return the label for this item.</p>
     */
    public String getLabel() {
        return label;
    }
    /**
     * <p>Set the label for this item.</p>
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * <p>The value for this item.</p>
     */
    private int value = 0;
    /**
     *<p>Return the value for this item.</p>
     */
    public int getValue() {
        return value;
    }
    /**
     * <p>Set the value for this item.</p>
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * <p>The color for this item.</p>
     */
    private String color = null;
    /**
     *<p>Return the color for this item.</p>
     */
    public String getColor() {
        return color;
    }
    /**
     * <p>Set the color for this item.</p>
     */
    public void setColor(String color) {
        this.color = color;
    }
}
