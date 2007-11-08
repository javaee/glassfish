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

import java.util.*;
import oracle.toplink.essentials.internal.sessions.ObjectChangeSet;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.sessions.MergeManager;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.descriptors.DescriptorEventManager;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.helper.IdentityHashtable;

/**
 * PUBLIC:
 * A DeferredChangeDetectionPolicy defers all change detection to the UnitOfWork's
 * change detection process.  Essentially, the calculateChanges() method will run
 * for all objects in a UnitOfWork.  This is the default ObjectChangePolicy.
 * 
 * @author Tom Ware
 */
public class DeferredChangeDetectionPolicy implements ObjectChangePolicy, java.io.Serializable {

    /**
     * INTERNAL:
     * calculateChanges creates a change set for a particular object.  In DeferredChangeDetectionPolicy
     * all mappings will be compared against a backup copy of the object.
     * @return oracle.toplink.essentials.changesets.ObjectChangeSet an object change set describing
     * the changes to this object
     * @param java.lang.Object clone the Object to compute a change set for
     * @param java.lang.Object backUp the old version of the object to use for comparison
     * @param oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet the change set to add changes to
     * @param Session the current session
     * @param Descriptor the descriptor for this object
     * @param shouldRiseEvent indicates whether PreUpdate event should be risen (usually true)
     */
    public ObjectChangeSet calculateChanges(Object clone, Object backUp, oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet changeSet, AbstractSession session, ClassDescriptor descriptor, boolean shouldRiseEvent) {
        boolean isNew = ((backUp == null) || ((((UnitOfWorkImpl)session).isObjectNew(clone)) && (!descriptor.isAggregateDescriptor())));
 
        // PERF: Avoid events if no listeners.
        if (descriptor.getEventManager().hasAnyEventListeners() && shouldRiseEvent) {
            // The query is built for compatability to old event mechanism.
            WriteObjectQuery writeQuery = new WriteObjectQuery(clone.getClass());
            writeQuery.setObject(clone);
            writeQuery.setBackupClone(backUp);
            writeQuery.setSession(session);
            writeQuery.setDescriptor(descriptor);

            descriptor.getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreWriteEvent, writeQuery));

            if (isNew) {
                descriptor.getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreInsertEvent, writeQuery));
            } else {
                descriptor.getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreUpdateEvent, writeQuery));
            }
        }

        ObjectChangeSet changes = createObjectChangeSet(clone, backUp, changeSet, isNew, session, descriptor);

        changes.setShouldModifyVersionField((Boolean)((UnitOfWorkImpl)session).getOptimisticReadLockObjects().get(clone));

        if (changes.hasChanges() || changes.hasForcedChanges()) {
            return changes;
        }
        return null;
    }

    /**
     * INTERNAL:
     * This is a place holder for reseting the listener on one of the subclasses
     */
    public void clearChanges(Object object, UnitOfWorkImpl uow, ClassDescriptor descriptor) {
    }

    /**
     * INTERNAL:
     * Create ObjectChangeSet
     */
    public ObjectChangeSet createObjectChangeSet(Object clone, Object backUp, oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet changeSet, boolean isNew, AbstractSession session, ClassDescriptor descriptor) {
        return this.createObjectChangeSetThroughComparison(clone, backUp, changeSet, isNew, session, descriptor);
    }

    /**
     * INTERNAL:
     * Create ObjectChangeSet
     */
    public ObjectChangeSet createObjectChangeSetThroughComparison(Object clone, Object backUp, oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet changeSet, boolean isNew, AbstractSession session, ClassDescriptor descriptor) {
        ObjectBuilder builder = descriptor.getObjectBuilder();
        ObjectChangeSet changes = builder.createObjectChangeSet(clone, changeSet, isNew, session);

        // The following code deals with reads that force changes to the flag associated with optimistic locking.
        if ((descriptor.usesOptimisticLocking()) && (changes.getPrimaryKeys() != null)) {
            changes.setOptimisticLockingPolicyAndInitialWriteLockValue(descriptor.getOptimisticLockingPolicy(), session);
        }

        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = descriptor.getMappings();
        int mappingsSize = mappings.size();
        for (int index = 0; index < mappingsSize; index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            changes.addChange(mapping.compareForChange(clone, backUp, changes, session));
        }

        return changes;
    }

    /**
     * INTERNAL:
     * This method is used to dissable changetracking temporarily
     */
    public void dissableEventProcessing(Object changeTracker){
        //no-op
    }

    /**
     * INTERNAL:
     * This method is used to enable changetracking temporarily
     */
    public void enableEventProcessing(Object changeTracker){
        //no-op
    }
    
    /**
     * INTERNAL:
     * Return true if the Object should be compared, false otherwise.  In DeferredChangeDetectionPolicy,
     * true is always returned since always allow the UnitOfWork to calculate changes.
     * @param java.lang.Object object - the object that will be compared
     * @param oracle.toplink.essentials.publicinterface.UnitOfWork unitOfWork - the active unitOfWork
     * @param oracle.toplink.essentials.publicinterface.Descriptor descriptor - the descriptor for the current object
     */
    public boolean shouldCompareForChange(Object object, UnitOfWorkImpl unitOfWork, ClassDescriptor descriptor) {
        return true;
    }

    /**
     * INTERNAL:
     * Build back up clone.  Used if clone is new because listener should not be set.
     */
    public Object buildBackupClone(Object clone, ObjectBuilder builder, UnitOfWorkImpl uow) {
        return builder.buildBackupClone(clone, uow);
    }

    /**
     * INTERNAL:
     * Assign Changelistner to an aggregate object
     */
    public void setAggregateChangeListener(Object parent, Object aggregate, UnitOfWorkImpl uow, ClassDescriptor descriptor, String mappingAttribute){
        //no-op
    }
    
    /**
     * INTERNAL:
     * Set ChangeListener for the clone
     */
    public void setChangeListener(Object clone, UnitOfWorkImpl uow, ClassDescriptor descriptor) {
        //no-op
    }

    /**
     * INTERNAL:
     * Set the ObjectChangeSet on the Listener, initially used for aggregate support
     */
    public void setChangeSetOnListener(ObjectChangeSet objectChangeSet, Object clone){
        //no-op
    }
    
    /**
     * INTERNAL:
     * Clear changes in the ChangeListener of the clone
     */
    public void updateWithChanges(Object clone, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow, ClassDescriptor descriptor) {
        if (objectChangeSet == null) {
            return;
        }
        Object backupClone = uow.getCloneMapping().get(clone);
        if (backupClone != null) {
            MergeManager mergeManager = new MergeManager(uow);
            mergeManager.setCascadePolicy(MergeManager.NO_CASCADE);
            descriptor.getObjectBuilder().mergeChangesIntoObject(backupClone, objectChangeSet, clone, mergeManager);
        }
        clearChanges(clone, uow, descriptor);
    }

    /**
     * INTERNAL:
     * This may cause a property change event to be raised to a listner in the case that a listener exists.
     * If there is no listener then this call is a no-op
     */
    public void raiseInternalPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue){
        //no-op
    }

    /**
     * INTERNAL:
     * This method is used to revert an object within the unit of work
     * @param cloneMapping may not be the same as whats in the uow
     */
    public void revertChanges(Object clone, ClassDescriptor descriptor, UnitOfWorkImpl uow, IdentityHashtable cloneMapping) {
        cloneMapping.put(clone, buildBackupClone(clone, descriptor.getObjectBuilder(), uow));
        clearChanges(clone, uow, descriptor);
    }

    /**
     * INTERNAL:
     * initialize the Policy
     */
    public void initialize(AbstractSession session, ClassDescriptor descriptor) {
        //do nothing
    }

    /**
     * Used to track instances of the change policies without doing an instance of check
     */
    public boolean isDeferredChangeDetectionPolicy(){
        return true;
    }

    /**
     * Used to track instances of the change policies without doing an instance of check
     */
    public boolean isObjectChangeTrackingPolicy(){
        return false;
    }

    /**
     * Used to track instances of the change policies without doing an instance of check
     */
    public boolean isAttributeChangeTrackingPolicy(){
        return false;
    }
}
