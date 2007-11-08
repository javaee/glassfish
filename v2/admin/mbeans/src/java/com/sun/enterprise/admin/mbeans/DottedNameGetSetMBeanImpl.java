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
 * $Header: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/mbeans/DottedNameGetSetMBeanImpl.java,v 1.3 2005/12/25 03:42:19 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:42:19 $
 */
 

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.dottedname.*;

import javax.management.*;
import java.lang.reflect.Array;


/*
	MBean which supports get/set CLI commands using dotted names
 */
public class DottedNameGetSetMBeanImpl
	extends StandardMBean implements DottedNameGetSetMBean
{
	final DottedNameGetSetForConfig		mConfigImpl;
	final DottedNameGetSetForMonitoring	mMonitoringImpl;
	final DottedNameServerInfoCache		mServerInfo;
	
	long								mTimeOfLastCall;
	
	/*
		Instantiate with a reference to an MBeanServerConnection which will be used
		as the server when searching for required objects (which is possibly different
		than the MBeanServer in which this object will be registered).
		
		Due to a bug in the server startup sequence, this is the only allowed
		constructor; avoiding the bug requires the SunoneInterceptor as 'conn'.
		Ideally the only constructor would be one that takes no arguments, and obtains
		its MBeanServerConnection from MBeanServerRegistration.preRegister().
	 */
		public
	DottedNameGetSetMBeanImpl(
		final MBeanServerConnection conn,
		final DottedNameRegistry	registry,
		final DottedNameRegistry	monitoringRegistry )
		throws NotCompliantMBeanException, MalformedObjectNameException
	{


		super( DottedNameGetSet.class );
	
		mServerInfo	= new DottedNameServerInfoCache( createServerInfo( conn ) );
		
		mConfigImpl		= new DottedNameGetSetForConfig( conn, registry, mServerInfo );
		mMonitoringImpl	= new DottedNameGetSetForMonitoring( conn, monitoringRegistry, mServerInfo );
	}
	
	// clear the DottedNameFactory every 10 minutes
	static private final long	CLEAR_CACHE_INTERVAL_MILLIS	= 10 * 60 * 1000;
	
		protected void
	pre()
	{
		mServerInfo.refresh();
		
		final long	now	= System.currentTimeMillis();
		if ( (now - mTimeOfLastCall) > CLEAR_CACHE_INTERVAL_MILLIS )
		{
			DottedNameFactory.getInstance().clear();
		}
		
		mTimeOfLastCall	= now;
	}
	
	// this method exists so unit test code can override it
		protected DottedNameServerInfo
	createServerInfo( final MBeanServerConnection conn )
	{
		return( new DottedNameServerInfoImpl( conn ) );
	}
	
		protected boolean
	checkGetResults( final Object[] results )
	{
		boolean 	good	= true;
		
		for( int i = 0; i < results.length; ++i )
		{
			final Object	o	= results[ i ];
			
			if ( ! (o instanceof Attribute ||
					o instanceof Attribute[] ||
					o instanceof Exception ) )
			{
				//System.out.println( "checkGetResults: object has class: " + o.getClass().getName() );
				good	= false;
				break;
			}
		}
		return( good );
	}
	
		protected boolean
	checkSetResults( final Object[] results )
	{
		boolean 	good	= true;
		
		for( int i = 0; i < results.length; ++i )
		{
			final Object	o	= results[ i ];
			
			if ( ! (o instanceof Attribute ||
					o instanceof Exception ) )
			{
				good	= false;
				break;
			}
		}
		return( good );
	}
	
	/*
		Return true if every element of the array has *exactly* the same class.
	 */
		private boolean
	hasIdenticalElementClasses( final Object [] a )
	{
		boolean	isUniform	= true;
		
		if ( a.length > 0 )
		{
			final Class		matchType	= a[ 0 ].getClass();
			
			for( int i = 1; i < a.length; ++i )
			{
				if ( a[ i ].getClass() != matchType )
				{
					isUniform	= false;
					break;
				}
			}
		}
		
		return( isUniform );
	}
	
	/*
		If every element of the array has the same class, return a new array whose type specifies that
		class, otherwise return the original array.
	 */
    protected Object []
	convertArrayType( final Object [] input )
	{
		Object []	result	= input;
		
		if ( hasIdenticalElementClasses( input ) && input.length != 0 )
		{
			result	= (Object [])Array.newInstance( input[ 0 ].getClass(), input.length );
			
			for( int i = 0; i < input.length; ++i )
			{
				result[ i ]	= input[ i ];
			}
		}
		
		return( result );
	}
	
		protected Object []
	dottedNameAnyGet( DottedNameGetSetMBeanBase impl, final String [] names )
	{
		pre();
		
		final Object []	results	= impl.dottedNameGet( names );
		
		assert( checkGetResults( results ) );
		
		assert( results.length == names.length );
		return( convertArrayType( results ) );
	}
	
		protected Object
	dottedNameAnyGet( DottedNameGetSetMBeanBase impl, final String name )
	{
		final Object []	results	= dottedNameAnyGet( impl, new String [] { name } );
		
		return( results[ 0 ] );
	}
	
		public Object []
	dottedNameGet( final String [] names )
	{
		return( dottedNameAnyGet( mConfigImpl, names ) );
	}
	
		public Object
	dottedNameGet( final String name )
	{
		final Object	result	= dottedNameAnyGet( mConfigImpl, name );
		return( result );
	}
	
		public Object []
	dottedNameSet( final String [] nameValuePairs )
	{
		pre();
		
		Object [] results	= mConfigImpl.dottedNameSet( nameValuePairs );
		
		assert( checkSetResults( results ) );
		assert( results.length == nameValuePairs.length );
		
		return( convertArrayType( results ) );
	}
	
		public Object
	dottedNameSet( final String nameValuePair )
	{
		final Object [] results	= dottedNameSet( new String [] { nameValuePair } );
		
		return( results[ 0 ] );
	}
	
		public String []
	dottedNameList( final String [] namePrefixes )
	{
		pre();
		
		return( mConfigImpl.dottedNameList( namePrefixes ) );
	}
	
	//------------------------- monitoring --------------------------
	
		public Object []
	dottedNameMonitoringGet( final String [] names )
	{
		return( dottedNameAnyGet( mMonitoringImpl, names ) );
	}
	
		public Object
	dottedNameMonitoringGet( final String name )
	{
		return( dottedNameAnyGet( mMonitoringImpl, name ) );
	}

		public String []
	dottedNameMonitoringList( final String [] namePrefixes )
	{
		pre();
		
		return( mMonitoringImpl.dottedNameList( namePrefixes  ) );
	}
}


/*
	Implementing subclass for monitoring dotted names
 */
final class DottedNameGetSetForMonitoring extends DottedNameGetSetMBeanBase
{
	final DottedNameResolver				mMonitoringResolver;
	
		public
	DottedNameGetSetForMonitoring(
		final MBeanServerConnection conn,
		final DottedNameRegistry	registry,
		final DottedNameServerInfo	serverInfo )
		throws MalformedObjectNameException
	{
		super( conn, registry, serverInfo );
		
		// DottedNameResolver for monitoring dotted names does NOT need to account for aliases
		mMonitoringResolver	= new DottedNameResolverFromRegistry( registry );
	}
	
	
		DottedNameResolver
	getResolver( )
	{
		return( mMonitoringResolver );
	}
	
	
		DottedNameQuery
	createQuery(  )
	{
		// no aliasing is done; the registry implements query directly
		return( mRegistry );
	}
}
