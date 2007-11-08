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
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p>
 * <b>Purpose</b>: This is the overall collection of changes.
 * <p>
 * <b>Description</b>: It holds all of the object changes and
 * all ObjectChanges, with the same classType and primary keys, referenced in a changeSet should be
 * the same object.
 * <p>
 */
public class UnitOfWorkChangeSet implements Serializable, oracle.toplink.essentials.changesets.UnitOfWorkChangeSet {

    /** This is the collection of ObjectChanges held by this ChangeSet */

    // Holds a hashtable of objectChageSets keyed on class
    transient protected java.util.Hashtable objectChanges;

    //This collection holds the new objects which will have no real identity until inserted
    transient protected java.util.Hashtable newObjectChangeSets;
    transient protected oracle.toplink.essentials.internal.helper.IdentityHashtable cloneToObjectChangeSet;
    transient protected oracle.toplink.essentials.internal.helper.IdentityHashtable objectChangeSetToUOWClone;
    protected IdentityHashtable aggregateList;
    protected IdentityHashtable allChangeSets;
    protected IdentityHashtable deletedObjects;

    /** This attribute is set to true if a changeSet with changes has been added */
    protected boolean hasChanges;
    protected boolean hasForcedChanges;

    /* Collection of ObjectChangeSets that is built from other collections and mapped with SDK */
    private transient Vector sdkAllChangeSets;
    private transient int objectChangeSetCounter = 0;

    /**
     * INTERNAL:
     * Create a ChangeSet
     */
    public UnitOfWorkChangeSet() {
        super();
        this.setHasChanges(false);
    }

    /**
     * INTERNAL:
     * Recreate a UnitOfWorkChangeSet that has been converted to a byte array with the
     * getByteArrayRepresentation() method.
     */
    public UnitOfWorkChangeSet(byte[] bytes) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayInputStream byteIn = new java.io.ByteArrayInputStream(bytes);
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
		//bug 4416412: allChangeSets set directly instead of using setInternalAllChangeSets
        allChangeSets = (IdentityHashtable)objectIn.readObject();
        deletedObjects = (IdentityHashtable)objectIn.readObject();
    }

    /**
     * INTERNAL:
     * Add the Deleted objects to the changeSet
     * @param objectChanges prototype.changeset.ObjectChanges
     */
    public void addDeletedObjects(IdentityHashtable deletedObjects, AbstractSession session) {
        Enumeration enumtr = deletedObjects.keys();
        while (enumtr.hasMoreElements()) {
            Object object = enumtr.nextElement();

            this.addDeletedObject(object, session);
        }
    }
    
    /**
     * INTERNAL:
     * Add the Deleted object to the changeSet
     * @param objectChanges prototype.changeset.ObjectChanges
     */
    public void addDeletedObject(Object object, AbstractSession session) {
        //CR 4080 - must prevent aggregate objects added to DeletedObjects list
        ClassDescriptor descriptor = session.getDescriptor(object);
        if (!descriptor.isAggregateCollectionDescriptor()) {
            ObjectChangeSet set = descriptor.getObjectBuilder().createObjectChangeSet(object, this, false, session);

            // needed for xml change set
            set.setShouldBeDeleted(true);
            getDeletedObjects().put(set, set);
        }
    }

    /**
     * INTERNAL:
     * Add to the changes for 'object' object to this changeSet.  This method will not
     * add to the lists that are used for identity lookups.
     * The passed change set *must* either have changes or forced changes.
     * @see addObjectChangeSetForIdentity()
     * @param objectChanges prototype.changeset.ObjectChanges
     * @param object java.lang.Object
     */
    public void addObjectChangeSet(ObjectChangeSet objectChanges) {
        if ((objectChanges == null)) {
            return;
        }

        // If this object change set has changes or forced changes then record this.  Must
        // be done for each change set added because some may not contain 'real' changes.  This
        // is the case for opt. read lock and forceUdpate.  Keep the flags separate because
        // we don't want to cache sync. a change set with no 'real' changes.
    	boolean objectChangeSetHasChanges = objectChanges.hasChanges();
        if (objectChangeSetHasChanges) {
            this.setHasChanges(true);
            this.hasForcedChanges = this.hasForcedChanges || objectChanges.hasForcedChanges();
        } else {
            // object change set doesn't have changes so it has to have forced changes.
            this.hasForcedChanges = true;
        }

        if (!objectChanges.isAggregate()) {
            if (objectChangeSetHasChanges) {
                // Each time I create a changeSet it is added to this list and when I compute a changeSet for this
                // object I again add it to these lists so that before this UOWChangeSet is
                // Serialised there is a copy of every changeSet which has changes affecting cache
                // in allChangeSets
                getAllChangeSets().put(objectChanges, objectChanges);
            }
            
            if (objectChanges.getCacheKey() != null) {
                Hashtable table = (Hashtable)getObjectChanges().get(objectChanges.getClassName());

                if (table == null) {
                    table = new Hashtable(2);
                    getObjectChanges().put(objectChanges.getClassName(), table);
                    table.put(objectChanges, objectChanges);
                } else {
                    table.put(objectChanges, objectChanges);
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Add to the changes for 'object' object to this changeSet.  This method will not
     * add to the lists that are used for identity lookups.  It is called specifically
     * for new objects, and new object will be moved to the standard changes list by
     * the QueryMechanism after insert.
     * @see addObjectChangeSetForIdentity()
     * @param objectChanges the new object change set
     */
    public void addNewObjectChangeSet(ObjectChangeSet objectChanges, AbstractSession session) {
        if ((objectChanges == null)) {
            return;
        }
        IdentityHashtable changeSetTable = (IdentityHashtable)getNewObjectChangeSets().get(objectChanges.getClassType(session));
        if (changeSetTable == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            changeSetTable = new IdentityHashtable();
            getNewObjectChangeSets().put(objectChanges.getClassType(session), changeSetTable);
        }
        changeSetTable.put(objectChanges, objectChanges);
    }

    /**
     * INTERNAL:
     * This method can be used find the equivalent changeset within this UnitOfWorkChangeSet
     * Aggregates, and new objects without primaryKeys from serialized ChangeSets will not be found
     * Which may result in duplicates, in the UnitOfWorkChangeSet.
     */
    public ObjectChangeSet findObjectChangeSet(ObjectChangeSet changeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        Hashtable changes = (Hashtable)getObjectChanges().get(changeSet.getClassName());
        ObjectChangeSet potential = null;
        if (changes != null) {
            potential = (ObjectChangeSet)changes.get(changeSet);
        }
        if (potential == null) {
            potential = (ObjectChangeSet)this.getObjectChangeSetForClone(changeSet.getUnitOfWorkClone());
        }
        return potential;
    }

    /**
     * INTERNAL:
     * This method will be used during the merge process to either find an equivalent change set
     * within this UnitOfWorkChangeSet or integrate that changeset into this UOW ChangeSet
     */
    public ObjectChangeSet findOrIntegrateObjectChangeSet(ObjectChangeSet tofind, UnitOfWorkChangeSet mergeFromChangeSet) {
        if (tofind == null) {
            return tofind;
        }
        ObjectChangeSet localChangeSet = this.findObjectChangeSet(tofind, mergeFromChangeSet);
        if (localChangeSet == null) {//not found locally then replace it with the one from the merging changeset
            localChangeSet = new ObjectChangeSet(tofind.getPrimaryKeys(), tofind.getClassType(), tofind.getUnitOfWorkClone(), this, tofind.isNew());
            this.addObjectChangeSetForIdentity(localChangeSet, localChangeSet.getUnitOfWorkClone());
        }
        return localChangeSet;
    }

    /**
     * INTERNAL:
     * Add change records to the lists used to maintain identity.  This will not actually
     * add the changes to 'object' to the change set.
     * @see addObjectChangeSet()
     * @param objectChanges prototype.changeset.ObjectChanges
     */
    public void addObjectChangeSetForIdentity(ObjectChangeSet objectChanges, Object object) {
        if ((objectChanges == null) || (object == null)) {
            return;
        }

        if (objectChanges.isAggregate()) {
            getAggregateList().put(objectChanges, objectChanges);
        }

        getObjectChangeSetToUOWClone().put(objectChanges, object);
        getCloneToObjectChangeSet().put(object, objectChanges);

    }

    /**
     * INTERNAL:
     * Get the Aggregate list.  Lazy initialises the hashtable if required
     * @return oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    protected IdentityHashtable getAggregateList() {
        if (aggregateList == null) {
            aggregateList = new IdentityHashtable();
        }
        return aggregateList;
    }

    /**
     * INTERNAL:
     * This method returns a reference to the collection
     * @return oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    public oracle.toplink.essentials.internal.helper.IdentityHashtable getAllChangeSets() {
        if (this.allChangeSets == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            this.allChangeSets = new IdentityHashtable();
        }
        return allChangeSets;
    }

    /**
     * INTERNAL:
     * Get the clone to object change hash table.  Lazy initialises the hashtable if required
     * @return oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    public oracle.toplink.essentials.internal.helper.IdentityHashtable getCloneToObjectChangeSet() {
        if (cloneToObjectChangeSet == null) {
            cloneToObjectChangeSet = new IdentityHashtable();
        }
        return cloneToObjectChangeSet;
    }

    /**
     * INTERNAL:
     * This method returns the reference to the deleted objects from the changeSet
     * @return oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    public oracle.toplink.essentials.internal.helper.IdentityHashtable getDeletedObjects() {
        if (this.deletedObjects == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            this.deletedObjects = new IdentityHashtable();
        }
        return deletedObjects;
    }

    /**
     * INTERNAL:
     * Returns the ObjectChanges held by this ChangeSet.
     * @return prototype.changeset.ObjectChanges
     */
    public Hashtable getObjectChanges() {
        if (objectChanges == null) {
            objectChanges = new Hashtable(2);
        }
        return objectChanges;
    }

    /**
     * INTERNAL:
     * Returns the set of classes corresponding to updated objects in objectChanges.
     * @return HashSet<Class>
     */
    public Set<Class> findUpdatedObjectsClasses() {
        if(objectChanges == null || objectChanges.isEmpty()) {
            return null;
        }
        HashSet<Class> updatedObjectsClasses = new HashSet<Class>(getObjectChanges().size());
        Iterator it = getObjectChanges().values().iterator();
        while(it.hasNext()) {
            Hashtable table = (Hashtable)it.next();
            Iterator itChangeSets = table.keySet().iterator();
            while(itChangeSets.hasNext()) {
                // any change set will do
                ObjectChangeSet changeSet = (ObjectChangeSet)itChangeSets.next();
                if(!changeSet.isNew()) {
                    // found updated object - add its class to the set
                    updatedObjectsClasses.add(changeSet.getClassType());
                    // and go to the table corresponding to the next class
                    break;
                }
            }
        }
        return updatedObjectsClasses;
    }

    /**
     * ADVANCED:
     * Get ChangeSet for a particular clone
     * @return ObjectChangeSet the changeSet that represents a particular clone
     */
    public oracle.toplink.essentials.changesets.ObjectChangeSet getObjectChangeSetForClone(Object clone) {
        if ((clone == null) || (getCloneToObjectChangeSet() == null)) {
            return null;
        }
        return (oracle.toplink.essentials.changesets.ObjectChangeSet)getCloneToObjectChangeSet().get(clone);
    }

    /**
     * INTERNAL:
     * This method returns a reference to the collection
     * @return oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    protected oracle.toplink.essentials.internal.helper.IdentityHashtable getObjectChangeSetToUOWClone() {
        if (this.objectChangeSetToUOWClone == null) {
            // 2612538 - the default size of IdentityHashtable (32) is appropriate
            this.objectChangeSetToUOWClone = new IdentityHashtable();
        }
        return objectChangeSetToUOWClone;
    }

    /**
     * ADVANCED:
     * This method returns the Clone for a particular changeSet
     * @return Object the clone represented by the changeSet
     */
    public Object getUOWCloneForObjectChangeSet(oracle.toplink.essentials.changesets.ObjectChangeSet changeSet) {
        if ((changeSet == null) || (getObjectChangeSetToUOWClone() == null)) {
            return null;
        }
        return getObjectChangeSetToUOWClone().get(changeSet);
    }

    /**
     * INTERNAL:
     * Returns true if the Unit Of Work change Set has changes
     */
    public boolean hasChanges() {
        // All of the object change sets were empty (none contained changes)
        // The this.hasChanges variable is set in addObjectChangeSet
        return (this.hasChanges || (!getDeletedObjects().isEmpty()));
    }

    /**
     * INTERNAL:
     * Set whether the Unit Of Work change Set has changes
     */
    public void setHasChanges(boolean flag) {
        this.hasChanges = flag;
    }

    /**
     * INTERNAL:
     * Returns true if this uowChangeSet contains an objectChangeSet that has forced
     * SQL changes.  This is true whenever CMPPolicy.getForceUpdate() == true.
     * @return boolean
     */
    public boolean hasForcedChanges() {
        return this.hasForcedChanges;
    }

    /**
     * INTERNAL:
     * This method will be used to merge a change set into an UnitOfWorkChangeSet
     * This method returns the local instance of the changeset
     */
    public ObjectChangeSet mergeObjectChanges(ObjectChangeSet objectChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        ObjectChangeSet localChangeSet = this.findOrIntegrateObjectChangeSet(objectChangeSet, mergeFromChangeSet);
        if (localChangeSet != null) {
            localChangeSet.mergeObjectChanges(objectChangeSet, this, mergeFromChangeSet);
        }
        return localChangeSet;
    }

    /**
    * INTERNAL:
    * THis method will be used to merge another changeset into this changeset.  The
    * Main use of this method is for non-deferred writes and checkpointing so that
    * the acumulated changes are collected and merged at the end of the transaction
    *
    */
    public void mergeUnitOfWorkChangeSet(UnitOfWorkChangeSet mergeFromChangeSet, AbstractSession session, boolean postCommit) {
        Iterator iterator = mergeFromChangeSet.getObjectChanges().values().iterator();
        while (iterator.hasNext()) {
            //iterate over the classes
            Hashtable table = (Hashtable)iterator.next();
            Iterator changes = table.values().iterator();
            while (changes.hasNext()) {
                ObjectChangeSet objectChangeSet = (ObjectChangeSet)changes.next();
                objectChangeSet = mergeObjectChanges(objectChangeSet, mergeFromChangeSet);
                if (objectChangeSet.isNew() && !postCommit) {// if it is post commit then we can trust the cache key
                    this.addNewObjectChangeSet(objectChangeSet, session);
                } else {
                    this.addObjectChangeSet(objectChangeSet);
                }
            }
        }

        //merging a serialized UnitOfWorkChangeSet can result in duplicate deletes
        //if a delete for the same object already exists in this UOWChangeSet.
        Enumeration deletedEnum = mergeFromChangeSet.getDeletedObjects().elements();
        while (deletedEnum.hasMoreElements()) {
            ObjectChangeSet objectChangeSet = (ObjectChangeSet)deletedEnum.nextElement();
            ObjectChangeSet localObjectChangeSet = findObjectChangeSet(objectChangeSet, mergeFromChangeSet);
            if (localObjectChangeSet == null) {
                localObjectChangeSet = objectChangeSet;
            }
            this.getDeletedObjects().put(localObjectChangeSet, localObjectChangeSet);
        }
    }

    /**
     * INTERNAL:
     * Used to rehash the new objects back into the objectChanges list for serialization
     */
    public void putNewObjectInChangesList(ObjectChangeSet objectChangeSet, AbstractSession session) {
        this.addObjectChangeSet(objectChangeSet);
        removeObjectChangeSetFromNewList(objectChangeSet, session);
    }

    /**
     * INTERNAL:
     * Used to remove a new object from the new objects list once it has been
     * inserted and added to the objectChangesList
     */
    public void removeObjectChangeSetFromNewList(ObjectChangeSet objectChangeSet, AbstractSession session) {
        IdentityHashtable table = (IdentityHashtable)getNewObjectChangeSets().get(objectChangeSet.getClassType(session));
        if (table != null) {
            table.remove(objectChangeSet);
        }
    }

    /**
     * INTERNAL:
     * Add the changed Object's records to the ChangeSet
     * @param objectChanges prototype.changeset.ObjectChanges
     */
    public void removeObjectChangeSet(ObjectChangeSet objectChanges) {
        if (objectChanges == null) {
            return;
        }
        Object object = getObjectChangeSetToUOWClone().get(objectChanges);
        if (objectChanges.isAggregate()) {
            getAggregateList().remove(objectChanges);
        } else {
            // Bug 3294426 - index object changes by classname instead of class for remote classloader issues
            Hashtable table = (Hashtable)getObjectChanges().get(object.getClass().getName());
            if (table != null) {
                table.remove(objectChanges);
            }
        }
        getObjectChangeSetToUOWClone().remove(objectChanges);
        if (object != null) {
            getCloneToObjectChangeSet().remove(object);
        }
        getAllChangeSets().remove(objectChanges);
    }

    /**
     * INTERNAL:
     * This method is used to set the hashtable for cloneToObject reference
     * @param newCloneToObjectChangeSet oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    protected void setCloneToObjectChangeSet(oracle.toplink.essentials.internal.helper.IdentityHashtable newCloneToObjectChangeSet) {
        cloneToObjectChangeSet = newCloneToObjectChangeSet;
    }

    /**
     * INTERNAL:
     * Sets the collection of ObjectChanges in the change Set
     * @param newValue prototype.changeset.ObjectChanges
     */
    protected void setObjectChanges(Hashtable objectChanges) {
        this.objectChanges = objectChanges;
    }

    /**
     * INTERNAL:
     * This method is used to insert a new collection into the UOWChangeSet.
     * @param newObjectChangeSetToUOWClone oracle.toplink.essentials.internal.helper.IdentityHashtable
     */
    protected void setObjectChangeSetToUOWClone(oracle.toplink.essentials.internal.helper.IdentityHashtable newObjectChangeSetToUOWClone) {
        objectChangeSetToUOWClone = newObjectChangeSetToUOWClone;
    }

    /**
     * INTERNAL:
     * This method will return a reference to the new object change set collections
     */
    public java.util.Hashtable getNewObjectChangeSets() {
        if (this.newObjectChangeSets == null) {
            this.newObjectChangeSets = new java.util.Hashtable();
        }
        return this.newObjectChangeSets;
    }

    /**
     * INTERNAL:
     * This method take a collection of ObjectChangeSet rebuilds this UOW change set to a ready to merge stage
     */
    public void setInternalAllChangeSets(Vector objectChangeSets) {
        if (objectChangeSets == null) {
            return;
        }
        sdkAllChangeSets = objectChangeSets;

        for (int i = 0; i < objectChangeSets.size(); i++) {
            ObjectChangeSet objChangeSet = (ObjectChangeSet)objectChangeSets.elementAt(i);
            objChangeSet.setUOWChangeSet(this);

            if (objChangeSet.isAggregate()) {
                getAggregateList().put(objChangeSet, objChangeSet);

            } else if (objChangeSet.shouldBeDeleted()) {
                getDeletedObjects().put(objChangeSet, objChangeSet);
            } else {
                getAllChangeSets().put(objChangeSet, objChangeSet);
            }
            if (objChangeSet.getCacheKey() != null) {
                Hashtable table = (Hashtable)getObjectChanges().get(objChangeSet.getClassName());
                if (table == null) {
                    table = new Hashtable(2);
                    getObjectChanges().put(objChangeSet.getClassName(), table);
                }
                table.put(objChangeSet, objChangeSet);
            }
        }
    }
}
