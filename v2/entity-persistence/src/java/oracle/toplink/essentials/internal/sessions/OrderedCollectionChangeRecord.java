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

/**
 * <p>
 * <b>Purpose</b>: This class holds the record of the changes made to a collection attribute of
 * an object.
 * <p>
 * <b>Description</b>: Collections must be compared to each other and added and removed objects must
 * be recorded seperately.
 *
 * NOTE: This class and its sub class are currently not used within TopLink and should be removed.
 */
public class OrderedCollectionChangeRecord extends ChangeRecord implements oracle.toplink.essentials.changesets.OrderedCollectionChangeRecord {
    protected Hashtable addObjectList;
    protected Vector addIndexes;
    protected int startIndexOfRemove;

    /**
     * This constructor returns a changeRecord representing the DirectCollection mapping
     * @param owner prototype.changeset.ObjectChangeSet that ObjectChangeSet that uses this record
     */
    public OrderedCollectionChangeRecord(ObjectChangeSet owner) {
        this.owner = owner;
        this.startIndexOfRemove = Integer.MAX_VALUE;
    }

    /**
     * This method takes a hastable of primitive objects and adds them to the add list.
     */
    public void addAdditionChange(Hashtable additions, Vector indexes, UnitOfWorkChangeSet changes, AbstractSession session) {
        for (Enumeration enumtr = additions.keys(); enumtr.hasMoreElements();) {
            Object index = enumtr.nextElement();
            Object object = additions.get(index);
            Object changeSet = session.getDescriptor(object.getClass()).getObjectBuilder().createObjectChangeSet(object, changes, session);
            additions.put(index, changeSet);
        }

        this.addObjectList = additions;
        this.addIndexes = indexes;
    }

    /**
     * This method returns the collection of indexes in which changes were made to this collection.
     */
    public Vector getAddIndexes() {
        if (this.addIndexes == null) {
            this.addIndexes = new Vector(1);
        }
        return addIndexes;
    }

    /**
     * This method returns the collection of ChangeSets that were added to the collection.
     */
    public Hashtable getAddObjectList() {
        if (this.addObjectList == null) {
            this.addObjectList = new Hashtable(1);
        }
        return addObjectList;
    }

    /**
     * This method returns the index from where objects must be removed from the collection
     */
    public int getStartRemoveIndex() {
        return this.startIndexOfRemove;
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        // NOTE: if this class is ever used then this method will need to be implemented
    }

    /**
     * This method sets the index from where objects must be removed from the collection
     */
    public void setStartRemoveIndex(int startRemoveIndex) {
        this.startIndexOfRemove = startRemoveIndex;
    }

    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     */
    public void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        //if this class is ever used this method will need to be implemented
    }
}
