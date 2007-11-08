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
import oracle.toplink.essentials.mappings.Association;

public class DirectMapChangeRecord extends ChangeRecord {
    protected HashMap addObjectsList;
    protected HashMap removeObjectsList;

    /**
     * Used for change tracking when customer sets entire collection
     */
    protected transient Object originalCollection;

    /**
     * Used for change tracking when customer sets entire collection
     */
    protected transient Object latestCollection;

    public DirectMapChangeRecord() {
        super();
    }

    public DirectMapChangeRecord(ObjectChangeSet owner) {
        this.owner = owner;
    }

    /**
     * INTERNAL:
     * Use in SDK propject for the mapping of this change record
     */
    public Vector getAddAssociations() {
        Vector addAssociations = new Vector();

        for (Iterator i = getAddObjects().keySet().iterator(); i.hasNext(); ) {
            Association association = new Association();
            Object key = i.next();
            Object value = getAddObjects().get(key);

            association.setKey(key);
            association.setValue(value);
            addAssociations.add(association);
        }
        if (addAssociations.size() == 0) {
            return null;
        }
        return addAssociations;
    }

    /**
     * returns true if the change set has changes
     */
    public boolean hasChanges() {
        return (!(getAddObjects().isEmpty() && getRemoveObjects().isEmpty())) || getOwner().isNew();
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        Iterator addKeys = ((DirectMapChangeRecord)mergeFromRecord).getAddObjects().keySet().iterator();
        while (addKeys.hasNext()) {
            Object key = addKeys.next();

            if (!this.getAddObjects().containsKey(key)) {
                if (this.getRemoveObjects().containsKey(key)) {
                    this.getRemoveObjects().remove(key);
                } else {
                    this.getAddObjects().put(key, ((DirectMapChangeRecord)mergeFromRecord).getAddObjects().get(key));
                }
            }
        }

        Iterator removeKeys = ((DirectMapChangeRecord)mergeFromRecord).getRemoveObjects().keySet().iterator();
        while (removeKeys.hasNext()) {
            Object key = removeKeys.next();

            if (!this.getRemoveObjects().containsKey(key)) {
                if (this.getAddObjects().containsKey(key)) {
                    this.getAddObjects().remove(key);
                } else {
                    this.getRemoveObjects().put(key, ((DirectMapChangeRecord)mergeFromRecord).getRemoveObjects().get(key));
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Use in SDK propject for the mapping of this change record
     */
    public void setAddAssociations(Vector addAssociations) {
        HashMap addMap = new HashMap();

        for (Enumeration enumtr = addAssociations.elements(); enumtr.hasMoreElements();) {
            Association association = (Association)enumtr.nextElement();
            addMap.put(association.getKey(), association.getValue());
        }
        if (addMap.isEmpty()) {
            addObjectsList = null;
        }
        addObjectsList = addMap;
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
    * INTERNAL:
    * Use in SDK propject for the mapping of this change record
    */
    public Vector getRemoveAssociations() {
        Vector removeAssociations = new Vector();

        for (Iterator i = getRemoveObjects().keySet().iterator(); i.hasNext(); ) {
            Association association = new Association();
            
            Object key = i.next();
            Object value = getAddObjects().get(key);

            association.setKey(key);
            association.setValue(value);
            removeAssociations.add(association);
        }
        if (removeAssociations.size() == 0) {
            return null;
        }
        return removeAssociations;
    }

    /**
     * INTERNAL:
     * Use in SDK propject for the mapping of this change record
     */
    public void setRemoveAssociations(Vector removeAssociations) {
        HashMap removeMap = new HashMap();

        for (Enumeration enumtr = removeAssociations.elements(); enumtr.hasMoreElements();) {
            Association association = (Association)enumtr.nextElement();
            removeMap.put(association.getKey(), association.getValue());
        }
        if (removeMap.isEmpty()) {
            removeObjectsList = null;
        }
        removeObjectsList = removeMap;
    }

    /**
     * ADVANCED:
     * Adds the items that were added to the collection
     */
    public void addAdditionChange(HashMap additions) {
        if (getAddObjects().size() == 0) {
            addObjectsList = additions;
            return;
        }

        for (Iterator i = additions.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            if (getAddObjects().containsKey(key)) {
                getAddObjects().put(key, additions.get(key));
            } else if (additions.get(key).equals(getAddObjects().get(key))) {
                getAddObjects().put(key, additions.get(key));
            }
        }
    }

    /**
    * ADVANCED:
    * Adds the items that were removed from the collection
    */
    public void addRemoveChange(HashMap subtractions) {
        if (getRemoveObjects().size() == 0) {
            this.removeObjectsList = subtractions;
            return;
        }

        for (Iterator i = subtractions.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            if (!getRemoveObjects().containsKey(key)) {
                getRemoveObjects().put(key, subtractions.get(key));
            } else if (subtractions.get(key).equals(getRemoveObjects().get(key))) {
                getRemoveObjects().put(key, subtractions.get(key));
            }
        }
    }

    /**
     * ADVANCED:
     * Adds the items that were added to the collection
     */
    public void addAdditionChange(Object key, Object value) {
        if ( getRemoveObjects().containsKey(key) ) { 
            if ( value.equals(getRemoveObjects().get(key)) ) {
                getRemoveObjects().remove(key);
            }else {
                getAddObjects().put(key, value);
            }
        } else {
            getAddObjects().put(key, value);
        }
    }

    /**
    * ADVANCED:
    * Adds the items that were removed from the collection
    */
    public void addRemoveChange(Object key, Object value) {
        //if an entry already exists in the remove it must remain untill added
        // as it contains the original removal.
        if ( getAddObjects().containsKey(key) ) {
            getAddObjects().remove(key);
        }else if ( ! getRemoveObjects().containsKey(key) ) {
            getRemoveObjects().put(key, value);
        }
    }
    
    /**
     * INTERNAL:
     * Sets the added items list
     */
    public void setAddObjects(HashMap addObjects) {
        this.addObjectsList = addObjects;
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
     * INTERNAL:
     * Returns the added items list
     */
    public HashMap getAddObjects() {
        if (addObjectsList == null) {
            // addObjectsList = new Hashtable();
            addObjectsList = new HashMap();
        }
        return addObjectsList;
    }

    /**
     * INTERNAL:
     * Sets the removed items list
     */
    public void setRemoveObjects(HashMap removeObjects) {
        this.removeObjectsList = removeObjects;
    }

    /**
     * INTERNAL:
     * Returns the removed items list
     */
    public HashMap getRemoveObjects() {
        if (removeObjectsList == null) {
            removeObjectsList = new HashMap();
        }
        
        return removeObjectsList;
    }

    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     */
    public void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        //nothing for this record type to do as it does not reference any changesets
    }
}
