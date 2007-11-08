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
package oracle.toplink.essentials.internal.indirection;

import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * QueryBasedValueHolder wraps a database-stored object and
 * implements behavior to access it. The object is read from
 * the database by invoking a user-specified query.
 *
 * @see        ObjectLevelReadQuery
 * @author    Dorin Sandu
 */
public class QueryBasedValueHolder extends DatabaseValueHolder {

    /**
     * Stores the query to be executed.
     */
    protected transient ReadQuery query;

    /**
     * Store the uow identity so that it can be used to determine new transaction logic
     */
    /**
     * Initialize the query-based value holder.
     */
    public QueryBasedValueHolder(ReadQuery query, AbstractRecord row, AbstractSession session) {
        this.row = row;
        this.session = session;

        // Make sure not to put a ClientSession or IsolatedClientSession in
        // the shared cache (indirectly).
        // Skip this if unitOfWork, for we use session.isUnitOfWork() to implement
        // isTransactionalValueholder(), saving us from needing a boolean instance variable.
        // If unitOfWork this safety measure is deferred until merge time with
        // releaseWrappedValuehHolder.
        // Note that if isolated session & query will return itself, which is safe
        // for if isolated it will not go in the shared cache.
        if (!session.isUnitOfWork()) {
            this.session = session.getRootSession(query);
        }
        this.query = query;
    }

    /**
     * Return the query.
     */
    protected ReadQuery getQuery() {
        return query;
    }

    protected Object instantiate() throws DatabaseException {
        return instantiate(getSession());
    }

    /**
     * Instantiate the object by executing the query on the session.
     */
    protected Object instantiate(AbstractSession session) throws DatabaseException {
        if (session == null){
            throw ValidationException.instantiatingValueholderWithNullSession();
        }
        return session.executeQuery(getQuery(), getRow());
    }

    /**
     * Triggers UnitOfWork valueholders directly without triggering the wrapped
     * valueholder (this).
     * <p>
     * When in transaction and/or for pessimistic locking the UnitOfWorkValueHolder
     * needs to be triggered directly without triggering the wrapped valueholder.
     * However only the wrapped valueholder knows how to trigger the indirection,
     * i.e. it may be a batchValueHolder, and it stores all the info like the row
     * and the query.
     * Note: This method is not thread-safe.  It must be used in a synchronizaed manner
     */
    public Object instantiateForUnitOfWorkValueHolder(UnitOfWorkValueHolder unitOfWorkValueHolder) {
        return instantiate(unitOfWorkValueHolder.getUnitOfWork());
    }

    /**
     * Releases a wrapped valueholder privately owned by a particular unit of work.
     * <p>
     * When unit of work clones are built directly from rows no object in the shared
     * cache points to this valueholder, so it can store the unit of work as its
     * session.  However once that UnitOfWork commits and the valueholder is merged
     * into the shared cache, the session needs to be reset to the root session, ie.
     * the server session.
     */
    public void releaseWrappedValueHolder() {
        AbstractSession session = getSession();
        if ((session != null) && session.isUnitOfWork()) {
            setSession(session.getRootSession(query));
        }
    }

    /**
     * Reset all the fields that are not needed after instantiation.
     */
    protected void resetFields() {
        super.resetFields();
        setQuery(null);
    }

    /**
     * Set the query.
     */
    protected void setQuery(ReadQuery theQuery) {
        query = theQuery;
    }

    /**
     * INTERNAL:
     * Answers if this valueholder is a pessimistic locking one.  Such valueholders
     * are special in that they can be triggered multiple times by different
     * UnitsOfWork.  Each time a lock query will be issued.  Hence even if
     * instantiated it may have to be instantiated again, and once instantatiated
     * all fields can not be reset.
     * <p>
     * Since locks will be issued each time this valueholder is triggered,
     * triggering this directly on the session in auto commit mode will generate
     * an exception.  This only UnitOfWorkValueHolder's wrapping this can trigger
     * it.
     * Note: This method is not thread-safe.  It must be used in a synchronizaed manner
     */
    public boolean isPessimisticLockingValueHolder() {
        // Get the easy checks out of the way first.
        if ((getQuery() == null) || !getQuery().isObjectLevelReadQuery()) {
            return false;
        }
        ObjectLevelReadQuery query = (ObjectLevelReadQuery)getQuery();

        // Note even if the reference class is not locked, but the valueholder query
        // has joined attributes, then this may count as a lock query.
        // This means it is possible to trigger a valueholder to get an object which
        // is not to be pess. locked and get an exception for triggering it on the
        // session outside a transaction.
        return query.isLockQuery(getSession());
    }
}
