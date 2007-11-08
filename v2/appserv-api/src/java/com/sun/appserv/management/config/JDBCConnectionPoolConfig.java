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
 
/*
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/JDBCConnectionPoolConfig.java,v 1.3 2007/01/04 18:18:09 anilam Exp $
 * $Revision: 1.3 $
 * $Date: 2007/01/04 18:18:09 $
 */

package com.sun.appserv.management.config;

import com.sun.appserv.management.base.XTypes;




/**
	 Configuration for the &lt;jdbc-connection-pool&gt; element.
     <p>
     NOTE: some getters/setters use java.lang.boolean. This is a problem; these
     methods cannot use the AppServer template facility, whereby an Attribute value can be of 
     the form attr-name=${ATTR_VALUE}.  For an example of where/how this facility is used, see
     the &lt;http-listener> element, which looks like this:<br/>
<pre>
&lt;http-listener id="http-listener-1" address="0.0.0.0" port="${HTTP_LISTENER_PORT}" acceptor-threads="1" security-enabled="false" default-virtual server="server" server-name="" xpowered-by="true" enabled="true">
</pre>
    The 'port' attribute above is set to the value "${HTTP_LISTENER_PORT}", which is a system
    property.  Obviously no method that uses 'boolean' could get or set a String.
*/

public interface JDBCConnectionPoolConfig
    extends NamedConfigElement, Description, PropertiesAccess, ResourceRefConfigReferent
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE			= XTypes.JDBC_CONNECTION_POOL_CONFIG;
	
	public String	getConnectionValidationMethod();
	public void	setConnectionValidationMethod( String value );

	public String	getDatasourceClassname();
	public void	setDatasourceClassname( String value );

	public boolean	getFailAllConnections();
	public void	setFailAllConnections( boolean value );

	public String	getIdleTimeoutInSeconds();
	public void	setIdleTimeoutInSeconds( String value );

	public boolean	getIsConnectionValidationRequired();
	public void	setIsConnectionValidationRequired( boolean value );

	public boolean	getIsIsolationLevelGuaranteed();
	public void	setIsIsolationLevelGuaranteed( boolean value );

	public String	getMaxPoolSize();
	public void	setMaxPoolSize( String value );

	public String	getMaxWaitTimeInMillis();
	public void	setMaxWaitTimeInMillis( String value );

	public String	getPoolResizeQuantity();
	public void	setPoolResizeQuantity( String value );

	public String	getResType();
	public void	setResType( String value );

	public String	getSteadyPoolSize();
	public void	setSteadyPoolSize( String value );

	public String	getTransactionIsolationLevel();
	/**
		See {@link IsolationValues}.
	 */
	public void	setTransactionIsolationLevel( String value );

	public String	getValidationTableName();
	public void	setValidationTableName( String value );

    /**                            
        A pool with this property set to true returns                 
        non-transactional connections. This connection does not get   
        automatically enlisted with the transaction manager.
        
        @since AppServer 9.0
     */
    public boolean getNonTransactionalConnections();
    
    /**
        @see #getNonTransactionalConnections          
        @since AppServer 9.0
     */
    public void setNonTransactionalConnections( boolean enabled );
    
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
    public boolean getAllowNonComponentCallers();
    
    /**
        @see #getAllowNonComponentCallers          
        @since AppServer 9.0
     */
    public void setAllowNonComponentCallers( boolean enabled );


    
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
    String  getConnectionLeakTimeoutInSeconds();
    
    /**
        @see #getConnectionLeakTimeoutInSeconds
        @since AppServer 9.1
     */
    void    setConnectionLeakTimeoutInSeconds( String timeout );
    
    /**
        connection-leak-reclaim (boolean) <br>
         If enabled, connection will be re-usable (put back to pool) after  
         connection-leak-timeout-in-seconds occurs. Default value is false.
         @since AppServer 9.1
     */
    String     getConnectionLeakReclaim();
    
    /**
        @see #getConnectionLeakReclaim
        @since AppServer 9.1
     */
    void       setConnectionLeakReclaim( String reclaim );
         
    /**
        connection-creation-retry-attempts (integer)<br>
         The number of attempts to create a new connection. Default is 0, which 
         implies no retries.
         @since AppServer 9.1
     */
    String     getConnectionCreationRetryAttempts();
    
    /**
        @see #getConnectionCreationRetryAttempts
        @since AppServer 9.1
     */
    void       setConnectionCreationRetryAttempts( String count );
         
    /**
        connection-creation-retry-interval-in-seconds (integer) <br>
         The time interval between retries while attempting to create a connection
         Default is 10 seconds. Effective when connection-creation-retry-attempts is 
         greater than 0.
         @since AppServer 9.1
     */
    String     getConnectionCreationRetryIntervalInSeconds();
    
    /**
        @see #getConnectionCreationRetryIntervalInSeconds
        @since AppServer 9.1
     */
    void       setConnectionCreationRetryIntervalInSeconds( String seconds );
         
    /**
        validate-atmost-once-period-in-seconds  (integer) <br>
         Used to set the time-interval within which a connection is validated atmost once. 
         Default is 0 seconds, not enabled.
         @since AppServer 9.1
     */
    String     getValidateAtMostOncePeriodInSeconds();
    
    /**
        @see #getValidateAtMostOncePeriodInSeconds
        @since AppServer 9.1
     */
    void       setValidateAtMostOncePeriodInSeconds( String seconds );
         
    /**
     lazy-connection-enlistment (boolean)<br>
         Enlist a resource to the transaction only when it is actually used in 
         a method, which avoids enlistment of connections, that are not used, 
         in a transaction. This also prevents unnecessary enlistment of connections
         cached in the calling components. Default value is false.
         @since AppServer 9.1
     */
    String     getLazyConnectionEnlistment();
    
    /**
        @see #getLazyConnectionEnlistment
        @since AppServer 9.1
     */
    void       setLazyConnectionEnlistment( String enlist );
         
    /**
        lazy-connection-association (boolean)<br>
         Connections are lazily associated when an operation  is performed on 
         them. Also they are disassociated when the transaction is completed 
         and a component method ends, which helps to reuse the physical 
         connections. Default value is false.
         @since AppServer 9.1
     */
    String     getLazyConnectionAssociation();
    
    /**
        @see #getLazyConnectionAssociation
        @since AppServer 9.1
     */
    void       setLazyConnectionAssociation( String associate);
         
    /**
        associate-with-thread (boolean)<br>
        Associate a connection with the thread such that when the
        same thread is in need of a connection, it can reuse the connection 
        already associated with that thread, thereby not incurring the overhead 
        of getting a connection from the pool. Default value is false.
         @since AppServer 9.1
     */
    String     getAssociateWithThread();
    
    /**
        @see #getAssociateWithThread
        @since AppServer 9.1
     */

    void       setAssociateWithThread( String associate);

         
    /**
        match-connections (boolean)<br>
        To switch on/off connection matching for the pool. It can be set to false if the
        administrator knows that the connections in the pool
        will always be homogeneous and hence a connection picked from the pool
        need not be matched by the resource adapter. Default value is true.
         @since AppServer 9.1
     */
    String     getMatchConnections();
    
    /**
        @see #getMatchConnections
        @since AppServer 9.1
     */

    void       setMatchConnections( String match );
         
    /**
        max-connection-usage-count<br>
        When specified, connections will be re-used by the pool for the specified number 
        of times after which it will be closed. eg : To avoid statement-leaks. 
        Default value is 0, which implies the feature is not enabled.
         @since AppServer 9.1
     */
    String     getMaxConnectionUsageCount();
    
    /**
        @see #getMaxConnectionUsageCount
        @since AppServer 9.1
     */

    void       setMaxConnectionUsageCount( String count);

    
    /**
       wrap-jdbc-objects (boolean)</br>
       When set to true, application will get wrapped jdbc objects for Statement,
       PreparedStatement, CallableStatement, ResultSet, DatabaseMetaData. 
         @since AppServer 9.1
     */
     String     getWrapJDBCObjects();
     
     /**
        @see #getWrapJDBCObjects
         @since AppServer 9.1
      */
     void       setWrapJDBCObjects( String wrap );

    
    /**
        associate-with-thread (integer)<br>
        @since AppServer 9.1
     */
    String     getStatementTimeoutInSeconds();
    
    /**
        associate-with-thread (integer)<br>
        @since AppServer 9.1
     */
    void     setStatementTimeoutInSeconds( final String value );


}
