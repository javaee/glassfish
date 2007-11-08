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
package oracle.toplink.essentials.descriptors;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.descriptors.InheritancePolicy;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: The query manager allows for the database opperations that TopLink
 * performs to be customized by the application.  For each descriptor a query can be
 * given that controls how a operation will occur.  A common example is if the application
 * requires a stored procedure to be used to insert the object, it can override the SQL call
 * in the insert query that TopLink will use to insert the object.
 * Queries can be customized to extend TopLink behavior, access non-relational data or use stored
 * procedures or customized SQL calls.
 * <p>
 * The queries that can be customized include:
 * <ul>
 * <li> insertQuery - used to insert the object
 * <li> updateQuery - used to update the object
 * <li> readObjectQuery - used to read a single object by primary key
 * <li> readAllQuery - used to read all of the objects of the class
 * <li> doesExistQuery - used to determine whether an insert or update should occur
 * <li> deleteQuery - used to delete the object
 * </ul>
 *
 * @see ClassDescriptor
 */
public class DescriptorQueryManager implements Cloneable, Serializable {
    protected transient InsertObjectQuery insertQuery;
    protected transient UpdateObjectQuery updateQuery;
    protected transient ReadObjectQuery readObjectQuery;
    protected transient ReadAllQuery readAllQuery;
    protected transient DeleteObjectQuery deleteQuery;
    protected DoesExistQuery doesExistQuery;
    protected ClassDescriptor descriptor;
    protected boolean hasCustomMultipleTableJoinExpression;
    protected transient Expression additionalJoinExpression;
    protected transient Expression multipleTableJoinExpression;
    protected transient Map queries;
    protected transient Map tablesJoinExpressions;

    /**
     * INTERNAL:
     * Initialize the state of the descriptor query manager
     */
    public DescriptorQueryManager() {
        this.queries = new HashMap(5);
        setDoesExistQuery(new DoesExistQuery());// Always has a does exist.
    }

    /**
     * PUBLIC:
     * Add the query to the descriptor queries with the given name
     * @param name This is the name of the query.  It will be set on the query and used to look it up.
     * @param query This is the query that will be added.  If the query being added has parameters, the
     * existing list of queries will be checked for matching queries.  If a matching query exists,
     * it will be replaced.
     */
    public void addQuery(String name, DatabaseQuery query) {
        query.setName(name);
        addQuery(query);
    }

    /**
     * PUBLIC:
     * Add the query to the session queries
     * @param query DatabaseQuery This is the query that will be added.  If the query being added has parameters, the
     * existing list of queries will be checked for matching queries.  If a matching query exists,
     * it will be replaced.
     */
    public synchronized void addQuery(DatabaseQuery query) {
        if (query instanceof ObjectLevelReadQuery && (((ObjectLevelReadQuery)query).getReferenceClassName() == null)) {
            ((ObjectLevelReadQuery)query).setReferenceClassName(getDescriptor().getJavaClassName());

            // try to set the reference ClassNotFoundException since it should only happen on the MW in which
            // case we will lazily initialize the reference class at a later point.
            try {
                ((ObjectLevelReadQuery)query).setReferenceClass(getDescriptor().getJavaClass());
            } catch (ConversionException exception) {
            }

            //this is an optimization
            query.setDescriptor(getDescriptor());
        }

        // Add query has been synchronized for bug 3355199.
        // Additionally code has been added to ensure that the same query is not added twice.
        Vector queriesByName = (Vector)getQueries().get(query.getName());
        if (queriesByName == null) {
            // lazily create Vector in Hashtable.
            queriesByName = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
            getQueries().put(query.getName(), queriesByName);
        } else {
            int argumentTypesSize = 0;
            if (query.getArguments() != null) {
                argumentTypesSize = query.getArguments().size();
            }
            Vector argumentTypes = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(argumentTypesSize);
            for (int i = 0; i < argumentTypesSize; i++) {
                argumentTypes.addElement(query.getArgumentTypeNames().elementAt(i));
            }

            // Search for a query with the same parameters and replace it if one is found
            for (int i = 0; i < queriesByName.size(); i++) {
                DatabaseQuery currentQuery = (DatabaseQuery)queriesByName.elementAt(i);

                // Here we are checking equality instead of assignability.  If you look at getQuery()
                // it is the other way around.
                // The reason we do this is we are replacing a query and we want to make sure we are
                // replacing the exact same one. - TW
                if (argumentTypes.equals(currentQuery.getArgumentTypeNames())) {
                    queriesByName.remove(i);
                    queriesByName.add(i, query);
                    return;
                }
            }
        }
        queriesByName.add(query);
    }

    /**
     * PUBLIC:
     * Assume that if the objects primary key does not include null then it must exist.
     * This may be used if the application guarantees or does not care about the existence check.
     */
    public void assumeExistenceForDoesExist() {
        getDoesExistQuery().assumeExistenceForDoesExist();
    }

    /**
     * PUBLIC:
     * Assume that the object does not exist. This may be used if the application guarantees or
     * does not care about the existence check.  This will always force an insert to be called.
     */
    public void assumeNonExistenceForDoesExist() {
        getDoesExistQuery().assumeNonExistenceForDoesExist();
    }

    /**
     * PUBLIC:
     * Default behavior.
     * Assume that if the objects primary key does not include null and it
     * is in the cache, then is must exist.
     */
    public void checkCacheForDoesExist() {
        getDoesExistQuery().checkCacheForDoesExist();
    }

    /**
     * PUBLIC:
     * Perform does exist check on the database
     */
    public void checkDatabaseForDoesExist() {
        getDoesExistQuery().checkDatabaseForDoesExist();
    }

    /**
     * INTERNAL:
     * Clone the query manager
     */
    public Object clone() {
        DescriptorQueryManager manager = null;
        try {
            manager = (DescriptorQueryManager)super.clone();
        } catch (Exception exception) {
            ;
        }

        // Bug 3037701 - clone the queries
        manager.setQueries(new Hashtable(getQueries().size()));
        Iterator iterator = queries.values().iterator();
        while (iterator.hasNext()) {
            Iterator queriesForKey = ((Vector)iterator.next()).iterator();
            while (queriesForKey.hasNext()) {
                DatabaseQuery initialQuery = (DatabaseQuery)queriesForKey.next();
                DatabaseQuery clonedQuery = (DatabaseQuery)initialQuery.clone();
                clonedQuery.setDescriptor(manager.getDescriptor());
                manager.addQuery(clonedQuery);
            }
        }
        manager.setDoesExistQuery((DoesExistQuery)getDoesExistQuery().clone());
        if (getReadAllQuery() != null) {
            manager.setReadAllQuery((ReadAllQuery)getReadAllQuery().clone());
        }
        if (getReadObjectQuery() != null) {
            manager.setReadObjectQuery((ReadObjectQuery)getReadObjectQuery().clone());
        }
        if (getUpdateQuery() != null) {
            manager.setUpdateQuery((UpdateObjectQuery)getUpdateQuery().clone());
        }
        if (getInsertQuery() != null) {
            manager.setInsertQuery((InsertObjectQuery)getInsertQuery().clone());
        }
        if (getDeleteQuery() != null) {
            manager.setDeleteQuery((DeleteObjectQuery)getDeleteQuery().clone());
        }

        return manager;
    }

    /**
     * PUBLIC:
     * Return true if the query is defined on the session
     */
    public boolean containsQuery(String queryName) {
        return queries.containsKey(queryName);
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this Query Manager to actual class-based
     * settings
     * This method is implemented by subclasses as necessary.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        Iterator queryVectors = getQueries().values().iterator();
        while (queryVectors.hasNext()){
            Iterator queries = ((Vector)queryVectors.next()).iterator();;
            while (queries.hasNext()){
                ((DatabaseQuery)queries.next()).convertClassNamesToClasses(classLoader);
            }
        }
    };

    /**
     * ADVANCED:
     * Returns the join expression that should be appended to all of the descriptors expressions
     * Contains any multiple table or inheritance dependencies
     */
    public Expression getAdditionalJoinExpression() {
        return additionalJoinExpression;
    }

    /**
     * ADVANCED:
     * Return the receiver's delete query.
     * This should be an instance of a valid subclass of DeleteObjectQuery.
     * If specified this is used by the descriptor to delete itself and its private parts from the database.
     * This gives the user the ability to define exactly how to delete the data from the database,
     * or access data external from the database or from some other framework.
     */
    public DeleteObjectQuery getDeleteQuery() {
        return deleteQuery;
    }

    /**
     * ADVANCED:
     * Return the receiver's delete SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the source row,
     * through replacing the field names marked by '#' with the values for those fields.
     * <p>
     *    Example, "delete from EMPLOYEE where EMPLOYEE_ID = #EMPLOYEE_ID".
     */
    public String getDeleteSQLString() {
        if (getDeleteQuery() == null) {
            return null;
        }

        return getDeleteQuery().getSQLString();
    }

    /**
     * INTERNAL:
     * Return the descriptor associated with this descriptor query manager
     */
    protected ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * ADVANCED:
     * Return the receiver's  does exist query.
     * This should be an instance of a valid subclass of DoesExistQuery.
     * If specified this is used by the descriptor to query existence of an object in the database.
     * This gives the user the ability to define exactly how to query existence from the database,
     * or access data external from the database or from some other framework.
     */
    public DoesExistQuery getDoesExistQuery() {
        return doesExistQuery;
    }

    /**

     * ADVANCED:
     * Return the receiver's does exist SQL string.
     * This allows the user to override the SQL generated by TopLink, with there own SQL or procedure call.
     * The arguments are translated from the fields of the source row, through replacing the field names marked by '#'
     * with the values for those fields.
     * This must return null if the object does not exist, otherwise return a database row.
     * <p>
     * Example, "select EMPLOYEE_ID from EMPLOYEE where EMPLOYEE_ID = #EMPLOYEE_ID".
     */
    public String getDoesExistSQLString() {
        if (getDoesExistQuery() == null) {
            return null;
        }

        return getDoesExistQuery().getSQLString();
    }

    /**
     * INTERNAL:
     * This method is explicitly used by the Builder only.
     */
    public String getExistenceCheck() {
        if (getDoesExistQuery().shouldAssumeExistenceForDoesExist()) {
            return "Assume existence";
        } else if (getDoesExistQuery().shouldAssumeNonExistenceForDoesExist()) {
            return "Assume non-existence";
        } else if (getDoesExistQuery().shouldCheckCacheForDoesExist()) {
            return "Check cache";
        } else if (getDoesExistQuery().shouldCheckDatabaseForDoesExist()) {
            return "Check database";
        } else {
            // Default.
            return "Check cache";
        }
    }

    /**
      * ADVANCED:
      * Return the receiver's insert query.
      * This should be an instance of a valid subclass of InsertObjectQuery.
      * If specified this is used by the descriptor to insert itself into the database.
      * If the receiver uses sequence numbers, this query must return the updated sequence value.
      * This gives the user the ability to define exactly how to insert the data into the database,
      * or access data externel from the database or from some other framework.
      */
    public InsertObjectQuery getInsertQuery() {
        return insertQuery;
    }

    /**
     * ADVANCED:
     * Return the receiver's insert SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the source row,
     * through replacing the field names marked by '#' with the values for those fields.
     * <p>
     * Example, "insert into EMPLOYEE (F_NAME, L_NAME) values (#F_NAME, #L_NAME)".
     */
    public String getInsertSQLString() {
        if (getInsertQuery() == null) {
            return null;
        }

        return getInsertQuery().getSQLString();
    }

    /**
     * ADVANCED:
     * This is normally generated for descriptors that have multiple tables.
     * However, if the additional table does not reference the primary tables primary key,
     * this expression may be set directly.
     */
    public Expression getMultipleTableJoinExpression() {
        return multipleTableJoinExpression;
    }

    /**
     * PUBLIC:
     * Return the pre-defined queries for the descriptor. The Hashtable returned
     * contains Vectors of queries.
     *
     * @see #getAllQueries()
     */
    public Map getQueries() {
        return queries;
    }

    /**
     * PUBLIC:
     * Return the pre-defined queries for the descriptor.  The Vector returned
     * contains all queries for this descriptor.
     *
     * @see #getQueries()
     */
    public Vector getAllQueries() {
        Vector allQueries = new Vector();
        for (Iterator vectors = getQueries().values().iterator(); vectors.hasNext();) {
            allQueries.addAll((Vector)vectors.next());
        }
        return allQueries;
    }

    /**
     * INTERNAL:
     * Set pre-defined queries for the descriptor.  Converts the Vector to a hashtable
     */
    public void setAllQueries(Vector vector) {
        for (Enumeration enumtr = vector.elements(); enumtr.hasMoreElements();) {
            addQuery((DatabaseQuery)enumtr.nextElement());
        }
    }

    /**
     * PUBLIC:
     * set the pre-defined queries for the descriptor.  Used to write out deployment XML
     */
    public void setQueries(Map hashtable) {
        queries = hashtable;
    }

    /**
     * PUBLIC:
     * Return the query name from the set of pre-defined queries
     * If only one query exists with this name, it will be returned.
     * If there are multiple queries of this name, this method will search for a query
     * with no arguments and return the first one it finds.
     *
     * @see #getQuery(String, Vector)
     */
    public DatabaseQuery getQuery(String queryName) {
        return getQuery(queryName, null);
    }

    /**
     * PUBLIC:
     * Return the query from the set of pre-defined queries with the given name and argument types.
     * This allows for common queries to be pre-defined, reused and executed by name.
     * This method should be used if the Session has multiple queries with the same name but
     * different arguments.
     * If only one query exists, it will be returned regardless of the arguments.
     * If multiple queries exist, the first query that has corresponding argument types will be returned
     *
     * @see #getQuery(String)
     */
    public DatabaseQuery getQuery(String name, Vector arguments) {
        DatabaseQuery query = getLocalQuery(name, arguments);

        // CR#3711: Check if a query with the same name exists for this descriptor.  
        // If not, recursively check descriptors of parent classes.  If nothing is 
        // found in parents, return null.
        if (query == null) {
            DatabaseQuery parentQuery =  getQueryFromParent(name, arguments);
            if (parentQuery != null && parentQuery.isReadQuery()) {
                parentQuery = (DatabaseQuery) parentQuery.clone();
                ((ObjectLevelReadQuery)parentQuery).setReferenceClass(getDescriptor().getJavaClass());
                addQuery(name, parentQuery);
            }
            return parentQuery;
        }
        return query;
    }

    /**
     * INTENAL:
     * Return the query from the set of pre-defined queries with the given name and argument types.
     * This allows for common queries to be pre-defined, reused and executed by name.
     * Only returns those queries locally defined, not superclass's queries
     * If only one query exists, it will be returned regardless of the arguments.
     * If multiple queries exist, the first query that has corresponding argument types will be returned
     *
     * @see #getQuery(String)
     */
    public DatabaseQuery getLocalQuery(String name, Vector arguments) {
        Vector queries = (Vector)getQueries().get(name);

        if (queries == null){
            return null;
        }
        
        // Short circuit the simple, most common case of only one query.
        if (queries.size() == 1) {
            return (DatabaseQuery)queries.firstElement();
        }

        // CR#3754; Predrag; mar 19/2002;
        // We allow multiple named queries with the same name but
        // different argument set; we can have only one query with
        // no arguments; Vector queries is not sorted;
        // When asked for the query with no parameters the
        // old version did return the first query - wrong: 
        // return (DatabaseQuery) queries.firstElement();
       int argumentTypesSize = 0;
        if (arguments != null) {
            argumentTypesSize = arguments.size();
        }
        Vector argumentTypes = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(argumentTypesSize);
        for (int i = 0; i < argumentTypesSize; i++) {
            argumentTypes.addElement(arguments.elementAt(i).getClass());
        }
        for (Enumeration queriesEnum = queries.elements(); queriesEnum.hasMoreElements();) {
            DatabaseQuery query = (DatabaseQuery)queriesEnum.nextElement();

            // BUG#2698755
            // This check was backward, we default the type to Object
            // Was checking Object is decendent of String not other way.
            if (Helper.areTypesAssignable(query.getArgumentTypes(), argumentTypes)) {
                return query;
            }
        }

        return null;

    }

    /**
     * INTERNAL:
     * CR#3711: Check if the class for this descriptor has a parent class.
     * Then search this parent's descriptor for a query with the same name
     * and arguments.  If nothing found, return null.
     *
     * This method should only be used recursively by getQuery().
     */
    protected DatabaseQuery getQueryFromParent(String name, Vector arguments) {
        ClassDescriptor descriptor = getDescriptor();
        if (descriptor.hasInheritance()) {
            InheritancePolicy inheritancePolicy = descriptor.getInheritancePolicy();
            ClassDescriptor parent = inheritancePolicy.getParentDescriptor();

            // if parent exists, check for the query
            if (parent != null) {
                return parent.getQueryManager().getQuery(name, arguments);
            }
        }
        return null;
    }

    /**
      * ADVANCED:
      * Return the receiver's read query.
      * This should be an instance of a valid subclass of ReadAllQuery.
      */
    public ReadAllQuery getReadAllQuery() {
        return readAllQuery;
    }

    /**
     * ADVANCED:
     * Return the receiver's read SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the read arguments row,
     * through replacing the field names marked by '#' with the values for those fields.
     * Note that this is only used on readAllObjects(Class), and not when an expression is provided.
     * <p>
     * Example, "select * from EMPLOYEE"
     */
    public String getReadAllSQLString() {
        if (getReadAllQuery() == null) {
            return null;
        }

        return getReadAllQuery().getSQLString();
    }

    /**
     * ADVANCED:
     * Return the receiver's read query.
     * This should be an instance of a valid subclass of ReadObjectQuery.
     * If specified this is used by the descriptor to read itself from the database.
     * The read arguments must be the primary key of the object only.
     * This gives the user the ability to define exactly how to read the object from the database,
     * or access data externel from the database or from some other framework.
     */
    public ReadObjectQuery getReadObjectQuery() {
        return readObjectQuery;
    }

    /**
     * ADVANCED:
     * Return the receiver's read SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the read arguments row,
     * through replacing the field names marked by '#' with the values for those fields.
     * This must accept only the primary key of the object as arguments.
     * <p>
     * Example, "select * from EMPLOYEE where EMPLOYEE_ID = #EMPLOYEE_ID"
     */
    public String getReadObjectSQLString() {
        if (getReadObjectQuery() == null) {
            return null;
        }

        return getReadObjectQuery().getSQLString();
    }

    /**
      * ADVANCED:
      * Return the receiver's update query.
      * This should be an instance of a valid subclass of UpdateObjectQuery.
      * If specified this is used by the descriptor to insert itself into the database.
      * If the receiver uses optimisitic locking this must raise an error on optimisitic lock failure.
      * This gives the user the ability to define exactly how to update the data into the database,
      * or access data externel from the database or from some other framework.
      */
    public UpdateObjectQuery getUpdateQuery() {
        return updateQuery;
    }

    /**
     * ADVANCED:
     * Return the receiver's update SQL string.
     * This allows the user to override the SQL generated by TopLink, with there own SQL or procedure call.
     * The arguments are translated from the fields of the source row,
     * through replacing the field names marked by '#' with the values for those fields.
     * This must check the optimistic lock field and raise an error on optimistic lock failure.
     * <p>
     * Example, "update EMPLOYEE set F_NAME to #F_NAME, L_NAME to #L_NAME where EMPLOYEE_ID = #EMPLOYEE_ID".
     */
    public String getUpdateSQLString() {
        if (getUpdateQuery() == null) {
            return null;
        }

        return getUpdateQuery().getSQLString();
    }

    /**
     * INTERNAL:
     * Return if a cutsom join expression is used.
     */
    public boolean hasCustomMultipleTableJoinExpression() {
        return hasCustomMultipleTableJoinExpression;
    }

    /**
     * INTERNAL:
     * Flag that specifies if a delete query is available
     */
    public boolean hasDeleteQuery() {
        return (deleteQuery != null);
    }

    /**
     * INTERNAL:
     * Flag that specifies if a does exist query is available
     */
    public boolean hasDoesExistQuery() {
        return (doesExistQuery != null);
    }

    /**
     * INTERNAL:
     * Flag that specifies if a insert query is available
     */
    public boolean hasInsertQuery() {
        return (insertQuery != null);
    }

    /**
     * INTERNAL:
     * Flag that specifies if a read all query is available
     */
    public boolean hasReadAllQuery() {
        return (readAllQuery != null);
    }

    /**
     * INTERNAL:
     * Flag that specifies if a read object  query is available
     */
    public boolean hasReadObjectQuery() {
        return (readObjectQuery != null);
    }

    /**
     * INTERNAL:
     * Flag that specifies if a update query is available
     */
    public boolean hasUpdateQuery() {
        return (updateQuery != null);
    }

    /**
     * INTERNAL:
     * populate the queries with the descriptor.
     */
    private void populateQueries() {

        /* CR2260
         * Descriptiron:
         *   NullPointerException accessing null descriptor
         * Fix:
         *   Initialize queries with an instantiated descriptor at this point
         */
        if (getInsertQuery() != null) {
            getInsertQuery().setDescriptor(descriptor);
        }
        if (getUpdateQuery() != null) {
            getUpdateQuery().setDescriptor(descriptor);
        }
        if (getReadObjectQuery() != null) {
            getReadObjectQuery().setReferenceClass(getDescriptor().getJavaClass());
            getReadObjectQuery().setDescriptor(descriptor);
        }
        if (getDeleteQuery() != null) {
            getDeleteQuery().setDescriptor(descriptor);
        }
        if (getReadAllQuery() != null) {
            getReadAllQuery().setReferenceClass(getDescriptor().getJavaClass());
            getReadAllQuery().setDescriptor(descriptor);
        }
        for (Iterator it = getAllQueries().iterator(); it.hasNext();) {
            ((DatabaseQuery)it.next()).setDescriptor(descriptor);
        }
    }

    /**
     * INTERNAL:
     * Post initialize the mappings
     */
    public void initialize(AbstractSession session) {

        if (getDescriptor().isAggregateDescriptor()) {
            return;
        }

        if (getMultipleTableJoinExpression() != null) {
            // Combine new multiple table expression to additional join expression
            setAdditionalJoinExpression(getMultipleTableJoinExpression().and(getAdditionalJoinExpression()));
        }

        if (getDescriptor().isAggregateCollectionDescriptor()) {
            return;
        }

        //PERF: set read-object query to cache generated SQL.
        if (!hasReadObjectQuery()) {
            // Prepare static read object query always.
            ReadObjectQuery readObjectQuery = new ReadObjectQuery();
            readObjectQuery.setSelectionCriteria(getDescriptor().getObjectBuilder().getPrimaryKeyExpression());
            setReadObjectQuery(readObjectQuery);
        }
        
        if (!hasInsertQuery()) {
            // Prepare insert query always.
            setInsertQuery(new InsertObjectQuery());
        }
        getInsertQuery().setModifyRow(getDescriptor().getObjectBuilder().buildTemplateInsertRow(session));

        if (!hasDeleteQuery()) {
            // Prepare delete query always.
            setDeleteQuery(new DeleteObjectQuery());
        }
        getDeleteQuery().setModifyRow(new DatabaseRecord());

        if (hasUpdateQuery()) {
            // Do not prepare to update by default to allow minimal update.
            getUpdateQuery().setModifyRow(getDescriptor().getObjectBuilder().buildTemplateUpdateRow(session));
        }
    }

    /**
     * INTERNAL:
     * Get the parent DescriptorQueryManager.
     * Caution must be used in using this method as it expects the descriptor
     * to have inheritance.
     * Calling this when the descriptor that does not use inheritance will cause problems, #hasInheritance() must
     * always first be called.
     */
    public DescriptorQueryManager getParentDescriptorQueryManager() {
        return getDescriptor().getInheritancePolicy().getParentDescriptor().getQueryManager();
    }

    /**
     * INTERNAL:
     * Execute the post delete operation for the query
     */
    public void postDelete(WriteObjectQuery query) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).postDelete(query);
        }
    }

    /**
     * INTERNAL:
     * Execute the post insert operation for the query
     */
    public void postInsert(WriteObjectQuery query) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).postInsert(query);
        }
    }

    /**
     * INTERNAL:
     * Execute the post update operation for the query
     */
    public void postUpdate(WriteObjectQuery query) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).postUpdate(query);
        }
    }

    /**
     * INTERNAL:
     * Execute the pre delete operation for the query
     */
    public void preDelete(WriteObjectQuery query) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).preDelete(query);
        }
    }

    /**
     * INTERNAL:
     * Initialize the query manager.
     * Any custom queries must be inherited from the parent before any initialization.
     */
    public void preInitialize(AbstractSession session) {
        if (getDescriptor().isAggregateDescriptor()) {
            return;
        }

        // Must inherit parent query customization if not redefined.
        if (getDescriptor().isChildDescriptor()) {
            DescriptorQueryManager parentQueryManager = getDescriptor().getInheritancePolicy().getParentDescriptor().getQueryManager();

            if ((!hasInsertQuery()) && (parentQueryManager.hasInsertQuery())) {
                setInsertQuery((InsertObjectQuery)parentQueryManager.getInsertQuery().clone());
            }
            if ((!hasUpdateQuery()) && (parentQueryManager.hasUpdateQuery())) {
                setUpdateQuery((UpdateObjectQuery)parentQueryManager.getUpdateQuery().clone());
            }
            if ((!hasDeleteQuery()) && (parentQueryManager.hasDeleteQuery())) {
                setDeleteQuery((DeleteObjectQuery)parentQueryManager.getDeleteQuery().clone());
            }
            if ((!hasReadObjectQuery()) && (parentQueryManager.hasReadObjectQuery())) {
                setReadObjectQuery((ReadObjectQuery)parentQueryManager.getReadObjectQuery().clone());
            }
            if ((!hasReadAllQuery()) && (parentQueryManager.hasReadAllQuery())) {
                setReadAllQuery((ReadAllQuery)parentQueryManager.getReadAllQuery().clone());
            }
            if ((!getDoesExistQuery().isUserDefined()) && getDoesExistQuery().shouldCheckCacheForDoesExist()) {
                setDoesExistQuery(((DoesExistQuery)parentQueryManager.getDoesExistQuery().clone()));
            }
        }
    }

    /**
     * INTERNAL:
     * Execute the pre insert  operation for the query.
     */
    public void preInsert(WriteObjectQuery query) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).preInsert(query);
        }
    }

    /**
     * INTERNAL:
     * Execute the pre update operation for the query
     */
    public void preUpdate(WriteObjectQuery query) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).preUpdate(query);
        }
    }

    /**
     * PUBLIC:
     * Remove all queries with the given query name from the set of pre-defined queries
     *
     * @see #removeQuery(String, Vector)
     */
    public void removeQuery(String queryName) {
        queries.remove(queryName);
    }

    /**
     * PUBLIC:
     * Remove the specific query with the given queryName and argumentTypes.
     *
     * @see #removeQuery(String)
     */
    public void removeQuery(String queryName, Vector argumentTypes) {
        Vector queries = (Vector)getQueries().get(queryName);
        if (queries == null) {
            return;
        } else {
            DatabaseQuery query = null;
            for (Enumeration enumtr = queries.elements(); enumtr.hasMoreElements();) {
                query = (DatabaseQuery)enumtr.nextElement();
                if (Helper.areTypesAssignable(argumentTypes, query.getArgumentTypes())) {
                    break;
                }
            }
            if (query != null) {
                queries.remove(query);
            }
        }
    }

    /**
     * ADVANCED:
     * Set the additional join expression. Used in conjuction with
     * multiple tables and inheritance relationships.
     * This can also be used if a sub-expression is always required to be
     * appended to all queries.  Such as tables that are shared based on a type field
     * without inheritance.
     */
    public void setAdditionalJoinExpression(Expression additionalJoinExpression) {
        this.additionalJoinExpression = additionalJoinExpression;
    }

    /**
     * ADVANCED:
     * Set the receiver's delete query.
     * This should be an instance of a valid subclas of DeleteObjectQuery.
     * If specified this is used by the descriptor to delete itself and its private parts from the database.
     * This gives the user the ability to define exactly how to delete the data from the database,
     * or access data external from the database or from some other framework.
     */
    public void setDeleteQuery(DeleteObjectQuery query) {
        this.deleteQuery = query;
        if (query == null) {
            return;
        }
        this.deleteQuery.setIsUserDefined(true);
        this.deleteQuery.setDescriptor(getDescriptor());

    }

    /**
     * ADVANCED:
     * Set the receiver's delete SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the source row,
     * through replacing the field names marked by '#' with the values for those fields.
     * <p>
     *    Example, "delete from EMPLOYEE where EMPLOYEE_ID = #EMPLOYEE_ID".
     */
    public void setDeleteSQLString(String sqlString) {
        if (sqlString == null) {
            return;
        }

        DeleteObjectQuery query = new DeleteObjectQuery();
        query.setSQLString(sqlString);
        setDeleteQuery(query);
    }

    /**
     * ADVANCED:
     * Set the receiver's delete call.
     * This allows the user to override the delete operation.
     */
    public void setDeleteCall(Call call) {
        if (call == null) {
            return;
        }
        DeleteObjectQuery query = new DeleteObjectQuery();
        query.setCall(call);
        setDeleteQuery(query);
    }

    /**
     * INTERNAL:
     * Set the descriptor.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
        //Gross alert: This is for the case when we are reading from XML, and 
        //we have to compensate for no descriptor available at read time.  - JL
        populateQueries();

    }

    /**
     * ADVANCED:
     * Set the receiver's  does exist query.
     * This should be an instance of a valid subclas of DoesExistQuery.
     * If specified this is used by the descriptor to query existence of an object in the database.
     * This gives the user the ability to define exactly how to query existence from the database,
     * or access data external from the database or from some other framework.
     */
    public void setDoesExistQuery(DoesExistQuery query) {
        this.doesExistQuery = query;
        if (query == null) {
            return;
        }
        this.doesExistQuery.setIsUserDefined(true);
        this.doesExistQuery.setDescriptor(getDescriptor());
    }

    /**
     * ADVANCED:
     * Set the receiver's does exist SQL string.
     * This allows the user to override the SQL generated by TopLink, with there own SQL or procedure call.
     * The arguments are translated from the fields of the source row, through replacing the field names marked by '#'
     * with the values for those fields.
     * This must return null if the object does not exist, otherwise return a database row.
     * <p>
     * Example, "select EMPLOYEE_ID from EMPLOYEE where EMPLOYEE_ID = #EMPLOYEE_ID".
     */
    public void setDoesExistSQLString(String sqlString) {
        if (sqlString == null) {
            return;
        }
        getDoesExistQuery().setSQLString(sqlString);
        getDoesExistQuery().checkDatabaseForDoesExist();
    }

    /**
     * ADVANCED:
     * Set the receiver's does exist call.
     * This allows the user to override the does exist operation.
     */
    public void setDoesExistCall(Call call) {
        if (call == null) {
            return;
        }
        getDoesExistQuery().setCall(call);
    }

    /**
     * INTERNAL:
     * This method is explicitly used by the Builder only.
     */
    public void setExistenceCheck(String token) throws DescriptorException {
        if (token.equals("Check cache")) {
            checkCacheForDoesExist();
        } else if (token.equals("Check database")) {
            checkDatabaseForDoesExist();
        } else if (token.equals("Assume existence")) {
            assumeExistenceForDoesExist();
        } else if (token.equals("Assume non-existence")) {
            assumeNonExistenceForDoesExist();
        } else {
            throw DescriptorException.setExistenceCheckingNotUnderstood(token, getDescriptor());
        }
    }

    /**
     * INTENAL:
     * Set if a cutsom join expression is used.
     */
    protected void setHasCustomMultipleTableJoinExpression(boolean hasCustomMultipleTableJoinExpression) {
        this.hasCustomMultipleTableJoinExpression = hasCustomMultipleTableJoinExpression;
    }

    /**
     * ADVANCED:
     * Set the receiver's insert query.
     * This should be an instance of a valid subclass of InsertObjectQuery.
     * If specified this is used by the descriptor to insert itself into the database.
     * This gives the user the ability to define exactly how to insert the data into the database,
     * or access data external from the database or from some other framework.
     */
    public void setInsertQuery(InsertObjectQuery insertQuery) {
        this.insertQuery = insertQuery;
        if (insertQuery == null) {
            return;
        }
        this.insertQuery.setIsUserDefined(true);
        this.insertQuery.setDescriptor(getDescriptor());
    }

    /**
     * ADVANCED:
     * Set the receiver's insert call.
     * This allows the user to override the insert operation.
     */
    public void setInsertCall(Call call) {
        if (call == null) {
            return;
        }
        InsertObjectQuery query = new InsertObjectQuery();
        query.setCall(call);
        setInsertQuery(query);
    }

    /**
     * ADVANCED:
     * Set the receiver's insert SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the source row,
     * through replacing the field names marked by '#' with the values for those fields.
     * <p>
     * Example, "insert into EMPLOYEE (F_NAME, L_NAME) values (#F_NAME, #L_NAME)".
     */
    public void setInsertSQLString(String sqlString) {
        if (sqlString == null) {
            return;
        }

        InsertObjectQuery query = new InsertObjectQuery();
        query.setSQLString(sqlString);
        setInsertQuery(query);
    }

    /**
     * ADVANCED:
     * Return the receiver's insert call.
     * This allows the user to override the insert operation.
     */
    public Call getInsertCall() {
        if (getInsertQuery() == null) {
            return null;
        }
        return getInsertQuery().getDatasourceCall();
    }

    /**
     * ADVANCED:
     * Return the receiver's update call.
     * This allows the user to override the update operation.
     */
    public Call getUpdateCall() {
        if (getUpdateQuery() == null) {
            return null;
        }
        return getUpdateQuery().getDatasourceCall();
    }

    /**
     * ADVANCED:
     * Return the receiver's delete call.
     * This allows the user to override the delete operation.
     */
    public Call getDeleteCall() {
        if (getDeleteQuery() == null) {
            return null;
        }
        return getDeleteQuery().getDatasourceCall();
    }

    /**
     * ADVANCED:
     * Return the receiver's read-object call.
     * This allows the user to override the read-object operation.
     */
    public Call getReadObjectCall() {
        if (getReadObjectQuery() == null) {
            return null;
        }
        return getReadObjectQuery().getDatasourceCall();
    }

    /**
     * ADVANCED:
     * Return the receiver's read-all call.
     * This allows the user to override the read-all operation.
     */
    public Call getReadAllCall() {
        if (getReadAllQuery() == null) {
            return null;
        }
        return getReadAllQuery().getDatasourceCall();
    }

    /**
     * ADVANCED:
     * Return the receiver's does-exist call.
     * This allows the user to override the does-exist operation.
     */
    public Call getDoesExistCall() {
        if (getDoesExistQuery() == null) {
            return null;
        }
        return getDoesExistQuery().getDatasourceCall();
    }

    /**
     * INTERNAL:
     * Used in case descriptor has additional tables:
     * each additional table mapped to an expression joining it.
     */
    public Map getTablesJoinExpressions() {
        if(tablesJoinExpressions == null) {
            tablesJoinExpressions = new HashMap();
        }
        return tablesJoinExpressions;
    }
    
    /**
     * INTERNAL:
     * Used to set the multiple table join expression that was generated by TopLink as opposed
     * to a custom one supplied by the user.
     * @see #setMultipleTableJoinExpression(Expression)
     */
    public void setInternalMultipleTableJoinExpression(Expression multipleTableJoinExpression) {
        this.multipleTableJoinExpression = multipleTableJoinExpression;
    }

    /**
     * ADVANCED:
     * This is normally generated for descriptors that have multiple tables.
     * However, if the additional table does not reference the primary table's primary key,
     * this expression may be set directly.
     */
    public void setMultipleTableJoinExpression(Expression multipleTableJoinExpression) {
        this.multipleTableJoinExpression = multipleTableJoinExpression;
        setHasCustomMultipleTableJoinExpression(true);
    }

    /**
     * ADVANCED:
     * Set the receiver's read all query.
     * This should be an instance of a valid subclass of ReadAllQuery.
     * If specified this is used by the descriptor to read all instances of its class from the database.
     * This gives the user the ability to define exactly how to read all objects from the database,
     * or access data external from the database or from some other framework.
     * Note that this is only used on readAllObjects(Class), and not when an expression is provided.
     */
    public void setReadAllQuery(ReadAllQuery query) {
        this.readAllQuery = query;
        if (query == null) {
            return;
        }

        this.readAllQuery.setIsUserDefined(true);

        /* CR2260 - Steven Vo
         * Description:
         *  NullPointerException accessing null descriptor
         * Fix:
         *   Setting query's descriptor and reference class when descriptor is not null.
         *   Otherwise, wait until the descriptor is set.See populateQueries() that is
         *   called by setDescriptor()
         */
        if (this.getDescriptor() != null) {
            this.readAllQuery.setDescriptor(getDescriptor());
            this.readAllQuery.setReferenceClassName(getDescriptor().getJavaClassName());
            try {
                readAllQuery.setReferenceClass(getDescriptor().getJavaClass());
            } catch (ConversionException exception) {
            }
        }
    }

    /**
     * ADVANCED:
     * Set the receiver's read SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the read arguments row,
     * through replacing the field names marked by '#' with the values for those fields.
     * Note that this is only used on readAllObjects(Class), and not when an expression is provided.
     * <p>
     * Example, "select * from EMPLOYEE"
     */
    public void setReadAllSQLString(String sqlString) {
        if (sqlString == null) {
            return;
        }

        ReadAllQuery query = new ReadAllQuery();
        query.setSQLString(sqlString);
        setReadAllQuery(query);
    }

    /**
     * ADVANCED:
     * Set the receiver's read all call.
     * This allows the user to override the read all operation.
     * Note that this is only used on readAllObjects(Class), and not when an expression is provided.
     */
    public void setReadAllCall(Call call) {
        if (call == null) {
            return;
        }
        ReadAllQuery query = new ReadAllQuery();
        query.setCall(call);
        setReadAllQuery(query);
    }

    /**
     * ADVANCED:
     * Set the receiver's read query.
     * This should be an instance of a valid subclass of ReadObjectQuery>
     * If specified this is used by the descriptor to read itself from the database.
     * The read arguments must be the primary key of the object only.
     * This gives the user the ability to define exactly how to read the object from the database,
     * or access data external from the database or from some other framework.
     */
    public void setReadObjectQuery(ReadObjectQuery query) {
        this.readObjectQuery = query;
        if (query == null) {
            return;
        }
        this.readObjectQuery.setIsUserDefined(true);

        /* CR2260 - Steven Vo
         * Description:
         *  NullPointerException accessing null descriptor
         * Fix:
         *   Setting query's descriptor and reference class when descriptor is not null.
         *   Otherwise, wait until the descriptor is set.See populateQueries() that is
         *   called by setDescriptor()
         */
        if (this.getDescriptor() != null) {
            this.readObjectQuery.setDescriptor(getDescriptor());
            this.readObjectQuery.setReferenceClassName(getDescriptor().getJavaClassName());
            try {
                readObjectQuery.setReferenceClass(getDescriptor().getJavaClass());
            } catch (ConversionException exception) {
            }
        }
    }

    /**
     * ADVANCED:
     * Set the receiver's read SQL string.
     * This allows the user to override the SQL generated by TopLink, with their own SQL or procedure call.
     * The arguments are translated from the fields of the read arguments row,
     * through replacing the field names marked by '#' with the values for those fields.
     * This must accept only the primary key of the object as arguments.
     * <p>
     * Example, "select * from EMPLOYEE where EMPLOYEE_ID = #EMPLOYEE_ID"
     */
    public void setReadObjectSQLString(String sqlString) {
        if (sqlString == null) {
            return;
        }

        ReadObjectQuery query = new ReadObjectQuery();
        query.setSQLString(sqlString);
        setReadObjectQuery(query);
    }

    /**
     * ADVANCED:
     * Set the receiver's read object call.
     * This allows the user to override the read object operation.
     * This must accept only the primary key of the object as arguments.
     */
    public void setReadObjectCall(Call call) {
        if (call == null) {
            return;
        }
        ReadObjectQuery query = new ReadObjectQuery();
        query.setCall(call);
        setReadObjectQuery(query);
    }

    /**
     * ADVANCED:
     * Set the receiver's update query.
     * This should be an instance of a valid subclass of UpdateObjectQuery.
     * If specified this is used by the descriptor to update itself in the database.
     * If the receiver uses optimisitic locking this must raise an error on optimisitic lock failure.
     * This gives the user the ability to define exactly how to update the data into the database,
     * or access data external from the database or from some other framework.
     */
    public void setUpdateQuery(UpdateObjectQuery updateQuery) {
        this.updateQuery = updateQuery;
        if (updateQuery == null) {
            return;
        }
        this.updateQuery.setIsUserDefined(true);
        this.updateQuery.setDescriptor(getDescriptor());
    }

    /**
     * ADVANCED:
     * Set the receiver's update SQL string.
     * This allows the user to override the SQL generated by TopLink, with there own SQL or procedure call.
     * The arguments are translated from the fields of the source row,
     * through replacing the field names marked by '#' with the values for those fields.
     * This must check the optimistic lock field and raise an error on optimistic lock failure.
     * <p>
     * Example, "update EMPLOYEE set F_NAME to #F_NAME, L_NAME to #L_NAME where EMPLOYEE_ID = #EMPLOYEE_ID".
     */
    public void setUpdateSQLString(String sqlString) {
        if (sqlString == null) {
            return;
        }

        UpdateObjectQuery query = new UpdateObjectQuery();
        query.setSQLString(sqlString);
        setUpdateQuery(query);
    }

    /**
     * ADVANCED:
     * Set the receiver's update call.
     * This allows the user to override the update operation.
     */
    public void setUpdateCall(Call call) {
        if (call == null) {
            return;
        }
        UpdateObjectQuery query = new UpdateObjectQuery();
        query.setCall(call);
        setUpdateQuery(query);
    }
}
