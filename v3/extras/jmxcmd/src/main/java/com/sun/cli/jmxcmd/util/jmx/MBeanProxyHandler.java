/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanProxyHandler.java,v 1.12 2005/11/15 20:59:56 llc Exp $
 * $Revision: 1.12 $
 * $Date: 2005/11/15 20:59:56 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import java.io.IOException;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MBeanServerInvocationHandler;
import javax.management.NotificationBroadcaster;
import javax.management.ReflectionException;
import javax.management.IntrospectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;

import com.sun.cli.jmxcmd.util.jmx.JMXUtil;
import com.sun.cli.jmxcmd.util.jmx.AttributeNameMangler;
import com.sun.cli.jmxcmd.util.jmx.AttributeNameMapper;
import com.sun.cli.jmxcmd.util.jmx.AttributeNameMapperImpl;
import com.sun.cli.jcmd.util.misc.ExceptionUtil;
import com.sun.cli.jcmd.util.misc.StringUtil;
import com.sun.cli.jcmd.util.misc.ObjectUtil;
import com.sun.cli.jcmd.util.misc.Output;
import com.sun.cli.jcmd.util.misc.DebugOut;
import com.sun.cli.jcmd.util.misc.JCMDDebug;


/**
	Implementation of a proxy <i>handler</i> that supports Attribute names which are not legal
	Java identifiers.  It does so by mapping illegal Java identifiers into legal
	names.  Any interface supplied needs to take mapped names into account.
	<p>
	Allows specification of either an AttributeNameMangler or AttributeNameMapper for maximum
	flexibility in how illegal Attribute names are mapped.
 */
public class MBeanProxyHandler extends MBeanServerInvocationHandler
	// implements MBeanProxyHandlerIntf
{
	protected final static String	GET	= "get";
	protected final static String	SET	= "set";
	protected final static String	IS	= "is";
	protected final static int	GET_PREFIX_LENGTH	= GET.length();
	protected final static int	IS_PREFIX_LENGTH	= IS.length();
	
	protected final ConnectionSource	mConnectionSource;
	protected AttributeNameMangler		mMangler;
	private AttributeNameMapper			mMapper;
	private final ObjectName			mTargetObjectName;
	private boolean						mCacheMBeanInfo;
	private MBeanInfo					mCachedMBeanInfo;
	private boolean						mMBeanInfoIsInvariant	= false;
	private Logger						mLogger;
	private boolean						mTargetValid;
	private final Integer	            mHashCode;
	
	protected Output                    mDebug;
	
	static protected final String DEBUG_ID =
	    "com.sun.cli.jmxcmd.util.jmx.MBeanProxyHandler";
   	
 	    public int
 	hashCode()
 	{
 	    return ObjectUtil.hashCode( mTargetObjectName,
 	            mConnectionSource, mLogger, mMangler, mMapper, mCachedMBeanInfo, mDebug) ^
 	        ObjectUtil.hashCode( mCacheMBeanInfo ) ^
 	        ObjectUtil.hashCode( mMBeanInfoIsInvariant ) ^
 	        ObjectUtil.hashCode( mTargetValid );
 	}
 	
   	    public boolean
   	equals( final Object rhs )
   	{
   	    if ( rhs == this )
   	    {
   	        return true;
   	    }
   	    
   	    final MBeanProxyHandler other   = (MBeanProxyHandler)rhs;
   	    
       	boolean equals  = mTargetObjectName.equals( other.getTargetObjectName() );
       	if ( equals )
       	{
       	    try
       	    {
           	    equals  = getConnection() == other.getConnection();
       	    }
       	    catch( Exception e )
       	    {
       	        equals  = false;
       	    }
   	    }
   	    
   	    return equals;
   	}
   	
   	
	    protected String
	getDebugID()
	{
	    return DEBUG_ID;
	}
	
	/**
		Normally created through MBeanProxyFactory.newProxyInstance().  Creates a new instance to be used
		as a <i>handler<i> object for Proxy.newProxyInstance.
		
		@param connectionSource	the connection
		@param objectName	the ObjectName of the proxied MBean
	 */
		public
	MBeanProxyHandler(
		ConnectionSource	connectionSource,
		ObjectName			objectName  )
		throws IOException
	{
		this( connectionSource, objectName, (AttributeNameMangler)null );
	}
	
		private
	MBeanProxyHandler(
		final ConnectionSource		connectionSource,
		final ObjectName				objectName,
		final AttributeNameMapper		mapper,
		final AttributeNameMangler	mangler )
		throws IOException
	{
		super( connectionSource.getMBeanServerConnection( false ), objectName );
		
        mDebug  = JCMDDebug.getInstance().getOutput( getDebugID() );
		debugMethod( "MBeanProxyHandler", connectionSource, objectName, mapper, mangler );
		mMangler			= mangler;
		mMapper				= mapper;
		mConnectionSource	= connectionSource;
		mTargetObjectName	= objectName;
		mTargetValid		= true;
		
		mCacheMBeanInfo		= true;
		mCachedMBeanInfo	= null;
		mLogger		= null;
		
		mHashCode	= this.hashCode();
	}
	
	
		public final void
	targetUnregistered()
	{
		debugMethod( mTargetObjectName.toString(), "targetUnregistered" );
		mTargetValid	= false;
		assert( ! targetIsValid() );
	}
	
		public final void
	connectionBad()
	{
		debugMethod( "connectionBad" );
		mTargetValid	= false;
		assert( ! targetIsValid() );
	}
	
		protected final boolean
	targetIsValid()
	{
		return( mTargetValid );
	}
	
		public final boolean
	checkValid()
	{
		if ( mTargetValid )
		{
			try
			{
				mTargetValid	= getConnection().isRegistered( getTargetObjectName() );
			}
			catch( Exception e )
			{
			    debug( "checkValid: connection failed" );
				mTargetValid	= false;
			}
		}
		return( mTargetValid );
	}
	
	/**
		Same as MBeanProxyHandler( connection, objectName ), but can take a name mangler.
		
		@param connectionSource	the connection
		@param objectName	the ObjectName of the proxied MBean
		@param mangler		optional name mangler for illegal Attribute names
	 */
		public
	MBeanProxyHandler(
		ConnectionSource		connectionSource,
		ObjectName				objectName,
		AttributeNameMangler	mangler )
		throws IOException
	{
		this( connectionSource, objectName, null, mangler );
	}
	
	/**
		Same as MBeanProxyHandler( connection, objectName ), but can take a supplied
		mapper for illegal Attribute names.
		
		@param connectionSource	the connection
		@param objectName	the ObjectName of the proxied MBean
		@param mapper		optional name mapper for illegal Attribute names
	 */
		public
	MBeanProxyHandler(
		ConnectionSource		connectionSource,
		ObjectName				objectName,
		AttributeNameMapper		mapper )
		throws IOException
	{
		this( connectionSource, objectName, mapper, null );
	}
	
		public void
	setProxyLogger( final Logger	logger )
	{
		mLogger	= logger;
	}
	
	protected final static String	LOGGER_NAME	= "com.sun.appserv.management.Proxy";
	
		public Logger
	getProxyLogger( )
	{
		if ( mLogger == null )
		{
			mLogger	= Logger.getLogger( this.getClass().getName() );
		}
		return( mLogger );
	}
	
		public final ConnectionSource
	getConnectionSource()
	{
		return( mConnectionSource );
	}
	
		protected final MBeanServerConnection
	getConnection()
		throws IOException
	{
		return( mConnectionSource.getMBeanServerConnection( false ) );
	}
	
		protected final ObjectName
	getTargetObjectName()
	{
		return( mTargetObjectName );
	}

		public static String[]
	getAllAttributeNames(
		final MBeanAttributeInfo[]	infos )
	{
		return( JMXUtil.getAttributeNames( infos ) );
	}
	
	/**
		Create a mapper based on the supplied attributeInfos
		
		@param attributeInfos
		@param mangler
		@return AttributeNameMapper
	 */
		AttributeNameMapper
	createMapper(
		final MBeanAttributeInfo[]	attributeInfos,
		final AttributeNameMangler	mangler)
	{
		return( new AttributeNameMapperImpl( getAllAttributeNames( attributeInfos ), mangler ) );
	}
	
	/**
		Initialize a mapper based on the supplied attributeInfos. Does nothing if already
		initialized.
	 */
		protected void
	initMapper( )
		throws IOException, IntrospectionException, ReflectionException, InstanceNotFoundException
	{
		// lazy initialization here
		if ( mMapper == null && mMangler != null)
		{
			final MBeanInfo	mbeanInfo	= getMBeanInfo( true );
			
			mMapper	= createMapper( mbeanInfo.getAttributes(), mMangler);
		}
	}
	
		protected String
	extractAttributeNameFromMethod( String methodName )
	{
		assert( methodName.startsWith( GET ) || 
				methodName.startsWith( SET ) || 
				methodName.startsWith( IS ) );
		final int startIndex = methodName.startsWith( GET ) || methodName.startsWith( SET ) ? 
			GET_PREFIX_LENGTH : IS_PREFIX_LENGTH;
		return( methodName.substring( startIndex, methodName.length() ) );
	}
	
		protected boolean
	isMappedAttributeMethod( final String attributeName )
	{
		boolean	isMapped	= false;
		
		if ( mMapper != null )
		{
			final String	originalName	= mMapper.derivedToOriginal( attributeName );
			
			isMapped	= ! attributeName.equals( originalName );
		}
		
		return( isMapped );
	}
	
	
		protected void
	cacheMBeanInfo( final boolean	cacheIt )
	{
		mCacheMBeanInfo	= cacheIt;
		mMBeanInfoIsInvariant	= cacheIt;
		if ( ! cacheIt )
		{
			mCachedMBeanInfo	= null;
		}
	}
	
		public final boolean
	getMBeanInfoIsInvariant()
	{
		return( mMBeanInfoIsInvariant );
	}
	
		protected final void
	setMBeanInfoIsInvariant( boolean isInvariant )
	{
		mMBeanInfoIsInvariant	= isInvariant;
	}
	
		protected final boolean
	getCacheMBeanInfo()
	{
		return( mCacheMBeanInfo );
	}
	
	/**
		Same as XAttributesAccess.getAttributes, but with exceptions
		
		@param refresh whether to get a fresh copy
	 */
		protected MBeanInfo
	getMBeanInfo( boolean refresh )
		throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException
	{
		if ( refresh ||
			(! mCacheMBeanInfo) ||
			mCachedMBeanInfo == null )
		{
			mCachedMBeanInfo	= getConnection().getMBeanInfo( getTargetObjectName() );
		}
		return( mCachedMBeanInfo );
	}
	
	/**
		Same as XAttributesAccess.getAttribute, but with exceptions
	 */
		public Object
	getAttribute( final String attributeName )
		throws InstanceNotFoundException, ReflectionException,
		MBeanException, AttributeNotFoundException, IOException
	{
		final Object	result	=
			getConnection().getAttribute( getTargetObjectName(), attributeName );
			
   		postGetAttributeHook( attributeName, result );
	
		return( result );
	}
	
	
	/**
		Same as XAttributesAccess.getAttributes, but with exceptions
	 */
		public AttributeList
	getAttributes( final String[] attrNames )
		throws IOException, InstanceNotFoundException, ReflectionException
	{
		final AttributeList	results	=
			getConnection().getAttributes( getTargetObjectName(), attrNames );
			
   		postGetAttributesHook( attrNames, results );
   		
   		return( results );
	}
	
	/**
		Same as XAttributesAccess.setAttribute, but with exceptions
	 */
		public void
	setAttribute( final Attribute attr )
		throws IOException, InstanceNotFoundException, ReflectionException,
			AttributeNotFoundException, MBeanException, InvalidAttributeValueException
	{
		getConnection().setAttribute( getTargetObjectName(), attr );
		
   		postSetAttributeHook( attr );
	}
	
	/**
		Same as XAttributesAccess.setAttributes, but with exceptions
	 */
		public AttributeList
	setAttributes( final AttributeList requested )
		throws IOException, InstanceNotFoundException, ReflectionException
	{
		final AttributeList	results	= getConnection().setAttributes( getTargetObjectName(), requested );
		
   		postSetAttributesHook( requested, results );
   				
		return( results );
	}
	
   	private final String	LOG_LEVEL_NAME	= "LogLevel";
   	
		protected void
	postGetAttributeHook(
		final String		name,
		final Object		value )
	{
	}
	
		protected void
	postGetAttributesHook(
		final String[]		requested,
		final AttributeList	actual )
	{
	}
	
		protected void
	postSetAttributeHook( final Attribute attr )
	{
	}
	
		protected void
	postSetAttributesHook(
		final AttributeList	requested,
		final AttributeList	actual )
	{
	}
	
   	
	/**
		Invoke the specified method.  This implementation supports additional functionality
		over the JMX MBeanServerInvocationHandler:
		(1) It supports mapped Attribute names (ones that are not legal Java names)
		(2) it supports XAttributesAccess, which otherwise does not work correctly
		<p>
		For anything else, the behavior of MBeanServerInvocationHandler is used.
	 */
		public Object
	invoke(
		Object		proxy,
    	Method		method,
		Object[]	args
		)
   		throws java.lang.Throwable
   	{
   		final String	methodName	= method.getName();
   		final int		numArgs	= args == null ? 0 : args.length;
   		
		debugMethod( method.getName(), args );
		
   		Object	result	= null;
   		
   		final boolean	isGetter		= JMXUtil.isIsOrGetter( method );
   		final boolean	isSetter		= isGetter ? false : JMXUtil.isSetter( method );
   		
   		boolean		handled	= false;
   		
		if ( methodName.equals( "getTargetObjectName" ) )
		{
			handled	= true;
			result	= getTargetObjectName();
		}
		else if ( methodName.equals( "getMBeanInfo" ) && numArgs <= 1)
		{
			handled	= true;
			
			if ( numArgs == 1 )
			{
				result	= getMBeanInfo( ((Boolean)args[ 0 ] ).booleanValue() );
			}
			else if ( numArgs == 0 )
			{
				result	= getMBeanInfo( mCacheMBeanInfo );
			}
			else
			{
				handled	= false;
			}
		}
		else if ( methodName.equals( "getProxyLogger" ) && numArgs == 0 )
		{
			handled	= true;
			result	= getProxyLogger();
		}
		else if ( methodName.equals( "setProxyLogger" ) &&
					numArgs == 1 &&
					method.getParameterTypes()[ 0 ] == Logger.class )
		{
			handled	= true;
			setProxyLogger( (Logger)args[ 0 ] );
		}
		else if ( (isGetter || isSetter) )
		{
			handled	= true;
			// it's a plain getFoo(), setFoo( f ) call 
			initMapper( );
			
			final String	javaName	= extractAttributeNameFromMethod( methodName );

			String	attributeName	= javaName;
			
			if ( isMappedAttributeMethod( javaName ) )
			{
	   			attributeName	= mMapper.derivedToOriginal( javaName );
	   		}
	   			
			//trace( "MBeanProxyHandler.invoke: mapped attribute: " + javaName + " => " + attributeName );
			
   			if ( isGetter )
   			{
   				result	= getAttribute( attributeName );
   			}
   			else
   			{
   				final Attribute	attr	= new Attribute( attributeName, args[ 0 ] );
   				setAttribute( attr );
   			}
   		}
   		else if ( methodName.indexOf( "etAttribute" ) == 1 )
   		{
	   		handled	= true;
	   			
   			// likely one of getAttribute(), getAttributes(), setAttribute(), setAttributes()
   			
			//p( "MBeanProxyHandler.invoke: " + method.getName() + " " + numArgs + " args." );
	   		if ( JMXUtil.isGetAttribute( method ) )
	   		{
	   			final String	attrName	= (String)args[ 0 ];
	   			result	= getAttribute( attrName );
	   		}
	   		else if ( JMXUtil.isGetAttributes( method ) )
	   		{
	   			final String[]	attrNames	= (String[])args[ 0 ];
	   			result	= (AttributeList)getAttributes( attrNames );
	   		}
	   		else if ( JMXUtil.isSetAttribute( method ) )
	   		{
	   			final Attribute	attr	= (Attribute)args[ 0 ];
	   			setAttribute( attr );
	   		}
	   		else if ( JMXUtil.isSetAttributes( method ) )
	   		{
	   			final AttributeList	requested	= (AttributeList)args[ 0 ];
	   			result	= (AttributeList)setAttributes( requested );
	   		}
	   		else
	   		{
	   			handled	= false;
	   		}
   		}
   		else if ( methodName.equals( "hashCode" ) )
	    {
   			/*
   				java.lang.reflect.Proxy will route all calls through invoke(),
   				even hashCode().  To avoid newing up an Integer every time,
   				just return a stored version.  hashCode() is called frequently
   				when proxies are inserted into Sets or Maps.  toString() and
   				equals() don't seem to get called however.
   			 */
   			result	= mHashCode;
	   		handled = true;
   		}
   		else if ( methodName.equals( "toString" ) )
	    {
	   		result  = "proxy to " + JMXUtil.toString( getTargetObjectName() );
	   		handled = true;
   		}
   		else if ( methodName.equals( "equals" ) && numArgs == 1)
	    {
	   		result  = this.equals( args[ 0 ] );
	   		handled = true;
   		}
   		
   		if ( ! handled )
   		{
   			debugMethod( getTargetObjectName().toString(), "super.invoke",
   				method.getName(), args );

   			result	= super.invoke( proxy, method, args );
   		}
			
   		return( result );
   	}

        protected boolean
    getDebug()
    {
        return JCMDDebug.getInstance().getDebug( getDebugID() );
    }
   
		protected void
	debugMethod( final String methodName, final Object... args )
	{
	    if ( getDebug() )
	    {
	        mDebug.println( JCMDDebug.methodString( methodName, args ) );
	    }
	}
	
		protected void
	debugMethod(
	    final String msg,
	    final String methodName,
	    final Object... args )
	{
	    if ( getDebug() )
	    {
	        mDebug.println( JCMDDebug.methodString( methodName, args ) + ": " + msg );
	    }
	}
	
		protected void
	debug( final Object... args )
	{
	    if ( getDebug() )
	    {
	        mDebug.println( StringUtil.toString( "", args) );
	    }
	}
	
		protected void
	debug( final Object o )
	{
	    mDebug.println( o );
	}
}





