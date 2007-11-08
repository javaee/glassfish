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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/ConnectorConnectionPoolConfigFactory.java,v 1.8 2007/05/21 23:25:10 llc Exp $
 * $Revision: 1.8 $
 * $Date: 2007/05/21 23:25:10 $
 */


package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.ObjectName;

import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ResourceAdapterConfig;
import com.sun.appserv.management.config.ConnectorModuleConfig;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.appserv.management.config.ConnectorConnectionPoolConfigKeys;
import com.sun.appserv.management.config.CommonConfigKeys;

/**
	MBean managing all instances of Connector resource.
 */

public final class ConnectorConnectionPoolConfigFactory  extends ResourceFactoryImplBase
	// implements ConnectorConnectionPoolConfigMgr
{
		public
	ConnectorConnectionPoolConfigFactory( final ConfigFactoryCallback	callbacks)
	{
		super( callbacks );
	}
     
		
	/**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
		return( getOldResourcesMBean().createConnectorConnectionPool( translatedAttrs ) );
	}
	
	
	public static final String	CONNECTION_DEFINITION_NAME_KEY	= "ConnectionDefinitionName";
	public static final String	RESOURCE_ADAPTER_NAME_KEY	= "ResourceAdapterName";
	
	
	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		ConnectorConnectionPoolConfigKeys.STEADY_POOL_SIZE_KEY,
		ConnectorConnectionPoolConfigKeys.MAX_POOL_SIZE_KEY,
		ConnectorConnectionPoolConfigKeys.MAX_WAIT_TIME_IN_MILLIS_KEY,
		ConnectorConnectionPoolConfigKeys.POOL_RESIZE_QUANTITY_KEY,
		ConnectorConnectionPoolConfigKeys.IDLE_TIMEOUT_IN_SECONDS_KEY,
		ConnectorConnectionPoolConfigKeys.FAIL_ALL_CONNECTIONS_KEY,
		ConnectorConnectionPoolConfigKeys.TRANSACTION_SUPPORT_KEY,
                ConnectorConnectionPoolConfigKeys.IS_CONNECTION_VALIDATION_REQUIRED_KEY,
		CommonConfigKeys.IGNORE_MISSING_REFERENCES_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
	
    private static final Set<String> IMPLICIT_RESOURCE_ADAPTERS    = GSetUtil.newStringSet( "jmsra", "jaxr-ra" );
        private void
    checkResourceAdapterExists( final String	resourceAdapterName )
    {
    /*
        I can't get solid information on what the requirements are for
        pre-existing items.  Removing this check.  Lloyd Chambers 5/31/07
        
        final DomainConfig domainConfig = getDomainRoot().getDomainConfig();

        final Map<String,ConnectorModuleConfig>	connectorModules	=
            getDomainRoot().getDomainConfig().getConnectorModuleConfigMap();
        if ( ! connectorModules.containsKey(resourceAdapterName) )
        {
            throw new IllegalArgumentException(
                "No ResourceAdapterConfig or " +
                "ConnectorModuleConfig exists with the name: " +
                resourceAdapterName );
        }
        */
    }
	
		public ObjectName
	create(
		final String	name,
		final String	resourceAdapterName,
		final String	connectionDefinitionName,
		final Map<String,String>		options )
	{
        final String[] requiredParams = new String[]
		{
			RESOURCE_ADAPTER_NAME_KEY,		resourceAdapterName,
			CONNECTION_DEFINITION_NAME_KEY,	connectionDefinitionName,
		};
		
		if ( requireValidReferences( options )  )
		{
			checkResourceAdapterExists( resourceAdapterName );
		}
		
		final Map<String,String> params	= initParams( name, requiredParams, options );
		
		final ObjectName	amxName	= createNamedChild( name, params );
		
		return( amxName );
	}
	
	
		protected void
	removeByName( final String name )
	{
		getOldResourcesMBean().removeConnectorConnectionPoolByName( name );	
    }				
}







