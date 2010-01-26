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

import com.sun.javaee.blueprints.components.ui.components.ChartItemComponent;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

/**
 * <p><strong>ChartItemTag</strong> is the tag handler that processes the 
 * <code>chartItem</code> custom tag.</p>
 */

public class ChartItemTag extends UIComponentTag {

    public ChartItemTag() {
        super();
    }

    //
    // Class methods
    //

    // 
    // Accessors
    //

    /**
     * <p>The label for this item</p>
     */
    private ValueExpression itemLabel = null;
    /**
     *<p>Set the label for this item.
     */
    public void setItemLabel(ValueExpression label) {
        this.itemLabel = label;
    }

    /**
     * <p>The color for this item.</p>
     */
    private ValueExpression itemColor = null;
    /**
     *<p>Set the color for this item.
     */
    public void setItemColor(ValueExpression color) {
        this.itemColor = color;
    }
    
    /**
     * <p>The value for this item.</p>
     */
    private ValueExpression itemValue = null;
    /**
     *<p>Set the ualue for this item.
     */
    public void setItemValue(ValueExpression itemVal) {
        this.itemValue = itemVal;
    }

    private ValueExpression value = null;
    public void setValue(ValueExpression value) {
        this.value = value;
    }

    //
    // General Methods
    //

    /**
     * <p>Return the type of the component.
     */
    public String getComponentType() {
        return "ChartItem";
    }

    /**
     * <p>Return the renderer type (if any)
     */
    public String getRendererType() {
        return null;
    }

    /**
     * <p>Release any resources used by this tag handler
     */
    public void release() {
        super.release();
        itemLabel = null;
        itemValue = null;
        itemColor = null;
    }

    //
    // Methods from BaseComponentTag
    //

    /**
     * <p>Set the component properties
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        ChartItemComponent chartItem = (ChartItemComponent) component;

        if (null != value) {
            if (!value.isLiteralText()) {
                chartItem.setValueExpression("value", value);
            } else {
                chartItem.setValue(value.getExpressionString());
            }
        }

        if (null != itemLabel) {
            if (!itemLabel.isLiteralText()) {
                chartItem.setValueExpression("itemLabel", itemLabel);
            } else {
                chartItem.setItemLabel(itemLabel.getExpressionString());
            }
        }
        
        if (null != itemColor) {
            if (!itemColor.isLiteralText()) {
                chartItem.setValueExpression("itemColor", itemColor);
            } else {
                chartItem.setItemColor(itemColor.getExpressionString());
            }
        }
        
        if (null != itemValue) {
            if (!itemValue.isLiteralText()) {
                chartItem.setValueExpression("itemValue", itemValue);
            } else {
                chartItem.setItemValue(itemValue.getExpressionString());
            }
        }
    }

}
