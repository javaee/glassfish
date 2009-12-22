/*
 * AjaxValidatorTag.java
 *
 * Created on May 31, 2005, 9:13 PM
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
