/*
 * $Id: ComponentBean.java,v 1.1 2005/09/20 21:11:24 edburns Exp $
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

package com.sun.faces.config.beans;


import java.util.Map;
import java.util.TreeMap;


/**
 * <p>Configuration bean for <code>&lt;attribute&gt; element.</p>
 */

public class ComponentBean extends FeatureBean
 implements AttributeHolder, PropertyHolder {


    // -------------------------------------------------------------- Properties


    private String componentClass;
    public String getComponentClass() { return componentClass; }
    public void setComponentClass(String componentClass)
    { this.componentClass = componentClass; }


    private String componentType;
    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType)
    { this.componentType = componentType; }


    // -------------------------------------------------------------- Extensions


    // baseComponentType == componentType of base class for this component class
    // (extends UIComponentBase if not specified)
    private String baseComponentType;
    public String getBaseComponentType() { return baseComponentType; }
    public void setBaseComponentType(String baseComponentType)
    { this.baseComponentType = baseComponentType; }


    // componentFamily == default componentFamily for this component class
    // (inherited from baseComponentType if not specified)
    private String componentFamily;
    public String getComponentFamily() { return componentFamily; }
    public void setComponentFamily(String componentFamily)
    { this.componentFamily = componentFamily; }


    // rendererType == default rendererType for this component class
    // (set to null if not specified)
    private String rendererType;
    public String getRendererType() { return rendererType; }
    public void setRendererType(String rendererType)
    { this.rendererType = rendererType; }


    // ------------------------------------------------- AttributeHolder Methods


    private Map<String,AttributeBean> attributes = new TreeMap<String, AttributeBean>();


    public void addAttribute(AttributeBean descriptor) {
        attributes.put(descriptor.getAttributeName(), descriptor);
    }


    public AttributeBean getAttribute(String name) {
        return (attributes.get(name));
    }


    public AttributeBean[] getAttributes() {
        AttributeBean results[] = new AttributeBean[attributes.size()];
        return (attributes.values().toArray(results));
    }


    public void removeAttribute(AttributeBean descriptor) {
        attributes.remove(descriptor.getAttributeName());
    }


    // ------------------------------------------------- PropertyHolder Methods


    private Map<String,PropertyBean> properties = new TreeMap<String, PropertyBean>();


    public void addProperty(PropertyBean descriptor) {
        properties.put(descriptor.getPropertyName(), descriptor);
    }


    public PropertyBean getProperty(String name) {
        return (properties.get(name));
    }


    public PropertyBean[] getProperties() {
        PropertyBean results[] = new PropertyBean[properties.size()];
        return (properties.values().toArray(results));
    }


    public void removeProperty(PropertyBean descriptor) {
        properties.remove(descriptor.getPropertyName());
    }


    // ----------------------------------------------------------------- Methods


}
