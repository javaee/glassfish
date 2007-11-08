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

import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import com.sun.org.apache.jdo.model.jdo.JDOElement;
import com.sun.org.apache.jdo.model.jdo.JDOExtension;

/**
 * This is the super interface for JDO metadata elements, 
 * such as JDOClass, JDOField and JDORelationship.
 *
 * @author Michael Bouschen
 */
public class JDOElementImpl
    implements JDOElement
{
    /** List of vendorExtensions. */
    private List vendorExtensions = new ArrayList();

    /** Property change support. */
    private transient PropertyChangeSupport propertyChangeSupport;

    /** Vetoable change support. */
    private transient VetoableChangeSupport vetoableChangeSupport;

    /**
     * Remove the supplied vendor extension from the collection of extensions 
     * maintained by this JDOElement.
     */
    public void removeJDOExtension(JDOExtension vendorExtension)
    {
        vendorExtensions.remove(vendorExtension);
    }

    /**
     * Returns the collection of vendor extensions for this JDOElement
     * in the form of an array.
     * @return the vendor extensions for this JDOClass
     */
    public JDOExtension[] getJDOExtensions()
    {
        return (JDOExtension[])vendorExtensions.toArray(
            new JDOExtension[vendorExtensions.size()]);
    }

    /**
     * Creates a new JDOExtension instance and attaches it to the specified 
     * JDOElement object.
     */
    public JDOExtension createJDOExtension()
    {
        JDOExtension jdoExtension = new JDOExtensionImpl();
        vendorExtensions.add(jdoExtension);
        return jdoExtension;
    }

    /** 
     * Fires property change event.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected void firePropertyChange (String name, Object o, Object n)
    {
        if (propertyChangeSupport != null) {
            propertyChangeSupport.firePropertyChange(name, o, n);
        }
    }

    /** 
     * Add a property change listener.
     * @param l the listener to add
     */
    public synchronized void addPropertyChangeListener (PropertyChangeListener l)
    {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /** 
     * Remove a property change listener.
     * @param l the listener to remove
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l)
    {
        if (propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(l);
        }
    }

    /** Fires vetoable change event.
     * @param name property name
     * @param o old value
     * @param n new value
     * @exception PropertyVetoException when the change is vetoed by a listener
     */
    protected void fireVetoableChange (String name, Object o, Object n)
        throws PropertyVetoException
    {
        if (vetoableChangeSupport != null) {
            vetoableChangeSupport.fireVetoableChange(name, o, n);
        }
    }

    /** 
     * Add a vetoable change listener.
     * @param l the listener to add
     */
    public void addVetoableChangeListener(VetoableChangeListener l)
    {
        if (vetoableChangeSupport == null) {
            vetoableChangeSupport = new VetoableChangeSupport(this);
        }
        vetoableChangeSupport.addVetoableChangeListener(l);
    }

    /** 
     * Remove a vetoable change listener.
     * @param l the listener to remove
     */
    public void removeVetoableChangeListener(VetoableChangeListener l)
    {
        if (vetoableChangeSupport != null) {
            vetoableChangeSupport.removeVetoableChangeListener(l);
        }
        
    }
}
