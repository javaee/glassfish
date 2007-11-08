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

import java.util.Vector;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.queryframework.DatabaseQueryMechanism;
import oracle.toplink.essentials.internal.queryframework.ExpressionQueryMechanism;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * PUBLIC:
 * Query used to perform a bulk delete using TopLink's expression framework.
 *
 * @author Andrei Ilitchev
 * @date August 18, 2005
 */
public abstract class ModifyAllQuery extends ModifyQuery {

    /** Cache usage flags */
    public static final int NO_CACHE = 0;
    public static final int INVALIDATE_CACHE = 1;

    private int m_cacheUsage = INVALIDATE_CACHE;

    protected Class referenceClass;
    protected String referenceClassName;
    
    /** Number of modified objects */
    protected transient Integer result;

    /** Indicates whether execution should be deferred in UOW */
    private boolean shouldDeferExecutionInUOW;

    /** Provide a default builder so that it's easier to be consistent */
    protected ExpressionBuilder defaultBuilder;
    
    /** Indicates whether the query was prepared so that it will execute using temp storage */
    protected boolean isPreparedUsingTempStorage;
    
    /**
     * PUBLIC:
     */
    public ModifyAllQuery() {
        super();
        shouldDeferExecutionInUOW = true;
    }

    /**
     * PUBLIC:
     * Create a new update all query for the class specified.
     */
    public ModifyAllQuery(Class referenceClass) {
        this();
        setReferenceClass(referenceClass);
    }

    /**
     * PUBLIC:
     * Create a new update all query for the class and the selection criteria
     * specified.
     */
    public ModifyAllQuery(Class referenceClass, Expression selectionCriteria) {
        this();
        setReferenceClass(referenceClass);
        setSelectionCriteria(selectionCriteria);
    }

    /**
     * PUBLIC:
     * Return true if this is a modify query.
     */
    public boolean isModifyQuery() {
        return true;
    }

    /**
     * INTERNAL:
     */
    public void setIsPreparedUsingTempStorage(boolean isPreparedUsingTempStorage) {
        this.isPreparedUsingTempStorage = isPreparedUsingTempStorage;
    }

    /**
     * INTERNAL:
     */
    public boolean isPreparedUsingTempStorage() {
        return isPreparedUsingTempStorage;
    }

    /**
     * INTERNAL
     * Used to give the subclasses oportunity to copy aspects of the cloned query
     * to the original query.  The clones of all the ModifyAllQueries will be added to modifyAllQueries for validation.
     */
    protected void clonedQueryExecutionComplete(DatabaseQuery query, AbstractSession session) {
        super.clonedQueryExecutionComplete(query, session);
        
        if (session.isUnitOfWork()) {
            ((UnitOfWorkImpl)session).storeModifyAllQuery(query);
        }
    }

    /**
     * INTERNAL:
     * Override query execution where Session is a UnitOfWork.
     * <p>
     * If there are objects in the cache return the results of the cache lookup.
     *
     * @param unitOfWork - the session in which the receiver will be executed.
     * @param translationRow - the arguments
     * @exception  DatabaseException - an error has occurred on the database.
     * @exception  OptimisticLockException - an error has occurred using the optimistic lock feature.
     * @return An object, the result of executing the query.
     */
    public Object executeInUnitOfWork(UnitOfWorkImpl unitOfWork, AbstractRecord translationRow) throws DatabaseException, OptimisticLockException {
        if (unitOfWork.isNestedUnitOfWork()) {
            throw ValidationException.nestedUOWNotSupportedForModifyAllQuery();
        }

        //Bug4607551  For UpdateAllQuery, if deferred, add the original query with a translation row to the deferredUpdateAllQueries for execution.  
        //No action for non-deferred.  Later on the clones of all the UpdateAllQuery's will be added to modifyAllQueries for validation.
        if(shouldDeferExecutionInUOW()) {
            unitOfWork.storeDeferredModifyAllQuery(this, translationRow);
            result = null;
        } else {
            if(!unitOfWork.isInTransaction()) {
                unitOfWork.beginEarlyTransaction();
            }
            unitOfWork.setWasNonObjectLevelModifyQueryExecuted(true);
            result = (Integer)super.executeInUnitOfWork(unitOfWork, translationRow);
        }
        return result;
    }

    /**
     * PUBLIC:
     * Return the cache usage for this query.
     */
    public int getCacheUsage() {
        return m_cacheUsage;
    }

    /**
     * PUBLIC:
     * Get the expression builder which should be used for this query.
     * This expression builder should be used to build all expressions used by this query.
     */
    public ExpressionBuilder getExpressionBuilder() {
        if (defaultBuilder == null) {
            initializeDefaultBuilder();
        }

        return defaultBuilder;
    }
    
    /**
     * INTERNAL
     * Sets the default expression builder for this query.
     */
    public void setExpressionBuilder(ExpressionBuilder builder) {
        this.defaultBuilder = builder;
    }

    /**
     * INTERNAL:
     * Return the name of the reference class of the query.
     * Used by the Mappign Workbench to avoid classpath dependancies
     */
    public String getReferenceClassName() {
        if ((referenceClassName == null) && (referenceClass != null)) {
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * PUBLIC:
     * Return the reference class for this query.
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * INTERNAL:
     * Invalid the cache, that is, those objects in the cache that were affected
     * by the query.
     */
    protected void invalidateCache() {
        oracle.toplink.essentials.sessions.IdentityMapAccessor identityMapAccessor = getSession().getIdentityMapAccessor();
        if (getSelectionCriteria() == null) {
            // Invalidate the whole class since the user did not specify a where clause
            if(getDescriptor().isChildDescriptor()) {
                Vector collectionToInvalidate = identityMapAccessor.getAllFromIdentityMap(null, getReferenceClass(), getTranslationRow(), null);
                identityMapAccessor.invalidateObjects(collectionToInvalidate);
            } else {
                // if it's either a root class or there is no inheritance just clear the identity map
                identityMapAccessor.invalidateClass(getReferenceClass());
            }
        } else {
            // Invalidate only those objects in the cache that match the selection criteria
            //Bug:4293920, expression parameters were not passed in
            boolean noObjectsModifiedInDb = result != null && result.intValue() == 0;
            try {
                InMemoryQueryIndirectionPolicy policy = new InMemoryQueryIndirectionPolicy();
                if(noObjectsModifiedInDb) {
                    policy.ignoreIndirectionExceptionReturnNotConformed();
                } else {
                    policy.ignoreIndirectionExceptionReturnConformed();
                }
                Vector collectionToInvalidate = identityMapAccessor.getAllFromIdentityMap(getSelectionCriteria(), getReferenceClass(), getTranslationRow(), policy);
                identityMapAccessor.invalidateObjects(collectionToInvalidate);
            } catch (QueryException ex) {
                if(ex.getErrorCode() == QueryException.CANNOT_CONFORM_EXPRESSION) {
                    // If no objects changed in the db - don't invalidate, ignore.
                    if(!noObjectsModifiedInDb) {
                        // Invalidate the whole class since the expression can't be selected in memory
                        identityMapAccessor.invalidateClass(getReferenceClass());
                    }
                } else {
                    throw ex;
                }
            }
        }
    }

    /**
     * INTERNAL:
     * After execution we need to merge the changes into the shared cache
     */
    public void mergeChangesIntoSharedCache() {
        if (shouldInvalidateCache()) {
            invalidateCache();
        }
    }

    /**
     * PUBLIC:
     * Set the level of cache support for this query, either NONE or INVALIDATE.
     */
    public void setCacheUsage(int cacheUsage) {
        m_cacheUsage = cacheUsage;
    }

    /**
     * PUBLIC:
     * Set the reference class this query.
     */
    public void setReferenceClass(Class referenceClass) {
        if (this.referenceClass != referenceClass) {
            setIsPrepared(false);
        }
        this.referenceClass = referenceClass;
    }

    /**
     * INTERNAL:
     * Set the class name of the reference class of this query.
     * Used by the Mapping Workbench to avoid classpath dependancies.
     */
    public void setReferenceClassName(String className) {
        referenceClassName = className;
    }
    
    /**
     * PUBLIC:
     * Set a flag indicating whether execution should be deferred in UOW until commit.
     */
    public void setShouldDeferExecutionInUOW(boolean shouldDeferExecutionInUOW) {
        this.shouldDeferExecutionInUOW = shouldDeferExecutionInUOW;
    }
    
    /**
     * PUBLIC:
     * Indicates whether execution should be deferred in UOW until commit.
     */
    public boolean shouldDeferExecutionInUOW() {
        return shouldDeferExecutionInUOW;
    }
    
    /**
     * INTERNAL:
     */
    protected boolean shouldInvalidateCache() {
        return m_cacheUsage == INVALIDATE_CACHE;
    }

    /**
     * INTERNAL:
     * Initialize the expression builder which should be used for this query. If
     * there is a where clause, use its expression builder, otherwise
     * generate one and cache it. This helps avoid unnecessary rebuilds.
     */
    protected void initializeDefaultBuilder() {
        initializeQuerySpecificDefaultBuilder();
        if(defaultBuilder == null) {
            defaultBuilder = new ExpressionBuilder();
        }
    }
    
    /**
     * INTERNAL:
     * Initialize the expression builder which should be used for this query. If
     * there is a where clause, use its expression builder.
     * If after this method defaultBuilder is still null,
     * then initializeDefaultBuilder method will generate and cache it.
     */
    protected void initializeQuerySpecificDefaultBuilder() {
        DatabaseQueryMechanism mech = getQueryMechanism();
        if (mech.isExpressionQueryMechanism() && ((ExpressionQueryMechanism)mech).getExpressionBuilder() != null) {
            this.defaultBuilder = ((ExpressionQueryMechanism)mech).getExpressionBuilder();
        }
    }
}
