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
import oracle.toplink.essentials.descriptors.DescriptorQueryManager;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Used for deleting objects.
 * <p>
 * <p><b>Responsibilities</b>:
 * Extract primary key from object and delete it.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class DeleteObjectQuery extends WriteObjectQuery {
    public DeleteObjectQuery() {
        super();
    }

    public DeleteObjectQuery(Object objectToDelete) {
        this();
        setObject(objectToDelete);
    }

    public DeleteObjectQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * INTERNAL:
     * Check to see if a custom query should be used for this query.
     * This is done before the query is copied and prepared/executed.
     * null means there is none.
     */
    protected DatabaseQuery checkForCustomQuery(AbstractSession session, AbstractRecord translationRow) {
        checkDescriptor(session);

        // check if user defined a custom query
        DescriptorQueryManager queryManager = getDescriptor().getQueryManager();
        if ((!isCallQuery())// this is not a hand-coded (custom SQL, SDK etc.) call
                 &&(!isUserDefined())// and this is not a user-defined query (in the query manager)
                 &&queryManager.hasDeleteQuery()) {// and there is a user-defined query (in the query manager)
            return queryManager.getDeleteQuery();
        }

        return null;
    }

    /**
     * INTERNAL:
     * Perform a delete.
     */
    public void executeCommit() throws DatabaseException {
        // object will only be null if the transaction is being commited directly from a changeset
        if (getObject() != null) {
            // if the object is not null then it is more effecient to build the row from the
            // object then the changeSet.
            getQueryMechanism().deleteObjectForWrite();
        } else {
            // TODO: Must this case be implemented?
            // has a changeSet so we must use it in the case that there is no object
//            getQueryMechanism().deleteObjectForWriteWithChangeSet();
        }
    }

    /**
     * INTERNAL:
     * Code was moved from UnitOfWork.internalExecuteQuery
     *
     * @param unitOfWork
     * @param translationRow
     * @return
     * @throws oracle.toplink.essentials.exceptions.DatabaseException
     * @throws oracle.toplink.essentials.exceptions.OptimisticLockException
     */
    protected Object executeInUnitOfWorkObjectLevelModifyQuery(UnitOfWorkImpl unitOfWork, AbstractRecord translationRow) throws DatabaseException, OptimisticLockException {
        Object result = unitOfWork.processDeleteObjectQuery(this);
        if (result != null) {
            // if the above method returned something then the unit of work
            //was not writing so the object has been stored to delete later
            //so return the object.  See the above method for the cases
            //where this object will be returned.
            return result;
        }
        return super.executeInUnitOfWorkObjectLevelModifyQuery(unitOfWork, translationRow);
    }

    /**
     * INTERNAL:
     * Perform a shallow delete.
     */
    public void executeShallowWrite() {
        getQueryMechanism().shallowDeleteObjectForWrite(getObject(), this, getSession().getCommitManager());
    }
    
    /**
     * PUBLIC:
     * Return if this is a delete object query.
     */
    public boolean isDeleteObjectQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() {
        super.prepare();

        getQueryMechanism().prepareDeleteObject();
    }

    /**
     * INTERNAL:
     * Set the properties needed to be cascaded into the custom query.
     */
    protected void prepareCustomQuery(DatabaseQuery customQuery) {
        DeleteObjectQuery customDeleteQuery = (DeleteObjectQuery)customQuery;
        customDeleteQuery.setObject(getObject());
        customDeleteQuery.setObjectChangeSet(getObjectChangeSet());
        customDeleteQuery.setCascadePolicy(getCascadePolicy());
        customDeleteQuery.setShouldMaintainCache(shouldMaintainCache());
        customDeleteQuery.setTranslationRow(customDeleteQuery.getDescriptor().getObjectBuilder().buildRow(getObject(), customDeleteQuery.getSession()));
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session. In particular,
     * verify that the object is not null and contains a valid primary key.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();

        // Add the write lock field if required		
        if (getDescriptor().usesOptimisticLocking()) {
            getDescriptor().getOptimisticLockingPolicy().addLockValuesToTranslationRow(this);
        }
    }
}
