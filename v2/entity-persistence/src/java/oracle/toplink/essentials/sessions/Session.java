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
package oracle.toplink.essentials.sessions;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.logging.SessionLogEntry;
import oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.essentials.internal.databaseaccess.Platform;

/**
 * <p>
 * <b>Purpose</b>: Define the TopLink session public interface.
 * <p>
 * <b>Description</b>: This interface is meant to clarify the public protocol into TopLink.
 * It also allows for non-subclasses of Session to conform to the TopLink API.
 * It should be used as the applications main interface into the TopLink API to
 * ensure compatibility between all TopLink sessions.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Define the API for all reading, units of work.
 * </ul>
 * @see UnitOfWork
 * @see DatabaseSession
 * @see oracle.toplink.essentials.publicinterface.Session
 * @see oracle.toplink.essentials.publicinterface.DatabaseSession
 * @see oracle.toplink.essentials.threetier.ServerSession
 * @see oracle.toplink.essentials.threetier.ClientSession
 */
public interface Session {

    /**
     * PUBLIC:
     * Return a unit of work for this session.
     * The unit of work is an object level transaction that allows
     * a group of changes to be applied as a unit.
     * The return value should be used as the oracle.toplink.essentials.sessions.UnitOfWork interface,
     * but must currently be returned as  oracle.toplink.essentials.publicinterface.UnitOfWork to maintain backward
     * compatibility.
     *
     * @see UnitOfWork
     */
    public UnitOfWork acquireUnitOfWork();

    /**
     * PUBLIC:
     * Add the query to the session queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public void addQuery(String name, DatabaseQuery query);
    
    /**
     * ADVANCED:
     * Add a pre-defined not yet parsed EJBQL String/query to the session to be parsed 
     * after descriptors are initialized.
     * @see #getAllQueries()
     */
    public void addEjbqlPlaceHolderQuery(DatabaseQuery query);

    /**
     * PUBLIC:
     * clear the integrityChecker, the integrityChecker holds all the Descriptor Exceptions.
     */
    public void clearIntegrityChecker();

    /**
     * PUBLIC:
     * Clear the profiler, this will end the current profile opperation.
     */
    public void clearProfile();

    /**
     * PUBLIC:
     * Return true if the pre-defined query is defined on the session.
     **/
    public boolean containsQuery(String queryName);

    /**
     * PUBLIC:
     * Return a complete copy of the object.
     * This can be used to obtain a scatch copy of an object,
     * or for templatizing an existing object into another new object.
     * The object and all of its privately owned parts will be copied, the object's primary key will be reset to null.
     *
     * @see #copyObject(Object, ObjectCopyingPolicy)
     */
    public Object copyObject(Object original);

    /**
     * PUBLIC:
     * Return a complete copy of the object.
     * This can be used to obtain a scatch copy of an object,
     * or for templatizing an existing object into another new object.
     * The object copying policy allow for the depth, and reseting of the primary key to null, to be specified.
     */
    public Object copyObject(Object original, ObjectCopyingPolicy policy);

    /**
     * PUBLIC:
     * Return if the object exists on the database or not.
     * This always checks existence on the database.
     */
    public boolean doesObjectExist(Object object) throws DatabaseException;

    /**
     * PUBLIC:
     * Turn off logging
     */
    public void dontLogMessages();

    /**
     * PUBLIC:
     * Execute the call on the database.
     * The row count is returned.
     * The call can be a stored procedure call, SQL call or other type of call.
     * <p>Example:
     * <p>session.executeNonSelectingCall(new SQLCall("Delete from Employee");
     *
     * @see #executeSelectingCall(Call)
     */
    public int executeNonSelectingCall(Call call);

    /**
     * PUBLIC:
     * Execute the non-selecting (update/DML) SQL string.
     */
    public void executeNonSelectingSQL(String sqlString);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.essentials.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.essentials.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.essentials.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.essentials.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2, Object arg3);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.essentials.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Vector argumentValues);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1, Object arg2);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1, Object arg2, Object arg3);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Vector argumentValues);

    /**
     * PUBLIC:
     * Execute the database query.
     * A query is a database operation such as reading or writting.
     * The query allows for the operation to be customized for such things as,
     * performance, depth, caching, etc.
     *
     * @see DatabaseQuery
     */
    public Object executeQuery(DatabaseQuery query) throws TopLinkException;

    /**
     * PUBLIC:
     * Return the results from exeucting the database query.
     * the arguments are passed in as a vector
     */
    public Object executeQuery(DatabaseQuery query, Vector argumentValues);

    /**
     * PUBLIC:
     * Execute the call on the database and return the result.
     * The call must return a value, if no value is return executeNonSelectCall must be used.
     * The call can be a stored procedure call, SQL call or other type of call.
     * A vector of database rows is returned, database row implements Java 2 Map which should be used to access the data.
     * <p>Example:
     * <p>session.executeSelectingCall(new SQLCall("Select * from Employee");
     *
     * @see #executeNonSelectingCall(Call)
     */
    public Vector executeSelectingCall(Call call);

    /**
     * PUBLIC:
     * Execute the selecting SQL string.
     * A Vector of DatabaseRecords are returned.
     */
    public Vector executeSQL(String sqlString);

    /**
     * PUBLIC:
     * Return the active session for the current active external (JTS) transaction.
     * This should only be used with JTS and will return the session if no external transaction exists.
     */
    public Session getActiveSession();

    /**
     * PUBLIC:
     * Return the active unit of work for the current active external (JTS) transaction.
     * This should only be used with JTS and will return null if no external transaction exists.
     */
    public UnitOfWork getActiveUnitOfWork();

    /**
     * ADVANCED:
     * Return the descriptor specified for the class.
     * If the class does not have a descriptor but implements an interface that is also implemented
     * by one of the classes stored in the hashtable, that descriptor will be stored under the
     * new class.
     */
    public ClassDescriptor getClassDescriptor(Class theClass);

    /**
     * ADVANCED:
     * Return the descriptor specified for the object's class.
     */
    public ClassDescriptor getClassDescriptor(Object domainObject);

    /**
     * PUBLIC:
     * Return the descriptor for the alias.
     */
    public ClassDescriptor getClassDescriptorForAlias(String alias);

    /**
     * ADVANCED:
     * Return the descriptor specified for the object's class.
     */
    public ClassDescriptor getDescriptor(Class theClass);

    /**
     * ADVANCED:
     * Return the descriptor specified for the object's class.
     */
    public ClassDescriptor getDescriptor(Object domainObject);

    /**
     * PUBLIC:
     * Return the descriptor for the alias.
     */
    public ClassDescriptor getDescriptorForAlias(String alias);

    /**
     * ADVANCED:
     * Return all registered descriptors.
     */
    public Map getDescriptors();

    /**
     * ADVANCED:
     * Return all pre-defined not yet parsed EJBQL queries.
     * @see #getAllQueries()
     */
    public List getEjbqlPlaceHolderQueries();
    
    /**
     * PUBLIC:
     * Return the event manager.
     * The event manager can be used to register for various session events.
     */
    public SessionEventManager getEventManager();

    /**
     * PUBLIC:
     * Return the ExceptionHandler.Exception handler can catch errors that occur on queries or during database access.
     */
    public ExceptionHandler getExceptionHandler();

    /**
     * PUBLIC:
     * Used for JTS integration.  If your application requires to have JTS control transactions instead of TopLink an
     * external transaction controler must be specified.  TopLink provides JTS controlers for JTS 1.0 and Weblogic's JTS.
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     */
    public ExternalTransactionController getExternalTransactionController();

    /**
     * PUBLIC:
     * The IdentityMapAccessor is the preferred way of accessing IdentityMap funcitons
     * This will return an object which implements an interface which exposes all public
     * IdentityMap functions.
     */
    public IdentityMapAccessor getIdentityMapAccessor();

    /**
     * PUBLIC:
     * Returns the integrityChecker,the integrityChecker holds all the Descriptor Exceptions.
     */
    public IntegrityChecker getIntegrityChecker();

    /**
     * PUBLIC:
     * Return the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference defaults to a writer on System.out.
     */
    public Writer getLog();

    /**
     * PUBLIC:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavoir.
     * NOTE: this must only be used for relational specific usage,
     * it will fail for non-relational datasources.
     */
    public DatabasePlatform getPlatform();

    /**
     * PUBLIC:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavoir.
     */
    public Platform getDatasourcePlatform();
        
    /**
     * PUBLIC:
     * Return the login, the login holds any database connection information given.
     * This has been replaced by getDatasourceLogin to make use of the Login interface
     * to support non-relational datasources,
     * if DatabaseLogin API is required it will need to be cast.
     */
    public DatabaseLogin getLogin();

    /**
     * PUBLIC:
     * Return the login, the login holds any database connection information given.
     * This return the Login interface and may need to be cast to the datasource specific implementation.
     */
    public Login getDatasourceLogin();

    /**
     * PUBLIC:
     * Return the name of the session.
     * This is used with the session broker, or to give the session a more meaningful name.
     */
    public String getName();

    /**
     * ADVANCED:
     * Return the sequnce number from the database
     */
    public Number getNextSequenceNumberValue(Class domainClass);

    /**
     * PUBLIC:
     * Return the profiler.
     * The profiler is a tool that can be used to determine performance bottlenecks.
     * The profiler can be queries to print summaries and configure for logging purposes.
     */
    public SessionProfiler getProfiler();

    /**
     * PUBLIC:
     * Return the project.
     * The project includes the login and descriptor and other configuration information.
     */
    public oracle.toplink.essentials.sessions.Project getProject();

    /**
     * ADVANCED:
     * Allow for user defined properties.
     */
    public Map getProperties();

    /**
     * ADVANCED:
     * Returns the user defined property.
     */
    public Object getProperty(String name);

    /**
     * ADVANCED:
     * Return all pre-defined queries.
     */
    public Map getQueries();

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public DatabaseQuery getQuery(String name);

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public DatabaseQuery getQuery(String name, Vector arguments);

    /**
     * PUBLIC:
     * Return the session log to which an accessor logs messages and SQL.
     * If not set, this will default to a session log on a writer on System.out.
     */
    public SessionLog getSessionLog();

    /**
     * PUBLIC:
     * Allow any WARNING level exceptions that occur within TopLink to be logged and handled by the exception handler.
     */
    public Object handleException(RuntimeException exception) throws RuntimeException;

    /**
     * ADVANCED:
     * Return true if a descriptor exists for the given class.
     */
    public boolean hasDescriptor(Class theClass);

    /**
     * PUBLIC:
     * Return if an exception handler is present.
     */
    public boolean hasExceptionHandler();

    /**
     * PUBLIC:
     * Used for JTS integration.  If your application requires to have JTS control transactions instead of TopLink an
     * external transaction controler must be specified.  TopLink provides JTS controlers for JTS 1.0 and Weblogic's JTS.
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     */
    public boolean hasExternalTransactionController();

    /**
     * PUBLIC:
     * Return if this session is a client session.
     */
    public boolean isClientSession();

    /**
     * PUBLIC:
     * Return if this session is connected to the database.
     */
    public boolean isConnected();

    /**
     * PUBLIC:
     * Return if this session is a database session.
     */
    public boolean isDatabaseSession();

    /**
     * PUBLIC:
     * Return if this session is a distributed session.
     */
    public boolean isDistributedSession();

    /**
     * PUBLIC:
     * Return if a profiler is being used.
     */
    public boolean isInProfile();

    /**
     * PUBLIC:
     * Return if this session is a remote session.
     */
    public boolean isRemoteSession();

    /**
     * PUBLIC:
     * Return if this session is a server session.
     */
    public boolean isServerSession();

    /**
     * PUBLIC:
     * Return if this session is a session broker.
     */
    public boolean isSessionBroker();

    /**
     * PUBLIC:
     * Return if this session is a unit of work.
     */
    public boolean isUnitOfWork();

    /**
     * PUBLIC:
     * Return if this session is a remote unit of work.
     */
    public boolean isRemoteUnitOfWork();
    
    /**
     * ADVANCED:
     * Extract and return the primary key from the object.
     */
    public Vector keyFromObject(Object domainObject) throws ValidationException;

    /**
     * PUBLIC:
     * Log the log entry.
     */
    public void log(SessionLogEntry entry);

    /**
     * Log a untranslated message to the TopLink log at FINER level.
     */
    public void logMessage(String message);
    
    /**
     * PUBLIC:
     * <p>
     * Log a message with level and category.
     * </p><p>
     *
     * @param level, the log request level value
     * </p><p>
     * @param message, the string message
     * </p><p>
     * @param category, the string representation of a TopLink category.
     * </p>
     */
    public void log(int level, String category, String message);

    /**
     * PUBLIC:
     * <p>
     * Log a throwable with level and category.
     * </p><p>
     *
     * @param level, the log request level value
     * </p><p>
     * @param category, the string representation of a TopLink category.
     * </p><p>
     * @param throwable, a Throwable
     * </p>
     */
    public void logThrowable(int level, String category, Throwable throwable);

    /**
     * PUBLIC:
     * Read all of the instances of the class from the database.
     * This operation can be customized through using a ReadAllQuery,
     * or through also passing in a selection criteria.
     *
     * @see ReadAllQuery
     * @see #readAllObjects(Class, Expression)
     */
    public Vector readAllObjects(Class domainClass) throws DatabaseException;

    /**
     * PUBLIC:
     * Read all the instances of the class from the database returned through execution the Call string.
     * The Call can be an SQLCall or EJBQLCall.
     *
     * example: session.readAllObjects(Employee.class, new SQLCall("SELECT * FROM EMPLOYEE"));
     * @see SQLCall
     * @see EJBQLCall
     */
    public Vector readAllObjects(Class domainClass, Call aCall) throws DatabaseException;

    /**
     * PUBLIC:
     * Read all of the instances of the class from the database matching the given expression.
     * This operation can be customized through using a ReadAllQuery.
     *
     * @see ReadAllQuery
     */
    public Vector readAllObjects(Class domainClass, Expression selectionCriteria) throws DatabaseException;

    /**
     * PUBLIC:
     * Read the first instance of the class from the database.
     * This operation can be customized through using a ReadObjectQuery,
     * or through also passing in a selection criteria.
     * By default, this method executes a query without selection criteria and
     * consequently it will always result in a database access even if an instance
     * of the specified Class exists in the cache. Executing a query with
     * selection criteria allows you to avoid a database access if the selected
     * instance is in the cache.
     * Because of this, you may whish to consider a readObject method that takes selection criteria, such as: {@link #readObject(Class, Call)}, {@link #readObject(Class, Expression)}, or {@link #readObject(Object)}.
     * @see ReadObjectQuery
     * @see #readAllObjects(Class, Expression)
     */
    public Object readObject(Class domainClass) throws DatabaseException;

    /**
     * PUBLIC:
     * Read the first instance of the class from the database returned through execution the Call string.
     * The Call can be an SQLCall or EJBQLCall.
     *
     * example: session.readObject(Employee.class, new SQLCall("SELECT * FROM EMPLOYEE"));
     * @see SQLCall
     * @see EJBQLCall
     */
    public Object readObject(Class domainClass, Call aCall) throws DatabaseException;

    /**
     * PUBLIC:
     * Read the first instance of the class from the database matching the given expression.
     * This operation can be customized through using a ReadObjectQuery.
     *
     * @see ReadObjectQuery
     */
    public Object readObject(Class domainClass, Expression selectionCriteria) throws DatabaseException;

    /**
     * PUBLIC:
     * Use the example object to consruct a read object query by the objects primary key.
     * This will read the object from the database with the same primary key as the object
     * or null if no object is found.
     */
    public Object readObject(Object object) throws DatabaseException;

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * This can be used to ensure the object is up to date with the database.
     * Caution should be used when using this to make sure the application has no un commited
     * changes to the object.
     */
    public Object refreshObject(Object object);

    /**
     * PUBLIC:
     * Release the session.
     * This does nothing by default, but allows for other sessions such as the ClientSession to do something.
     */
    public void release();

    /**
     * PUBLIC:
     * Remove the user defined property.
     */
    public void removeProperty(String property);

    /**
     * PUBLIC:
     * Remove the query name from the set of pre-defined queries
     */
    public void removeQuery(String queryName);

    /**
     * PUBLIC:
     * Set the exceptionHandler.
     * Exception handler can catch errors that occur on queries or during database access.
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler);

    /**
     * Set the transaction controller, allow integration with JTA.
     */
    public void setExternalTransactionController(ExternalTransactionController externalTransactionController);

    /**
     * PUBLIC:
     * Set the integrityChecker, the integrityChecker holds all the Descriptor Exceptions.
     */
    public void setIntegrityChecker(IntegrityChecker integrityChecker);

    /**
     * PUBLIC:
     * Set the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference defaults to a writer on System.out.
     */
    public void setLog(Writer log);

    /**
     * PUBLIC:
     * Set the name of the session.
     * This is used with the session broker, or to give the session a more meaningful name.
     */
    public void setName(String name);

    /**
     * PUBLIC:
     * Set the profiler for the session.
     * This allows for performance operations to be profiled.
     */
    public void setProfiler(SessionProfiler profiler);

    /**
     * PUBLIC:
     * Allow for user defined properties.
     */
    public void setProperty(String propertyName, Object propertyValue);

    /**
     * PUBLIC:
     * Set the session log to which an accessor logs messages and SQL.
     * If not set, this will default to a session log on a writer on System.out.
     * To enable logging, logMessages must be turned on.
     */
    public void setSessionLog(SessionLog sessionLog);

    /**
     * PUBLIC:
     * Return if logging is enabled (false if log level is OFF)
     */
    public boolean shouldLogMessages();

    /**
     * PUBLIC:
     * Return the log level
     */
    public int getLogLevel(String category);

    /**
     * PUBLIC:
     * Return the log level
     */
    public int getLogLevel();

    /**
     * PUBLIC:
     * Set the log level
     */
    public void setLogLevel(int level);

    /**
     * PUBLIC:
     * Check if a message of the given level would actually be logged.
     */
    public boolean shouldLog(int Level, String category);

    /**
     * PUBLIC:
     * Allow any SEVERE level exceptions that occur within TopLink to be logged and handled by the exception handler.
     */
    public Object handleSevere(RuntimeException exception) throws RuntimeException;
}
