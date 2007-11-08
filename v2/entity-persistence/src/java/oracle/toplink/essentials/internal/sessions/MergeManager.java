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
package oracle.toplink.essentials.internal.sessions;

import java.util.*;
import oracle.toplink.essentials.descriptors.VersionLockingPolicy;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.helper.linkedlist.LinkedNode;
import oracle.toplink.essentials.internal.queryframework.ContainerPolicy;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.descriptors.ObjectBuilder;
import oracle.toplink.essentials.internal.descriptors.OptimisticLockingPolicy;

/**
 * <p><b>Purpose</b>:
 * Used to manage the merge of two objects in a unit of work.
 *
 * @author James Sutherland
 * @since TOPLink/Java 1.1
 */
public class MergeManager {

    /** The unit of work merging for. */
    protected AbstractSession session;

    /** Used only while refreshing objects on remote session */
    protected IdentityHashtable objectDescriptors;

    /** Used to unravel recursion. */
    protected IdentityHashtable objectsAlreadyMerged;
    
    /** Used to keep track of merged new objects. */
    protected IdentityHashtable mergedNewObjects;

    /** Used to store the list of locks that this merge manager has acquired for this merge */
    protected ArrayList acquiredLocks;

    /** If this variable is not null then the mergemanager is waiting on a particular cacheKey */
    protected CacheKey writeLockQueued;

    /** Stores the node that holds this mergemanager within the WriteLocksManager queue */
    protected LinkedNode queueNode;

    /** Policy that determines merge type (i.e. merge is used for several usages). */
    protected int mergePolicy;
    protected static final int WORKING_COPY_INTO_ORIGINAL = 1;
    protected static final int ORIGINAL_INTO_WORKING_COPY = 2;
    protected static final int CLONE_INTO_WORKING_COPY = 3;
    protected static final int WORKING_COPY_INTO_REMOTE = 4;
    protected static final int REFRESH_REMOTE_OBJECT = 5;
    protected static final int CHANGES_INTO_DISTRIBUTED_CACHE = 6;
    protected static final int CLONE_WITH_REFS_INTO_WORKING_COPY = 7;
    protected static final int WORKING_COPY_INTO_BACKUP = 9;

    /** Policy that determines how the merge will cascade to its object's parts. */
    protected int cascadePolicy;
    public static final int NO_CASCADE = 1;
    public static final int CASCADE_PRIVATE_PARTS = 2;
    public static final int CASCADE_ALL_PARTS = 3;
    public static final int CASCADE_BY_MAPPING = 4;
    protected long systemTime = 0;// stored so that all objects merged by a merge manager can have the same readTime
    public static boolean LOCK_ON_MERGE = true;

    /** Force cascade merge even if a clone is already registered */
    // GF#1139 Cascade doesn't work when merging managed entity
    protected boolean forceCascade;

    public MergeManager(AbstractSession session) {
        this.session = session;
        this.mergedNewObjects = new IdentityHashtable();
        this.objectsAlreadyMerged = new IdentityHashtable();
        this.cascadePolicy = CASCADE_ALL_PARTS;
        this.mergePolicy = WORKING_COPY_INTO_ORIGINAL;
        this.objectDescriptors = new IdentityHashtable();
        this.acquiredLocks = new ArrayList();
    }

    /**
     * Build and return an identity set for the specified container.
     */
    protected IdentityHashtable buildIdentitySet(Object container, ContainerPolicy containerPolicy, boolean keyByTarget) {
        // find next power-of-2 size
        IdentityHashtable result = new IdentityHashtable(containerPolicy.sizeFor(container) + 1);
        for (Object iter = containerPolicy.iteratorFor(container); containerPolicy.hasNext(iter);) {
            Object element = containerPolicy.next(iter, getSession());
            if (keyByTarget) {
                result.put(getTargetVersionOfSourceObject(element), element);
            } else {
                result.put(element, element);
            }
        }
        return result;
    }

    /**
     * Cascade all parts, this is the default for the merge.
     */
    public void cascadeAllParts() {
        setCascadePolicy(CASCADE_ALL_PARTS);
    }

    /**
     * Cascade private parts, this can be used to merge clone when using RMI.
     */
    public void cascadePrivateParts() {
        setCascadePolicy(CASCADE_PRIVATE_PARTS);
    }

    /**
     * Merge only direct parts, this can be used to merge clone when using RMI.
     */
    public void dontCascadeParts() {
        setCascadePolicy(NO_CASCADE);
    }

    public ArrayList getAcquiredLocks() {
        return this.acquiredLocks;
    }

    public int getCascadePolicy() {
        return cascadePolicy;
    }

    protected int getMergePolicy() {
        return mergePolicy;
    }

    public IdentityHashtable getObjectDescriptors() {
        return objectDescriptors;
    }

    // cr 2855 changed visibility of following method
    public IdentityHashtable getObjectsAlreadyMerged() {
        return objectsAlreadyMerged;
    }

    public Object getObjectToMerge(Object sourceValue) {
        if (shouldMergeOriginalIntoWorkingCopy()) {
            return getTargetVersionOfSourceObject(sourceValue);
        }

        return sourceValue;
    }

    /**
     * INTENRAL:
     * Used to get the node that this merge manager is stored in, within the WriteLocksManager write lockers queue
     */
    public LinkedNode getQueueNode() {
        return this.queueNode;
    }

    public AbstractSession getSession() {
        return session;
    }

    /**
     * Get the stored value of the current time.  This method lazily initializes
     * so that read times for the same merge manager can all be set to the same read time
     */
    public long getSystemTime() {
        if (systemTime == 0) {
            systemTime = System.currentTimeMillis();
        }
        return systemTime;
    }

    /**
     * Return the coresponding value that should be assigned to the target object for the source object.
     * This value must be local to the targets object space.
     */
    public Object getTargetVersionOfSourceObject(Object source) {
        if (shouldMergeWorkingCopyIntoOriginal() || shouldMergeWorkingCopyIntoRemote()) {
            // Target is in uow parent, or original instance for new object.
            return ((UnitOfWorkImpl)getSession()).getOriginalVersionOfObject(source);
        } else if (shouldMergeCloneIntoWorkingCopy() || shouldMergeOriginalIntoWorkingCopy() || shouldMergeCloneWithReferencesIntoWorkingCopy()) {
            // Target is clone from uow.
            //make sure we use the register for merge
            //bug 3584343
            return registerObjectForMergeCloneIntoWorkingCopy(source);
        } else if (shouldRefreshRemoteObject()) {
            // Target is in session's cache.
            ClassDescriptor descriptor = getSession().getDescriptor(source);
            Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(source, getSession());
            return getSession().getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, source.getClass(), descriptor);
        }

        throw ValidationException.invalidMergePolicy();
    }

    /**
     * INTENRAL:
     * Used to get the object that the merge manager is waiting on, in order to acquire locks
     */
    public CacheKey getWriteLockQueued() {
        return this.writeLockQueued;
    }

    /**
     * Recursively merge changes in the object dependent on the merge policy.
     * The hastable is used to resolv recursion.
     */
    public Object mergeChanges(Object object, ObjectChangeSet objectChangeSet) throws ValidationException {
        if (object == null) {
            return object;
        }

        // Do not merged read-only objects in a unit of work.
        if (getSession().isClassReadOnly(object.getClass())) {
            return object;
        }

        // Means that object is either already merged or in the process of being merged.	
        if (getObjectsAlreadyMerged().containsKey(object)) {
            return object;
        }

        // Put the object to be merged in the set.
        getObjectsAlreadyMerged().put(object, object);

        Object mergedObject;
        if (shouldMergeWorkingCopyIntoOriginal()) {
            mergedObject = mergeChangesOfWorkingCopyIntoOriginal(object, objectChangeSet);
        } else if (shouldMergeCloneIntoWorkingCopy() || shouldMergeCloneWithReferencesIntoWorkingCopy()) {
            mergedObject = mergeChangesOfCloneIntoWorkingCopy(object);
        } else if (shouldMergeOriginalIntoWorkingCopy()) {
            mergedObject = mergeChangesOfOriginalIntoWorkingCopy(object);
        } else {
            throw ValidationException.invalidMergePolicy();
        }

        return mergedObject;
    }

    /**
     * INTERNAL:
     * Merge the changes to all objects to session's cache.
     */
    public void mergeChangesFromChangeSet(UnitOfWorkChangeSet uowChangeSet) {
        getSession().startOperationProfile(SessionProfiler.DistributedMerge);
        // Ensure concurrency if cache isolation requires.
        getSession().getIdentityMapAccessorInstance().acquireWriteLock();
        getSession().log(SessionLog.FINER, SessionLog.PROPAGATION, "received_updates_from_remote_server");
        getSession().getEventManager().preDistributedMergeUnitOfWorkChangeSet(uowChangeSet);

        try {
            // Iterate over each clone and let the object build merge to clones into the originals.
            getSession().getIdentityMapAccessorInstance().getWriteLockManager().acquireRequiredLocks(this, uowChangeSet);
            Enumeration objectChangeEnum = uowChangeSet.getAllChangeSets().keys();
            while (objectChangeEnum.hasMoreElements()) {
                ObjectChangeSet objectChangeSet = (ObjectChangeSet)objectChangeEnum.nextElement();
                Object object = objectChangeSet.getTargetVersionOfSourceObject(getSession(), false);

                // Don't read the object here.  If it is null then we won't merge it at this stage, unless it
                // is being referenced which will force the load later
                Object mergedObject = this.mergeChanges(object, objectChangeSet);

                // if mergedObject is null, it might be because objectChangeSet represents a new object and could not look it up
                // Check the descriptor setting for this change set to see if the new object should be added to the cache.
                if (mergedObject == null) {
                    if (objectChangeSet.isNew()) {
                        mergedObject = mergeNewObjectIntoCache(objectChangeSet);
                    }
                }
                if (mergedObject == null) {
                    getSession().incrementProfile(SessionProfiler.ChangeSetsNotProcessed);
                } else {
                    getSession().incrementProfile(SessionProfiler.ChangeSetsProcessed);
                }
            }
            Enumeration deletedObjects = uowChangeSet.getDeletedObjects().elements();
            while (deletedObjects.hasMoreElements()) {
                ObjectChangeSet changeSet = (ObjectChangeSet)deletedObjects.nextElement();
                changeSet.removeFromIdentityMap(getSession());
                getSession().incrementProfile(SessionProfiler.DeletedObject);
            }
        } catch (RuntimeException exception) {
            getSession().handleException(exception);
        } finally {
            getSession().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(this);
            getSession().getIdentityMapAccessorInstance().releaseWriteLock();
            getSession().getEventManager().postDistributedMergeUnitOfWorkChangeSet(uowChangeSet);
            getSession().endOperationProfile(SessionProfiler.DistributedMerge);
        }
    }

    /**
     * Merge the changes from the collection of a source object into the target using the backup as the diff.
     * Return true if any changes occurred.
     */
    public boolean mergeChangesInCollection(Object source, Object target, Object backup, DatabaseMapping mapping) {
        ContainerPolicy containerPolicy = mapping.getContainerPolicy();

        // The vectors must be converted to identity sets to avoid dependency on #equals().
        IdentityHashtable backupSet = buildIdentitySet(backup, containerPolicy, false);
        IdentityHashtable sourceSet = null;
        IdentityHashtable targetToSources = null;

        // We need either a source or target-to-source set, depending on the merge type.
        if (shouldMergeWorkingCopyIntoOriginal()) {
            sourceSet = buildIdentitySet(source, containerPolicy, false);
        } else {
            targetToSources = buildIdentitySet(source, containerPolicy, true);
        }

        boolean changeOccured = false;

        // If the backup is also the target, clone it; since it may change during the loop.
        if (backup == target) {
            backup = containerPolicy.cloneFor(backup);
        }

        // Handle removed elements.
        for (Object backupIter = containerPolicy.iteratorFor(backup);
                 containerPolicy.hasNext(backupIter);) {
            Object backupElement = containerPolicy.next(backupIter, getSession());

            // The check for delete depends on the type of merge.
            if (shouldMergeWorkingCopyIntoOriginal()) {// Source and backup are the same space.
                if (!sourceSet.containsKey(backupElement)) {
                    changeOccured = true;
                    containerPolicy.removeFrom((Object)null, getTargetVersionOfSourceObject(backupElement), target, getSession());

                    // Registered new object in nested units of work must not be registered into the parent,
                    // so this records them in the merge to parent case.
                    if (mapping.isPrivateOwned()) {
                        registerRemovedNewObjectIfRequired(backupElement);
                    }
                }
            } else {// Target and backup are same for all types of merge.
                if (!targetToSources.containsKey(backupElement)) {// If no source for target then was removed.
                    changeOccured = true;
                    containerPolicy.removeFrom((Object)null, backupElement, target, getSession());// Backup value is same as target value.
                }
            }
        }

        // Handle added elements.
        for (Object sourceIter = containerPolicy.iteratorFor(source);
                 containerPolicy.hasNext(sourceIter);) {
            Object sourceElement = containerPolicy.next(sourceIter, getSession());

            // The target object must be completely merged before adding to the collection
            // otherwise another thread could pick up the partial object.
            mapping.cascadeMerge(sourceElement, this);
            // The check for add depends on the type of merge.
            if (shouldMergeWorkingCopyIntoOriginal()) {// Source and backup are the same space.
                if (!backupSet.containsKey(sourceElement)) {
                    changeOccured = true;
                    containerPolicy.addInto(getTargetVersionOfSourceObject(sourceElement), target, getSession());
                } else {
                    containerPolicy.validateElementAndRehashIfRequired(sourceElement, target, getSession(), getTargetVersionOfSourceObject(sourceElement));
                }
            } else {// Target and backup are same for all types of merge.
                Object targetVersionOfSourceElement = getTargetVersionOfSourceObject(sourceElement);
                if (!backupSet.containsKey(targetVersionOfSourceElement)) {// Backup value is same as target value.
                    changeOccured = true;
                    containerPolicy.addInto(targetVersionOfSourceElement, target, getSession());
                }
            }
        }

        return changeOccured;
    }

    /**
     * Recursively merge to rmi clone into the unit of work working copy.
     * The hastable is used to resolv recursion.
     */
    protected Object mergeChangesOfCloneIntoWorkingCopy(Object rmiClone) {
        ClassDescriptor descriptor = getSession().getDescriptor(rmiClone);
        Object registeredObject = registerObjectForMergeCloneIntoWorkingCopy(rmiClone);

        if (registeredObject == rmiClone && !shouldForceCascade()) {
            //need to find better better fix.  prevents merging into itself.
            return rmiClone;
        }

        boolean changeTracked = false;
        try {
            ObjectBuilder builder = descriptor.getObjectBuilder();
            
            if (registeredObject != rmiClone && descriptor.usesVersionLocking() && ! mergedNewObjects.containsKey(registeredObject)) {
                VersionLockingPolicy policy = (VersionLockingPolicy) descriptor.getOptimisticLockingPolicy();
                if (policy.isStoredInObject()) {
                    Object currentValue = builder.extractValueFromObjectForField(registeredObject, policy.getWriteLockField(), session); 
                
                    if (policy.isNewerVersion(currentValue, rmiClone, session.keyFromObject(rmiClone), session)) {
                        throw OptimisticLockException.objectChangedSinceLastMerge(rmiClone);
                    }
                }
            }
            
            // Toggle change tracking during the merge.
            descriptor.getObjectChangePolicy().dissableEventProcessing(registeredObject);
            
            boolean cascadeOnly = false;
            if(registeredObject == rmiClone){
                // GF#1139 Cascade merge operations to relationship mappings even if already registered
                cascadeOnly = true;
            }
            // Merge into the clone from the original, use clone as backup as anything different should be merged.
            builder.mergeIntoObject(registeredObject, false, rmiClone, this, cascadeOnly);
        } finally {
            descriptor.getObjectChangePolicy().enableEventProcessing(registeredObject);
        }

        return registeredObject;
    }

    /**
     * Recursively merge to original from its parent into the clone.
     * The hastable is used to resolv recursion.
     */
    protected Object mergeChangesOfOriginalIntoWorkingCopy(Object clone) {
        ClassDescriptor descriptor = getSession().getDescriptor(clone);

        // Find the original object, if it is not there then do nothing.
        Object original = ((UnitOfWorkImpl)getSession()).getOriginalVersionOfObjectOrNull(clone);

        if (original == null) {
            return clone;
        }

        // Merge into the clone from the original, use clone as backup as anything different should be merged.
        descriptor.getObjectBuilder().mergeIntoObject(clone, false, original, this);

        //update the change policies with the refresh
        descriptor.getObjectChangePolicy().revertChanges(clone, descriptor, (UnitOfWorkImpl)this.getSession(), ((UnitOfWorkImpl)this.getSession()).getCloneMapping());
        Vector primaryKey = getSession().keyFromObject(clone);
        if (descriptor.usesOptimisticLocking()) {
            descriptor.getOptimisticLockingPolicy().mergeIntoParentCache((UnitOfWorkImpl)getSession(), primaryKey, clone);
        }

        CacheKey parentCacheKey = ((UnitOfWorkImpl)getSession()).getParent().getIdentityMapAccessorInstance().getCacheKeyForObject(primaryKey, clone.getClass(), descriptor);
        CacheKey uowCacheKey = getSession().getIdentityMapAccessorInstance().getCacheKeyForObject(primaryKey, clone.getClass(), descriptor);

        // Check for null because when there is NoIdentityMap, CacheKey will be null
        if ((parentCacheKey != null) && (uowCacheKey != null)) {
            uowCacheKey.setReadTime(parentCacheKey.getReadTime());
        }

        return clone;
    }

    /**
     * Recursively merge to clone into the orignal in its parent.
     * The hastable is used to resolv recursion.
     */
    protected Object mergeChangesOfWorkingCopyIntoOriginal(Object clone, ObjectChangeSet objectChangeSet) {
        UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();

        // This always finds an original different from the clone, even if it has to create one.
        // This must be done after special cases have been computed because it registers unregistered new objects.
        Object original = unitOfWork.getOriginalVersionOfObjectOrNull(clone);

        ClassDescriptor descriptor = unitOfWork.getDescriptor(clone.getClass());

        // Always merge into the original.
        try {
            if (original == null) {
                // if original does not exist then we must merge the entire object
                original = unitOfWork.buildOriginal(clone);
                if (objectChangeSet == null) {
                    descriptor.getObjectBuilder().mergeIntoObject(original, true, clone, this);
                } else if (!objectChangeSet.isNew()) {
                    //once the original is created we must put it in the cache and
                    //lock it to prevent a reading thread from creating it as well
                    //there will be no deadlock situation because no other threads
                    //will be able to reference this object.
                    AbstractSession parent = unitOfWork.getParent();
                    original = parent.getIdentityMapAccessorInstance().getWriteLockManager().appendLock(objectChangeSet.getPrimaryKeys(), original, descriptor, this, parent);
                    descriptor.getObjectBuilder().mergeIntoObject(original, true, clone, this);
                } else {
                    descriptor.getObjectBuilder().mergeChangesIntoObject(original, objectChangeSet, clone, this);
                }
            } else if (objectChangeSet == null) {
                // if we have no change set then we must merge the entire object
                descriptor.getObjectBuilder().mergeIntoObject(original, false, clone, this);
            } else {
                // not null and we have a valid changeSet then merge the changes
                if (!objectChangeSet.isNew()) {
                    AbstractSession parent = unitOfWork.getParent();
                    if(objectChangeSet.shouldInvalidateObject(original, parent)) {
                        parent.getIdentityMapAccessor().invalidateObject(original);
                        return clone;
                    }
                }
                descriptor.getObjectBuilder().mergeChangesIntoObject(original, objectChangeSet, clone, this);
            }
        } catch (QueryException exception) {
            // Ignore validation errors if unit of work validation is suppressed.
            // Also there is a very specific case under EJB wrappering where
            // a related object may have never been accessed in the unit of work context
            // but is still valid, so this error must be ignored.
            if (unitOfWork.shouldPerformNoValidation() || (descriptor.hasWrapperPolicy())) {
                if ((exception.getErrorCode() != QueryException.BACKUP_CLONE_DELETED) && (exception.getErrorCode() != QueryException.BACKUP_CLONE_IS_ORIGINAL_FROM_PARENT) && (exception.getErrorCode() != QueryException.BACKUP_CLONE_IS_ORIGINAL_FROM_SELF)) {
                    throw exception;
                }
                return clone;
            } else {
                throw exception;
            }
        }

        if (!unitOfWork.isNestedUnitOfWork()) {
            Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(clone, unitOfWork);

            // Must ensure the get and put of the cache occur as a single operation.
            // Cache key hold a reference to a concurrency manager which is used for the lock/release operation
            CacheKey cacheKey = unitOfWork.getParent().getIdentityMapAccessorInstance().acquireLock(primaryKey, original.getClass(), descriptor);
            try {
                if (descriptor.usesOptimisticLocking()) {
                    if (descriptor.getOptimisticLockingPolicy().isChildWriteLockValueGreater(unitOfWork, primaryKey, original.getClass())) {
                        cacheKey.setWriteLockValue(unitOfWork.getIdentityMapAccessor().getWriteLockValue(original));
                    }
                }

                // Always put in the parent im for root because it must now be persistent.
                cacheKey.setObject(original);
                if (descriptor.getCacheInvalidationPolicy().shouldUpdateReadTimeOnUpdate() || ((objectChangeSet != null) && objectChangeSet.isNew())) {
                    cacheKey.setReadTime(getSystemTime());
                }
            } finally {
                cacheKey.updateAccess();
                cacheKey.release();
            }
        }
        return clone;
    }

    /**
     * This can be used by the user for merging clones from RMI into the unit of work.
     */
    public void mergeCloneIntoWorkingCopy() {
        setMergePolicy(CLONE_INTO_WORKING_COPY);
    }

    /**
     * This is used during the merge of dependent objects referencing independent objects, where you want
     * the independent objects merged as well.
     */
    public void mergeCloneWithReferencesIntoWorkingCopy() {
        setMergePolicy(CLONE_WITH_REFS_INTO_WORKING_COPY);
    }

    /**
     * This is used during cache synchronisation to merge the changes into the distributed cache.
     */
    public void mergeIntoDistributedCache() {
        setMergePolicy(CHANGES_INTO_DISTRIBUTED_CACHE);
    }

    /**
     * Merge a change set for a new object into the cache.  This method will create a
     * shell for the new object and then merge the changes from the change set into the object.
     * The newly merged object will then be added to the cache.
     */
    public Object mergeNewObjectIntoCache(ObjectChangeSet changeSet) {
        if (changeSet.isNew()) {
            Class objectClass = changeSet.getClassType(session);
            ClassDescriptor descriptor = getSession().getDescriptor(objectClass);
            //Try to find the object first we may have merged it all ready
            Object object = changeSet.getTargetVersionOfSourceObject(getSession(), false);
            if (object == null) {
                if (!getObjectsAlreadyMerged().containsKey(changeSet)) {
                    // if we haven't merged this object allready then build a new object
                    // otherwise leave it as null which will stop the recursion
                    object = descriptor.getObjectBuilder().buildNewInstance();
                    //Store the changeset to prevent us from creating this new object again
                    getObjectsAlreadyMerged().put(changeSet, object);
                } else {
                    //we have all ready created the object, must be in a cyclic
                    //merge on a new object so get it out of the allreadymerged collection
                    object = getObjectsAlreadyMerged().get(changeSet);
                }
            } else {
                object = changeSet.getTargetVersionOfSourceObject(getSession(), true);
            }
            mergeChanges(object, changeSet);
            Object implementation = descriptor.getObjectBuilder().unwrapObject(object, getSession());

            return getSession().getIdentityMapAccessorInstance().putInIdentityMap(implementation, descriptor.getObjectBuilder().extractPrimaryKeyFromObject(implementation, getSession()), changeSet.getWriteLockValue(), getSystemTime(), descriptor);
        }
        return null;
    }

    /**
     * This is used to revert changes to objects, or during refreshes.
     */
    public void mergeOriginalIntoWorkingCopy() {
        setMergePolicy(ORIGINAL_INTO_WORKING_COPY);
    }

    /**
     * This is used during the unit of work commit to merge changes into the parent.
     */
    public void mergeWorkingCopyIntoBackup() {
        setMergePolicy(WORKING_COPY_INTO_BACKUP);
    }

    /**
     * This is used during the unit of work commit to merge changes into the parent.
     */
    public void mergeWorkingCopyIntoOriginal() {
        setMergePolicy(WORKING_COPY_INTO_ORIGINAL);
    }

    /**
     * This is used during the unit of work commit to merge changes into the parent.
     */
    public void mergeWorkingCopyIntoRemote() {
        setMergePolicy(WORKING_COPY_INTO_REMOTE);
    }

    /**
     * INTERNAL:
     * This is used to refresh remote session object
     */
    public void refreshRemoteObject() {
        setMergePolicy(REFRESH_REMOTE_OBJECT);
    }

    /**
     * INTERNAL:
     * When merging froma clone when the cache cannot be gaurenteed the object must be first read if it is existing
     * and not in the cache. Otherwise no changes will be detected as the original state is missing.
     */
    protected Object registerObjectForMergeCloneIntoWorkingCopy(Object clone) {
        ClassDescriptor descriptor = getSession().getDescriptor(clone.getClass());
        Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(clone, getSession());

        //Must use the java class as this may be a bean that we are merging and it may not have the same class as the
        // objects in the cache.  As of EJB 2.0
        Object objectFromCache = getSession().getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, descriptor.getJavaClass(), false, descriptor);
        if (objectFromCache != null) {
            return objectFromCache;
        }

        oracle.toplink.essentials.queryframework.DoesExistQuery existQuery = descriptor.getQueryManager().getDoesExistQuery();

        // Optimize cache option to avoid executing the does exist query.
        if (existQuery.shouldCheckCacheForDoesExist()) {
            return ((UnitOfWorkImpl)getSession()).internalRegisterObject(clone, descriptor);
        }

        // Check early return to check if it is a new object, i.e. null primary key.
        Boolean doesExist = (Boolean)existQuery.checkEarlyReturn(clone, primaryKey, getSession(), null);
        if (doesExist == Boolean.FALSE) {
            Object registeredObject = ((UnitOfWorkImpl)getSession()).internalRegisterObject(clone, descriptor);
            mergedNewObjects.put(registeredObject, registeredObject);
            return registeredObject;
        }

        // Otherwise it is existing and not in the cache so it must be read.
        Object object = getSession().readObject(clone);
        if (object == null) {
            //bug6180972: avoid internal register's existence check and be sure to put the new object in the mergedNewObjects collection
            object =  ((UnitOfWorkImpl)getSession()).cloneAndRegisterNewObject(clone);
            mergedNewObjects.put(object, object);
        }
        return object;
    }

    /**
     * Determine if the object is a registered new object, and that this is a nested unit of work
     * merge into the parent.  In this case private mappings will register the object as being removed.
     */
    public void registerRemovedNewObjectIfRequired(Object removedObject) {
        if (getSession().isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();

            if (shouldMergeWorkingCopyIntoOriginal() && unitOfWork.getParent().isUnitOfWork() && unitOfWork.isCloneNewObject(removedObject)) {
                Object originalVersionOfRemovedObject = unitOfWork.getOriginalVersionOfObject(removedObject);
                unitOfWork.addRemovedObject(originalVersionOfRemovedObject);
            }
        }
    }

    public void setCascadePolicy(int cascadePolicy) {
        this.cascadePolicy = cascadePolicy;
    }

    protected void setMergePolicy(int mergePolicy) {
        this.mergePolicy = mergePolicy;
    }

    public void setForceCascade(boolean forceCascade) {
        this.forceCascade = forceCascade;
    }

    public void setObjectDescriptors(IdentityHashtable objectDescriptors) {
        this.objectDescriptors = objectDescriptors;
    }

    protected void setObjectsAlreadyMerged(IdentityHashtable objectsAlreadyMerged) {
        this.objectsAlreadyMerged = objectsAlreadyMerged;
    }

    /**
     * INTENRAL:
     * Used to set the node that this merge manager is stored in, within the WriteLocksManager write lockers queue
     */
    public void setQueueNode(LinkedNode node) {
        this.queueNode = node;
    }

    protected void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * INTENRAL:
     * Used to set the object that the merge manager is waiting on, in order to acquire locks
     * If this value is null then the merge manager is not waiting on any locks.
     */
    public void setWriteLockQueued(CacheKey writeLockQueued) {
        this.writeLockQueued = writeLockQueued;
    }

    /**
     * Flag used to determine that the mappings should be checked for
     * cascade requirements.
     */
    public boolean shouldCascadeByMapping() {
        return getCascadePolicy() == CASCADE_BY_MAPPING;
    }

    /**
     * Flag used to determine if all parts should be cascaded
     */
    public boolean shouldCascadeAllParts() {
        return getCascadePolicy() == CASCADE_ALL_PARTS;
    }

    /**
     * Flag used to determine if any parts should be cascaded
     */
    public boolean shouldCascadeParts() {
        return getCascadePolicy() != NO_CASCADE;
    }

    /**
     * Flag used to determine if any private parts should be cascaded
     */
    public boolean shouldCascadePrivateParts() {
        return (getCascadePolicy() == CASCADE_PRIVATE_PARTS) || (getCascadePolicy() == CASCADE_ALL_PARTS);
    }

    /**
     * Refreshes are based on the objects row, so all attributes of the object must be refreshed.
     * However merging from RMI, normally reference are made transient, so should not be merge unless
     * specified.
     */
    public boolean shouldCascadeReferences() {
        return !shouldMergeCloneIntoWorkingCopy();
    }

    /**
     * INTERNAL:
     * This happens when changes from an UnitOfWork is propagated to a distributed class.
     */
    public boolean shouldMergeChangesIntoDistributedCache() {
        return getMergePolicy() == CHANGES_INTO_DISTRIBUTED_CACHE;
    }

    /**
     * This can be used by the user for merging clones from RMI into the unit of work.
     */
    public boolean shouldMergeCloneIntoWorkingCopy() {
        return getMergePolicy() == CLONE_INTO_WORKING_COPY;
    }

    /**
     * This can be used by the user for merging remote EJB objects into the unit of work.
     */
    public boolean shouldMergeCloneWithReferencesIntoWorkingCopy() {
        return getMergePolicy() == CLONE_WITH_REFS_INTO_WORKING_COPY;
    }

    /**
     * This is used to revert changes to objects, or during refreshes.
     */
    public boolean shouldMergeOriginalIntoWorkingCopy() {
        return getMergePolicy() == ORIGINAL_INTO_WORKING_COPY;
    }

    /**
     * This is used during the unit of work commit to merge changes into the parent.
     */
    public boolean shouldMergeWorkingCopyIntoBackup() {
        return getMergePolicy() == WORKING_COPY_INTO_BACKUP;
    }

    /**
     * This is used during the unit of work commit to merge changes into the parent.
     */
    public boolean shouldMergeWorkingCopyIntoOriginal() {
        return getMergePolicy() == WORKING_COPY_INTO_ORIGINAL;
    }

    /**
     * INTERNAL:
     * This happens when serialized remote unit of work has to be merged with local remote unit of work.
     */
    public boolean shouldMergeWorkingCopyIntoRemote() {
        return getMergePolicy() == WORKING_COPY_INTO_REMOTE;
    }

    /**
     * INTERNAL:
     * This is used to refresh objects on the remote session
     */
    public boolean shouldRefreshRemoteObject() {
        return getMergePolicy() == REFRESH_REMOTE_OBJECT;
    }

    /**
     * This is used to cascade merge even if a clone is already registered. 
     */
    public boolean shouldForceCascade() {
        return forceCascade;
    }
}
