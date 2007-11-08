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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/ClusterConfigFactory.java,v 1.4 2006/03/09 20:30:36 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2006/03/09 20:30:36 $
 */
package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Collections;

import javax.management.AttributeList;
import javax.management.ObjectName;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.enterprise.management.support.oldconfig.OldClustersMBean;

final class ClusterConfigFactory extends ConfigFactory
{
	private final OldClustersMBean	mOldClustersMBean;
	
		public
	ClusterConfigFactory(
		final ConfigFactoryCallback	callbacks )
	{
		super( callbacks );
		
		mOldClustersMBean	= getOldConfigProxies().getOldClustersMBean();
	}

		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs, 
		final Properties	props )
	{
		// YUCK--createCluster( AttributeList ) cannot be called;
		// it won't work correctly; we must make the call explicity.
		final Map<String,String>	m	= JMXUtil.attributeListToStringMap( translatedAttrs );
		
		final String	config	= (String)m.get( "config-ref" );
		final String	name	= (String)m.get( "name" );
		
		return mOldClustersMBean.createCluster( name, config, props );
	}
	
	private final static String	  REFERENCED_CONFIG_NAME	= "ReferencedConfigName";
	
		protected Map<String,String>
	getParamNameOverrides()
	{
		return( MapUtil.newMap( REFERENCED_CONFIG_NAME, "config-ref" ) );
	}
	        
		private void
	checkConfigExists( final String	configName )
	{
		final Map<String,ConfigConfig>	configs	= getDomainConfig().getConfigConfigMap();
		
		if( ! configs.keySet().contains( configName ) )
		{
			throw new IllegalArgumentException( "No ConfigConfig exists with the name: " + configName );
		}
	}
	
		private boolean
	clusterExists( final String name )
	{
		return getDomainConfig().getClusterConfigMap().keySet().contains( name );
	}
	
		public ObjectName
	create(
		final String name, 
		final String configName, 
		final Map<String,String> optional )
	{
		if ( configName != null )
		{
			checkNonEmptyString( configName, "configName" );
			checkConfigExists( configName );
			
			// This is explicitly disallowed
			if ( configName.equals( "server-config" ) || configName.equals( "default-config" ) )
			{
				throw new IllegalArgumentException( configName );
			}
		}
		
		if ( clusterExists( name )  )
		{
			throw new IllegalArgumentException( "Cluster already exists: " + quote( name ) );
		}

		
        final String[] requiredParams =
		{
			"ReferencedConfigName",		configName,
		};
		
		final Map<String,String> params	= initParams( name, requiredParams, optional );
		
		return createNamedChild( name, params );
	}
	
	// default-config to be used
		public ObjectName
	create(
		final String name, 
		final Map<String,String> optional )
	{
		return create( name, null, optional );
	}

    protected void internalRemove(final ObjectName objectName){
        final String name = Util.getName(objectName);
        if ( ! clusterExists( name ) )
		{
			throw new IllegalArgumentException( "No such cluster: " + quote( name ) );
		}
        mOldClustersMBean.deleteCluster(name);
    }
}





