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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/com/sun/enterprise/management/config/StandaloneServerConfigTest.java,v 1.8 2006/11/13 17:14:53 llc Exp $
 * $Revision: 1.8 $
 * $Date: 2006/11/13 17:14:53 $
 */
package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.Arrays;

import javax.management.ObjectName;


import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.AppserverConnectionSource;

import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.NamedConfigElement;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.NodeAgentConfig;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;


/**
 */
public final class StandaloneServerConfigTest extends AMXTestBase
{
		public
	StandaloneServerConfigTest()
	{
	    if ( checkNotOffline( "ensureDefaultInstance" ) )
	    {
	        ensureDefaultInstance( getDomainRoot() );
	    }
	}
	
	    public static String
    getDefaultInstanceName()
    {
        return "server";
    }
	
	/**
	    We want the default instance to be available on both PE and EE
	    so we have no choice but to use the DAS instance.
	 */
        public static StandaloneServerConfig
	ensureDefaultInstance( final DomainRoot domainRoot )
	{
	    final Map<String,StandaloneServerConfig>    servers =
	        domainRoot.getDomainConfig().getStandaloneServerConfigMap();
	    
	    StandaloneServerConfig  server  = servers.get( getDefaultInstanceName() );
	    assert( server != null );
	    
	    return server;
	}
	
	    public static Capabilities
	getCapabilities()
	{
	    return getOfflineCapableCapabilities( false );
	}
	
        
	    private void
	_testCreateStandaloneServerConfig( final String serverNameSuffix, final int basePort )
	{
        final ConfigSetup setup  = new ConfigSetup( getDomainRoot() );
        
        final Map<String,NodeAgentConfig> nodeAgentConfigs    = getDomainConfig().getNodeAgentConfigMap();
        
        if ( nodeAgentConfigs.keySet().size() == 0 )
        {
            warning( "testCreateStandaloneServerConfig: No node agents available, skipping test." );
        }
        else
        {
            // create a server for each node agent
            for( final String nodeAgentName : nodeAgentConfigs.keySet() )
            {
                final String serverName    = nodeAgentName + serverNameSuffix;
                final String configName    = serverName + "-config";
                
                // in case a previous failed run left them around
                setup.removeServer( serverName );
                setup.removeConfig( configName );
                
                final ConfigConfig  config  = setup.createConfig( configName );
                assert( configName.equals( config.getName() ) );
                
                // sanity check
                final Map<String,Object>    attrs   = Util.getExtra( config ).getAllAttributes();
                
                try
                {
                    final StandaloneServerConfig  server =
                        setup.createServer( serverName, basePort, nodeAgentName, config.getName() );
                    // it worked, get rid of it
                    setup.removeServer( server.getName() );
                }
                catch( Throwable t)
                {
                    assert false : ExceptionUtil.toString( t );
                }
                finally
                {
                    try
                    {
                        setup.removeConfig( config.getName() );
                    }
                    catch( Exception ee )
                    {
                        // we wanted to get rid of it...oh well.
                    }
                }
            }
        }
	}
    
       public void
	testCreateStandaloneServerConfigWithDefaults()
	{
        final int basePort = 0; // use the defaults
        
        _testCreateStandaloneServerConfig( ".StandaloneServerConfigTestWithDefaults", basePort);
	}


	    public void
	testCreateStandaloneServerConfig()
	{
        final int basePort = 52788;
        
        _testCreateStandaloneServerConfig( ".StandaloneServerConfigTest", basePort );
    }

}

























