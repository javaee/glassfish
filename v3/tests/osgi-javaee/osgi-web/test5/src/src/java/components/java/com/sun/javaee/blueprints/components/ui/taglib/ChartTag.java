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

package com.sun.javaee.blueprints.components.ui.taglib;

import com.sun.javaee.blueprints.components.ui.components.ChartComponent;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;


/**
 * <p><strong>ChartTag</strong> is the tag handler that processes the 
 * <code>chart</code> custom tag.</p>
 */

public class ChartTag extends UIComponentTag {

    /**
     * <p>The width of the chart
     */
    private ValueExpression width = null;
    /**
     * <p>Set the width of the chart
     */
    public void setWidth(ValueExpression width) {
        this.width = width;
    }

    /**
     * <p>The height of the chart
     */
    private ValueExpression height = null;
    /**
     * <p>Set the height of the chart
     */
    public void setHeight(ValueExpression height) {
        this.height = height;
    }

    /**
     * <p>The layout of the chart.  This attribute is applicable to bar
     * charts, and the value can be "horizontal" or "vertical".</p>
     */
    private ValueExpression orientation = null;
    /**
     * <p>Set the orientation of the chart
     */
    public void setOrientation(ValueExpression orientation) {
        this.orientation = orientation;
    }
    
    private ValueExpression value = null;
    public void setValue(ValueExpression value) {
        this.value = value;
    }
    
    /**
     * <p>The type of chart.  Values can be "bar" or "pie".
     */
    private ValueExpression type = null;
    /**
     * <p>Set the type of the chart
     */
    public void setType(ValueExpression type) {
        this.type = type;
    }

    /**
     * <p>The title of the chart
     */
    private ValueExpression title = null;
    /**
     * <p>Set the title of the chart
     */
    public void setTitle(ValueExpression title) {
        this.title = title;
    }

    /**
     * <p>The label for the x-axis of the bar chart
     */
    private ValueExpression xlabel = null;
    /**
     * <p>Set the x-axis label for the bar chart
     */
    public void setXlabel(ValueExpression xlabel) {
        this.xlabel = xlabel;
    }

    /**
     * <p>The label for the y-axis of the bar chart
     */
    private ValueExpression ylabel = null;
    /**
     * <p>Set the y-axis label for the bar chart
     */
    public void setYlabel(ValueExpression ylabel) {
        this.ylabel = ylabel;
    }

    /**
     * <p>Return the type of the component.
     */
    public String getComponentType() {
        return ("Chart");
    }


    /**
     * <p>Return the renderer type (if any)
     */
    public String getRendererType() {
        return (null);
    }


    /**
     * <p>Release any resources used by this tag handler
     */
    public void release() {
        super.release();
        width = null;
        height = null;
        orientation = null;
        title = null;
        xlabel = null;
        ylabel = null;
        type = null;
    }


    /**
     * <p>Set the component properties
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        ChartComponent chart = (ChartComponent) component;
       
        if (width != null) {
            if (!width.isLiteralText()) {
                chart.setValueExpression("width", width);
            } else {
                chart.setWidth(width.getExpressionString());
            }
        }
        
        if (height != null) {
            if (!height.isLiteralText()) {
                chart.setValueExpression("height", height);
            } else {
                chart.setHeight(height.getExpressionString());
            }
        }
        
        if (orientation != null) {
            if (!orientation.isLiteralText()) {
                chart.setValueExpression("orientation", orientation);
            } else {
                chart.setOrientation(orientation.getExpressionString());
            }
        }
        
        if (type != null) {
            if (!type.isLiteralText()) {
                chart.setValueExpression("type", type);
            } else {
                chart.setType(type.getExpressionString());
            }
        }
        
        if (value != null) {
            if (!value.isLiteralText()) {
                chart.setValueExpression("value", value);
            } else {
                chart.setValue(value.getExpressionString());
            }
        }
        
        if (title != null) {
            if (!title.isLiteralText()) {
                chart.setValueExpression("title", title);
            } else {
                chart.setTitle(title.getExpressionString());
            }
        }
        
        if (xlabel != null) {
            if (!xlabel.isLiteralText()) {
                chart.setValueExpression("xlabel", xlabel);
            } else {
                chart.setXlabel(xlabel.getExpressionString());
            }
        }
        
        if (ylabel != null) {
            if (!ylabel.isLiteralText()) {
                chart.setValueExpression("ylabel", ylabel);
            } else {
                chart.setYlabel(ylabel.getExpressionString());
            }
        }
    }
}
