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

/**
 * <p>
 * <b>Purpose</b>: To record the changes for an attribute that references a single Object
 * @see RelatedClasses prototype.changeset.CollectionChangeRecord,prototype.changeset.SingleObjectChangeRecord
 */
public class ObjectReferenceChangeRecord extends ChangeRecord implements oracle.toplink.essentials.changesets.ObjectReferenceChangeRecord {

    /** This is the object change set that the attribute points to. */
    protected ObjectChangeSet newValue;
    
    /** A reference to the old value must also be sotred.  This is only required for the commit and must never be serialized. */
    protected transient Object oldValue;

    /**
     * INTERNAL:
     * This default constructor is reference internally by SDK XML project to mapp this class
     */
    public ObjectReferenceChangeRecord() {
        super();
    }

    /**
     * INTERNAL:
     * This Constructor is used to create an ObjectReferenceChangeRecord With an owner
     * @param owner prototype.changeset.ObjectChangeSet
     */
    public ObjectReferenceChangeRecord(ObjectChangeSet owner) {
        this.owner = owner;
    }

    /**
     * ADVANCED:
     * Returns the new reference for this object
     * @return prototype.changeset.ObjectChangeSet
     */
    public oracle.toplink.essentials.changesets.ObjectChangeSet getNewValue() {
        return newValue;
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet((ObjectChangeSet)((ObjectReferenceChangeRecord)mergeFromRecord).getNewValue(), mergeFromChangeSet);
        this.newValue = localChangeSet;
    }


    /**
     * This method sets the value of the change to be made.
     * @param newValue prototype.changeset.ObjectChangeSet
     */
    public void setNewValue(oracle.toplink.essentials.changesets.ObjectChangeSet newValue) {
        this.newValue = (ObjectChangeSet)newValue;
    }

    /**
     * This method sets the value of the change to be made.
     * @param newValue prototype.changeset.ObjectChangeSet
     */
    public void setNewValue(ObjectChangeSet newValue) {
        this.newValue = newValue;
    }
    
    /**
     * Return the old value of the object reference.
     * This is used during the commit for private-owned references.
     */
    public Object getOldValue() {
        return oldValue;
    }
    
    /**
     * Set the old value of the object reference.
     * This is used during the commit for private-owned references.
     */
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     */
    public void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        this.setNewValue(mergeToChangeSet.findOrIntegrateObjectChangeSet((ObjectChangeSet)this.getNewValue(), mergeFromChangeSet));
    }
}
