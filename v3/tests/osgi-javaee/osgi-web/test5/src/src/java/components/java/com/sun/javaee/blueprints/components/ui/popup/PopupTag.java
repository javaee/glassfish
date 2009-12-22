/*
 * AJAXPopupTag.java
 *
 * Created on September 26, 2005, 9:24 AM
 */

package com.sun.javaee.blueprints.components.ui.popup;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.webapp.UIComponentTag;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.FactoryFinder;

/**
 * This tag is the glue between the JSF tag and the renderer and JSF Component
 *
 * @author  basler
 */

public class PopupTag extends UIComponentTag {
    
    private String id=null, xmlHttpRequestURL=null, elementNamePairs=null;
    
    public String getComponentType() {
        return ("javax.faces.Output");
    }
    
    public String getRendererType() {
        return ("Popup");
    }

    public void setId(String sxId) {
        id=sxId;
    }
    public String getId() {
        return id;
    }

    public void setXmlHttpRequestURL(String sxURL) {
        xmlHttpRequestURL=sxURL;
    }
    public String getXmlHttpRequestURL() {
        return xmlHttpRequestURL;
    }

    public void setElementNamePairs(String sxNames) {
        elementNamePairs=sxNames;
    }
    public String getElementNamePairs() {
        return elementNamePairs;
    }


    /**
     * This method takes the properties from the JSP and populates the UIOutput components properties
     * for use in the PopupRenderer
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UIOutput outComp = (UIOutput) component;
        
        // pull out id attribute
        if (id != null) {
            if (isValueReference(id)) {
                outComp.setValueBinding("id", getValueBinding(id));
            } else {
                outComp.getAttributes().put("id", id);
            }
        }

        // pull out xmlHttpRequestURL attribute
        if (xmlHttpRequestURL != null) {
            if (isValueReference(xmlHttpRequestURL)) {
                outComp.setValueBinding("xmlHttpRequestURL", getValueBinding(xmlHttpRequestURL));
            } else {
                outComp.getAttributes().put("xmlHttpRequestURL", xmlHttpRequestURL);
            }
        }

        // pull out elementNamePairs attribute
        if (elementNamePairs != null) {
            if (isValueReference(elementNamePairs)) {
                outComp.setValueBinding("elementNamePairs", getValueBinding(elementNamePairs));
            } else {
                outComp.getAttributes().put("elementNamePairs", elementNamePairs);
            }
        }
    }
    
    
    private ValueBinding getValueBinding(String valueRef) {
        ApplicationFactory af=(ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application a = af.getApplication();
        return (a.createValueBinding(valueRef));
    }
    
}
