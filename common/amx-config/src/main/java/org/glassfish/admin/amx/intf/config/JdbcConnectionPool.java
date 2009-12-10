/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.amx.intf.config;

/**
Configuration for the &lt;jdbc-connection-pool&gt; element.
<p>
NOTE: some getters/setters use java.lang.String. This is a problem; these
methods cannot use the AppServer template facility, whereby an Attribute value can be of 
the form attr-name=${ATTR_VALUE}.  For an example of where/how this facility is used, see
the &lt;http-listener> element, which looks like this:<br/>
<pre>
&lt;http-listener id="http-listener-1" address="0.0.0.0" port="${HTTP_LISTENER_PORT}" acceptor-threads="1" security-enabled="false" default-virtual server="server" server-name="" xpowered-by="true" enabled="true">
</pre>
The 'port' attribute above is set to the value "${HTTP_LISTENER_PORT}", which is a system
property.  Obviously no method that uses 'String' could get or set a String.
 */
public interface JdbcConnectionPool
        extends NamedConfigElement, Description, PropertiesAccess, ResourceRefReferent
{
    /**
    Key for use with {@link ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)}

    See {@link ConnectionValidationMethodValues}.
     */
    public final static String CONNECTION_VALIDATION_METHOD_KEY = "connection-validation-method";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String VALIDATION_TABLE_NAME_KEY = "validation-table-name";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String DATASOURCE_CLASSNAME_KEY = "datasource-classname";
    /** Key for use with {@link ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)}  */
    public final static String FAIL_ALL_CONNECTIONS_KEY = "fail-all-connections";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String IDLE_TIMEOUT_IN_SECONDS_KEY = "idle-timeout-in-seconds";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String IS_CONNECTION_VALIDATION_REQUIRED_KEY = "is-connection-validation-required";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String IS_ISOLATION_LEVEL_GUARANTEED_KEY = "is-isolation-level-guaranteed";
    /**
    Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)}
    See {@link IsolationValues}.
     */
    public final static String TRANSACTION_ISOLATION_LEVEL_KEY = "transaction-isolation-level";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String MAX_POOL_SIZE_KEY = "max-pool-size";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String MAX_WAIT_TIME_MILLIS_KEY = "max-wait-time-in-millis";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String POOL_RESIZE_QUANTITY_KEY = "pool-resize-quantity";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String NON_TRANSACTIONAL_CONNECTIONS_KEY = "non-transactional-connections";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String ALLOW_NON_COMPONENT_CALLERS_KEY = "allow-non-component-callers";
    /**
    Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)}
    Possible values:
    <ul>
    <li>javax.sql.DataSource</li>
    <li>javax.sql.XADataSource</li>
    <li>javax.sql.ConnectionPoolDataSource</li>
    </ul>
     */
    public final static String RES_TYPE_KEY = "res-type";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String STEADY_POOL_SIZE_KEY = "steady-pool-size";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String DATABASE_NAME_KEY = "property.DatabaseName";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String DATABASE_USER_KEY = "property.User";
    /** Key for use with @link { ResourcesConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
    public final static String DATABASE_PASSWORD_KEY = "property.Password";

    public String getConnectionValidationMethod();

    public void setConnectionValidationMethod(String value);

    public String getDatasourceClassname();

    public void setDatasourceClassname(String value);

    
    public String getFailAllConnections();

    public void setFailAllConnections(String value);

    
    public String getIdleTimeoutInSeconds();

    public void setIdleTimeoutInSeconds(String value);

    
    public String getIsConnectionValidationRequired();

    public void setIsConnectionValidationRequired(String value);

    
    public String getIsIsolationLevelGuaranteed();

    public void setIsIsolationLevelGuaranteed(String value);

    
    public String getMaxPoolSize();

    public void setMaxPoolSize(String value);

    
    public String getMaxWaitTimeInMillis();

    public void setMaxWaitTimeInMillis(String value);

    
    public String getPoolResizeQuantity();

    public void setPoolResizeQuantity(String value);

    public String getResType();

    public void setResType(String value);

    
    public String getSteadyPoolSize();

    public void setSteadyPoolSize(String value);

    public String getTransactionIsolationLevel();

    /**
    See {@link IsolationValues}.
     */
    public void setTransactionIsolationLevel(String value);

    public String getValidationTableName();

    public void setValidationTableName(String value);

    /**                            
    A pool with this property set to true returns
    non-transactional connections. This connection does not get
    automatically enlisted with the transaction manager.

    @since AppServer 9.0
     */
    
    public String getNonTransactionalConnections();

    /**
    @see #getNonTransactionalConnections
    @since AppServer 9.0
     */
    public void setNonTransactionalConnections(String enabled);

    /**                                
    A pool with this property set to true, can be used by
    non-J2EE components (i.e components other than EJBs or
    Servlets). The returned connection is enlisted automatically
    with the transaction context obtained from the transaction
    manager. This property is to enable the pool to be used by
    non-component callers such as ServletFilters, Lifecycle
    modules, and 3rd party persistence managers. Standard J2EE
    components can continue to use such pools. Connections
    obtained by non-component callers are not automatically
    cleaned at the end of a transaction by the container. They
    need to be explicitly closed by the the caller.

    @since AppServer 9.0
     */
    
    public String getAllowNonComponentCallers();

    /**
    @see #getAllowNonComponentCallers
    @since AppServer 9.0
     */
    public void setAllowNonComponentCallers(String enabled);

    /**
    connection-leak-timeout-in-seconds (integer)<br>
    To aid user in detecting potential connection leaks by the application.
    When a connection is not returned back to the pool by the application
    within the specified period, it is assumed to be a potential leak and
    stack trace of the caller will be logged. Default is 0 seconds, which
    implies there is no leak detection, by default. A non-zero value turns
    on leak tracing.
    @since AppServer 9.1
     */
    
    String getConnectionLeakTimeoutInSeconds();

    /**
    @see #getConnectionLeakTimeoutInSeconds
    @since AppServer 9.1
     */
    void setConnectionLeakTimeoutInSeconds(String timeout);

    /**
    connection-leak-reclaim (String) <br>
    If enabled, connection will be re-usable (put back to pool) after
    connection-leak-timeout-in-seconds occurs. Default value is false.
    @since AppServer 9.1
     */
    
    String getConnectionLeakReclaim();

    /**
    @see #getConnectionLeakReclaim
    @since AppServer 9.1
     */
    void setConnectionLeakReclaim(String reclaim);

    /**
    connection-creation-retry-attempts (integer)<br>
    The number of attempts to create a new connection. Default is 0, which
    implies no retries.
    @since AppServer 9.1
     */
    
    String getConnectionCreationRetryAttempts();

    /**
    @see #getConnectionCreationRetryAttempts
    @since AppServer 9.1
     */
    void setConnectionCreationRetryAttempts(String count);

    /**
    connection-creation-retry-interval-in-seconds (integer) <br>
    The time interval between retries while attempting to create a connection
    Default is 10 seconds. Effective when connection-creation-retry-attempts is
    greater than 0.
    @since AppServer 9.1
     */
    
    String getConnectionCreationRetryIntervalInSeconds();

    /**
    @see #getConnectionCreationRetryIntervalInSeconds
    @since AppServer 9.1
     */
    void setConnectionCreationRetryIntervalInSeconds(String seconds);

    /**
    validate-atmost-once-period-in-seconds  (integer) <br>
    Used to set the time-interval within which a connection is validated atmost once.
    Default is 0 seconds, not enabled.
    @since AppServer 9.1
     */
    
    String getValidateAtmostOncePeriodInSeconds();

    /**
    @see #getValidateAtMostOncePeriodInSeconds
    @since AppServer 9.1
     */
    void setValidateAtmostOncePeriodInSeconds(String seconds);

    /**
    lazy-connection-enlistment (String)<br>
    Enlist a resource to the transaction only when it is actually used in
    a method, which avoids enlistment of connections, that are not used,
    in a transaction. This also prevents unnecessary enlistment of connections
    cached in the calling components. Default value is false.
    @since AppServer 9.1
     */
    
    String getLazyConnectionEnlistment();

    /**
    @see #getLazyConnectionEnlistment
    @since AppServer 9.1
     */
    void setLazyConnectionEnlistment(String enlist);

    /**
    lazy-connection-association (String)<br>
    Connections are lazily associated when an operation  is performed on
    them. Also they are disassociated when the transaction is completed
    and a component method ends, which helps to reuse the physical
    connections. Default value is false.
    @since AppServer 9.1
     */
    
    String getLazyConnectionAssociation();

    /**
    @see #getLazyConnectionAssociation
    @since AppServer 9.1
     */
    void setLazyConnectionAssociation(String associate);

    /**
    associate-with-thread (String)<br>
    Associate a connection with the thread such that when the
    same thread is in need of a connection, it can reuse the connection
    already associated with that thread, thereby not incurring the overhead
    of getting a connection from the pool. Default value is false.
    @since AppServer 9.1
     */
    
    String getAssociateWithThread();

    /**
    @see #getAssociateWithThread
    @since AppServer 9.1
     */
    void setAssociateWithThread(String associate);

    /**
    match-connections (String)<br>
    To switch on/off connection matching for the pool. It can be set to false if the
    administrator knows that the connections in the pool
    will always be homogeneous and hence a connection picked from the pool
    need not be matched by the resource adapter. Default value is true.
    @since AppServer 9.1
     */
    
    String getMatchConnections();

    /**
    @see #getMatchConnections
    @since AppServer 9.1
     */
    void setMatchConnections(String match);

    /**
    max-connection-usage-count<br>
    When specified, connections will be re-used by the pool for the specified number
    of times after which it will be closed. eg : To avoid statement-leaks.
    Default value is 0, which implies the feature is not enabled.
    @since AppServer 9.1
     */
    
    String getMaxConnectionUsageCount();

    /**
    @see #getMaxConnectionUsageCount
    @since AppServer 9.1
     */
    void setMaxConnectionUsageCount(String count);

    /**
    wrap-jdbc-objects (String)</br>
    When set to true, application will get wrapped jdbc objects for Statement,
    PreparedStatement, CallableStatement, ResultSet, DatabaseMetaData.
    @since AppServer 9.1
     */
    
    String getWrapJdbcObjects();

    /**
    @see #getWrapJdbcObjects
    @since AppServer 9.1
     */
    void setWrapJdbcObjects(String wrap);

    /**
    associate-with-thread (integer)<br>
    @since AppServer 9.1
     */
    
    String getStatementTimeoutInSeconds();

    /**
    associate-with-thread (integer)<br>
    @since AppServer 9.1
     */
    void setStatementTimeoutInSeconds(final String value);
    
    
    public String getStatementCacheSize();
    public void   setStatementCacheSize(String val);
    
    public String getSqlTraceListeners();
    public void   setSqlTraceListeners(String val);
    
    public String getValidationClassname();
    public void   setValidationClassname(String val);
    
    public String getPing();
    public void   setPing(String val);
    
    public String getPooling();
    public void   setPooling(String val);
    
    public String getInitSql();
    public void   setInitSql(String val);
    
    public String getDriverClassname();
    public void setDriverClassname(String val);
}





