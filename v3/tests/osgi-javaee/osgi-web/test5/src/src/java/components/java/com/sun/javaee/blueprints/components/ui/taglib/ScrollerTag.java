/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: ScrollerTag.java,v 1.3 2005/11/01 21:59:12 jenniferb Exp $ */

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
