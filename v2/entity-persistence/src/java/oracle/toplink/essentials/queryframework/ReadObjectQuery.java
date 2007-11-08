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

import java.util.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.descriptors.DescriptorQueryManager;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>:
 * Concrete class for all read queries involving a single object.
 * <p>
 * <p><b>Responsibilities</b>:
 * Return a single object for the query.
 * Implements the inheritance feature when dealing with abstract descriptors.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class ReadObjectQuery extends ObjectLevelReadQuery {

    /** Object that can be used in place of a selection criteria. */
    protected transient Object selectionObject;

    /** Key that can be used in place of a selection criteria. */
    protected Vector selectionKey;

    /** Can be used to refresh a specific non-cached instance from the database. */
    protected boolean shouldLoadResultIntoSelectionObject = false;

    /**
     * PUBLIC:
     * Return a new read object query.
     * A reference class must be specified before execution.
     * It is better to provide the class and expression builder on construction to esnure a single expression builder is used.
     * If no selection criteria is specified this will reads the first object found in the database.
     */
    public ReadObjectQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Return a new read object query.
     * By default, the query has no selection criteria. Executing this query without
     * selection criteria will always result in a database access to read the first
     * instance of the specified Class found in the database. This is true no
     * matter how cache usage is configured and even if an instance of the
     * specified Class exists in the cache.
     * Executing a query with selection criteria allows you to avoid a database
     * access if the selected instance is in the cache. For this reason, you may whish to use a ReadObjectQuery constructor that takes selection criteria, such as: {@link #ReadObjectQuery(Class, Call)}, {@link #ReadObjectQuery(Class, Expression)}, {@link #ReadObjectQuery(Class, ExpressionBuilder)}, {@link #ReadObjectQuery(ExpressionBuilder)}, {@link #ReadObjectQuery(Object)}, or {@link #ReadObjectQuery(Object, QueryByExamplePolicy)}.
     */
    public ReadObjectQuery(Class classToRead) {
        this();
        setReferenceClass(classToRead);
    }

    /**
     * PUBLIC:
     * Return a new read object query for the class and the selection criteria.
     */
    public ReadObjectQuery(Class classToRead, Expression selectionCriteria) {
        this();
        setReferenceClass(classToRead);
        setSelectionCriteria(selectionCriteria);
    }

    /**
     * PUBLIC:
     * Return a new read object query for the class.
     * The expression builder must be used for all associated expressions used with the query.
     */
    public ReadObjectQuery(Class classToRead, ExpressionBuilder builder) {
        this();
        this.defaultBuilder = builder;
        setReferenceClass(classToRead);
    }

    /**
     * PUBLIC:
     * Return a new read object query.
     * The call represents a database interaction such as SQL, Stored Procedure.
     */
    public ReadObjectQuery(Class classToRead, Call call) {
        this();
        setReferenceClass(classToRead);
        setCall(call);
    }

    /**
     * PUBLIC:
     * Return a new read object query.
     * The call represents a database interaction such as SQL, Stored Procedure.
     */
    public ReadObjectQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * PUBLIC:
     * Return a query to read the object with the same primary key as the provided object.
     * Note: This is not a query by example object, only the primary key will be used for the selection criteria.
     */
    public ReadObjectQuery(Object objectToRead) {
        this();
        setSelectionObject(objectToRead);
    }

    /**
     * PUBLIC:
     * The expression builder should be provide on creation to ensure only one is used.
     */
    public ReadObjectQuery(ExpressionBuilder builder) {
        this();
        this.defaultBuilder = builder;
    }

    /**
     * PUBLIC:
     * The cache will be checked only if the query contains exactly the primary key.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void checkCacheByExactPrimaryKey() {
        setCacheUsage(CheckCacheByExactPrimaryKey);
    }

    /**
     * PUBLIC:
     * This is the default, the cache will be checked only if the query contains the primary key.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void checkCacheByPrimaryKey() {
        setCacheUsage(CheckCacheByPrimaryKey);
    }

    /**
     * PUBLIC:
     * The cache will be checked completely, then if the object is not found or the query too complex the database will be queried.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void checkCacheThenDatabase() {
        setCacheUsage(CheckCacheThenDatabase);
    }

    /**
     * INTERNAL:
     * Ensure that the descriptor has been set.
     */
    public void checkDescriptor(AbstractSession session) throws QueryException {
        if (getReferenceClass() == null) {
            throw QueryException.referenceClassMissing(this);
        }

        if (getDescriptor() == null) {
            ClassDescriptor referenceDescriptor;
            //Bug#3947714  In case getSelectionObject() is proxy            
            if (getSelectionObject() != null && session.getProject().hasProxyIndirection()) {
                referenceDescriptor = session.getDescriptor(getSelectionObject());            
            } else {
                referenceDescriptor = session.getDescriptor(getReferenceClass());                
            }
            if (referenceDescriptor == null) {
                throw QueryException.descriptorIsMissing(getReferenceClass(), this);
            }
            setDescriptor(referenceDescriptor);
        }
    }

    /**
     * INTERNAL:
     * The cache check is done before the prepare as a hit will not require the work to be done.
     */
    protected Object checkEarlyReturnImpl(AbstractSession session, AbstractRecord translationRow) {
        // Do a cache lookup
        if (shouldMaintainCache() && (!shouldRefreshIdentityMapResult()) && (!(shouldCheckDescriptorForCacheUsage() && getDescriptor().shouldDisableCacheHits())) && (shouldCheckCache())) {
            Object cachedObject = getQueryMechanism().checkCacheForObject(translationRow, session);

            // Optimization: If find deleted object by exact primary
            // key expression or selection object/key just abort.
            if (cachedObject == InvalidObject.instance) {
                return cachedObject;
            }
            if (cachedObject != null) {
                if (shouldLoadResultIntoSelectionObject()) {
                    ObjectBuilder builder = getDescriptor().getObjectBuilder();
                    builder.copyInto(cachedObject, getSelectionObject());
                    //put this object into the cache.  This may cause some loss of identity
                    session.getIdentityMapAccessorInstance().putInIdentityMap(getSelectionObject());
                    cachedObject = getSelectionObject();
                }

                // check locking.  If clone has not been locked, do not early return cached object
                if (isLockQuery() && (session.isUnitOfWork() && !((UnitOfWorkImpl)session).isPessimisticLocked(cachedObject))) {
                    return null;
                }
            }
            if (shouldUseWrapperPolicy()) {
                cachedObject = getDescriptor().getObjectBuilder().wrapObject(cachedObject, session);
            }
            return cachedObject;
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Check to see if a custom query should be used for this query.
     * This is done before the query is copied and prepared/executed.
     * null means there is none.
     */
    protected DatabaseQuery checkForCustomQuery(AbstractSession session, AbstractRecord translationRow) {
        checkDescriptor(session);

        // check if user defined a custom query in the query manager
        if (!isUserDefined()) {
            if (isCallQuery()) {
                // this is a hand-coded (custom SQL, SDK etc.) call
                return null;
            }
            DescriptorQueryManager descriptorQueryManager = getDescriptor().getQueryManager();

            // By default all descriptors have a custom ("static") read-object query.
            // This allows the read-object query and SQL to be prepare once.
            if (descriptorQueryManager.hasReadObjectQuery()) {
                // If the query require special SQL generation or execution do not use the static read object query.
                // PERF: the read-object query should always be static to ensure no regeneration of SQL.
                if (getJoinedAttributeManager().hasJoinedAttributeExpressions() || hasPartialAttributeExpressions() || hasAsOfClause() || hasNonDefaultFetchGroup() || (!wasDefaultLockMode()) || (!shouldIgnoreBindAllParameters())) {
                    return null;
                }

                if ((getSelectionKey() != null) || (getSelectionObject() != null)) {// Must be primary key.
                    return descriptorQueryManager.getReadObjectQuery();
                }

                if (getSelectionCriteria() != null) {
                    AbstractRecord primaryKeyRow = getDescriptor().getObjectBuilder().extractPrimaryKeyRowFromExpression(getSelectionCriteria(), translationRow, session);

                    // Only execute the query if the selection criteria has the primary key fields set
                    if (primaryKeyRow != null) {
                        return descriptorQueryManager.getReadObjectQuery();
                    }
                }
            }
        }

        return null;
    }

    /**
     * INTERNAL:
     * Conform the result in the UnitOfWork.
     */
    protected Object conformResult(Object result, UnitOfWorkImpl unitOfWork, AbstractRecord databaseRow, boolean buildDirectlyFromRows) {
        // Note that if the object does not conform even though other objects might exist on the database null is returned.
        // Note that new objects is checked before the read is executed so does not have to be re-checked.
        // Must unwrap as the built object is always wrapped.
        // Note the object is unwrapped on the parent which it belongs to, as we
        // do not want to trigger a registration just yet.
        Object implementation = null;
        if (buildDirectlyFromRows) {
            implementation = result;
        } else {
            implementation = getDescriptor().getObjectBuilder().unwrapObject(result, unitOfWork.getParent());
        }

        Expression selectionCriteriaClone = null;
        if ((getSelectionCriteria() != null) && (getSelectionKey() == null) && (getSelectionObject() == null)) {
            selectionCriteriaClone = (Expression)getSelectionCriteria().clone();
            selectionCriteriaClone.getBuilder().setSession(unitOfWork.getRootSession(null));
            selectionCriteriaClone.getBuilder().setQueryClass(getReferenceClass());
        }

        Object clone = conformIndividualResult(implementation, unitOfWork, databaseRow, selectionCriteriaClone, null, buildDirectlyFromRows);
        if (clone == null) {
            return clone;
        }

        if (shouldUseWrapperPolicy()) {
            return getDescriptor().getObjectBuilder().wrapObject(clone, unitOfWork);
        } else {
            return clone;
        }
    }

    /**
     * PUBLIC:
     * Do not refesh/load into the selection object, this is the default.
     * This property allows for the selection object of the query to be refreshed or put into the TopLink cache.
     * By default on a read or refresh the object in the cache is refreshed and returned or a new object is built from the database,
     * in some cases such as EJB BMP it is desirable to refresh or load into the object passed into the read object query.
     * <p>Note: This forces the selection object into the cache a replaces any existing object that may already be there,
     * this is a strict violation of object identity and other objects can still be refering to the old object.
     */
    public void dontLoadResultIntoSelectionObject() {
        setShouldLoadResultIntoSelectionObject(false);
    }

    /**
     * INTERNAL:
     * Execute the query.
     * Do a cache lookup and build object from row if required.
     * @exception  DatabaseException - an error has occurred on the database
     * @return object - the first object found or null if none.
     */
    protected Object executeObjectLevelReadQuery() throws DatabaseException {
        AbstractRecord row = null;

        // If using -m joins, must select all rows.
        if (getJoinedAttributeManager().isToManyJoin()) {
            List rows = getQueryMechanism().selectAllRows();
            if (rows.size() > 0) {
                row = (AbstractRecord)rows.get(0);
            }
            getJoinedAttributeManager().setDataResults(rows, getSession());
        } else {
            row = getQueryMechanism().selectOneRow();
        }
        setExecutionTime(System.currentTimeMillis());
        Object result = null;

        if (getSession().isUnitOfWork()) {
            result = registerResultInUnitOfWork(row, (UnitOfWorkImpl)getSession(), getTranslationRow(), true);
        } else {
            if (row != null) {
                result = buildObject(row);
            }
        }

        if (shouldIncludeData()) {
            ComplexQueryResult complexResult = new ComplexQueryResult();
            complexResult.setResult(result);
            complexResult.setData(row);
            return complexResult;
        }

        return result;
    }

    /**
     * PUBLIC:
     * The primary key can be specified if used instead of an expression or selection object.
     * If composite the primary must be in the same order as defined in the descriptor.
     */
    public Vector getSelectionKey() {
        return selectionKey;

    }

    /**
     * PUBLIC:
     * Return the selection object of the query.
     * This can be used instead of a where clause expression for single object primary key queries.
     * The selection object given should have a primary key defined,
     * this primary key will be used to query the database instance of the same object.
     * This is a basic form of query by example where only the primary key is required,
     * it can be used for simple query forms, or testing.
     */
    public Object getSelectionObject() {
        return selectionObject;
    }

    /**
     * PUBLIC:
     * Return if this is a read object query.
     */
    public boolean isReadObjectQuery() {
        return true;
    }

    /**
     * PUBLIC:
     * Allow for the selection object of the query to be refreshed or put into the TopLink cache.
     * By default on a read or refresh the object in the cache is refreshed and returned or a new object is built from the database,
     * in some cases such as EJB BMP it is desirable to refresh or load into the object passed into the read object query.
     * <p>Note: This forces the selection object into the cache a replaces any existing object that may already be there,
     * this is a strict violation of object identity and other objects can still be refering to the old object.
     */
    public void loadResultIntoSelectionObject() {
        setShouldLoadResultIntoSelectionObject(true);
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() throws QueryException {
        super.prepare();

        if ((getSelectionKey() != null) || (getSelectionObject() != null)) {
            // The expression is set in the prepare as params.
            setSelectionCriteria(getDescriptor().getObjectBuilder().getPrimaryKeyExpression());
            // For bug 2989998 the translation row is required to be set at this point.
            if (!shouldPrepare()) {
                if (getSelectionKey() != null) {
                    // Row must come from the key.
                    setTranslationRow(getDescriptor().getObjectBuilder().buildRowFromPrimaryKeyValues(getSelectionKey(), getSession()));
                } else {//(getSelectionObject() != null)
                    setTranslationRow(getDescriptor().getObjectBuilder().buildRowForTranslation(getSelectionObject(), getSession()));
                }
            }
        }

        // If using -m joining select all rows.
        if (getJoinedAttributeManager().isToManyJoin()) {
            getQueryMechanism().prepareSelectAllRows();
        } else {
            getQueryMechanism().prepareSelectOneRow();
        }
    }

    /**
     * INTERNAL:
     * Set the properties needed to be cascaded into the custom query inlucding the translation row.
     */
    protected void prepareCustomQuery(DatabaseQuery customQuery) {
        ReadObjectQuery customReadQuery = (ReadObjectQuery)customQuery;
        customReadQuery.setShouldRefreshIdentityMapResult(shouldRefreshIdentityMapResult());
        customReadQuery.setCascadePolicy(getCascadePolicy());
        customReadQuery.setShouldMaintainCache(shouldMaintainCache());
        customReadQuery.setShouldUseWrapperPolicy(shouldUseWrapperPolicy());
        // CR... was missing some values, execution could cause infinite loop.
        customReadQuery.setQueryId(getQueryId());
        customReadQuery.setExecutionTime(getExecutionTime());
        customReadQuery.setShouldLoadResultIntoSelectionObject(shouldLoadResultIntoSelectionObject());
        AbstractRecord primaryKeyRow;
        if (getSelectionObject() != null) {
            // CR#... Must also set the selection object as may be loading into the object (refresh)
            customReadQuery.setSelectionObject(getSelectionObject());
            // The translation/primary key row will be set in prepareForExecution.
        } else if (getSelectionKey() != null) {
            customReadQuery.setSelectionKey(getSelectionKey());
        } else {
            // The primary key row must be used.
            primaryKeyRow = customQuery.getDescriptor().getObjectBuilder().extractPrimaryKeyRowFromExpression(getSelectionCriteria(), customQuery.getTranslationRow(), customReadQuery.getSession());
            customReadQuery.setTranslationRow(primaryKeyRow);
        }
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();

        // For bug 2989998 the translation row now sometimes set earlier in prepare.
        if (shouldPrepare()) {
            if (getSelectionKey() != null) {
                // Row must come from the key.
                setTranslationRow(getDescriptor().getObjectBuilder().buildRowFromPrimaryKeyValues(getSelectionKey(), getSession()));
            } else if (getSelectionObject() != null) {
                // The expression is set in the prepare as params.
                setTranslationRow(getDescriptor().getObjectBuilder().buildRowForTranslation(getSelectionObject(), getSession()));
            }
        }
    }

    /**
     * INTERNAL:
     * All objects queried via a UnitOfWork get registered here.  If the query
     * went to the database.
     * <p>
     * Involves registering the query result individually and in totality, and
     * hence refreshing / conforming is done here.
     * @param result may be collection (read all) or an object (read one),
     * or even a cursor.  If in transaction the shared cache will
     * be bypassed, meaning the result may not be originals from the parent
     * but raw database rows.
     * @param unitOfWork the unitOfWork the result is being registered in.
     * @param arguments the original arguments/parameters passed to the query
     * execution.  Used by conforming
     * @param buildDirectlyFromRows If in transaction must construct
     * a registered result from raw database rows.
     * @return the final (conformed, refreshed, wrapped) UnitOfWork query result
     */
    public Object registerResultInUnitOfWork(Object result, UnitOfWorkImpl unitOfWork, AbstractRecord arguments, boolean buildDirectlyFromRows) {
        if (result == null) {
            return null;
        }
        if (shouldConformResultsInUnitOfWork() || getDescriptor().shouldAlwaysConformResultsInUnitOfWork()) {
            return conformResult(result, unitOfWork, arguments, buildDirectlyFromRows);
        }

        Object clone = registerIndividualResult(result, unitOfWork, buildDirectlyFromRows, null);

        if (shouldUseWrapperPolicy()) {
            clone = getDescriptor().getObjectBuilder().wrapObject(clone, unitOfWork);
        }
        return clone;
    }

    /**
     * PUBLIC:
     * The primary key can be specified if used instead of an expression or selection object.
     * If composite the primary must be in the same order as defined in the descriptor.
     */
    public void setSelectionKey(Vector selectionKey) {
        this.selectionKey = selectionKey;
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Used to set the where clause of the query.
     * This can be used instead of a where clause expression for single object primary key queries.
     * The selection object given should have a primary key defined,
     * this primary key will be used to query the database instance of the same object.
     * This is a basic form of query by example where only the primary key is required,
     * it can be used for simple query forms, or testing.
     */
    public void setSelectionObject(Object selectionObject) {
        if (selectionObject == null) {
            throw QueryException.selectionObjectCannotBeNull(this);
        }
        setSelectionKey(null);
        // setIsPrepared(false) triggered by previous.
        setReferenceClass(selectionObject.getClass());
        this.selectionObject = selectionObject;
    }

    /**
     * PUBLIC:
     * Allow for the selection object of the query to be refreshed or put into the TopLink cache.
     * By default on a read or refresh the object in the cache is refreshed and returned or a new object is built from the database,
     * in some cases such as EJB BMP it is desirable to refresh or load into the object passed into the read object query.
     * <p>Note: This forces the selection object into the cache a replaces any existing object that may already be there,
     * this is a strict violation of object identity and other objects can still be refering to the old object.
     */
    public void setShouldLoadResultIntoSelectionObject(boolean shouldLoadResultIntoSelectionObject) {
        this.shouldLoadResultIntoSelectionObject = shouldLoadResultIntoSelectionObject;
    }

    /**
     * PUBLIC:
     * The primary key can be specified if used instead of an expression or selection object.
     */
    public void setSingletonSelectionKey(Object selectionKey) {
        Vector key = new Vector();
        key.addElement(selectionKey);
        setSelectionKey(key);

    }

    /**
     * PUBLIC:
     * Return if the cache should be checked.
     */
    public boolean shouldCheckCache() {
        return getCacheUsage() != DoNotCheckCache;
    }

    /**
     * PUBLIC:
     * Return if cache should be checked.
     */
    public boolean shouldCheckCacheByExactPrimaryKey() {
        return getCacheUsage() == CheckCacheByExactPrimaryKey;
    }

    /**
     * PUBLIC:
     * Return if cache should be checked.
     */
    public boolean shouldCheckCacheByPrimaryKey() {
        return (getCacheUsage() == CheckCacheByPrimaryKey) || (getCacheUsage() == UseDescriptorSetting);
    }

    /**
     * PUBLIC:
     * Return if cache should be checked.
     */
    public boolean shouldCheckCacheThenDatabase() {
        return getCacheUsage() == CheckCacheThenDatabase;
    }

    /**
     * PUBLIC:
     * return true if the result should be loaded into the passed in selection Object
     */
    public boolean shouldLoadResultIntoSelectionObject() {
        return shouldLoadResultIntoSelectionObject;
    }

    /**
     * INTERNAL:
     * Return if the query has an non-default fetch group defined for itself.
     */
    protected boolean hasNonDefaultFetchGroup() {
        return getDescriptor().hasFetchGroupManager() && ((this.getFetchGroup() != null) || (this.getFetchGroupName() != null) || (!this.shouldUseDefaultFetchGroup()));

    }
}
