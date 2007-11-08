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

package com.sun.org.apache.jdo.model.jdo;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import com.sun.org.apache.jdo.model.ModelException;

/**
 * This is the super interface for JDO metadata elements, 
 * such as JDOClass, JDOField and JDORelationship.
 *
 * @author Michael Bouschen
 */
public interface JDOElement 
{
    /**
     * Remove the supplied vendor extension from the collection of extensions 
     * maintained by this JDOElement.
     * @exception ModelException if impossible
     */
    public void removeJDOExtension(JDOExtension vendorExtension)
        throws ModelException;

    /**
     * Returns the collection of vendor extensions for this JDOElement
     * in the form of an array.
     * @return the vendor extensions for this JDOClass
     */
    public JDOExtension[] getJDOExtensions();

    /**
     * Creates a new JDOExtension instance and attaches it to the specified 
     * JDOElement object.
     * @exception ModelException if impossible
     */
    public JDOExtension createJDOExtension()
        throws ModelException;

    /** 
     * Add a property change listener.
     * @param l the listener to add
     * @exception ModelException if impossible
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
        throws ModelException;

    /** 
     * Remove a property change listener.
     * @param l the listener to remove
     * @exception ModelException if impossible
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
        throws ModelException;

    /** 
     * Add a vetoable change listener.
     * @param l the listener to add
     * @exception ModelException if impossible
     */
    public void addVetoableChangeListener(VetoableChangeListener l)
        throws ModelException;

    /** 
     * Remove a vetoable change listener.
     * @param l the listener to remove
     * @exception ModelException if impossible
     */
    public void removeVetoableChangeListener(VetoableChangeListener l)
        throws ModelException;

}
