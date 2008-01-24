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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/ConnectorConnectionPoolConfigKeys.java,v 1.2 2007/05/05 05:30:32 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:32 $
 */
package com.sun.appserv.management.config;

/**
	Keys for use with {@link DomainConfig#createConnectorConnectionPoolConfig}.
 */
public final class ConnectorConnectionPoolConfigKeys extends CommonConfigKeys
{
	private	ConnectorConnectionPoolConfigKeys()	{}
	/**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.String.
	*/
	public static final String	STEADY_POOL_SIZE_KEY		= "SteadyPoolSize";

	/**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.String.
	*/
	public static final String	MAX_POOL_SIZE_KEY		    = "MaxPoolSize";

	/**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.String.
	*/
	public static final String	MAX_WAIT_TIME_IN_MILLIS_KEY	= "MaxWaitTimeInMillis";

	/**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.String.
	*/
	public static final String	POOL_RESIZE_QUANTITY_KEY	= "PoolResizeQuantity";

	/**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.String.
	*/
	public static final String	IDLE_TIMEOUT_IN_SECONDS_KEY	= "IdleTimeoutInSeconds";

	/**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.Boolean.
	*/
	public static final String	FAIL_ALL_CONNECTIONS_KEY		= "FailAllConnections";
	
        /**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.Boolean.
	*/
	public static final String	IS_CONNECTION_VALIDATION_REQUIRED_KEY	= "IsConnectionValidationRequired";
	
	/**
		Key for use with {@link DomainConfig#createConnectorConnectionPoolConfig}, value must be java.lang.String.
		
		                                   
        Indicates the level of transaction support that this pool     
        will have. Possible values are as defined in {@link TransactionSupportValues}.
        This attribute will   
        override that transaction support attribute in the Resource   
        Adapter in a downward compatible way, i.e it can support a    
        lower/equal transaction level than specified in the RA, but   
        not a higher level.
        @see ConnectorConnectionPoolConfig#setTransactionSupport      
	*/
	public static final String	TRANSACTION_SUPPORT_KEY		= "TransactionSupport";
}









