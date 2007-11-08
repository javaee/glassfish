/*
 * $Id: AttributeBean.java,v 1.1 2005/09/20 21:11:24 edburns Exp $
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


/**
 * <p>Configuration bean for <code>&lt;attribute&gt; element.</p>
 */

public class AttributeBean extends FeatureBean {


    // -------------------------------------------------------------- Properties


    private String attributeClass;
    public String getAttributeClass() { return attributeClass; }
    public void setAttributeClass(String attributeClass)
    { this.attributeClass = attributeClass; }


    private String attributeName;
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName)
    { this.attributeName = attributeName; }


    private String suggestedValue;
    public String getSuggestedValue() { return suggestedValue; }
    public void setSuggestedValue(String suggestedValue)
    { this.suggestedValue = suggestedValue; }


    // -------------------------------------------------------------- Extensions


    // defaultValue == Non-standard default value (if any)
    private String defaultValue = null;
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue)
    { this.defaultValue = defaultValue; }

    // passThrough == HTML attribute that passes through [default=false]
    private boolean passThrough = false;
    public boolean isPassThrough() { return passThrough; }
    public void setPassThrough(boolean passThrough)
    { this.passThrough = passThrough; }


    // required == in TLD <attribute>, set required to true [default=false]
    private boolean required = false;
    public boolean isRequired() { return required; }
    public void setRequired(boolean required)
    { this.required = required; }


    // tagAttribute == Generate TLD attribute [default=true]
    private boolean tagAttribute = true;
    public boolean isTagAttribute() { return tagAttribute; }
    public void setTagAttribute(boolean tagAttribute)
    { this.tagAttribute = tagAttribute; }


    // ----------------------------------------------------------------- Methods


}
