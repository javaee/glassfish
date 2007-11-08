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
 * MappingElementImpl.java
 *
 * Created on March 24, 2000, 10:06 AM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.model.mapping.MappingElement;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public abstract class MappingElementImpl implements MappingElement {

    // <editor-fold desc="//===================== constants & variables =======================">

    /**
     * I18N message handler
     */
    private static final I18NHelper i18nHelper = I18NHelper.getInstance(
            "com.sun.persistence.impl.model.mapping.Bundle", // NOI18N
            MappingElementImpl.class.getClassLoader());

    /**
     * Property change support
     */
    private transient PropertyChangeSupport support;

    /**
     * Vetoable change support
     */
    private transient VetoableChangeSupport vetoableSupport;

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingElementImpl.(  This constructor should only be used for
     * cloning and archiving.)
     */
    protected MappingElementImpl() {
        super();
    }

    // </editor-fold>

    // <editor-fold desc="//========= MappingElement & related convenience methods ============">

    // <editor-fold desc="//=================== property change handling ======================">

    /**
     * Add a property change listener.
     * @param l the listener to add
     */
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener l) {
        if (support == null) {
            synchronized (this) {
                // new test under synchronized block
                if (support == null) {
                    support = new PropertyChangeSupport(this);
                }
            }
        }

        support.addPropertyChangeListener(l);
    }

    /**
     * Remove a property change listener.
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (support != null) {
            support.removePropertyChangeListener(l);
        }
    }

    /**
     * Fires property change event.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected void firePropertyChange(String name, Object o, Object n) {
        if (support != null) {
            support.firePropertyChange(name, o, n);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//=================== vetoable change handling ======================">

    /**
     * Add a vetoable change listener.
     * @param l the listener to add
     */
    public synchronized void addVetoableChangeListener(
            VetoableChangeListener l) {
        if (vetoableSupport == null) {
            vetoableSupport = new VetoableChangeSupport(this);
        }

        vetoableSupport.addVetoableChangeListener(l);
    }

    /**
     * Remove a vetoable change listener.
     * @param l the listener to remove
     */
    public synchronized void removeVetoableChangeListener(
            VetoableChangeListener l) {
        if (vetoableSupport != null) {
            vetoableSupport.removeVetoableChangeListener(l);
        }
    }

    /**
     * Fires vetoable change event.
     * @param name property name
     * @param o old value
     * @param n new value
     * @throws PropertyVetoException when the change is vetoed by a listener
     */
    protected void fireVetoableChange(String name, Object o, Object n)
            throws PropertyVetoException {
        if (vetoableSupport != null) {
            vetoableSupport.fireVetoableChange(name, o, n);
        }
    }

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="//===================== internal helper methods =====================">

    /**
     * @return I18N message handler for this element
     */
    protected static final I18NHelper getMessageHelper() {
        return i18nHelper;
    }

    // </editor-fold>

}
