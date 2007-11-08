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
 * $Header: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/dottedname/DottedNameServerInfoImpl.java,v 1.4 2007/01/04 23:25:17 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2007/01/04 23:25:17 $
 */


package com.sun.enterprise.admin.dottedname;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.AttributeNotFoundException;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

import com.sun.enterprise.admin.util.ArrayConversion;

import com.sun.enterprise.admin.common.ObjectNames;


import javax.management.MBeanServerInvocationHandler;


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
	getControllerObjectName()
	{
		return( ObjectNames.getControllerObjectName() );
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
		return( ObjectNames.getServerObjectName( serverName ) );
	}
	
	
		Set
	_getConfigNames()
		throws ReflectionException, InstanceNotFoundException, MBeanException, java.io.IOException,
		MalformedObjectNameException, AttributeNotFoundException
	{
		// we can't use a proxy; it won't work when the method name starts with "get", apparently
		// thinking it's an Attribute
		final ObjectName []	configObjectNames	=
			(ObjectName [])mConn.invoke( getConfigsObjectName(), "getConfig", null, null );
		
		final HashSet	configNames	= new HashSet();
		for( int i = 0; i < configObjectNames.length; ++i )
		{
			final String	name	= (String)mConn.getAttribute( configObjectNames[ i ], "name" );
			
			configNames.add( name );
		}
		
		return( configNames );
	}
	
		public Set
	getConfigNames()
		throws DottedNameServerInfo.UnavailableException
	{
		Set	namesSet	= null;
		
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
	
	// used to create a proxy to controller mbean
	private interface MyController
	{
		String []	listServerInstances();
	};
	
		protected Set
	_getServerNames()
		throws ReflectionException, InstanceNotFoundException, MBeanException, java.io.IOException
	{
		final MyController	controller	= (MyController)
			MBeanServerInvocationHandler.newProxyInstance( mConn, getControllerObjectName(), MyController.class, false );
		
		final String []	names	= controller.listServerInstances();
		
		return( ArrayConversion.toSet( names ) );
	}
	
		public Set
	getServerNames()
		throws DottedNameServerInfo.UnavailableException
	{
		Set	namesSet	= null;
		
		try
		{
			namesSet	= _getServerNames();
		}
		catch( Exception e )
		{
			throw new DottedNameServerInfo.UnavailableException( e );
		}
		
		return( namesSet );
	}
	
		public String
	getConfigNameForServer( String serverName )
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
		final java.util.Iterator iter			= getServerNames().iterator();
		final java.util.ArrayList	namesOut	= new java.util.ArrayList();
		
		while ( iter.hasNext() )
		{
			final String	serverName	= (String)iter.next();
			
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









