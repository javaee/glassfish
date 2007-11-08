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
package oracle.toplink.essentials.internal.ejb.cmp3.base;

import java.util.*;
import oracle.toplink.essentials.ejb.cmp3.EJBQuery;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.ClassConstants;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.internal.helper.BasicTypeHelperImpl;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.internal.parsing.EJBQLParseTree;
import oracle.toplink.essentials.internal.parsing.ejbql.*;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.sessions.DatabaseRecord;

/**
* Concrete EJB 3.0 query class
*
* To do:
* Internationalize exceptions
* Change TopLink exceptions to exception types used by the spec
* Named Parameters
* Report Query
* firstResultIndex set in query framework
* temporal type parameters
* Change single result to use ReadObjectQuery
**/
public abstract class EJBQueryImpl {
    protected DatabaseQuery databaseQuery = null;
    protected EntityManagerImpl entityManager = null;
    protected String queryName = null;
    protected Map parameters = null;
    protected int firstResultIndex = -1; // -1 indicates undefined
    protected int maxResults = -1; // -1 indicates undefined
    protected int maxRows = -1; // -1 indicates undefined

    abstract protected void throwNoResultException(String message);    
    abstract protected void throwNonUniqueResultException(String message);    

    /**
     * Base constructor for EJBQueryImpl.  Initializes basic variables.
     */
    protected EJBQueryImpl(EntityManagerImpl entityManager) {
        parameters = new HashMap();
        this.entityManager = entityManager;
    }

    /**
     * Create an EJBQueryImpl with a TopLink query.
     * @param query
     */
    public EJBQueryImpl(DatabaseQuery query, EntityManagerImpl entityManager) {
        this(entityManager);
        this.databaseQuery = query;
    }

    /**
     * Build an EJBQueryImpl based on the given ejbql string
     * @param ejbql
     * @param entityManager
     */
    public EJBQueryImpl(String ejbql, EntityManagerImpl entityManager) {
        this(ejbql, entityManager, false);
    }

    /**
     * Create an EJBQueryImpl with either a query name or an ejbql string
     * @param queryDescription
     * @param entityManager
     * @param isNamedQuery determines whether to treat the query description as ejbql or a query name
     */
    public EJBQueryImpl(String queryDescription, EntityManagerImpl entityManager, boolean isNamedQuery) {
        this(entityManager);
        if (isNamedQuery) {
            this.queryName = queryDescription;
        } else {
            if (databaseQuery == null) {
                databaseQuery = buildEJBQLDatabaseQuery(queryDescription, getActiveSession());
            }
        }
    }

    /**
     * Internal method to change the wrapped query to a DataModifyQuery if neccessary
     */
    protected void setAsSQLModifyQuery(){
        if (getDatabaseQuery().isDataReadQuery()){
            DataModifyQuery query = new DataModifyQuery();
            query.setSQLString(databaseQuery.getSQLString());
            query.setIsUserDefined(databaseQuery.isUserDefined());
            query.setFlushOnExecute(databaseQuery.getFlushOnExecute());
            databaseQuery = query;
        }
    }

    /**
     * Internal method to change the wrapped query to a DataReadQuery if neccessary
     */
    protected void setAsSQLReadQuery(){
        if(getDatabaseQuery().isDataModifyQuery()){
            DataReadQuery query = new DataReadQuery();
            query.setUseAbstractRecord(false);
            query.setSQLString(databaseQuery.getSQLString());
            query.setIsUserDefined(databaseQuery.isUserDefined());
            query.setFlushOnExecute(databaseQuery.getFlushOnExecute());
            databaseQuery = query;
        }
    }

    /**
     * Build a DatabaseQuery from an EJBQL string.
     * @param ejbql
     * @param session the session to get the descriptors for this query for.
     * @return a DatabaseQuery representing the given ejbql
     */
    public static DatabaseQuery buildEJBQLDatabaseQuery(String ejbql, Session session) {
        return buildEJBQLDatabaseQuery(ejbql, null, session);
    }
    
    /**
     * Build a DatabaseQuery from an EJBQL string.
     * 
     * @param ejbql
     * @param session the session to get the descriptors for this query for.
     * @param hints a list of hints to be applied to the query
     * @return a DatabaseQuery representing the given ejbql
     */
    public static DatabaseQuery buildEJBQLDatabaseQuery(String ejbql, Session session, HashMap hints) {
        return buildEJBQLDatabaseQuery(null, ejbql, null, session, hints, null);
    }
    
    /**
     * Build a DatabaseQuery from an EJBQL string.
     * 
     * @param ejbql
     * @param session the session to get the descriptors for this query for.
     * @param hints a list of hints to be applied to the query
     * @param classLoader the class loader to build the query with
     * @return a DatabaseQuery representing the given ejbql
     */
    public static DatabaseQuery buildEJBQLDatabaseQuery(String ejbql, Session session, HashMap hints, ClassLoader classLoader) {
        return buildEJBQLDatabaseQuery(null, ejbql, null, session, hints, classLoader);
    }
    
    /**
     * Build a DatabaseQuery from an EJBQL string.
     * @param ejbql
     * @parem flushOnExecute
     * @param session the session to get the descriptors for this query for.
     * @return a DatabaseQuery representing the given ejbql
     */
    public static DatabaseQuery buildEJBQLDatabaseQuery(String ejbql,  Boolean flushOnExecute, Session session) {
        return buildEJBQLDatabaseQuery(null, ejbql, flushOnExecute, session, null, null);
    }
    
    /**
     * Build a DatabaseQuery from an EJBQL string.
     * @param ejbql
     * @parem flushOnExecute
     * @param session the session to get the descriptors for this query for.
     * @return a DatabaseQuery representing the given ejbql
     */
    public static DatabaseQuery buildEJBQLDatabaseQuery(String ejbql,  Boolean flushOnExecute, Session session, ClassLoader classLoader) {
        return buildEJBQLDatabaseQuery(null, ejbql, flushOnExecute, session, null, classLoader);
    }
    
    /**
     * Build a DatabaseQuery from an EJBQL string.
     * 
     * @param ejbql
     * @parem flushOnExecute
     * @param session the session to get the descriptors for this query for.
     * @param hints a list of hints to be applied to the query
     * @return a DatabaseQuery representing the given ejbql
     */
    public static DatabaseQuery buildEJBQLDatabaseQuery(String queryName, String ejbql, 
            Boolean flushOnExecute, Session session, HashMap hints, ClassLoader classLoader) {
        DatabaseQuery databaseQuery = null;
        EJBQLParseTree parseTree = EJBQLParser.buildParseTree(queryName, ejbql);
        parseTree.setClassLoader(classLoader);
        databaseQuery = parseTree.createDatabaseQuery();
        databaseQuery.setEJBQLString(ejbql);
        parseTree.populateQuery(databaseQuery, (oracle.toplink.essentials.internal.sessions.AbstractSession)session);
        //Bug#4646580  Add arguments to query
        parseTree.addParametersToQuery(databaseQuery);
        ((EJBQLCallQueryMechanism)databaseQuery.getQueryMechanism()).getEJBQLCall().setIsParsed(true);
        databaseQuery.setFlushOnExecute(flushOnExecute);

        //GF#1324 toplink.refresh query hint does not cascade
        //cascade by mapping as default for read query
        if(databaseQuery.isReadQuery ()) {
            databaseQuery.cascadeByMapping();
        }
        
        // apply any query hints
        applyHints(hints, databaseQuery);
        
        return databaseQuery;
    }
    
    /**
     * Build a ReadAllQuery from a class and sql string.
     * 
     * @param resultClass 
     * @param flushOnExecute 
     * @param sqlString 
     * @return 
     */
    public static DatabaseQuery buildSQLDatabaseQuery(Class resultClass, String sqlString) {
        return buildSQLDatabaseQuery(resultClass, sqlString, null);
    }
    
    /**
     * Build a ReadAllQuery from a class and sql string.
     * 
     * @param resultClass 
     * @param flushOnExecute 
     * @param sqlString 
     * @param hints a list of hints to be applied to the query
     * @return 
     */
    public static DatabaseQuery buildSQLDatabaseQuery(Class resultClass, String sqlString, HashMap hints) {
        ReadAllQuery query = new ReadAllQuery(resultClass);
        query.setSQLString(sqlString);
        query.setIsUserDefined(true);
        
        // apply any query hints
        applyHints(hints, query);

        return query;
    }
    /**
     * Build a ResultSetMappingQuery from a sql result set mapping name and sql string.
     * 
     * @param sqlResultSetMappingName 
     * @param flushOnExecute 
     * @param sqlString 
     * @return 
     */
    public static DatabaseQuery buildSQLDatabaseQuery(String sqlResultSetMappingName, String sqlString) {
        return buildSQLDatabaseQuery(sqlResultSetMappingName, sqlString, null);
    }
    
    /**
     * Build a ResultSetMappingQuery from a sql result set mapping name and sql string.
     * 
     * @param sqlResultSetMappingName 
     * @param flushOnExecute 
     * @param sqlString 
     * @param hints a list of hints to be applied to the query
     * @return 
     */
    public static DatabaseQuery buildSQLDatabaseQuery(String sqlResultSetMappingName, String sqlString, HashMap hints) {
        ResultSetMappingQuery query = new ResultSetMappingQuery();
        query.setSQLResultSetMappingName(sqlResultSetMappingName);
        query.setSQLString(sqlString);
        query.setIsUserDefined(true);
        
        // apply any query hints
        applyHints(hints, query);

        return query;
    }

    /**
     * Build a DataReadQuery from a sql string.
     */
    public static DatabaseQuery buildSQLDatabaseQuery(String sqlString, Boolean flushOnExecute) {
        return buildSQLDatabaseQuery(sqlString, new HashMap());
    }
    
    /**
     * Build a DataReadQuery from a sql string.
     */
    public static DatabaseQuery buildSQLDatabaseQuery(String sqlString, HashMap hints) {
        DataReadQuery query = new DataReadQuery();
        query.setUseAbstractRecord(false);
        query.setSQLString(sqlString);
        query.setIsUserDefined(true);

        // apply any query hints
        applyHints(hints, query);
        
        return query;
    }

    /**
     * Execute a ReadQuery by assigning the stored parameter values and running it
     * in the database
     * @return the results of the query execution
     */
    protected Object executeReadQuery() {
        Vector parameterValues = processParameters();
        //TODO: the following performFlush() call is a temporary workaround for bug 4752493:
        // CTS: INMEMORY QUERYING IN EJBQUERY BROKEN DUE TO CHANGE TO USE REPORTQUERY.
        // Ideally we should only flush in case the selectionExpression can't be conformed in memory.
        // There are two alternative ways to implement that:
        // 1. Try running the query with conformInUOW flag first - if it fails with 
        //    QueryException.cannotConformExpression then flush and run the query again -
        //    now without conforming.
        // 2. Implement a new isComformable method on Expression which would determine whether the expression
        //    could be conformed in memory, flush only in case it returns false.
        //    Note that doesConform method currently implemented on Expression
        //    requires object(s) to be confirmed as parameter(s).
        //    The new isComformable method should not take any objects as parameters,
        //    it should return false if there could be such an object that
        //    passed to doesConform causes it to throw QueryException.cannotConformExpression -
        //    and true otherwise.
        boolean shouldResetConformResultsInUnitOfWork = false;
        if(isFlushModeAUTO()) {
            performPreQueryFlush();
            if(getDatabaseQuery().isObjectLevelReadQuery()) {
                if(((ObjectLevelReadQuery)getDatabaseQuery()).shouldConformResultsInUnitOfWork()) {
                    ((ObjectLevelReadQuery)getDatabaseQuery()).setCacheUsage(ObjectLevelReadQuery.UseDescriptorSetting);
                    shouldResetConformResultsInUnitOfWork = true;
                }
            }
        }
        try {
            // in case it's a user-defined query
            if(getDatabaseQuery().isUserDefined()) {
                // and there is an active transaction
                if(this.entityManager.checkForTransaction(false) != null) {
                    // verify whether uow has begun early transaction
                    if(!((oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl)getActiveSession()).wasTransactionBegunPrematurely()) {
                        // uow begins early transaction in case it hasn't already begun.
                        ((oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl)getActiveSession()).beginEarlyTransaction();
                    }
                }
            }
            return getActiveSession().executeQuery(getDatabaseQuery(), parameterValues);
        } finally {
            if(shouldResetConformResultsInUnitOfWork) {
                ((ObjectLevelReadQuery)getDatabaseQuery()).conformResultsInUnitOfWork();
            }
        }
    }

    /**
    * Execute an update or delete statement.
    * @return the number of entities updated or deleted
    */
    public int executeUpdate() {
        try {
            //bug51411440: need to throw IllegalStateException if query executed on closed em
            entityManager.verifyOpen();
            setAsSQLModifyQuery();
            //bug:4294241, only allow modify queries - UpdateAllQuery prefered
            if ( !(getDatabaseQuery() instanceof ModifyQuery) ){
                throw new IllegalStateException(ExceptionLocalization.buildMessage("incorrect_query_for_execute_update"));
            }
            
            // need to throw TransactionRequiredException if there is no active transaction
            entityManager.checkForTransaction(true);
            
            //fix for bug:4288845, did not add the parameters to the query
            Vector parameterValues = processParameters();
            if(isFlushModeAUTO()) {
                performPreQueryFlush();
            }
            Integer changedRows = (Integer)((Session)getActiveSession()).executeQuery(databaseQuery, parameterValues);
            return changedRows.intValue();
        } catch (RuntimeException e) {
            setRollbackOnly();
            throw e;
        }
    }

    /**
     * Return the cached database query for this EJBQueryImpl.  If the query is
     * a named query and it has not yet been looked up, the query will be looked up
     * and stored as the cached query.
     */
    public DatabaseQuery getDatabaseQuery() {
        if ((queryName != null) && (databaseQuery == null)) {
            // need error checking and appropriate exception for non-existing query
            databaseQuery = getActiveSession().getQuery(queryName);
            if (databaseQuery != null) {
                if (!databaseQuery.isPrepared()){
                    //prepare the query before cloning, this ensures we do not have to continually prepare on each usage
                    databaseQuery.prepareCall(getActiveSession(), new DatabaseRecord());
                }
                //Bug5040609  Make a clone of the original DatabaseQuery for this EJBQuery
                databaseQuery = (DatabaseQuery)databaseQuery.clone();
            } else {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("unable_to_find_named_query", new Object[] {queryName}));
            }
            
        }
        return databaseQuery;
    }

    /**
     * Non-standard method to return results of a ReadQuery that has a containerPoliry
     * that returns objects as a collection rather than a List
     * @return Collection of results
     */
    public Collection getResultCollection() {
        //bug51411440: need to throw IllegalStateException if query executed on closed em
        entityManager.verifyOpen();
        setAsSQLReadQuery();
        propagateResultProperties();
        //bug:4297903, check container policy class and throw exception if its not the right type 
        if (getDatabaseQuery() instanceof ReadAllQuery){
          Class containerClass = ((ReadAllQuery)getDatabaseQuery()).getContainerPolicy().getContainerClass();
          if (! Helper.classImplementsInterface(containerClass, ClassConstants.Collection_Class)){
            throw QueryException.invalidContainerClass( containerClass, ClassConstants.Collection_Class );
          }
        } else if (getDatabaseQuery() instanceof ReadObjectQuery){
            //bug:4300879, no support for ReadObjectQuery if a collection is required
            throw QueryException.incorrectQueryObjectFound( getDatabaseQuery(), ReadAllQuery.class );
        } else if (!(getDatabaseQuery() instanceof ReadQuery)){
            throw new IllegalStateException(ExceptionLocalization.buildMessage("incorrect_query_for_get_result_collection"));
        }
        Object result = executeReadQuery();
        return (Collection)result;
    }

    /**
    * Execute the query and return the query results
    * as a List.
    * @return a list of the results
    */
    public List getResultList() {
        try {
            //bug51411440: need to throw IllegalStateException if query executed on closed em
            entityManager.verifyOpen();
            setAsSQLReadQuery();
            propagateResultProperties();
            //bug:4297903, check container policy class and throw exception if its not the right type 
            if (getDatabaseQuery() instanceof ReadAllQuery){
              Class containerClass = ((ReadAllQuery)getDatabaseQuery()).getContainerPolicy().getContainerClass();
              if (! Helper.classImplementsInterface(containerClass, ClassConstants.List_Class)){
                throw QueryException.invalidContainerClass( containerClass, ClassConstants.List_Class );
              }
            } else if (getDatabaseQuery() instanceof ReadObjectQuery){
                //bug:4300879, handle ReadObjectQuery returning null
                throw QueryException.incorrectQueryObjectFound( getDatabaseQuery(), ReadAllQuery.class );
            } else if (!(getDatabaseQuery() instanceof ReadQuery)){
                throw new IllegalStateException(ExceptionLocalization.buildMessage("incorrect_query_for_get_result_list"));
            }
            Object result = executeReadQuery();
            return (List)result;
        } catch (RuntimeException e) {
            setRollbackOnly();
            throw e;
        }
    }

    /**
    * Execute a query that returns a single result.
    * @return the result
    * @throws EntityNotFoundException if there is no result
    * @throws NonUniqueResultException if more than one result
    */
    public Object getSingleResult() {
        boolean rollbackOnException = true;
        try {
            //bug51411440: need to throw IllegalStateException if query executed on closed em
            entityManager.verifyOpen();
            setAsSQLReadQuery();
            propagateResultProperties();
            //bug:4301674, requires lists to be returned from ReadAllQuery objects
            if (getDatabaseQuery() instanceof ReadAllQuery){
              Class containerClass = ((ReadAllQuery)getDatabaseQuery()).getContainerPolicy().getContainerClass();
              if (! Helper.classImplementsInterface(containerClass, ClassConstants.List_Class)){
                throw QueryException.invalidContainerClass( containerClass, ClassConstants.List_Class );
              }
            } else if (!(getDatabaseQuery() instanceof ReadQuery)){
                throw new IllegalStateException(ExceptionLocalization.buildMessage("incorrect_query_for_get_single_result"));
            }
            Object result = executeReadQuery();
            if (result instanceof List){
                List results = (List)result;
                if (results.isEmpty()) {
                    rollbackOnException = false;
                    throwNoResultException(ExceptionLocalization.buildMessage("no_entities_retrieved_for_get_single_result", (Object[])null));                
                } else if (results.size() > 1) {
                    rollbackOnException = false;
                    throwNonUniqueResultException(ExceptionLocalization.buildMessage("too_many_results_for_get_single_result", (Object[])null));
                }
                return results.get(0);
            }else{
                if (result == null) {
                    rollbackOnException = false;
                    throwNoResultException(ExceptionLocalization.buildMessage("no_entities_retrieved_for_get_single_result", (Object[])null));
                }
                return result;
            }
        } catch (RuntimeException e) {
            if(rollbackOnException) {
                setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Internal method to add the parameters values to the query prior to execution. 
     * Returns a list of parameter values in the order the parameters are
     * defined for the databaseQuery.
     */
    protected Vector processParameters() {
        if (databaseQuery == null) {
            getDatabaseQuery();
        }
        List arguments = databaseQuery.getArguments();
        if (arguments.isEmpty()) {
            Iterator params = parameters.keySet().iterator();
            while (params.hasNext()) {
                databaseQuery.addArgument((String)params.next());
            }
            arguments = databaseQuery.getArguments();
        }
        // now create parameterValues in the same order as the argument list
        Vector parameterValues = new Vector(arguments.size());
        for (Iterator i = arguments.iterator(); i.hasNext();) {
            String name = (String)i.next();
            if (parameters.containsKey(name)) {
                parameterValues.add(parameters.get(name));
            } else {
                // Error: missing actual parameter value
                throw new IllegalStateException(ExceptionLocalization.buildMessage("missing_parameter_value", new Object[]{name}));
            }
        }
        return parameterValues;
    }

    /**
     * Replace the cached query with the given query.
     */
    public void setDatabaseQuery(DatabaseQuery query) {
        databaseQuery = query;
    }

    /**
    * Set the position of the first result to retrieve.
    * @param start position of the first result, numbered from 0
    */
    protected void setFirstResultInternal(int startPosition) {
        if (startPosition < 0) {
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("negative_start_position", (Object[])null));
        }
        firstResultIndex = startPosition;
    }

    /**
     * Set implementation-specific hints.
     * 
     * @param hints a list of hints to be applied to the query
     * @param query the query to apply the hints to
     */
    protected static void applyHints(HashMap hints, DatabaseQuery query) {
        QueryHintsHandler.apply(hints, query);
    }
    

    /**
     * Spec. 3.5.2:
     * "FlushMode.AUTO is set on the Query object, or if
     * the flush mode setting for the persistence context is AUTO (the default) 
     * and a flush mode setting has not been specified for the Query object,
     * the persistence provider is responsible for ensuring that all updates
     * to the state of all entities in the persistence context which could potentially 
     * affect the result of the query are visible to the processing of the query."
     */
    protected boolean isFlushModeAUTO() {
        if(getDatabaseQuery().getFlushOnExecute() != null) {
            return getDatabaseQuery().getFlushOnExecute().booleanValue();
        } else {
            return entityManager.isFlushModeAUTO();
        }
    }
    
    /**
     * Set an implementation-specific hint.
     * If the hint name is not recognized, it is silently ignored.
     * @param hintName
     * @param value
     * @throws IllegalArgumentException if the second argument is not
     * valid for the implementation
     */
    protected void setHintInternal(String hintName, Object value) {
        QueryHintsHandler.apply(hintName, value, getDatabaseQuery());
    }

    /**
    * Set the maximum number of results to retrieve.
    * @param maxResult
    */
    public void setMaxResultsInternal(int maxResult) {
        if (maxResult < 0) {
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("negative_max_result", (Object[])null));
        }
        this.maxResults = maxResult;
    }

    /** */
    protected void propagateResultProperties() {
        DatabaseQuery databaseQuery = getDatabaseQuery();
        if (databaseQuery.isReadQuery()) {
            ReadQuery readQuery = (ReadQuery)databaseQuery;
            if (maxResults >= 0) {
                maxRows = maxResults + ((firstResultIndex >= 0) ? firstResultIndex : 0);
                readQuery.setMaxRows(maxRows);
                maxResults = -1;
            }
            if (firstResultIndex > -1) {
                readQuery.setFirstResult(firstResultIndex);
                firstResultIndex = -1;
            }
        }
    }

    /**
    * Bind an argument to a named parameter.
    * @param name the parameter name
    * @param value
    */
    protected void setParameterInternal(String name, Object value) {
        int index  = getDatabaseQuery().getArguments().indexOf(name);
        if (getDatabaseQuery().getEJBQLString() != null){  //only non native queries
            if (index == -1){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-wrong-argument-name",new Object[]{name, getDatabaseQuery().getEJBQLString()}));
            }
            if (!isValidActualParameter(value, getDatabaseQuery().getArgumentTypes().get(index))) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-incorrect-parameter-type", new Object[] {name, value.getClass(), getDatabaseQuery().getArgumentTypes().get(index), getDatabaseQuery().getEJBQLString()}));
            }
        }
        parameters.put(name, value);
    }

    /**
    * Bind an argument to a positional parameter.
    * @param position
    * @param value
    */
    protected void setParameterInternal(int position, Object value) {
        String pos = (new Integer(position)).toString();
        int index = getDatabaseQuery().getArguments().indexOf(pos);
        if (getDatabaseQuery().getEJBQLString() != null){  //only non native queries
            if (index == -1) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-wrong-argument-index", new Object[]{position, getDatabaseQuery().getEJBQLString()}));
            }
            if (!isValidActualParameter(value, getDatabaseQuery().getArgumentTypes().get(index))) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-incorrect-parameter-type", new Object[] {position, value.getClass(), getDatabaseQuery().getArgumentTypes().get(index), getDatabaseQuery().getEJBQLString()}));
            }
        }
        parameters.put(pos, value);
    }

    protected boolean isValidActualParameter(Object value, Object parameterType) {
        if (value == null) {
            return true;
        } else {
            return BasicTypeHelperImpl.getInstance().isAssignableFrom(parameterType, value.getClass());
        }
    }

    protected Session getActiveSession() {
        return entityManager.getActiveSession();
    }    
    
    protected void performPreQueryFlush(){
        if (this.entityManager.shouldFlushBeforeQuery()){
            this.entityManager.flush();
        }
    }

    protected void setRollbackOnly() {
        entityManager.setRollbackOnly();
    }
}
