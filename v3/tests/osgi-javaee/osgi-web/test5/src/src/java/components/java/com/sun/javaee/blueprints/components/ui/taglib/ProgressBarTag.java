/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
