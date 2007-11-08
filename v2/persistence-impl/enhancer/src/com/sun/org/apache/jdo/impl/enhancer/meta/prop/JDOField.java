/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer.meta.prop;

import java.lang.reflect.Modifier;


/**
 * A class to hold the properties of a field.
 */
final class JDOField
{
    /**
     * The name of this field.
     */
    final private String name;
    
    /**
     * The type of this field.
     */
    private String type;
    
    /**
     * The access modifier of this field.
     */
    private int access = Modifier.PRIVATE;
    
    /**
     * The JDO modifier of this field.
     */
    private String jdoModifier;
    
    /**
     * The annotation type of this field.
     */
    private String annotation;
    
    /**
     * Creates a new object with the given name.
     * @param name  the name of this field
     */
    JDOField(String name)
    {
        this.name = name;
    }
    
    /**
     * Returns the name of this field.
     * @return  the name of this field
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Sets the type of this field. The given classname should have a
     * natural form(with dots) and is converted to a VM-similar
     * notation(with slashes).
     * @param type  the natural classname
     */
    public void setType(String type)
    {
        this.type = NameHelper.fromCanonicalClassName(type);
    }
    
    /**
     * Returns the type of this field.
     * @return  the type of this field
     */
    public String getType()
    {
        return type;
    }
    
    /**
     * Returns the access modifier of this field.
     * @param access  the access modifier of this field
     */
    public void setAccessModifiers(int access)
    {
        this.access = access;
    }
    
    /**
     * Returns the access modifier of this field.
     * @return  the access modifier of this field
     */
    public int getAccessModifiers()
    {
        return access;
    }
    
    /**
     * Sets the annotation type of this field.
     * @param  annotation annotation type
     */
    public void setAnnotationType(String annotation)
    {
        this.annotation = annotation;
    }
    
    /**
     * Tests if this field is annotated.
     * @return  <code>true</code> if annotated field
     */
    public boolean isAnnotated()
    {
        return annotation != null;
    }
    
    /**
     * Tests if this class member is a key field.
     * @return  <code>true</code> if key field
     */
    public boolean isKeyField()
    {
        return (annotation != null
                && annotation.equals(MetaDataProperties.ANNOTATION_KEY));
    }
    
    /**
     * Tests if this class member is a default-fetch-group field.
     * @return <code>true</code> if default-fetch-group field
     */
    public boolean isDfgField()
    {
        return (annotation != null
                && annotation.equals(MetaDataProperties.ANNOTATION_DFG));
    }
    
    /**
     * Tests if this class member is a mediated field.
     * @return  <code>true</code> if mediated field
     */
    public boolean isMediatedField()
    {
        return (annotation != null
                && annotation.equals(MetaDataProperties.ANNOTATION_MEDIATED));
    }
    
    /**
     * Sets the persistence modifier of this field.
     * @param jdoModifier  the persistence modifier of this field
     */
    public void setJdoModifier(String jdoModifier)
    {
        this.jdoModifier = jdoModifier;
    }
    
    /**
     * Tests if this field is declared transient.
     * @return  <code>true</code> if declared transient field
     * @see  #setJdoModifier
     */
    public boolean isKnownTransient()
    {
        return (jdoModifier != null
                && jdoModifier.equals(MetaDataProperties.JDO_TRANSIENT));
    }
    
    /**
     * Tests if this field is persistent.
     *
     * @return  <code>true</code> if persistent field
     * @see  #setJdoModifier
     */
    public boolean isPersistent()
    {
        // extended for property-based persistent field
        return (jdoModifier != null
                && (jdoModifier.equals(MetaDataProperties.JDO_PERSISTENT)
                    || jdoModifier.equals(MetaDataProperties.JDO_PROPERTY)));
    }
    
    /**
     * Tests if this field is transactional.
     *
     * @return  <code>true</code> if transactional field
     * @see  #setJdoModifier
     */
    public boolean isTransactional()
    {
        return (jdoModifier != null
                && jdoModifier.equals(MetaDataProperties.JDO_TRANSACTIONAL));
    }
    
    /**
     * Tests if this class member is a property.
     *
     * @return < code>true</code> if property
     * @see  #setJdoModifier
     */
    public boolean isProperty()
    {
        return (jdoModifier != null
                && jdoModifier.equals(MetaDataProperties.JDO_PROPERTY));
    }
    
    /**
     * Tests if this field is managed.
     *
     * @return  <code>true</code> if managed field
     */
    public boolean isManaged()
    {
        return (isPersistent() || isTransactional() || isProperty());
    }
    
    /**
     * Returns a string representation of this instance.
     *
     * @return  a string representation
     */
    public String toString()
    {
        return ('<' + "name:" + name
                + ',' + MetaDataProperties.PROPERTY_TYPE
                + ':' + type
                + ',' + MetaDataProperties.PROPERTY_ACCESS_MODIFIER
                + ':' + Modifier.toString(access)
                + ',' + MetaDataProperties.PROPERTY_JDO_MODIFIER
                + ':' + jdoModifier
                + ',' + MetaDataProperties.PROPERTY_ANNOTATION
                + ':' + annotation + '>');
    }
}
