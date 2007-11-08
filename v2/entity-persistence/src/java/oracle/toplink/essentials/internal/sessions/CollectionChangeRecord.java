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

import oracle.toplink.essentials.internal.helper.IdentityHashtable;
import java.util.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p>
 * <b>Purpose</b>: This class holds the record of the changes made to a collection attribute of
 * an object.
 * <p>
 * <b>Description</b>: Collections must be compared to each other and added and removed objects must
 * be recorded seperately
 * @see OtherRelatedClasses prototype.changeset.DirectToFieldChangeRecord,prototype.changeset.SingleObjectChangeRecord
 */
public class CollectionChangeRecord extends ChangeRecord implements oracle.toplink.essentials.changesets.CollectionChangeRecord {

    /**
     * Contains the added values to the collection and their corresponding ChangeSets.
     */
    protected IdentityHashtable addObjectList;
    
    /** 
     * Contains the added values to the collection and their corresponding ChangeSets in order.
     */
    protected transient Vector orderedAddObjects;
    
    /**
     * Contains the added values index to the collection. 
     */
    protected IdentityHashtable orderedAddObjectIndices;
    
    /** 
     * Contains the removed values to the collection and their corresponding ChangeSets.
     */
    protected Hashtable orderedRemoveObjects;
    
    /**
     * Contains the removed values index to the collection. 
     */
    protected transient Vector orderedRemoveObjectIndices;
    
    /**
     * Contains a list of extra adds.  These extra adds are used by attribute change tracking
     * to replicate behaviour when someone adds the same object to a list and removes it once
     * In this case the object should still appear once in the change set.
     */
     protected transient List addOverFlow;

    /**
     * Contains the removed values from the collection and their corresponding ChangeSets.
     */
    protected IdentityHashtable removeObjectList;

    /**
     * Contain the same added values as in addObjectList.  It is only used by SDK mapping of this change record.
     */
    protected transient Vector sdkAddObjects;

    /**
     * Contain the same added values as in addObjectList.  It is only used by SDK mapping of this change record.
     */
    protected transient Vector sdkRemoveObjects;
    
    /**
     * Used for change tracking when customer sets entire collection
     */
    protected transient Object originalCollection;

    /**
     * Used for change tracking when customer sets entire collection
     */
    protected transient Object latestCollection;

    /**
     * This default constructor is reference internally by SDK XML project to mapp this class
     */
    public CollectionChangeRecord() {
        super();
    }

    /**
     * Constructor for the ChangeRecord representing a collection mapping
     * @param owner prototype.changeset.ObjectChangeSet the changeSet that uses this record
     */
    public CollectionChangeRecord(ObjectChangeSet owner) {
        this.owner = owner;
    }
    
    /**
     * This method takes a IdentityHashtable of objects, converts these into 
     * ObjectChangeSets.
     * @param objectChanges prototype.changeset.ObjectChangeSet
     */
    public void addAdditionChange(IdentityHashMap objectChanges, UnitOfWorkChangeSet changeSet, AbstractSession session) {
        Iterator enumtr = objectChanges.keySet().iterator();
        while (enumtr.hasNext()) {
            Object object = enumtr.next();
            ObjectChangeSet change = session.getDescriptor(object.getClass()).getObjectBuilder().createObjectChangeSet(object, changeSet, session);
            if (change.hasKeys()){
                // if change set has keys this is a map comparison.  Maps are
                // not support in change tracking so do not need to prevent duplicates
                // when map support is added this will have to be refactored
                getAddObjectList().put(change, change);
            }else{
                if (getRemoveObjectList().contains(change)){
                    getRemoveObjectList().remove(change);
                }else{
                    getAddObjectList().put(change, change);
                }
            }
        }
    }
    
    /**
     * INTERNAL:
     * This method takes a Vector of objects and converts them into 
     * ObjectChangeSets. This method should only be called from a 
     * ListContainerPolicy. Additions to the list are made by index, hence,
     * the second IdentityHashtable of objectChangesIndices.
     */
    public void addOrderedAdditionChange(Vector objectChanges, IdentityHashtable objectChangesIndices, UnitOfWorkChangeSet changeSet, AbstractSession session) {
        Enumeration e = objectChanges.elements();
        
        while (e.hasMoreElements()) {
            Object object = e.nextElement();
            ObjectChangeSet change = session.getDescriptor(object.getClass()).getObjectBuilder().createObjectChangeSet(object, changeSet, session);
            
            getOrderedAddObjects().add(change);
            getOrderedAddObjectIndices().put(change, (Integer) objectChangesIndices.get(object));
        }
    }
    
    /**
     * INTERNAL:
     * This method takes a Hashtable of objects and converts them into 
     * ObjectChangeSets. This method should only be called from a
     * ListContainerPolicy. Deletions from the list is made by index, hence,
     * the second Vector of indicesToRemove.
     */
    public void addOrderedRemoveChange(Vector indicesToRemove, Hashtable objectChanges, UnitOfWorkChangeSet changeSet, AbstractSession session) {
        orderedRemoveObjectIndices = indicesToRemove;
        Enumeration e = orderedRemoveObjectIndices.elements();
        
        while (e.hasMoreElements()) {
            Integer index = (Integer) e.nextElement();
            Object object = objectChanges.get(index);
            ObjectChangeSet change = session.getDescriptor(object.getClass()).getObjectBuilder().createObjectChangeSet(object, changeSet, session);

            getOrderedRemoveObjects().put(index, change);
        }
    }

    /**
     * This method takes a IdentityHashtable of objects, converts these into ObjectChangeSets.
     * @param objectChanges prototype.changeset.ObjectChangeSet
     */
    public void addRemoveChange(IdentityHashMap objectChanges, UnitOfWorkChangeSet changeSet, AbstractSession session) {
        // There is no need to keep track of removed new objects because it will not be in the backup,
        // It will not be in the backup because it is new.
        Iterator enumtr = objectChanges.keySet().iterator();
        while (enumtr.hasNext()) {
            Object object = enumtr.next();
            ClassDescriptor descriptor = session.getDescriptor(object.getClass());
            ObjectChangeSet change = descriptor.getObjectBuilder().createObjectChangeSet(object, changeSet, session);
            if (change.hasKeys()){
                // if change set has keys this is a map comparison.  Maps are
                // not support in change tracking so do not need to prevent duplicates
                // when map support is added this will have to be refactored
                getRemoveObjectList().put(change, change);
            }else{
                if (getAddObjectList().contains(change)){
                    getAddObjectList().remove(change);
                }else{
                    getRemoveObjectList().put(change, change);
                }
            }
        }
    }

    /**
     * ADVANCED:
     * This method returns the collection of ChangeSets that were added to the collection.
     * @return java.util.Vector
     */
    public IdentityHashtable getAddObjectList() {
        if (addObjectList == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            addObjectList = new IdentityHashtable();
        }
        return addObjectList;
    }

     /**
     * ADVANCED:
     * This method returns the collection of ChangeSets that were added to the collection.
     * @return java.util.Vector
     */
    public List getAddOverFlow() {
        if (addOverFlow == null) {
            addOverFlow = new ArrayList();
        }
        return addOverFlow;
    }

   /**
     * ADVANCED:
     * This method returns the IdentityHashtable that contains the removed values from the collection
     * and their corresponding ChangeSets.
     * @return java.util.Vector
     */
    public IdentityHashtable getRemoveObjectList() {
        if (removeObjectList == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            removeObjectList = new IdentityHashtable();
        }
        return removeObjectList;
    }

    /**
     * returns true if the change set has changes
     */
    public boolean hasChanges() {
        return (!(  getAddObjectList().isEmpty() && 
                    getRemoveObjectList().isEmpty() && 
                    getOrderedAddObjects().isEmpty() && 
                    getOrderedRemoveObjects().isEmpty())) 
                || getOwner().isNew();
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        Enumeration addEnum = ((CollectionChangeRecord)mergeFromRecord).getAddObjectList().keys();
        while (addEnum.hasMoreElements()) {
            ObjectChangeSet mergingObject = (ObjectChangeSet)addEnum.nextElement();
            ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet(mergingObject, mergeFromChangeSet);
            if (getRemoveObjectList().containsKey(localChangeSet)) {
                getRemoveObjectList().remove(localChangeSet);
            } else {
                getAddObjectList().put(localChangeSet, localChangeSet);
            }
        }
        Enumeration removeEnum = ((CollectionChangeRecord)mergeFromRecord).getRemoveObjectList().keys();
        while (removeEnum.hasMoreElements()) {
            ObjectChangeSet mergingObject = (ObjectChangeSet)removeEnum.nextElement();
            ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet(mergingObject, mergeFromChangeSet);
            if (getAddObjectList().containsKey(localChangeSet)) {
                getAddObjectList().remove(localChangeSet);
            } else {
                getRemoveObjectList().put(localChangeSet, localChangeSet);
            }
        }
    }

    /**
     * Sets the Added objects list
     * @param newValue java.util.Vector
     */
    public void setAddObjectList(IdentityHashtable objectChangesList) {
        this.addObjectList = objectChangesList;
    }

    /**
     * Sets the removed objects list
     * @param newValue java.util.Vector
     */
    public void setRemoveObjectList(IdentityHashtable objectChangesList) {
        this.removeObjectList = objectChangesList;
    }
    
    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     */
    public void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        IdentityHashtable addList = new IdentityHashtable(getAddObjectList().size());
        IdentityHashtable removeList = new IdentityHashtable(getRemoveObjectList().size());
        
        // If we have ordered lists we need to iterate through those.
        if (getOrderedAddObjects().size() > 0 || getOrderedRemoveObjectIndices().size() > 0) {
            // Do the ordered adds first ...
            Vector orderedAddList = new Vector(getOrderedAddObjects().size());
            IdentityHashtable orderedAddListIndices = new IdentityHashtable(getOrderedAddObjectIndices().size());
            
            for (int i = 0; i < getOrderedAddObjects().size(); i++) {
                ObjectChangeSet changeSet = (ObjectChangeSet) getOrderedAddObjects().elementAt(i);
                ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet(changeSet, mergeFromChangeSet);
                
                orderedAddList.add(localChangeSet);
                orderedAddListIndices.put(localChangeSet, getOrderedAddObjectIndices().get(changeSet));    
                
                // Object was actually added and not moved.
                if (getAddObjectList().contains(changeSet)) {
                    addList.put(localChangeSet, localChangeSet);
                }
            }
            
            setOrderedAddObjects(orderedAddList);
            setOrderedAddObjectIndices(orderedAddListIndices);
            
            // Do the ordered removes now ...
            Hashtable orderedRemoveList = new Hashtable(getOrderedRemoveObjects().size());
            Enumeration changes = getOrderedRemoveObjects().keys();
            
            while (changes.hasMoreElements()) {
                Object index = changes.nextElement();
                ObjectChangeSet changeSet = (ObjectChangeSet) getOrderedRemoveObjects().get(index);
                ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet(changeSet, mergeFromChangeSet);
                
                orderedRemoveList.put(index, localChangeSet);
                
                // Object was actually removed and not moved.
                if (getRemoveObjectList().contains(changeSet)) {
                    removeList.put(localChangeSet, localChangeSet);
                }
            }
            
            setOrderedRemoveObjects(orderedRemoveList);
            // Don't need to worry about the vector of indices (Integer's), just leave them as is.
        } else {
            Enumeration changes = getAddObjectList().elements();
            while (changes.hasMoreElements()) {
                ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet((ObjectChangeSet)changes.nextElement(), mergeFromChangeSet);
                addList.put(localChangeSet, localChangeSet);
            }
        
            changes = getRemoveObjectList().elements();
            while (changes.hasMoreElements()) {
                ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet((ObjectChangeSet)changes.nextElement(), mergeFromChangeSet);
                removeList.put(localChangeSet, localChangeSet);
            }
        }
        
        setAddObjectList(addList);
        setRemoveObjectList(removeList);
    }

    /**
     * INTERNAL:
     * This method used by SDK mapping that only supports Collection type not IdentityHashtable.
     * This method is mapped in oracle.toplink.essentials.internal.remotecommand.CommandProject
     * @return java.util.Vector
     */
    public Vector getAddObjectsForSDK() {
        if (sdkAddObjects == null) {
            sdkAddObjects = new Vector();

            for (Enumeration enumtr = this.getAddObjectList().keys(); enumtr.hasMoreElements();) {
                sdkAddObjects.add(enumtr.nextElement());
            }
        }
        return sdkAddObjects;
    }

    /**
     * INTERNAL:
     * This method used by SDK mapping that only supports Collection type not IdentityHashtable.
     * This method is mapped in oracle.toplink.essentials.internal.remotecommand.CommandProject
     * @param java.util.Vector addObjects
     */
    public void setAddObjectsForSDK(Vector addObjects) {
        sdkAddObjects = addObjects;

        // build the equivalent addObjectList
        IdentityHashtable newList = new IdentityHashtable();
        for (int i = 0; i < sdkAddObjects.size(); i++) {
            Object change = sdkAddObjects.elementAt(i);
            newList.put(change, change);
        }
        this.setAddObjectList(newList);
    }

    /**
     * INTERNAL:
     * This method used by SDK mapping that only supports Collection type not IdentityHashtable.
     * This method is mapped in oracle.toplink.essentials.internal.remotecommand.CommandProject
     * @return java.util.Vector
     */
    public Vector getRemoveObjectsForSDK() {
        if (sdkRemoveObjects == null) {
            sdkRemoveObjects = new Vector();

            for (Enumeration enumtr = this.getRemoveObjectList().keys(); enumtr.hasMoreElements();) {
                sdkRemoveObjects.add(enumtr.nextElement());
            }
        }
        return sdkRemoveObjects;
    }

    /**
     * INTERNAL:
     * This method used by SDK mapping that only supports Collection type not IdentityHashtable.
     * This method is mapped in oracle.toplink.essentials.internal.remotecommand.CommandProject
     * @param java.util.Vector removeObjects
     */
    public void setRemoveObjectsForSDK(Vector removeObjects) {
        sdkRemoveObjects = removeObjects;

        // build the equivalent removeObjectList
        IdentityHashtable newList = new IdentityHashtable();
        for (int i = 0; i < sdkRemoveObjects.size(); i++) {
            Object change = sdkRemoveObjects.elementAt(i);
            newList.put(change, change);
        }
        this.setRemoveObjectList(newList);
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the last collection that was set on the object
     */
    public Object getLatestCollection() {
        return latestCollection;
    }
    
    /**
     * ADVANCED:
     * This method returns the collection of ChangeSets in the order they were 
     * added to the collection. This list includes those objects that were 
     * moved within the collection.
     */
    public Vector getOrderedAddObjects() {
        if (orderedAddObjects == null) {
            orderedAddObjects = new Vector();
        }
        
        return orderedAddObjects;
    }
    
    /**
     * ADVANCED:
     * This method returns the index of an object added to the collection.
     */
    public Integer getOrderedAddObjectIndex(ObjectChangeSet changes) {
        return (Integer) getOrderedAddObjectIndices().get(changes);
    }
    
    /**
     * ADVANCED:
     * This method returns the collection of ChangeSets that they were 
     * added to the collection.
     */
    public IdentityHashtable getOrderedAddObjectIndices() {
        if (orderedAddObjectIndices == null) {
            orderedAddObjectIndices = new IdentityHashtable();
        }
        
        return orderedAddObjectIndices;
    }
    
    /**
     * ADVANCED:
     * This method returns the ordered list of indices to remove from the 
     * collection.
     */
    public Vector getOrderedRemoveObjectIndices() {
        if (orderedRemoveObjectIndices == null) {
            orderedRemoveObjectIndices = new Vector();
        }
        
        return orderedRemoveObjectIndices;
    }
    
    /**
     * ADVANCED:
     * This method returns the index of an object removed from the collection.
     */
    public Object getOrderedRemoveObject(Integer index) {
        return getOrderedRemoveObjects().get(index);
    }
    
    /**
     * ADVANCED:
     * This method returns the collection of ChangeSets of objects removed from
     * the collection.
     */
    public Hashtable getOrderedRemoveObjects() {
        if (orderedRemoveObjects == null) {
            orderedRemoveObjects = new Hashtable();
        }
        
        return orderedRemoveObjects;
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the last collection that was set on the object
     */
    public void setLatestCollection(Object latestCollection) {
        this.latestCollection = latestCollection;
    }
    
    /**
     * ADVANCED:
     * Sets collection of ChangeSets (and their respective index) that they 
     * were added to the collection.
     */
    public void setOrderedAddObjectIndices(IdentityHashtable orderedAddObjectIndices) {
        this.orderedAddObjectIndices = orderedAddObjectIndices;
    }
    
    /**
     * ADVANCED:
     * Sets collection of ChangeSets that they were added to the collection.
     */
    public void setOrderedAddObjects(Vector orderedAddObjects) {
        this.orderedAddObjects = orderedAddObjects;
    }
    
    /**
     * ADVANCED:
     * Sets collection of ChangeSets that they were remvoved from the collection.
     */
    public void setOrderedRemoveObjects(Hashtable orderedRemoveObjects) {
        this.orderedRemoveObjects = orderedRemoveObjects;
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the original collection that was set on the object when it was cloned
     */
    public Object getOriginalCollection() {
        return originalCollection;
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the original collection that was set on the object when it was cloned
     */
    public void setOriginalCollection(Object originalCollection) {
        this.originalCollection = originalCollection;
    }
}
