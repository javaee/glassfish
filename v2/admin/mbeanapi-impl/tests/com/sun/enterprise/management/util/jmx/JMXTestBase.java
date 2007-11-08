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
package com.sun.enterprise.management.util.jmx;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistrationException;

import com.sun.enterprise.management.PropertyKeys;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.jmx.stringifier.AttributeStringifier;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

/**
	Base class for AMX unit tests.
 */
public class JMXTestBase extends junit.framework.TestCase
{
	private static MBeanServerConnection	sConn;
	private static Map<String,Object>		sEnv;
	protected final String                  NEWLINE;
	
		public static void
	setGlobalConnection( final MBeanServerConnection conn )
	{
		sConn	= conn;
	}
	
    	protected <T> T
    newProxy(
    	final ObjectName	target,
    	final Class<T>		interfaceClass )
    {
        try
        {
	        assert getMBeanServerConnection().isRegistered( target );
	    }
	    catch( java.io.IOException e )
	    {
	        throw new RuntimeException( e );
	    }
	    
    	return interfaceClass.cast( MBeanServerInvocationHandler.newProxyInstance(
    				getMBeanServerConnection(), target, interfaceClass, true ) );
    }
	
		public static MBeanServerConnection
	getGlobalConnection( )
	{
		return( getMBeanServerConnection() );
	}
	
		public static MBeanServerConnection
	getMBeanServerConnection( )
	{
		return( sConn );
	}

		public static synchronized Object
	getEnvValue(  final String	key )
	{
		return( sEnv == null ? null : sEnv.get( key ) );
	}
	
		public static Integer
	getEnvInteger( final String	key, Integer defaultValue )
	{
		final String	s	= getEnvString( key, null);
		Integer	result	= defaultValue;
		if ( s != null )
		{
			result	= new Integer( s.trim() );
		}

		return( result );
	}
	
		public static String
	getEnvString( final String	key, final String defaultValue )
	{
		final String	s	= (String)getEnvValue( key );

		return( s == null ? defaultValue : s  );
	}
	
	
		public static Boolean
	getEnvBoolean( final String	key, final Boolean defaultValue )
	{
		Boolean	result	= defaultValue;
		final String	s	= getEnvString( key, null );
		if ( s != null )
		{
			result	= Boolean.valueOf( s );
		}

		return( result  );
	}


		private static synchronized void
	initEnv()
	{
		if ( sEnv == null )
		{
			sEnv	= new HashMap<String,Object>();
		}
	}
	
		public static synchronized void
	setEnvValue(
		final String	key,
		final Object	value )
	{
		initEnv();
		sEnv.put( key, value );
	}
	
		public static synchronized void
	setEnvValues( final Map<String,Object> m)
	{
		initEnv();
		sEnv.putAll( m );
	}
	
	
		public
	JMXTestBase()
	{
        super( "JMXTestBase" );
        
        NEWLINE = StringUtil.NEWLINE();
        
        checkAssertsOn();
	}

		public
	JMXTestBase(  String name )
	{
 		super( name );
        NEWLINE = StringUtil.NEWLINE();
		checkAssertsOn();
	}
	
			

		protected String
	toString( final ObjectName objectName )
	{
	    return JMXUtil.toString( objectName );
	}
	
		protected String
	toString( final Object o )
	{
		String	result	= null;
		
		if ( o instanceof Collection )
		{
			result	= CollectionUtil.toString( (Collection)o, "\n" );
		}
		else
		{
			result	= SmartStringifier.toString( o );
		}
		
		return( result );
	}
	
	
		protected static void
	trace( final Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}
	
		protected void
	println( final Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}
	
			protected long
	now()
	{
		return( System.currentTimeMillis() );
	}
	
		protected final void
	printElapsed( final String msg, final long start )
	{
		printVerbose( msg + ": " + (now() - start) + "ms" );
	}
	
		protected final void
	printElapsedIter( final String msg, final long start, final long iterations)
	{
		printVerbose( msg + "(" + iterations + " iterations): " + (now() - start) + "ms" );
	}
	
		protected final void
	printElapsed(
		final String	msg,
		final int		numItems,
		final long		start )
	{
		printVerbose( msg + ", " + numItems + " MBeans: " + (now() - start) + "ms" );
	}


	


		protected final String
	quote( final Object o )
	{
		return( StringUtil.quote( SmartStringifier.toString( o ) ) );
	}




		protected boolean
	getVerbose()
	{
		final String	value	= (String)getEnvValue( PropertyKeys.VERBOSE_KEY );
		
		return( value != null && Boolean.valueOf( value ).booleanValue() );
	}
	
		protected void
	printVerbose( final Object o )
	{
		if ( getVerbose() )
		{
			trace( o );
		}
	}


		protected void
	warning( final String msg )
	{
		trace( "\nWARNING: " + msg + "\n" );
	}
	
		protected void
	failure( final String msg )
	{
		trace( "\nFAILURE: " + msg + "\n" );
		assert( false ) : msg;
		throw new Error( msg );
	}

		protected void
	checkAssertsOn()
	{
		try
		{
			assert( false );
			throw new Error( "Assertions must be enabled for unit tests" );
		}
		catch( AssertionError a )
		{
		}
	}
	
	
		protected void
	registerMBean( Object mbean, String name )
		throws MalformedObjectNameException, InstanceAlreadyExistsException,
		NotCompliantMBeanException, MBeanRegistrationException
	{
		if ( sConn instanceof MBeanServer )
		{
			((MBeanServer)sConn).registerMBean( mbean, new ObjectName( name ) );
		}
		else
		{
			throw new IllegalArgumentException( "test connection is not an MBeanServer" );
		}
	}
	
		public void
	setUp() throws Exception
	{
	    checkAssertsOn();
		assert( sConn != null );
	}
	
		public void
	tearDown()
		throws Exception
	{
		// do NOT destroy the MBeanServer or its contents
	}

};

