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
package oracle.toplink.essentials.mappings;

import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.indirection.ValueHolderInterface;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.identitymaps.CacheKey;
import oracle.toplink.essentials.internal.indirection.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.ObjectCopyingPolicy;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: Abstract class for 1:1, varibale 1:1 and reference mappings
 */
public abstract class ObjectReferenceMapping extends ForeignReferenceMapping {

    /** Keeps track if any of the fields are foreign keys. */
    protected boolean isForeignKeyRelationship;

    /** Keeps track of which fields are foreign keys on a per field basis (can have mixed foreign key relationships). */
    protected Vector<DatabaseField> foreignKeyFields;

    protected ObjectReferenceMapping() {
        super();
    }

    /**
     * INTERNAL:
     * Used during building the backup shallow copy to copy the vector without re-registering the target objects.
     * For 1-1 or ref the reference is from the clone so it is already registered.
     */
    public Object buildBackupCloneForPartObject(Object attributeValue, Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        return attributeValue;
    }

    /**
     * INTERNAL:
     * Require for cloning, the part must be cloned.
     * Ignore the objects, use the attribute value.
     */
    public Object buildCloneForPartObject(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean isExisting) {
        // Optimize registration to knowledge of existence.
        if (isExisting) {
            return unitOfWork.registerExistingObject(attributeValue);
        } else {
            // Not known wether existing or not.
            return unitOfWork.registerObject(attributeValue);
        }
    }

    /**
     * INTERNAL:
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    public void buildCopy(Object copy, Object original, ObjectCopyingPolicy policy) {
        Object attributeValue = getRealAttributeValueFromObject(original, policy.getSession());
        if ((attributeValue != null) && (policy.shouldCascadeAllParts() || (policy.shouldCascadePrivateParts() && isPrivateOwned()))) {
            attributeValue = policy.getSession().copyObject(attributeValue, policy);
        } else if (attributeValue != null) {
            // Check for copy of part, i.e. back reference.
            Object copyValue = policy.getCopies().get(attributeValue);
            if (copyValue != null) {
                attributeValue = copyValue;
            }
        }
        setRealAttributeValueInObject(copy, attributeValue);
    }

    /**
     * INTERNAL:
     * This method was created in VisualAge.
     * @return prototype.changeset.ChangeRecord
     */
    public ChangeRecord compareForChange(Object clone, Object backUp, ObjectChangeSet owner, AbstractSession session) {
        Object cloneAttribute = null;
        Object backUpAttribute = null;

        cloneAttribute = getAttributeValueFromObject(clone);

        if (!owner.isNew()) {
            backUpAttribute = getAttributeValueFromObject(backUp);
            if ((backUpAttribute == null) && (cloneAttribute == null)) {
                return null;
            }
        }

        if ((cloneAttribute != null) && (!getIndirectionPolicy().objectIsInstantiated(cloneAttribute))) {
            //the clone's valueholder was never triggered so there will be no change
            return null;
        }
        Object cloneAttributeValue = null;
        Object backUpAttributeValue = null;

        if (cloneAttribute != null) {
            cloneAttributeValue = getRealAttributeValueFromObject(clone, session);
        }
        if (backUpAttribute != null) {
            backUpAttributeValue = getRealAttributeValueFromObject(backUp, session);
        }

        if ((cloneAttributeValue == backUpAttributeValue) && (!owner.isNew())) {// if it is new record the value
            return null;
        }

        ObjectReferenceChangeRecord record = internalBuildChangeRecord(cloneAttributeValue, owner, session);
        if (!owner.isNew()) {
            record.setOldValue(backUpAttributeValue);
        }
        return record;
    }

    /**
     * INTERNAL:
     * Directly build a change record based on the newValue without comparison
     */
    public ObjectReferenceChangeRecord internalBuildChangeRecord(Object newValue, ObjectChangeSet owner, AbstractSession session) {
        ObjectReferenceChangeRecord changeRecord = new ObjectReferenceChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        setNewValueInChangeRecord(newValue, changeRecord, owner, session);
        return changeRecord;
    }

    /**
     * INTERNAL:
     * Set the newValue in the change record
     */
    public void setNewValueInChangeRecord(Object newValue, ObjectReferenceChangeRecord changeRecord, ObjectChangeSet owner, AbstractSession session) {
        if (newValue != null) {
            // Bug 2612571 - added more flexible manner of getting descriptor
            ObjectChangeSet newSet = getDescriptorForTarget(newValue, session).getObjectBuilder().createObjectChangeSet(newValue, (UnitOfWorkChangeSet)owner.getUOWChangeSet(), session);
            changeRecord.setNewValue(newSet);
        } else {
            changeRecord.setNewValue(null);
        }
    }

    /**
     * INTERNAL:
     * Compare the references of the two objects are the same, not the objects themselves.
     * Used for independent relationships.
     * This is used for testing and validation purposes.
     */
    protected boolean compareObjectsWithoutPrivateOwned(Object firstObject, Object secondObject, AbstractSession session) {
        Object firstReferencedObject = getRealAttributeValueFromObject(firstObject, session);
        Object secondReferencedObject = getRealAttributeValueFromObject(secondObject, session);

        if ((firstReferencedObject == null) && (secondReferencedObject == null)) {
            return true;
        }

        if ((firstReferencedObject == null) || (secondReferencedObject == null)) {
            return false;
        }

        Vector firstKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(firstReferencedObject, session);
        Vector secondKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(secondReferencedObject, session);

        for (int index = 0; index < firstKey.size(); index++) {
            Object firstValue = firstKey.elementAt(index);
            Object secondValue = secondKey.elementAt(index);

            if (!((firstValue == null) && (secondValue == null))) {
                if ((firstValue == null) || (secondValue == null)) {
                    return false;
                }
                if (!firstValue.equals(secondValue)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * INTERNAL:
     * Compare the references of the two objects are the same, and the objects themselves are the same.
     * Used for private relationships.
     * This is used for testing and validation purposes.
     */
    protected boolean compareObjectsWithPrivateOwned(Object firstObject, Object secondObject, AbstractSession session) {
        Object firstPrivateObject = getRealAttributeValueFromObject(firstObject, session);
        Object secondPrivateObject = getRealAttributeValueFromObject(secondObject, session);

        return session.compareObjects(firstPrivateObject, secondPrivateObject);
    }

    /**
     * INTERNAL:
     * Return a descriptor for the target of this mapping
     * @see oracle.toplink.essentials.mappings.VariableOneToOneMapping
     * Bug 2612571
     */
    public ClassDescriptor getDescriptorForTarget(Object object, AbstractSession session) {
        return session.getDescriptor(object);
    }

    /**
     * INTERNAL:
     * Object reference must unwrap the reference object if required.
     */
    public Object getRealAttributeValueFromObject(Object object, AbstractSession session) {
        Object value = super.getRealAttributeValueFromObject(object, session);
        value = getReferenceDescriptor().getObjectBuilder().unwrapObject(value, session);

        return value;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isObjectReferenceMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Iterate on the attribute value.
     * The value holder has already been processed.
     */
    public void iterateOnRealAttributeValue(DescriptorIterator iterator, Object realAttributeValue) {
        // This may be wrapped as the caller in iterate on foreign reference does not unwrap as the type is generic.
        Object unwrappedAttributeValue = getReferenceDescriptor().getObjectBuilder().unwrapObject(realAttributeValue, iterator.getSession());
        iterator.iterateReferenceObjectForMapping(unwrappedAttributeValue, this);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object. Which is the original from the parent UnitOfWork
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        Object targetValueOfSource = null;

        // The target object must be completely merged before setting it otherwise
        // another thread can pick up the partial object.
        if (shouldMergeCascadeParts(mergeManager)) {
            ObjectChangeSet set = (ObjectChangeSet)((ObjectReferenceChangeRecord)changeRecord).getNewValue();
            if (set != null) {
                if (mergeManager.shouldMergeChangesIntoDistributedCache()) {
                    //Let's try and find it first.  We may have merged it allready. In which case merge
                    //changes will  stop the recursion
                    targetValueOfSource = set.getTargetVersionOfSourceObject(mergeManager.getSession(), false);
                    if ((targetValueOfSource == null) && (set.isNew() || set.isAggregate()) && set.containsChangesFromSynchronization()) {
                        if (!mergeManager.getObjectsAlreadyMerged().containsKey(set)) {
                            // if we haven't merged this object allready then build a new object
                            // otherwise leave it as null which will stop the recursion
                            // CR 2855
                            // CR 3424 Need to build the right instance based on class type instead of refernceDescriptor
                            Class objectClass = set.getClassType(mergeManager.getSession());
                            targetValueOfSource = mergeManager.getSession().getDescriptor(objectClass).getObjectBuilder().buildNewInstance();
                            //Store the changeset to prevent us from creating this new object again
                            mergeManager.getObjectsAlreadyMerged().put(set, targetValueOfSource);
                        } else {
                            //CR 4012
                            //we have all ready created the object, must be in a cyclic
                            //merge on a new object so get it out of the allreadymerged collection
                            targetValueOfSource = mergeManager.getObjectsAlreadyMerged().get(set);
                        }
                    } else {
                        // If We have not found it anywhere else load it from the database
                        targetValueOfSource = set.getTargetVersionOfSourceObject(mergeManager.getSession(), true);
                    }
                    if (set.containsChangesFromSynchronization()) {
                        mergeManager.mergeChanges(targetValueOfSource, set);
                    }
                    //bug:3604593 - ensure reference not changed source is invalidated if target object not found
                    if (targetValueOfSource ==null)
                    {
                      mergeManager.getSession().getIdentityMapAccessorInstance().invalidateObject(target);
                      return;
                    }
                } else {
                    mergeManager.mergeChanges(set.getUnitOfWorkClone(), set);
                }
            }
        }
        if ((targetValueOfSource == null) && (((ObjectReferenceChangeRecord)changeRecord).getNewValue() != null)) {
            targetValueOfSource = ((ObjectChangeSet)((ObjectReferenceChangeRecord)changeRecord).getNewValue()).getTargetVersionOfSourceObject(mergeManager.getSession());
        }

        // Register new object in nested units of work must not be registered into the parent,
        // so this records them in the merge to parent case.
        if (isPrivateOwned() && (source != null)) {
            mergeManager.registerRemovedNewObjectIfRequired(getRealAttributeValueFromObject(source, mergeManager.getSession()));
        }

        targetValueOfSource = getReferenceDescriptor().getObjectBuilder().wrapObject(targetValueOfSource, mergeManager.getSession());
        setRealAttributeValueInObject(target, targetValueOfSource);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    public void mergeIntoObject(Object target, boolean isTargetUnInitialized, Object source, MergeManager mergeManager) {
        if (isTargetUnInitialized) {
            // This will happen if the target object was removed from the cache before the commit was attempted
            if (mergeManager.shouldMergeWorkingCopyIntoOriginal() && (!isAttributeValueInstantiated(source))) {
                setAttributeValueInObject(target, getIndirectionPolicy().getOriginalIndirectionObject(getAttributeValueFromObject(source), mergeManager.getSession()));
                return;
            }
        }
        if (!shouldMergeCascadeReference(mergeManager)) {
            // This is only going to happen on mergeClone, and we should not attempt to merge the reference
            return;
        }
        if (mergeManager.shouldMergeOriginalIntoWorkingCopy()) {
            if (!isAttributeValueInstantiated(target)) {
                // This will occur when the clone's value has not been instantiated yet and we do not need
                // the refresh that attribute
                return;
            }
        } else if (!isAttributeValueInstantiated(source)) {
            // I am merging from a clone into an original.  No need to do merge if the attribute was never
            // modified
            return;
        }

        Object valueOfSource = getRealAttributeValueFromObject(source, mergeManager.getSession());

        Object targetValueOfSource = null;

        // The target object must be completely merged before setting it otherwise
        // another thread can pick up the partial object.
        if (shouldMergeCascadeParts(mergeManager) && (valueOfSource != null)) {
            if ((mergeManager.getSession().isUnitOfWork()) && (((UnitOfWorkImpl)mergeManager.getSession()).getUnitOfWorkChangeSet() != null)) {
                // If it is a unit of work, we have to check if I have a change Set fot this object
                mergeManager.mergeChanges(mergeManager.getObjectToMerge(valueOfSource), (ObjectChangeSet)((UnitOfWorkChangeSet)((UnitOfWorkImpl)mergeManager.getSession()).getUnitOfWorkChangeSet()).getObjectChangeSetForClone(valueOfSource));
            } else {
                mergeManager.mergeChanges(mergeManager.getObjectToMerge(valueOfSource), null);
            }
        }

        if (valueOfSource != null) {
            // Need to do this after merge so that an object exists in the database
            targetValueOfSource = mergeManager.getTargetVersionOfSourceObject(valueOfSource);
        }

        if (this.getDescriptor().getObjectChangePolicy().isObjectChangeTrackingPolicy()) {
            // Object level or attribute level so lets see if we need to raise the event?
            Object valueOfTarget = getRealAttributeValueFromObject(target, mergeManager.getSession());
            if ( valueOfTarget != targetValueOfSource ) { //equality comparison cause both are uow clones
                this.getDescriptor().getObjectChangePolicy().raiseInternalPropertyChangeEvent(target, getAttributeName(), valueOfTarget, targetValueOfSource);
            }
        }
 
        targetValueOfSource = getReferenceDescriptor().getObjectBuilder().wrapObject(targetValueOfSource, mergeManager.getSession());
        setRealAttributeValueInObject(target, targetValueOfSource);
    }

    /**
     * INTERNAL:
     * Return all the fields populated by this mapping, these are foreign keys only.
     */
    protected Vector<DatabaseField> collectFields() {
        return getForeignKeyFields();
    }

    /**
     * INTERNAL:
     * Returns the foreign key names associated with the mapping.
     * These are the fields that will be populated by the 1-1 mapping when writting.
     */
    public Vector<DatabaseField> getForeignKeyFields() {
        return foreignKeyFields;
    }

    /**
    * INTERNAL:
    * Set the foreign key fields associated with the mapping.
    * These are the fields that will be populated by the 1-1 mapping when writting.
    */
    protected void setForeignKeyFields(Vector<DatabaseField> foreignKeyFields) {
        this.foreignKeyFields = foreignKeyFields;
        if (!foreignKeyFields.isEmpty()) {
            setIsForeignKeyRelationship(true);
        }
    }

    /**
     * INTERNAL:
     * Return if the 1-1 mapping has a foreign key dependency to its target.
     * This is true if any of the foreign key fields are true foreign keys,
     * i.e. populated on write from the targets primary key.
     */
    public boolean isForeignKeyRelationship() {
        return isForeignKeyRelationship;
    }

    /**
     * INTERNAL:
     * Set if the 1-1 mapping has a foreign key dependency to its target.
     * This is true if any of the foreign key fields are true foreign keys,
     * i.e. populated on write from the targets primary key.
     */
    public void setIsForeignKeyRelationship(boolean isForeignKeyRelationship) {
        this.isForeignKeyRelationship = isForeignKeyRelationship;
    }

    /**
     * INTERNAL:
     * Insert privately owned parts
     */
    public void preInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (isForeignKeyRelationship()) {
            insert(query);
        }
    }

    /**
     * INTERNAL:
     * Reads the private owned object.
     */
    protected Object readPrivateOwnedForObject(ObjectLevelModifyQuery modifyQuery) throws DatabaseException {
        if (modifyQuery.getSession().isUnitOfWork()) {
            if (modifyQuery.getObjectChangeSet() != null) {
                ObjectReferenceChangeRecord record = (ObjectReferenceChangeRecord) modifyQuery.getObjectChangeSet().getChangesForAttributeNamed(getAttributeName());
                if (record != null) {
                    return record.getOldValue();
                } 
            } else { // Old commit.
                return getRealAttributeValueFromObject(modifyQuery.getBackupClone(), modifyQuery.getSession());
            }
        }
        
        return null;
    }

    /**
     * INTERNAL:
     * Update privately owned parts
     */
    public void preUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isAttributeValueInstantiated(query.getObject())) {
            return;
        }

        if (isPrivateOwned()) {
            Object objectInDatabase = readPrivateOwnedForObject(query);
            if (objectInDatabase != null) {
                query.setProperty(this, objectInDatabase);
            }
        }

        if (!isForeignKeyRelationship()) {
            return;
        }

        update(query);
    }

    /**
     * INTERNAL:
     * Delete the referenced objects.
     */
    public void postDelete(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        // Deletion takes place according the the cascading policy
        if (!shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // There are no dependencies after this object has been deleted successfully!
        if (query.shouldCascadeOnlyDependentParts() && !isPrivateOwned()) {
            return;
        }

        Object object = query.getProperty(this);

        // The object is stored in the query by preDeleteForObjectUsing(...).
        if (isForeignKeyRelationship()) {
            if (object != null) {
                query.removeProperty(this);

                //if the query is being passed from an aggregate collection descriptor then 
                // The delete will have been cascaded at update time.  This will cause sub objects
                // to be ignored, and real only classes to throw exceptions.
                // If it is an aggregate Collection then delay deletes until they should be deleted
                //CR 2811	
                if (query.isCascadeOfAggregateDelete()) {
                    query.getSession().getCommitManager().addObjectToDelete(object);
                } else {
                    DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
                    deleteQuery.setObject(object);
                    deleteQuery.setCascadePolicy(query.getCascadePolicy());
                    query.getSession().executeQuery(deleteQuery);
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Insert privately owned parts
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isForeignKeyRelationship()) {
            insert(query);
        }
    }

    /**
     * INTERNAL:
     * Update privately owned parts
     */
    public void postUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isAttributeValueInstantiated(query.getObject())) {
            return;
        }

        if (!isForeignKeyRelationship()) {
            update(query);
        }

        // If a private owned reference was changed the old value will be set on the query as a property.
        Object objectInDatabase = query.getProperty(this);
        if (objectInDatabase != null) {
            query.removeProperty(this);
        } else {
            return;
        }

        // If there is no change (old commit), it must be determined if the value changed.
        if (query.getObjectChangeSet() == null) {
            Object objectInMemory = getRealAttributeValueFromObject(query.getObject(), query.getSession());
    
            // delete the object in the database if it is no more a referenced object.						
            if (objectInDatabase != objectInMemory) {
                CacheKey cacheKeyForObjectInDatabase = null;
                CacheKey cacheKeyForObjectInMemory = new CacheKey(new Vector());
    
                cacheKeyForObjectInDatabase = new CacheKey(getPrimaryKeyForObject(objectInDatabase, query.getSession()));
    
                if (objectInMemory != null) {
                    cacheKeyForObjectInMemory = new CacheKey(getPrimaryKeyForObject(objectInMemory, query.getSession()));
                }
    
                if (cacheKeysAreEqual(cacheKeyForObjectInDatabase, cacheKeyForObjectInMemory)) {
                    return;
                }
            } else {
                return;
            }
        }            
            
        if (query.shouldCascadeOnlyDependentParts()) {
            query.getSession().getCommitManager().addObjectToDelete(objectInDatabase);
        } else {
            query.getSession().deleteObject(objectInDatabase);
        }
    }

    /**
     * INTERNAL:
     * Delete the referenced objects.
     */
    public void preDelete(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        // Deletion takes place according the the cascading policy
        if (!shouldObjectModifyCascadeToPartsForPreDelete(query)) {
            return;
        }

        // Get the referenced objects.
        Object objectInMemory = getRealAttributeValueFromObject(query.getObject(), query.getSession());
        Object objectFromDatabase = null;

        // Because the value in memory may have been changed we check the previous value or database value.
        objectFromDatabase = readPrivateOwnedForObject(query);

        // If the value was changed, both values must be deleted (uow will have inserted the new one).
        if ((objectFromDatabase != null) && (objectFromDatabase != objectInMemory)) {
            // Also check pk as may not be maintaining identity.			
            CacheKey cacheKeyForObjectInDatabase = null;
            CacheKey cacheKeyForObjectInMemory = new CacheKey(new Vector());

            cacheKeyForObjectInDatabase = new CacheKey(getPrimaryKeyForObject(objectFromDatabase, query.getSession()));

            if (objectInMemory != null) {
                cacheKeyForObjectInMemory = new CacheKey(getPrimaryKeyForObject(objectInMemory, query.getSession()));
            }
            if (!cacheKeysAreEqual(cacheKeyForObjectInMemory, cacheKeyForObjectInDatabase)) {
                if (objectFromDatabase != null) {
                    // Make sure only objects sheduled for deletion are deleted
                    if (shouldObjectDeleteCascadeToPart(query, objectFromDatabase)) {
                        DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
                        deleteQuery.setObject(objectFromDatabase);
                        deleteQuery.setCascadePolicy(query.getCascadePolicy());
                        query.getSession().executeQuery(deleteQuery);
                    }
                }
            }
        }

        if (!isForeignKeyRelationship()) {
            if (objectInMemory != null) {
                // Make sure only objects sheduled for deletion are deleted
                if (shouldObjectDeleteCascadeToPart(query, objectInMemory)) {
                    DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
                    deleteQuery.setObject(objectInMemory);
                    deleteQuery.setCascadePolicy(query.getCascadePolicy());
                    query.getSession().executeQuery(deleteQuery);
                }
            }
        } else {
            // The actual deletion of part takes place in postDelete(...).
            if (objectInMemory != null) {
                query.setProperty(this, objectInMemory);
            }
        }
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects){
        Object attributeValue = getAttributeValueFromObject(object);
        if (attributeValue != null && this.isCascadeRemove() ){
            Object reference = getIndirectionPolicy().getRealAttributeValueFromObject(object, attributeValue);
            if (reference != null && (! visitedObjects.contains(reference)) ){
                visitedObjects.put(reference, reference);
                uow.performRemove(reference, visitedObjects);
            }
        }
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects){
        Object attributeValue = getAttributeValueFromObject(object);
        if (attributeValue != null && this.isCascadePersist() && getIndirectionPolicy().objectIsInstantiated(attributeValue)){
            Object reference = getIndirectionPolicy().getRealAttributeValueFromObject(object, attributeValue);
            uow.registerNewObjectForPersist(reference, visitedObjects);
        }
    }

    /**
     * INTERNAL:
     */
    protected boolean cacheKeysAreEqual(CacheKey cacheKey1, CacheKey cacheKey2) {
        return cacheKey1.equals(cacheKey2);
    }

    /**
     * INTERNAL:
     */
    protected Vector getPrimaryKeyForObject(Object object, AbstractSession session) {
        return getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(object, session);
    }

    /**
     * INTERNAL:
     * The returns if the mapping has any constraint dependencies, such as foreign keys and join tables.
     */
    public boolean hasConstraintDependency() {
        return isForeignKeyRelationship();
    }

    /**
     * INTERNAL:
     * Builder the unit of work value holder.
     * @param buildDirectlyFromRow indicates that we are building the clone directly
     * from a row as opposed to building the original from the row, putting it in
     * the shared cache, and then cloning the original.
     */
    public UnitOfWorkValueHolder createUnitOfWorkValueHolder(ValueHolderInterface attributeValue, Object original, Object clone, AbstractRecord row, UnitOfWorkImpl unitOfWork, boolean buildDirectlyFromRow) {
        UnitOfWorkQueryValueHolder valueHolder = null;
        if ((row == null) && (getDescriptor().getObjectBuilder().isPrimaryKeyMapping(this))) {
            // The row must be built if a primary key mapping for remote case.
            AbstractRecord rowFromTargetObject = extractPrimaryKeyRowForSourceObject(original, unitOfWork);
            valueHolder = new UnitOfWorkQueryValueHolder(attributeValue, clone, this, rowFromTargetObject, unitOfWork);
        } else {
            valueHolder = new UnitOfWorkQueryValueHolder(attributeValue, clone, this, row, unitOfWork);
        }

        // In case of joined attributes it so happens that the attributeValue 
        // contains a registered clone, as valueFromRow was called with a
        // UnitOfWork.  So switch the values.
        // Note that this UOW valueholder starts off as instantiated but that
        // is fine, for the reality is that it is.
        if (buildDirectlyFromRow && attributeValue.isInstantiated()) {
            Object cloneAttributeValue = attributeValue.getValue();
            valueHolder.privilegedSetValue(cloneAttributeValue);
            valueHolder.setInstantiated();

            // PERF: Do not modify the original value-holder, it is never used.
        }
        return valueHolder;
    }

    /**
     * INTERNAL:
     * Extract the reference pk for rvh usage in remote model.
     */
    public AbstractRecord extractPrimaryKeyRowForSourceObject(Object domainObject, AbstractSession session) {
        AbstractRecord databaseRow = getDescriptor().getObjectBuilder().createRecord();
        writeFromObjectIntoRow(domainObject, databaseRow, session);
        return databaseRow;
    }

    /**
     * INTERNAL:
     * Extract the reference pk for rvh usage in remote model.
     */
    public Vector extractPrimaryKeysForReferenceObject(Object domainObject, AbstractSession session) {
        return getIndirectionPolicy().extractPrimaryKeyForReferenceObject(getAttributeValueFromObject(domainObject), session);
    }

    /**
     * INTERNAL:
     *    Return the primary key for the reference object (i.e. the object
     * object referenced by domainObject and specified by mapping).
     * This key will be used by a RemoteValueHolder.
     */
    public Vector extractPrimaryKeysForReferenceObjectFromRow(AbstractRecord row) {
        return new Vector(1);
    }

    /**
     * INTERNAL:
     * Extract the reference pk for rvh usage in remote model.
     */
    public Vector extractPrimaryKeysFromRealReferenceObject(Object object, AbstractSession session) {
        if (object == null) {
            return new Vector(1);
        } else {
            Object implementation = getReferenceDescriptor().getObjectBuilder().unwrapObject(object, session);
            return getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(implementation, session);
        }
    }

    /**
     * INTERNAL:
     * Insert the referenced objects.
     */
    protected void insert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        // Insertion takes place according the the cascading policy
        if (!shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // Get the privately owned parts
        Object object = getRealAttributeValueFromObject(query.getObject(), query.getSession());

        if (object == null) {
            return;
        }
        ObjectChangeSet changeSet = query.getObjectChangeSet();
        if (changeSet != null) {
            ObjectReferenceChangeRecord changeRecord = (ObjectReferenceChangeRecord)query.getObjectChangeSet().getChangesForAttributeNamed(getAttributeName());
            if (changeRecord != null) {
                changeSet = (ObjectChangeSet)changeRecord.getNewValue();
            } else {
                // no changeRecord no reference.
                return;
            }
        } else {
            UnitOfWorkChangeSet uowChangeSet = null;

            // get changeSet for referenced object.  Could get it from the changeRecord but that would as much work
            if (query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
                uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
                changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(object);
            }
        }

        WriteObjectQuery writeQuery = null;
        if (isPrivateOwned()) {
            // no identity check needed for private owned
            writeQuery = new InsertObjectQuery();
        } else {
            writeQuery = new WriteObjectQuery();
        }
        writeQuery.setObject(object);
        writeQuery.setObjectChangeSet(changeSet);
        writeQuery.setCascadePolicy(query.getCascadePolicy());
        query.getSession().executeQuery(writeQuery);
    }

    /**
     * INTERNAL:
     * Update the private owned part.
     */
    protected void update(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // If objects are not instantiated that means they are not changed.
        if (!isAttributeValueInstantiated(query.getObject())) {
            return;
        }

        // Get the privately owned parts in the memory
        Object object = getRealAttributeValueFromObject(query.getObject(), query.getSession());
        if (object != null) {
            ObjectChangeSet changeSet = query.getObjectChangeSet();
            if (changeSet != null) {
                ObjectReferenceChangeRecord changeRecord = (ObjectReferenceChangeRecord)query.getObjectChangeSet().getChangesForAttributeNamed(getAttributeName());
                if (changeRecord != null) {
                    changeSet = (ObjectChangeSet)changeRecord.getNewValue();
                } else {
                    // no changeRecord no change to reference.
                    return;
                }
            } else {
                UnitOfWorkChangeSet uowChangeSet = null;

                // get changeSet for referenced object.  Could get it from the changeRecord but that would as much work
                if (query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
                    uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
                    changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(object);
                }
            }
            WriteObjectQuery writeQuery = new WriteObjectQuery();
            writeQuery.setObject(object);
            writeQuery.setObjectChangeSet(changeSet);
            writeQuery.setCascadePolicy(query.getCascadePolicy());
            query.getSession().executeQuery(writeQuery);
        }
    }

    /**
     * INTERNAL:
     * To verify if the specified object is deleted or not.
     */
    public boolean verifyDelete(Object object, AbstractSession session) throws DatabaseException {
        if (isPrivateOwned()) {
            Object attributeValue = getRealAttributeValueFromObject(object, session);

            if (attributeValue != null) {
                return session.verifyDelete(attributeValue);
            }
        }

        return true;
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     * But before that check if the reference object is instantiated or not.
     */
    public void writeFromObjectIntoRowForUpdate(WriteObjectQuery query, AbstractRecord databaseRow) {
        Object object = query.getObject();
        AbstractSession session = query.getSession();

        if (!isAttributeValueInstantiated(object)) {
            return;
        }

        if (session.isUnitOfWork()) {
            if (compareObjectsWithoutPrivateOwned(query.getBackupClone(), object, session)) {
                return;
            }
        }

        writeFromObjectIntoRow(object, databaseRow, session);
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRowForWhereClause(ObjectLevelModifyQuery query, AbstractRecord databaseRow) {
        if (isReadOnly()) {
            return;
        }

        if (query.isDeleteObjectQuery()) {
            writeFromObjectIntoRow(query.getObject(), databaseRow, query.getSession());
        } else {
            // If the original was never instantiated the backup clone has a ValueHolder of null 
            // so for this case we must extract from the original object.
            if (isAttributeValueInstantiated(query.getObject())) {
                writeFromObjectIntoRow(query.getBackupClone(), databaseRow, query.getSession());
            } else {
                writeFromObjectIntoRow(query.getObject(), databaseRow, query.getSession());
            }
        }
    }
    
    /**
     * INTERNAL:
     * Return if this mapping supports change tracking.
     */
    public boolean isChangeTrackingSupported() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Either create a new change record or update the change record with the new value.
     * This is used by attribute change tracking.
     */
    public void updateChangeRecord(Object clone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        // Must ensure values are unwrapped.
        Object unwrappedNewValue = newValue;
        Object unwrappedOldValue = oldValue;
        if (newValue != null) {
            unwrappedNewValue = getReferenceDescriptor().getObjectBuilder().unwrapObject(newValue, uow);
        }
        if (oldValue != null) {
            unwrappedOldValue = getReferenceDescriptor().getObjectBuilder().unwrapObject(oldValue, uow);
        }
        ObjectReferenceChangeRecord changeRecord = (ObjectReferenceChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (changeRecord == null) {
            changeRecord = internalBuildChangeRecord(unwrappedNewValue, objectChangeSet, uow);
            changeRecord.setOldValue(unwrappedOldValue);
            objectChangeSet.addChange(changeRecord);
            
        } else {
            setNewValueInChangeRecord(unwrappedNewValue, changeRecord, objectChangeSet, uow);
        }
    }

    /**
     * INTERNAL:
     * Directly build a change record without comparison
     */
    public ChangeRecord buildChangeRecord(Object clone, ObjectChangeSet owner, AbstractSession session) {
        return internalBuildChangeRecord(getRealAttributeValueFromObject(clone, session), owner, session);
    }
}
