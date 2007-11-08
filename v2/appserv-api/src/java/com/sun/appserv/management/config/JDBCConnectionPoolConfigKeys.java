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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/JDBCConnectionPoolConfigKeys.java,v 1.1 2006/12/02 06:03:10 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2006/12/02 06:03:10 $
 */


package com.sun.appserv.management.config;


/**
	Keys for use with {@link DomainConfig#createJDBCConnectionPoolConfig}.
	
	@see IsolationValues
 */
public final class JDBCConnectionPoolConfigKeys
{
	private	JDBCConnectionPoolConfigKeys()	{}
	
	/**
		Key for use with {@link DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)}
		
		See {@link ConnectionValidationMethodValues}.
	*/
	public final static String	CONNECTION_VALIDATION_METHOD_KEY= "ConnectionValidationMethod";
	
	
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	VALIDATION_TABLE_NAME_KEY       = "ValidationTableName";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	DATASOURCE_CLASSNAME_KEY		= "DatasourceClassname";
	/** Key for use with {@link DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)}  */
	public final static String	FAIL_ALL_CONNECTIONS_KEY		= "FailAllConnections";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	IDLE_TIMEOUT_IN_SECONDS_KEY		= "IdleTimeoutInSeconds";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	IS_CONNECTION_VALIDATION_REQUIRED_KEY= "IsConnectionValidationRequired";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	IS_ISOLATION_LEVEL_GUARANTEED_KEY= "IsIsolationLevelGuaranteed";
	/**
		Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)}
		See {@link IsolationValues}.
	*/
	public final static String	TRANSACTION_ISOLATION_LEVEL_KEY= "TransactionIsolationLevel";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	MAX_POOL_SIZE_KEY				= "MaxPoolSize";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	MAX_WAIT_TIME_MILLIS_KEY		= "MaxWaitTimeInMillis";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	POOL_RESIZE_QUANTITY_KEY		= "PoolResizeQuantity";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String NON_TRANSACTIONAL_CONNECTIONS_KEY = "NonTransactionalConnections";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String ALLOW_NON_COMPONENT_CALLERS_KEY = "AllowNonComponentCallers";

	/**
		Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)}
		Possible values:
		<ul>
		<li>javax.sql.DataSource</li>
		<li>javax.sql.XADataSource</li>
		<li>javax.sql.ConnectionPoolDataSource</li>
		</ul>
	*/
	public final static String	RES_TYPE_KEY					= "ResType";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	STEADY_POOL_SIZE_KEY			= "SteadyPoolSize";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	DATABASE_NAME_KEY				= "property.DatabaseName";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	DATABASE_USER_KEY				= "property.User";
	/** Key for use with @link { DomainConfig#createJDBCConnectionPoolConfig(String, String, Map)} */
	public final static String	DATABASE_PASSWORD_KEY			= "property.Password";
	
	
}
