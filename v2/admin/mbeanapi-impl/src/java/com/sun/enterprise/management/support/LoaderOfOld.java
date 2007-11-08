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
package com.sun.enterprise.management.support;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import java.lang.reflect.Constructor;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.Output;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.DomainRoot;

/**
	Loads MBeans.
 */
abstract class LoaderOfOld
{
	protected final Loader	mLoader;
	private final Output    mDebug;
	
	LoaderOfOld( final Loader loader )
	{
		mLoader	= loader;
		
		mDebug  = AMXDebug.getInstance().getOutput( this.getClass().getName() );
	}
	
	 	protected final void
	debug(final Object o)
	{
	    mDebug.println( o.toString() );
	}
	
	
	protected abstract Set<ObjectName>	findAllOldCandidates();
	
	
		protected DomainRoot
	getDomainRoot()
	{
		return( mLoader.getDomainRoot() );
	}
	
	protected abstract Set	getNeedsSupport();
	protected abstract Set	getIgnoreTypes();
	protected abstract boolean	isOldMBean( final ObjectName objectName );
	
	
		public final boolean
	shouldSync( final ObjectName o )
	{
		boolean	shouldSync	= isOldMBean( o );
		
		if ( shouldSync )
		{
			final String	type	= o.getKeyProperty( "type" );
			
			if ( getNeedsSupport().contains( type ) )
			{
				shouldSync	= false;
				getLogger().warning(
					"com.sun.appserv MBean not yet supported: " +
					StringUtil.quote(o) );
			}
			else if ( isDefectiveIgnore( o ) )
			{
				shouldSync	= false;
				debug(
					"com.sun.appserv MBean was last determined to be defective " +
					"and will not be represented in AMX: " +
					StringUtil.quote(o) );
			}
		}

		return( shouldSync );
	}
	
	
	/**
	    Is the com.sun.appserv MBean defective in some way such that it
	    cannot be properly represented in AMX?
	 */
	    protected boolean
	isDefectiveIgnore( final ObjectName objectName )
	{
	    return false;
	}
	
	
	/**
		Create a list of all old MBeans and return a list in the order in which
		they must be registered.
	 */
		public final List<ObjectName>
	findAllOld()
	{
		final Set<ObjectName>	all	= findAllOldCandidates();
		final Set<ObjectName>	results	= new HashSet<ObjectName>();
		
		for( final ObjectName objectName : all)
		{
			if ( shouldSync( objectName ) )
			{
				results.add( objectName );
			}
		}
		
		return( ListUtil.newListFromCollection( results ) );
	}
	
	protected abstract ObjectName	oldToNewObjectName( final ObjectName o );
	
	
	
	/**
		Create a Map of Set keyed by the ObjectName key value specified.
		Each Set contains all the ObjectNames with that key.
	 */
		protected final Map<String,Set<ObjectName>>
	candidatesToMap(
		final Set<ObjectName> candidates,
		final String	key)
	{
		final Map<String,Set<ObjectName>> setMap	= new HashMap<String,Set<ObjectName>>();
		for( final ObjectName candidate : candidates )
		{
			final String keyValue	= candidate.getKeyProperty( key );
			
			Set<ObjectName>	typeSet	= setMap.get( keyValue );
			if ( typeSet == null )
			{
				typeSet	= new HashSet<ObjectName>();
				setMap.put( keyValue, typeSet );
			}
			typeSet.add( candidate );
		}
		
		return( setMap );
	}
	
	
	

		protected final ObjectName
	registerNew(
		final Object		impl,
		final ObjectName	implObjectName,
		final ObjectName	oldObjectName )
		throws MBeanRegistrationException,
			InstanceAlreadyExistsException, NotCompliantMBeanException
	{				
		if ( impl == null )
		{
		    final String msg    = "unable to create new impl for old: " + oldObjectName;
		    debug( msg );
			throw new IllegalArgumentException( msg );
		}

		return( mLoader.registerNew( impl, implObjectName, oldObjectName ) );
	}
	
		protected final void
	trace( final Object o )
	{
		debug( o );
	}
			protected String
	toString( final Object o )
	{
		return( com.sun.appserv.management.util.stringifier.SmartStringifier.toString( o ) );
	}
	
		protected Logger
	getLogger()
	{
		return( mLoader.getMBeanLogger() );
	}
	
		public String
	getAMXJMXDomainName()
	{
		return( mLoader.getAMXJMXDomainName() );
	}
	
		public MBeanServer
	getMBeanServer()
	{
		return( mLoader.getMBeanServer() );
	}
	
		

	private static final Class[]	EMPTY_SIG	= new Class[0];
	private static final Class[]	DELEGATE_SIG	=
		new Class[] { Delegate.class };
	
	

		private Constructor
	findConstructor( final Constructor[] constructors, final Class[] sig )
	{
		Constructor	constructor	= null;
		
		for( int i = 0; i < constructors.length; ++i )
		{
			final Class<?>[]	csig	= constructors[ i ].getParameterTypes();
			
			if ( csig.length == sig.length )
			{
				constructor	= constructors[ i ];
			
				for( int c = 0; c < sig.length; ++c )
				{
					if ( ! csig[ i ].isAssignableFrom( sig[ i ] ) )
					{
						constructor	= null;
						break;
					}
				}
				
				if ( constructor != null )
				{
					break;
				}
			}
		}
		
		return( constructor );
	}
	
		private Constructor
	getDelegateConstructor( final Constructor[] constructors )
	{
		return( findConstructor( constructors, DELEGATE_SIG ) );
	}
	
	
		private Constructor
	getEmptyConstructor( final Constructor[] constructors )
	{
		return( findConstructor( constructors, EMPTY_SIG ) );
	}
	
			
		protected Class
	getImplClass(
		final ObjectName	newObjectName,
		final ObjectName	oldObjectName)
	{
		final String		newType	= Util.getJ2EEType( newObjectName );
		
		final TypeInfo	info	= TypeInfos.getInstance().getInfo( newType );
		assert( info != null );
		final Class			implClass	= info.getImplClass();
		
		return( implClass );
	}
			
		protected Object
	newImpl(
		final ObjectName	newObjectName,
		final ObjectName	oldObjectName )
		throws Exception
	{
		Object	impl	= null;
		
		final Class			implClass	= getImplClass( newObjectName, oldObjectName );
		
		try
		{
			final Constructor[]	constructors	= implClass.getConstructors();
			Constructor			constructor	= null;
			
			if ( (constructor = getDelegateConstructor( constructors )) != null )
			{
				final DelegateToMBeanDelegate	delegate	=
					new DelegateToMBeanDelegate( mLoader.getMBeanServer(), oldObjectName );
				assert( delegate != null );
				debug( "created Delegate with target of " + oldObjectName +
				    " for " + newObjectName );
					
				impl = constructor.newInstance( new Object[] { delegate } );
			}
			else if ( getEmptyConstructor( constructors ) != null )
			{
				impl	= implClass.newInstance();
			}
			else
			{
				assert( false );
			    throw new Error( "Delegate has no constructor" );
			}
		}
		catch( Exception e )
		{
		    final Throwable rootCause   = ExceptionUtil.getRootCause( e );
			debug( "Loader.newImpl: exception creating new impl: "  + e + "\n" +
			    ExceptionUtil.getStackTrace( rootCause ) );
			throw e;
		}
		
		return( impl );
	}


		protected ObjectName
	findExisting(
		final Set<ObjectName>	newObjectNames,
		final ObjectName	    oldObjectName )
	{
		ObjectName	resultName	= null;
		
		if ( newObjectNames.size() == 1 )
		{
			// already registered
			resultName	= GSetUtil.getSingleton( newObjectNames );
		}

		return( resultName );
	}
	
		private final ObjectName
	ensureNew(
		final ObjectName	newObjectName,
		final ObjectName	oldObjectName )
		throws MBeanRegistrationException,
			InstanceAlreadyExistsException, NotCompliantMBeanException, Exception
	{
		// don't assume this ObjectName is the entire name; query for it
		final ObjectName	pattern	= Util.newObjectNamePattern( newObjectName );
		
		// don't assume this ObjectName is the entire name; query for it
		final Set<ObjectName>	objectNames	= JMXUtil.queryNames( getMBeanServer(), pattern, null );
		
		final ObjectName	existingObjectName	= findExisting( objectNames, oldObjectName );
		
		ObjectName	resultName	= null;
		
		if ( existingObjectName == null )
		{
			// not yet registered, create it
			final Object		impl			= newImpl( newObjectName, oldObjectName );
			
			resultName	= registerNew( impl, newObjectName, oldObjectName );
		}
		else
		{
			resultName	= existingObjectName;
		}
		
		assert( resultName != null );
		
		return( resultName );
	}
	
	
		protected ObjectName
	syncWithOld( final ObjectName oldObjectName )
		throws MBeanRegistrationException,
			InstanceAlreadyExistsException, NotCompliantMBeanException, Exception
	{
		final ObjectName	newObjectName	= oldToNewObjectName( oldObjectName );
		debug( "\nsyncWithOld: " + JMXUtil.toString(oldObjectName) + "=> " + JMXUtil.toString(newObjectName) + "\n" );		
		final ObjectName	resultName	= ensureNew( newObjectName, oldObjectName );
		
		return( resultName );
	}
}








