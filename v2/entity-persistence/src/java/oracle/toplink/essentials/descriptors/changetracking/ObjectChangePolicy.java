/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package oracle.toplink.essentials.descriptors.changetracking;

import java.io.Serializable;
import oracle.toplink.essentials.internal.sessions.ObjectChangeSet;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.helper.IdentityHashtable;

/**
 * INTERNAL:
 * Implementers of ObjectChangePolicy implement the code which computes changes sets
 * for TopLink's UnitOfWork commit process.  An ObjectChangePolicy is stored on an
 * Object's descriptor.
 * @see DeferredChangeDetectionPolicy
 * @see ObjectChangeTrackingPolicy
 * @see AttributeChangeTrackingPolicy
 * @author Tom Ware
 */
public interface ObjectChangePolicy extends Serializable {

    /**
     * INTERNAL:
     * calculateChanges creates a change set for a particular object
     * @return oracle.toplink.essentials.changesets.ObjectChangeSet an object change set describing
     * the changes to this object
     * @param clone the Object to compute a change set for
     * @param backUp the old version of the object to use for comparison
     * @param changes the change set to add changes to
     * @param session the current session
     * @param descriptor the descriptor for this object
     * @param shouldRiseEvent indicates whether PreUpdate event should be risen (usually true)
     */
    public ObjectChangeSet calculateChanges(Object clone, Object backUp, oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet changes, AbstractSession session, ClassDescriptor descriptor, boolean shouldRiseEvent);

    /**
     * INTERNAL:
     * Create ObjectChangeSet through comparison.  Used in cases where we need to force change calculation (ie aggregates)
     */
    public ObjectChangeSet createObjectChangeSetThroughComparison(Object clone, Object backUp, oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet changeSet, boolean isNew, AbstractSession session, ClassDescriptor descriptor);

    /**
     * INTERNAL:
     * This method is used to dissable changetracking temporarily
     */
    public void dissableEventProcessing(Object changeTracker);

    /**
     * INTERNAL:
     * This method is used to enable changetracking temporarily
     */
    public void enableEventProcessing(Object changeTracker);
    
    /**
     * INTERNAL:
     * This may cause a property change event to be raised to a listner in the case that a listener exists.
     * If there is no listener then this call is a no-op
     */
    public void raiseInternalPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue);
    
    /**
     * INTERNAL:
     * This method is used to revert an object within the unit of work
     */
    public void revertChanges(Object clone, ClassDescriptor descriptor, UnitOfWorkImpl uow, IdentityHashtable cloneMapping);

    /**
     * INTERNAL:
     * This is a place holder for reseting the listener on one of the subclasses
     */
    public void clearChanges(Object object, UnitOfWorkImpl uow, ClassDescriptor descriptor);
    
    /**
     * INTERNAL:
     * This method is used internally to rest the policies back to original state
     * This is used when the clones are to be reused.
     */
    public void updateWithChanges(Object clone, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow, ClassDescriptor descriptor);

    /**
     * INTERNAL:
     * Return true if the Object should be compared, false otherwise.  This method is implemented to allow
     * run time determination of whether a change set should be computed for an object. In general, calculateChanges()
     * will only be executed in a UnitOfWork if this method returns true.
     * @param object the object that will be compared
     * @param unitOfWork the active unitOfWork
     * @param descriptor the descriptor for the current object
     */
    public boolean shouldCompareForChange(Object object, UnitOfWorkImpl unitOfWork, ClassDescriptor descriptor);

    /**
     * INTERNAL:
     * Assign Changelistner to an aggregate object
     */
    public void setAggregateChangeListener(Object parent, Object aggregate, UnitOfWorkImpl uow, ClassDescriptor descriptor, String mappingAttribute);

    /**
     * INTERNAL:
     * Assign appropriate ChangeListener to PropertyChangeListener based on the policy.
     */
    public void setChangeListener(Object clone, UnitOfWorkImpl uow, ClassDescriptor descriptor);

    /**
     * INTERNAL:
     * Set the ObjectChangeSet on the Listener, initially used for aggregate support
     */
    public void setChangeSetOnListener(ObjectChangeSet objectChangeSet, Object clone);
    
    /**
     * INTERNAL:
     * Build back up clone.
     */
    public Object buildBackupClone(Object clone, ObjectBuilder builder, UnitOfWorkImpl uow);

    /**
     * INTERNAL:
     * initialize the Policy
     */
    public void initialize(AbstractSession session, ClassDescriptor descriptor);

    /**
     * Used to track instances of the change policies without doing an instance of check
     */
    public boolean isDeferredChangeDetectionPolicy();

    /**
     * Used to track instances of the change policies without doing an instance of check
     */
    public boolean isObjectChangeTrackingPolicy();

    /**
     * Used to track instances of the change policies without doing an instance of check
     */
    public boolean isAttributeChangeTrackingPolicy();
}
