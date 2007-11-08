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
package oracle.toplink.essentials.internal.databaseaccess;

import java.util.*;
import java.sql.*;
import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.sessions.DatabaseLogin;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL:
 *    DatabaseAccessor is private to TopLink. It encapsulates low level database operations (such as executing
 *    SQL and reading data by row). Database accessor defines a protocol by which TopLink may invoke these
 *    operations. <p>
 *    DatabaseAccessor also defines a single reference through which all configuration dependent behaviour may
 *    be invoked. <p>
 *
 *    DabaseAccessor implements the following behavior. <ul>
 *    <li> Connect and disconnect from the database.
 *    <li> Execute SQL statements on the database, returning results.
 *    <li> Handle auto-commit and transactions.
 *    </ul>
 *    DatabaseAccessor dispatches the following protocols to its platform reference. <ul>
 *    <li> Provision of database platform specific type names.
 *    </ul>
 *    DatabaseAccessor dispatches the following protocols to the schema object. <ul>
 *    <li> Creation and deletion of schema objects.
 *    </ul>
 *    @see DatabasePlatform
 *    @since TOPLink/Java 1.0
 */
public class DatabaseAccessor extends DatasourceAccessor {

    /** PERF: Backdoor to disabling dynamic statements. Reverts to old prepared statement usage if set. */
    public static boolean shouldUseDynamicStatements = true;

    /** Stores statement handles for common used prepared statements. */
    protected Hashtable statementCache;

    /** Cache of the connection's java.sql.DatabaseMetaData */
    protected DatabaseMetaData metaData;

    // Bug 2804663 - Each DatabaseAccessor holds on to its own LOBValueWriter instance
    protected LOBValueWriter lobWriter;

    /**
     * PERF: Option to allow concurrent thread processing of result sets.
     * This allows the objects to be built while the rows are being fetched.
     */
    protected boolean shouldUseThreadCursors;

    /** PERF: Cache the statement object for dynamic SQL execution. */
    protected Statement dynamicStatement;
    protected boolean isDynamicStatementInUse;

    public DatabaseAccessor() {
        super();
        this.statementCache = new Hashtable(50);
        this.lobWriter = null;
        this.shouldUseThreadCursors = false;
        this.isDynamicStatementInUse = false;
    }

    /**
     * Execute any deferred select calls stored in the LOBValueWriter instance.
     * This method will typically be called by the CallQueryMechanism object.
     * Bug 2804663.
     *
     * @see oracle.toplink.essentials.internal.helper.LOBValueWriter
     * @see oracle.toplink.essentials.internal.queryframework.CallQueryMechanism#insertObject()
     */
    public void flushSelectCalls(AbstractSession session) {
        if (lobWriter != null) {
            lobWriter.buildAndExecuteSelectCalls(session);
        }
    }

    /**
     * Return the LOBValueWriter instance.  Lazily initialize the instance.
     * Bug 2804663.
     *
     * @see oracle.toplink.essentials.internal.helper.LOBValueWriter
     */
    public LOBValueWriter getLOBWriter() {
        if (lobWriter == null) {
            lobWriter = new LOBValueWriter(this);
        }
        return lobWriter;
    }

    /**
     * Allocate a statement for dynamic SQL execution.
     * Either return the cached dynamic statement, or a new statement.
     * This statement must be released after execution.
     */
    public synchronized Statement allocateDynamicStatement() throws SQLException {
        if (dynamicStatement == null) {
            dynamicStatement = getConnection().createStatement();
        }
        if (isDynamicStatementInUse()) {
            return getConnection().createStatement();
        }
        setIsDynamicStatementInUse(true);
        return dynamicStatement;
    }

    /**
     * Return the cached statement for dynamic SQL execution is in use.
     * Used to handle concurrency for the dynamic statement, this
     * method must only be called from within a synchronized method/block.
     */
    public boolean isDynamicStatementInUse() {
        return isDynamicStatementInUse;
    }

    /**
     * Set if the cached statement for dynamic SQL execution is in use.
     * Used to handle concurrency for the dynamic statement.
     */
    public synchronized void setIsDynamicStatementInUse(boolean isDynamicStatementInUse) {
        this.isDynamicStatementInUse = isDynamicStatementInUse;
    }

    /**
     *    Begin a transaction on the database. This means toggling the auto-commit option.
     */
    public void basicBeginTransaction(AbstractSession session) throws DatabaseException {
        try {
            if (getPlatform().supportsAutoCommit()) {
                getConnection().setAutoCommit(false);
            } else {
                getPlatform().beginTransaction(this);
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
    }

    /**
     * If logging is turned on and the JDBC implementation supports meta data then display connection info.
     */
    protected void buildConnectLog(AbstractSession session) {
        try {
            // Log connection information.
            if (session.shouldLog(SessionLog.CONFIG, SessionLog.CONNECTION)) {// Avoid printing if no logging required.
                DatabaseMetaData metaData = getConnectionMetaData();
                Object[] args = { metaData.getURL(), metaData.getUserName(), metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion(), metaData.getDriverName(), metaData.getDriverVersion(), Helper.cr() + "\t" };
                session.log(SessionLog.CONFIG, SessionLog.CONNECTION, "connected_user_database_driver", args, this);
            }
        } catch (SQLException exception) {
            // Some databases do not support metadata, ignore exception.
            session.warning("JDBC_driver_does_not_support_meta_data", SessionLog.CONNECTION);
        }
    }

    /**
     * Build a row from the output parameters of a sp call.
     */
    public AbstractRecord buildOutputRow(CallableStatement statement, DatabaseCall call, AbstractSession session) throws DatabaseException {
        try {
            return call.buildOutputRow(statement);
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, call, this, session);
        }
    }

    /**
     * Return the field sorted in the correct order coresponding to the result set.
     * This is used for cursored selects where custom sql was provided.
     * If the fields passed in are null, this means that the field are not known and should be
     * built from the column names.  This case occurs for DataReadQuery's.
     */
    public Vector buildSortedFields(Vector fields, ResultSet resultSet, AbstractSession session) throws DatabaseException {
        Vector sortedFields;
        try {
            Vector columnNames = getColumnNames(resultSet, session);
            if (fields == null) {// Means fields not known.
                sortedFields = new Vector(columnNames.size());
                for (Enumeration columnNamesEnum = columnNames.elements();
                         columnNamesEnum.hasMoreElements();) {
                    sortedFields.addElement(new DatabaseField((String)columnNamesEnum.nextElement()));
                }
            } else {
                sortedFields = sortFields(fields, columnNames);
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
        return sortedFields;
    }

    /**
     * Connect to the database.
     * Exceptions are caught and re-thrown as TopLink exceptions.
     * Must set the transaction isolation.
     */
    protected void connect(Login login) throws DatabaseException {
        super.connect(login);
        checkTransactionIsolation();
    }

    /**
     * Check to see if the transaction isolation needs to
     * be set for the newly created connection. This must
     * be done outside of a transaction.
     * Exceptions are caught and re-thrown as TopLink exceptions.
     */
    protected void checkTransactionIsolation() throws DatabaseException {
        if ((!isInTransaction()) && (getLogin() != null) && (((DatabaseLogin)getLogin()).getTransactionIsolation() != -1)) {
            try {
                getConnection().setTransactionIsolation(((DatabaseLogin)getLogin()).getTransactionIsolation());
            } catch (java.sql.SQLException sqlEx) {
                throw DatabaseException.sqlException(sqlEx, this, null);
            }
        }
    }

    /**
     * Flush the statement cache.
     * Each statement must first be closed.
     */
    public void clearStatementCache(AbstractSession session) {
        if (hasStatementCache()) {
            for (Enumeration statements = getStatementCache().elements();
                     statements.hasMoreElements();) {
                Statement statement = (Statement)statements.nextElement();
                try {
                    statement.close();
                } catch (SQLException exception) {
                    // an exception can be raised if
                    // a statement is closed twice.
                }
            }
            setStatementCache(null);
        }

        // Close cached dynamic statement.
        if (this.dynamicStatement != null) {
            try {
                this.dynamicStatement.close();
            } catch (SQLException exception) {
                // an exception can be raised if
                // a statement is closed twice.
            }
            this.dynamicStatement = null;
            this.setIsDynamicStatementInUse(false);
        }
    }

    /**
     * Close the result set of the cursored stream.
     */
    public void closeCursor(ResultSet resultSet) throws DatabaseException {
        try {
            resultSet.close();
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, null);
        }
    }

    /**
     * INTERNAL:
     * Closes a PreparedStatment (which is supposed to close it's current resultSet).
     * Factored out to simplify coding and handle exceptions.
     */
    public void closeStatement(Statement statement, AbstractSession session) throws SQLException {
        if (statement == null) {
            decrementCallCount();
            return;
        }

        try {
            session.startOperationProfile(SessionProfiler.STATEMENT_EXECUTE);
            statement.close();
        } finally {
            session.endOperationProfile(SessionProfiler.STATEMENT_EXECUTE);
            decrementCallCount();
            // If this is the cached dynamic statement, release it.
            if (statement == this.dynamicStatement) {
                this.dynamicStatement = null;
                // The dynamic statement is cached and only closed on disconnect.
                setIsDynamicStatementInUse(false);
            }
        }
    }

    /**
     *    Commit a transaction on the database. First flush any batched statements.
     */
    public void commitTransaction(AbstractSession session) throws DatabaseException {
        this.writesCompleted(session);
        super.commitTransaction(session);
    }

    /**
     * Commit a transaction on the database. This means toggling the auto-commit option.
     */
    public void basicCommitTransaction(AbstractSession session) throws DatabaseException {
        try {
            if (getPlatform().supportsAutoCommit()) {
                getConnection().commit();
                getConnection().setAutoCommit(true);
            } else {
                getPlatform().commitTransaction(this);
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
    }

    /**
     * Advance the result set and return a DatabaseRow populated
     * with values from the next valid row in the result set. Intended solely
     * for cursored stream support.
     */
    public AbstractRecord cursorRetrieveNextRow(Vector fields, ResultSet resultSet, AbstractSession session) throws DatabaseException {
        try {
            if (resultSet.next()) {
                return fetchRow(fields, resultSet, resultSet.getMetaData(), session);
            } else {
                return null;
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
    }

    /**
     * Advance the result set and return a DatabaseRow populated
     * with values from the next valid row in the result set. Intended solely
     * for scrollable cursor support.
     */
    public AbstractRecord cursorRetrievePreviousRow(Vector fields, ResultSet resultSet, AbstractSession session) throws DatabaseException {
        try {
            if (resultSet.previous()) {
                return fetchRow(fields, resultSet, resultSet.getMetaData(), session);
            } else {
                return null;
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
    }

    /**
     * Close the connection.
     */
    public void closeDatasourceConnection() throws DatabaseException {
        try {
            getConnection().close();
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, null);
        }
    }

    /**
     *    Disconnect from the datasource.
     *  Added for bug 3046465 to ensure the statement cache is cleared
     */
    public void disconnect(AbstractSession session) throws DatabaseException {
        clearStatementCache(session);
        super.disconnect(session);
    }

    /**
     * Close the accessor's connection.
     * This is used only for external connection pooling
     * when it is intended for the connection to be reconnected in the future.
     */
    public void closeConnection() {
        // Unfortunately do not have the session to pass, fortunately it is not used.
        clearStatementCache(null);
        super.closeConnection();
    }

    /**
     * Execute the TopLink dynamicly batch/concat statement.
     */
    protected void executeBatchedStatement(PreparedStatement statement, AbstractSession session) throws DatabaseException {
        try {
            executeDirectNoSelect(statement, null, session);
        } catch (RuntimeException exception) {
            try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                closeStatement(statement, session);
            } catch (SQLException closeException) {
            }

            throw exception;
        }

        // This is in seperate try block to ensure that the real exception is not masked by the close exception.
        try {
            closeStatement(statement, session);
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
    }

    /**
     * Execute the call.
     * The execution can differ slightly depending on the type of call.
     * The call may be parameterized where the arguments are in the translation row.
     * The row will be empty if there are no parameters.
     * @return depending of the type either the row count, row or vector of rows.
     */
    public Object executeCall(Call call, AbstractRecord translationRow, AbstractSession session) throws DatabaseException {
        // Keep complete implementation.
        return basicExecuteCall(call, translationRow, session);
    }

    /**
     * Execute the call.
     * The execution can differ slightly depending on the type of call.
     * The call may be parameterized where the arguments are in the translation row.
     * The row will be empty if there are no parameters.
     * @return depending of the type either the row count, row or vector of rows.
     */
    public Object basicExecuteCall(Call call, AbstractRecord translationRow, AbstractSession session) throws DatabaseException {
        Statement statement = null;
        Object result = null;
        DatabaseCall dbCall = null;
        ResultSet resultSet = null;// only used if this is a read query
        try {
            dbCall = (DatabaseCall)call;
        } catch (ClassCastException e) {
            throw QueryException.invalidDatabaseCall(call);
        }

        // If the login is null, then this accessor has never been connected.
        if (getLogin() == null) {
            throw DatabaseException.databaseAccessorNotConnected();
        }

        try {
            incrementCallCount(session);
            if (session.shouldLog(SessionLog.FINE, SessionLog.SQL)) {// Avoid printing if no logging required.
                session.log(SessionLog.FINE, SessionLog.SQL, dbCall.getLogString(this), (Object[])null, this, false);
            }
            session.startOperationProfile(SessionProfiler.SQL_PREPARE);
            try {
                statement = dbCall.prepareStatement(this, translationRow, session);
            } finally {
                session.endOperationProfile(SessionProfiler.SQL_PREPARE);
            }

            // effectively this means that someone is executing an update type query.
            if (dbCall.isNothingReturned()) {
                result = executeNoSelect(dbCall, statement, session);
                if (dbCall.isLOBLocatorNeeded()) {
                    // add original (insert or update) call to the LOB locator
                    // Bug 2804663 - LOBValueWriter is no longer a singleton
                    getLOBWriter().addCall(dbCall);
                }
            } else if (!dbCall.getReturnsResultSet() || (dbCall.getReturnsResultSet() && dbCall.shouldBuildOutputRow())) {
                result = session.getPlatform().executeStoredProcedure(dbCall, (PreparedStatement)statement, this, session);
            } else {// not a stored procedure
                resultSet = executeSelect(dbCall, statement, session);
                if (dbCall.getFirstResult() != 0) {
                    resultSet.absolute(dbCall.getFirstResult());
                }
                ResultSetMetaData metaData = resultSet.getMetaData();
                dbCall.matchFieldOrder(resultSet, this, session);

                if (dbCall.isCursorReturned()) {
                    dbCall.setStatement(statement);
                    dbCall.setResult(resultSet);
                    return dbCall;
                }

                session.startOperationProfile(SessionProfiler.ROW_FETCH);
                try {
                    if (dbCall.isOneRowReturned()) {
                        if (resultSet.next()) {
                            if (dbCall.isLOBLocatorNeeded()) {
                                //if Oracle BLOB/CLOB field is being written, and the thin driver is used, the driver 4k
                                //limit bug prevent the call from directly writing to the table if the LOB value size exceeds 4k.
                                //Instead, a LOB locator is retrieved and value is then piped into the table through the locator.
                                // Bug 2804663 - LOBValueWriter is no longer a singleton
                                getLOBWriter().fetchLocatorAndWriteValue(dbCall, resultSet);
                            } else {
                                result = fetchRow(dbCall.getFields(), resultSet, metaData, session);
                            }
                            if (resultSet.next()) {
                                // Raise more rows event, some apps may interpret as error or warning.
                                session.getEventManager().moreRowsDetected(dbCall);
                            }
                        } else {
                            result = null;
                        }
                    } else {
                        boolean hasNext = resultSet.next();
                        Vector results = null;

                        // PERF: Optimize out simple empty case.
                        if (hasNext) {
                            if (shouldUseThreadCursors()) {
                                // If using threading return the cursored list,
                                // do not close the result or statement as the rows are being fetched by the thread.
                                return buildThreadCursoredResult(dbCall, resultSet, statement, metaData, session);
                            } else {
                                results = new Vector(20);
                                while (hasNext) {
                                    results.addElement(fetchRow(dbCall.getFields(), resultSet, metaData, session));
                                    hasNext = resultSet.next();
                                }
                            }
                        } else {
                            results = new Vector(0);
                        }
                        result = results;
                    }
                    resultSet.close();// This must be closed incase the statement is cached and not closed.
                } finally {
                    session.endOperationProfile(SessionProfiler.ROW_FETCH);
                }
            }
        } catch (SQLException exception) {
            try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                closeStatement(statement, session);
            } catch (Exception closeException) {
            }
            throw DatabaseException.sqlException(exception, dbCall, this, session);
        } catch (RuntimeException exception) {
            try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                closeStatement(statement, session);
            } catch (Exception closeException) {
            }
            if (exception instanceof DatabaseException) {
                ((DatabaseException)exception).setCall(dbCall);
            }
            throw exception;
        }

        // This is in seperate try block to ensure that the real exception is not masked by the close exception.
        try {
            // Allow for caching of statement, forced closes are not cache as they failed execution so are most likely bad.
            releaseStatement(statement, dbCall.getSQLString(), dbCall, session);
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }

        return result;
    }

    protected Vector buildThreadCursoredResult(final DatabaseCall dbCall, final ResultSet resultSet, final Statement statement, final ResultSetMetaData metaData, final AbstractSession session) {
        final ThreadCursoredList results = new ThreadCursoredList(20);
        Thread thread = new Thread() {
            public void run() {
                session.startOperationProfile(SessionProfiler.ROW_FETCH);
                try {
                    // Initial next was already validated before this method is called.
                    boolean hasNext = true;
                    while (hasNext) {
                        results.addElement(fetchRow(dbCall.getFields(), resultSet, metaData, session));
                        hasNext = resultSet.next();
                    }
                    resultSet.close();// This must be closed incase the statement is cached and not closed.
                } catch (SQLException exception) {
                    try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                        closeStatement(statement, session);
                    } catch (Exception closeException) {
                    }
                    results.throwException(DatabaseException.sqlException(exception, dbCall, DatabaseAccessor.this, session));
                } catch (RuntimeException exception) {
                    try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                        closeStatement(statement, session);
                    } catch (Exception closeException) {
                    }
                    if (exception instanceof DatabaseException) {
                        ((DatabaseException)exception).setCall(dbCall);
                    }
                    results.throwException(exception);
                } finally {
                    session.endOperationProfile(SessionProfiler.ROW_FETCH);
                }

                // This is in seperate try block to ensure that the real exception is not masked by the close exception.
                try {
                    // Allow for caching of statement, forced closes are not cache as they failed execution so are most likely bad.
                    DatabaseAccessor.this.releaseStatement(statement, dbCall.getSQLString(), dbCall, session);
                } catch (SQLException exception) {
                    results.throwException(DatabaseException.sqlException(exception, DatabaseAccessor.this, session));
                }
                results.setIsComplete(true);
            }
        };
        thread.start();

        return results;
    }

    /**
     * Execute the statement.
     */
    public Integer executeDirectNoSelect(Statement statement, DatabaseCall call, AbstractSession session) throws DatabaseException {
        int rowCount = 0;

        try {
            session.startOperationProfile(SessionProfiler.STATEMENT_EXECUTE);
            if ((call != null) && call.isDynamicCall(session)) {
                rowCount = statement.executeUpdate(call.getSQLString());
            } else {
                rowCount = ((PreparedStatement)statement).executeUpdate();
            }
            if ((!getPlatform().supportsAutoCommit()) && (!isInTransaction())) {
                getPlatform().autoCommit(this);
            }
        } catch (SQLException exception) {
            if (!getPlatform().shouldIgnoreException(exception)) {
                throw DatabaseException.sqlException(exception, this, session);
            }
        } finally {
            session.endOperationProfile(SessionProfiler.STATEMENT_EXECUTE);
        }

        return new Integer(rowCount);
    }

    /**
     * Execute the batched statement through the JDBC2 API.
     */
    protected void executeJDK12BatchStatement(Statement statement, DatabaseCall dbCall, AbstractSession session) throws DatabaseException {
        try {
            statement.executeBatch();
        } catch (SQLException exception) {
            try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                closeStatement(statement, session);
            } catch (SQLException closeException) {
            }

            throw DatabaseException.sqlException(exception, this, session);
        } catch (RuntimeException exception) {
            try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                closeStatement(statement, session);
            } catch (SQLException closeException) {
            }

            throw exception;
        }

        // This is in seperate try block to ensure that the real exception is not masked by the close exception.
        try {
            // if we are called from the ParameterizedBatchWritingMechanism then dbCall will not be null
            //and we should try an release the statement
            if (dbCall != null) {
                releaseStatement((PreparedStatement)statement, dbCall.getSQLString(), dbCall, session);
            } else {
                closeStatement(statement, session);
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
    }

    /**
     * Execute the statement.
     */
    protected Integer executeNoSelect(DatabaseCall call, Statement statement, AbstractSession session) throws DatabaseException {
        Integer rowCount = executeDirectNoSelect(statement, call, session);

        // Allow for procs with outputs to be raised as events for error handling.
        if (call.shouldBuildOutputRow()) {
            AbstractRecord outputRow = buildOutputRow((CallableStatement)statement, call, session);
            call.getQuery().setProperty("output", outputRow);
            session.getEventManager().outputParametersDetected(outputRow, call);
        }

        return rowCount;
    }

    /**
     * Execute the statement.
     */
    protected ResultSet executeSelect(DatabaseCall call, Statement statement, AbstractSession session) throws SQLException {
        ResultSet resultSet;

        session.startOperationProfile(SessionProfiler.STATEMENT_EXECUTE);
        try {
            if (call.isDynamicCall(session)) {
                resultSet = statement.executeQuery(call.getSQLString());
            } else {
                resultSet = ((PreparedStatement)statement).executeQuery();
            }
        } finally {
            session.endOperationProfile(SessionProfiler.STATEMENT_EXECUTE);
        }

        // Allow for procs with outputs to be raised as events for error handling.
        if (call.shouldBuildOutputRow()) {
            AbstractRecord outputRow = buildOutputRow((CallableStatement)statement, call, session);
            call.getQuery().setProperty("output", outputRow);
            session.getEventManager().outputParametersDetected(outputRow, call);
        }

        return resultSet;
    }

    /**
     * Return a new DatabaseRow.<p>
     * Populate the row from the data in cursor. The fields representing the results
     * and the order of the results are stored in fields.
     * <p><b>NOTE</b>:
     * Make sure that the field name is set.  An empty field name placeholder is
     * used in the sortFields() method when the number of fields defined does not
     * match the number of column names available on the database.
     */
    protected AbstractRecord fetchRow(Vector fields, ResultSet resultSet, ResultSetMetaData metaData, AbstractSession session) throws DatabaseException {
        Vector values = new Vector(fields.size());
        // PERF: Pass platform and optimize data flag.
        DatabasePlatform platform = getPlatform();
        boolean optimizeData = platform.shouldOptimizeDataConversion();
        int size = fields.size();
        for (int index = 0; index < size; index++) {
            DatabaseField field = (DatabaseField)fields.elementAt(index);
            // Field can be null for fetch groups.
            if (field != null) {
                values.add(getObject(resultSet, field, metaData, index + 1, platform, optimizeData, session));
            } else {
                values.add(null);
            }
        }

        // Row creation is optimized through sharing the same fields for the entire result set.
        return new DatabaseRecord(fields, values);
    }

    /**
     * Get a description of table columns available in a catalog.
     *
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *    <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *    <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *    <LI><B>TABLE_NAME</B> String => table name
     *    <LI><B>COLUMN_NAME</B> String => column name
     *    <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *    <LI><B>TYPE_NAME</B> String => Data source dependent type name
     *    <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
     *        types this is the maximum number of characters, for numeric or
     *        decimal types this is precision.
     *    <LI><B>BUFFER_LENGTH</B> is not used.
     *    <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
     *    <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *    <LI><B>NULLABLE</B> int => is NULL allowed?
     *      <UL>
     *      <LI> columnNoNulls - might not allow NULL values
     *      <LI> columnNullable - definitely allows NULL values
     *      <LI> columnNullableUnknown - nullability unknown
     *      </UL>
     *    <LI><B>REMARKS</B> String => comment describing column (may be null)
     *     <LI><B>COLUMN_DEF</B> String => default value (may be null)
     *    <LI><B>SQL_DATA_TYPE</B> int => unused
     *    <LI><B>SQL_DATETIME_SUB</B> int => unused
     *    <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
     *       maximum number of bytes in the column
     *    <LI><B>ORDINAL_POSITION</B> int    => index of column in table
     *      (starting at 1)
     *    <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
     *      does not allow NULL values; "YES" means the column might
     *      allow NULL values.  An empty string means nobody knows.
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern
     * @param columnNamePattern a column name pattern
     * @return a Vector of DatabaseRows.
     */
    public Vector getColumnInfo(String catalog, String schema, String tableName, String columnName, AbstractSession session) throws DatabaseException {
        if (session.shouldLog(SessionLog.FINEST, SessionLog.QUERY)) {// Avoid printing if no logging required.
            Object[] args = { catalog, schema, tableName, columnName };
            session.log(SessionLog.FINEST, SessionLog.QUERY, "query_column_meta_data_with_column", args, this);
        }
        Vector result = new Vector();
        ResultSet resultSet = null;
        try {
            incrementCallCount(session);
            resultSet = getConnectionMetaData().getColumns(catalog, schema, tableName, columnName);
            Vector fields = buildSortedFields(null, resultSet, session);
            ResultSetMetaData metaData = resultSet.getMetaData();

            while (resultSet.next()) {
                result.addElement(fetchRow(fields, resultSet, metaData, session));
            }
            resultSet.close();
        } catch (SQLException sqlException) {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException closeException) {
            }

            // Ensure that real exception is thrown.			
            throw DatabaseException.sqlException(sqlException, this, session);
        } finally {
            decrementCallCount();
        }
        return result;
    }

    /**
     * Return the column names from a result sets meta data.
     * This is required for custom SQL execution only,
     * as generated SQL already knows the fields returned.
     */
    protected Vector getColumnNames(ResultSet resultSet, AbstractSession session) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Vector columnNames = new Vector(metaData.getColumnCount());

        for (int index = 0; index < metaData.getColumnCount(); index++) {
            // Changed the following code to use metaData#getColumnLabel() instead of metaData.getColumnName()
            // This is as required by JDBC spec to access metadata for queries using column aliases.
            // Reconsider whether to migrate this change to other versions of Toplink with older native query support
            String columnName = metaData.getColumnLabel(index + 1);
            if ((columnName == null) || columnName.equals("")) {
                columnName = "C" + (index + 1);// Some column may be unnamed.
            }

            // Force field names to upper case is set.
            if (getPlatform().shouldForceFieldNamesToUpperCase()) {
                columnName = columnName.toUpperCase();
            }
            columnNames.addElement(columnName);
        }
        return columnNames;
    }

    /**
     *    Return the receiver's connection to its data source. A connection is used to execute queries on,
     *    and retreive data from, a data source.
     *    @see java.sql.Connection
     */
    public Connection getConnection() throws DatabaseException {
        return (Connection)getDatasourceConnection();
    }

    /**
     * Return the platform.
     */
    public DatabasePlatform getPlatform() {
        return (DatabasePlatform)platform;
    }

    /**
     * return the cached metaData
     */
    public DatabaseMetaData getConnectionMetaData() throws SQLException {
        return getConnection().getMetaData();
    }

    /**
     * Return an object retrieved from resultSet with the getObject() method.
     * Optimize the get for certain type to avoid double conversion.
     * <b>NOTE</b>: This method handles a virtual machine error thrown when retrieving times & dates from Oracle or Sybase.
     */
    protected Object getObject(ResultSet resultSet, DatabaseField field, ResultSetMetaData metaData, int columnNumber, DatabasePlatform platform, boolean optimizeData, AbstractSession session) throws DatabaseException {
        Object value = null;
        try {
            // PERF: Cache the JDBC type in the field to avoid JDBC call.
            int type = field.sqlType;
            if (type == -1) {
                type = metaData.getColumnType(columnNumber);
                field.setSqlType(type);
            }

            if (optimizeData) {
                try {
                    value = getObjectThroughOptimizedDataConversion(resultSet, field, type, columnNumber, platform, session);
                    // Since null cannot be distighighsed from no optimization done, this is return for no-op.
                    if (value == null) {
                        return null;
                    }
                    if (value == this) {
                        value = null;
                    }
                } catch (SQLException exception) {
                    // Log the exception and try non-optimized data conversion
                    if (session.shouldLog(SessionLog.WARNING, SessionLog.SQL)) {
                        session.logThrowable(SessionLog.WARNING, SessionLog.SQL, exception);
                    }
                }
            }
            if (value == null) {
                if ((type == Types.LONGVARBINARY) && platform.usesStreamsForBinding()) {
                    //can read large binary data as a stream
                    InputStream tempInputStream;
                    tempInputStream = resultSet.getBinaryStream(columnNumber);
                    if (tempInputStream != null) {
                        try {
                            ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
                            int tempInt = tempInputStream.read();
                            while (tempInt != -1) {
                                tempOutputStream.write(tempInt);
                                tempInt = tempInputStream.read();
                            }
                            value = tempOutputStream.toByteArray();
                        } catch (IOException exception) {
                            throw DatabaseException.errorReadingBlobData();
                        }
                    } else {
                        value = null;
                    }
                } else {
                    value = platform.getObjectFromResultSet(resultSet, columnNumber, type);                      
                    // PERF: only perform blob check on non-optimized types.
                    // CR2943 - convert early if the type is a BLOB or a CLOB.  
                    if (isBlob(type)) {
                        value = platform.convertObject(value, ClassConstants.APBYTE);
                    }
                    if (isClob(type)) {
                        value = getPlatform().convertObject(value, ClassConstants.STRING);
                    }
                }
            }
            if (resultSet.wasNull()) {
                value = null;
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }

        return value;
    }

    /**
     * Handle the conversion into java optimially through calling the direct type API.
     * If the type is not one that can be optimized return null.
     */
    protected Object getObjectThroughOptimizedDataConversion(ResultSet resultSet, DatabaseField field, int type, int columnNumber, DatabasePlatform platform, AbstractSession session) throws SQLException {
        Object value = this;// Means no optimization, need to distighuuish from null.
        Class fieldType = field.type;        

        // Optimize numeric values to avoid conversion into big-dec and back to primitives.
        if ((fieldType == ClassConstants.PLONG) || (fieldType == ClassConstants.LONG)) {
            value = new Long(resultSet.getLong(columnNumber));
        } else if ((type == Types.VARCHAR) || (type == Types.CHAR)) {
            // CUSTOM PATCH for oracle drivers because they don't respond to getObject() when using scrolling result sets. 
            // Chars may require blanks to be trimmed.
            value = resultSet.getString(columnNumber);
            if ((type == Types.CHAR) && (value != null) && platform.shouldTrimStrings()) {
                value = Helper.rightTrimString((String)value);
            }
        } else if ((fieldType == ClassConstants.INTEGER) || (fieldType == ClassConstants.PINT)) {
            value = new Integer(resultSet.getInt(columnNumber));
        } else if ((fieldType == ClassConstants.FLOAT) || (fieldType == ClassConstants.PFLOAT)) {
            value = new Float(resultSet.getFloat(columnNumber));
        } else if ((fieldType == ClassConstants.DOUBLE) || (fieldType == ClassConstants.PDOUBLE)) {
            value = new Double(resultSet.getDouble(columnNumber));
        } else if ((fieldType == ClassConstants.SHORT) || (fieldType == ClassConstants.PSHORT)) {
            value = new Short(resultSet.getShort(columnNumber));
        } else if (Helper.shouldOptimizeDates() && (fieldType != null) && ((type == Types.TIME) || (type == Types.DATE) || (type == Types.TIMESTAMP))) {
            // Optimize dates by avoid conversion to timestamp then back to date or time or util.date.
            String dateString = resultSet.getString(columnNumber);
            value = platform.convertObject(dateString, fieldType);
        } else if ((fieldType != null) && ((type == Types.TIME) || (type == Types.DATE) || (type == Types.TIMESTAMP))) {
            // PERF: Optimize dates by calling direct get method if type is Date or Time,
            // unfortunately the double conversion is unavoidable for Calendar and util.Date.
            if (fieldType == ClassConstants.SQLDATE) {
                value = resultSet.getDate(columnNumber);
            } else if (fieldType == ClassConstants.TIME) {
                value = resultSet.getTime(columnNumber);
            } else if (fieldType == ClassConstants.TIMESTAMP) {
                value = resultSet.getTimestamp(columnNumber);
            }
        }

        return value;
    }

    /**
     * Return if the accessor has any cached statements.
     * This should be used to avoid lazy instantiation of the cache.
     */
    protected boolean hasStatementCache() {
        return (statementCache != null) && (!statementCache.isEmpty());
    }

    /**
     * The statement cache stores a fixed sized number of prepared statements.
     */
    protected synchronized Hashtable getStatementCache() {
        if (statementCache == null) {
            statementCache = new Hashtable(50);
        }
        return statementCache;
    }

    /**
     * Get a description of tables available in a catalog.
     *
     * <P>Only table descriptions matching the catalog, schema, table
     * name and type criteria are returned.  They are ordered by
     * TABLE_TYPE, TABLE_SCHEM and TABLE_NAME.
     *
     * <P>Each table description has the following columns:
     *  <OL>
     *    <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *    <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *    <LI><B>TABLE_NAME</B> String => table name
     *    <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *            "VIEW",    "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *            "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *    <LI><B>REMARKS</B> String => explanatory comment on the table
     *  </OL>
     *
     * <P><B>Note:</B> Some databases may not return information for
     * all tables.
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern
     * @param types a list of table types to include; null returns all types
     * @return a Vector of DatabaseRows.
     */
    public Vector getTableInfo(String catalog, String schema, String tableName, String[] types, AbstractSession session) throws DatabaseException {
        if (session.shouldLog(SessionLog.FINEST, SessionLog.QUERY)) {// Avoid printing if no logging required.
            Object[] args = { catalog, schema, tableName };
            session.log(SessionLog.FINEST, SessionLog.QUERY, "query_column_meta_data", args, this);
        }
        Vector result = new Vector();
        ResultSet resultSet = null;
        try {
            incrementCallCount(session);
            resultSet = getConnectionMetaData().getTables(catalog, schema, tableName, types);
            Vector fields = buildSortedFields(null, resultSet, session);
            ResultSetMetaData metaData = resultSet.getMetaData();

            while (resultSet.next()) {
                result.addElement(fetchRow(fields, resultSet, metaData, session));
            }
            resultSet.close();
        } catch (SQLException sqlException) {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException closeException) {
            }

            // Ensure that real exception is thrown.			
            throw DatabaseException.sqlException(sqlException, this, session);
        } finally {
            decrementCallCount();
        }
        return result;
    }

    /**
     *    Return true if the receiver is currently connected to a data source. Return false otherwise.
     */
    public boolean isDatasourceConnected() {
        try {
            return !getConnection().isClosed();
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, null);
        }
    }

    /**
     * Return if thread cursors should be used for fetch the result row.
     * This allows the objects to be built while the rows are being fetched.
     */
    public boolean shouldUseThreadCursors() {
        return shouldUseThreadCursors;
    }

    /**
     * Set if thread cursors should be used for fetch the result row.
     * This allows the objects to be built while the rows are being fetched.
     */
    public void setShouldUseThreadCursors(boolean shouldUseThreadCursors) {
        this.shouldUseThreadCursors = shouldUseThreadCursors;
    }

    /**
     * Prepare the SQL statement for the call.
     * First check if the statement is cached before building a new one.
     * Currently the SQL string is used as the cache key, this may have to be switched if it becomes a performance problem.
     */
    public Statement prepareStatement(DatabaseCall call, AbstractSession session) throws SQLException {
        Statement statement = null;
        if (call.usesBinding(session) && call.shouldCacheStatement(session)) {
            // Check the cache by sql string, must syncronize check and removal.
            synchronized (getStatementCache()) {
                statement = (PreparedStatement)getStatementCache().get(call.getSQLString());
                if (statement != null) {
                    // Need to remove to allow concurrent statement execution.
                    getStatementCache().remove(call.getSQLString());
                }
            }
        }

        if (statement == null) {
            if (call.isCallableStatementRequired()) {
                // Callable statements are used for StoredProcedures and PLSQL blocks.
                if (call.isResultSetScrollable()) {
                    statement = getConnection().prepareCall(call.getSQLString(), call.getResultSetType(), call.getResultSetConcurrency());
                } else {
                    statement = getConnection().prepareCall(call.getSQLString());
                }
            } else if (call.isResultSetScrollable()) {
                // Scrollable statements are used for ScrollableCursors.
                statement = getConnection().prepareStatement(call.getSQLString(), call.getResultSetType(), call.getResultSetConcurrency());
            } else if (call.isDynamicCall(session)) {
                // PERF: Dynamic statements are used for dynamic SQL.
                statement = allocateDynamicStatement();
            } else {
                // Prepared statements are used if binding is used, or dynamic statements are turned off.
                statement = getConnection().prepareStatement(call.getSQLString());
            }
        }

        return statement;
    }

    /**
     * Attempt to save some of the cost associated with getting a fresh connection.
     * Assume the DatabaseDriver has been cached, if appropriate.
     * Note: Connections that are participating in transactions will not be refreshd.^M
     * Added for bug 3046465 to ensure the statement cache is cleared
     */
    protected void reconnect(AbstractSession session) {
        clearStatementCache(session);
        super.reconnect(session);
    }

    /**
     * Release the statement through closing it or putting it back in the statement cache.
     */
    protected void releaseStatement(Statement statement, String sqlString, DatabaseCall call, AbstractSession session) throws SQLException {
        if (call.usesBinding(session) && call.shouldCacheStatement(session)) {
            synchronized (getStatementCache()) {
                PreparedStatement preparedStatement = (PreparedStatement)statement;
                if (!getStatementCache().containsKey(sqlString)) {// May already be there by other thread.
                    preparedStatement.clearParameters();
                    if (getStatementCache().size() > getPlatform().getStatementCacheSize()) {
                        // Currently one is removed at random...
                        PreparedStatement removedStatement = (PreparedStatement)getStatementCache().remove(getStatementCache().keys().nextElement());
                        closeStatement(removedStatement, session);
                    } else {
                        decrementCallCount();
                    }
                    getStatementCache().put(sqlString, preparedStatement);
                } else {
                    // CR... Must close the statement if not cached.
                    closeStatement(statement, session);
                }
            }
        } else if (statement == this.dynamicStatement) {
            if (call.getMaxRows() > 0) { 
                statement.setMaxRows(0); 
            } 
            setIsDynamicStatementInUse(false);
            decrementCallCount();
        } else {
            closeStatement(statement, session);
        }
    }

    /**
     * Rollback a transaction on the database. This means toggling the auto-commit option.
     */
    public void basicRollbackTransaction(AbstractSession session) throws DatabaseException {
        try {
            if (getPlatform().supportsAutoCommit()) {
                getConnection().rollback();
                getConnection().setAutoCommit(true);
            } else {
                getPlatform().rollbackTransaction(this);
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception, this, session);
        }
    }

    /**
     * The statement cache stores a fixed sized number of prepared statements.
     */
    protected void setStatementCache(Hashtable statementCache) {
        this.statementCache = statementCache;
    }

    /**
     * This method will sort the fields in correct order based
     * on the column names.
     */
    protected Vector sortFields(Vector fields, Vector columnNames) {
        Vector sortedFields = new Vector(columnNames.size());
        Vector eligableFields = (Vector)fields.clone();// Must clone to allow removing to support the same field twice.
        Enumeration columnNamesEnum = columnNames.elements();
        boolean valueFound = false;
        while (columnNamesEnum.hasMoreElements()) {
            String columnName = (String)columnNamesEnum.nextElement();

            DatabaseField field = null;
            Enumeration fieldEnum = eligableFields.elements();
            while (fieldEnum.hasMoreElements()) {
                field = (DatabaseField)fieldEnum.nextElement();
                if (DatabasePlatform.shouldIgnoreCaseOnFieldComparisons()) {
                    if (field.getName().equalsIgnoreCase(columnName)) {
                        valueFound = true;
                        sortedFields.addElement(field);
                        break;
                    }
                } else {
                    if (field.getName().equals(columnName)) {
                        valueFound = true;
                        sortedFields.addElement(field);
                        break;
                    }
                }
            }

            if (valueFound) {
                // The eligable fields must be maintained as two field can have the same name, but different tables.
                eligableFields.removeElement(field);
            } else {
                // Need to add a place holder in case the column is not in the fiels vector
                sortedFields.addElement(new DatabaseField());
            }
            valueFound = false;
        }

        return sortedFields;
    }

    public String toString() {
        StringWriter writer = new StringWriter();
        writer.write("DatabaseAccessor(");
        if (isConnected()) {
            writer.write(ToStringLocalization.buildMessage("connected", (Object[])null));
        } else {
            writer.write(ToStringLocalization.buildMessage("disconnected", (Object[])null));
        }
        writer.write(")");
        return writer.toString();
    }

    /**
     * Return if the JDBC type is a binary type such as blob.
     */
    private boolean isBlob(int type) {
        return (type == Types.BLOB) || (type == Types.LONGVARBINARY);
    }

    /**
     * Return if the JDBC type is a large character type such as clob.
     */
    private boolean isClob(int type) {
        return (type == Types.CLOB) || (type == Types.LONGVARCHAR);
    }

    /**
     * This method will be called after a series of writes have been issued to
     * mark where a particular set of writes has completed.  It will be called
     * from commitTransaction and may be called from writeChanges.   Its main
     * purpose is to ensure that the batched statements have been executed
     */
    public void writesCompleted(AbstractSession session) {
        // Place holder for batch writing.
    }
}
