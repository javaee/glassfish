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
 * This change Record is used to record the changes for AggregateObjectMapping.
 */
public class AggregateChangeRecord extends ChangeRecord implements oracle.toplink.essentials.changesets.AggregateChangeRecord {
    protected oracle.toplink.essentials.changesets.ObjectChangeSet changedObject;

    /**
     * This default constructor is reference internally by SDK XML project to mapp this class
     */
    public AggregateChangeRecord() {
        super();
    }

    /**
     * This constructor returns an ChangeRecord representing.
     * an AggregateMapping.
     * @param owner prototype.changeset.ObjectChangeSet represents the changeSet that uses this record
     */
    public AggregateChangeRecord(ObjectChangeSet owner) {
        this.owner = owner;
    }

    /**
     * ADVANCED:
     * This method is used to return the ObjectChangeSet representing the changed Aggregate.
     * @return prototype.changeset.ObjectChanges
     */
    public oracle.toplink.essentials.changesets.ObjectChangeSet getChangedObject() {
        return changedObject;
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        if (this.changedObject == null) {
            this.changedObject = ((AggregateChangeRecord)mergeFromRecord).getChangedObject();
            if(this.changedObject == null) {
                return;
            } else {
                mergeToChangeSet.addObjectChangeSetForIdentity((ObjectChangeSet)this.changedObject, mergeFromChangeSet.getUOWCloneForObjectChangeSet(this.changedObject));
                ((ObjectChangeSet)this.changedObject).updateReferences(mergeToChangeSet, mergeFromChangeSet);
                return;
            }
        }
        ((ObjectChangeSet)this.changedObject).mergeObjectChanges((ObjectChangeSet)((AggregateChangeRecord)mergeFromRecord).getChangedObject(), mergeToChangeSet, mergeFromChangeSet);
    }

    /**
     * INTERNAL:
     * This method is used to set the changed value or values
     * @param newValue prototype.changeset.ObjectChanges
     */
    public void setChangedObject(oracle.toplink.essentials.changesets.ObjectChangeSet newValue) {
        changedObject = newValue;
    }

    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     * If this is an aggregate change set then there is no need to update the
     * reference as the ChangeSet has no identity outside of this record
     * Check to see if it exists here already to prevent us from creating a little
     * extra garbage.
     */
    public void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        Object localChangeSet = mergeToChangeSet.getUOWCloneForObjectChangeSet(this.changedObject);
        if (localChangeSet == null) {
            mergeToChangeSet.addObjectChangeSetForIdentity((ObjectChangeSet)this.changedObject, mergeFromChangeSet.getUOWCloneForObjectChangeSet(this.changedObject));
        }
    }
}
