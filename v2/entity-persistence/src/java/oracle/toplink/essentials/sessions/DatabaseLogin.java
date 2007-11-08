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

import java.io.*;
import java.sql.Connection;
import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;
import oracle.toplink.essentials.internal.databaseaccess.DatabaseAccessor;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.platform.database.*;
import oracle.toplink.essentials.platform.database.oracle.OraclePlatform;
import oracle.toplink.essentials.sequencing.NativeSequence;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.jndi.*;

/**
 * <p>
 * <b>Purpose</b>:
 * Hold the configuration information necessary to connect to a JDBC driver.
 * <p>
 * <b>Description</b>:
 * A DatabaseLogin is used by a TopLink database session to connect to a
 * JDBC server.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Hold the driver class name and URL header
 * <li> Hold the database URL
 * <li> Hold any driver-specific database connection properties (e.g. "user", "database")
 * <li> Build the JDBC driver connect string
 * <li> Hold the database platform (e.g. Oracle, DB2)
 * <li> Hold the message logging stream
 * <li> Hold other assorted configuration settings
 * </ul>
 */
public class DatabaseLogin extends DatasourceLogin {

    /**
     * Transaction isolation levels used in setTransactionIsolation().
     * These constants are cribbed from java.sql.Connection.
     */
    /** Transactions are not supported. */
    public static final int TRANSACTION_NONE = Connection.TRANSACTION_NONE;

    /** Dirty reads, non-repeatable reads and phantom reads can occur. */
    public static final int TRANSACTION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;

    /** Dirty reads are prevented; non-repeatable reads and phantom reads can occur. */
    public static final int TRANSACTION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;

    /** Dirty reads and non-repeatable reads are prevented; phantom reads can occur. */
    public static final int TRANSACTION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;

    /** Dirty reads, non-repeatable reads and phantom reads are prevented. */
    public static final int TRANSACTION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;

    /**
     * PUBLIC:
     * Create a new login.
     */
    public DatabaseLogin() {
        this(new DatabasePlatform());
    }

    /**
     * ADVANCED:
     * Create a new login for the given platform.
     */
    public DatabaseLogin(DatabasePlatform databasePlatform) {
        super(databasePlatform);
        this.useDefaultDriverConnect();
    }

    /**
     * ADVANCED:
     * Set the database platform to be custom platform.
     */
    public void usePlatform(DatabasePlatform platform) {
        super.usePlatform((Platform)platform);
    }

    /**
     * PUBLIC:
     * Bind all arguments to any SQL statement.
     */
    public void bindAllParameters() {
        setShouldBindAllParameters(true);
    }

    /**
     * INTERNAL:
     * Build and return an appropriate Accessor.
     * The default is a DatabaseAccessor.
     */
    public Accessor buildAccessor() {
        return new DatabaseAccessor();
    }

    /**
     * PUBLIC:
     * Cache all prepared statements, this requires full parameter binding as well.
     * @see #bindAllParameters()
     */
    public void cacheAllStatements() {
        setShouldCacheAllStatements(true);
    }

    /**
     * PUBLIC:
     * Do not bind all arguments to any SQL statement.
     */
    public void dontBindAllParameters() {
        setShouldBindAllParameters(false);
    }

    /**
     * PUBLIC:
     * Do not cache all prepared statements.
     */
    public void dontCacheAllStatements() {
        setShouldCacheAllStatements(false);
    }

    /**
     * PUBLIC:
     * Disable driver level data conversion optimization.
     * This can be disabled as some drivers perform data conversion themselves incorrectly.
     */
    public void dontOptimizeDataConversion() {
        setShouldOptimizeDataConversion(false);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use parameter binding for large binary data.
     * By default TopLink will print this data as hex through the JDBC binary excape clause.
     * Both binding and printing have various limits on all databases (e.g. 5k - 32k).
     */
    public void dontUseByteArrayBinding() {
        setUsesByteArrayBinding(false);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use database-specific SQL grammar,
     * as opposed to the JDBC standard grammar.
     * This is because, unfortunately, some drivers to not support the full JDBC standard.
     * By default TopLink uses the JDBC SQL grammar.
     */
    public void dontUseNativeSQL() {
        setUsesNativeSQL(false);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use streams to store large binary data.
     */
    public void dontUseStreamsForBinding() {
        setUsesStreamsForBinding(false);
    }

    /**
     * PUBLIC:
     * Do not bind strings of any size.
     */
    public void dontUseStringBinding() {
        getPlatform().setStringBindingSize(0);
        getPlatform().setUsesStringBinding(false);
    }

    /**
     * INTERNAL:
     * Return whether the specified driver is being used.
     */
    protected boolean driverIs(String driverName) {
        try {
            return getDriverClassName().equals(driverName);
        } catch (ValidationException e) {
            // this exception will be thrown if we are using something other than a DefaultConnector
            return false;
        }
    }

    /**
     * PUBLIC:
     * Return the JDBC connection string.
     * This is a combination of the driver-specific URL header and the database URL.
     */
    public String getConnectionString() throws ValidationException {
        return getDefaultConnector().getConnectionString();
    }

    /**
     * ADVANCED:
     * Return the code for preparing cursored output
     * parameters in a stored procedure
     */
    public int getCursorCode() {
        return getPlatform().getCursorCode();
    }

    /**
     * PUBLIC:
     * The database name is required when connecting to databases that support
     * multiple databases within a single server instance (e.g. Sybase, SQL Server).
     * This is ONLY used when connecting through ODBC type JDBC drivers.
     * This is NEVER used with Oracle.
     */
    public String getDatabaseName() {
        return properties.getProperty("database");
    }

    /**
     * PUBLIC:
     * The database URL is the JDBC URL for the database server.
     * The driver header is <i>not</i> be included in this URL
     * (e.g. "dbase files"; not "jdbc:odbc:dbase files").
     */
    public String getDatabaseURL() throws ValidationException {
        return getDefaultConnector().getDatabaseURL();
    }

    /**
     * PUBLIC:
     * The data source name is required if connecting through ODBC (JDBC-ODBC, etc.).
     * This is the ODBC name given in the ODBC Data Source Administrator.
     * This is just the database part of the URL.
     */
    public String getDataSourceName() throws ValidationException {
        return getDatabaseURL();
    }

    /**
     * INTERNAL:
     * Return the connector that will instantiate the java.sql.Connection.
     */
    protected DefaultConnector getDefaultConnector() throws ValidationException {
        try {
            return (DefaultConnector)getConnector();
        } catch (ClassCastException e) {
            throw ValidationException.invalidConnector(connector);
        }
    }

    /**
     * PUBLIC:
     * The driver class is the name of the Java class for the JDBC driver being used
     * (e.g. "sun.jdbc.odbc.JdbcOdbcDriver").
     */
    public String getDriverClassName() throws ValidationException {
        return getDefaultConnector().getDriverClassName();
    }

    /**
     * PUBLIC:
     * The driver URL header is the string predetermined by the JDBC driver to be
     * part of the URL connection string, (e.g. "jdbc:odbc:").
     * This is required to connect to the database.
     */
    public String getDriverURLHeader() throws ValidationException {
        return getDefaultConnector().getDriverURLHeader();
    }

    /**
     * PUBLIC:
     * The server name is the name of the database instance.
     * This is ONLY required if using an ODBC JDBC driver
     * and overriding the server name specified in the ODBC
     * Data Source Administrator.
     */
    public String getServerName() {
        return properties.getProperty("server");
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getShouldBindAllParameters() {
        return shouldBindAllParameters();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getShouldCacheAllStatements() {
        return shouldCacheAllStatements();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getShouldOptimizeDataConversion() {
        return shouldOptimizeDataConversion();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getShouldTrimStrings() {
        return shouldTrimStrings();
    }

    /**
     * PUBLIC:
     * If prepared statement caching is used, return the cache size.
     * The default is 50.
     */
    public int getStatementCacheSize() {
        return getPlatform().getStatementCacheSize();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public int getStringBindingSize() {
        return getPlatform().getStringBindingSize();
    }

    /**
     * PUBLIC:
     * Return the transaction isolation setting for the connection.
     * Return -1 if it has not been set.
     */
    public int getTransactionIsolation() {
        return getPlatform().getTransactionIsolation();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getUsesBinding() {
        return shouldUseByteArrayBinding();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getUsesNativeSequencing() {
        return shouldUseNativeSequencing();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getUsesNativeSQL() {
        return shouldUseNativeSQL();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getUsesStreamsForBinding() {
        return shouldUseStreamsForBinding();
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public boolean getUsesStringBinding() {
        return getPlatform().usesStringBinding();
    }

    /**
     * PUBLIC:
     * Force TopLink to manually begin transactions instead of using autoCommit.
     * Although autoCommit should be used, and work, under JDBC,
     * some drivers (e.g. Sybase JConnect)
     * do not correctly map autoCommit to transactions, so stored procedures
     * may not work correctly.
     * This property should only be used as a workaround for the
     * Sybase JConnect transaction problem.
     */
    public void handleTransactionsManuallyForSybaseJConnect() {
        getPlatform().setSupportsAutoCommit(false);
    }

    /**
     * PUBLIC:
     * Return whether an Oracle JDBC driver is being used.
     */
    public boolean isAnyOracleJDBCDriver() {
        return oracleDriverIs("jdbc:oracle:");
    }

    /**
     * PUBLIC:
     * Return whether a Cloudscape JDBC driver is being used.
     */
    public boolean isCloudscapeJDBCDriver() {
        return driverIs("COM.cloudscape.core.JDBCDriver");
    }

    /**
     * PUBLIC:
     * Return whether an IBM DB2 native client JDBC driver is being used.
     */
    public boolean isDB2JDBCDriver() {
        return driverIs("COM.ibm.db2.jdbc.app.DB2Driver");
    }

    /**
     * PUBLIC:
     * Return whether an Intersolv SeqeLink JDBC driver is being used.
     */
    public boolean isIntersolvSequeLinkDriver() {
        return driverIs("intersolv.jdbc.sequelink.SequeLinkDriver");
    }

    /**
     * PUBLIC:
     * Return whether a Sybase JConnect JDBC driver is being used.
     */
    public boolean isJConnectDriver() {
        return driverIs("com.sybase.jdbc.SybDriver");
    }

    /**
     * PUBLIC:
     * Return whether a Borland JDBCConnect JDBC driver is being used.
     */
    public boolean isJDBCConnectDriver() {
        return driverIs("borland.jdbc.Bridge.LocalDriver");
    }

    /**
     * PUBLIC:
     * Return whether a Borland JDBCConnect JDBC driver is being used.
     */
    public boolean isJDBCConnectRemoteDriver() {
        return driverIs("borland.jdbc.Broker.RemoteDriver");
    }

    /**
     * PUBLIC:
     * Return whether a Sun/Merant JDBC-ODBC bridge driver is being used.
     */
    public boolean isJDBCODBCBridge() {
        return driverIs("sun.jdbc.odbc.JdbcOdbcDriver");
    }

    /**
     * PUBLIC:
     * Return whether an Oracle native 7.x OCI JDBC driver is being used.
     */
    public boolean isOracle7JDBCDriver() {
        return oracleDriverIs("jdbc:oracle:oci7:@");
    }

    /**
     * PUBLIC:
     * Return whether an Oracle 8.x native OCI JDBC driver is being used.
     */
    public boolean isOracleJDBCDriver() {
        return oracleDriverIs("jdbc:oracle:oci8:@");
    }

    /**
     * PUBLIC:
     * Return whether an Oracle thin JDBC driver is being used.
     */
    public boolean isOracleServerJDBCDriver() {
        return oracleDriverIs("jdbc:oracle:kprb:");
    }

    /**
     * PUBLIC:
     * Return whether an Oracle thin JDBC driver is being used.
     */
    public boolean isOracleThinJDBCDriver() {
        return oracleDriverIs("jdbc:oracle:thin:@");
    }

    /**
     * PUBLIC:
     * Return whether a WebLogic Oracle OCI JDBC driver is being used.
     */
    public boolean isWebLogicOracleOCIDriver() {
        return driverIs("weblogic.jdbc.oci.Driver");
    }

    /**
     * PUBLIC:
     * Return whether a WebLogic SQL Server dblib JDBC driver is being used.
     */
    public boolean isWebLogicSQLServerDBLibDriver() {
        return driverIs("weblogic.jdbc.dblib.Driver");
    }

    /**
     * PUBLIC:
     * Return whether a WebLogic SQL Server JDBC driver is being used.
     */
    public boolean isWebLogicSQLServerDriver() {
        return driverIs("weblogic.jdbc.mssqlserver4.Driver");
    }

    /**
     * PUBLIC:
     * Return whether a WebLogic Sybase dblib JDBC driver is being used.
     */
    public boolean isWebLogicSybaseDBLibDriver() {
        return driverIs("weblogic.jdbc.dblib.Driver");
    }

    /**
     * PUBLIC:
     * Return whether a WebLogic thin client JDBC driver is being used.
     */
    public boolean isWebLogicThinClientDriver() {
        return driverIs("weblogic.jdbc.t3Client.Driver");
    }

    /**
     * PUBLIC:
     * Return whether a WebLogic thin JDBC driver is being used.
     */
    public boolean isWebLogicThinDriver() {
        return driverIs("weblogic.jdbc.t3.Driver");
    }

    /**
     * PUBLIC:
     * Enable driver level data conversion optimization.
     * This can be disabled as some drivers perform data conversion themselves incorrectly.
     */
    public void optimizeDataConversion() {
        setShouldOptimizeDataConversion(true);
    }

    /**
     * INTERNAL:
     * Return whether the specified Oracle JDBC driver is being used.
     */
    protected boolean oracleDriverIs(String urlPrefix) {
        try {
            if (getDriverURLHeader().length() != 0) {
                return getDriverURLHeader().indexOf(urlPrefix) != -1;
            } else {
                return getDatabaseURL().indexOf(urlPrefix) != -1;
            }
        } catch (ValidationException e) {
            // this exception will be thrown if we are using something other than a DefaultConnector
            return false;
        }
    }

    /**
     * PUBLIC:
     * Set the JDBC connection string.
     * This is the full JDBC connect URL. Normally TopLink breaks this into two parts to
     * allow for the driver header to be automatically set, however sometimes it is easier just to set the
     * entire URL at once.
     */
    public void setConnectionString(String url) throws ValidationException {
        setDriverURLHeader("");
        setDatabaseURL(url);
    }

    /**
     * ADVANCED:
     * Set the code for preparing cursored output
     * parameters in a stored procedure
     */
    public void setCursorCode(int cursorCode) {
        getPlatform().setCursorCode(cursorCode);
    }

    /**
     * PUBLIC:
     * The database name is required when connecting to databases that support
     * multiple databases within a single server instance (e.g. Sybase, SQL Server).
     * This is ONLY used when connecting through ODBC type JDBC drivers.
     * This is NEVER used with Oracle.
     */
    public void setDatabaseName(String databaseName) {
        setProperty("database", databaseName);
    }

    /**
     * PUBLIC:
     * The database URL is the JDBC URL for the database server.
     * The driver header should <i>not</i> be included in this URL
     * (e.g. "dbase files"; not "jdbc:odbc:dbase files").
     */
    public void setDatabaseURL(String databaseURL) throws ValidationException {
        getDefaultConnector().setDatabaseURL(databaseURL);
    }
    
    /**
     * PUBLIC:
     * The data source name is required if connecting through ODBC (JDBC-ODBC, etc.).
     * This is the ODBC name given in the ODBC Data Source Administrator.
     * This is just the database part of the URL.
     */
    public void setODBCDataSourceName(String dataSourceName) {
        setDatabaseURL(dataSourceName);
    }

    /**
     * PUBLIC:
     * The default value to substitute for database NULLs can be configured
     * on a per-class basis.
     * Example: login.setDefaultNullValue(long.class, new Long(0))
     */
    public void setDefaultNullValue(Class type, Object value) {
        getPlatform().getConversionManager().setDefaultNullValue(type, value);
    }

    /**
     * PUBLIC:
     * The driver class is the Java class for the JDBC driver to be used
     * (e.g. sun.jdbc.odbc.JdbcOdbcDriver.class).
     */
    public void setDriverClass(Class driverClass) {
        setDriverClassName(driverClass.getName());
    }

    /**
     * PUBLIC:
     * The name of the JDBC driver class to be used
     * (e.g. "sun.jdbc.odbc.JdbcOdbcDriver").
     */
    public void setDriverClassName(String driverClassName) throws ValidationException {
        getDefaultConnector().setDriverClassName(driverClassName);
    }

    /**
     * PUBLIC:
     * The driver URL header is the string predetermined by the JDBC driver to be
     * part of the URL connection string, (e.g. "jdbc:odbc:").
     * This is required to connect to the database.
     */
    public void setDriverURLHeader(String driverURLHeader) throws ValidationException {
        getDefaultConnector().setDriverURLHeader(driverURLHeader);
    }

    /**
     * PUBLIC:
     * The server name is the name of the database instance.
     * This is ONLY used when connecting through ODBC type JDBC drivers,
     * and only if the data source does not specify it already.
     */
    public void setServerName(String name) {
        setProperty("server", name);
    }

    /**
     * PUBLIC:
     * Set whether to bind all arguments to any SQL statement.
     */
    public void setShouldBindAllParameters(boolean shouldBindAllParameters) {
        getPlatform().setShouldBindAllParameters(shouldBindAllParameters);
    }

    /**
     * PUBLIC:
     * Set whether prepared statements should be cached.
     */
    public void setShouldCacheAllStatements(boolean shouldCacheAllStatements) {
        getPlatform().setShouldCacheAllStatements(shouldCacheAllStatements);
    }

    /**
     * ADVANCED:
     * This setting can be used if the application expects upper case
     * but the database does not return consistent case (e.g. different databases).
     */
    public void setShouldForceFieldNamesToUpperCase(boolean shouldForceFieldNamesToUpperCase) {
        getPlatform().setShouldForceFieldNamesToUpperCase(shouldForceFieldNamesToUpperCase);
    }

    /**
     * ADVANCED:
     * Allow for case in field names to be ignored as some databases are not case sensitive.
     * When using custom this can be an issue if the fields in the descriptor have a different case.
     */
    public static void setShouldIgnoreCaseOnFieldComparisons(boolean shouldIgnoreCaseOnFieldComparisons) {
        DatabasePlatform.setShouldIgnoreCaseOnFieldComparisons(shouldIgnoreCaseOnFieldComparisons);
    }

    /**
     * PUBLIC:
     * Set whether driver level data conversion optimization is enabled.
     * This can be disabled as some drivers perform data conversion themselves incorrectly.
     */
    public void setShouldOptimizeDataConversion(boolean value) {
        getPlatform().setShouldOptimizeDataConversion(value);
    }

    /**
     * PUBLIC:
     * By default CHAR field values have trailing blanks trimmed, this can be configured.
     */
    public void setShouldTrimStrings(boolean shouldTrimStrings) {
        getPlatform().setShouldTrimStrings(shouldTrimStrings);
    }

    /**
     * PUBLIC:
     * If prepared statement caching is used this configures the cache size.
     * The default is 50.
     */
    public void setStatementCacheSize(int size) {
        getPlatform().setStatementCacheSize(size);
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public void setStringBindingSize(int stringBindingSize) {
        getPlatform().setStringBindingSize(stringBindingSize);
    }

    /**
     * PUBLIC:
     * Set the default qualifier for all tables.
     * This can be the creator of the table or database name the table exists on.
     * This is required by some databases such as Oracle and DB2.
     */
    public void setTableQualifier(String qualifier) {
        getPlatform().setTableQualifier(qualifier);
    }

    /**
     * PUBLIC:
     * Set the transaction isolation setting for the connection.
     * This is an optional setting. The default isolation level
     * set on the database will apply if it is not set here.
     * Use one of the TRANSACTION_* constants for valid input values.
     * Note: This setting will only take effect upon connection.
     */
    public void setTransactionIsolation(int isolationLevel) {
        getPlatform().setTransactionIsolation(isolationLevel);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use parameter binding for large binary data.
     * By default TopLink will print this data as hex through the JDBC binary excape clause.
     * Both binding and printing have various limits on all databases (e.g. 5k - 32k).
     */
    public void setUsesByteArrayBinding(boolean value) {
        getPlatform().setUsesByteArrayBinding(value);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use database specific sql grammar not JDBC specific.
     * This is because unfortunately some bridges to not support the full JDBC standard.
     * By default TopLink uses the JDBC sql grammar.
     */
    public void setUsesNativeSQL(boolean value) {
        getPlatform().setUsesNativeSQL(value);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use streams to store large binary data.
     * This can improve the max size for reading/writing on some JDBC drivers.
     */
    public void setUsesStreamsForBinding(boolean value) {
        getPlatform().setUsesStreamsForBinding(value);
    }

    /**
     * PUBLIC:
     * Used to help bean introspection.
     */
    public void setUsesStringBinding(boolean usesStringBindingSize) {
        getPlatform().setUsesStringBinding(usesStringBindingSize);
    }

    /**
     * PUBLIC:
     * Bind all arguments to any SQL statement.
     */
    public boolean shouldBindAllParameters() {
        return getPlatform().shouldBindAllParameters();
    }

    /**
     * PUBLIC:
     * Cache all prepared statements, this requires full parameter binding as well.
     */
    public boolean shouldCacheAllStatements() {
        return getPlatform().shouldCacheAllStatements();
    }

    /**
     * ADVANCED:
     * Can be used if the app expects upper case but the database is not return consistent case, i.e. different databases.
     */
    public boolean shouldForceFieldNamesToUpperCase() {
        return getPlatform().shouldForceFieldNamesToUpperCase();
    }

    /**
     * ADVANCED:
     * Allow for case in field names to be ignored as some databases are not case sensitive.
     * When using custom this can be an issue if the fields in the descriptor have a different case.
     */
    public static boolean shouldIgnoreCaseOnFieldComparisons() {
        return DatabasePlatform.shouldIgnoreCaseOnFieldComparisons();
    }

    /**
     * PUBLIC:
     * Return if our driver level data conversion optimization is enabled.
     * This can be disabled as some drivers perform data conversion themselves incorrectly.
     */
    public boolean shouldOptimizeDataConversion() {
        return getPlatform().shouldOptimizeDataConversion();
    }

    /**
     * PUBLIC:
     * By default CHAR field values have trailing blanks trimmed, this can be configured.
     */
    public boolean shouldTrimStrings() {
        return getPlatform().shouldTrimStrings();
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use parameter binding for large binary data.
     * By default TopLink will print this data as hex through the JDBC binary excape clause.
     * Both binding and printing have various limits on all databases (e.g. 5k - 32k).
     */
    public boolean shouldUseByteArrayBinding() {
        return getPlatform().usesByteArrayBinding();
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use a sequence table
     * or native sequencing to generate unique object IDs.
     * Native sequencing uses the ID generation service provided by the database
     * (e.g. SEQUENCE objects on Oracle and IDENTITY columns on Sybase).
     * By default a sequence table is used. Using a sequence table is recommended
     * as it supports preallocation.
     * (Native sequencing on Sybase/SQL Server/Informix does not support preallocation.
     * Preallocation can be supported on Oracle by setting the increment size of the
     * SEQUENCE object to match the preallocation size.)
     */
    public boolean shouldUseNativeSequencing() {
        return getPlatform().getDefaultSequence() instanceof NativeSequence;
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use database-specific SQL grammar,
     * as opposed to the JDBC standard grammar.
     * This is because, unfortunately, some drivers to not support the full JDBC standard.
     * By default TopLink uses the JDBC SQL grammar.
     */
    public boolean shouldUseNativeSQL() {
        return getPlatform().usesNativeSQL();
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use streams to store large binary data.
     */
    public boolean shouldUseStreamsForBinding() {
        return getPlatform().usesStreamsForBinding();
    }

    /**
     * PUBLIC:
     * TopLink can be configured to bind large strings.
     */
    public boolean shouldUseStringBinding() {
        return getPlatform().usesStringBinding();
    }

    /**
     * PUBLIC:
     * Print all of the connection information.
     */
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        writer.println("DatabaseLogin(");
        writer.println("\t" + ToStringLocalization.buildMessage("platform", (Object[])null) + "=>" + getPlatform());
        writer.println("\t" + ToStringLocalization.buildMessage("user_name", (Object[])null) + "=> \"" + getUserName() + "\"");
        writer.print("\t");
        getConnector().toString(writer);
        if (getServerName() != null) {
            writer.println("\t" + ToStringLocalization.buildMessage("server_name", (Object[])null) + "=> \"" + getServerName() + "\"");
        }
        if (getDatabaseName() != null) {
            writer.println("\t" + ToStringLocalization.buildMessage("database_name", (Object[])null) + "=> \"" + getDatabaseName() + "\"");
        }
        writer.write(")");
        return stringWriter.toString();
    }

    /**
     * PUBLIC:
     * Set the database platform to be Access.
     */
    public void useAccess() {
        if (getPlatform().isAccess()) {
            return;
        }

        DatabasePlatform newPlatform = new AccessPlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use parameter binding for large binary data.
     * By default TopLink will print this data as hex through the JDBC binary excape clause.
     * Both binding and printing have various limits on all databases (e.g. 5k - 32k).
     */
    public void useByteArrayBinding() {
        setUsesByteArrayBinding(true);
    }

    /**
     * PUBLIC:
     * Set the database platform to be Cloudscape.
     */
    public void useCloudscape() {
        if (getPlatform().isCloudscape()) {
            return;
        }

        DatabasePlatform newPlatform = new CloudscapePlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    public void useDerby() {
        if (getPlatform().isDerby()) {
            return;
        }

        DatabasePlatform newPlatform = new DerbyPlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }


    /**
     * PUBLIC:
     * Use the Cloudscape JDBC driver.
     */
    public void useCloudscapeDriver() {
        useCloudscape();
        setDriverClassName("COM.cloudscape.core.JDBCDriver");
        setDriverURLHeader("jdbc:cloudscape:");
    }

    /**
     * PUBLIC:
     * Set the database platform to be DB2.
     */
    public void useDB2() {
        if (getPlatform().isDB2()) {
            return;
        }

        DatabasePlatform newPlatform = new DB2Platform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Use the IBM DB2 native client interface.
     */
    public void useDB2JDBCDriver() {
        useDB2();
        setDriverClassName("COM.ibm.db2.jdbc.app.DB2Driver");
        setDriverURLHeader("jdbc:db2:");
        useStreamsForBinding();// Works best with IBM driver
    }

    /**
     * PUBLIC:
     * Use the IBM DB2 thin JDBC driver.
     */
    public void useDB2NetJDBCDriver() {
        useDB2();
        setDriverClassName("COM.ibm.db2.jdbc.net.DB2Driver");
        setDriverURLHeader("jdbc:db2:");
        useStreamsForBinding();// Works best with IBM driver
    }

    /**
     * PUBLIC:
     * Set the database platform to be DBase.
     */
    public void useDBase() {
        if (getPlatform().isDBase()) {
            return;
        }

        DatabasePlatform newPlatform = new DBasePlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Connect to the JDBC driver via DriverManager.
     * @see #useDirectDriverConnect()
     */
    public void useDefaultDriverConnect() {
        setConnector(new DefaultConnector());
    }

    /**
     * PUBLIC:
     * Connect to the JDBC driver via DriverManager.
     * @see #useDirectDriverConnect(String, String, String)
     */
    public void useDefaultDriverConnect(String driverClassName, String driverURLHeader, String databaseURL) {
        setConnector(new DefaultConnector(driverClassName, driverURLHeader, databaseURL));
    }

    /**
     * PUBLIC:
     * Some JDBC drivers don't support connecting correctly (via DriverManager),
     * but do support connecting incorrectly (e.g. Castanet).
     * @see #useDirectDriverConnect()
     */
    public void useDirectDriverConnect() {
        setConnector(new DirectConnector());
    }
    
    /**
     * PUBLIC:
     * Specify the J2EE DataSource name to connect to.
     * Also enable external connection pooling.
     * @see JNDIConnector
     */
    public void useDataSource(String dataSource) {
        setConnector(new JNDIConnector(dataSource));
        useExternalConnectionPooling();
    }
    
    /**
     * PUBLIC:
     * Specify the J2EE JTA enabled DataSource name to connect to.
     * Also enable external transaction control and connection pooling.
     * @see JNDIConnector
     */
    public void useJTADataSource(String dataSource) {
        useDataSource(dataSource);
        useExternalTransactionController();
    }

    /**
     * PUBLIC:
     * Some JDBC drivers don't support connecting correctly (via DriverManager),
     * but do support connecting incorrectly (e.g. Castanet).
     * @see #useDefaultDriverConnect(String, String, String)
     */
    public void useDirectDriverConnect(String driverClassName, String driverURLHeader, String databaseURL) {
        setConnector(new DirectConnector(driverClassName, driverURLHeader, databaseURL));
    }

    /**
     * PUBLIC:
     * Use external connection pooling, such as WebLogic's JTS driver.
     *
     * @see #dontUseExternalConnectionPooling()
     * @see #shouldUseExternalConnectionPooling()
     */
    public void useExternalConnectionPooling() {
        setUsesExternalConnectionPooling(true);
    }

    /**
     * PUBLIC:
     * Use an external transaction controller such as a JTS service
     *
     * @see #dontUseExternalTransactionController()
     * @see #shouldUseExternalTransactionController()
     */
    public void useExternalTransactionController() {
        setUsesExternalTransactionController(true);
    }

    /**
     * PUBLIC:
     * Use the HSQL JDBC driver.
     */
    public void useHSQL() {
        if (getPlatform().isHSQL()) {
            return;
        }

        DatabasePlatform newPlatform = new HSQLPlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Use the HSQL JDBC driver.
     */
    public void useHSQLDriver() {
        useHSQL();
        setDriverClassName("org.hsqldb.jdbcDriver");
        setDriverURLHeader("jdbc:hsqldb:");
    }

    /**
     * PUBLIC:
     * Use the i-net SQL Server JDBC driver.
     */
    public void useINetSQLServerDriver() {
        setDriverClassName("com.inet.tds.TdsDriver");
        setDriverURLHeader("jdbc:inetdae:");
    }

    /**
     * PUBLIC:
     * Set the database platform to be Informix.
     */
    public void useInformix() {
        if (getPlatform().isInformix()) {
            return;
        }

        DatabasePlatform newPlatform = new InformixPlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Use the Intersolv/Merant SequeLink JDBC driver.
     */
    public void useIntersolvSequeLinkDriver() {
        setDriverClassName("intersolv.jdbc.sequelink.SequeLinkDriver");
        setDriverURLHeader("jdbc:sequelink:");
    }

    /**
     * PUBLIC:
     * Use the Sybase JConnect JDBC driver.
     */
    public void useJConnect50Driver() {
        useSybase();
        setDriverClassName("com.sybase.jdbc2.jdbc.SybDriver");
        setDriverURLHeader("jdbc:sybase:Tds:");
        // JConnect does not support the JDBC SQL grammar
        useNativeSQL();
    }

    /**
     * PUBLIC:
     * Use the Sybase JConnect JDBC driver.
     */
    public void useJConnectDriver() {
        useSybase();
        setDriverClassName("com.sybase.jdbc.SybDriver");
        setDriverURLHeader("jdbc:sybase:Tds:");
        // JConnect does not support the JDBC SQL grammar
        useNativeSQL();
    }

    /**
     * PUBLIC:
     * Set the database platform to be JDBC.
     */
    public void useJDBC() {
        DatabasePlatform newPlatform = new DatabasePlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Use the Borland JDBCConnect JDBC driver.
     */
    public void useJDBCConnectDriver() {
        setDriverClassName("borland.jdbc.Bridge.LocalDriver");
        setDriverURLHeader("jdbc:BorlandBridge:");
    }

    /**
     * PUBLIC:
     * Use the Borland JDBCConnect JDBC driver.
     */
    public void useJDBCConnectRemoteDriver() {
        setDriverClassName("borland.jdbc.Broker.RemoteDriver");
        setDriverURLHeader("jdbc:BorlandBridge:");
    }

    /**
     * PUBLIC:
     * User the Sun/Merant JDBC-ODBC bridge driver.
     */
    public void useJDBCODBCBridge() {
        setDriverClassName("sun.jdbc.odbc.JdbcOdbcDriver");
        setDriverURLHeader("jdbc:odbc:");
    }

    /**
     * PUBLIC:
     * Set the database platform to be MySQL.
     */
    public void useMySQL() {
        if (getPlatform().isMySQL()) {
            return;
        }

        DatabasePlatform newPlatform = new MySQL4Platform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use a sequence table
     * or native sequencing to generate unique object IDs.
     * Native sequencing uses the ID generation service provided by the database
     * (e.g. SEQUENCE objects on Oracle and IDENTITY columns on Sybase).
     * By default a sequence table is used. Using a sequence table is recommended
     * as it supports preallocation.
     * (Native sequencing on Sybase/SQL Server/Informix does not support preallocation.
     * Preallocation can be supported on Oracle by setting the increment size of the
     * SEQUENCE object to match the preallocation size.)
     */
    public void useNativeSequencing() {
        if(!shouldUseNativeSequencing()) {
            getPlatform().setDefaultSequence(new NativeSequence(getPlatform().getDefaultSequence().getName(), 
                    getPlatform().getDefaultSequence().getPreallocationSize(),
                    getPlatform().getDefaultSequence().getInitialValue()));
        }
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use database-specific SQL grammar,
     * as opposed to the JDBC standard grammar.
     * This is because, unfortunately, some drivers to not support the full JDBC standard.
     * By default TopLink uses the JDBC SQL grammar.
     */
    public void useNativeSQL() {
        setUsesNativeSQL(true);
    }

    /**
     * PUBLIC:
     * Set the database platform to be Oracle.
     */
    public void useOracle() {
        if (getPlatform().isOracle()) {
            return;
        }

        DatabasePlatform newPlatform = new OraclePlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Use the Oracle 7.x native OCI JDBC driver.
     */
    public void useOracle7JDBCDriver() {
        useOracle();
        setDriverClassName("oracle.jdbc.OracleDriver");
        setDriverURLHeader("jdbc:oracle:oci7:@");
        // Oracle works best with stream binding.
        useByteArrayBinding();
        useStreamsForBinding();
    }

    /**
     * PUBLIC:
     * Use the Oracle 8.x native OCI JDBC driver.
     */
    public void useOracleJDBCDriver() {
        useOracle();
        setDriverClassName("oracle.jdbc.OracleDriver");
        setDriverURLHeader("jdbc:oracle:oci8:@");
        // Oracle works best with stream binding.
        useByteArrayBinding();
        useStreamsForBinding();
    }

    /**
     * PUBLIC:
     * Use the Oracle server JDBC driver.
     */
    public void useOracleServerJDBCDriver() {
        useOracle();
        setDriverClassName("oracle.jdbc.OracleDriver");
        setDriverURLHeader("jdbc:oracle:kprb:");
        // Oracle works best with stream binding.
        useByteArrayBinding();
    }

    /**
     * PUBLIC:
     * Use the Oracle thin JDBC driver.
     */
    public void useOracleThinJDBCDriver() {
        useOracle();
        setDriverClassName("oracle.jdbc.OracleDriver");
        setDriverURLHeader("jdbc:oracle:thin:@");
        // Oracle works best with stream binding.
        useByteArrayBinding();
        useStreamsForBinding();
    }

    /**
     * PUBLIC:
     * Set the database platform to be PointBase.
     */
    public void usePointBase() {
        if (getPlatform().isPointBase()) {
            return;
        }

        DatabasePlatform newPlatform = new PointBasePlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Use the PointBase JDBC driver.
     */
    public void usePointBaseDriver() {
        usePointBase();
        setDriverClassName("com.pointbase.jdbc.jdbcUniversalDriver");
        setDriverURLHeader("jdbc:pointbase:");
    }

    /**
     * PUBLIC:
     * Set the database platform to be SQL Server.
     */
    public void useSQLServer() {
        if (getPlatform().isSQLServer()) {
            return;
        }

        DatabasePlatform newPlatform = new SQLServerPlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * TopLink can be configured to use streams to store large binary data.
     */
    public void useStreamsForBinding() {
        setUsesStreamsForBinding(true);
    }

    /**
     * PUBLIC:
     * Bind strings larger than 255 characters.
     */
    public void useStringBinding() {
        this.useStringBinding(255);
    }

    /**
     * PUBLIC:
     * Bind strings that are larger than the specified size.
     * Strings that are smaller will not be bound.
     */
    public void useStringBinding(int size) {
        getPlatform().setStringBindingSize(size);
        getPlatform().setUsesStringBinding(true);
    }

    /**
     * PUBLIC:
     * Set the database platform to be Sybase.
     */
    public void useSybase() {
        if (getPlatform().isSybase()) {
            return;
        }

        DatabasePlatform newPlatform = new SybasePlatform();
        getPlatform().copyInto(newPlatform);
        setPlatform(newPlatform);
    }

    /**
     * PUBLIC:
     * Set the prepare cursor code to what the WebLogic
     * Oracle OCI JDBC driver expects.
     */
    public void useWebLogicDriverCursoredOutputCode() {
        setCursorCode(1111);
    }

    /**
     * PUBLIC:
     * Set a WebLogic JDBC connection pool (a pool must be defined for the entity beans that are to be deployed)
     */
    public void useWebLogicJDBCConnectionPool(String poolName) {
        setDriverClassName("weblogic.jdbc.jts.Driver");
        setConnectionString("jdbc:weblogic:jts:" + poolName);
    }

    /**
     * PUBLIC:
     * Use the WebLogic Oracle OCI JDBC driver.
     */
    public void useWebLogicOracleOCIDriver() {
        useOracle();
        setDriverClassName("weblogic.jdbc.oci.Driver");
        setDriverURLHeader("jdbc:weblogic:oracle:");
        // WebLogic has a bug converting dates to strings, which our optimizations require.
        dontOptimizeDataConversion();
        useWebLogicDriverCursoredOutputCode();
    }

    /**
     * PUBLIC:
     * Use the WebLogic SQL Server dblib JDBC driver.
     */
    public void useWebLogicSQLServerDBLibDriver() {
        useSQLServer();
        setDriverClassName("weblogic.jdbc.dblib.Driver");
        setDriverURLHeader("jdbc:weblogic:mssqlserver:");
        // WebLogic has a bug converting dates to strings, which our optimizations require.
        dontOptimizeDataConversion();
    }

    /**
     * PUBLIC:
     * Use the WebLogic SQL Server JDBC driver.
     */
    public void useWebLogicSQLServerDriver() {
        useSQLServer();
        setDriverClassName("weblogic.jdbc.mssqlserver4.Driver");
        setDriverURLHeader("jdbc:weblogic:mssqlserver4:");
        // WebLogic has a bug converting dates to strings, which our optimizations require.
        dontOptimizeDataConversion();
    }

    /**
     * PUBLIC:
     * Use the WebLogic Sybase dblib JDBC driver.
     */
    public void useWebLogicSybaseDBLibDriver() {
        useSybase();
        setDriverClassName("weblogic.jdbc.dblib.Driver");
        setDriverURLHeader("jdbc:weblogic:sybase:");
        // WebLogic has a bug converting dates to strings, which our optimizations require.
        dontOptimizeDataConversion();
    }

    /**
     * PUBLIC:
     * Use the WebLogic thin client JDBC driver.
     */
    public void useWebLogicThinClientDriver() {
        setDriverClassName("weblogic.jdbc.t3Client.Driver");
        setDriverURLHeader("jdbc:weblogic:t3Client:");
    }

    /**
     * PUBLIC:
     * Use the WebLogic thin JDBC driver.
     */
    public void useWebLogicThinDriver() {
        setDriverClassName("weblogic.jdbc.t3.Driver");
        setDriverURLHeader("jdbc:weblogic:t3:");
    }
}
