/*
 * $Id: ProgressBarTag.java,v 1.2 2005/11/01 21:59:12 jenniferb Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * [Name of File] [ver.__] [Date]
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.javaee.blueprints.components.ui.taglib;

import com.sun.javaee.blueprints.components.ui.components.ProgressBarComponent;
import com.sun.javaee.blueprints.components.ui.util.Util;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.el.ValueExpression;
import javax.el.MethodExpression;
import javax.faces.webapp.UIComponentELTag;


/**
 * <p><strong>ProgressBarTag</strong> is the tag handler that processes the 
 * <code>chart</code> custom tag.</p>
 */

public class ProgressBarTag extends UIComponentELTag {


    protected ValueExpression value;

    public void setValue(ValueExpression newValue) {
	value = newValue;
    }

    private ValueExpression interval;

    public void setInterval(ValueExpression newInterval) {
	interval = newInterval;
    }


    /**
     * <p>Return the type of the component.
     */
    public String getComponentType() {
        return ("ProgressBar");
    }


    /**
     * <p>Return the renderer type (if any)
     */
    public String getRendererType() {
        return ("ProgressBar");
    }


    /**
     * <p>Release any resources used by this tag handler
     */
    public void release() {
        super.release();
	value = null;
	interval = null;
    }


    /**
     * <p>Set the component properties
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        ProgressBarComponent progressBar = (ProgressBarComponent) component;
       
        if (interval != null) {
            if (!interval.isLiteralText()) {
                progressBar.setValueExpression("interval", interval);
            } else {
		Integer timeInterval = new Integer(interval.getExpressionString());
		try {
		    int millis = timeInterval.intValue();
		    progressBar.setInterval(millis);
		}
		catch (NumberFormatException e) {
		}
            }
        }
        
       if (value != null) {
            if (!value.isLiteralText()) {
                progressBar.setValueExpression("value", value);
            } else {
                progressBar.setValue(value.getExpressionString());
            }
        }

        if (action != null) {
                progressBar.setActionExpression(action);
        }

        
    }

    /**
     * Holds value of property action.
     */
    private javax.el.MethodExpression action;

    /**
     * Getter for property action.
     * @return Value of property action.
     */
    public javax.el.MethodExpression getAction() {

        return this.action;
    }

    /**
     * Setter for property action.
     * @param action New value of property action.
     */
    public void setAction(javax.el.MethodExpression action) {

        this.action = action;
    }
}