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
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Properties;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.config.ServerConfigKeys;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.config.ServerRefConfigCR;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.appserv.management.config.ClusterConfig;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;


import com.sun.enterprise.management.support.oldconfig.OldServersMBean;

public final class ClusteredServerConfigFactory extends StandaloneServerConfigFactory
{
		public
	ClusteredServerConfigFactory( final ConfigFactoryCallback	callbacks )
	{
		super( callbacks );
	}
	
	    private boolean
	getLBEnabled( final ClusterConfig clusterConfig )
	{
	    final Map<String,ServerRefConfig>	refs    = clusterConfig.getServerRefConfigMap();
	    
	    boolean enabled = false;
	    // take lb-enabled from the first server
	    if ( refs.keySet().size() != 0 )
	    {
	        final ServerRefConfig   first   = refs.values().iterator().next();
	        enabled = first.getLBEnabled();
	    }
	    
	    return enabled;
	}
	
		public ObjectName
	create(
		final String serverName,
		final String clusterName,
		final String nodeAgentName,
		final Map<String,String> optional)
	{
	    final ClusterConfig clusterConfig   =
	        getDomainConfig().getClusterConfigMap().get( clusterName );
	    if ( clusterConfig == null )
	    {
	        throw new IllegalArgumentException( "" + clusterName );
	    }
	    
	    final String configName = clusterConfig.getReferencedConfigName();
	    
	    debug( "creating server:\n" + (optional == null ? "null" : MapUtil.toString( optional )) );
	    final ObjectName    serverObjectName =
	        super.create( serverName, nodeAgentName, configName, optional );
	    
	    // now create a reference.
	    debug( "created server: " + serverName + " in cluster " + clusterName +
	        "with node agent " + nodeAgentName );
	    final Map<String,String>    options = new HashMap<String,String>();
	    options.put( ServerRefConfigCR.ENABLED_KEY, "true" );
	    options.put( ServerRefConfigCR.LB_ENABLED_KEY, "" + getLBEnabled(clusterConfig) );
	    debug( "creating reference" );
	    final ServerRefConfig   refConfig =
	        clusterConfig.createServerRefConfig( serverName, options );
	    debug( "created reference" );
	    
	    // Now wait until internal code mutates the STANDALONE_SERVER_CONFIG into
	    // a CLUSTERED_SERVER_CONFIG
	    final String props  = Util.makeRequiredProps( XTypes.CLUSTERED_SERVER_CONFIG, serverName );
	    Set<ClusteredServerConfig>    s = null;
	    while( (s = getQueryMgr().queryPropsSet( props )).size() == 0 )
	    {
	        try
	        {
	            Thread.sleep( 20 );
	        }
	        catch( InterruptedException e )
	        {
	            throw new RuntimeException( e );
	        }
	    }
	        
	    return Util.getObjectName( s.iterator().next() );
	}           


        private ClusterConfig
    findClusterForClusteredServer( final String clusteredServerName )
    {
	    // find the cluster referencing this server
	    ClusterConfig clusterConfig = null;
	    
	    final Map<String,ClusterConfig> clusters    = getDomainConfig().getClusterConfigMap();
	    for( final ClusterConfig cc : clusters.values() )
	    {
	        final Map<String,ServerRefConfig>   servers = cc.getServerRefConfigMap();
	        
	        if ( servers.keySet().contains( clusteredServerName ) )
	        {
	            clusterConfig   = cc;
	            break;
	        }
	    }
	    
	    return clusterConfig;
    }
    
		protected void
	removeByName(final String clusteredServerName)
	{
        // find the cluster referencing this server
        final ClusterConfig clusterConfig =
            findClusterForClusteredServer( clusteredServerName );
        if ( clusterConfig == null )
        {
            throw new IllegalArgumentException( "" + clusteredServerName );
        }
        
	    // removal of the <server-ref> is apparently implied...
	    super.removeByName( clusteredServerName );
	}
}



















