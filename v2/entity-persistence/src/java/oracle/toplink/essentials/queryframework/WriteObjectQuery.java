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
package oracle.toplink.essentials.queryframework;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;

/**
 * <p><b>Purpose</b>:
 * Used for inserting or updating objects
 * WriteObjectQuery determines whether to perform a insert or an update on the database.
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Determines whether to perform a insert or an update on the database.
 * <li> Stores object in identity map for insert if required.
 * </ul>
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class WriteObjectQuery extends ObjectLevelModifyQuery {
    public WriteObjectQuery() {
        super();
    }

    public WriteObjectQuery(Object objectToWrite) {
        this();
        setObject(objectToWrite);
    }

    public WriteObjectQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * INTERNAL:
     * Return if the object exists on the database or not.
     * This first checks existence in the chache, then on the database.
     */
    protected boolean doesObjectExist() {
        boolean doesExist;

        if (getSession().isUnitOfWork()) {
            doesExist = !((UnitOfWorkImpl)getSession()).isCloneNewObject(getObject());
            if (doesExist) {
                doesExist = ((UnitOfWorkImpl)getSession()).isObjectRegistered(getObject());
            }
        } else {
            //Initialize does exist query
            DoesExistQuery existQuery = (DoesExistQuery)getDescriptor().getQueryManager().getDoesExistQuery().clone();
            existQuery.setObject(getObject());
            existQuery.setPrimaryKey(getPrimaryKey());
            existQuery.setDescriptor(getDescriptor());
            existQuery.setTranslationRow(getTranslationRow());

            doesExist = ((Boolean)getSession().executeQuery(existQuery)).booleanValue();
        }
        
        return doesExist;
    }

    /**
     * INTERNAL:
     * Perform a does exist check to decide whether to perform an insert or update and
     * delegate the work to the mechanism.  Does exists check will also perform an
     * optimistic lock check if required.
     * @exception  DatabaseException - an error has occurred on the database
     * @exception  OptimisticLockException - an error has occurred using the optimistic lock feature
     * @return object - the object being written.
     */
    public Object executeDatabaseQuery() throws DatabaseException, OptimisticLockException {
        if (getObjectChangeSet() != null) {
            return getQueryMechanism().executeWriteWithChangeSet();
        } else {
            return getQueryMechanism().executeWrite();
        }
    }

    /**
     * INTERNAL:
     * Decide whether to perform an insert, update or delete and
     * delegate the work to the mechanism.
     */
    public void executeCommit() throws DatabaseException, OptimisticLockException {
        boolean doesExist = doesObjectExist();
        boolean shouldBeDeleted = shouldObjectBeDeleted();

        // Do insert, update or delete                    
        if (doesExist) {
            if (shouldBeDeleted) {
                // Must do a delete
                getQueryMechanism().deleteObjectForWrite();
            } else {
                // Must do an update            
                getQueryMechanism().updateObjectForWrite();
            }
        } else if (!shouldBeDeleted) {
            // Must do an insert
            getQueryMechanism().insertObjectForWrite();
        }
    }

    /**
     * INTERNAL:
     * Perform a does exist check to decide whether to perform an insert or update and
     * delegate the work to the mechanism.
     */
    public void executeCommitWithChangeSet() throws DatabaseException, OptimisticLockException {
        // Do insert of update                    
        if (!getObjectChangeSet().isNew()) {
            // Must do an update            
            if (!getSession().getCommitManager().isCommitInPreModify(objectChangeSet)) {
                //If the changeSet is in the PreModify then it is in the process of being written
                getQueryMechanism().updateObjectForWriteWithChangeSet();
            }
        } else {
            // check whether the object is already being committed -
            // if it is and it is new, then a shallow insert must be done
            if (getSession().getCommitManager().isCommitInPreModify(objectChangeSet)) {
                // a shallow insert must be performed
                this.dontCascadeParts();
                getQueryMechanism().insertObjectForWriteWithChangeSet();
                getSession().getCommitManager().markShallowCommit(object);
            } else {
                // Must do an insert
                getQueryMechanism().insertObjectForWriteWithChangeSet();
            }
        }
    }

    /**
     * INTERNAL:
     * Perform a shallow write. The decision, which shallow action should be 
     * executed is based on the existence of the associated object. If
     * the object exists, perform a shallow delete. Do a shallow
     * insert otherwise. 
     * Note that there currently is *no* shallow update operation. 
     * If shallow updates become necessary, the decision logic must
     * also perform a delete check as in {@link this.executeCommit}.
     */
    public void executeShallowWrite() {
        boolean doesExist = doesObjectExist();

        // Shallow writes only occur for inserts or deletes
        if (doesExist) {
            getQueryMechanism().shallowDeleteObjectForWrite(getObject(), this, getSession().getCommitManager());
        } else {
            getQueryMechanism().shallowInsertObjectForWrite(getObject(), this, getSession().getCommitManager());
        }
    }
    
    /**
     * PUBLIC:
     * Return if this is a write object query.
     */
    public boolean isWriteObjectQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();

        // Set the tranlation row, it may already be set in the custom query situation.
        if ((getTranslationRow() == null) || (getTranslationRow().isEmpty())) {
            setTranslationRow(getDescriptor().getObjectBuilder().buildRowForTranslation(getObject(), getSession()));
        }
    }

    /**
     * INTERNAL:
     * Return whether a dependent object should be deleted from the database or not.  
     * Dependent objects should not be removed if not already scheduled for removal in a UoW. 
     * Returns "true" outside a UoW. Used by relationship mappings when cascading a delete operation.
     */
    public boolean shouldDependentObjectBeDeleted(Object object) {
        boolean shouldBeDeleted;
        
        if (getSession().isUnitOfWork()) {
            shouldBeDeleted = ((UnitOfWorkImpl)getSession()).isObjectDeleted(object);
        }  else {
            // Deletes are cascaded outside a UoW
            shouldBeDeleted = true;
        }
        
        return shouldBeDeleted;
    }

    /**
     * INTERNAL:
     * Return if the attached object should be deleted from the database or not. 
     * This information is available only, if the session is a UoW. Returns "false" outside a UoW.
     * In this case an existence check should be performed and either an insert or update executed.
     * Only used internally.
     */
    protected boolean shouldObjectBeDeleted() {
        boolean shouldBeDeleted;
        
        if (getSession().isUnitOfWork()) {
            shouldBeDeleted = ((UnitOfWorkImpl)getSession()).isObjectDeleted(getObject());
        }  else {
            // Deletes must be explicitly user defined outside a UoW
            shouldBeDeleted = false;
        }
        
        return shouldBeDeleted;
    }
}
