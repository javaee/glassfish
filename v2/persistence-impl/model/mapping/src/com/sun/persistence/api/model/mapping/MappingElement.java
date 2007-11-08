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
 * MappingElement.java
 *
 */


package com.sun.persistence.api.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

/**
 *
 */
public interface MappingElement extends MappingElementProperties {
    // <editor-fold desc="//=================== property change handling ======================">

    /**
     * Add a property change listener.
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove a property change listener.
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l);

    // </editor-fold>

    // <editor-fold desc="//=================== vetoable change handling ======================">

    /**
     * Add a vetoable change listener.
     * @param l the listener to add
     */
    public void addVetoableChangeListener(VetoableChangeListener l);

    /**
     * Remove a vetoable change listener.
     * @param l the listener to remove
     */
    public void removeVetoableChangeListener(VetoableChangeListener l);

    // </editor-fold>
}
