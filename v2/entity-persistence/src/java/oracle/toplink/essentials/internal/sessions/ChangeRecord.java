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

import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import java.io.Serializable;

/**
 * <p>
 * <b>Purpose</b>: This class was designed as a superclass to all possible Change Record types.
 * These Change Records holds the changes made to the objects
 * <p>
 *
 * @see KnownSubclasses prototype.changeset.CollectionChangeRecord,prototype.changeset.DirectToFieldChangeRecord,prototype.changeset.SingleObjectChangeRecord
 */
public abstract class ChangeRecord implements Serializable, oracle.toplink.essentials.changesets.ChangeRecord {

    /**
     * This is the attribute name that this change record represents
     */
    protected String attribute;

    /**
     * This attribute stores the mapping allong with the attribute so that the mapping does not need to be looked up
     */
    protected transient DatabaseMapping mapping;

    /** This is the object change set that holds this record **/
    protected ObjectChangeSet owner;

    /**
     * ADVANCED:
     * Returns the name of the attribute this ChangeRecord Represents
     * @return java.lang.String
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * ADVANCED:
     * Returns the mapping for the attribute this ChangeRecord Represents
     */
    public DatabaseMapping getMapping() {
        return mapping;
    }

    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 3:42:14 PM)
     * @return prototype.changeset.ObjectChangeSet
     */
    public oracle.toplink.essentials.changesets.ObjectChangeSet getOwner() {
        return (oracle.toplink.essentials.changesets.ObjectChangeSet)owner;
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public abstract void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet);

    /**
     * INTERNAL:
     * Ensure this change record is ready to by sent remotely for cache synchronization
     * In general, this means setting the CacheSynchronizationType on any ObjectChangeSets
     * associated with this ChangeRecord
     */
    public void prepareForSynchronization(AbstractSession session) {
    }

    /**
     * Sets the name of the attribute that this Record represents
     * @param newValue java.lang.String
     */
    public void setAttribute(String newValue) {
        this.attribute = newValue;
    }

    /**
     * Sets the mapping for the attribute that this Record represents
     */
    public void setMapping(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * INTERNAL:
     * This method is used to set the ObjectChangeSet that uses this Record in that Record
     * @param newOwner prototype.changeset.ObjectChangeSet The changeSet that uses this record
     */
    public void setOwner(ObjectChangeSet newOwner) {
        owner = newOwner;
    }

    public String toString() {
        return this.getClass().getName() + "(" + getAttribute() + ")";
    }

    /**
     * INTERNAL:
     * used by the record to update the new value ignores the value in the default implementation
     */
    public void updateChangeRecordWithNewValue(Object newValue) {
        //no op
    }

    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     */
    public abstract void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet);
}
