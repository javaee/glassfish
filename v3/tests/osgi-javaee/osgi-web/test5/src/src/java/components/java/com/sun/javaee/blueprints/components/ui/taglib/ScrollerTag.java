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

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.webapp.UIComponentELTag;

import java.util.Map;

import com.sun.javaee.blueprints.components.ui.components.ScrollerComponent;

/**
 * <p> ScrollerTag is the tag handler class for <code>ScrollerComponent.
 */
public class ScrollerTag extends UIComponentELTag {

    protected MethodExpression actionListener = null;
    protected ValueExpression navFacetOrientation = null;
    protected ValueExpression forValue = null;


    /**
     * <code>MethodExpression</code> to handle an action event generated as 
     * a result of clicking on a link that points a particular page in the 
     * result-set.
     */
    public void setActionListener(MethodExpression actionListener) {
        this.actionListener = actionListener;
    }


    /*
     * When rendering a widget representing "page navigation" where
     * should the facet markup be rendered in relation to the page
     * navigation widget?  Values are "NORTH", "SOUTH", "EAST", "WEST".
     * Case insensitive. 
     */
    public void setNavFacetOrientation(ValueExpression navFacetOrientation) {
        this.navFacetOrientation = navFacetOrientation;
    }


    /*
     * The data grid component for which this acts as a scroller.     
     */
    public void setFor(ValueExpression forValue) {
        this.forValue = forValue;
    }


    public String getComponentType() {
        return ("Scroller");
    }


    public String getRendererType() {
        return (null);
    }

    @Override
    public void release() {
        super.release();
        navFacetOrientation = null;
        actionListener = null;
        forValue = null;
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);        
        ScrollerComponent scroller = (ScrollerComponent) component;
        Map<String,Object> attributes = scroller.getAttributes();

        if (actionListener != null) {
            scroller.addActionListener(
                  new MethodExpressionActionListener((actionListener)));
        }        

        // if the attributes are values set them directly on the component, if
        // not set the ValueExpression reference so that the expressions can be
        // evaluated lazily.
        if (navFacetOrientation != null) {
            if (!navFacetOrientation.isLiteralText()) {
                scroller.setValueExpression("navFacetOrientation",
                                            navFacetOrientation);
            } else {
                attributes.put("navFacetOrientation",
                               navFacetOrientation.getExpressionString());
            }
        }

        if (forValue != null) {
            if (!forValue.isLiteralText()) {
                scroller.setValueExpression("for", forValue);
            } else {
                attributes.put("for", forValue.getExpressionString());
            }
        }       
    }
}
