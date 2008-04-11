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
package org.glassfish.admin.amx.dotted;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.AttributeNotFoundException;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerInvocationHandler;

import com.sun.appserv.management.util.misc.ArrayConversion;
import com.sun.appserv.management.client.ProxyFactory;

import com.sun.appserv.management.DomainRoot;

import com.sun.appserv.management.config.ServersConfig;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.ConfigsConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.base.Util;




/*
	This is the 'glue' that knows how to get server names, and how to get
	config names from server names, etc.  It isolates the DottedNameResolverForAliases
	code from direct knowledge of the server structure.
 */
public class DottedNameServerInfoImpl implements DottedNameServerInfo
{
	final MBeanServerConnection	mConn;
	
		public
	DottedNameServerInfoImpl( MBeanServerConnection conn )
	{
		mConn	= conn;
	}
	
		ObjectName
	getConfigsObjectName()
		throws MalformedObjectNameException
	{
		return( new ObjectName( "com.sun.appserv:type=configs,category=config" )  );
	}
	
		ObjectName
	getServerObjectName( final String serverName )
	{
        return Util.getObjectName( _getServers().get( serverName ) );
	}
	
        private DomainRoot
    getDomainRoot()
    {
        return ProxyFactory.getInstance( mConn ).getDomainRoot();
    }
    
		private Set<String>
	_getConfigNames()
		throws ReflectionException, InstanceNotFoundException, MBeanException, java.io.IOException,
		MalformedObjectNameException, AttributeNotFoundException
	{
        final ConfigsConfig configsConfig = getDomainRoot().getDomainConfig().getConfigsConfig();
        
        final Map<String,ConfigConfig> configs = configsConfig.getConfigConfigMap();
        
        return Collections.unmodifiableSet( configs.keySet() );
	}
	
		public Set<String>
	getConfigNames()
		throws DottedNameServerInfo.UnavailableException
	{
		Set<String>	namesSet	= null;
		
		try
		{
			namesSet	= _getConfigNames();
		}
		catch( Exception e )
		{
			throw new DottedNameServerInfo.UnavailableException( e );
		}
		
		return( namesSet );
	}
		
		private  Map<String,ServerConfig>
	_getServers()
	{
        final ServersConfig serversConfig = getDomainRoot().getDomainConfig().getServersConfig();
        
        final Map<String,ServerConfig> result = new HashMap<String,ServerConfig>();
        
        result.putAll( serversConfig.getStandaloneServerConfigMap() );
        result.putAll( serversConfig.getClusteredServerConfigMap() );
        
        return result;
    }
	
		public Set<String>
	getServerNames()
		throws DottedNameServerInfo.UnavailableException
	{
		Set<String>	namesSet	= null;
		
		try
		{
			namesSet	= _getServers().keySet();
		}
		catch( Exception e )
		{
			throw new DottedNameServerInfo.UnavailableException( e );
		}
		
		return( namesSet );
	}
	
		public String
	getConfigNameForServer( final String serverName )
		throws DottedNameServerInfo.UnavailableException
	{
		final ObjectName	serverObjectName	= getServerObjectName( serverName );
		
		if ( serverObjectName == null )
		{
			throw new DottedNameServerInfo.UnavailableException( serverName );
		}
		
		String	configName	= null;
		try
		{
			configName	= (String)mConn.getAttribute( serverObjectName, "config_ref" );
		}
		catch( Exception e )
		{
			throw new DottedNameServerInfo.UnavailableException( e );
		}
		
		return( configName );
	}
	
		public String []
	getServerNamesForConfig( String configName )
		throws DottedNameServerInfo.UnavailableException
	{
		final List<String>	namesOut	= new ArrayList<String>();
		
        for( final String serverName : getServerNames() )
		{
			if ( configName.equals( getConfigNameForServer( serverName ) ) )
			{
				namesOut.add( serverName );
			}
		}
		
		final String []	namesOutArray	= new String [ namesOut.size() ];
		namesOut.toArray( namesOutArray );
		
		return( namesOutArray );
	}
}









