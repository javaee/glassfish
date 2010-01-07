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

import javax.faces.component.UIComponent;
import com.sun.javaee.blueprints.components.ui.components.AjaxValidatorComponent;
import javax.faces.webapp.UIComponentELTag;
import javax.el.*;

/**
 * <p>Expose the AjaxValidator component to the JSP page author.</p>
 *
 * @author edburns
 */
public class AjaxValidatorTag extends UIComponentELTag {
    
    /** Creates a new instance of AjaxValidatorTag */
    public AjaxValidatorTag() {
    }
    
    public String getRendererType() { return null; }
    
    public String getComponentType() { return "AjaxValidator"; }
 
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        AjaxValidatorComponent ajaxComponent = (AjaxValidatorComponent) component;
	
        // A truly robust implementation would value expression enable these
        // attribute.
        if (null != messageId) {
	    if (!messageId.isLiteralText()) {
		    ajaxComponent.setValueExpression("messageId", messageId);
	    } else {
		    ajaxComponent.setMessageId(messageId.getExpressionString());
	    }
        }
        if (null != eventHook) {
	    if (!eventHook.isLiteralText()) {
		    ajaxComponent.setValueExpression("eventHook", eventHook);
	    } else {
		    ajaxComponent.setEventHook(eventHook.getExpressionString());
	    }
        }
    }

    private ValueExpression messageId = null;
    /**
     * Setter for property messageId.
     * @param messageId New value of property messageId.
     */
    public void setMessageId(ValueExpression messageId) {
        this.messageId = messageId;
    }

    /**
     * Holds value of property eventHook.
     */
    private ValueExpression eventHook;

    /**
     * Getter for property eventHook.
     * @return Value of property eventHook.
     */
    public ValueExpression getEventHook() {

        return this.eventHook;
    }

    /**
     * Setter for property eventHook.
     * @param eventHook New value of property eventHook.
     */
    public void setEventHook(ValueExpression eventHook) {

        this.eventHook = eventHook;
    }
    
}
