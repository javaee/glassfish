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

import java.util.Map;

/**
Configuration for the &lt;connector-connection-pool&gt; element.                                                  
<p>
connector-connection-pool defines configuration used to create    
and manage a pool of connections to a EIS. Pool definition is     
named, and can be referred to by multiple connector-resource      
elements (See connector-resource).                                                    
<p>                             
Each named pool definition results in a pool instantiated at server        
start-up. Pool is populated when accessed for the first time. If two or    
more connector-resource elements point to the same                         
connector-connection-pool element, they are using the same pool of         
connections, at run time.                                                  
<p>
There can be more than one pool for one connection-definition in one       
resource-adapter.    
 */
public interface ConnectorConnectionPool
        extends NamedConfigElement, Description, PropertiesAccess,
        ResourceRefReferent
{

    /**
    Specifies if the connection that is about to
    be returned is to be validated by the container.
     */
    
    public String getIsConnectionValidationRequired();

    /**
    See {@link #getConnectionValidationRequired}.
     */
    public void setIsConnectionValidationRequired(final String required);

    /**
    Unique name, identifying one connection-definition in a
    Resource Adapter. Currently this is ConnectionFactory type.
     */
    public String getConnectionDefinitionName();

    /**
    See {@link #getConnectionDefinitionName}.
     */
    public void setConnectionDefinitionName(final String value);

    /**
    Indicates if all connections in the pool must be closed
    should a single connection fail validation. The default is
    false. One attempt will be made to re-establish failed
    connections.
     */
    
    public String getFailAllConnections();

    /**
    See {@link #getFailAllConnections}.
     */
    public void setFailAllConnections(final String value);

    /**
    Maximum time in seconds, that a connection can remain idle in
    the pool. After this time, the pool implementation can close
    this connection. Note that this does not control connection
    timeouts enforced at the database server side. Adminsitrators
    are advised to keep this timeout shorter than the EIS
    connection timeout (if such timeouts are configured on the
    specific EIS), to prevent accumulation of unusable connection
    in Application Server.
     */
    
    public String getIdleTimeoutInSeconds();

    /**
    See {@link #getIdleTimeoutInSeconds}.
     */
    public void setIdleTimeoutInSeconds(final String value);

    /**
    Maximum number of conections that can be created.
     */
    
    public String getMaxPoolSize();

    /**
    See {@link #getMaxPoolSize}.
     */
    public void setMaxPoolSize(final String value);

    /**
    Amount of time the caller will wait before getting a
    connection timeout. The default is 60 seconds. A value of 0
    will force caller to wait indefinitely.
     */
    
    public String getMaxWaitTimeInMillis();

    /**
    See {@link #getMaxWaitTimeInMillis}.
     */
    public void setMaxWaitTimeInMillis(final String value);

    /**
    Number of connections to be removed when
    idle-timeout-in-seconds timer expires. Connections that have
    idled for longer than the timeout are candidates for removal.
    When the pool size reaches steady-pool-size, the connection
    removal stops.
     */
    
    public String getPoolResizeQuantity();

    /**
    See {@link #getPoolResizeQuantity}.
     */
    public void setPoolResizeQuantity(final String value);

    /**
    Name of resource adapter. Name of .rar file is
    taken as the unique name for the resource adapter.
     */
    public String getResourceAdapterName();

    /**
    See {@link #getResourceAdapterName}.
     */
    public void setResourceAdapterName(final String value);

    /**
    Minimum and initial number of connections maintained in the
    pool.
     */
    
    public String getSteadyPoolSize();

    /**
    See {@link #getSteadyPoolSize}.
     */
    public void setSteadyPoolSize(final String value);

    /**
    Indicates the level of transaction support that this pool
    will have. Possible values are "XATransaction",
    "LocalTransaction" and "NoTransaction". This attribute will
    override that transaction support attribute in the Resource
    Adapter in a downward compatible way, i.e it can support a
    lower/equal transaction level than specified in the RA, but
    not a higher level.

    @see TransactionSupportValues
     */
    public String getTransactionSupport();

    /**
    See {@link #getTransactionSupport}.
     */
    public void setTransactionSupport(final String value);

    /**
    @return Map of all SecurityMap contained in this item.
     */
    public Map<String, SecurityMap> getSecurityMap();

// 	/**
// 	    At least one of 'principals' and 'userGroups' must be non-null.
// 	 */
// 	    public SecurityMap
// 	createSecurityMapConfig(
// 	    final String    name,
// 	    final String    backendPrincipalUsername,
// 	    final String    backendPrincipalPassword,
// 	    final String[]  principals,
// 	    final String[]  userGroups );
// 	
// 	/**
// 	    Remove the specified SecurityMap.
// 	 */
// 	public void removeSecurityMapConfig( String name );
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
}





















