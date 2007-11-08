/*
 * $Id: RendererBean.java,v 1.1 2005/09/20 21:11:29 edburns Exp $
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
 * <p>Configuration bean for <code>&lt;renderer&gt; element.</p>
 */

public class RendererBean extends FeatureBean implements AttributeHolder {


    // -------------------------------------------------------------- Properties


    private String componentFamily;
    public String getComponentFamily() { return componentFamily; }
    public void setComponentFamily(String componentFamily)
    { this.componentFamily = componentFamily; }


    private String rendererClass;
    public String getRendererClass() { return rendererClass; }
    public void setRendererClass(String rendererClass)
    { this.rendererClass = rendererClass; }


    private String rendererType;
    public String getRendererType() { return rendererType; }
    public void setRendererType(String rendererType)
    { this.rendererType = rendererType; }


    // -------------------------------------------------------------- Extensions


    // true == this Renderer returns true for getRendersChildren()
    private boolean rendersChildren = false;
    public boolean isRendersChildren() { return rendersChildren; }
    public void setRendersChildren(boolean rendersChildren)
    { this.rendersChildren = rendersChildren; }

    private String excludeAttributes;
    public String getExcludeAttributes() {
        return excludeAttributes;
    }
    public void setExcludeAttributes(String newExcludeAttributes) {
        excludeAttributes = newExcludeAttributes;
    }

    // true if the tag handler for this renderer should be a BodyTag
    // [default=false]
    public boolean bodyTag = false;
    public boolean isBodyTag() { return bodyTag; }
    public void setBodyTag(boolean bodyTag) { this.bodyTag = bodyTag; }

    // Tag name (if it doesn't follow the standard convention)
    private String tagName;
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }


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


    // ----------------------------------------------------------------- Methods


}
