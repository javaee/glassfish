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
package org.glassfish.admin.amx.impl.mbean;

import org.glassfish.admin.amx.core.AMXConstants;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.core.proxy.AMXProxyHandler;

import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.ObjectNameQueryImpl;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.RegexUtil;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 */
public class QueryMgrImpl extends AMXImplBase
	// implements Query
{
		public
	QueryMgrImpl(final ObjectName parentObjectName)
	{
        super( parentObjectName, Query.class );
	}

	    private static Set<ObjectName>
	checked( final Set<ObjectName> s )
	{
	    return Collections.checkedSet(s, ObjectName.class);
	}
	
	/**
	 */
		public static Set<ObjectName>
	queryPropsObjectNameSet(	
		final MBeanServerConnection		conn,
		final String					jmxDomainName,
		final String					props )
		throws IOException
	{
		final ObjectName	pattern		=
				Util.newObjectNamePattern( jmxDomainName, props );
		
		final Set<ObjectName>	names	= JMXUtil.queryNames( conn, pattern, null );
		
		return( checked(names) );
	}
	
	/**
	 */
		public static Set<ObjectName>
	queryPropsObjectNameSet(	
		final MBeanServer			server,
		final String				jmxDomainName,
		final String				props )
	{
		final ObjectName	pattern		=
				Util.newObjectNamePattern( jmxDomainName, props );
		
		final Set<ObjectName>	names	= JMXUtil.queryNames( server, pattern, null );
		
		return( checked(names) );
	}
	
		public Set<ObjectName>
	queryPropsObjectNameSet( final String props )
	{
		final String	myDomain	= getObjectName().getDomain();
		
		final Set<ObjectName>	names	=
			queryPropsObjectNameSet( getMBeanServer(), myDomain, props );
		
		return( checked(names) );
	}
	
		public ObjectName
	querySingletonJ2EETypeObjectName( final String type )
	{
		return( querySingletonJ2EETypeObjectName( getMBeanServer(), getObjectName().getDomain(), type ) );
	}
	
		public static ObjectName
	querySingletonJ2EETypeObjectName( 
		final MBeanServer	server,
		final String		domainName,
		final String		typeValue )
	{
		try
		{
			final ObjectName	objectName	=
								querySingletonJ2EETypeObjectName(
									(MBeanServerConnection)server,
									domainName,
									typeValue );
			return( objectName );
		}
		catch( IOException e )
		{
			assert( false );
			throw new RuntimeException( e );
		}
	}
	
		public static ObjectName
	querySingletonJ2EETypeObjectName(
		final MBeanServerConnection	conn,
		final String					domainName,
		final String					typeValue )
		throws IOException
	{
		final Set<ObjectName>	names	=
		    queryJ2EETypeObjectNameSet( conn, domainName, typeValue );
		if ( names.size() != 1 )
		{
			throw new IllegalArgumentException(
				"request was for a single Object name of type: " + typeValue +
				" but found several: " + SmartStringifier.toString( names ) );
		}
		
		final ObjectName	objectName	=SetUtil.getSingleton( names );
		
		return( objectName );
	}
	

	/**
	 */
		public static Set<ObjectName>
	queryJ2EETypeObjectNameSet(
		final MBeanServerConnection	conn,
		final String				domainName,
		final String				typeValue )
		throws IOException
	{
		final String		prop	= Util.makeTypeProp( typeValue ) ;
		final ObjectName	pat		= Util.newObjectNamePattern( domainName, prop );
		
		final Set<ObjectName>	names	= queryPatternObjectNameSet( conn, pat );
		
		return( checked(names) );
	}
	
	/**
	 */
		public Set<ObjectName>
	queryJ2EETypesObjectNameSet( final Set<String> types )
		throws IOException
	{
        final Set<ObjectName> result    = new HashSet<ObjectName>();
        
        for( final String type : types )
        {
            result.addAll( queryJ2EETypeObjectNameSet( type ) );
        }
		
		return result;
	}
	
	//---------------------------------------
	
	
	/**
		Get the ObjectName of an MBean having the specified J2EEType.  If
		there is more than one such MBean, an IllegalArgumentException
		is thrown.  If no object is found, then null is returned.
		
		@throws IllegalArgumentException if more than one object is found or null if none found
	 */
		public ObjectName
	querySingletonJ2EEType( final String typeValue )
	{
		final Set<ObjectName>	names	= queryJ2EETypeObjectNameSet( typeValue );
		if ( names.size() > 1 )
		{
			trace( "QueryMgrImpl.getJ2EETypeObjectName: expected 1, got " + names.size() );
			throw new IllegalArgumentException( typeValue );
		}
		
		final ObjectName	objectName	= names.size() == 0 ?
							null :SetUtil.getSingleton( names );
		
		return( objectName );
	}
	
	/**
	 */
		public Set<ObjectName>
	queryJ2EETypeObjectNameSet( final String typeValue )
	{
		final String		prop	= Util.makeTypeProp( typeValue ) ;
		final ObjectName	pat		= Util.newObjectNamePattern( getJMXDomain(), prop );
		
		final Set<ObjectName>	names	= queryPropsObjectNameSet( getMBeanServer(),
								getJMXDomain(), prop );
		
		return( names );
	}
	

	/**
		Get all MBeans having the specified name.
		
		@param nameValue	the value of the "name" property to match
	 */
		public Set<ObjectName>
	queryJ2EENameObjectNameSet( final String nameValue )
	{
		final String	prop	= Util.makeNameProp( nameValue );
		final Set<ObjectName>   names	= queryPropsObjectNameSet( getMBeanServer(),
									getJMXDomain(), prop );
													
		return( names );
	}
	

	/**
	 */
		public static Set<ObjectName>
	queryPatternObjectNameSet(	
		final MBeanServerConnection	conn,
		final ObjectName				pattern )
		throws IOException
	{
		final Set<ObjectName>	names	= JMXUtil.queryNames( conn, pattern, null );
		
		return checked(names);
	}
	
	
	/**
	 */
		public static Set<ObjectName>
	queryPatternObjectNameSet(	
		final MBeanServer			server,
		final ObjectName			pattern )
	{
		final Set<ObjectName>	names	= JMXUtil.queryNames( server, pattern, null );
		
		return checked(names);
	}
	/**
	 */
		public Set<ObjectName>
	queryPatternObjectNameSet( final ObjectName	pattern )
	{
		return( queryPatternObjectNameSet( getMBeanServer(), pattern ) );
	}
	
	/**
	 */
		public Set<ObjectName>
	queryPatternObjectNameSet( String domain, String props )
	{
	    final String myDomain   = getObjectName().getDomain();
	    
		final ObjectName	pattern	= Util.newObjectNamePattern(
		    domain == null ? myDomain : domain, props );
		return( queryPatternObjectNameSet( getMBeanServer(), pattern ) );
	}
	
	
	/**
	 */
		public String[]
	queryJ2EETypeNames( final String type )
	{
		final String	prop	= Util.makeTypeProp( type );
		
		final Set<ObjectName> objectNameSet	= queryPropsObjectNameSet( prop );
		final ObjectName[]	objectNames	= JMXUtil.objectNameSetToArray( objectNameSet );
		
		final String[]	nameKeyValues	=
			JMXUtil.getKeyProperty( AMXConstants.NAME_KEY, objectNames );
		
		return( nameKeyValues );
	}
	
	
	/**
	    @return Set<ObjectName> containing all items that have the matching type and name
	 */
	    public Set<ObjectName>
	queryJ2EETypeNameObjectNameSet(
	    final String type,
	    final String name )
	{
	    final String    props   = Util.makeRequiredProps( type, name );
	    return queryPatternObjectNameSet( Util.newObjectNamePattern( getObjectName().getDomain(), props ) );
	}
	
	
		private static String[]
	convertToRegex( String[] wildExprs )
	{
		String[]	regexExprs	= null;
		
		if ( wildExprs != null )
		{
			regexExprs	= new String[ wildExprs.length ];
								
			for( int i = 0; i < wildExprs.length; ++i )
			{
				final String	expr	= wildExprs[ i ];
				
				final String	regex	= expr == null ? 
									null : RegexUtil.wildcardToJavaRegex( expr );
				
				regexExprs[ i ]	= regex;
			}
		}
		return( regexExprs );
	}
	
	/**
	 */
		private Set<ObjectName>
	matchWild(
		final Set<ObjectName>		candidates,
		final String[]	wildKeys,
		final String[]	wildValues)
	{
		final String[]	regexNames	= convertToRegex( wildKeys );
		final String[]	regexValues	= convertToRegex( wildValues );
		
		final ObjectNameQueryImpl	query	= new ObjectNameQueryImpl();
		final Set<ObjectName>	resultSet	= query.matchAll( candidates, regexNames, regexValues );
		
		return( resultSet );
	}
	
	
	/**
	 */
		public Set<ObjectName>
	queryWildObjectNameSet( 
		final String[]	wildKeys,
		final String[]	wildValues )
	{
		final Set<ObjectName>	candidates	= queryAllObjectNameSet();
		
		return( matchWild( candidates, wildKeys, wildValues ) );
	}
	

	/**
		Return a Set containing all AMX ObjectNames.
		
		@return a Set of all ObjectNames
	 */
		public Set<ObjectName>
	queryAllObjectNameSet()
	{
		final ObjectName	pat	= Util.newObjectNamePattern( getJMXDomain(), "" );
		
		return( queryPatternObjectNameSet( pat ) );
	}
	
	/**
		Match an interface classname against the value of an Attribute.
		
		@param objectNames		the set to search
		@param interfaceName	the name of the interface to find
		@param attributeName	the name of the Attribute to search
	 */
		private Set<ObjectName>
	queryInterfaceObjectNameSet(
		final Set<ObjectName>	objectNames,
		final String	        interfaceName,
		final String	        attributeName)
	{
		final Set<ObjectName>	result		= new HashSet<ObjectName>();
		
		final MBeanServer	server	= getMBeanServer();
		for( final ObjectName objectName : objectNames )
		{
			try
			{
				if ( server.getAttribute( objectName, attributeName ).equals( interfaceName ) )
				{
					result.add( objectName );
				}
			}
			catch( JMException e )
			{
				// ignore
			}
		}
		
		return checked(result);
	}

	
	/**
	 */
		public Set<ObjectName>
	queryInterfaceObjectNameSet(
		final String	searchInterfaceName,
		final Set<ObjectName>		candidates )
		throws ClassNotFoundException
	{
		final Set<ObjectName>	result = new HashSet<ObjectName>();
		final Set<ObjectName>   iter   = candidates == null ?
			                        queryAllObjectNameSet() : candidates;
		
		final Class<?>	searchInterface	= ClassUtil.getClassFromName( searchInterfaceName );
		
		final MBeanServer	server	= getMBeanServer();
		for( final ObjectName objectName : iter )
		{
			try
			{
                final MBeanInfo info = server.getMBeanInfo(objectName);
				final String interfaceName	= AMXProxyHandler.interfaceName(info);
				
				final Class<?>	c	= ClassUtil.getClassFromName( interfaceName );
					
				if ( searchInterface.isAssignableFrom( c ) )
				{
					result.add( objectName );
				}
			}
			catch( JMException e )
			{
				// ignore
			}
		}
		
		return( checked(result) );
	}
}











