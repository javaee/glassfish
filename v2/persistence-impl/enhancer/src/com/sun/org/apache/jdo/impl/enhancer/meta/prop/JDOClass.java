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

import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;


/**
 * A class to hold all parsed attributes of a class.
 */
final class JDOClass
{
    /**
     * The name of this class.
     */
    private final String name;
    
    /**
     * The name of the superclass.
     */
    private String superClassName = null;
    
    /**
     * The name of the oid class.
     */
    private String oidClassName = null;
    
    /**
     * The access modifier of this class.
     */
    private int access = Modifier.PUBLIC;
    
    /**
     * The persistence modifier of this class.
     */
    private boolean isPersistent = true;
    
    /**
     * Flag indicating whether this class is serializable.
     */
    private boolean isSerializable = true;
    
    /**
     * A list of all parsed fields.
     */
    private final List fields = new ArrayList();
    
    /**
     * The names of all managed fields this class.
     */
    private String[] managedFieldNames = null;
    
    /**
     * The names of all fields this class.
     */
    private String[] fieldNames = null;
    
    /**
     * Constructs a new object with the given name.
     * @param  name  The name of this class.
     */
    JDOClass(String name)
    {
        this.name = name;
    }
    
    /**
     * Returns the name of this class.
     * @return  The name of this class.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the access modifier of this class.
     * @param access  the access modifier of this class
     */
    public void setAccessModifiers(int access)
    {
        this.access = access;
    }
    
    /**
     * Returns the access modifiers of this class.
     * @return  the access modifiers of this class
     */
    public int getAccessModifiers()
    {
        return access;
    }
    
    /**
     * Sets the superclassname. The given classname should have a canonical
     * form (with dots). It is converted to the CM-similar notation
     * (with slashes).
     * @param  classname  The superclassname.
     */
    public void setSuperClassName(String classname)
    {
        superClassName = NameHelper.fromCanonicalClassName(classname);
    }
    
    /**
     * Returns the superclassname.
     * @return  The superclassname.
     */
    public String getSuperClassName()
    {
        return superClassName;
    }
    
    /**
     * Sets the oid classname. The given classname should have a canonical
     * form (with dots). It is converted to the CM-similar notation
     * (with slashes).
     * @param  classname  The oid classname
     */
    public void setOidClassName(String classname)
    {
        oidClassName = NameHelper.fromCanonicalClassName(classname);
    }
    
    /**
     * Returns the oid classname.
     * @return  The oid classname
     */
    public String getOidClassName()
    {
        return oidClassName;
    }
    
    /**
     * Sets the persistence modifier of this class.
     * @param persistent  the persistence modifer
     * @see  #isPersistent
     */
    public void setPersistent(boolean persistent)
    {
        this.isPersistent = persistent;
    }
    
    /**
     * Tests if this class is persistent.
     * @return  true if persistent class.
     * @see  #isPersistent
     */
    public boolean isPersistent()
    {
        return isPersistent;
    }
    
    /**
     * Tests if this class is transient.
     * @return  true if transient class.
     * @see  #isPersistent
     */
    public boolean isTransient()
    {
        return !isPersistent();
    }
    
    /**
     * Tests if this class is serializable.
     * @return  true if serializable class.
     * @see  #isSerializable
     */
    public boolean isSerializable()
    {
        return isSerializable;
    }
    
    /**
     * Sets the serializable flag of this class.
     * @param serializable  the serializable flag
     * @see  #isSerializable
     */
    public void setSerializable(boolean serializable)
    {
        this.isSerializable = serializable;
    }
    /**
     * Adds a new field.
     * @param  field  The new field.
     */
    public void addField(JDOField field)
    {
        fields.add(field);
    }
    
    /**
     * Returns the field with the given name.
     * @param  name  The name of the requested field.
     * @return  The field or <code>null</code> if not found.
     */
    public JDOField getField(String name)
    {
        int idx = getIndexOfField(name);
        return (idx > -1  ? (JDOField) fields.get(idx)  :  null);
    }
    
    /**
     * Returns the index of the field with the given name.
     * @param  name  The name of the field.
     * @return  The index or <code>-1</code> if the field was not found.
     */
    public int getIndexOfField(String name)
    {
        for (int i = 0; i < fields.size(); i++) {
            JDOField field = (JDOField)fields.get(i);
            if (field.getName().equals(name)) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Returns all fields of this class.
     * @return  The fields
     */
    public List getFields()
    {
        return fields;
    }
    
    /**
     * Returns the names of all fields of this class.
     * @return  The field names
     */
    public String[] getFieldNames()
    {
        if (fieldNames == null) {
            final int n = fields.size();
            String[] fields = new String[n];
            for (int i = 0; i < n; i++) {
                fields[i] = ((JDOField)this.fields.get(i)).getName();
            }
            fieldNames = fields;
        }
        
        return fieldNames;
    }
    
    /**
     * Sorts the fields of this class according to the names. This method
     * should be called if all fields are added. It is necessary to
     * establish an order on the fields.
     */
    final void sortFields()
    {
        Collections.sort(
            fields,
            new Comparator() {
                public int compare(Object f1, Object f2)
                {
                    JDOField field1 = (JDOField)f1;
                    JDOField field2 = (JDOField)f2;
                    //if we dont have managed fields we dont care
                    if (!(field1.isManaged() && field2.isManaged()))
                    {
                        return (field1.isManaged() ? -1 : 1);
                    }
                    return field1.getName().compareTo(field2.getName());
                }
            });
    }
    
    /**
     * Returns the names of all managed fields this class.
     * @return  The persistent fieldnames.
     */
    public String[] getManagedFieldNames()
    {
        if (managedFieldNames == null) {
            final int n = fields.size();
            List tmp = new ArrayList(n);
            for (int i = 0; i < n; i++) {
                JDOField field = (JDOField)fields.get(i);
                if (field.isManaged()) {
                    tmp.add(field.getName());
                }
            }
            managedFieldNames
                = (String[])tmp.toArray(new String[tmp.size()]);
        }
        
        return managedFieldNames;
    }
    
    /**
     * Creates a string-representation for this object.
     * @return  The string-representation of this object.
     */
    public String toString()
    {
        return ('<' + MetaDataProperties.PROPERTY_SUPER_CLASSNAME
                + ':' + superClassName
                + MetaDataProperties.PROPERTY_OID_CLASSNAME
                + ':' + oidClassName
                + ',' + MetaDataProperties.PROPERTY_ACCESS_MODIFIER
                + ':' + Modifier.toString(access)
                + ',' + MetaDataProperties.PROPERTY_JDO_MODIFIER
                + ':' + isPersistent
                + ',' + "fields:" + fields + '>');
    }
}
