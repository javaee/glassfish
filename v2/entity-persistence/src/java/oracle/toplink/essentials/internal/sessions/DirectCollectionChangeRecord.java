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
import oracle.toplink.essentials.internal.queryframework.ContainerPolicy;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: This class holds the record of the changes made to a collection attribute of
 * an object.
 * <p>
 * <b>Description</b>: Collections must be compared to each other and added and removed objects must
 * be recorded seperately
 * @see OtherRelatedClasses prototype.changeset.DirectToFieldChangeRecord,prototype.changeset.SingleObjectChangeRecord
 */
public class DirectCollectionChangeRecord extends ChangeRecord implements oracle.toplink.essentials.changesets.DirectCollectionChangeRecord {
    protected java.util.HashMap addObjectMap;
    protected java.util.HashMap removeObjectMap;
    //contains the number of objects that must be inserted to once the value is removed
    //in the database as a delete where value = "value" will remove all instances
    //of that value in the database not just one.
    protected java.util.HashMap commitAddMap;
    
    /**
     * Used for change tracking when customer sets entire collection
     */
    protected transient Object originalCollection;

    /**
     * Used for change tracking when customer sets entire collection
     */
    protected transient Object latestCollection;

    public static final NULL Null = new NULL();

    /**
     * This defaul constructor is reference internally by SDK XML project to mapp this class
     */
    public DirectCollectionChangeRecord() {
        super();
    }

    /**
     * This constructor returns a changeRecord representing the DirectCollection mapping
     * @param owner prototype.changeset.ObjectChangeSet that ObjectChangeSet that uses this record
     */
    public DirectCollectionChangeRecord(ObjectChangeSet owner) {
        this.owner = owner;
    }

    /**
     * This method takes a hastable of primitive objects and adds them to the add list.
     * the hashtable stores the number of times the object is in the list
     * @param objectChanges prototype.changeset.ObjectChangeSet
     */
    public void addAdditionChange(HashMap additions, HashMap databaseCount) {
        Iterator enumtr = additions.keySet().iterator();
        while (enumtr.hasNext()) {
            Object object = enumtr.next();
            if (databaseCount.containsKey(object)){
                getCommitAddMap().put(object, databaseCount.get(object));
            }
            addAdditionChange(object, (Integer)additions.get(object));
        }
    }

    /**
     * This method takes a single addition value and records it.
     */
    public void addAdditionChange(Object key, Integer count){
        if (getRemoveObjectMap().containsKey(key)){
            int removeValue = ((Integer)getRemoveObjectMap().get(key)).intValue();
            int addition = count.intValue();
            int result = removeValue - addition;
            if (result > 0 ) { // more removes still
                getRemoveObjectMap().put(key, new Integer(result));
            }else if (result < 0) { // more adds now
                getRemoveObjectMap().remove(key);
                getAddObjectMap().put(key, new Integer(Math.abs(result)));
            }else{ // equal
                getRemoveObjectMap().remove(key);
            }
        }else{
            if (this.getAddObjectMap().containsKey(key)){
                int addValue = ((Integer)this.getAddObjectMap().get(key)).intValue();
                addValue += count.intValue();
                this.getAddObjectMap().put(key, new Integer(addValue));
            }else{
                this.getAddObjectMap().put(key, count);
            }
        }
        // this is an attribute change track add keep count
        int addValue = count.intValue();
        int commitValue = 0;
        if (getCommitAddMap().containsKey(key)){
            commitValue = ((Integer)getCommitAddMap().get(key)).intValue();
        }
        getCommitAddMap().put(key, new Integer(addValue+commitValue));
    }
    /**
     * This method takes a hashtable of primitive objects and adds them to the remove list.
     * Each reference in the hashtable lists the number of this object that needs to be removed from the
     * collection.
     * @param objectChanges prototype.changeset.ObjectChangeSet
     */
    public void addRemoveChange(HashMap additions, HashMap databaseCount) {
        Iterator enumtr = additions.keySet().iterator();
        while (enumtr.hasNext()) {
            Object object = enumtr.next();
            if (databaseCount.containsKey(object)){
                getCommitAddMap().put(object, databaseCount.get(object));
            }
            addRemoveChange(object, (Integer)additions.get(object));
        }
    }
    
    /**
     * This method takes a single remove change and integrates it with this changeset
     */
    public void addRemoveChange(Object key, Integer count){
        if (getAddObjectMap().containsKey(key)){
            int removeValue = ((Integer)getAddObjectMap().get(key)).intValue();
            int addition = count.intValue();
            int result = removeValue - addition;
            if (result > 0 ) { // more removes still
                getAddObjectMap().put(key, new Integer(result));
            }else if (result < 0) { // more adds now
                getAddObjectMap().remove(key);
                getRemoveObjectMap().put(key, new Integer(Math.abs(result)));
            }else{ // equal
                getAddObjectMap().remove(key);
            }
        }else{
            if (this.getRemoveObjectMap().containsKey(key)){
                int addValue = ((Integer)this.getRemoveObjectMap().get(key)).intValue();
                addValue += count.intValue();
                this.getRemoveObjectMap().put(key, new Integer(addValue));
            }else{
                this.getRemoveObjectMap().put(key, count);
            }
        }
        int removeValue = count.intValue();
        int commitValue = 0;
        if (getCommitAddMap().containsKey(key)){
            commitValue = ((Integer)getCommitAddMap().get(key)).intValue();
        }
        getCommitAddMap().put(key, new Integer(commitValue - removeValue));
        
    }

    /**
     * This method takes a hashtable of primitives and adds them to the commit list.
     * This count value provided is the number of instances that will need to be
     * inserted into the database once a remove has occured.  This is only set
     * once for each object type
     */
    public void setCommitAddition(Hashtable additions){
        Enumeration enumtr = additions.keys();
        while (enumtr.hasMoreElements()) {
            Object object = enumtr.nextElement();
            getCommitAddMap().put(object, additions.get(object));
        }
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the last collection that was set on the object
     */
    public void setLatestCollection(Object latestCollection) {
        this.latestCollection = latestCollection;
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the original collection that was set on the object when it was cloned
     */
    public void setOriginalCollection(Object originalCollection) {
        this.originalCollection = originalCollection;
    }

    /**
     * This method will iterate over the collection and store the database counts for
     * the objects within the collection, this is used for minimal updates
     */
    public void storeDatabaseCounts(Object collection, ContainerPolicy containerPolicy, AbstractSession session){
        Object iterator = containerPolicy.iteratorFor(collection);
        while (containerPolicy.hasNext(iterator)){
            Object object = containerPolicy.next(iterator, session);
            if (getCommitAddMap().containsKey(object)){
                int count = ((Integer)getCommitAddMap().get(object)).intValue();
                getCommitAddMap().put(object, new Integer(++count));
            }else{
                getCommitAddMap().put(object, new Integer(1));
            }
        }
    }

    /**
     * ADVANCED:
     * This method returns the list of added objects
     */
    public Vector getAddObjectList(){
        Vector vector = new Vector();
        for (Iterator iterator = getAddObjectMap().keySet().iterator(); iterator.hasNext();){
            Object object = iterator.next();
            int count = ((Integer)getAddObjectMap().get(object)).intValue();
            while (count > 0){
                vector.add(object);
                --count;
            }
        }
        return vector;
    }

    /**
     * INTERNAL:
     * This method sets the list of added objects.  It should only be used in RCM
     */
    public void setAddObjectList(Vector list){
        for (Iterator iterator = list.iterator(); iterator.hasNext();){
            Object object = iterator.next();
            this.addAdditionChange(object, new Integer(1));
        }
    }

    /**
     * ADVANCED:
     * This method returns the collection of objects that were added to the collection.
     * @return java.util.Vector
     */
    public java.util.HashMap getAddObjectMap() {
        if (this.addObjectMap == null) {
            this.addObjectMap = new HashMap(1);
        }
        return addObjectMap;
    }

    /**
     * ADVANCED:
     * This method returns the collection of objects that were added to the collection.
     * @return java.util.Vector
     */
    public java.util.HashMap getCommitAddMap() {
        if (this.commitAddMap == null) {
            this.commitAddMap = new HashMap(1);
        }
        return commitAddMap;
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the last collection that was set on the object
     */
    public Object getLatestCollection() {
        return latestCollection;
    }

    /**
     * Used for change tracking when cutomer sets entire collection
     * This is the original collection that was set on the object when it was cloned
     */
    public Object getOriginalCollection() {
        return originalCollection;
    }

    /**
     * ADVANCED:
     * This method returns the list of removed objects
     */
    public Vector getRemoveObjectList(){
        Vector vector = new Vector();
        for (Iterator iterator = getRemoveObjectMap().keySet().iterator(); iterator.hasNext();){
            Object object = iterator.next();
            int count = ((Integer)getRemoveObjectMap().get(object)).intValue();
            while (count > 0){
                vector.add(object);
                --count;
            }
        }
        return vector;
    }

    /**
     * INTERNAL:
     * This method sets the list of added objects.  It should only be used in RCM
     */
    public void setRemoveObjectList(Vector list){
        for (Iterator iterator = list.iterator(); iterator.hasNext();){
            Object object = iterator.next();
            this.addRemoveChange(object, new Integer(1));
        }
    }

    /**
     * ADVANCED:
     * This method returns the collection of objects that were removed from the collection.
     * @return java.util.Vector
     */
    public java.util.HashMap getRemoveObjectMap() {
        if (this.removeObjectMap == null) {
            removeObjectMap = new HashMap(1);
        }
        return removeObjectMap;
    }

    /**
     * returns true if the change set has changes
     */
    public boolean hasChanges() {
        return (!(getAddObjectMap().isEmpty() && getRemoveObjectMap().isEmpty())) || getOwner().isNew();
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        HashMap addMapToMerge = ((DirectCollectionChangeRecord)mergeFromRecord).getAddObjectMap();
        HashMap removeMapToMerge = ((DirectCollectionChangeRecord)mergeFromRecord).getRemoveObjectMap();
        //merge additions
        for (Iterator iterator = addMapToMerge.keySet().iterator(); iterator.hasNext();){
            Object added = iterator.next();
            if (!((DirectCollectionChangeRecord)mergeFromRecord).getCommitAddMap().containsKey(added)){
                // we have not recorded a change of this type in this class before so  add it
                this.getCommitAddMap().put(added, ((DirectCollectionChangeRecord)mergeFromRecord).getCommitAddMap().get(added));
            }
            this.addAdditionChange(added, (Integer)addMapToMerge.get(added));
        }
        //merge removals
        for (Iterator iterator = removeMapToMerge.keySet().iterator(); iterator.hasNext();){
            Object removed = iterator.next();
            if (!((DirectCollectionChangeRecord)mergeFromRecord).getCommitAddMap().containsKey(removed)){
                // we have not recorded a change of this type in this class before so  add it
                this.getCommitAddMap().put(removed, ((DirectCollectionChangeRecord)mergeFromRecord).getCommitAddMap().get(removed));
            }
            this.addRemoveChange(removed, (Integer)addMapToMerge.get(removed));
        }
   }

    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     */
    public void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        //nothing for this record type to do as it does not reference any changesets
    }
    
    public static class NULL {
        // This is a placeholder for null instances.
        public NULL(){
        }
        
        public boolean equals(Object object){
            return object instanceof NULL;
        }
        
    }
}
