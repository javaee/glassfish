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
import oracle.toplink.essentials.indirection.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.internal.indirection.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.ObjectCopyingPolicy;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: Abstract class for relationship mappings which store collection of objects
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public abstract class CollectionMapping extends ForeignReferenceMapping implements ContainerMapping {
    /** Used for delete all in m-m, dc and delete all optimization in 1-m. */
    protected transient ModifyQuery deleteAllQuery;
    protected transient boolean hasCustomDeleteAllQuery;
    protected ContainerPolicy containerPolicy;
    protected transient boolean hasOrderBy;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public CollectionMapping() {
        this.selectionQuery = new ReadAllQuery();
        this.hasCustomDeleteAllQuery = false;
        this.containerPolicy = ContainerPolicy.buildPolicyFor(ClassConstants.Vector_class);
        this.hasOrderBy = false;
    }
    
    /**
     * PUBLIC:
     * Provide order support for queryKeyName in ascending order
     */
    public void addAscendingOrdering(String queryKeyName) {
        if (queryKeyName == null) {
            return;
        }
        
        ((ReadAllQuery)getSelectionQuery()).addAscendingOrdering(queryKeyName);
    }
    
    /**
     * PUBLIC:
     * Provide order support for queryKeyName in descending order.
     */
    public void addDescendingOrdering(String queryKeyName) {
        if (queryKeyName == null) {
            return;
        }
        
        ((ReadAllQuery)getSelectionQuery()).addDescendingOrdering(queryKeyName);
    }
    
    /**
     * PUBLIC:
     * Provide order support for queryKeyName in descending or ascending order.
     * Called from the EJBAnnotationsProcessor when an @OrderBy is found.
     */
    public void addOrderBy(String queryKeyName, boolean isDescending) {
        this.hasOrderBy = true;
        
        if (isDescending) {
            addDescendingOrdering(queryKeyName);
        } else {
            addAscendingOrdering(queryKeyName);
        }
    }
    
    /**
     * PUBLIC:
     * Provide order support for queryKeyName in ascending order.
     * Called from the EJBAnnotationsProcessor when an @OrderBy on an
     * aggregate is found.
     */
    public void addAggregateOrderBy(String aggregateName, String queryKeyName, boolean isDescending) {
        this.hasOrderBy = true;
        
        ReadAllQuery readAllQuery = (ReadAllQuery) getSelectionQuery();
        ExpressionBuilder builder = readAllQuery.getExpressionBuilder();
        Expression expression = builder.get(aggregateName).get(queryKeyName).toUpperCase();
        
        if (isDescending) {
            readAllQuery.addOrdering(expression.descending());
        } else {
            readAllQuery.addOrdering(expression.ascending());
        }
    }

    /**
     * INTERNAL:
     * Used during building the backup shallow copy to copy
     * the vector without re-registering the target objects.
     */
    public Object buildBackupCloneForPartObject(Object attributeValue, Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        // Check for null
        if (attributeValue == null) {
            return getContainerPolicy().containerInstance(1);
        } else {
            return getContainerPolicy().cloneFor(attributeValue);
        }
    }

    /**
     * INTERNAL:
     * Require for cloning, the part must be cloned.
     * Ignore the objects, use the attribute value.
     */
    public Object buildCloneForPartObject(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean isExisting) {
        ContainerPolicy containerPolicy = getContainerPolicy();
        if (attributeValue == null) {
            Object container = containerPolicy.containerInstance(1);
            return container;
        }
        Object clonedAttributeValue = containerPolicy.containerInstance(containerPolicy.sizeFor(attributeValue));

        // I need to synchronize here to prevent the collection from changing while I am cloning it.
        // This will occur when I am merging into the cache and I am instantiating a UOW valueHolder at the same time
        // I can not synchronize around the clone, as this will cause deadlocks, so I will need to copy the collection then create the clones
        // I will use a temporary collection to help speed up the process
        Object temporaryCollection = null;
        synchronized (attributeValue) {
            temporaryCollection = containerPolicy.cloneFor(attributeValue);
        }
        for (Object valuesIterator = containerPolicy.iteratorFor(temporaryCollection);
                 containerPolicy.hasNext(valuesIterator);) {
            Object cloneValue = buildElementClone(containerPolicy.next(valuesIterator, unitOfWork), unitOfWork, isExisting);
            containerPolicy.addInto(cloneValue, clonedAttributeValue, unitOfWork);
        }
        return clonedAttributeValue;
    }

    /**
     * INTERNAL:
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    public void buildCopy(Object copy, Object original, ObjectCopyingPolicy policy) {
        Object attributeValue = getRealCollectionAttributeValueFromObject(original, policy.getSession());
        Object valuesIterator = getContainerPolicy().iteratorFor(attributeValue);
        attributeValue = getContainerPolicy().containerInstance(getContainerPolicy().sizeFor(attributeValue));
        while (getContainerPolicy().hasNext(valuesIterator)) {
            Object originalValue = getContainerPolicy().next(valuesIterator, policy.getSession());
            Object copyValue = originalValue;
            if (policy.shouldCascadeAllParts() || (policy.shouldCascadePrivateParts() && isPrivateOwned())) {
                copyValue = policy.getSession().copyObject(originalValue, policy);
            } else {
                // Check for backrefs to copies.
                copyValue = policy.getCopies().get(originalValue);
                if (copyValue == null) {
                    copyValue = originalValue;
                }
            }
            getContainerPolicy().addInto(copyValue, attributeValue, policy.getSession());
        }
        setRealAttributeValueInObject(copy, attributeValue);
    }

    /**
     * INTERNAL:
     * Clone the element, if necessary.
     */
    protected Object buildElementClone(Object element, UnitOfWorkImpl unitOfWork, boolean isExisting) {
        // optimize registration to knowledge of existence
        if (isExisting) {
            return unitOfWork.registerExistingObject(element);
        } else {// not known whether existing or not
            return unitOfWork.registerObject(element);
        }
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects){
        Object cloneAttribute = null;
        cloneAttribute = getAttributeValueFromObject(object);
        if ((cloneAttribute == null) || (!this.isCascadeRemove())) {
            return;
        }

        ContainerPolicy cp = getContainerPolicy();
        Object cloneObjectCollection = null;
        cloneObjectCollection = getRealCollectionAttributeValueFromObject(object, uow);
        Object cloneIter = cp.iteratorFor(cloneObjectCollection);
        while (cp.hasNext(cloneIter)) {
            Object nextObject = cp.next(cloneIter, uow);
            if (nextObject != null && (! visitedObjects.contains(nextObject)) ){
                visitedObjects.put(nextObject, nextObject);
                uow.performRemove(nextObject, visitedObjects);
            }
        }
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects){
        Object cloneAttribute = null;
        cloneAttribute = getAttributeValueFromObject(object);
        if ((cloneAttribute == null) || (!this.isCascadePersist()) || (!getIndirectionPolicy().objectIsInstantiated(cloneAttribute))) {
            return;
        }

        ContainerPolicy cp = getContainerPolicy();
        Object cloneObjectCollection = null;
        cloneObjectCollection = getRealCollectionAttributeValueFromObject(object, uow);
        Object cloneIter = cp.iteratorFor(cloneObjectCollection);
        while (cp.hasNext(cloneIter)) {
            Object nextObject = cp.next(cloneIter, uow);
            uow.registerNewObjectForPersist(nextObject, visitedObjects);
        }
    }
    
    /**
     * INTERNAL:
     * Common validation for a collection mapping using a Map class.
     */
    private void checkMapClass(Class concreteClass) {
        // the reference class has to be specified before coming here
        if (getReferenceClass() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }
        
        if (! Helper.classImplementsInterface(concreteClass, ClassConstants.Map_Class)) {
            throw ValidationException.illegalContainerClass(concreteClass);
        }
    }

    /**
     * INTERNAL:
     * Used by AttributeLevelChangeTracking to update a changeRecord with calculated changes
     * as apposed to detected changes.  If an attribute can not be change tracked it's
     * changes can be detected through this process.
     */
    public void calculateDeferredChanges(ChangeRecord changeRecord, AbstractSession session){
        CollectionChangeRecord collectionRecord = (CollectionChangeRecord) changeRecord;
        //clear incase events were fired since the set of the collection
//        collectionRecord.getAddObjectList().clear();
//        collectionRecord.getRemoveObjectList().clear();
        compareCollectionsForChange(collectionRecord.getOriginalCollection(), collectionRecord.getLatestCollection(), collectionRecord, session);
    }

    /**
     * INTERNAL:
     * Cascade the merge to the component object, if appropriate.
     */
    public void cascadeMerge(Object sourceElement, MergeManager mergeManager) {
        if (shouldMergeCascadeParts(mergeManager)) {
            mergeManager.mergeChanges(mergeManager.getObjectToMerge(sourceElement), null);
        }
    }

    /**
     * INTERNAL:
     * This method is used to calculate the differences between two collections.
     * It is passed to the container policy to calculate the changes.
     */
    public void compareCollectionsForChange(Object oldCollection, Object newCollection, ChangeRecord changeRecord, AbstractSession session) {
        getContainerPolicy().compareCollectionsForChange(oldCollection, newCollection, (CollectionChangeRecord) changeRecord, session, getReferenceDescriptor());
    }
    
    /**
     * INTERNAL:
     * This method is used to create a change record from comparing two collections
     * @return prototype.changeset.ChangeRecord
     */
    public ChangeRecord compareForChange(Object clone, Object backUp, ObjectChangeSet owner, AbstractSession session) {
        Object cloneAttribute = null;
        Object backUpAttribute = null;

        Object backUpObjectCollection = null;

        cloneAttribute = getAttributeValueFromObject(clone);

        if ((cloneAttribute != null) && (!getIndirectionPolicy().objectIsInstantiated(cloneAttribute))) {
            return null;
        }


        if (!owner.isNew()) {// if the changeSet is for a new object then we must record all off the attributes
            backUpAttribute = getAttributeValueFromObject(backUp);

            if ((cloneAttribute == null) && (backUpAttribute == null)) {
                return null;
            }

            backUpObjectCollection = getRealCollectionAttributeValueFromObject(backUp, session);
       }

        Object cloneObjectCollection = null;
        if (cloneAttribute != null) {
            cloneObjectCollection = getRealCollectionAttributeValueFromObject(clone, session);
        } else {
            cloneObjectCollection = getContainerPolicy().containerInstance(1);
        }

        CollectionChangeRecord changeRecord = new CollectionChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        compareCollectionsForChange(backUpObjectCollection, cloneObjectCollection, changeRecord, session);
        if (changeRecord.hasChanges()) {
            return changeRecord;
        }
        return null;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        Object firstObjectCollection = getRealCollectionAttributeValueFromObject(firstObject, session);
        Object secondObjectCollection = getRealCollectionAttributeValueFromObject(secondObject, session);

        return super.compareObjects(firstObjectCollection, secondObjectCollection, session);
    }

    /**
     * INTERNAL:
     * The memory objects are compared and only the changes are written to the database
     */
    protected void compareObjectsAndWrite(Object previousObjects, Object currentObjects, WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        ContainerPolicy cp = getContainerPolicy();
        
        // If it is for an aggregate collection let it continue so that all of 
        // the correct values are deleted and then re-added  This could be 
        // changed to make AggregateCollection changes smarter.
        if ((query.getObjectChangeSet() != null) && !this.isAggregateCollectionMapping()) {
            ObjectChangeSet changeSet = query.getObjectChangeSet();
            CollectionChangeRecord record = (CollectionChangeRecord)changeSet.getChangesForAttributeNamed(this.getAttributeName());
            
            if (record != null) {
                ObjectChangeSet removedChangeSet = null;
                ObjectChangeSet addedChangeSet = null;
                UnitOfWorkChangeSet uowChangeSet = (UnitOfWorkChangeSet)changeSet.getUOWChangeSet();
                Enumeration removedObjects = record.getRemoveObjectList().elements();
                
                while (removedObjects.hasMoreElements()) {
                    removedChangeSet = (ObjectChangeSet)removedObjects.nextElement();
                    objectRemovedDuringUpdate(query, removedChangeSet.getUnitOfWorkClone());
                }

                Enumeration addedObjects = record.getAddObjectList().elements();
                
                while (addedObjects.hasMoreElements()) {
                    addedChangeSet = (ObjectChangeSet)addedObjects.nextElement();
                    objectAddedDuringUpdate(query, addedChangeSet.getUnitOfWorkClone(), addedChangeSet);
                }
            }
            
            return;
        }

        Hashtable previousObjectsByKey = new Hashtable(cp.sizeFor(previousObjects) + 2);// Read from db or from backup in uow.
        Hashtable currentObjectsByKey = new Hashtable(cp.sizeFor(currentObjects) + 2);// Current value of object's attribute (clone in uow).

        IdentityHashtable cacheKeysOfCurrentObjects = new IdentityHashtable(cp.sizeFor(currentObjects) + 1);

        // First index the current objects by their primary key.
        for (Object currentObjectsIter = cp.iteratorFor(currentObjects);
                 cp.hasNext(currentObjectsIter);) {
            Object currentObject = cp.next(currentObjectsIter, query.getSession());
            try {
                Vector primaryKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(currentObject, query.getSession());
                CacheKey key = new CacheKey(primaryKey);
                currentObjectsByKey.put(key, currentObject);
                cacheKeysOfCurrentObjects.put(currentObject, key);
            } catch (NullPointerException e) {
                // For CR#2646 quietly discard nulls added to a collection mapping.
                // This try-catch is essentially a null check on currentObject, for
                // ideally the customer should check for these themselves.
                if (currentObject != null) {
                    throw e;
                }
            }
        }

        // Next index the previous objects (read from db or from backup in uow)
        // and process the difference to current (optimized in same loop).
        for (Object previousObjectsIter = cp.iteratorFor(previousObjects);
                 cp.hasNext(previousObjectsIter);) {
            Object previousObject = cp.next(previousObjectsIter, query.getSession());
            Vector primaryKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(previousObject, query.getSession());
            CacheKey key = new CacheKey(primaryKey);
            previousObjectsByKey.put(key, previousObject);

            // Delete must occur first, incase object with same pk is removed and added,
            // (technically should not happen, but same applies to unquie constainsts)
            if (!currentObjectsByKey.containsKey(key)) {
                objectRemovedDuringUpdate(query, previousObject);
            }
        }

        for (Object currentObjectsIter = cp.iteratorFor(currentObjects);
                 cp.hasNext(currentObjectsIter);) {
            Object currentObject = cp.next(currentObjectsIter, query.getSession());
            try {
                CacheKey cacheKey = (CacheKey)cacheKeysOfCurrentObjects.get(currentObject);

                if (!(previousObjectsByKey.containsKey(cacheKey))) {
                    objectAddedDuringUpdate(query, currentObject, null);
                } else {
                    objectUnchangedDuringUpdate(query, currentObject, previousObjectsByKey, cacheKey);
                }
            } catch (NullPointerException e) {
                // For CR#2646 skip currentObject if it is null.
                if (currentObject != null) {
                    throw e;
                }
            }
        }
    }

    /**
     * Compare two objects if their parts are not private owned
     */
    protected boolean compareObjectsWithoutPrivateOwned(Object firstCollection, Object secondCollection, AbstractSession session) {
        ContainerPolicy cp = getContainerPolicy();
        if (cp.sizeFor(firstCollection) != cp.sizeFor(secondCollection)) {
            return false;
        }

        Object firstIter = cp.iteratorFor(firstCollection);
        Object secondIter = cp.iteratorFor(secondCollection);

        Vector keyValue = new Vector();

        while (cp.hasNext(secondIter)) {
            Object secondObject = cp.next(secondIter, session);
            Vector primaryKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(secondObject, session);
            keyValue.addElement(new CacheKey(primaryKey));
        }

        while (cp.hasNext(firstIter)) {
            Object firstObject = cp.next(firstIter, session);
            Vector primaryKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(firstObject, session);

            if (!keyValue.contains(new CacheKey(primaryKey))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare two objects if their parts are private owned
     */
    protected boolean compareObjectsWithPrivateOwned(Object firstCollection, Object secondCollection, AbstractSession session) {
        ContainerPolicy cp = getContainerPolicy();
        if (cp.sizeFor(firstCollection) != cp.sizeFor(secondCollection)) {
            return false;
        }

        Object firstIter = cp.iteratorFor(firstCollection);
        Object secondIter = cp.iteratorFor(secondCollection);

        Hashtable keyValueToObject = new Hashtable(cp.sizeFor(firstCollection) + 2);
        CacheKey cacheKey;

        while (cp.hasNext(secondIter)) {
            Object secondObject = cp.next(secondIter, session);
            Vector primaryKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(secondObject, session);
            keyValueToObject.put(new CacheKey(primaryKey), secondObject);
        }

        while (cp.hasNext(firstIter)) {
            Object firstObject = cp.next(firstIter, session);
            Vector primaryKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(firstObject, session);
            cacheKey = new CacheKey(primaryKey);

            if (keyValueToObject.containsKey(cacheKey)) {
                Object object = keyValueToObject.get(cacheKey);

                if (!session.compareObjects(firstObject, object)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings
     * This method is implemented by subclasses as necessary.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        containerPolicy.convertClassNamesToClasses(classLoader);
    };

    /**
     * INTERNAL:
     * Returns the receiver's containerPolicy.
     */
    public ContainerPolicy getContainerPolicy() {
        return containerPolicy;
    }

    protected ModifyQuery getDeleteAllQuery() {
        if (deleteAllQuery == null) {
            deleteAllQuery = new DataModifyQuery();
        }
        return deleteAllQuery;
    }
 
    /**
     * INTERNAL:
     * Return the value of an attribute, unwrapping value holders if necessary.
     * Also check to ensure the collection is a vector.
     */
    public Object getRealAttributeValueFromObject(Object object, AbstractSession session) throws DescriptorException {
        Object value = super.getRealAttributeValueFromObject(object, session);
        if (value != null) {
            if (!getContainerPolicy().isValidContainer(value)) {
                throw DescriptorException.attributeTypeNotValid(this);
            }
        }
        return value;
    }

    /**
     * Convenience method.
     * Return the value of an attribute, unwrapping value holders if necessary.
     * If the value is null, build a new container.
     */
    public Object getRealCollectionAttributeValueFromObject(Object object, AbstractSession session) throws DescriptorException {
        Object value = this.getRealAttributeValueFromObject(object, session);
        if (value == null) {
            value = this.getContainerPolicy().containerInstance(1);
        }
        return value;
    }

    protected boolean hasCustomDeleteAllQuery() {
        return hasCustomDeleteAllQuery;
    }

    /**
     * INTERNAL:
     * Return true if ascending or descending ordering has been set on this 
     * mapping via the @OrderBy annotation.
     */
    public boolean hasOrderBy() {
        return hasOrderBy;
    }
    
    /**
     * INTERNAL:
     * Initialize the state of mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        setFields(collectFields());
        getContainerPolicy().prepare(getSelectionQuery(), session);

        // Check that the container policy is correct for the collection type.
        if ((!usesIndirection()) && (!getAttributeAccessor().getAttributeClass().isAssignableFrom(getContainerPolicy().getContainerClass()))) {
            throw DescriptorException.incorrectCollectionPolicy(this, getAttributeAccessor().getAttributeClass(), getContainerPolicy().getContainerClass());
        }
    }

    /**
     * INTERNAL:
     */
    public boolean isCollectionMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Iterate on the specified element.
     */
    public void iterateOnElement(DescriptorIterator iterator, Object element) {
        iterator.iterateReferenceObjectForMapping(element, this);
    }

    /**
     * INTERNAL:
     * Iterate on the attribute value.
     * The value holder has already been processed.
     */
    public void iterateOnRealAttributeValue(DescriptorIterator iterator, Object realAttributeValue) {
        if (realAttributeValue == null) {
            return;
        }
        ContainerPolicy cp = getContainerPolicy();
        for (Object iter = cp.iteratorFor(realAttributeValue); cp.hasNext(iter);) {
            iterateOnElement(iterator, cp.next(iter, iterator.getSession()));
        }
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object. Because this is a 
     * collection mapping, values are added to or removed from the collection 
     * based on the change set.
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord chgRecord, Object source, MergeManager mergeManager) {
        Object valueOfTarget = null;
        Object valueOfSource = null;
        AbstractSession parentSession = null;
        ContainerPolicy containerPolicy = getContainerPolicy();
        CollectionChangeRecord changeRecord = (CollectionChangeRecord) chgRecord;
        UnitOfWorkChangeSet uowChangeSet = (UnitOfWorkChangeSet)changeRecord.getOwner().getUOWChangeSet();

        // Collect the changes into a vector. Check to see if the target has an instantiated 
        // collection, if it does then iterate over the changes and merge the collections.
        if (isAttributeValueInstantiated(target)) {
            // If it is new will need a new collection.
            if (changeRecord.getOwner().isNew()) {
                valueOfTarget = containerPolicy.containerInstance(changeRecord.getAddObjectList().size());
            } else {
                valueOfTarget = getRealCollectionAttributeValueFromObject(target, mergeManager.getSession());
            }

            // Remove must happen before add to allow for changes in hash keys.
            // This is required to return the appropriate object from the parent when unwrapping.
            if (mergeManager.getSession().isUnitOfWork() && !mergeManager.shouldMergeWorkingCopyIntoBackup()) {
                parentSession = ((UnitOfWorkImpl)mergeManager.getSession()).getParent();
            } else {
                parentSession = mergeManager.getSession();
            }
            
            containerPolicy.mergeChanges(changeRecord, valueOfTarget, shouldMergeCascadeParts(mergeManager), mergeManager, parentSession);
        } else { 
            // The valueholder has not been instantiated
            if (mergeManager.shouldMergeChangesIntoDistributedCache()) {
                return; // do nothing
            }

            // If I'm not merging on another server then create instance of the collection.
            valueOfSource = getRealCollectionAttributeValueFromObject(source, mergeManager.getSession());
            Object iterator = containerPolicy.iteratorFor(valueOfSource);
            valueOfTarget = containerPolicy.containerInstance(containerPolicy.sizeFor(valueOfSource));
            
            while (containerPolicy.hasNext(iterator)) {
                // CR2195 - Problem with merging Collection mapping in unit of work and inheritance.
                Object objectToMerge = containerPolicy.next(iterator, mergeManager.getSession());
                
                ObjectChangeSet changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(objectToMerge);
                if (shouldMergeCascadeParts(mergeManager) && (valueOfSource != null)) {
                    mergeManager.mergeChanges(objectToMerge, changeSet);
                }

                // Let the mergemanager get it because I don't have the change for the object.
                // CR2188 - Problem with merging Collection mapping in unit of work and transparent indirection.
                containerPolicy.addInto(mergeManager.getTargetVersionOfSourceObject(objectToMerge), valueOfTarget, mergeManager.getSession());
            }
        }
        
        if (valueOfTarget == null) {
            valueOfTarget = containerPolicy.containerInstance();
        }
        
        setRealAttributeValueInObject(target, valueOfTarget);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object. This merge is only called when a changeSet for the target
     * does not exist or the target is uninitialized
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

        Object valueOfSource = getRealCollectionAttributeValueFromObject(source, mergeManager.getSession());

        // There is a very special case when merging into the shared cache that the original
        // has been refreshed and now has non-instantiated indirection objects.
        // Force instantiation is not necessary and can cause problem with JTS drivers.
        AbstractSession mergeSession = mergeManager.getSession();
        Object valueOfTarget = getRealCollectionAttributeValueFromObject(target, mergeSession);
        ContainerPolicy containerPolicy = getContainerPolicy();
        boolean fireChangeEvents = false;
        if (! mergeManager.shouldMergeOriginalIntoWorkingCopy()){
   	        // if we are copying from original to clone then the source will be     
            // instantiated anyway and we must continue to use the UnitOfWork 
            // valueholder in the case of transparent indirection
            Object newContainer = containerPolicy.containerInstance(containerPolicy.sizeFor(valueOfSource));
            valueOfTarget = newContainer;
        }else{
            //bug 3953038 - set a new collection in the object until merge completes, this
            //              prevents rel-maint. from adding duplicates.
            setRealAttributeValueInObject(target, containerPolicy.containerInstance(containerPolicy.sizeFor(valueOfSource)));
            containerPolicy.clear(valueOfTarget);
        }

        synchronized(valueOfSource){
            Object sourceIterator = containerPolicy.iteratorFor(valueOfSource);
            while (containerPolicy.hasNext(sourceIterator)) {
                Object object = containerPolicy.next(sourceIterator, mergeManager.getSession());
                if (object == null){
                    continue; // skip the null
                }
                if (shouldMergeCascadeParts(mergeManager)) {
                    if ((mergeManager.getSession().isUnitOfWork()) && (((UnitOfWorkImpl)mergeManager.getSession()).getUnitOfWorkChangeSet() != null)) {
                        // If it is a unit of work, we have to check if I have a change Set fot this object
                        mergeManager.mergeChanges(mergeManager.getObjectToMerge(object), (ObjectChangeSet)((UnitOfWorkImpl)mergeManager.getSession()).getUnitOfWorkChangeSet().getObjectChangeSetForClone(object));
                    } else {
                        mergeManager.mergeChanges(mergeManager.getObjectToMerge(object), null);
                    }
                }
                object = getReferenceDescriptor().getObjectBuilder().wrapObject(mergeManager.getTargetVersionOfSourceObject(object), mergeManager.getSession());
                synchronized (valueOfTarget){
                    containerPolicy.addInto(object, valueOfTarget, mergeManager.getSession());
                }
            }
        }
        // Must re-set variable to allow for set method to re-morph changes if the collection is not being stored directly.
        setRealAttributeValueInObject(target, valueOfTarget);
    }

    /**
     * INTERNAL:
     * An object was added to the collection during an update, insert it if private.
     */
    protected void objectAddedDuringUpdate(ObjectLevelModifyQuery query, Object objectAdded, ObjectChangeSet changeSet) throws DatabaseException, OptimisticLockException {
        if (!shouldObjectModifyCascadeToParts(query)) {// Called always for M-M
            return;
        }

        // Only cascade dependents writes in uow.
        if (query.shouldCascadeOnlyDependentParts()) {
            return;
        }

        // Insert must not be done for uow or cascaded queries and we must cascade to cascade policy.
        // We should distiguish between insert and write (optimization/paraniod).
        if (isPrivateOwned()) {
            InsertObjectQuery insertQuery = new InsertObjectQuery();
            insertQuery.setObject(objectAdded);
            insertQuery.setCascadePolicy(query.getCascadePolicy());
            query.getSession().executeQuery(insertQuery);
        } else {
            // Always write for updates, either private or in uow if calling this method.
            UnitOfWorkChangeSet uowChangeSet = null;
            if ((changeSet == null) && query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
                uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
                changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(query.getObject());
            }
            WriteObjectQuery writeQuery = new WriteObjectQuery();
            writeQuery.setObject(objectAdded);
            writeQuery.setObjectChangeSet(changeSet);
            writeQuery.setCascadePolicy(query.getCascadePolicy());
            query.getSession().executeQuery(writeQuery);
        }
    }

    /**
     * INTERNAL:
     * An object was removed to the collection during an update, delete it if private.
     */
    protected void objectRemovedDuringUpdate(ObjectLevelModifyQuery query, Object objectDeleted) throws DatabaseException, OptimisticLockException {
        if (isPrivateOwned()) {// Must check ownership for uow and cascading.
            if (query.shouldCascadeOnlyDependentParts()) {
                // If the session is a unit of work
                if (query.getSession().isUnitOfWork()) {
                    // ...and the object has not been explictly deleted in the unit of work
                    if (!(((UnitOfWorkImpl)query.getSession()).getDeletedObjects().containsKey(objectDeleted))) {
                        query.getSession().getCommitManager().addObjectToDelete(objectDeleted);
                    }
                } else {
                    query.getSession().getCommitManager().addObjectToDelete(objectDeleted);
                }
            } else {
                query.getSession().deleteObject(objectDeleted);
            }
        }
    }

    /**
     * INTERNAL:
     * An object is still in the collection, update it as it may have changed.
     */
    protected void objectUnchangedDuringUpdate(ObjectLevelModifyQuery query, Object object) throws DatabaseException, OptimisticLockException {
        if (!shouldObjectModifyCascadeToParts(query)) {// Called always for M-M
            return;
        }

        // Only cascade dependents writes in uow.
        if (query.shouldCascadeOnlyDependentParts()) {
            return;
        }

        // Always write for updates, either private or in uow if calling this method.
        WriteObjectQuery writeQuery = new WriteObjectQuery();
        writeQuery.setObject(object);
        writeQuery.setCascadePolicy(query.getCascadePolicy());
        query.getSession().executeQuery(writeQuery);
    }

    /**
     * INTERNAL:
     * copies the non primary key information into the row currently used only in ManyToMany
     */
    protected void prepareTranslationRow(AbstractRecord translationRow, Object object, AbstractSession session) {
        //Do nothing for the generic Collection Mapping
    }

    /**
     * INTERNAL:
     * An object is still in the collection, update it as it may have changed.
     */
    protected void objectUnchangedDuringUpdate(ObjectLevelModifyQuery query, Object object, Hashtable backupclones, CacheKey keys) throws DatabaseException, OptimisticLockException {
        objectUnchangedDuringUpdate(query, object);
    }

    /**
     * INTERNAL:
     * All the privately owned parts are read
     */
    protected Object readPrivateOwnedForObject(ObjectLevelModifyQuery modifyQuery) throws DatabaseException {
        if (modifyQuery.getSession().isUnitOfWork()) {
            return getRealCollectionAttributeValueFromObject(modifyQuery.getBackupClone(), modifyQuery.getSession());
        } else {
            // cr 3819
            prepareTranslationRow(modifyQuery.getTranslationRow(), modifyQuery.getObject(), modifyQuery.getSession());
            return modifyQuery.getSession().executeQuery(getSelectionQuery(), modifyQuery.getTranslationRow());
        }
    }

    /**
     * ADVANCED:
     * Configure the mapping to use a container policy.
     * The policy manages the access to the collection.
     */
    public void setContainerPolicy(ContainerPolicy containerPolicy) {
        this.containerPolicy = containerPolicy;
        ((ReadAllQuery)getSelectionQuery()).setContainerPolicy(containerPolicy);
    }

    /**
     * PUBLIC:
     * The default delete all query for mapping can be overridden by specifying the new query.
     * This query is responsible for doing the deletion required by the mapping,
     * such as deletion of all the rows from join table for M-M, or optimized delete all of target objects for 1-M.
     */
    public void setCustomDeleteAllQuery(ModifyQuery query) {
        setDeleteAllQuery(query);
        setHasCustomDeleteAllQuery(true);
    }

    protected void setDeleteAllQuery(ModifyQuery query) {
        deleteAllQuery = query;
    }

    /**
     * PUBLIC:
     * Set the receiver's delete all SQL string. This allows the user to override the SQL
     * generated by TopLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row, through replacing the field names
     * marked by '#' with the values for those fields.
     * This SQL is responsible for doing the deletion required by the mapping,
     * such as deletion of all the rows from join table for M-M, or optimized delete all of target objects for 1-M.
     * Example, 'delete from PROJ_EMP where EMP_ID = #EMP_ID'.
     */
    public void setDeleteAllSQLString(String sqlString) {
        DataModifyQuery query = new DataModifyQuery();
        query.setSQLString(sqlString);
        setCustomDeleteAllQuery(query);
    }
    
    /**
     * PUBLIC:
     * Set the receiver's delete all call. This allows the user to override the SQL
     * generated by TopLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row.
     * This call is responsible for doing the deletion required by the mapping,
     * such as deletion of all the rows from join table for M-M, or optimized delete all of target objects for 1-M.
     * Example, 'new SQLCall("delete from PROJ_EMP where EMP_ID = #EMP_ID")'.
     */
    public void setDeleteAllCall(Call call) {
        DataModifyQuery query = new DataModifyQuery();
        query.setCall(call);
        setCustomDeleteAllQuery(query);
    }
    
    protected void setHasCustomDeleteAllQuery(boolean bool) {
        hasCustomDeleteAllQuery = bool;
    }

    /**
     * PUBLIC:
     * Set the name of the session to execute the mapping's queries under.
     * This can be used by the session broker to override the default session
     * to be used for the target class.
     */
    public void setSessionName(String name) {
        getDeleteAllQuery().setSessionName(name);
        getSelectionQuery().setSessionName(name);
    }

    /**
     * ADVANCED:
     * This method is used to have an object add to a collection once the 
     * changeSet is applied. The referenceKey parameter should only be used for 
     * direct Maps.
     */
    public void simpleAddToCollectionChangeRecord(Object referenceKey, Object changeSetToAdd, ObjectChangeSet changeSet, AbstractSession session) {
        CollectionChangeRecord collectionChangeRecord = (CollectionChangeRecord)changeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new CollectionChangeRecord(changeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            collectionChangeRecord.getAddObjectList().put(changeSetToAdd, changeSetToAdd);
            collectionChangeRecord.getOrderedAddObjects().add(changeSetToAdd);
            changeSet.addChange(collectionChangeRecord);
        } else {
            getContainerPolicy().recordAddToCollectionInChangeRecord((ObjectChangeSet)changeSetToAdd, collectionChangeRecord);
        }
        if (referenceKey != null){
            ((ObjectChangeSet)changeSetToAdd).setNewKey(referenceKey);
        }
    }

    /**
     * ADVANCED:
     * This method is used to have an object removed from a collection once the 
     * changeSet is applied. The referenceKey parameter should only be used for 
     * direct Maps.
     */
    public void simpleRemoveFromCollectionChangeRecord(Object referenceKey, Object changeSetToRemove, ObjectChangeSet changeSet, AbstractSession session) {
        CollectionChangeRecord collectionChangeRecord = (CollectionChangeRecord)changeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new CollectionChangeRecord(changeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            collectionChangeRecord.getRemoveObjectList().put(changeSetToRemove, changeSetToRemove);
            changeSet.addChange(collectionChangeRecord);
        } else {
            getContainerPolicy().recordRemoveFromCollectionInChangeRecord((ObjectChangeSet)changeSetToRemove, collectionChangeRecord);
        }
        if (referenceKey != null){
            ((ObjectChangeSet)changeSetToRemove).setOldKey(referenceKey);
        }
    }

    /**
     * INTERNAL:
     * Either create a new change record or update with the new value.  This is used
     * by attribute change tracking.
     * Specifically in a collection mapping this will be called when the customer
     * Set a new collection.  In this case we will need to mark the change record
     * with the new and the old versions of the collection.
     * And mark the ObjectChangeSet with the attribute name then when the changes are calculated
     * force a compare on the collections to determine changes.
     */
    public void updateChangeRecord(Object clone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        CollectionChangeRecord collectionChangeRecord = (CollectionChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new CollectionChangeRecord(objectChangeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            objectChangeSet.addChange(collectionChangeRecord);
        }
        if (collectionChangeRecord.getOriginalCollection() == null){
            collectionChangeRecord.setOriginalCollection(oldValue);
        }
        collectionChangeRecord.setLatestCollection(newValue);
        
        objectChangeSet.deferredDetectionRequiredOn(getAttributeName());
    }

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>The container class must implement (directly or indirectly) the
     * <code>java.util.Collection</code> interface.
     */
    public void useCollectionClass(Class concreteClass) {
        ContainerPolicy policy = ContainerPolicy.buildPolicyFor(concreteClass, hasOrderBy());
        setContainerPolicy(policy);
    }

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>The container class must implement (directly or indirectly) the
     * <code>java.util.SortedSet</code> interface.
     */
    public void useSortedSetClass(Class concreteClass, Comparator comparator) {
        try {
            SortedCollectionContainerPolicy policy = (SortedCollectionContainerPolicy)ContainerPolicy.buildPolicyFor(concreteClass);
            policy.setComparator(comparator);
            setContainerPolicy(policy);
        } catch (ClassCastException e) {
            useCollectionClass(concreteClass);
        }
    }
    
    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container 
     * clas to hold the target objects. The key used to index a value in the
     * <code>Map</code> is the value returned by either a call to a 
     * specified zero-argument method or the value of a field.
     * <p> To facilitate resolving the keyName to a method or field, 
     * the mapping's referenceClass must set before calling this method.
     * <p> Note: If the keyName is for a method, that method must be implemented 
     * by the class (or a superclass) of any value to be inserted into the 
     * <code>Map</code>.
     * <p> The container class must implement (directly or indirectly) the
     * <code>java.util.Map</code> interface.
     */
    public void useMapClass(Class concreteClass, String keyName) {
        // the reference class has to be specified before coming here
        if (getReferenceClassName() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }
        
        ContainerPolicy policy = ContainerPolicy.buildPolicyFor(concreteClass);
        policy.setKeyName(keyName, getReferenceClassName());
        setContainerPolicy(policy);
    }
    
    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container 
     * class to hold the target objects. The key used to index a value in the
     * <code>Map</code> is an instance of the composite primary key class.
     * <p> To facilitate resolving the primary key class, the mapping's 
     * referenceClass must set before calling this method.
     * <p> The container class must implement (directly or indirectly) the
     * <code>java.util.Map</code> interface.
     */
    public void useMapClass(Class concreteClass) {
        useMapClass(concreteClass, null);
    }

    /**
     * PUBLIC:
     * If transparent indirection is used, a special collection will be placed in the source
     * object's attribute.
     * Fetching of the contents of the collection from the database will be delayed
     * until absolutely necessary. (Any message sent to the collection will cause
     * the contents to be faulted in from the database.)
     * This can result in rather significant performance gains, without having to change
     * the source object's attribute from Collection (or List or Vector) to
     * ValueHolderInterface.
     */
    public void useTransparentCollection() {
        setIndirectionPolicy(new TransparentIndirectionPolicy());
        useCollectionClass(ClassConstants.IndirectList_Class);
    }
    
    /**
     * PUBLIC:
     * If transparent indirection is used, a special collection will be placed in the source
     * object's attribute.
     * Fetching of the contents of the collection from the database will be delayed
     * until absolutely necessary. (Any message sent to the collection will cause
     * the contents to be faulted in from the database.)
     * This can result in rather significant performance gains, without having to change
     * the source object's attribute from Set to
     * ValueHolderInterface.
     */
    public void useTransparentSet() {
        setIndirectionPolicy(new TransparentIndirectionPolicy());
        useCollectionClass(IndirectSet.class);
    }
    
    /**
     * PUBLIC:
     * If transparent indirection is used, a special collection will be placed in the source
     * object's attribute.
     * Fetching of the contents of the collection from the database will be delayed
     * until absolutely necessary. (Any message sent to the collection will cause
     * the contents to be faulted in from the database.)
     * This can result in rather significant performance gains, without having to change
     * the source object's attribute from List to
     * ValueHolderInterface.
     */
    public void useTransparentList() {
        setIndirectionPolicy(new TransparentIndirectionPolicy());
        useCollectionClass(ClassConstants.IndirectList_Class);
    }

    /**
     * PUBLIC:
     * If transparent indirection is used, a special map will be placed in the source
     * object's attribute.
     * Fetching of the contents of the map from the database will be delayed
     * until absolutely necessary. (Any message sent to the map will cause
     * the contents to be faulted in from the database.)
     * This can result in rather significant performance gains, without having to change
     * the source object's attribute from Map (or Dictionary or Hashtable) to
     * ValueHolderInterface.<p>
     * The key used in the Map is the value returned by a call to the zero parameter
     * method named methodName. The method should be a zero argument method implemented (or
     * inherited) by the value to be inserted into the Map.
     */
    public void useTransparentMap(String methodName) {
        setIndirectionPolicy(new TransparentIndirectionPolicy());
        useMapClass(ClassConstants.IndirectMap_Class, methodName);
    }

    /**
     * INTERNAL:
     * To validate mappings declaration
     */
    public void validateBeforeInitialization(AbstractSession session) throws DescriptorException {
        super.validateBeforeInitialization(session);

        getIndirectionPolicy().validateContainerPolicy(session.getIntegrityChecker());

        if (getAttributeAccessor() instanceof InstanceVariableAttributeAccessor) {
            Class attributeType = ((InstanceVariableAttributeAccessor)getAttributeAccessor()).getAttributeType();
            getIndirectionPolicy().validateDeclaredAttributeTypeForCollection(attributeType, session.getIntegrityChecker());
        } else if (getAttributeAccessor() instanceof MethodAttributeAccessor) {
            Class returnType = ((MethodAttributeAccessor)getAttributeAccessor()).getGetMethodReturnType();
            getIndirectionPolicy().validateGetMethodReturnTypeForCollection(returnType, session.getIntegrityChecker());

            Class parameterType = ((MethodAttributeAccessor)getAttributeAccessor()).getSetMethodParameterType();
            getIndirectionPolicy().validateSetMethodParameterTypeForCollection(parameterType, session.getIntegrityChecker());
        }
    }

    /**
     * INTERNAL:
     * Checks if object is deleted from the database or not.
     */
    public boolean verifyDelete(Object object, AbstractSession session) throws DatabaseException {
        // Row is built for translation
        if (isReadOnly()) {
            return true;
        }

        if (isPrivateOwned()) {
            Object objects = getRealCollectionAttributeValueFromObject(object, session);

            ContainerPolicy containerPolicy = getContainerPolicy();
            for (Object iter = containerPolicy.iteratorFor(objects); containerPolicy.hasNext(iter);) {
                if (!session.verifyDelete(containerPolicy.next(iter, session))) {
                    return false;
                }
            }
        }

        AbstractRecord row = getDescriptor().getObjectBuilder().buildRowForTranslation(object, session);

        //cr 3819 added the line below to fix the translationtable to ensure that it
        // contains the required values
        prepareTranslationRow(row, object, session);
        Object value = session.executeQuery(getSelectionQuery(), row);

        return getContainerPolicy().isEmpty(value);
    }

    /**
     * INTERNAL:
     * Add a new value and its change set to the collection change record.  This is used by
     * attribute change tracking.
     */
    public void addToCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        if (newValue != null) {
            ClassDescriptor descriptor;
            //PERF: Use referenceDescriptor if it does not have inheritance
            if (!getReferenceDescriptor().hasInheritance()) {
                descriptor = getReferenceDescriptor();
            } else {
                descriptor = uow.getDescriptor(newValue);
            }
            newValue = descriptor.getObjectBuilder().unwrapObject(newValue, uow);
            ObjectChangeSet newSet = descriptor.getObjectBuilder().createObjectChangeSet(newValue, (UnitOfWorkChangeSet)objectChangeSet.getUOWChangeSet(), uow);
            simpleAddToCollectionChangeRecord(newKey, newSet, objectChangeSet, uow);
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
     * Remove a value and its change set from the collection change record.  This is used by
     * attribute change tracking.
     */
    public void removeFromCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        if (newValue != null) {
            ClassDescriptor descriptor;

            //PERF: Use referenceDescriptor if it does not have inheritance
            if (!getReferenceDescriptor().hasInheritance()) {
                descriptor = getReferenceDescriptor();
            } else {
                descriptor = uow.getDescriptor(newValue);
            }
            newValue = descriptor.getObjectBuilder().unwrapObject(newValue, uow);
            ObjectChangeSet newSet = descriptor.getObjectBuilder().createObjectChangeSet(newValue, (UnitOfWorkChangeSet)objectChangeSet.getUOWChangeSet(), uow);
            simpleRemoveFromCollectionChangeRecord(newKey, newSet, objectChangeSet, uow);
        }
    }

    /**
     * INTERNAL:
     * Directly build a change record without comparison
     */
    public ChangeRecord buildChangeRecord(Object clone, ObjectChangeSet owner, AbstractSession session) {
        Object cloneAttribute = null;
        cloneAttribute = getAttributeValueFromObject(clone);
        if ((cloneAttribute != null) && (!getIndirectionPolicy().objectIsInstantiated(cloneAttribute))) {
            return null;
        }

        // 2612538 - the default size of IdentityHashtable (32) is appropriate
        IdentityHashMap cloneKeyValues = new IdentityHashMap();
        ContainerPolicy cp = getContainerPolicy();
        Object cloneObjectCollection = null;
        if (cloneAttribute != null) {
            cloneObjectCollection = getRealCollectionAttributeValueFromObject(clone, session);
        } else {
            cloneObjectCollection = cp.containerInstance(1);
        }
        Object cloneIter = cp.iteratorFor(cloneObjectCollection);

        while (cp.hasNext(cloneIter)) {
            Object firstObject = cp.next(cloneIter, session);
            if (firstObject != null) {
                cloneKeyValues.put(firstObject, firstObject);
            }
        }

        CollectionChangeRecord changeRecord = new CollectionChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        changeRecord.addAdditionChange(cloneKeyValues, (UnitOfWorkChangeSet)owner.getUOWChangeSet(), session);
        if (changeRecord.hasChanges()) {
            return changeRecord;
        }
        return null;
    }

    /**
     * INTERNAL:
     * Indicates whether valueFromRow should call valueFromRowInternalWithJoin (true)
     * or valueFromRowInternal (false)
     */
    protected boolean shouldUseValueFromRowWithJoin(JoinedAttributeManager joinManager) {
        return joinManager.getDataResults_()!=null && super.shouldUseValueFromRowWithJoin(joinManager);
    }
    
    /**
     * INTERNAL:
     * Return the value of the field from the row or a value holder on the query to obtain the object.
     * To get here the mapping's isJoiningSupported() should return true,
     * currently that's the case for only 1-m and m-m.
     */
    protected Object valueFromRowInternalWithJoin(AbstractRecord row, JoinedAttributeManager joinManager, AbstractSession executionSession) throws DatabaseException {
        // If the query was using joining, all of the result rows will have been set.
        List rows = joinManager.getDataResults_();
        Object value = getContainerPolicy().containerInstance();
        
        // A nested query must be built to pass to the descriptor that looks like the real query execution would,
        // these should be cached on the query during prepare.
        ObjectLevelReadQuery nestedQuery = null;
        if (joinManager.getJoinedMappingQueries_() != null) {
            nestedQuery = (ObjectLevelReadQuery) joinManager.getJoinedMappingQueries_().get(this);
        } else {
            nestedQuery = prepareNestedJoins(joinManager, executionSession);
        }
        nestedQuery.setSession(executionSession);                
        //CR #4365 - used to prevent infinite recursion on refresh object cascade all
        nestedQuery.setQueryId(joinManager.getBaseQuery().getQueryId());

        // Extract the primary key of the source object, to filter only the joined rows for that object.
        Vector sourceKey = getDescriptor().getObjectBuilder().extractPrimaryKeyFromRow(row, executionSession);
        CacheKey sourceCacheKey = new CacheKey(sourceKey);
        
        // A set of target cache keys must be maintained to avoid duplicates from multiple 1-m joins.
        Set targetCacheKeys = new HashSet();

        // For each rows, extract the target row and build the target object and add to the collection.
        for (int index = 0; index < rows.size(); index++) {
            AbstractRecord sourceRow = (AbstractRecord)rows.get(index);
            AbstractRecord targetRow = sourceRow;
            
            // Row will be set to null if part of another object's join already processed.
            if (targetRow != null) {
                // CR #... the field for many objects may be in the row,
                // so build the subpartion of the row through the computed values in the query,
                // this also helps the field indexing match.
                targetRow = trimRowForJoin(targetRow, joinManager, executionSession);
                AbstractRecord pkRow = trimRowForJoin(sourceRow, new Integer(joinManager.getParentResultIndex()), executionSession);
                nestedQuery.setTranslationRow(targetRow);

                // Extract the primary key of the row to filter only the joined rows for the source object.
                Vector rowSourceKey = getDescriptor().getObjectBuilder().extractPrimaryKeyFromRow(pkRow, executionSession);
                if(rowSourceKey != null) {
                    CacheKey rowSourceCacheKey = new CacheKey(rowSourceKey);
                    
                    // Only build/add the object if the join row is for the object.
                    if (sourceCacheKey.equals(rowSourceCacheKey)) {
                        // Partial object queries must select the primary key of the source and related objects.
                        // If the target joined rows in null (outerjoin) means an empty collection.
                        Vector targetKey = getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromRow(targetRow, executionSession);
                        if (targetKey == null) {
                            // A null primary key means an empty collection returned as nulls from an outerjoin.
                            return getIndirectionPolicy().valueFromRow(value);
                        }
                        CacheKey targetCacheKey = new CacheKey(targetKey);
                        
                        // Only build/add the taregt object once, skip duplicates from multiple 1-m joins.
                        if (!targetCacheKeys.contains(targetCacheKey)) {
                            targetCacheKeys.add(targetCacheKey);
                            Object targetObject = getReferenceDescriptor().getObjectBuilder().buildObject(nestedQuery, targetRow, nestedQuery.getJoinedAttributeManager());
                            nestedQuery.setTranslationRow(null);
                            getContainerPolicy().addInto(targetObject, value, executionSession);
                        }
                    }
                } else {
                    // Clear an empty row
                    rows.set(index, null);
                }
            }
        }
        return getIndirectionPolicy().valueFromRow(value);
    }
}
