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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/JDBCConnectionPoolConfigFactory.java,v 1.6 2006/10/06 19:37:32 anilam Exp $
 * $Revision: 1.6 $
 * $Date: 2006/10/06 19:37:32 $
 */


package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.Map;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;
	
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.appserv.management.config.JDBCConnectionPoolConfigKeys;
import com.sun.appserv.management.config.ResourceConfigKeys;

/**
	MBean managing all instances of JDBC resource.
 */

public final class JDBCConnectionPoolConfigFactory  extends ResourceFactoryImplBase
{
		public
	JDBCConnectionPoolConfigFactory( final ConfigFactoryCallback	callbacks )
	{
		super( callbacks );
	}

	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		JDBCConnectionPoolConfigKeys.CONNECTION_VALIDATION_METHOD_KEY,
		JDBCConnectionPoolConfigKeys.VALIDATION_TABLE_NAME_KEY,
		JDBCConnectionPoolConfigKeys.FAIL_ALL_CONNECTIONS_KEY,
		JDBCConnectionPoolConfigKeys.IDLE_TIMEOUT_IN_SECONDS_KEY,
		JDBCConnectionPoolConfigKeys.IS_CONNECTION_VALIDATION_REQUIRED_KEY,
		JDBCConnectionPoolConfigKeys.IS_ISOLATION_LEVEL_GUARANTEED_KEY,
		JDBCConnectionPoolConfigKeys.TRANSACTION_ISOLATION_LEVEL_KEY,
		JDBCConnectionPoolConfigKeys.MAX_POOL_SIZE_KEY,
		JDBCConnectionPoolConfigKeys.MAX_WAIT_TIME_MILLIS_KEY,
		JDBCConnectionPoolConfigKeys.POOL_RESIZE_QUANTITY_KEY,
		JDBCConnectionPoolConfigKeys.RES_TYPE_KEY,
		JDBCConnectionPoolConfigKeys.STEADY_POOL_SIZE_KEY,
                JDBCConnectionPoolConfigKeys.NON_TRANSACTIONAL_CONNECTIONS_KEY,
		JDBCConnectionPoolConfigKeys.ALLOW_NON_COMPONENT_CALLERS_KEY,
		JDBCConnectionPoolConfigKeys.DATABASE_NAME_KEY,
		JDBCConnectionPoolConfigKeys.DATABASE_USER_KEY,
		JDBCConnectionPoolConfigKeys.DATABASE_PASSWORD_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
                
                
        /**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
		trace( "JDBCConnectionPoolConfigFactory.createOldChildConfig: creating using: " +
			stringify( translatedAttrs ) );
			
		final ObjectName	objectName	=
				getOldResourcesMBean().createJdbcConnectionPool( translatedAttrs );
		
		return( objectName );
	}

           
         /**
		Create a new &lt;jdbc-connection-pool>. Name and datasource classname 
		are required parameters.
		
		@param name name of the &lt;jdbc-connection-pool&gt; to be created
		@param datasourceClassname vendor supplied JDBC resource manager such
		       as javax.sql.XADatasource interface.
		@param optional optional parameters for jdbc connection pool creation
	 */
		public ObjectName
	create( 
                final String    name, 
                final String    datasourceClassname,
                final Map<String,String> optional)
	{
        final String[] requiredParams = new String[]
		{
			JDBCConnectionPoolConfigKeys.DATASOURCE_CLASSNAME_KEY,       datasourceClassname,
		};
                
		final Map<String,String> params	= initParams( name, requiredParams, optional );
		
		final ObjectName	amxName = createNamedChild( name, params );
		
		return( amxName );
	}
                
                
		public ObjectName
	create(
		final String name,
		final String connectionValidationMethod,
		final String datasourceClassname,
		final boolean	failAllConnections,
		final int		idleTimeoutSeconds,
		final boolean	connectionValidationRequired,
		final boolean	isolationLevelGuaranteed,
		final String	transactionIsolationLevel,
		final int		maxPoolSize,
		final int		maxWaitTimeMillis,
		final int		poolResizeQuantity,
		final String	resType,
		final int		steadyPoolSize,
		final String	databaseName,
		final String	databaseUserName,
		final String	databasePassword,
		final Map<String,String>		reserved )
	{
		final Map<String,String> optionalParams = new java.util.HashMap<String,String>();
		optionalParams.put(JDBCConnectionPoolConfigKeys.CONNECTION_VALIDATION_METHOD_KEY,connectionValidationMethod);
		optionalParams.put(JDBCConnectionPoolConfigKeys.FAIL_ALL_CONNECTIONS_KEY,Boolean.toString(failAllConnections));
		optionalParams.put(JDBCConnectionPoolConfigKeys.IDLE_TIMEOUT_IN_SECONDS_KEY,Integer.toString(idleTimeoutSeconds));
		optionalParams.put(JDBCConnectionPoolConfigKeys.IS_CONNECTION_VALIDATION_REQUIRED_KEY,Boolean.toString(connectionValidationRequired));
		optionalParams.put(JDBCConnectionPoolConfigKeys.IS_ISOLATION_LEVEL_GUARANTEED_KEY,Boolean.toString(isolationLevelGuaranteed));
		optionalParams.put(JDBCConnectionPoolConfigKeys.TRANSACTION_ISOLATION_LEVEL_KEY, transactionIsolationLevel);
		optionalParams.put(JDBCConnectionPoolConfigKeys.MAX_POOL_SIZE_KEY,Integer.toString(maxPoolSize));
		optionalParams.put(JDBCConnectionPoolConfigKeys.MAX_WAIT_TIME_MILLIS_KEY,Integer.toString(maxWaitTimeMillis));
		optionalParams.put(JDBCConnectionPoolConfigKeys.POOL_RESIZE_QUANTITY_KEY,Integer.toString(poolResizeQuantity));
		optionalParams.put(JDBCConnectionPoolConfigKeys.RES_TYPE_KEY,resType);
		optionalParams.put(JDBCConnectionPoolConfigKeys.STEADY_POOL_SIZE_KEY,Integer.toString(steadyPoolSize));
		optionalParams.put(JDBCConnectionPoolConfigKeys.DATABASE_NAME_KEY,databaseName);
		optionalParams.put(JDBCConnectionPoolConfigKeys.DATABASE_USER_KEY,databaseUserName);
		optionalParams.put(JDBCConnectionPoolConfigKeys.DATABASE_PASSWORD_KEY,databasePassword);

		optionalParams.putAll( reserved );

		final ObjectName amxName =  create( name, datasourceClassname, optionalParams);
		return( amxName );
	}

		protected void
	removeByName( final String name )
	{
		getOldResourcesMBean().removeJdbcConnectionPoolByName( name );
	}

}

