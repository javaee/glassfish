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

package com.sun.org.apache.jdo.impl.model.jdo;

import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOMember;

/**
 * This is the super interface for named JDO metadata elements, 
 * such as JDOClass and JDOField.
 *
 * @author Michael Bouschen
 */
public class JDOMemberImpl
    extends JDOElementImpl
    implements JDOMember
{
    /** Property name.*/
    private String name;

    /** Relationship JDOClass<->JDOMember. */
    private JDOClass declaringClass;

    /** Constructor. */
    protected JDOMemberImpl(String name, JDOClass declaringClass)
    {
        this.name = name;
        this.declaringClass = declaringClass;
    }

    /**
     * Returns the name of this JDOMember.
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /** 
     * Get the declaring class of this JDOMember.
     * @return the class that owns this JDOMember, or <code>null</code>
     * if the element is not attached to any class
     */
    public JDOClass getDeclaringClass()
    {
        return declaringClass;
    }

    //================= redefinition of java.lang.Object methods ================
    
    /** 
     * Overrides Object's <code>toString</code> method to return the name
     * of this persistence element.
     * @return a string representation of the object
     */
    public String toString () 
    { 
        return getName(); 
    }
    
    /** 
     * Overrides Object's <code>equals</code> method by comparing the name 
     * of this member with the name of the argument obj. The method returns 
     * <code>false</code> if obj does not have the same dynamic type as this 
     * member.
     * @return <code>true</code> if this object is the same as the obj argument;
     * <code>false</code> otherwise.
     * @param obj the reference object with which to compare.
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        
        // check for the right class and then do the name check by 
        // calling compareTo.
        return (getClass() == obj.getClass()) && (compareTo(obj) == 0);
    }
    
    /** Overrides Object's <code>hashCode</code> method to return the hashCode 
     * of this name.
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        return (getName()==null) ? 0 : getName().hashCode();
    }
    
    //================= implementation of Comparable ================
    
    /** 
     * Compares this object with the specified object for order. Returns a 
     * negative integer, zero, or a positive integer as this object is less than, 
     * equal to, or greater than the specified object. The specified object must 
     * be a an instance of JDOMember, if not a ClassCastException is thrown.
     * The order of JDOMember instances is defined by the order of their names.
     * JDOMember instances without name are considered to be less than any named 
     * member.
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     * @exception ClassCastException - if the specified object is null or is not 
     * an instance of JDOMember
     */
    public int compareTo(Object o)
    {
        // null is not allowed
        if (o == null)
            throw new ClassCastException();
        if (o == this)
            return 0;
        
        String thisName = getName();
        // the following throws a ClassCastException if o is not a JDOMember
        String otherName = ((JDOMember)o).getName();
        // if this does not have a name it should compare less than any named
        if (thisName == null) {
            return (otherName == null) ? 0 : -1;
        }
        
        // if this is named and o does not have a name it should compare greater
        if (otherName == null) {
            return 1;
        }
        
        // now we know that this and o are named JDOMembers => compare the names
        int ret = thisName.compareTo(otherName);
        // If both names are equal, both objects might have different types.
        // If so order both objects by their type names 
        // (necessary to be consistent with equals)
        if ((ret == 0) && (getClass() != o.getClass()))
            ret = getClass().getName().compareTo(o.getClass().getName());
        return ret;
    }
}
