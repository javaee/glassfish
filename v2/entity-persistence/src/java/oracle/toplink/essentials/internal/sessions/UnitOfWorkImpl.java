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
import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.platform.server.ServerPlatform;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sequencing.Sequencing;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.localization.LoggingLocalization;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.DescriptorEventManager;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.helper.IdentityHashtable;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;

/**
 * Implementation of oracle.toplink.essentials.sessions.UnitOfWork
 * The public interface should be used by public API and testing, the implementation should be used internally.
 * @see oracle.toplink.essentials.sessions.UnitOfWork
 *
 * <b>Purpose</b>: To allow object level transactions.
 * <p>
 * <b>Description</b>: The unit of work is a session that implements all of the normal
 * protocol of a TopLink session. It can be spawned from any other session including another unit of work.
 * Objects can be brought into the unit of work through reading them or through registering them.
 * The unit of work will opperate on its own object space, that is the objects within the unit of work
 * will be clones of the orignial objects.  When the unit of work is commited, all changes to any objects
 * registered within the unit of work will be commited to the database.  A minimal commit/update will
 * be performed and any foreign keys/circular reference/referencial integrity will be resolved.
 * If the commit to the database is successful the changed objects will be merged back into the unit of work
 * parent session.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Allow parallel transactions against a session's objects.
 * <li> Allow nested transactions.
 * <li> Not require the application to write objects that is changes, automatically determine what has changed.
 * <li> Perform a minimal commit/update of all changes that occured.
 * <li> Resolve foreign keys for newly created objects and maintain referencial integrity.
 * <li> Allow for the object transaction to use its own object space.
 * </ul>
 */
public class UnitOfWorkImpl extends AbstractSession implements oracle.toplink.essentials.sessions.UnitOfWork {

    /** Fix made for weak caches to avoid garbage collection of the originals. **/
    /** As well as used as lookup in merge algorithm for aggregates and others **/
    protected transient IdentityHashtable cloneToOriginals;
    protected transient AbstractSession parent;

    /** Hashtable of all the clones.  The key contains the clone of the object. */
    protected IdentityHashtable cloneMapping;
    protected IdentityHashtable newObjectsCloneToOriginal;
    protected IdentityHashtable newObjectsOriginalToClone;
    protected IdentityHashtable deletedObjects;

    /** This member variable contains a copy of all of the clones for this particular UOW */
    protected IdentityHashtable allClones;
    protected IdentityHashtable objectsDeletedDuringCommit;
    protected IdentityHashtable removedObjects;
    protected IdentityHashtable unregisteredNewObjects;
    protected IdentityHashtable unregisteredExistingObjects;

    protected IdentityHashtable newAggregates;

    /** This method is used to store the current changeSet for this UnitOfWork. */
    protected UnitOfWorkChangeSet unitOfWorkChangeSet;

    /** use to track pessimistic locked objects  */
    protected IdentityHashtable pessimisticLockedObjects;

    /** Used to store the list of locks that this UnitOfWork has acquired for this merge */
    protected MergeManager lastUsedMergeManager;

    /** Read-only class can be used for reference data to avoid cloning when not required. */
    protected Hashtable readOnlyClasses;

    /** Flag indicating that the transaction for this UOW was already begun. */
    protected boolean wasTransactionBegunPrematurely;

    /** Allow for double merges of new objects by putting them into the cache. */
    protected boolean shouldNewObjectsBeCached;

    /** Flag indicating that deletes should be performed before other updates. */
    protected boolean shouldPerformDeletesFirst;

    /** Flag indicating how to deal with exceptions on conforming queries. **/
    protected int shouldThrowConformExceptions;

    /** The amount of validation can be configured. */
    protected int validationLevel;
    static public final int None = 0;
    static public final int Partial = 1;
    static public final int Full = 2;

    /**
     * With the new synchronized unit of work, need a lifecycle state variable to
     * track birth, commited, pending_merge and death.
     */
    protected boolean isSynchronized;
    protected int lifecycle;
    public static final int Birth = 0;
    public static final int CommitPending = 1;

    // After a call to writeChanges() but before commit.
    public static final int CommitTransactionPending = 2;

    // After an unsuccessful call to writeChanges().  No recovery at all.
    public static final int WriteChangesFailed = 3;
    public static final int MergePending = 4;
    public static final int Death = 5;
    public static final int AfterExternalTransactionRolledBack = 6;

    /** Used for Conforming Queries */
    public static final int DO_NOT_THROW_CONFORM_EXCEPTIONS = 0;
    public static final int THROW_ALL_CONFORM_EXCEPTIONS = 1;
    
    public static final String LOCK_QUERIES_PROPERTY = "LockQueriesProperties";

    /** Used for merging dependent values without use of WL SessionAccessor */
    protected static boolean SmartMerge = false;

    /** Kept reference of read lock objects*/
    protected Hashtable optimisticReadLockObjects;

    /** lazy initialization done in storeModifyAllQuery.  For UpdateAllQuery, only clones of all UpdateAllQuery's (deferred and non-deferred) are stored here for validation only.*/
    protected List modifyAllQueries;

    /** Contains deferred ModifyAllQuery's that have translation row for execution only.  At commit their clones will be added to modifyAllQueries for validation afterwards*/
     //Bug4607551 
    protected List deferredModifyAllQueries;

    /**
     * Used during the cloning process to track the recursive depth in.  This will
     * be used to determine at which point the process can begin to wait on locks
     * without being concerned about creating deadlock situations.
     */
    protected int cloneDepth = 0;

    /**
     * This collection will be used to store those objects that are currently locked
     * for the clone process. It should be populated with an TopLinkIdentityHashMap
     */
    protected Map objectsLockedForClone;
        
    /**
     * PERF: Stores the JTA transaction to optimize activeUnitOfWork lookup.
     */
    protected Object transaction;
    




    
    /**
     * PERF: Cache the write-lock check to avoid cost of checking in every register/clone.
     */
    protected boolean shouldCheckWriteLock;

    /**
     * True if UnitOfWork should be resumed on completion of transaction.
     * Used when UnitOfWork is Synchronized with external transaction control
     */
    protected boolean resumeOnTransactionCompletion;
    
    /**
     * True if either DataModifyQuery or ModifyAllQuery was executed.
     * Gets reset on commit, effects DoesExistQuery behaviour and reading.
     */
    protected boolean wasNonObjectLevelModifyQueryExecuted;

    /**
     * True if the value holder for the joined attribute should be triggered.
     * Required by ejb30 fetch join.
     */
    protected boolean shouldCascadeCloneToJoinedRelationship;

    /**
     * INTERNAL:
     * Create and return a new unit of work with the sesson as its parent.
     */
    public UnitOfWorkImpl(AbstractSession parent) {
        super();
        this.name = parent.getName();
        this.parent = parent;
        // 2612538 - the default size of IdentityHashtable (32) is appropriate
        this.cloneMapping = new IdentityHashtable();
        // PERF: lazy-init hashtables (3286089) - cloneToOriginals,
        // newObjectsInParentOriginalToClone, objectsDeletedDuringCommit
        // removedObjects.
        this.project = parent.getProject();
        this.profiler = parent.getProfiler();
        this.isInProfile = parent.isInProfile;
        this.sessionLog = parent.getSessionLog();
        this.eventManager = parent.getEventManager().clone(this);
        this.exceptionHandler = parent.getExceptionHandler();

        // Initialize the readOnlyClasses variable.
        this.setReadOnlyClasses(parent.copyReadOnlyClasses());
        this.wasTransactionBegunPrematurely = false;
        // False by default as this may screw up things for objects with 0, -1 or other non-null default keys.
        this.shouldNewObjectsBeCached = false;
        this.validationLevel = Partial;

        this.shouldPerformDeletesFirst = false;

        // for 3.0.x this conforming queries will not throw exceptions unless explicitly asked to
        this.shouldThrowConformExceptions = DO_NOT_THROW_CONFORM_EXCEPTIONS;

        // initialize lifecycle state variable
        this.isSynchronized = false;
        this.lifecycle = Birth;
        // PERF: Cache the write-lock check to avoid cost of checking in every register/clone.
        this.shouldCheckWriteLock = parent.getDatasourceLogin().shouldSynchronizedReadOnWrite() || parent.getDatasourceLogin().shouldSynchronizeWrites();
        this.resumeOnTransactionCompletion = false;

        getEventManager().postAcquireUnitOfWork();
        incrementProfile(SessionProfiler.UowCreated);
    }

    /**
     * PUBLIC:
     * Nested units of work are not supported in TopLink Essentials.
     */
    public UnitOfWork acquireUnitOfWork() {
        throw ValidationException.notSupported("acquireUnitOfWork", getClass());
    }

    /**
     * INTERNAL:
     * Register a new aggregate object with the unit of work.
     */
    public void addNewAggregate(Object originalObject) {
        getNewAggregates().put(originalObject, originalObject);
    }

    /**
     * INTERNAL:
     * Add object deleted during root commit of unit of work.
     */
    public void addObjectDeletedDuringCommit(Object object, ClassDescriptor descriptor) {
        // The object's key is keyed on the object, this avoids having to compute the key later on.
        getObjectsDeletedDuringCommit().put(object, keyFromObject(object, descriptor));
        //bug 4730595: changed to add deleted objects to the changesets.
        ((UnitOfWorkChangeSet)getUnitOfWorkChangeSet()).addDeletedObject(object, this);
    }

    /**
     * PUBLIC:
     * Adds the given Java class to the receiver's set of read-only classes.
     * Cannot be called after objects have been registered in the unit of work.
     */
    public void addReadOnlyClass(Class theClass) throws ValidationException {
        if (!canChangeReadOnlySet()) {
            throw ValidationException.cannotModifyReadOnlyClassesSetAfterUsingUnitOfWork();
        }

        getReadOnlyClasses().put(theClass, theClass);

        ClassDescriptor descriptor = getDescriptor(theClass);

        // Also mark all subclasses as read-only.
        if (descriptor.hasInheritance()) {
            for (Enumeration childEnum = descriptor.getInheritancePolicy().getChildDescriptors().elements();
                     childEnum.hasMoreElements();) {
                ClassDescriptor childDescriptor = (ClassDescriptor)childEnum.nextElement();
                addReadOnlyClass(childDescriptor.getJavaClass());
            }
        }
    }

    /**
     * PUBLIC:
     * Adds the classes in the given Vector to the existing set of read-only classes.
     * Cannot be called after objects have been registered in the unit of work.
     */
    public void addReadOnlyClasses(Vector classes) {
        for (Enumeration enumtr = classes.elements(); enumtr.hasMoreElements();) {
            Class theClass = (Class)enumtr.nextElement();
            addReadOnlyClass(theClass);
        }
    }

    /**
     * INTERNAL:
     * Register that an object was removed in a nested unit of work.
     */
    public void addRemovedObject(Object orignal) {
        getRemovedObjects().put(orignal, orignal);// Use as set.
    }

    /**
     * ADVANCED:
     * Assign sequence number to the object.
     * This allows for an object's id to be assigned before commit.
     * It can be used if the application requires to use the object id before the object exists on the database.
     * Normally all ids are assigned during the commit automatically.
     */
    public void assignSequenceNumber(Object object) throws DatabaseException {
        //** sequencing refactoring
        startOperationProfile(SessionProfiler.AssignSequence);
        try {
            ObjectBuilder builder = getDescriptor(object).getObjectBuilder();

            // This is done outside of a transaction to ensure optimial concurrency and deadlock avoidance in the sequence table.
            if (builder.getDescriptor().usesSequenceNumbers() && !getSequencing().shouldAcquireValueAfterInsert(object.getClass())) {
                Object implementation = builder.unwrapObject(object, this);
                builder.assignSequenceNumber(implementation, this);
            }
        } catch (RuntimeException exception) {
            handleException(exception);
        }
        endOperationProfile(SessionProfiler.AssignSequence);
    }

    /**
     * ADVANCED:
     * Assign sequence numbers to all new objects registered in this unit of work,
     * or any new objects reference by any objects registered.
     * This allows for an object's id to be assigned before commit.
     * It can be used if the application requires to use the object id before the object exists on the database.
     * Normally all ids are assigned during the commit automatically.
     */
    public void assignSequenceNumbers() throws DatabaseException {
        // This should be done outside of a transaction to ensure optimal concurrency and deadlock avoidance in the sequence table.
        // discoverAllUnregisteredNewObjects() should be called no matter whether sequencing used
        // or not, because collectAndPrepareObjectsForCommit() method (which calls assignSequenceNumbers())
        // needs it.
        // It would be logical to remove discoverAllUnregisteredNewObjects() from  assignSequenceNumbers()
        // and make collectAndPrepareObjectsForCommit() to call discoverAllUnregisteredNewObjects()
        // first and assignSequenceNumbers() next,
        // but assignSequenceNumbers() is a public method which could be called by user - and
        // in this case discoverAllUnregisteredNewObjects() is needed again (though 
        // if sequencing is not used the call will make no sense - but no harm, too).
        discoverAllUnregisteredNewObjects();
        Sequencing sequencing = getSequencing();
        if (sequencing == null) {
            return;
        }
        int whenShouldAcquireValueForAll = sequencing.whenShouldAcquireValueForAll();
        if (whenShouldAcquireValueForAll == Sequencing.AFTER_INSERT) {
            return;
        }
        boolean shouldAcquireValueBeforeInsertForAll = whenShouldAcquireValueForAll == Sequencing.BEFORE_INSERT;
        startOperationProfile(SessionProfiler.AssignSequence);
        Enumeration unregisteredNewObjectsEnum = getUnregisteredNewObjects().keys();
        while (unregisteredNewObjectsEnum.hasMoreElements()) {
            Object object = unregisteredNewObjectsEnum.nextElement();
            if (getDescriptor(object).usesSequenceNumbers() && ((!isObjectRegistered(object)) || isCloneNewObject(object)) && (shouldAcquireValueBeforeInsertForAll || !sequencing.shouldAcquireValueAfterInsert(object.getClass()))) {
                getDescriptor(object).getObjectBuilder().assignSequenceNumber(object, this);
            }
        }
        Enumeration registeredNewObjectsEnum = getNewObjectsCloneToOriginal().keys();
        while (registeredNewObjectsEnum.hasMoreElements()) {
            Object object = registeredNewObjectsEnum.nextElement();
            if (getDescriptor(object).usesSequenceNumbers() && ((!isObjectRegistered(object)) || isCloneNewObject(object)) && (shouldAcquireValueBeforeInsertForAll || !sequencing.shouldAcquireValueAfterInsert(object.getClass()))) {
                getDescriptor(object).getObjectBuilder().assignSequenceNumber(object, this);
            }
        }

        endOperationProfile(SessionProfiler.AssignSequence);
    }

    /**
     * PUBLIC:
     * Tell the unit of work to begin a transaction now.
     * By default the unit of work will begin a transaction at commit time.
     * The default is the recommended approach, however sometimes it is
     * neccessary to start the transaction before commit time.  When the
     * unit of work commits, this transcation will be commited.
     *
     * @see #commit()
     * @see #release()
     */
    public void beginEarlyTransaction() throws DatabaseException {
        beginTransaction();
        setWasTransactionBegunPrematurely(true);
    }

    /**
     * INTERNAL:
     * This is internal to the uow, transactions should not be used explictly in a uow.
     * The uow shares its parents transactions.
     */
    public void beginTransaction() throws DatabaseException {
        getParent().beginTransaction();
    }

    /**
     * INTERNAL:
     * Unregistered new objects have no original so we must create one for commit and resume and
     * to put into the parent.  We can NEVER let the same copy of an object exist in multiple units of work.
     */
    public Object buildOriginal(Object workingClone) {
        ClassDescriptor descriptor = getDescriptor(workingClone);
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object original = builder.instantiateClone(workingClone, this);

        // If no original exists can mean any of the following:
        // -A RemoteUnitOfWork and cloneToOriginals is transient.
        // -A clone read while in transaction, and built directly from
        // the database row with no intermediary original.
        // -An unregistered new object
        if (checkIfAlreadyRegistered(workingClone, descriptor) != null) {
            getCloneToOriginals().put(workingClone, original);
            return original;
        } else {
            // Assume it is an unregisteredNewObject, but this is worrisome, as
            // it may be an unregistered existing object, not in the parent cache?
            Object backup = builder.instantiateClone(workingClone, this);
            
            // Original is fine for backup as state is the same.
            getCloneMapping().put(workingClone, backup);

            // Must register new instance / clone as the original.
            getNewObjectsCloneToOriginal().put(workingClone, original);
            getNewObjectsOriginalToClone().put(original, workingClone);

            // no need to register in identity map as the DatabaseQueryMechanism will have
            //placed the object in the identity map on insert.  bug 3431586
        }
        return original;
    }

    /**
     * INTERNAL:
     *<p> This Method is designed to calculate the changes for all objects
     * within the PendingObjects.
     */
    public UnitOfWorkChangeSet calculateChanges(IdentityHashtable allObjects, UnitOfWorkChangeSet changeSet) {
        getEventManager().preCalculateUnitOfWorkChangeSet();

        Enumeration objects = allObjects.elements();
        while (objects.hasMoreElements()) {
            Object object = objects.nextElement();

            //block of code removed because it will never be touched see bug # 2903565
            ClassDescriptor descriptor = getDescriptor(object);

            //Block of code removed for code coverage, as it would never have been touched. bug # 2903600
            // Use the object change policy to determine if we should run a comparison for this object - TGW
            if (descriptor.getObjectChangePolicy().shouldCompareForChange(object, this, descriptor)) {
                ObjectChangeSet changes = descriptor.getObjectChangePolicy().calculateChanges(object, getBackupClone(object), changeSet, this, descriptor, true);
                if ((changes != null) && changes.isNew()) {
                    // add it to the new list as well so we do not loose it as it may not have a valid primary key
                    // it will be moved to the standard list once it is inserted.
                    changeSet.addNewObjectChangeSet(changes, this);
                } else {
                    changeSet.addObjectChangeSet(changes);
                }
            }
        }
        
        getEventManager().postCalculateUnitOfWorkChangeSet(changeSet);
        return changeSet;
    }

    /**
     * INTERNAL:
     * Checks whether the receiver has been used. i.e. objects have been registered.
     *
     * @return true or false depending on whether the read-only set can be changed or not.
     */
    protected boolean canChangeReadOnlySet() {
        return !hasCloneMapping() && !hasDeletedObjects();
    }

    /**
     * INTERNAL:
     */
    public boolean checkForUnregisteredExistingObject(Object object) {
        ClassDescriptor descriptor = getDescriptor(object.getClass());
        Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(object, this);

        DoesExistQuery existQuery = descriptor.getQueryManager().getDoesExistQuery();

        existQuery = (DoesExistQuery)existQuery.clone();
        existQuery.setObject(object);
        existQuery.setPrimaryKey(primaryKey);
        existQuery.setDescriptor(descriptor);
        existQuery.setCheckCacheFirst(true);

        if (((Boolean)executeQuery(existQuery)).booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * INTERNAL:
     * Register the object and return the clone if it is existing otherwise return null if it is new.
     * The unit of work determines existence during registration, not during the commit.
     */
    public Object checkExistence(Object object) {
        ClassDescriptor descriptor = getDescriptor(object.getClass());
        Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(object, this);
        // PERF: null primary key cannot exist.
        if (primaryKey.contains(null)) {
            return null;
        }
        DoesExistQuery existQuery = descriptor.getQueryManager().getDoesExistQuery();

        existQuery = (DoesExistQuery)existQuery.clone();
        existQuery.setObject(object);
        existQuery.setPrimaryKey(primaryKey);
        existQuery.setDescriptor(descriptor);
        existQuery.setCheckCacheFirst(true);

        if (((Boolean)executeQuery(existQuery)).booleanValue()) {
            //we know if it exists or not, now find or register it
            Object objectFromCache = getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, object.getClass(), descriptor);
    
            if (objectFromCache != null) {
                // Ensure that the registered object is the one from the parent cache.
                if (shouldPerformFullValidation()) {
                    if ((objectFromCache != object) && (getParent().getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, object.getClass(), descriptor) != object)) {
                        throw ValidationException.wrongObjectRegistered(object, objectFromCache);
                    }
                }
    
                // Has already been cloned.
                if (!this.isObjectDeleted(objectFromCache))
                    return objectFromCache;
            }
            // This is a case where the object is not in the session cache,
            // so a new cache-key is used as there is no original to use for locking.
            return cloneAndRegisterObject(object, new CacheKey(primaryKey));
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Return the value of the object if it already is registered, otherwise null.
     */
    protected Object checkIfAlreadyRegistered(Object object, ClassDescriptor descriptor) {
        // Don't register read-only classes
        if (isClassReadOnly(object.getClass(), descriptor)) {
            return null;
        }

        // Check if the working copy is again being registered in which case we return the same working copy
        Object registeredObject = getCloneMapping().get(object);
        if (registeredObject != null) {
            return object;
        }

        // Check if object exists in my new objects if it is in the new objects cache then it means domain object is being 
        // re-registered and we should return the same working clone. This check holds only for the new registered objects 
        // PERF: Avoid initialization of new objects if none.
        if (hasNewObjects()) {
            registeredObject = getNewObjectsOriginalToClone().get(object);
            if (registeredObject != null) {
                return registeredObject;
            }
        }

        return null;
    }

    /**
     * ADVANCED:
     * Register the new object with the unit of work.
     * This will register the new object with cloning.
     * Normally the registerObject method should be used for all registration of new and existing objects.
     * This version of the register method can only be used for new objects.
     * This method should only be used if a new object is desired to be registered without an existence Check.
     *
     * @see #registerObject(Object)
     */
    public Object cloneAndRegisterNewObject(Object original) {
        ClassDescriptor descriptor = getDescriptor(original);
        ObjectBuilder builder = descriptor.getObjectBuilder();

        // bug 2612602 create the working copy object.
        Object clone = builder.instantiateWorkingCopyClone(original, this);

        // Must put in the original to clone to resolv circular refs.
        getNewObjectsOriginalToClone().put(original, clone);
        // Must put in clone mapping.
        getCloneMapping().put(clone, clone);

        builder.populateAttributesForClone(original, clone, this);

        // Must reregister in both new objects.
        registerNewObjectClone(clone, original, descriptor);

        //Build backup clone for DeferredChangeDetectionPolicy or ObjectChangeTrackingPolicy,
        //but not for AttributeChangeTrackingPolicy
        Object backupClone = descriptor.getObjectChangePolicy().buildBackupClone(clone, builder, this);
        getCloneMapping().put(clone, backupClone);// The backup clone must be updated.

        return clone;
    }

    /**
     * INTERNAL:
     * Clone and register the object.
     * The cache key must the cache key from the session cache,
     * as it will be used for locking.
     */
    public Object cloneAndRegisterObject(Object original, CacheKey cacheKey) {
        ClassDescriptor descriptor = getDescriptor(original);

        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object workingClone = builder.instantiateWorkingCopyClone(original, this);

        // The cache/objects being registered must first be locked to ensure
        // that a merge or refresh does not oocur on the object while being cloned to
        // avoid cloning a partially merged/refreshed object.
        // If a cache isolation level is used, then lock the entire cache.
        // otherwise lock the object and it related objects (not using indirection) as a unit.
        // If just a simple object (all indirection) a simple read-lock can be used.
        // PERF: Cache if check to write is required.
        boolean identityMapLocked = this.shouldCheckWriteLock && getParent().getIdentityMapAccessorInstance().acquireWriteLock();
        boolean rootOfCloneRecursion = false;
        if ((!identityMapLocked) && (this.objectsLockedForClone == null)) {//we may have locked all required objects already
            // PERF: If a simple object just acquire a simple read-lock.
            if (descriptor.shouldAcquireCascadedLocks()) {
                this.objectsLockedForClone = getParent().getIdentityMapAccessorInstance().getWriteLockManager().acquireLocksForClone(original, descriptor, cacheKey.getKey(), getParent());
            } else {
                cacheKey.acquireReadLock();
            }
            rootOfCloneRecursion = true;
        }
        try {
            // This must be registered before it is built to avoid really obscure cycles.
            getCloneMapping().put(workingClone, workingClone);

            //also clone the fetch group reference if applied
            if (descriptor.hasFetchGroupManager()) {
                descriptor.getFetchGroupManager().copyFetchGroupInto(original, workingClone);
            }

            //store this for look up later
            getCloneToOriginals().put(workingClone, original);
            // just clone it.
            populateAndRegisterObject(original, workingClone, cacheKey.getKey(), descriptor, cacheKey.getWriteLockValue(), cacheKey.getReadTime());

        } finally {
            // If the entire cache was locke, release the cache lock,
            // otherwise either release the cache-key for a simple lock,
            // otherwise release the entire set of locks for related objects if this was the root.
            if (identityMapLocked) {
                getParent().getIdentityMapAccessorInstance().releaseWriteLock();
            } else {
                if (rootOfCloneRecursion) {
                    if (this.objectsLockedForClone == null) {
                        cacheKey.releaseReadLock();
                    } else {
                        for (Iterator iterator = this.objectsLockedForClone.values().iterator();
                                 iterator.hasNext();) {
                            ((CacheKey)iterator.next()).releaseReadLock();
                        }
                        this.objectsLockedForClone = null;
                    }
                }
            }
        }
        return workingClone;
    }

    /**
     * INTERNAL:
     * Prepare for commit.
     */
    public IdentityHashtable collectAndPrepareObjectsForCommit() {
        IdentityHashtable changedObjects = new IdentityHashtable(1 + getCloneMapping().size());

        // SPECJ: Avoid for CMP.
        if (! getProject().isPureCMP2Project()) {
            assignSequenceNumbers();
        }

        //assignSequenceNumbers will collect the unregistered new objects and assign id's to all new
        // objects
        // Add any registered objects.
        for (Enumeration clonesEnum = getCloneMapping().keys(); clonesEnum.hasMoreElements();) {
            Object clone = clonesEnum.nextElement();
            changedObjects.put(clone, clone);
        }
        for (Enumeration unregisteredNewObjectsEnum = getUnregisteredNewObjects().keys();
                 unregisteredNewObjectsEnum.hasMoreElements();) {
            Object newObject = unregisteredNewObjectsEnum.nextElement();
            changedObjects.put(newObject, newObject);
        }

        return changedObjects;
    }

    /**
     * INTERNAL:
     * Prepare for merge in nested uow.
     */
    public IdentityHashtable collectAndPrepareObjectsForNestedMerge() {
        IdentityHashtable changedObjects = new IdentityHashtable(1 + getCloneMapping().size());

        discoverAllUnregisteredNewObjects();

        //assignSequenceNumbers will collect the unregistered new objects and assign id's to all new
        // objects
        // Add any registered objects.
        for (Enumeration clonesEnum = getCloneMapping().keys(); clonesEnum.hasMoreElements();) {
            Object clone = clonesEnum.nextElement();
            changedObjects.put(clone, clone);
        }
        for (Enumeration unregisteredNewObjectsEnum = getUnregisteredNewObjects().keys();
                 unregisteredNewObjectsEnum.hasMoreElements();) {
            Object newObject = unregisteredNewObjectsEnum.nextElement();
            changedObjects.put(newObject, newObject);
        }

        return changedObjects;
    }

    /**
     * PUBLIC:
     * Commit the unit of work to its parent.
     * For a nested unit of work this will merge any changes to its objects
     * with its parents.
     * For a first level unit of work it will commit all changes to its objects
     * to the database as a single transaction.  If successful the changes to its
     * objects will be merged to its parent's objects.  If the commit fails the database
     * transaction will be rolledback, and the unit of work will be released.
     * If the commit is successful the unit of work is released, and a new unit of work
     * must be acquired if further changes are desired.
     *
     * @see #commitAndResumeOnFailure()
     * @see #commitAndResume()
     * @see #release()
     */
    public void commit() throws DatabaseException, OptimisticLockException {
        //CR#2189 throwing exception if UOW try to commit again(XC)
        if (!isActive()) {
            throw ValidationException.cannotCommitUOWAgain();
        }
        if (isAfterWriteChangesFailed()) {
            throw ValidationException.unitOfWorkAfterWriteChangesFailed("commit");
        }

        if (!isNestedUnitOfWork()) {
            if (isSynchronized()) {
                // If we started the JTS transaction then we have to commit it as well.
                if (getParent().wasJTSTransactionInternallyStarted()) {
                    commitInternallyStartedExternalTransaction();
                }

                // Do not commit until the JTS wants to.
                return;
            }
        }
        if (getLifecycle() == CommitTransactionPending) {
            commitAfterWriteChanges();
            return;
        }
        log(SessionLog.FINER, SessionLog.TRANSACTION, "begin_unit_of_work_commit");// bjv - correct spelling
        getEventManager().preCommitUnitOfWork();
        setLifecycle(CommitPending);
        commitRootUnitOfWork();
        getEventManager().postCommitUnitOfWork();
        log(SessionLog.FINER, SessionLog.TRANSACTION, "end_unit_of_work_commit");
        release();
    }

    /**
     * PUBLIC:
     * Commit the unit of work to its parent.
     * For a nested unit of work this will merge any changes to its objects
     * with its parents.
     * For a first level unit of work it will commit all changes to its objects
     * to the database as a single transaction.  If successful the changes to its
     * objects will be merged to its parent's objects.  If the commit fails the database
     * transaction will be rolledback, and the unit of work will be released.
     * The normal commit releases the unit of work, forcing a new one to be acquired if further changes are desired.
     * The resuming feature allows for the same unit of work (and working copies) to be continued to be used.
     *
     * @see #commitAndResumeOnFailure()
     * @see #commit()
     * @see #release()
     */
    public void commitAndResume() throws DatabaseException, OptimisticLockException {
        //CR#2189 throwing exception if UOW try to commit again(XC)
        if (!isActive()) {
            throw ValidationException.cannotCommitUOWAgain();
        }

        if (isAfterWriteChangesFailed()) {
            throw ValidationException.unitOfWorkAfterWriteChangesFailed("commit");
        }

        if (!isNestedUnitOfWork()) {
            if (isSynchronized()) {
                // JTA synchronized units of work, cannot be resumed as there is no
                // JTA transaction to register with after the commit,
                // technically this could be supported if the uow started the transaction,
                // but currently the after completion releases the uow and client session so not really possible.
                throw ValidationException.cannotCommitAndResumeSynchronizedUOW(this);
            }
        }
        if (getLifecycle() == CommitTransactionPending) {
            commitAndResumeAfterWriteChanges();
            return;
        }
        log(SessionLog.FINER, SessionLog.TRANSACTION, "begin_unit_of_work_commit");// bjv - correct spelling
        getEventManager().preCommitUnitOfWork();
        setLifecycle(CommitPending);
        commitRootUnitOfWork();
        getEventManager().postCommitUnitOfWork();
        log(SessionLog.FINER, SessionLog.TRANSACTION, "end_unit_of_work_commit");
        
        log(SessionLog.FINER, SessionLog.TRANSACTION, "resume_unit_of_work");
        synchronizeAndResume();
        getEventManager().postResumeUnitOfWork();
    }

    /**
     * INTERNAL:
     * This method is used by the MappingWorkbench for their read-only file feature
     * this method must not be exposed to or used by customers until it has been revised
     * and the feature revisited to support OptimisticLocking and Serialization
     */
    public void commitAndResumeWithPreBuiltChangeSet(UnitOfWorkChangeSet uowChangeSet) throws DatabaseException, OptimisticLockException {
        if (!isNestedUnitOfWork()) {
            if (isSynchronized()) {
                // If we started the JTS transaction then we have to commit it as well.
                if (getParent().wasJTSTransactionInternallyStarted()) {
                    commitInternallyStartedExternalTransaction();
                }

                // Do not commit until the JTS wants to.
                return;
            }
        }
        log(SessionLog.FINER, SessionLog.TRANSACTION, "begin_unit_of_work_commit");// bjv - correct spelling
        getEventManager().preCommitUnitOfWork();
        setLifecycle(CommitPending);
        commitRootUnitOfWorkWithPreBuiltChangeSet(uowChangeSet);
        getEventManager().postCommitUnitOfWork();
        log(SessionLog.FINER, SessionLog.TRANSACTION, "end_unit_of_work_commit");
        log(SessionLog.FINER, SessionLog.TRANSACTION, "resume_unit_of_work");

        synchronizeAndResume();
        getEventManager().postResumeUnitOfWork();
    }

    /**
     * PUBLIC:
     * Commit the unit of work to its parent.
     * For a nested unit of work this will merge any changes to its objects
     * with its parents.
     * For a first level unit of work it will commit all changes to its objects
     * to the database as a single transaction.  If successful the changes to its
     * objects will be merged to its parent's objects.  If the commit fails the database
     * transaction will be rolledback, but the unit of work will remain active.
     * It can then be retried or released.
     * The normal commit failure releases the unit of work, forcing a new one to be acquired if further changes are desired.
     * The resuming feature allows for the same unit of work (and working copies) to be continued to be used if an error occurs.
     *
     * @see #commit()
     * @see #release()
     */
    public void commitAndResumeOnFailure() throws DatabaseException, OptimisticLockException {        
        // First clone the identity map, on failure replace the clone back as the cache.
        IdentityMapManager failureManager = (IdentityMapManager)getIdentityMapAccessorInstance().getIdentityMapManager().clone();
        try {
            // Call commitAndResume.
            // Oct 13, 2000 - JED PRS #13551
            // This method will always resume now. Calling commitAndResume will sync the cache 
            // if successful. This method will take care of resuming if a failure occurs
            commitAndResume();
        } catch (RuntimeException exception) {
            //reset unitOfWorkChangeSet.  Needed for ObjectChangeTrackingPolicy and DeferredChangeDetectionPolicy
            setUnitOfWorkChangeSet(null);
            getIdentityMapAccessorInstance().setIdentityMapManager(failureManager);
            log(SessionLog.FINER, SessionLog.TRANSACTION, "resuming_unit_of_work_from_failure");
            throw exception;
        }
    }

    /**
     * INTERNAL:
     * Commits a UnitOfWork where the commit process has already been
     * initiated by all call to writeChanges().
     * <p>
     * a.k.a finalizeCommit()
     */
    protected void commitAfterWriteChanges() {
        commitTransactionAfterWriteChanges();
        mergeClonesAfterCompletion();
        setDead();
        release();
    }

    /**
     * INTERNAL:
     * Commits and resumes a UnitOfWork where the commit process has already been
     * initiated by all call to writeChanges().
     * <p>
     * a.k.a finalizeCommit()
     */
    protected void commitAndResumeAfterWriteChanges() {
        commitTransactionAfterWriteChanges();
        mergeClonesAfterCompletion();
        log(SessionLog.FINER, SessionLog.TRANSACTION, "resume_unit_of_work");
        synchronizeAndResume();
        getEventManager().postResumeUnitOfWork();
    }

    /**
     * PROTECTED:
     * Used in commit and commit-like methods to commit
     * internally started external transaction
     */
    protected boolean commitInternallyStartedExternalTransaction() {
        boolean committed = false;
        if (!getParent().isInTransaction() || (wasTransactionBegunPrematurely() && (getParent().getTransactionMutex().getDepth() == 1))) {
            committed = getParent().commitExternalTransaction();
        }
        return committed;
    }

    /**
     * INTERNAL:
     * Commit the changes to any objects to the parent.
     */
    public void commitRootUnitOfWork() throws DatabaseException, OptimisticLockException {
        commitToDatabaseWithChangeSet(true);

        // Merge after commit	
        mergeChangesIntoParent();
    }

    /**
     * INTERNAL:
     * This method is used by the MappingWorkbench read-only files feature
     * It will commit a pre-built unitofwork change set to the database
     */
    public void commitRootUnitOfWorkWithPreBuiltChangeSet(UnitOfWorkChangeSet uowChangeSet) throws DatabaseException, OptimisticLockException {
        //new code no need to check old commit
        commitToDatabaseWithPreBuiltChangeSet(uowChangeSet, true);

        // Merge after commit	
        mergeChangesIntoParent();
    }

    /**
     * INTERNAL:
     * CommitChanges To The Database from a calculated changeSet
     * @param commitTransaction false if called by writeChanges as intent is
     * not to finalize the transaction.
     */
    protected void commitToDatabase(boolean commitTransaction) {
        try {
            //CR4202 - ported from 3.6.4
            if (wasTransactionBegunPrematurely()) {
                // beginTransaction() has been already called
                setWasTransactionBegunPrematurely(false);
            } else {
                beginTransaction();
            }

            if(commitTransaction) {
                setWasNonObjectLevelModifyQueryExecuted(false);
            }
            
            Vector deletedObjects = null;// PERF: Avoid deletion if nothing to delete.
            if (hasDeletedObjects()) {
                deletedObjects = new Vector(getDeletedObjects().size());
                for (Enumeration objects = getDeletedObjects().keys(); objects.hasMoreElements();) {
                    deletedObjects.addElement(objects.nextElement());
                }
            }

            if (shouldPerformDeletesFirst) {
                if (hasDeletedObjects()) {
                    // This must go to the commit manager because uow overrides to do normal deletion.
                    getCommitManager().deleteAllObjects(deletedObjects);

                    // Clear change sets of the deleted object to avoid redundant updates.
                    for (Enumeration objects = getObjectsDeletedDuringCommit().keys();
                             objects.hasMoreElements();) {
                        oracle.toplink.essentials.internal.sessions.ObjectChangeSet objectChangeSet = (oracle.toplink.essentials.internal.sessions.ObjectChangeSet)this.unitOfWorkChangeSet.getObjectChangeSetForClone(objects.nextElement());
                        if (objectChangeSet != null) {
                            objectChangeSet.clear();
                        }
                    }
                }

                // Let the commit manager figure out how to write the objects
                super.writeAllObjectsWithChangeSet(this.unitOfWorkChangeSet);
                // Issue all the SQL for the ModifyAllQuery's, don't touch the cache though
                issueModifyAllQueryList();
            } else {
                // Let the commit manager figure out how to write the objects
                super.writeAllObjectsWithChangeSet(this.unitOfWorkChangeSet);
                if (hasDeletedObjects()) {
                    // This must go to the commit manager because uow overrides to do normal deletion.
                    getCommitManager().deleteAllObjects(deletedObjects);
                }

                // Issue all the SQL for the ModifyAllQuery's, don't touch the cache though
                issueModifyAllQueryList();
            }

            // Issue prepare event.
            getEventManager().prepareUnitOfWork();

            // writeChanges() does everything but this step.
            // do not lock objects unless we are at the commit s
            if (commitTransaction) {
                try{
                    // if we should be acquiring locks before commit let's do that here 
                    if (getDatasourceLogin().shouldSynchronizeObjectLevelReadWriteDatabase()){
                        setMergeManager(new MergeManager(this));
                        //If we are merging into the shared cache acquire all required locks before merging.
                        getParent().getIdentityMapAccessorInstance().getWriteLockManager().acquireRequiredLocks(getMergeManager(), (UnitOfWorkChangeSet)getUnitOfWorkChangeSet());
                    }
                    commitTransaction();
                }catch (RuntimeException throwable){
                    if (getDatasourceLogin().shouldSynchronizeObjectLevelReadWriteDatabase() && (getMergeManager() != null)) {
                        // exception occurred durring the commit.
                        getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(getMergeManager());
                        this.setMergeManager(null);
                    }
                    throw throwable;
                }catch (Error throwable){
                    if (getDatasourceLogin().shouldSynchronizeObjectLevelReadWriteDatabase() && (getMergeManager() != null)) {
                        // exception occurred durring the commit.
                        getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(getMergeManager());
                        this.setMergeManager(null);
                    }
                    throw throwable;
                }
            }else{
                setWasTransactionBegunPrematurely(true);
            }

        } catch (RuntimeException exception) {
            rollbackTransaction(commitTransaction);
            if (hasExceptionHandler()) {
                getExceptionHandler().handleException(exception);
            } else {
                throw exception;
            }
        }
    }

    /**
     * INTERNAL:
     * Commit the changes to any objects to the parent.
     * @param commitTransaction false if called by writeChanges as intent is
     * not to finalize the transaction.
     */
    protected void commitToDatabaseWithChangeSet(boolean commitTransaction) throws DatabaseException, OptimisticLockException {
        try {
            startOperationProfile(SessionProfiler.UowCommit);
            // The sequence numbers are assigned outside of the commit transaction.
            // This improves concurrency, avoids deadlock and in the case of three-tier will
            // not leave invalid cached sequences on rollback.
            // Also must first set the commit manager active.
            getCommitManager().setIsActive(true);
            // This will assgin sequence numbers.
            IdentityHashtable allObjects = collectAndPrepareObjectsForCommit();

            // Must clone because the commitManager will remove the objects from the collection
            // as the objects are written to the database.
            setAllClonesCollection((IdentityHashtable)allObjects.clone());
            // Iterate over each clone and let the object build merge to clones into the originals.
            // The change set may already exist if using change tracking.
            if (getUnitOfWorkChangeSet() == null) {
                setUnitOfWorkChangeSet(new UnitOfWorkChangeSet());
            }
            calculateChanges(getAllClones(), (UnitOfWorkChangeSet)getUnitOfWorkChangeSet());

            // Bug 2834266 only commit to the database if changes were made, avoid begin/commit of transaction
            if (hasModifications()) {
                commitToDatabase(commitTransaction);
            } else {
                // CR#... need to commit the transaction if begun early.
                if (wasTransactionBegunPrematurely()) {
                    if (commitTransaction) {
                        // Must be set to false for release to know not to rollback.
                        setWasTransactionBegunPrematurely(false);
                        setWasNonObjectLevelModifyQueryExecuted(false);
                        commitTransaction();
                    }
                }
                getCommitManager().setIsActive(false);
            }
            endOperationProfile(SessionProfiler.UowCommit);
        } catch (RuntimeException exception) {
            handleException((RuntimeException)exception);
        }
    }

    /**
     * INTERNAL:
     * Commit pre-built changeSet to the database changest to the database.
     */
    protected void commitToDatabaseWithPreBuiltChangeSet(UnitOfWorkChangeSet uowChangeSet, boolean commitTransaction) throws DatabaseException, OptimisticLockException {
        try {
            // The sequence numbers are assigned outside of the commit transaction.
            // This improves concurrency, avoids deadlock and in the case of three-tier will
            // not leave invalid cached sequences on rollback.
            // Also must first set the commit manager active.
            getCommitManager().setIsActive(true);
            //Set empty collection in allClones for merge.
            setAllClonesCollection(new IdentityHashtable());
            // Iterate over each clone and let the object build merge to clones into the originals.
            setUnitOfWorkChangeSet(uowChangeSet);
            commitToDatabase(commitTransaction);

        } catch (RuntimeException exception) {
            handleException((RuntimeException)exception);
        }
    }

    /**
     * INTERNAL:
     * This is internal to the uow, transactions should not be used explictly in a uow.
     * The uow shares its parents transactions.
     */
    public void commitTransaction() throws DatabaseException {
        getParent().commitTransaction();
    }

    /**
     * INTERNAL:
     * After writeChanges() everything has been done except for committing
     * the transaction.  This allows that execution path to 'catch up'.
     */
    protected void commitTransactionAfterWriteChanges() {
        setWasNonObjectLevelModifyQueryExecuted(false);
        if (hasModifications() || wasTransactionBegunPrematurely()) {
             try{
                //gf934: ensuring release doesn't cause an extra rollback call if acquireRequiredLocks throws an exception
                setWasTransactionBegunPrematurely(false);
                // if we should be acquiring locks before commit let's do that here 
                if (getDatasourceLogin().shouldSynchronizeObjectLevelReadWriteDatabase() && (getUnitOfWorkChangeSet() != null)) {
                    setMergeManager(new MergeManager(this));
                    //If we are merging into the shared cache acquire all required locks before merging.
                    getParent().getIdentityMapAccessorInstance().getWriteLockManager().acquireRequiredLocks(getMergeManager(), (UnitOfWorkChangeSet)getUnitOfWorkChangeSet());
                }
                commitTransaction();
            }catch (RuntimeException exception){
                if (getDatasourceLogin().shouldSynchronizeObjectLevelReadWriteDatabase() && (getMergeManager() != null)) {
                    // exception occurred durring the commit.
                    getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(getMergeManager());
                    this.setMergeManager(null);
                }
                rollbackTransaction();
                release();
                handleException(exception);
            }catch (Error throwable){
                if (getDatasourceLogin().shouldSynchronizeObjectLevelReadWriteDatabase() && (getMergeManager() != null)) {
                    // exception occurred durring the commit.
                    getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(getMergeManager());
                    this.setMergeManager(null);
                }
                throw throwable;
            }
        }
    }

    /**
     * INTERNAL:
     * Copy the read only classes from the unit of work.
     */
    // Added Nov 8, 2000 JED for Patch 2.5.1.8, Ref: Prs 24502
    public Vector copyReadOnlyClasses() {
        return Helper.buildVectorFromHashtableElements(getReadOnlyClasses());
    }

    /**
     * PUBLIC:
     * Merge the attributes of the clone into the unit of work copy.
     * This can be used for objects that are returned from the client through
     * RMI serialization or other serialization mechanisms, because the RMI object will
     * be a clone this will merge its attributes correctly to preserve object identity
     * within the unit of work and record its changes.
     * Everything connected to this object (i.e. the entire object tree where rmiClone
     * is the root) is also merged.
     *
     * @return the registered version for the clone being merged.
     * @see #mergeClone(Object)
     * @see #shallowMergeClone(Object)
     */
    public Object deepMergeClone(Object rmiClone) {
        return mergeClone(rmiClone, MergeManager.CASCADE_ALL_PARTS);
    }

    /**
     * PUBLIC:
     * Revert the object's attributes from the parent.
     * This reverts everything the object references.
     *
     * @return the object reverted.
     * @see #revertObject(Object)
     * @see #shallowRevertObject(Object)
     */
    public Object deepRevertObject(Object clone) {
        return revertObject(clone, MergeManager.CASCADE_ALL_PARTS);
    }

    /**
     * ADVANCED:
     * Unregister the object with the unit of work.
     * This can be used to delete an object that was just created and is not yet persistent.
     * Delete object can also be used, but will result in inserting the object and then deleting it.
     * The method should be used carefully because it will delete all the reachable parts.
     */
    public void deepUnregisterObject(Object clone) {
        unregisterObject(clone, DescriptorIterator.CascadeAllParts);
    }

    /**
     * PUBLIC:
     * Delete all of the objects and all of their privately owned parts in the database.
     * Delete operations are delayed in a unit of work until commit.
     */
    public void deleteAllObjects(Vector domainObjects) {
        // This must be overriden to avoid dispatching to the commit manager.
        for (Enumeration objectsEnum = domainObjects.elements(); objectsEnum.hasMoreElements();) {
            deleteObject(objectsEnum.nextElement());
        }
    }

    /**
     * INTERNAL:
     * Search for any objects in the parent that have not been registered.
     * These are required so that the nested unit of work does not add them to the parent
     * clone mapping on commit, causing possible incorrect insertions if they are dereferenced.
     */
    protected void discoverAllUnregisteredNewObjects() {
        // 2612538 - the default size of IdentityHashtable (32) is appropriate
        IdentityHashtable visitedNodes = new IdentityHashtable();
        IdentityHashtable newObjects = new IdentityHashtable();
        IdentityHashtable existingObjects = new IdentityHashtable();

        // Iterate over each clone.
        for (Enumeration clonesEnum = getCloneMapping().keys(); clonesEnum.hasMoreElements();) {
            Object clone = clonesEnum.nextElement();
            discoverUnregisteredNewObjects(newObjects, existingObjects, visitedNodes);
        }

        setUnregisteredNewObjects(newObjects);
        setUnregisteredExistingObjects(existingObjects);
    }

    /**
     * INTERNAL:
     * Traverse the object to find references to objects not registered in this unit of work.
     */
    public void discoverUnregisteredNewObjects(IdentityHashtable knownNewObjects, IdentityHashtable unregisteredExistingObjects, IdentityHashtable visitedObjects) {
        // This define an inner class for process the itteration operation, don't be scared, its just an inner class.
        DescriptorIterator iterator = new DescriptorIterator() {
            public void iterate(Object object) {
                // If the object is read-only the do not continue the traversal.
                if (isClassReadOnly(object.getClass(), this.getCurrentDescriptor())) {
                    this.setShouldBreak(true);
                    return;
                }

                /* CR3440: Steven Vo
                 * Include the case that object is original then do nothing
                 */
                if (isSmartMerge() && isOriginalNewObject(object)) {
                    return;
                } else if (!isObjectRegistered(object)) {// Don't need to check for aggregates, as iterator does not iterate on them by default.
                    if ((shouldPerformNoValidation()) && (checkForUnregisteredExistingObject(object))) {
                        // If no validation is performed and the object exists we need
                        // To keep a record of this object to ignore it, also I need to
                        // Stop iterating over it.
                        ((IdentityHashtable)getUnregisteredExistingObjects()).put(object, object);
                        this.setShouldBreak(true);
                        return;

                    }

                    // This means it is a unregistered new object
                    ((IdentityHashtable)getResult()).put(object, object);
                }
            }
        };

        //set the collection in the UnitofWork to be this list
        setUnregisteredExistingObjects(unregisteredExistingObjects);

        iterator.setVisitedObjects(visitedObjects);
        iterator.setResult(knownNewObjects);
        iterator.setSession(this);
        // When using wrapper policy in EJB the iteration should stop on beans,
        // this is because EJB forces beans to be registered anyway and clone identity can be violated
        // and the violated clones references to session objects should not be traversed.
        iterator.setShouldIterateOverWrappedObjects(false);
        // Iterate over each clone.
        for (Enumeration clonesEnum = getCloneMapping().keys(); clonesEnum.hasMoreElements();) {
            iterator.startIterationOn(clonesEnum.nextElement());
        }
    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public void dontPerformValidation() {
        setValidationLevel(None);
    }

    /**
     * INTERNAL:
     * Override From session.  Get the accessor based on the query, and execute call,
     * this is here for session broker.
     */
    public Object executeCall(Call call, AbstractRecord translationRow, DatabaseQuery query) throws DatabaseException {
        Accessor accessor;
        if (query.getSessionName() == null) {
            accessor = query.getSession().getAccessor(query.getReferenceClass());
        } else {
            accessor = query.getSession().getAccessor(query.getSessionName());
        }

        query.setAccessor(accessor);
        try {
            return query.getAccessor().executeCall(call, translationRow, this);
        } finally {
            if (call.isFinished()) {
                query.setAccessor(null);
            }
        }
    }

    /**
     * ADVANCED:
     * Set optmistic read lock on the object.  This feature is overide by normal optimistic lock.
     * when the object is changed in UnitOfWork. The cloneFromUOW must be the clone of from this
     * UnitOfWork and it must implements version locking or timestamp locking.
     * The SQL would look like the followings.
     *
     * If shouldModifyVersionField is true,
     * "UPDATE EMPLOYEE SET VERSION = 2 WHERE EMP_ID = 9 AND VERSION = 1"
     *
     * If shouldModifyVersionField is false,
     * "UPDATE EMPLOYEE SET VERSION = 1 WHERE EMP_ID = 9 AND VERSION = 1"
     */
    public void forceUpdateToVersionField(Object lockObject, boolean shouldModifyVersionField) {
        ClassDescriptor descriptor = getDescriptor(lockObject);
        if (descriptor == null) {
            throw DescriptorException.missingDescriptor(lockObject.getClass().toString());
        }
        getOptimisticReadLockObjects().put(descriptor.getObjectBuilder().unwrapObject(lockObject, this), new Boolean(shouldModifyVersionField));
    }

    /**
     * INTERNAL:
     * The uow does not store a local accessor but shares its parents.
     */
    public Accessor getAccessor() {
        return getParent().getAccessor();
    }

    /**
     * INTERNAL:
     * The commit manager is used to resolve referncial integrity on commits of multiple objects.
     * The commit manage is lazy init from parent.
     */
    public CommitManager getCommitManager() {
        // PERF: lazy init, not always required for release/commit with no changes.
        if (commitManager == null) {
            commitManager = new CommitManager(this);
            // Initialize the commit manager
            commitManager.setCommitOrder(getParent().getCommitManager().getCommitOrder());
        }
        return commitManager;
    }

    /**
     * INTERNAL:
     * The uow does not store a local accessor but shares its parents.
     */
    public Accessor getAccessor(Class domainClass) {
        return getParent().getAccessor(domainClass);
    }

    /**
     * INTERNAL:
     * The uow does not store a local accessor but shares its parents.
     */
    public Accessor getAccessor(String sessionName) {
        return getParent().getAccessor(sessionName);
    }

    /**
     * PUBLIC:
     * Return the active unit of work for the current active external (JTS) transaction.
     * This should only be used with JTS and will return null if no external transaction exists.
     */
    public oracle.toplink.essentials.sessions.UnitOfWork getActiveUnitOfWork() {

        /* Steven Vo:  CR# 2517
           This fixed the problem of returning null when this method is called on a UOW.
           UOW does not copy the parent session's external transaction controller
           when it is acquired but session does  */
        return getParent().getActiveUnitOfWork();
    }

    /**
     * INTERNAL:
     * This method is used to get a copy of the collection of all clones in the UnitOfWork
     * @return oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    protected IdentityHashtable getAllClones() {
        return this.allClones;
    }

    /**
     * INTERNAL:
     * Return any new objects matching the expression.
     * Used for in-memory querying.
     */
    public Vector getAllFromNewObjects(Expression selectionCriteria, Class theClass, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) {
        // If new object are in the cache then they will have already been queried.
        if (shouldNewObjectsBeCached()) {
            return new Vector(1);
        }

        // PERF: Avoid initialization of new objects if none.
        if (!hasNewObjects()) {
            return new Vector(1);
        }

        Vector objects = new Vector();
        for (Enumeration newObjectsEnum = getNewObjectsOriginalToClone().elements();
                 newObjectsEnum.hasMoreElements();) {
            Object object = newObjectsEnum.nextElement();
            if (theClass.isInstance(object)) {
                if (selectionCriteria == null) {
                    objects.addElement(object);
                } else if (selectionCriteria.doesConform(object, this, translationRow, valueHolderPolicy)) {
                    objects.addElement(object);
                }
            }
        }
        return objects;
    }

    /**
     * INTERNAL:
     * Return the backup clone for the working clone.
     */
    public Object getBackupClone(Object clone) throws QueryException {
        Object backupClone = getCloneMapping().get(clone);
        if (backupClone != null) {
            return backupClone;
        }

        /* CR3440: Steven Vo
         * Smart merge if neccessary in isObjectRegistered()
         */
        if (isObjectRegistered(clone)) {
            return getCloneMapping().get(clone);

        } else {
            ClassDescriptor descriptor = getDescriptor(clone);
            Vector primaryKey = keyFromObject(clone, descriptor);

            // This happens if clone was from the parent identity map.		
            if (getParent().getIdentityMapAccessorInstance().containsObjectInIdentityMap(primaryKey, clone.getClass(), descriptor)) {
                //cr 3796
                if ((getUnregisteredNewObjects().get(clone) != null) && isMergePending()) {
                    //Another thread has read the new object before it has had a chance to
                    //merge this object.
                    // It also means it is an unregistered new object, so create a new backup clone for it.
                    return descriptor.getObjectBuilder().buildNewInstance();
                }
                if (hasObjectsDeletedDuringCommit() && getObjectsDeletedDuringCommit().containsKey(clone)) {
                    throw QueryException.backupCloneIsDeleted(clone);
                }
                throw QueryException.backupCloneIsOriginalFromParent(clone);
            }
            // Also check that the object is not the original to a registered new object
            // (the original should not be referenced if not smart merge, this is an error.	
            else if (hasNewObjects() && getNewObjectsOriginalToClone().containsKey(clone)) {

                /* CR3440: Steven Vo
                 * Check case that clone is original
                 */
                if (isSmartMerge()) {
                    backupClone = getCloneMapping().get(getNewObjectsOriginalToClone().get(clone));

                } else {
                    throw QueryException.backupCloneIsOriginalFromSelf(clone);
                }
            } else {
                // This means it is an unregistered new object, so create a new backup clone for it.
                backupClone = descriptor.getObjectBuilder().buildNewInstance();
            }
        }

        return backupClone;
    }

    /**
     * INTERNAL:
     * Return the backup clone for the working clone.
     */
    public Object getBackupCloneForCommit(Object clone) {
        Object backupClone = getBackupClone(clone);

        /* CR3440: Steven Vo
         * Build new instance only if it was not handled by getBackupClone()
         */
        if (isCloneNewObject(clone)) {
            return getDescriptor(clone).getObjectBuilder().buildNewInstance();
        }

        return backupClone;
    }

    /**
     * ADVANCED:
     * This method Will Calculate the chages for the UnitOfWork.  Without assigning sequence numbers
     * This is a Computationaly intensive operation and should be avoided unless necessary.
     * A valid changeSet, with sequencenumbers can be collected from the UnitOfWork After the commit
     * is complete by calling unitOfWork.getUnitOfWorkChangeSet()
     */
    public oracle.toplink.essentials.changesets.UnitOfWorkChangeSet getCurrentChanges() {
        IdentityHashtable allObjects = null;
        allObjects = collectAndPrepareObjectsForNestedMerge();
        return calculateChanges(allObjects, new UnitOfWorkChangeSet());
    }

    /**
     * INTERNAL:
     * Gets the next link in the chain of sessions followed by a query's check
     * early return, the chain of sessions with identity maps all the way up to
     * the root session.
     * <p>
     * Used for session broker which delegates to registered sessions, or UnitOfWork
     * which checks parent identity map also.
     * @param canReturnSelf true when method calls itself.  If the path
     * starting at <code>this</code> is acceptable.  Sometimes true if want to
     * move to the first valid session, i.e. executing on ClientSession when really
     * should be on ServerSession.
     * @param terminalOnly return the session we will execute the call on, not
     * the next step towards it.
     * @return this if there is no next link in the chain
     */
    public AbstractSession getParentIdentityMapSession(DatabaseQuery query, boolean canReturnSelf, boolean terminalOnly) {
        if (canReturnSelf && !terminalOnly) {
            return this;
        } else {
            return getParent().getParentIdentityMapSession(query, true, terminalOnly);
        }
    }

    /**
     * INTERNAL:
     * Gets the session which this query will be executed on.
     * Generally will be called immediately before the call is translated,
     * which is immediately before session.executeCall.
     * <p>
     * Since the execution session also knows the correct datasource platform
     * to execute on, it is often used in the mappings where the platform is
     * needed for type conversion, or where calls are translated.
     * <p>
     * Is also the session with the accessor.  Will return a ClientSession if
     * it is in transaction and has a write connection.
     * @return a session with a live accessor
     * @param query may store session name or reference class for brokers case
     */
    public AbstractSession getExecutionSession(DatabaseQuery query) {
        // This optimization is only for when executing with a ClientSession in
        // transaction.  In that case log with the UnitOfWork instead of the
        // ClientSession.
        // Note that if actually executing on ServerSession or a registered
        // session of a broker, must execute on that session directly.

        //bug 5201121 Always use the parent or execution session from the parent
        // should never use the unit of work as it does not controll the
        //accessors and with a sessioon broker it will not have the correct
        //login info
        return getParent().getExecutionSession(query);
    }

    /**
     * INTERNAL:
     * Return the clone mapping.
     * The clone mapping contains clone of all registered objects,
     * this is required to store the original state of the objects when registered
     * so that only what is changed will be commited to the database and the parent,
     * (this is required to support parralel unit of work).
     */
    public IdentityHashtable getCloneMapping() {
        // PERF: lazy-init (3286089)
        if (cloneMapping == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            cloneMapping = new IdentityHashtable();
        }
        return cloneMapping;
    }

    protected boolean hasCloneMapping() {
        return ((cloneMapping != null) && !cloneMapping.isEmpty());
    }

    /**
     * INTERNAL:
     * Hashtable used to avoid garbage collection in weak caches.
     * ALSO, hashtable used as lookup when originals used for merge when original in
     * identitymap can not be found.  As in a CacheIdentityMap
     */
    public IdentityHashtable getCloneToOriginals() {
        //Helper.toDo("proper fix, collection merge can have objects disapear for original.");
        if (cloneToOriginals == null) {// Must lazy initialize for remote.
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            cloneToOriginals = new IdentityHashtable();
        }
        return cloneToOriginals;
    }

    protected boolean hasCloneToOriginals() {
        return ((cloneToOriginals != null) && !cloneToOriginals.isEmpty());
    }

    /**
     * INTERNAL:
     * Return if there are any registered new objects.
     * This is used for both newObjectsOriginalToClone and newObjectsCloneToOriginal as they are always in synch.
     * PERF: Used to avoid initialization of new objects hashtable unless required.
     */
    public boolean hasNewObjects() {
        return ((newObjectsOriginalToClone != null) && !newObjectsOriginalToClone.isEmpty());
    }

    /**
     * INTERNAL: Returns the set of read-only classes that gets assigned to each newly created UnitOfWork.
     *
     * @see oracle.toplink.essentials.sessions.Project#setDefaultReadOnlyClasses(Vector)
     */
    public Vector getDefaultReadOnlyClasses() {
        return getParent().getDefaultReadOnlyClasses();
    }

    /**
     * INTERNAL:
     * The deleted objects stores any objects removed during the unit of work.
     * On commit they will all be removed from the database.
     */
    public IdentityHashtable getDeletedObjects() {
        if (deletedObjects == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            deletedObjects = new IdentityHashtable();
        }
        return deletedObjects;
    }

    protected boolean hasDeletedObjects() {
        return ((deletedObjects != null) && !deletedObjects.isEmpty());
    }

    /**
     * PUBLIC:
     * Return the descriptor for  the alias.
     * UnitOfWork delegates this to the parent
     * Introduced because of Bug#2610803
     */
    public ClassDescriptor getDescriptorForAlias(String alias) {
        return getParent().getDescriptorForAlias(alias);
    }

    /**
     * PUBLIC:
     * Return all registered descriptors.
     * The unit of work inherits its parent's descriptors. The each descriptor's Java Class
     * is used as the key in the Hashtable returned.
     */
    public Map getDescriptors() {
        return getParent().getDescriptors();
    }

    /**
     * INTERNAL:
     * The life cycle tracks if the unit of work is active and is used for JTS.
     */
    public int getLifecycle() {
        return lifecycle;
    }

    /**
     * A reference to the last used merge manager.  This is used to track locked
     * objects.
     */
    public MergeManager getMergeManager() {
        return this.lastUsedMergeManager;
    }

    /**
     * INTERNAL:
     * The hashtable stores any new aggregates that have been cloned.
     */
    public IdentityHashtable getNewAggregates() {
        if (this.newAggregates == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            this.newAggregates = new IdentityHashtable();
        }
        return newAggregates;
    }

    /**
     * INTERNAL:
     * The new objects stores any objects newly created during the unit of work.
     * On commit they will all be inserted into the database.
     */
    public synchronized IdentityHashtable getNewObjectsCloneToOriginal() {
        if (newObjectsCloneToOriginal == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            newObjectsCloneToOriginal = new IdentityHashtable();
        }
        return newObjectsCloneToOriginal;
    }

    /**
     * INTERNAL:
     * The new objects stores any objects newly created during the unit of work.
     * On commit they will all be inserted into the database.
     */
    public synchronized IdentityHashtable getNewObjectsOriginalToClone() {
        if (newObjectsOriginalToClone == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            newObjectsOriginalToClone = new IdentityHashtable();
        }
        return newObjectsOriginalToClone;
    }

    /**
     * INTERNAL:
     * Return the Sequencing object used by the session.
     */
    public Sequencing getSequencing() {
        return getParent().getSequencing();
    }

    /**
     * INTERNAL:
     * Marked internal as this is not customer API but helper methods for
     * accessing the server platform from within TopLink's other sessions types
     * (ie not DatabaseSession)
     */
    public ServerPlatform getServerPlatform(){
        return getParent().getServerPlatform();
    }

    /**
     * INTERNAL:
     * Returns the type of session, its class.
     * <p>
     * Override to hide from the user when they are using an internal subclass
     * of a known class.
     * <p>
     * A user does not need to know that their UnitOfWork is a
     * non-deferred UnitOfWork, or that their ClientSession is an
     * IsolatedClientSession.
     */
    public String getSessionTypeString() {
        return "UnitOfWork";
    }

    /**
     * INTERNAL:
     * Called after transaction is completed (committed or rolled back)
     */
    public void afterTransaction(boolean committed, boolean isExternalTransaction) {
        if (!committed && isExternalTransaction) {
            // In case jts transaction was internally started but rolled back
            // directly by TransactionManager this flag may still be true during afterCompletion
            getParent().setWasJTSTransactionInternallyStarted(false);
            //bug#4699614 -- added a new life cycle status so we know if the external transaction was rolledback and we don't try to rollback again later            
            setLifecycle(AfterExternalTransactionRolledBack);
        }
        if ((getMergeManager() != null) && (getMergeManager().getAcquiredLocks() != null) && (!getMergeManager().getAcquiredLocks().isEmpty())) {
            //may have unreleased cache locks because of a rollback...
            getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(getMergeManager());
            this.setMergeManager(null);
        }
        getParent().afterTransaction(committed, isExternalTransaction);
    }

    /**
     * INTERNAL:
     * Return any new object matching the expression.
     * Used for in-memory querying.
     */
    public Object getObjectFromNewObjects(Class theClass, Vector selectionKey) {
        // PERF: Avoid initialization of new objects if none.
        if (!hasNewObjects()) {
            return null;
        }
        ObjectBuilder objectBuilder = getDescriptor(theClass).getObjectBuilder();
        for (Enumeration newObjectsEnum = getNewObjectsOriginalToClone().elements();
                 newObjectsEnum.hasMoreElements();) {
            Object object = newObjectsEnum.nextElement();
            if (theClass.isInstance(object)) {
                // removed dead null check as this method is never called if selectionKey == null
                Vector primaryKey = objectBuilder.extractPrimaryKeyFromObject(object, this);
                if (new CacheKey(primaryKey).equals(new CacheKey(selectionKey))) {
                    return object;
                }
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Return any new object matching the expression.
     * Used for in-memory querying.
     */
    public Object getObjectFromNewObjects(Expression selectionCriteria, Class theClass, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) {
        // PERF: Avoid initialization of new objects if none.
        if (!hasNewObjects()) {
            return null;
        }
        for (Enumeration newObjectsEnum = getNewObjectsOriginalToClone().elements();
                 newObjectsEnum.hasMoreElements();) {
            Object object = newObjectsEnum.nextElement();
            if (theClass.isInstance(object)) {
                if (selectionCriteria == null) {
                    return object;
                }
                if (selectionCriteria.doesConform(object, this, translationRow, valueHolderPolicy)) {
                    return object;
                }
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Returns all the objects which are deleted during root commit of unit of work.
     */
    public IdentityHashtable getObjectsDeletedDuringCommit() {
        // PERF: lazy-init (3286089)
        if (objectsDeletedDuringCommit == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            objectsDeletedDuringCommit = new IdentityHashtable();
        }
        return objectsDeletedDuringCommit;
    }

    protected boolean hasObjectsDeletedDuringCommit() {
        return ((objectsDeletedDuringCommit != null) && !objectsDeletedDuringCommit.isEmpty());
    }

    /**
     * INTERNAL:
     * Return optimistic read lock objects
     */
    public Hashtable getOptimisticReadLockObjects() {
        if (optimisticReadLockObjects == null) {
            optimisticReadLockObjects = new Hashtable(2);
        }
        return optimisticReadLockObjects;
    }

    /**
     * INTERNAL:
     * Return the original version of the new object (working clone).
     */
    public Object getOriginalVersionOfNewObject(Object workingClone) {
        // PERF: Avoid initialization of new objects if none.
        if (!hasNewObjects()) {
            return null;
        }
        return getNewObjectsCloneToOriginal().get(workingClone);
    }

    /**
     * ADVANCED:
     * Return the original version of the object(clone) from the parent's identity map.
     */
    public Object getOriginalVersionOfObject(Object workingClone) {
        // Can be null when called from the mappings.
        if (workingClone == null) {
            return null;
        }
        ClassDescriptor descriptor = getDescriptor(workingClone);
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(workingClone, this);

        Vector primaryKey = builder.extractPrimaryKeyFromObject(implementation, this);
        Object original = getParent().getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, implementation.getClass(), descriptor);

        if (original == null) {
            // Check if it is a registered new object.
            original = getOriginalVersionOfNewObject(implementation);
        }

        if (original == null) {
            // For bug 3013948 looking in the cloneToOriginals mapping will not help
            // if the object was never registered.
            if (isClassReadOnly(implementation.getClass(), descriptor)) {
                return implementation;
            }

            // The object could have been removed from the cache even though it was in the unit of work.
            // fix for 2.5.1.3 PWK (1360)
            if (hasCloneToOriginals()) {
                original = getCloneToOriginals().get(workingClone);
            }
        }

        if (original == null) {
            // This means that it must be an unregistered new object, so register a new clone as its original.
            original = buildOriginal(implementation);
        }

        return original;
    }

    /**
     * ADVANCED:
     * Return the original version of the object(clone) from the parent's identity map.
     */
    public Object getOriginalVersionOfObjectOrNull(Object workingClone) {
        // Can be null when called from the mappings.
        if (workingClone == null) {
            return null;
        }
        ClassDescriptor descriptor = getDescriptor(workingClone);
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(workingClone, this);

        Vector primaryKey = builder.extractPrimaryKeyFromObject(implementation, this);
        Object original = getParent().getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, implementation.getClass(), descriptor);

        if (original == null) {
            // Check if it is a registered new object.
            original = getOriginalVersionOfNewObject(implementation);
        }

        if (original == null) {
            // For bug 3013948 looking in the cloneToOriginals mapping will not help
            // if the object was never registered.
            if (isClassReadOnly(implementation.getClass(), descriptor)) {
                return implementation;
            }

            // The object could have been removed from the cache even though it was in the unit of work.
            // fix for 2.5.1.3 PWK (1360)
            if (hasCloneToOriginals()) {
                original = getCloneToOriginals().get(workingClone);
            }
        }
        return original;
    }

    /**
     * PUBLIC:
     * Return the parent.
     * This is a unit of work if nested, otherwise a database session or client session.
     */
    public AbstractSession getParent() {
        return parent;
    }

    /**
     * INTERNAL:
     * Return the platform for a particular class.
     */
    public Platform getPlatform(Class domainClass) {
        return getParent().getPlatform(domainClass);
    }
    
    /**
     * Search for and return the user defined property from this UOW, if it not found then search for the property
     * from parent.
     */
    public Object getProperty(String name){
        Object propertyValue = super.getProperties().get(name);
        if (propertyValue == null) {
           propertyValue = getParent().getProperty(name);
        }
        return propertyValue;
    }
    
    /**
     * INTERNAL:
     * Return whether to throw exceptions on conforming queries
     */
    public int getShouldThrowConformExceptions() {
        return shouldThrowConformExceptions;
    }

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public DatabaseQuery getQuery(String name, Vector arguments) {
        DatabaseQuery query = super.getQuery(name, arguments);
        if (query == null) {
            query = getParent().getQuery(name, arguments);
        }

        return query;
    }

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public DatabaseQuery getQuery(String name) {
        DatabaseQuery query = super.getQuery(name);
        if (query == null) {
            query = getParent().getQuery(name);
        }

        return query;
    }

    /**
     * INTERNAL:
     * Returns the set of read-only classes for the receiver.
     * Use this method with setReadOnlyClasses() to modify a UnitOfWork's set of read-only
     * classes before using the UnitOfWork.
     * @return Hashtable containing the Java Classes that are currently read-only.
     * @see #setReadOnlyClasses(Vector)
     */
    public Hashtable getReadOnlyClasses() {
        return readOnlyClasses;
    }

    /**
     * INTERNAL:
     * The removed objects stores any newly registered objects removed during the nested unit of work.
     * On commit they will all be removed from the parent unit of work.
     */
    protected IdentityHashtable getRemovedObjects() {
        // PERF: lazy-init (3286089)
        if (removedObjects == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            removedObjects = new IdentityHashtable();
        }
        return removedObjects;
    }

    protected boolean hasRemovedObjects() {
        return ((removedObjects != null) && !removedObjects.isEmpty());
    }

    protected boolean hasModifyAllQueries() {
        return ((modifyAllQueries != null) && !modifyAllQueries.isEmpty());
    }

    protected boolean hasDeferredModifyAllQueries() {
        return ((deferredModifyAllQueries != null) && !deferredModifyAllQueries.isEmpty());
    }

    /**
     * INTERNAL:
     * Find out what the lifecycle state of this UoW is in.
     */
    public int getState() {
        return lifecycle;
    }
    
    /**
     * INTERNAL:
     * PERF: Return the associated external transaction.
     * Used to optimize activeUnitOfWork lookup.
     */
    public Object getTransaction() {
        return transaction;
    }
    
    /**
     * INTERNAL:
     * PERF: Set the associated external transaction.
     * Used to optimize activeUnitOfWork lookup.
     */
    public void setTransaction(Object transaction) {
        this.transaction = transaction;
    }

    /**
     * ADVANCED:
     * Returns the currentChangeSet from the UnitOfWork.
     * This is only valid after the UnitOfWOrk has commited successfully
     */
    public oracle.toplink.essentials.changesets.UnitOfWorkChangeSet getUnitOfWorkChangeSet() {
        return unitOfWorkChangeSet;
    }

    /**
     * INTERNAL:
     * Used to lazy Initialize the unregistered existing Objects collection.
     * @return oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    public oracle.toplink.essentials.internal.helper.IdentityHashtable getUnregisteredExistingObjects() {
        if (this.unregisteredExistingObjects == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            this.unregisteredExistingObjects = new IdentityHashtable();
        }
        return unregisteredExistingObjects;
    }

    /**
     * INTERNAL:
     * This is used to store unregistred objects discovered in the parent so that the child
     * unit of work knows not to register them on commit.
     */
    protected IdentityHashtable getUnregisteredNewObjects() {
        if (unregisteredNewObjects == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            unregisteredNewObjects = new IdentityHashtable();
        }
        return unregisteredNewObjects;
    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public int getValidationLevel() {
        return validationLevel;
    }

    /**
     * ADVANCED:
     * The Unit of work is capable of preprocessing to determine if any on the clone have been changed.
     * This is computationaly expensive and should be avoided on large object graphs.
     */
    public boolean hasChanges() {
        if (hasNewObjects()) {
            return true;
        }
        IdentityHashtable allObjects = collectAndPrepareObjectsForNestedMerge();

        //Using the nested merge prevent the UnitOfWork from assigning sequence numbers
        if (!getUnregisteredNewObjects().isEmpty()) {
            return true;
        }
        if (hasDeletedObjects()) {
            return true;
        }
        UnitOfWorkChangeSet changeSet = calculateChanges(allObjects, new UnitOfWorkChangeSet());
        return changeSet.hasChanges();
    }

    /**
     * INTERNAL:
     * Does this unit of work have any changes or anything that requires a write
     * to the database and a transaction to be started.
     * Should be called after changes are calculated internally by commit.
     * <p>
     * Note if a transaction was begun prematurely it still needs to be committed.
     */
    protected boolean hasModifications() {
        if (getUnitOfWorkChangeSet().hasChanges() || hasDeletedObjects() || hasModifyAllQueries() || hasDeferredModifyAllQueries() || ((oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet)getUnitOfWorkChangeSet()).hasForcedChanges()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * INTERNAL:
     * Set up the IdentityMapManager.  This method allows subclasses of Session to override
     * the default IdentityMapManager functionality.
     */
    public void initializeIdentityMapAccessor() {
        this.identityMapAccessor = new UnitOfWorkIdentityMapAccessor(this, new IdentityMapManager(this));
    }

    /**
     * INTERNAL:
     * Return the results from exeucting the database query.
     * the arguments should be a database row with raw data values.
     */
    public Object internalExecuteQuery(DatabaseQuery query, AbstractRecord databaseRow) throws DatabaseException, QueryException {
        if (!isActive()) {
            throw QueryException.querySentToInactiveUnitOfWork(query);
        }
        return query.executeInUnitOfWork(this, databaseRow);
    }

    /**
     * INTERNAL:
     * Register the object with the unit of work.
     * This does not perform wrapping or unwrapping.
     * This is used for internal registration in the merge manager.
     */
    public Object internalRegisterObject(Object object, ClassDescriptor descriptor) {
        if (object == null) {
            return null;
        }
        if (descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
            throw ValidationException.cannotRegisterAggregateObjectInUnitOfWork(object.getClass());
        }
        Object registeredObject = checkIfAlreadyRegistered(object, descriptor);
        if (registeredObject == null) {
            registeredObject = checkExistence(object);
            if (registeredObject == null) {
                // This means that the object is not in the parent im, so was created under this unit of work.
                // This means that it must be new.
                registeredObject = cloneAndRegisterNewObject(object);
            }
        }
        return registeredObject;
    }

    /**
     * PUBLIC:
     * Return if the unit of work is active. (i.e. has not been released).
     */
    public boolean isActive() {
        return !isDead();
    }

    /**
     * PUBLIC:
     * Checks to see if the specified class or descriptor is read-only or not in this UnitOfWork.
     *
     * @return boolean, true if the class is read-only, false otherwise.
     */
    public boolean isClassReadOnly(Class theClass, ClassDescriptor descriptor) {
        if ((descriptor != null) && (descriptor.shouldBeReadOnly())) {
            return true;
        }
        if ((theClass != null) && getReadOnlyClasses().containsKey(theClass)) {
            return true;
        }
        return false;
    }

    /**
     * INTERNAL:
     * Check if the object is already registered.
     */
    public boolean isCloneNewObject(Object clone) {
        // PERF: Avoid initialization of new objects if none.
        if (!hasNewObjects()) {
            return false;
        }
        return getNewObjectsCloneToOriginal().containsKey(clone);
    }

    /**
     * INTERNAL:
     * Return if the unit of work is waiting to be committed or in the process of being committed.
     */
    public boolean isCommitPending() {
        return getLifecycle() == CommitPending;
    }

    /**
     * INTERNAL:
     * Return if the unit of work is dead.
     */
    public boolean isDead() {
        return getLifecycle() == Death;
    }

    /**
     * PUBLIC:
     * Return whether the session currently has a database transaction in progress.
     */
    public boolean isInTransaction() {
        return getParent().isInTransaction();
    }

    /**
     * INTERNAL:
     * Return if the unit of work is waiting to be merged or in the process of being merged.
     */
    public boolean isMergePending() {
        return getLifecycle() == MergePending;
    }

    /**
     * INTERNAL:
     * Has writeChanges() been attempted on this UnitOfWork?  It may have
     * either suceeded or failed but either way the UnitOfWork is in a highly
     * restricted state.
     */
    public boolean isAfterWriteChangesButBeforeCommit() {
        return ((getLifecycle() == CommitTransactionPending) || (getLifecycle() == WriteChangesFailed));
    }

    /**
     * INTERNAL:
     * Once writeChanges has failed all a user can do really is rollback.
     */
    protected boolean isAfterWriteChangesFailed() {
        return getLifecycle() == WriteChangesFailed;
    }

    /**
     * PUBLIC:
     * Return whether this session is a nested unit of work or not.
     */
    public boolean isNestedUnitOfWork() {
        return false;
    }

    /**
     * INTERNAL:
     * Return if the object has been deleted in this unit of work.
     */
    public boolean isObjectDeleted(Object object) {
        boolean isDeleted = false;
        if (hasDeletedObjects()) {
            isDeleted = getDeletedObjects().containsKey(object);
        }
        if (getParent().isUnitOfWork()) {
            return isDeleted || ((UnitOfWorkImpl)getParent()).isObjectDeleted(object);
        } else {
            return isDeleted;
        }
    }

    /**
     * INTERNAL:
     * This method is used to determine if the clone is a new Object in the UnitOfWork
     */
    public boolean isObjectNew(Object clone) {
        //CR3678 - ported from 4.0
        return (isCloneNewObject(clone) || (!isObjectRegistered(clone) && !getReadOnlyClasses().contains(clone.getClass()) && !getUnregisteredExistingObjects().contains(clone)));
    }

    /**
     * INTERNAL:
     * Return whether the clone object is already registered.
     */
    public boolean isObjectRegistered(Object clone) {
        if (getCloneMapping().containsKey(clone)) {
            return true;
        }

        // We do smart merge here 
        if (isSmartMerge()){
            ClassDescriptor descriptor = getDescriptor(clone);
            if (getParent().getIdentityMapAccessorInstance().containsObjectInIdentityMap(keyFromObject(clone, descriptor), clone.getClass(), descriptor) ) {
                mergeCloneWithReferences(clone);

                // don't put clone in  clone mapping since it would result in duplicate clone
                return true;
            }
        }
        return false;
    }

    /**
     * INTERNAL:
     * Return whether the original object is new.
     * It was either registered as new or discovered as a new aggregate
     * within another new object.
     */
    public boolean isOriginalNewObject(Object original) {
        return (hasNewObjects() && getNewObjectsOriginalToClone().containsKey(original)) || getNewAggregates().containsKey(original);
    }

    /**
     * INTERNAL:
     * Return the status of smart merge
     */
    public static boolean isSmartMerge() {
        return SmartMerge;
    }

    /**
     * INTERNAL:
     * For synchronized units of work, dump SQL to database.
     * For cases where writes occur before the end of the transaction don't commit
     */
    public void issueSQLbeforeCompletion() {
        issueSQLbeforeCompletion(true);
    }
    
    /**
     * INTERNAL:
     * For synchronized units of work, dump SQL to database.
     * For cases where writes occur before the end of the transaction don't commit
     */
    public void issueSQLbeforeCompletion(boolean commitTransaction) {
        if (getLifecycle() == CommitTransactionPending) {
            commitTransactionAfterWriteChanges();
            return;
        }
        // CR#... call event and log.
        log(SessionLog.FINER, SessionLog.TRANSACTION, "begin_unit_of_work_commit");
        getEventManager().preCommitUnitOfWork();
        setLifecycle(CommitPending);
        commitToDatabaseWithChangeSet(commitTransaction);
    }

    /**
       * INTERNAL:
       * Will notify all the deferred ModifyAllQuery's (excluding UpdateAllQuery's) and deferred UpdateAllQuery's to execute.
       */
    protected void issueModifyAllQueryList() {
        if (deferredModifyAllQueries != null) {
            for (int i = 0; i < deferredModifyAllQueries.size(); i++) {
                Object[] queries = (Object[])deferredModifyAllQueries.get(i);
                ModifyAllQuery query = (ModifyAllQuery)queries[0];
                AbstractRecord translationRow = (AbstractRecord)queries[1];
                getParent().executeQuery(query, translationRow);
            }
        }
    }

    /**
     * INTERNAL:
     * Return if this session is a synchronized unit of work.
     */
    public boolean isSynchronized() {
        return isSynchronized;
    }

    /**
     * PUBLIC:
     * Return if this session is a unit of work.
     */
    public boolean isUnitOfWork() {
        return true;
    }

    /**
     * INTERNAL: Merge the changes to all objects to the parent.
     */
    protected void mergeChangesIntoParent() {
        UnitOfWorkChangeSet uowChangeSet = (UnitOfWorkChangeSet)getUnitOfWorkChangeSet();
        if (uowChangeSet == null) {
            // may be using the old commit prosess usesOldCommit()
            setUnitOfWorkChangeSet(new UnitOfWorkChangeSet());
            uowChangeSet = (UnitOfWorkChangeSet)getUnitOfWorkChangeSet();
            calculateChanges(getAllClones(), (UnitOfWorkChangeSet)getUnitOfWorkChangeSet());
        }

        // 3286123 - if no work to be done, skip this part of uow.commit()
        if (hasModifications()) {
            setPendingMerge();
            startOperationProfile(SessionProfiler.Merge);
            // Ensure concurrency if cache isolation requires.
            getParent().getIdentityMapAccessorInstance().acquireWriteLock();
            MergeManager manager = getMergeManager();
            if (manager == null){
                // no MergeManager created for locks durring commit
                manager = new MergeManager(this);
            }
             
            try {
                if (!isNestedUnitOfWork()) {
                    preMergeChanges();
                }

                // Must clone the clone mapping because entries can be added to it during the merging,
                // and that can lead to concurrency problems.
                getParent().getEventManager().preMergeUnitOfWorkChangeSet(uowChangeSet);
                if (!isNestedUnitOfWork() && getDatasourceLogin().shouldSynchronizeObjectLevelReadWrite()) {
                    setMergeManager(manager);
                    //If we are merging into the shared cache acquire all required locks before merging.
                    getParent().getIdentityMapAccessorInstance().getWriteLockManager().acquireRequiredLocks(getMergeManager(), (UnitOfWorkChangeSet)getUnitOfWorkChangeSet());
                }
                Enumeration changeSetLists = ((UnitOfWorkChangeSet)getUnitOfWorkChangeSet()).getObjectChanges().elements();
                while (changeSetLists.hasMoreElements()) {
                    Hashtable objectChangesList = (Hashtable)((Hashtable)changeSetLists.nextElement()).clone();
                    if (objectChangesList != null) {// may be no changes for that class type.
                        for (Enumeration pendingEnum = objectChangesList.elements();
                                 pendingEnum.hasMoreElements();) {
                            ObjectChangeSet changeSetToWrite = (ObjectChangeSet)pendingEnum.nextElement();
                            if (changeSetToWrite.hasChanges()) {
                                Object objectToWrite = changeSetToWrite.getUnitOfWorkClone();

                                //bug#4154455 -- only merge into the shared cache if the object is new or if it already exists in the shared cache
                                if (changeSetToWrite.isNew() || (getOriginalVersionOfObjectOrNull(objectToWrite) != null)) {
                                    manager.mergeChanges(objectToWrite, changeSetToWrite);
                                }
                            } else {
                                // if no 'real' changes to the object change set, remove it from the
                                // list so it won't be unnecessarily sent via cache sync.
                                uowChangeSet.removeObjectChangeSet(changeSetToWrite);
                            }
                        }
                    }
                }

                // Notify the queries to merge into the shared cache
                if (modifyAllQueries != null) {
                    for (int i = 0; i < modifyAllQueries.size(); i++) {
                        ModifyAllQuery query = (ModifyAllQuery)modifyAllQueries.get(i);
                        query.setSession(getParent());// ensure the query knows which cache to update
                        query.mergeChangesIntoSharedCache();
                    }
                }

                if (isNestedUnitOfWork()) {
                    changeSetLists = ((UnitOfWorkChangeSet)getUnitOfWorkChangeSet()).getNewObjectChangeSets().elements();
                    while (changeSetLists.hasMoreElements()) {
                        IdentityHashtable objectChangesList = (IdentityHashtable)((IdentityHashtable)changeSetLists.nextElement()).clone();
                        if (objectChangesList != null) {// may be no changes for that class type.
                            for (Enumeration pendingEnum = objectChangesList.elements();
                                     pendingEnum.hasMoreElements();) {
                                ObjectChangeSet changeSetToWrite = (ObjectChangeSet)pendingEnum.nextElement();
                                if (changeSetToWrite.hasChanges()) {
                                    Object objectToWrite = changeSetToWrite.getUnitOfWorkClone();
                                    manager.mergeChanges(objectToWrite, changeSetToWrite);
                                } else {
                                    // if no 'real' changes to the object change set, remove it from the
                                    // list so it won't be unnecessarily sent via cache sync.
                                    uowChangeSet.removeObjectChangeSet(changeSetToWrite);
                                }
                            }
                        }
                    }
                }
                if (!isNestedUnitOfWork()) {
                    //If we are merging into the shared cache release all of the locks that we acquired.
                    getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(manager);
                    setMergeManager(null);

                    postMergeChanges();
                }
            } finally {
                if (!isNestedUnitOfWork() && !manager.getAcquiredLocks().isEmpty()) {
                    // if the locks have not already been released (!acquiredLocks.empty)
                    // then there must have been an error, release all of the locks.
                    getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(manager);
                    setMergeManager(null);
                }
                getParent().getIdentityMapAccessorInstance().releaseWriteLock();
                getParent().getEventManager().postMergeUnitOfWorkChangeSet(uowChangeSet);
                endOperationProfile(SessionProfiler.Merge);
            }
        }
    }

    /**
     * PUBLIC:
     * Merge the attributes of the clone into the unit of work copy.
     * This can be used for objects that are returned from the client through
     * RMI serialization (or another serialization mechanism), because the RMI object
     * will be a clone this will merge its attributes correctly to preserve object
     * identity within the unit of work and record its changes.
     *
     * The object and its private owned parts are merged.
     *
     * @return the registered version for the clone being merged.
     * @see #shallowMergeClone(Object)
     * @see #deepMergeClone(Object)
     */
    public Object mergeClone(Object rmiClone) {
        return mergeClone(rmiClone, MergeManager.CASCADE_PRIVATE_PARTS);
    }

    /**
     * INTERNAL:
     * Merge the attributes of the clone into the unit of work copy.
     */
    public Object mergeClone(Object rmiClone, int cascadeDepth) {
        if (rmiClone == null) {
            return null;
        }

        //CR#2272
        logDebugMessage(rmiClone, "merge_clone");

        startOperationProfile(SessionProfiler.Merge);
        ObjectBuilder builder = getDescriptor(rmiClone).getObjectBuilder();
        Object implementation = builder.unwrapObject(rmiClone, this);

        MergeManager manager = new MergeManager(this);
        manager.mergeCloneIntoWorkingCopy();
        manager.setCascadePolicy(cascadeDepth);

        Object merged = null;
        try {
            merged = manager.mergeChanges(implementation, null);
        } catch (RuntimeException exception) {
            merged = handleException(exception);
        }
        endOperationProfile(SessionProfiler.Merge);

        return merged;
    }

    /**
     * INTERNAL:
     * for synchronized units of work, merge changes into parent
     */
    public void mergeClonesAfterCompletion() {
        mergeChangesIntoParent();
        // CR#... call event and log.
        getEventManager().postCommitUnitOfWork();
        log(SessionLog.FINER, SessionLog.TRANSACTION, "end_unit_of_work_commit");
    }

    /**
     * PUBLIC:
     * Merge the attributes of the clone into the unit of work copy.
     * This can be used for objects that are returned from the client through
     * RMI serialization (or another serialization mechanism), because the RMI object
     * will be a clone this will merge its attributes correctly to preserve object
     * identity within the unit of work and record its changes.
     *
     * The object and its private owned parts are merged. This will include references from
     * dependent objects to independent objects.
     *
     * @return the registered version for the clone being merged.
     * @see #shallowMergeClone(Object)
     * @see #deepMergeClone(Object)
     */
    public Object mergeCloneWithReferences(Object rmiClone) {
        return this.mergeCloneWithReferences(rmiClone, MergeManager.CASCADE_PRIVATE_PARTS);
    }
    

    /**
     * PUBLIC:
     * Merge the attributes of the clone into the unit of work copy.
     * This can be used for objects that are returned from the client through
     * RMI serialization (or another serialization mechanism), because the RMI object
     * will be a clone this will merge its attributes correctly to preserve object
     * identity within the unit of work and record its changes.
     *
     * The object and its private owned parts are merged. This will include references from
     * dependent objects to independent objects.
     *
     * @return the registered version for the clone being merged.
     * @see #shallowMergeClone(Object)
     * @see #deepMergeClone(Object)
     */
    public Object mergeCloneWithReferences(Object rmiClone, int cascadePolicy) {
        return mergeCloneWithReferences(rmiClone, cascadePolicy, false);
    }
    
    /**
     * INTERNAL:
     * Merge the attributes of the clone into the unit of work copy.
     * This can be used for objects that are returned from the client through
     * RMI serialization (or another serialization mechanism), because the RMI object
     * will be a clone this will merge its attributes correctly to preserve object
     * identity within the unit of work and record its changes.
     *
     * The object and its private owned parts are merged. This will include references from
     * dependent objects to independent objects.
     *
     * @return the registered version for the clone being merged.
     * @see #shallowMergeClone(Object)
     * @see #deepMergeClone(Object)
     */
    public Object mergeCloneWithReferences(Object rmiClone, int cascadePolicy, boolean forceCascade) {
       if (rmiClone == null) {
            return null;
        }
        ClassDescriptor descriptor = getDescriptor(rmiClone);
        if ((descriptor == null) || descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
            if (cascadePolicy == MergeManager.CASCADE_BY_MAPPING){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[]{rmiClone}));
            }
            return rmiClone;
        }

        //CR#2272
        logDebugMessage(rmiClone, "merge_clone_with_references");

        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(rmiClone, this);

        MergeManager manager = new MergeManager(this);
        manager.mergeCloneWithReferencesIntoWorkingCopy();
        manager.setCascadePolicy(cascadePolicy);
        manager.setForceCascade(forceCascade);
        Object mergedObject = manager.mergeChanges(implementation, null);
        if (isSmartMerge()) {
            return builder.wrapObject(mergedObject, this);
        } else {
            return mergedObject;
        }
    }

    /**
     * PUBLIC:
     * Return a new instance of the class registered in this unit of work.
     * This can be used to ensure that new objects are registered correctly.
     */
    public Object newInstance(Class theClass) {
        //CR#2272
        logDebugMessage(theClass, "new_instance");

        ClassDescriptor descriptor = getDescriptor(theClass);
        Object newObject = descriptor.getObjectBuilder().buildNewInstance();
        return registerObject(newObject);
    }

    /**
     * INTERNAL:
     * This method will perform a delete operation on the provided objects pre-determing
     * the objects that will be deleted by a commit of the UnitOfWork including privately
     * owned objects.  It does not execute a query for the deletion of these objects as the 
     * normal deleteobject operation does.  Mainly implemented to provide EJB 3.0 deleteObject
     * support.
     */
    public void performRemove(Object toBeDeleted, IdentityHashtable visitedObjects){
        try {
            if (toBeDeleted == null) {
                return;
            }
            ClassDescriptor descriptor = getDescriptor(toBeDeleted);
            if ((descriptor == null) || descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[]{toBeDeleted}));
            }
            logDebugMessage(toBeDeleted, "deleting_object");

            startOperationProfile(SessionProfiler.DeletedObject);
            //bug 4568370+4599010; fix EntityManager.remove() to handle new objects
            if (getDeletedObjects().contains(toBeDeleted)){
              return;
            }
            visitedObjects.put(toBeDeleted,toBeDeleted);
            Object registeredObject = checkIfAlreadyRegistered(toBeDeleted, descriptor);
            if (registeredObject == null) {
                Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(toBeDeleted, this);
                DoesExistQuery existQuery = descriptor.getQueryManager().getDoesExistQuery();
                existQuery = (DoesExistQuery)existQuery.clone();
                existQuery.setObject(toBeDeleted);
                existQuery.setPrimaryKey(primaryKey);
                existQuery.setDescriptor(descriptor);
        
                existQuery.setCheckCacheFirst(true);
                if (((Boolean)executeQuery(existQuery)).booleanValue()){
                    throw new IllegalArgumentException(ExceptionLocalization.buildMessage("cannot_remove_detatched_entity", new Object[]{toBeDeleted}));
                }//else, it is a new or previously deleted object that should be ignored (and delete should cascade)
            }else{
                //fire events only if this is a managed object
                if (descriptor.getEventManager().hasAnyEventListeners()) {
                    oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(toBeDeleted);
                    event.setEventCode(DescriptorEventManager.PreRemoveEvent);
                    event.setSession(this);
                    descriptor.getEventManager().executeEvent(event);
                }
                if (hasNewObjects() && getNewObjectsOriginalToClone().contains(registeredObject)){
                    unregisterObject(registeredObject, DescriptorIterator.NoCascading);
                }else{
                    getDeletedObjects().put(toBeDeleted, toBeDeleted);
                }
            }
            descriptor.getObjectBuilder().cascadePerformRemove(toBeDeleted, this, visitedObjects);
        } finally {
            endOperationProfile(SessionProfiler.DeletedObject);
        }
    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public void performFullValidation() {
        setValidationLevel(Full);

    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public void performPartialValidation() {
        setValidationLevel(Partial);
    }

    /**
     * INTERNAL:
     * This method is called from clone and register.  It includes the processing
     * required to clone an object, including populating attributes, putting in
     * UOW identitymap and building a backupclone
     */
    protected void populateAndRegisterObject(Object original, Object workingClone, Vector primaryKey, ClassDescriptor descriptor, Object writeLockValue, long readTime) {
        // This must be registered before it is built to avoid cycles.
        getIdentityMapAccessorInstance().putInIdentityMap(workingClone, primaryKey, writeLockValue, readTime, descriptor);

        //Set ChangeListener for ObjectChangeTrackingPolicy and AttributeChangeTrackingPolicy,
        //but not DeferredChangeDetectionPolicy.  Build backup clone for DeferredChangeDetectionPolicy 
        //or ObjectChangeTrackingPolicy, but not for AttributeChangeTrackingPolicy.  
        // - Set listener before populating attributes so aggregates can find the parent's listener
        descriptor.getObjectChangePolicy().setChangeListener(workingClone, this, descriptor);
        descriptor.getObjectChangePolicy().dissableEventProcessing(workingClone);

        ObjectBuilder builder = descriptor.getObjectBuilder();
        builder.populateAttributesForClone(original, workingClone, this);
        Object backupClone = descriptor.getObjectChangePolicy().buildBackupClone(workingClone, builder, this);
        getCloneMapping().put(workingClone, backupClone);
        
        descriptor.getObjectChangePolicy().enableEventProcessing(workingClone);
    }

    /**
     * INTERNAL:
     * Remove objects from parent's identity map.
     */
    protected void postMergeChanges() {
        //bug 4730595: objects removed during flush are not removed from the cache during commit
        if (!this.getUnitOfWorkChangeSet().getDeletedObjects().isEmpty()){
            oracle.toplink.essentials.internal.helper.IdentityHashtable deletedObjects = this.getUnitOfWorkChangeSet().getDeletedObjects();
            for (Enumeration removedObjects = deletedObjects.keys(); removedObjects.hasMoreElements(); ) {
                ObjectChangeSet removedObjectChangeSet = (ObjectChangeSet) removedObjects.nextElement();
                java.util.Vector primaryKeys = removedObjectChangeSet.getPrimaryKeys();
                getParent().getIdentityMapAccessor().removeFromIdentityMap(primaryKeys, removedObjectChangeSet.getClassType(this));
            }
        }
    }

    /**
     * INTERNAL:
     * Remove objects deleted during commit from clone and new object cache so that these are not merged
     */
    protected void preMergeChanges() {
        if (hasObjectsDeletedDuringCommit()) {
            for (Enumeration removedObjects = getObjectsDeletedDuringCommit().keys();
                     removedObjects.hasMoreElements();) {
                Object removedObject = removedObjects.nextElement();
                getCloneMapping().remove(removedObject);
                getAllClones().remove(removedObject);
                // PERF: Avoid initialization of new objects if none.
                if (hasNewObjects()) {
                    Object referenceObjectToRemove = getNewObjectsCloneToOriginal().get(removedObject);
                    if (referenceObjectToRemove != null) {
                        getNewObjectsCloneToOriginal().remove(removedObject);
                        getNewObjectsOriginalToClone().remove(referenceObjectToRemove);
                    }
                }
            }
        }
    }

    /**
     * PUBLIC:
     * Print the objects in the unit of work.
     * The output of this method will be logged to this unit of work's SessionLog at SEVERE level.
     */
    public void printRegisteredObjects() {
        if (shouldLog(SessionLog.SEVERE, SessionLog.CACHE)) {
            basicPrintRegisteredObjects();
        }
    }

    /**
     * INTERNAL:
     * This method is used to process delete queries that pass through the unitOfWork
     * It is extracted out of the internalExecuteQuery method to reduce duplication
     */
    public Object processDeleteObjectQuery(DeleteObjectQuery deleteQuery) {
        // We must ensure that we delete the clone not the original, (this can happen in the mappings update)
        if (deleteQuery.getObject() == null) {// Must validate.
            throw QueryException.objectToModifyNotSpecified(deleteQuery);
        }

        ClassDescriptor descriptor = getDescriptor(deleteQuery.getObject());
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(deleteQuery.getObject(), this);

        if (isClassReadOnly(implementation.getClass(), descriptor)) {
            throw QueryException.cannotDeleteReadOnlyObject(implementation);
        }

        if (isCloneNewObject(implementation)) {
            unregisterObject(implementation);
            return implementation;
        }
        Vector primaryKey = builder.extractPrimaryKeyFromObject(implementation, this);
        Object clone = getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, implementation.getClass(), descriptor);
        if (clone == null) {
            clone = implementation;
        }

        // Register will wrap so must unwrap again.
        clone = builder.unwrapObject(clone, this);

        deleteQuery.setObject(clone);
        if (!getCommitManager().isActive()) {
            getDeletedObjects().put(clone, primaryKey);
            return clone;
        } else {
            // If the object has already been deleted i.e. private-owned + deleted then don't do it twice.
            if (hasObjectsDeletedDuringCommit()) {
                if (getObjectsDeletedDuringCommit().containsKey(clone)) {
                    return clone;
                }
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Print the objects in the unit of work.
     */
    protected void basicPrintRegisteredObjects() {
        String cr = Helper.cr();
        StringWriter writer = new StringWriter();
        writer.write(LoggingLocalization.buildMessage("unitofwork_identity_hashcode", new Object[] { cr, String.valueOf(System.identityHashCode(this)) }));
        if (hasDeletedObjects()) {
            writer.write(cr + LoggingLocalization.buildMessage("deleted_objects"));
            for (Enumeration enumtr = getDeletedObjects().keys(); enumtr.hasMoreElements();) {
                Object object = enumtr.nextElement();
                writer.write(LoggingLocalization.buildMessage("key_identity_hash_code_object", new Object[] { cr, Helper.printVector(getDescriptor(object).getObjectBuilder().extractPrimaryKeyFromObject(object, this)), "\t", String.valueOf(System.identityHashCode(object)), object }));
            }
        }
        writer.write(cr + LoggingLocalization.buildMessage("all_registered_clones"));
        for (Enumeration enumtr = getCloneMapping().keys(); enumtr.hasMoreElements();) {
            Object object = enumtr.nextElement();
            writer.write(LoggingLocalization.buildMessage("key_identity_hash_code_object", new Object[] { cr, Helper.printVector(getDescriptor(object).getObjectBuilder().extractPrimaryKeyFromObject(object, this)), "\t", String.valueOf(System.identityHashCode(object)), object }));
        }
        log(SessionLog.SEVERE, SessionLog.TRANSACTION, writer.toString(), null, null, false);
    }

    /**
     * PUBLIC:
     * Register the objects with the unit of work.
     * All newly created root domain objects must be registered to be inserted on commit.
     * Also any existing objects that will be edited and were not read from this unit of work
     * must also be registered.
     * Once registered any changes to the objects will be commited to the database on commit.
     *
     * @return is the clones of the original objects, the return value must be used for editing.
     * Editing the original is not allowed in the unit of work.
     */
    public Vector registerAllObjects(Collection domainObjects) {
        Vector clones = new Vector(domainObjects.size());
        for (Iterator objectsEnum = domainObjects.iterator(); objectsEnum.hasNext();) {
            clones.addElement(registerObject(objectsEnum.next()));
        }
        return clones;
    }

    /**
     * PUBLIC:
     * Register the objects with the unit of work.
     * All newly created root domain objects must be registered to be inserted on commit.
     * Also any existing objects that will be edited and were not read from this unit of work
     * must also be registered.
     * Once registered any changes to the objects will be commited to the database on commit.
     *
     * @return is the clones of the original objects, the return value must be used for editing.
     * Editing the original is not allowed in the unit of work.
     */
    public Vector registerAllObjects(Vector domainObjects) throws DatabaseException, OptimisticLockException {
        Vector clones = new Vector(domainObjects.size());
        for (Enumeration objectsEnum = domainObjects.elements(); objectsEnum.hasMoreElements();) {
            clones.addElement(registerObject(objectsEnum.nextElement()));
        }
        return clones;
    }

    /**
     * ADVANCED:
     * Register the existing object with the unit of work.
     * This is a advanced API that can be used if the application can guarentee the object exists on the database.
     * When registerObject is called the unit of work determines existence through the descriptor's doesExist setting.
     *
     * @return The clone of the original object, the return value must be used for editing.
     * Editing the original is not allowed in the unit of work.
     */
    public synchronized Object registerExistingObject(Object existingObject) {
        if (existingObject == null) {
            return null;
        }
        ClassDescriptor descriptor = getDescriptor(existingObject);
        if (descriptor == null) {
            throw DescriptorException.missingDescriptor(existingObject.getClass().toString());
        }
        if (this.isClassReadOnly(descriptor.getJavaClass(), descriptor)) {
            return existingObject;
        }

        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(existingObject, this);
        Object registeredObject = this.registerExistingObject(implementation, descriptor);

        // Bug # 3212057 - workaround JVM bug (MWN)
        if (implementation != existingObject) {
            return builder.wrapObject(registeredObject, this);
        } else {
            return registeredObject;
        }
    }

    /**
     * INTERNAL:
     * Register the existing object with the unit of work.
     * This is a advanced API that can be used if the application can guarentee the object exists on the database.
     * When registerObject is called the unit of work determines existence through the descriptor's doesExist setting.
     *
     * @return The clone of the original object, the return value must be used for editing.
     * Editing the original is not allowed in the unit of work.
     */
    protected synchronized Object registerExistingObject(Object objectToRegister, ClassDescriptor descriptor) {
        if (isAfterWriteChangesButBeforeCommit()) {
            throw ValidationException.illegalOperationForUnitOfWorkLifecycle(getLifecycle(), "registerExistingObject");
        }
        if (descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
            throw ValidationException.cannotRegisterAggregateObjectInUnitOfWork(objectToRegister.getClass());
        }
        //CR#2272
        logDebugMessage(objectToRegister, "register_existing");
        Object registeredObject;
        try {
            startOperationProfile(SessionProfiler.Register);
            registeredObject = checkIfAlreadyRegistered(objectToRegister, descriptor);
            if (registeredObject == null) {
                // Check if object is existing, if it is it must be cloned into the unit of work
                // otherwise it is a new object
                Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(objectToRegister, this);

                // Always check the cache first.
                registeredObject = getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, objectToRegister.getClass(), descriptor);

                if (registeredObject == null) {
                    // This is a case where the object is not in the session cache,
                    // so a new cache-key is used as there is no original to use for locking.
                    registeredObject = cloneAndRegisterObject(objectToRegister, new CacheKey(primaryKey));
                }
            }
            
            //bug3659327
            //fetch group manager control fetch group support
            if (descriptor.hasFetchGroupManager()) {
                //if the object is already registered in uow, but it's partially fetched (fetch group case)	
                if (descriptor.getFetchGroupManager().shouldWriteInto(objectToRegister, registeredObject)) {
                    //there might be cases when reverting/refreshing clone is needed.
                    descriptor.getFetchGroupManager().writePartialIntoClones(objectToRegister, registeredObject, this);
                }
            }
        } finally {
            endOperationProfile(SessionProfiler.Register);
        }
        return registeredObject;
    }

    /**
     * ADVANCED:
     * Register the new object with the unit of work.
     * This will register the new object without cloning.
     * Normally the registerObject method should be used for all registration of new and existing objects.
     * This version of the register method can only be used for new objects.
     * This method should only be used if a new object is desired to be registered without cloning.
     *
     * @see #registerObject(Object)
     */
    public synchronized Object registerNewObject(Object newObject) {
        if (newObject == null) {
            return null;
        }
        ClassDescriptor descriptor = getDescriptor(newObject);
        if (descriptor == null) {
            throw DescriptorException.missingDescriptor(newObject.getClass().toString());
        }

        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(newObject, this);

        this.registerNewObject(implementation, descriptor);

        if (implementation == newObject) {
            return newObject;
        } else {
            return builder.wrapObject(implementation, this);
        }
    }

    /**
     * INTERNAL:
     * Updated to allow passing in of the object's descriptor
     *
     * Register the new object with the unit of work.
     * This will register the new object without cloning.
     * Normally the registerObject method should be used for all registration of new and existing objects.
     * This version of the register method can only be used for new objects.
     * This method should only be used if a new object is desired to be registered without cloning.
     *
     * @see #registerObject(Object)
     */
    protected synchronized Object registerNewObject(Object implementation, ClassDescriptor descriptor) {
        if (isAfterWriteChangesButBeforeCommit()) {
            throw ValidationException.illegalOperationForUnitOfWorkLifecycle(getLifecycle(), "registerNewObject");
        }
        if (descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
            throw ValidationException.cannotRegisterAggregateObjectInUnitOfWork(implementation.getClass());
        }
        try {
            //CR#2272
            logDebugMessage(implementation, "register_new");

            startOperationProfile(SessionProfiler.Register);
            Object registeredObject = checkIfAlreadyRegistered(implementation, descriptor);
            if (registeredObject == null) {
                // Ensure that the registered object is the one from the parent cache.
                if (shouldPerformFullValidation()) {
                    Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(implementation, this);
                    Object objectFromCache = getParent().getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, implementation.getClass(), descriptor);
                    if (objectFromCache != null) {
                        throw ValidationException.wrongObjectRegistered(implementation, objectFromCache);
                    }
                }
                ObjectBuilder builder = descriptor.getObjectBuilder();
                Object original = builder.buildNewInstance();

                registerNewObjectClone(implementation, original, descriptor);
                Object backupClone = builder.buildNewInstance();
                getCloneMapping().put(implementation, backupClone);

                // Check if the new objects should be cached.
                registerNewObjectInIdentityMap(implementation, implementation);
            }
        } finally {
            endOperationProfile(SessionProfiler.Register);
        }

        //as this is register new return the object passed in.
        return implementation;
    }

    /**
     * INTERNAL:
     *
     * Register the new object with the unit of work.
     * This will register the new object without cloning.
     * Checks based on existence will be completed and the create will be cascaded based on the
     * object's mappings cascade requirements.  This is specific to EJB 3.0 support and is
     * @see #registerObject(Object)
     */
    public synchronized void registerNewObjectForPersist(Object newObject, IdentityHashtable visitedObjects) {
        try {
            if (newObject == null) {
                return;
            }
            if(visitedObjects.containsKey(newObject)) {
                return;
            }
            visitedObjects.put(newObject, newObject);
            ClassDescriptor descriptor = getDescriptor(newObject);
            if ((descriptor == null) || descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[]{newObject}));
            }
            
            startOperationProfile(SessionProfiler.Register);          
            
            Object registeredObject = checkIfAlreadyRegistered(newObject, descriptor);
            
            if (registeredObject == null) {
                registerNotRegisteredNewObjectForPersist(newObject, descriptor);
            } else if (this.isObjectDeleted(newObject)){
                //if object is deleted and a create is issued on the that object
                // then the object must be transitioned back to existing and not deleted
                this.undeleteObject(newObject);
            }
            descriptor.getObjectBuilder().cascadeRegisterNewForCreate(newObject, this, visitedObjects);
        } finally {
            endOperationProfile(SessionProfiler.Register);
        }
    }

    /**
     * INTERNAL:
     * Called only by registerNewObjectForPersist method,
     * and only if newObject is not already registered.
     * Could be overridden in subclasses.
     */
    protected void registerNotRegisteredNewObjectForPersist(Object newObject, ClassDescriptor descriptor) {
        // Ensure that the registered object is not detached.
        newObject.getClass();

        DoesExistQuery existQuery = descriptor.getQueryManager().getDoesExistQuery();
        existQuery = (DoesExistQuery)existQuery.clone();
        existQuery.setObject(newObject);
        existQuery.setDescriptor(descriptor);
        // only check the cache as we can wait until commit for the unique 
        // constraint error to be thrown.  This does ignore user's settings
        // on descriptor but calling persist() tells us the object is new.
        existQuery.checkCacheForDoesExist(); 
        if (((Boolean)executeQuery(existQuery)).booleanValue()) {
            throw ValidationException.cannotPersistExistingObject(newObject, this);
        }
        
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object original = builder.buildNewInstance();

        registerNewObjectClone(newObject, original, descriptor);
        Object backupClone = builder.buildNewInstance();
        getCloneMapping().put(newObject, backupClone);
        assignSequenceNumber(newObject);

        // Check if the new objects should be cached.
        registerNewObjectInIdentityMap(newObject, newObject);
    }
    
    /**
     * INTERNAL:
     * Register the working copy of a new object and its original.
     * The user must edit the working copy and the original is used to merge into the parent.
     * This mapping is kept both ways because lookup is required in both directions.
     */
    protected void registerNewObjectClone(Object clone, Object original, ClassDescriptor descriptor) {
        // Check if the new objects should be cached.
        registerNewObjectInIdentityMap(clone, original);

        getNewObjectsCloneToOriginal().put(clone, original);
        getNewObjectsOriginalToClone().put(original, clone);
        
        // run prePersist callbacks if any
        logDebugMessage(clone, "register_new_for_persist");
        
        if (descriptor.getEventManager().hasAnyEventListeners()) {
            oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(clone);
            event.setEventCode(DescriptorEventManager.PrePersistEvent);
            event.setSession(this);
            descriptor.getEventManager().executeEvent(event);
        }  
    }

    /**
     * INTERNAL:
     * Add the new object to the cache if set to.
     * This is useful for using mergeclone on new objects.
     */
    protected void registerNewObjectInIdentityMap(Object clone, Object original) {
        // CR 2728 Added check for sequencing to allow zero primitives for id's if the client
        //is not using sequencing.
        Class cls = clone.getClass();
        ClassDescriptor descriptor = getDescriptor(cls);
        boolean usesSequences = descriptor.usesSequenceNumbers();
        if (shouldNewObjectsBeCached()) {
            // Also put it in the cache if it has a valid primary key, this allows for double new object merges
            Vector key = keyFromObject(clone, descriptor);
            boolean containsNull = false;

            // begin CR#2041 Unit Of Work incorrectly put new objects with a primitive primary key in its cache
            Object pkElement;
            for (int index = 0; index < key.size(); index++) {
                pkElement = key.elementAt(index);
                if (pkElement == null) {
                    containsNull = true;
                } else if (usesSequences) {
                    containsNull = containsNull || getSequencing().shouldOverrideExistingValue(cls, pkElement);
                }
            }

            // end cr #2041
            if (!containsNull) {
                getIdentityMapAccessorInstance().putInIdentityMap(clone, key, null, 0, descriptor);
            }
        }
    }

    /**
     * PUBLIC:
     * Register the object with the unit of work.
     * All newly created root domain objects must be registered to be inserted on commit.
     * Also any existing objects that will be edited and were not read from this unit of work
     * must also be registered.
     * Once registered any changes to the objects will be commited to the database on commit.
     *
     * @return the clone of the original object, the return value must be used for editing,
     *
     * ** Editing the original is not allowed in the unit of work. **
     */
    public synchronized Object registerObject(Object object) {
        if (object == null) {
            return null;
        }
        ClassDescriptor descriptor = getDescriptor(object);
        if (descriptor == null) {
            throw DescriptorException.missingDescriptor(object.getClass().toString());
        }
        if (this.isClassReadOnly(descriptor.getJavaClass(), descriptor)) {
            return object;
        }
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(object, this);
        boolean wasWrapped = implementation != object;
        Object registeredObject = this.registerObject(implementation, descriptor);
        if (wasWrapped) {
            return builder.wrapObject(registeredObject, this);
        } else {
            return registeredObject;
        }
    }

    /**
     * INTERNAL:
     * Allows for calling method to provide the descriptor information for this
     * object.  Prevents double lookup of descriptor.
     *
     *
     * Register the object with the unit of work.
     * All newly created root domain objects must be registered to be inserted on commit.
     * Also any existing objects that will be edited and were not read from this unit of work
     * must also be registered.
     * Once registered any changes to the objects will be commited to the database on commit.
     *
     * calling this method will also sort the objects into different different groups
     * depending on if the object being registered is a bean or a regular Java
     * object and if its updates are deferred, non-deferred or if all modifications
     * are deferred.
     *
     * @return the clone of the original object, the return value must be used for editing,
     */
    protected synchronized Object registerObject(Object object, ClassDescriptor descriptor) {
        if (this.isClassReadOnly(descriptor.getJavaClass(), descriptor)) {
            return object;
        }
        if (isAfterWriteChangesButBeforeCommit()) {
            throw ValidationException.illegalOperationForUnitOfWorkLifecycle(getLifecycle(), "registerObject");
        }

        //CR#2272
        logDebugMessage(object, "register");

        Object registeredObject;
        try {
            startOperationProfile(SessionProfiler.Register);

            registeredObject = internalRegisterObject(object, descriptor);

        } finally {
            endOperationProfile(SessionProfiler.Register);
        }
        return registeredObject;
    }

    /**
     * INTERNAL:
     * Register this UnitOfWork against an external transaction controller
     */
    public void registerWithTransactionIfRequired() {
        if (getParent().hasExternalTransactionController() && ! isSynchronized()) {
            boolean hasAlreadyStarted = getParent().wasJTSTransactionInternallyStarted();
            getParent().getExternalTransactionController().registerSynchronizationListener(this, getParent());

            // CR#2998 - registerSynchronizationListener may toggle the wasJTSTransactionInternallyStarted 
            // flag. As a result, we must compare the states and if the state is changed, then we must set the
            // setWasTransactionBegunPrematurely flag to ensure that we handle the transaction depth count 
            // appropriately
            if (!hasAlreadyStarted && getParent().wasJTSTransactionInternallyStarted()) {
                // registerSynchronizationListener caused beginTransaction() called
                // and an external transaction internally started.
                this.setWasTransactionBegunPrematurely(true);
            }
        }
    }

    /**
     * PUBLIC:
     * Release the unit of work. This terminates this unit of work.
     * Because the unit of work operates on its own object space (clones) no work is required.
     * The unit of work should no longer be used or referenced by the application beyond this point
     * so that it can be garbage collected.
     *
     * @see #commit()
     */
    public void release() {
        log(SessionLog.FINER, SessionLog.TRANSACTION, "release_unit_of_work");
        getEventManager().preReleaseUnitOfWork();

        // If already succeeded at a writeChanges(), then transaction still open.
        // As already issued sql must at least mark the external transaction for rollback only.
        if (getLifecycle() == CommitTransactionPending) {
            if (hasModifications() || wasTransactionBegunPrematurely()) {
                rollbackTransaction(false);
                setWasTransactionBegunPrematurely(false);
            }
        } else if (wasTransactionBegunPrematurely() && (!isNestedUnitOfWork())) {
            rollbackTransaction();
            setWasTransactionBegunPrematurely(false);
        }
        if ((getMergeManager() != null) && (getMergeManager().getAcquiredLocks() != null) && (!getMergeManager().getAcquiredLocks().isEmpty())) {
            //may have unreleased cache locks because of a rollback...  As some 
            //locks may be acquired durring commit.
            getParent().getIdentityMapAccessorInstance().getWriteLockManager().releaseAllAcquiredLocks(getMergeManager());
            this.setMergeManager(null);
        }
        setDead();
        if(shouldClearForCloseOnRelease()) {
            clearForClose(true);
        }
        getParent().releaseUnitOfWork(this);
        getEventManager().postReleaseUnitOfWork();
    }

    /**
     * PUBLIC:
     * Empties the set of read-only classes.
     * It is illegal to call this method on nested UnitOfWork objects. A nested UnitOfWork
     * cannot have a subset of its parent's set of read-only classes.
     * Also removes classes which are read only because their descriptors are readonly
     */
    public void removeAllReadOnlyClasses() throws ValidationException {
        if (isNestedUnitOfWork()) {
            throw ValidationException.cannotRemoveFromReadOnlyClassesInNestedUnitOfWork();
        }
        getReadOnlyClasses().clear();
    }

    /**
     * ADVANCED:
     * Remove optimistic read lock from the object
     * See forceUpdateToVersionField(Object)
     */
    public void removeForceUpdateToVersionField(Object lockObject) {
        getOptimisticReadLockObjects().remove(lockObject);
    }

    /**
     * PUBLIC:
     * Removes a Class from the receiver's set of read-only classes.
     * It is illegal to try to send this method to a nested UnitOfWork.
     */
    public void removeReadOnlyClass(Class theClass) throws ValidationException {
        if (!canChangeReadOnlySet()) {
            throw ValidationException.cannotModifyReadOnlyClassesSetAfterUsingUnitOfWork();
        }
        if (isNestedUnitOfWork()) {
            throw ValidationException.cannotRemoveFromReadOnlyClassesInNestedUnitOfWork();
        }
        getReadOnlyClasses().remove(theClass);

    }

    /**
     * INTERNAL:
     * Used in the resume to reset the all clones collection
     */
    protected void resetAllCloneCollection() {
        this.allClones = null;
    }

    /**
     * PUBLIC:
     * Revert all changes made to any registered object.
     * Clear all deleted and new objects.
     * Revert should not be confused with release which it the normal compliment to commit.
     * Revert is more similar to commit and resume, however reverts all changes and resumes.
     * If you do not require to resume the unit of work release should be used instead.
     *
     * @see #commitAndResume()
     * @see #release()
     */
    public void revertAndResume() {
        if (isAfterWriteChangesButBeforeCommit()) {
            throw ValidationException.illegalOperationForUnitOfWorkLifecycle(getLifecycle(), "revertAndResume");
        }
        log(SessionLog.FINER, SessionLog.TRANSACTION, "revert_unit_of_work");

        MergeManager manager = new MergeManager(this);
        manager.mergeOriginalIntoWorkingCopy();
        manager.cascadeAllParts();
        for (Enumeration cloneEnum = getCloneMapping().keys(); cloneEnum.hasMoreElements();) {
            Object clone = cloneEnum.nextElement();

            // Revert each clone.
            manager.mergeChanges(clone, null);
            ClassDescriptor descriptor = this.getDescriptor(clone);

            //revert the tracking policy
            descriptor.getObjectChangePolicy().revertChanges(clone, descriptor, this, this.getCloneMapping());
        }

        // PERF: Avoid initialization of new objects if none.
        if (hasNewObjects()) {
            for (Enumeration cloneEnum = getNewObjectsCloneToOriginal().keys();
                     cloneEnum.hasMoreElements();) {
                Object clone = cloneEnum.nextElement();

                // De-register the object.
                getCloneMapping().remove(clone);
                
            }
            if (this.getUnitOfWorkChangeSet() != null){
                ((UnitOfWorkChangeSet)this.getUnitOfWorkChangeSet()).getNewObjectChangeSets().clear();
            }
        }

        // Clear new and deleted objects.
        setNewObjectsCloneToOriginal(null);
        setNewObjectsOriginalToClone(null);
        // Reset the all clones collection
        resetAllCloneCollection();
        // 2612538 - the default size of IdentityHashtable (32) is appropriate
        setObjectsDeletedDuringCommit(new IdentityHashtable());
        setDeletedObjects(new IdentityHashtable());
        setRemovedObjects(new IdentityHashtable());
        setUnregisteredNewObjects(new IdentityHashtable());
        log(SessionLog.FINER, SessionLog.TRANSACTION, "resume_unit_of_work");
    }

    /**
     * PUBLIC:
     * Revert the object's attributes from the parent.
     * This also reverts the object privately-owned parts.
     *
     * @return the object reverted.
     * @see #shallowRevertObject(Object)
     * @see #deepRevertObject(Object)
     */
    public Object revertObject(Object clone) {
        return revertObject(clone, MergeManager.CASCADE_PRIVATE_PARTS);
    }

    /**
     * INTERNAL:
     * Revert the object's attributes from the parent.
     * This uses merging to merge the object changes.
     */
    public Object revertObject(Object clone, int cascadeDepth) {
        if (clone == null) {
            return null;
        }

        //CR#2272
        logDebugMessage(clone, "revert");

        ClassDescriptor descriptor = getDescriptor(clone);
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(clone, this);

        MergeManager manager = new MergeManager(this);
        manager.mergeOriginalIntoWorkingCopy();
        manager.setCascadePolicy(cascadeDepth);
        try {
            manager.mergeChanges(implementation, null);
        } catch (RuntimeException exception) {
            return handleException(exception);
        }
        return clone;
    }

    /**
     * INTERNAL:
     * This is internal to the uow, transactions should not be used explictly in a uow.
     * The uow shares its parents transactions.
     */
    public void rollbackTransaction() throws DatabaseException {
        incrementProfile(SessionProfiler.UowRollbacks);
        getParent().rollbackTransaction();
    }

    /**
     * INTERNAL:
     * rollbackTransaction() with a twist for external transactions.
     * <p>
     * writeChanges() is called outside the JTA beforeCompletion(), so the
     * accompanying exception won't propogate up and cause a rollback by itself.
     * <p>
     * Instead must mark the transaction for rollback only here.
     * <p>
     * If internally started external transaction or no external transaction
     * can still rollback normally.
     * @param intendedToCommitTransaction whether we were inside a commit or just trying to
     * write out changes early.
     */
    protected void rollbackTransaction(boolean intendedToCommitTransaction) throws DatabaseException {
        if (!intendedToCommitTransaction && getParent().hasExternalTransactionController() && !getParent().wasJTSTransactionInternallyStarted()) {
            getParent().getExternalTransactionController().markTransactionForRollback();
        }
        rollbackTransaction();
    }

    /**
     * INTERNAL:
     * Scans the UnitOfWork identity map for conforming instances.
     * <p>
     * Later this method can be made recursive to check all parent units of
     * work also.
     * @param selectionCriteria must be cloned and specially prepared for conforming
     * @return IdentityHashtable to facilitate merging with conforming instances
     * returned from a query on the database.
     */
    public IdentityHashtable scanForConformingInstances(Expression selectionCriteria, Class referenceClass, AbstractRecord arguments, ObjectLevelReadQuery query) {
        // for bug 3568141 use the painstaking shouldTriggerIndirection if set
        InMemoryQueryIndirectionPolicy policy = query.getInMemoryQueryIndirectionPolicy();
        if (!policy.shouldTriggerIndirection()) {
            policy = new InMemoryQueryIndirectionPolicy(InMemoryQueryIndirectionPolicy.SHOULD_IGNORE_EXCEPTION_RETURN_NOT_CONFORMED);
        }
        IdentityHashtable indexedInterimResult = new IdentityHashtable();
        try {
            Vector fromCache = null;
            if (selectionCriteria != null) {
                // assume objects that have the compared relationship 
                // untriggered do not conform as they have not been changed.
                // bug 2637555
                fromCache = getIdentityMapAccessor().getAllFromIdentityMap(selectionCriteria, referenceClass, arguments, policy);
                for (Enumeration fromCacheEnum = fromCache.elements();
                         fromCacheEnum.hasMoreElements();) {
                    Object object = fromCacheEnum.nextElement();
                    if (!isObjectDeleted(object)) {
                        indexedInterimResult.put(object, object);
                    }
                }
            }

            // Add any new objects that conform to the query.
            Vector newObjects = null;
            newObjects = getAllFromNewObjects(selectionCriteria, referenceClass, arguments, policy);
            for (Enumeration newObjectsEnum = newObjects.elements();
                     newObjectsEnum.hasMoreElements();) {
                Object object = newObjectsEnum.nextElement();
                if (!isObjectDeleted(object)) {
                    indexedInterimResult.put(object, object);
                }
            }
        } catch (QueryException exception) {
            if (getShouldThrowConformExceptions() == THROW_ALL_CONFORM_EXCEPTIONS) {
                throw exception;
            }
        }
        return indexedInterimResult;
    }

    /**
     * INTERNAL:
     * Used to set the collections of all objects in the UnitOfWork.
     * @param newUnregisteredExistingObjects oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    protected void setAllClonesCollection(IdentityHashtable objects) {
        this.allClones = objects;
    }

    /**
     * INTERNAL:
     * Set the clone mapping.
     * The clone mapping contains clone of all registered objects,
     * this is required to store the original state of the objects when registered

     * so that only what is changed will be commited to the database and the parent,
     * (this is required to support parralel unit of work).
     */
    protected void setCloneMapping(IdentityHashtable cloneMapping) {
        this.cloneMapping = cloneMapping;
    }

    /**
     * INTERNAL:
     * set UoW lifecycle state variable to DEATH
     */
    public void setDead() {
        setLifecycle(Death);
    }

    /**
     * INTERNAL:
     * The deleted objects stores any objects removed during the unit of work.
     * On commit they will all be removed from the database.
     */
    protected void setDeletedObjects(IdentityHashtable deletedObjects) {
        this.deletedObjects = deletedObjects;
    }

    /**
     * INTERNAL:
     * The life cycle tracks if the unit of work is active and is used for JTS.
     */
    protected void setLifecycle(int lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * INTERNAL:
     * A reference to the last used merge manager.  This is used to track locked
     * objects.
     */
    public void setMergeManager(MergeManager mergeManager) {
        this.lastUsedMergeManager = mergeManager;
    }

    /**
     * INTERNAL:
     * The new objects stores any objects newly created during the unit of work.
     * On commit they will all be inserted into the database.
     */
    protected void setNewObjectsCloneToOriginal(IdentityHashtable newObjects) {
        this.newObjectsCloneToOriginal = newObjects;
    }

    /**
     * INTERNAL:
     * The new objects stores any objects newly created during the unit of work.
     * On commit they will all be inserted into the database.
     */
    protected void setNewObjectsOriginalToClone(IdentityHashtable newObjects) {
        this.newObjectsOriginalToClone = newObjects;
    }

    /**
     * INTERNAL:
     * Set the objects that have been deleted.
     */
    public void setObjectsDeletedDuringCommit(IdentityHashtable deletedObjects) {
        objectsDeletedDuringCommit = deletedObjects;
    }

    /**
     * INTERNAL:
     * Set the parent.
     * This is a unit of work if nested, otherwise a database session or client session.
     */
    public void setParent(AbstractSession parent) {
        this.parent = parent;
    }

    /**
     * INTERNAL:
     * set UoW lifecycle state variable to PENDING_MERGE
     */
    public void setPendingMerge() {
        setLifecycle(MergePending);
    }

    /**
     * INTERNAL:
     * Gives a new set of read-only classes to the receiver.
     * This set of classes given are checked that subclasses of a read-only class are also
     * in the read-only set provided.
     */
    public void setReadOnlyClasses(Vector classes) {
        this.readOnlyClasses = new Hashtable(classes.size() + 10);
        for (Enumeration enumtr = classes.elements(); enumtr.hasMoreElements();) {
            Class theClass = (Class)enumtr.nextElement();
            addReadOnlyClass(theClass);
        }
    }

    /**
     * INTERNAL:
     * The removed objects stores any newly registered objects removed during the nested unit of work.
     * On commit they will all be removed from the parent unit of work.
     */
    protected void setRemovedObjects(IdentityHashtable removedObjects) {
        this.removedObjects = removedObjects;
    }

    /**
     * INTERNAL:
     * Set if this UnitofWork should be resumed after the end of the transaction
     * Used when UnitOfWork is synchronized with external transaction control
     */
    public void setResumeUnitOfWorkOnTransactionCompletion(boolean resumeUnitOfWork){
        this.resumeOnTransactionCompletion = resumeUnitOfWork;
    }

    /**
     * INTERNAL:
     * True if the value holder for the joined attribute should be triggered.
     * Required by ejb30 fetch join.
     */
    public void setShouldCascadeCloneToJoinedRelationship(boolean shouldCascadeCloneToJoinedRelationship) {
        this.shouldCascadeCloneToJoinedRelationship = shouldCascadeCloneToJoinedRelationship;
    }

    /**
     * ADVANCED:
     * By default new objects are not cached until the exist on the database.
     * Occasionally if mergeClone is used on new objects and is required to allow multiple merges
     * on the same new object, then if the new objects are not cached, each mergeClone will be
     * interpretted as a different new object.
     * By setting new objects to be cached mergeClone can be performed multiple times before commit.
     * New objects cannot be cached unless they have a valid assigned primary key before being registered.
     * New object with non-null invalid primary keys such as 0 or '' can cause problems and should not be used with this option.
     */
    public void setShouldNewObjectsBeCached(boolean shouldNewObjectsBeCached) {
        this.shouldNewObjectsBeCached = shouldNewObjectsBeCached;
    }

    /**
     * ADVANCED:
     * By default deletes are performed last in a unit of work.
     * Sometimes you may want to have the deletes performed before other actions.
     */
    public void setShouldPerformDeletesFirst(boolean shouldPerformDeletesFirst) {
        this.shouldPerformDeletesFirst = shouldPerformDeletesFirst;
    }

    /**
     * ADVANCED:
     * Conforming queries can be set to provide different levels of detail about the
     * exceptions they encounter
     * There are three levels:
     *    DO_NOT_THROW_CONFORM_EXCEPTIONS = 0;
     *    THROW_ALL_CONFORM_EXCEPTIONS = 1;
     */
    public void setShouldThrowConformExceptions(int shouldThrowExceptions) {
        this.shouldThrowConformExceptions = shouldThrowExceptions;
    }

    /**
     * INTERNAL:
     * Set smart merge flag.  This feature is used in WL to merge dependent values without SessionAccessor
     */
    public static void setSmartMerge(boolean option) {
        SmartMerge = option;
    }

    /**
     * INTERNAL:
     * Set isSynchronized flag to indicate that this session is a synchronized unit of work.
     */
    public void setSynchronized(boolean synched) {
        isSynchronized = synched;
    }

    /**
     * INTERNAL:
     * Sets the current UnitOfWork change set to be the one passed in.
     */
    public void setUnitOfWorkChangeSet(UnitOfWorkChangeSet unitOfWorkChangeSet) {
        this.unitOfWorkChangeSet = unitOfWorkChangeSet;
    }

    /**
     * INTERNAL:
     * Used to set the unregistered existing objects vector used when validation has been turned off.
     * @param newUnregisteredExistingObjects oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    protected void setUnregisteredExistingObjects(oracle.toplink.essentials.internal.helper.IdentityHashtable newUnregisteredExistingObjects) {
        unregisteredExistingObjects = newUnregisteredExistingObjects;
    }

    /**
     * INTERNAL:
     */
    protected void setUnregisteredNewObjects(IdentityHashtable newObjects) {
        unregisteredNewObjects = newObjects;
    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public void setValidationLevel(int validationLevel) {
        this.validationLevel = validationLevel;
    }

    /**
     * INTERNAL:
     * Set a flag in the root UOW to indicate that a pess. locking or non-selecting SQL query was executed
     * and forced a transaction to be started.
     */
    public void setWasTransactionBegunPrematurely(boolean wasTransactionBegunPrematurely) {
        if (isNestedUnitOfWork()) {
            ((UnitOfWorkImpl)getParent()).setWasTransactionBegunPrematurely(wasTransactionBegunPrematurely);
        }
        this.wasTransactionBegunPrematurely = wasTransactionBegunPrematurely;
    }

    /**
     * PUBLIC:
     * Merge the attributes of the clone into the unit of work copy.
     * This can be used for objects that are returned from the client through
     * RMI serialization (or other serialization mechanisms), because the RMI object will
     * be a clone this will merge its attributes correctly to preserve object identity
     * within the unit of work and record its changes.
     *
     * Only direct attributes are merged.
     *
     * @return the registered version for the clone being merged.
     * @see #mergeClone(Object)
     * @see #deepMergeClone(Object)
     */
    public Object shallowMergeClone(Object rmiClone) {
        return mergeClone(rmiClone, MergeManager.NO_CASCADE);
    }

    /**
     * PUBLIC:
     * Revert the object's attributes from the parent.
     * This only reverts the object's direct attributes.
     *
     * @return the object reverted.
     * @see #revertObject(Object)
     * @see #deepRevertObject(Object)
     */
    public Object shallowRevertObject(Object clone) {
        return revertObject(clone, MergeManager.NO_CASCADE);
    }

    /**
     * ADVANCED:
     * Unregister the object with the unit of work.
     * This can be used to delete an object that was just created and is not yet persistent.
     * Delete object can also be used, but will result in inserting the object and then deleting it.
     * The method will only unregister the clone, none of its parts.
     */
    public void shallowUnregisterObject(Object clone) {
        unregisterObject(clone, DescriptorIterator.NoCascading);
    }

    /**
     * INTERNAL:
     * True if the value holder for the joined attribute should be triggered.
     * Required by ejb30 fetch join.
     */
    public boolean shouldCascadeCloneToJoinedRelationship() {
        return shouldCascadeCloneToJoinedRelationship;
    }

    /**
     * ADVANCED:
     * By default new objects are not cached until they exist on the database.
     * Occasionally if mergeClone is used on new objects and is required to allow multiple merges
     * on the same new object, then if the new objects are not cached, each mergeClone will be
     * interpretted as a different new object.
     * By setting new objects to be cached mergeClone can be performed multiple times before commit.
     * New objects cannot be cached unless they have a valid assigned primary key before being registered.
     * New object with non-null invalid primary keys such as 0 or '' can cause problems and should not be used with this option.
     */
    public boolean shouldNewObjectsBeCached() {
        return shouldNewObjectsBeCached;
    }

    /**
     * ADVANCED:
     * By default all objects are inserted and updated in the database before
     * any object is deleted. If this flag is set to true, deletes will be
     * performed before inserts and updates
     */
    public boolean shouldPerformDeletesFirst() {
        return shouldPerformDeletesFirst;
    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public boolean shouldPerformFullValidation() {
        return getValidationLevel() == Full;
    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public boolean shouldPerformNoValidation() {
        return getValidationLevel() == None;
    }

    /**
     * ADVANCED:
     * The unit of work performs validations such as,
     * ensuring multiple copies of the same object don't exist in the same unit of work,
     * ensuring deleted objects are not refered after commit,
     * ensures that objects from the parent cache are not refered in the unit of work cache.
     * The level of validation can be increased or decreased for debugging purposes or under
     * advanced situation where the application requires/desires to violate clone identity in the unit of work.
     * It is strongly suggested that clone identity not be violate in the unit of work.
     */
    public boolean shouldPerformPartialValidation() {
        return getValidationLevel() == Partial;
    }

    /**
     * INTERNAL:
     * Returns true if this UnitofWork should be resumed after the end of the transaction
     * Used when UnitOfWork is synchronized with external transaction control
     */
    public boolean shouldResumeUnitOfWorkOnTransactionCompletion(){
        return this.resumeOnTransactionCompletion;
    }
    
    /**
     * INTERNAL:
     * Store the ModifyAllQuery's from the UoW in the list. They are always
     * deferred to commit time
     */
    public void storeModifyAllQuery(DatabaseQuery query) {
        if (modifyAllQueries == null) {
            modifyAllQueries = new ArrayList();
        }

        modifyAllQueries.add(query);
    }

    /**
     * INTERNAL:
     * Store the deferred UpdateAllQuery's from the UoW in the list. 
     */
    public void storeDeferredModifyAllQuery(DatabaseQuery query, AbstractRecord translationRow) {
        if (deferredModifyAllQueries == null) {
            deferredModifyAllQueries = new ArrayList();
        }
        deferredModifyAllQueries.add(new Object[]{query, translationRow});
    }

    /**
     * INTERNAL
     * Synchronize the clones and update their backup copies.
     * Called after commit and commit and resume.
     */
    public void synchronizeAndResume() {
        // For pessimistic locking all locks were released by commit.
        getPessimisticLockedObjects().clear();
        getProperties().remove(LOCK_QUERIES_PROPERTY);

        // find next power-of-2 size
        IdentityHashtable newCloneMapping = new IdentityHashtable(1 + getCloneMapping().size());

        for (Enumeration cloneEnum = getCloneMapping().keys(); cloneEnum.hasMoreElements();) {
            Object clone = cloneEnum.nextElement();

            // Do not add object that were deleted, what about private parts??
            if ((!isObjectDeleted(clone)) && (!getRemovedObjects().containsKey(clone))) {
                ClassDescriptor descriptor = getDescriptor(clone);
                ObjectBuilder builder = descriptor.getObjectBuilder();

                //Build backup clone for DeferredChangeDetectionPolicy or ObjectChangeTrackingPolicy,
                //but not for AttributeChangeTrackingPolicy
                descriptor.getObjectChangePolicy().revertChanges(clone, descriptor, this, newCloneMapping);
            }
        }
        setCloneMapping(newCloneMapping);

        if (hasObjectsDeletedDuringCommit()) {
            for (Enumeration removedObjects = getObjectsDeletedDuringCommit().keys();
                     removedObjects.hasMoreElements();) {
                Object removedObject = removedObjects.nextElement();
                getIdentityMapAccessor().removeFromIdentityMap((Vector)getObjectsDeletedDuringCommit().get(removedObject), removedObject.getClass());
            }
        }

        // New objects are not new anymore.
        // can not set multi clone for NestedUnitOfWork.CR#2015 - XC
        if (!isNestedUnitOfWork()) {
            //Need to move objects and clones from NewObjectsCloneToOriginal to CloneToOriginals for use in the continued uow
            if (hasNewObjects()) {
                for (Enumeration newClones = getNewObjectsCloneToOriginal().keys(); newClones.hasMoreElements();) {
                    Object newClone = newClones.nextElement();
                    getCloneToOriginals().put(newClone, getNewObjectsCloneToOriginal().get(newClone));
                }
            }
            setNewObjectsCloneToOriginal(null);
            setNewObjectsOriginalToClone(null);
        }

        //reset unitOfWorkChangeSet.  Needed for ObjectChangeTrackingPolicy and DeferredChangeDetectionPolicy
        setUnitOfWorkChangeSet(null);

        // The collections of clones may change in the new UnitOfWork
        resetAllCloneCollection();
        // 2612538 - the default size of IdentityHashtable (32) is appropriate
        setObjectsDeletedDuringCommit(new IdentityHashtable());
        setDeletedObjects(new IdentityHashtable());
        setRemovedObjects(new IdentityHashtable());
        setUnregisteredNewObjects(new IdentityHashtable());
        //Reset lifecycle
        this.lifecycle = Birth;
        this.isSynchronized = false;
    }

    /**
     * INTERNAL:
     * THis method is used to transition an object from the deleted objects list
     * to be simply be register.
     */
    protected void undeleteObject(Object object){
        getDeletedObjects().remove(object);
        if (getParent().isUnitOfWork()) {
            ((UnitOfWorkImpl)getParent()).undeleteObject(object);
        }
    }
    
    /**
     * PUBLIC:
     * Unregister the object with the unit of work.
     * This can be used to delete an object that was just created and is not yet persistent.
     * Delete object can also be used, but will result in inserting the object and then deleting it.
     * The method will only unregister the object and its privately owned parts
     */
    public void unregisterObject(Object clone) {
        unregisterObject(clone, DescriptorIterator.CascadePrivateParts);
    }

    /**
     * INTERNAL:
     * Unregister the object with the unit of work.
     * This can be used to delete an object that was just created and is not yet persistent.
     * Delete object can also be used, but will result in inserting the object and then deleting it.
     */
    public void unregisterObject(Object clone, int cascadeDepth) {
        // Allow register to be called with null and just return true
        if (clone == null) {
            return;
        }

        //CR#2272
        logDebugMessage(clone, "unregister");
        Object implementation = getDescriptor(clone).getObjectBuilder().unwrapObject(clone, this);

        // This define an inner class for process the itteration operation, don't be scared, its just an inner class.
        DescriptorIterator iterator = new DescriptorIterator() {
            public void iterate(Object object) {
                if (isClassReadOnly(object.getClass(), getCurrentDescriptor())) {
                    setShouldBreak(true);
                    return;
                }

                // Check if object exists in the IM.
                Vector primaryKey = getCurrentDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(object, UnitOfWorkImpl.this);

                // If object exists in IM remove it from the IM and also from clone mapping.
                getIdentityMapAccessorInstance().removeFromIdentityMap(primaryKey, object.getClass(), getCurrentDescriptor());
                getCloneMapping().remove(object);

                // Remove object from the new object cache				
                // PERF: Avoid initialization of new objects if none.
                if (hasNewObjects()) {
                    Object original = getNewObjectsCloneToOriginal().remove(object);
                    if (original != null) {
                        getNewObjectsOriginalToClone().remove(original);
                    }
                }
            }
        };

        iterator.setSession(this);
        iterator.setCascadeDepth(cascadeDepth);
        iterator.startIterationOn(implementation);
    }

    /**
     * INTERNAL:
     * This method is used internally to update the tracked objects if required
     */
    public void updateChangeTrackersIfRequired(Object objectToWrite, ObjectChangeSet changeSetToWrite, UnitOfWorkImpl uow, ClassDescriptor descriptor) {
        //this is a no op in this unitOfWork Class see subclasses for implementation.
    }

    /**
     * ADVANCED:
     * This can be used to help debugging an object-space corruption.
     * An object-space corruption is when your application has incorrectly related a clone to an original object.
     * This method will validate that all registered objects are in a correct state and throw
     * an error if not,  it will contain the full stack of object references in the error message.
     * If you call this method after each register or change you perform it will pin-point where the error was made.
     */
    public void validateObjectSpace() {
        log(SessionLog.FINER, SessionLog.TRANSACTION, "validate_object_space");
        // This define an inner class for process the itteration operation, don't be scared, its just an inner class.
        DescriptorIterator iterator = new DescriptorIterator() {
            public void iterate(Object object) {
                try {
                    if (isClassReadOnly(object.getClass(), getCurrentDescriptor())) {
                        setShouldBreak(true);
                        return;
                    } else {
                        getBackupClone(object);
                    }
                } catch (TopLinkException exception) {
                    log(SessionLog.FINEST, SessionLog.TRANSACTION, "stack_of_visited_objects_that_refer_to_the_corrupt_object", getVisitedStack());
                    log(SessionLog.FINER, SessionLog.TRANSACTION, "corrupt_object_referenced_through_mapping", getCurrentMapping());
                    throw exception;
                }
            }
        };

        iterator.setSession(this);
        for (Enumeration clonesEnum = getCloneMapping().keys(); clonesEnum.hasMoreElements();) {
            iterator.startIterationOn(clonesEnum.nextElement());
        }
    }

    /**
     * INTERNAL:
     * Indicates if a transaction was begun by a pessimistic locking or non-selecting query.
     * Traverse to the root UOW to get value.
     */

    // * 2.5.1.8 Nov 17, 2000 JED
    // * Prs 25751 Changed to make this method public
    public boolean wasTransactionBegunPrematurely() {
        if (isNestedUnitOfWork()) {
            return ((UnitOfWorkImpl)getParent()).wasTransactionBegunPrematurely();
        }
        return wasTransactionBegunPrematurely;
    }

    /**
     * ADVANCED: Writes all changes now before commit().
     * The commit process will begin and all changes will be written out to the datastore, but the datastore transaction will not
     * be committed, nor will changes be merged into the global cache.
     * <p>
     * A subsequent commit (on UnitOfWork or global transaction) will be required to finalize the commit process.
     * <p>
     * As the commit process has begun any attempt to register objects, or execute object-level queries will
     * generate an exception.  Report queries, non-caching queries, and data read/modify queries are allowed.
     * <p>
     * On exception any global transaction will be rolled back or marked rollback only.  No recovery of this UnitOfWork will be possible.
     * <p>
     * Can only be called once.  It can not be used to write out changes in an incremental fashion.
     * <p>
     * Use to partially commit a transaction outside of a JTA transaction's callbacks.  Allows you to get back any exception directly.
     * <p>
     * Use to commit a UnitOfWork in two stages.
     */
    public void writeChanges() {
        if (!isActive()) {
            throw ValidationException.inActiveUnitOfWork("writeChanges");
        }
        if (isAfterWriteChangesButBeforeCommit()) {
            throw ValidationException.cannotWriteChangesTwice();
        }
        if (isNestedUnitOfWork()) {
            throw ValidationException.writeChangesOnNestedUnitOfWork();
        }
        log(SessionLog.FINER, SessionLog.TRANSACTION, "begin_unit_of_work_commit");
        getEventManager().preCommitUnitOfWork();
        setLifecycle(CommitPending);
        try {
            commitToDatabaseWithChangeSet(false);
        } catch (RuntimeException e) {
            setLifecycle(WriteChangesFailed);
            throw e;
        }
        setLifecycle(CommitTransactionPending);
    }

    /**
     * INTERNAL:
     * This method notifies the accessor that a particular sets of writes has
     * completed.  This notification can be used for such thing as flushing the
     * batch mechanism
     */
    public void writesCompleted() {
        getParent().writesCompleted();
    }

    /**
     * log the message and debug info if option is set. (reduce the duplicate codes)
     */
    private void logDebugMessage(Object object, String debugMessage) {
        log(SessionLog.FINEST, SessionLog.TRANSACTION, debugMessage, object);
    }

    /**
     * INTERNAL:
     * Return the registered working copy from the unit of work identity map.
     * If not registered in the unit of work yet, return null
     */
    public Object getWorkingCopyFromUnitOfWorkIdentityMap(Object object, Vector primaryKey) {
        //return the descriptor of the passed object
        ClassDescriptor descriptor = getDescriptor(object);
        if (descriptor == null) {
            throw DescriptorException.missingDescriptor(object.getClass().toString());
        }

        //aggregated object cannot be registered directly, but through the parent owning object.
        if (descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()) {
            throw ValidationException.cannotRegisterAggregateObjectInUnitOfWork(object.getClass());
        }

        // Check if the working copy is again being registered in which case we return the same working copy
        Object registeredObject = getCloneMapping().get(object);
        if (registeredObject != null) {
            return object;
        }

        //check the unit of work cache first to see if already registered.
        Object objectFromUOWCache = getIdentityMapAccessorInstance().getIdentityMapManager().getFromIdentityMap(primaryKey, object.getClass(), descriptor);
        if (objectFromUOWCache != null) {
            // Has already been cloned, return the working clone from the IM rather than the passed object.
            return objectFromUOWCache;
        }

        //not found, return null
        return null;
    }

    /**
     * INTERNAL:
     */
    public IdentityHashtable getPessimisticLockedObjects() {
        if (pessimisticLockedObjects == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            pessimisticLockedObjects = new IdentityHashtable();
        }
        return pessimisticLockedObjects;
    }

    /**
     * INTERNAL:
     */
    public void addPessimisticLockedClone(Object clone) {
        log(SessionLog.FINEST, SessionLog.TRANSACTION, "tracking_pl_object", clone, new Integer(this.hashCode()));
        getPessimisticLockedObjects().put(clone, clone);
    }

    /**
      * INTERNAL:
      */
    public boolean isPessimisticLocked(Object clone) {
        return getPessimisticLockedObjects().containsKey(clone);
    }

    /**
     * INTERNAL:
     * True if either DataModifyQuery or ModifyAllQuery was executed.
     * In absense of transaction the query execution starts one, therefore
     * the flag may only be true in transaction, it's reset on commit or rollback.
     */
    public void setWasNonObjectLevelModifyQueryExecuted(boolean wasNonObjectLevelModifyQueryExecuted) {
        this.wasNonObjectLevelModifyQueryExecuted = wasNonObjectLevelModifyQueryExecuted;
    }
    
    /**
     * INTERNAL:
     * True if either DataModifyQuery or ModifyAllQuery was executed.
     */
    public boolean wasNonObjectLevelModifyQueryExecuted() {
        return wasNonObjectLevelModifyQueryExecuted;
    }
    
    /**
      * INTERNAL:
      * Indicates whether readObject should return the object read from the db
      * in case there is no object in uow cache (as opposed to fetching the object from 
      * parent's cache). Note that wasNonObjectLevelModifyQueryExecuted()==true implies inTransaction()==true.
      */
    public boolean shouldReadFromDB() {
        return wasNonObjectLevelModifyQueryExecuted();
    }

    /**
     * INTERNAL:
     * This method will clear all registered objects from this UnitOfWork.
     * If parameter value is 'true' then the cache(s) are cleared, too.
     */
    public void clear(boolean shouldClearCache) {
        this.cloneToOriginals = null;
        this.cloneMapping = new IdentityHashtable();
        this.newObjectsCloneToOriginal = null;
        this.newObjectsOriginalToClone = null;
        this.deletedObjects = null;
        this.allClones = null;
        this.objectsDeletedDuringCommit = null;
        this.removedObjects = null;
        this.unregisteredNewObjects = null;
        this.unregisteredExistingObjects = null;
        this.newAggregates = null;
        this.unitOfWorkChangeSet = null;
        this.pessimisticLockedObjects = null;
        this.optimisticReadLockObjects = null;
        if(shouldClearCache) {
            this.getIdentityMapAccessor().initializeIdentityMaps();
            if (this.getParent() instanceof IsolatedClientSession) {
                this.getParent().getIdentityMapAccessor().initializeIdentityMaps();
            }
        }
    }
    
    /**
     * INTERNAL:
     * Call this method if the uow will no longer used for comitting transactions:
     * all the changes sets will be dereferenced, and (optionally) the cache cleared.
     * If the uow is not released, but rather kept around for ValueHolders, then identity maps shouldn't be cleared:
     * the parameter value should be 'false'. The lifecycle set to Birth so that uow ValueHolder still could be used.
     * Alternatively, if called from release method then everything should go and therefore parameter value should be 'true'.
     * In this case lifecycle won't change - uow.release (optionally) calls this method when it (uow) is already dead.
     * The reason for calling this method from release is to free maximum memory right away:
     * the uow might still be referenced by objects using UOWValueHolders (though they shouldn't be around
     * they still might).
     */
    public void clearForClose(boolean shouldClearCache) {
        clear(shouldClearCache);
        if(isActive()) {
            //Reset lifecycle
            this.lifecycle = Birth;
            this.isSynchronized = false;
        }
    }
    
    /**
     * INTERNAL:
     * Indicates whether clearForClose methor should be called by release method.
     */
    public boolean shouldClearForCloseOnRelease() {
        return false;
    }    
}
