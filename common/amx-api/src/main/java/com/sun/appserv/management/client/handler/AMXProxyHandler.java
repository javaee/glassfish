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
package com.sun.appserv.management.client.handler;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXAttributes;
import com.sun.appserv.management.base.AMXClientLogger;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Extra;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.jmx.MBeanProxyHandler;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.stringifier.ArrayStringifier;
import com.sun.appserv.management.util.stringifier.SmartStringifier;

import javax.management.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
	Extends MBeanProxyHandler by also supporting the functionality required of an AMX.
    <p><b>THREAD SAFE</b>
 */
class AMXProxyHandler extends MBeanProxyHandler
	implements Extra
{
	protected final PerMBeanCache	mCache;
	private boolean	mCheckedForInvariantMBeanInfo	= false;
	
	static protected final String DEBUG_ID =
	    "com.sun.appserv.management.client.handler.AMXProxyHandler";
	
	
	    protected String
	getDebugID()
	{
	    return DEBUG_ID;
	}
	
	/**
		Create a new AMX proxy.
		
		@param connectionSource	the connection
		@param proxiedMBeanObjectName	the ObjectName of the proxied MBean
	 */
		protected
	AMXProxyHandler(
		final ConnectionSource		connectionSource,
		final ObjectName			proxiedMBeanObjectName )
		throws IOException
	{
		super( connectionSource, proxiedMBeanObjectName );
		
        mDebug  = AMXDebug.getInstance().getOutput( getDebugID() );
		
		mCache	= new PerMBeanCache();
		
		setProxyLogger( AMXClientLogger.getInstance() );
	}
	
		protected void
	cacheAttribute( final Attribute attr )
	{
		mCache.cacheAttribute( attr );
	}
	
	/**
		Get an Attribute, first from the cache, but if not in the cache, fetching a new
		copy, then caching it.  This routine should only be used on invariant Attributes.
	 */
		protected Attribute
	getCachedAttribute( final String attrName )
		throws IOException, InstanceNotFoundException,
		MBeanException, AttributeNotFoundException, ReflectionException
	{
		Attribute	attr	= mCache.getCachedAttribute( attrName );
		
		if ( attr == null )
		{
			final MBeanServerConnection	conn	= getConnection();
				
			final Object value	= getConnection().getAttribute( getTargetObjectName(), attrName );
			attr	= new Attribute( attrName, value );
			mCache.cacheAttribute( attr );
		}
		
		return( attr );
	}
	
		protected Object
	getCachedAttributeValue( final String attrName )
		throws IOException, JMException
	{
		final Attribute	attr	= getCachedAttribute( attrName );
		
		assert( attr != null ) : "getCachedAttributeValue: null for " + attrName;
		return( attr == null ? null : attr.getValue() );
	}
	
	/**
		All proxies cached by ObjectName get cached by the ProxyFactory.
		
		Proxies keyed by other values may not be unique and need to be cached
		as items in mCache.
	 */
		private AMX
	getCachedProxy( Object key )
	{
		AMX	proxy	= null;
		
		if ( key instanceof ObjectName )
		{
			proxy	= getProxyFactory().getProxy( (ObjectName)key, AMX.class );
		}
		else
		{
			proxy	= Util.asAMX(mCache.getCachedItem( key ) );
		}
		
		if ( proxy != null )
		{
			final AMXProxyHandler	handler	= (AMXProxyHandler)Proxy.getInvocationHandler( proxy );
			if ( ! handler.targetIsValid() )
			{
				debug( "removing cached proxy for key: ", key );
				mCache.remove( key );
				proxy	= null;
			}
		}
		
		return( proxy );
	}
	
	/**
		A proxy cached by ObjectName can safely be shared globally, since the
		ObjectNames are unique per connection.  But non-ObjectName keys may
		actually conflict from like MBeans
	 */
		private void
	cacheProxy( final String key, final AMX proxy)
	{
		mCache.cacheItem( key, proxy );
	}
	
	
	private static final String		CREATE	= "create";
	private static final String		GET	= "get";
	
	private static final String		MAP_SUFFIX	= "Map";
	private static final String		SET_SUFFIX	= "Set";
	private static final String		LIST_SUFFIX	= "List";
	private static final String		OBJECT_NAME_MAP_SUFFIX= "ObjectName" + MAP_SUFFIX;
	private static final String		OBJECT_NAME_SET_SUFFIX= "ObjectName" + SET_SUFFIX;
	private static final String		OBJECT_NAME_LIST_SUFFIX= "ObjectName" + LIST_SUFFIX;
	
	private static final String		OBJECT_NAME_SUFFIX	= "ObjectName";
	
	private static final String	CONTAINEE_J2EE_TYPES	= Container.ATTR_CONTAINEE_J2EE_TYPES;
	
	
	private static final String	CONTAINER		= "Container";
	private static final String	DOMAIN_ROOT		= "DomainRoot";
	private static final String	MBEAN_INFO		= "MBeanInfo";
	private static final String	ATTRIBUTE_NAMES	= "AttributeNames";
	private static final String	J2EE_NAME		= "Name";
	private static final String	J2EE_TYPE		= "J2EEType";
	
	public final static String	ADD_NOTIFICATION_LISTENER		= "addNotificationListener";
	public final static String	REMOVE_NOTIFICATION_LISTENER	= "removeNotificationListener";
	
	private final static String	QUERY	= "query";

		protected ObjectName
	getContainerObjectName()
		throws IOException, JMException
	{
		return( (ObjectName)getCachedAttributeValue( AMXAttributes.ATTR_CONTAINER_OBJECT_NAME ) );
	}
	
	
		protected Class
	getProxyInterface( final ObjectName	objectName )
		throws IOException, JMException, ClassNotFoundException
	{
		// by fetching a proxy this way, it may already exist, with an already-cached
		// interface.
		final AMX	proxy	= getProxyFactory().getProxy( objectName, AMX.class );
		
		final Class	proxyInterface	= ClassUtil.getClassFromName( Util.getExtra( proxy ).getInterfaceName() );
		
		return( proxyInterface );
	}
	
		private String
	_getInterfaceName()
		throws IOException, JMException
	{
		return( (String)getCachedAttributeValue( AMXAttributes.ATTR_INTERFACE_NAME ) );
	}
		
		public String
	getInterfaceName()
	{
		try
		{
			return( _getInterfaceName() );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
		public ProxyFactory
	getProxyFactory()
	{
		return( ProxyFactory.getInstance( getConnectionSource() ) );
	}
	
	/**
		Get the proxy which is the parent of this one.
	 */
		synchronized Container
	getContainer( final AMX myProxy )
		throws IOException, JMException,
		ClassNotFoundException
	{
		Container	containerProxy	= null;
		
		if ( ! ( myProxy instanceof DomainRoot ) )
		{
            final ObjectName objectName = getContainerObjectName();
            
            // a few MBeans propogated from other instances, such as Logging,
            // do not have a Container.
            if ( objectName != null )
            {
                containerProxy =
                    getProxyFactory().getProxy( objectName, Container.class );
            }
		}
		
		return( containerProxy );
	}
	
	/**
		Get the proxy corresponding to the DomainMBean for the domain to which
		this proxy corresponds.
	 */
		private final DomainRoot
	getDomainRoot( )
		throws IOException
	{
		return( getProxyFactory().getDomainRoot( ) );
	}
	
	private static final String	STRING	= String.class.getName();
	
	private static final String[]	EMPTY_SIG		= new String[ 0 ];
	private static final String[]	STRING_SIG		= new String[] { STRING } ;
	private static final String[]	STRING2_SIG		= new String[] { STRING, STRING } ;
	
	private static final String		GET_SINGLETON_CONTAINEE	= "getSingletonContainee";
	private static final String		GET_CONTAINEE			= "getContainee";
	private static final String		GET_SINGLETON_CONTAINEE_OBJECT_NAME=
										GET_SINGLETON_CONTAINEE + OBJECT_NAME_SUFFIX;
	private static final String[]	GET_SINGLETON_CONTAINEE_OBJECT_NAME_SIG1	= STRING_SIG;
	private static final String[]	GET_SINGLETON_CONTAINEE_OBJECT_NAME_SIG2	= STRING2_SIG;
	
	private static final String[]	GET_OBJECT_NAMES_SIG_EMPTY	= EMPTY_SIG;
	private static final String[]	GET_OBJECT_NAMES_SIG_STRING	= STRING_SIG;
	

		protected synchronized AMX
	createProxy(
		final ObjectName	objectName )
	{
   		return( getProxyFactory().getProxy( objectName, AMX.class ) );
	}
	
	/**
		Return true if the method is one that is requesting a Map of AMX object.
	 */
		protected static boolean
	isProxyMapGetter(
		final Method	method,
		final int		argCount )
	{
		boolean	isProxyMapGetter	= false;
		
		final String	name	= method.getName();
		if ( name.startsWith( GET ) &&
			name.endsWith( MAP_SUFFIX ) && 
			(! name.endsWith( OBJECT_NAME_MAP_SUFFIX )) && 
			argCount <= 1 &&
			Map.class.isAssignableFrom( method.getReturnType() ) )
		{
			isProxyMapGetter	= true;
		}
		
		return( isProxyMapGetter );
	}
	
	/**
		Return true if the method is one that is requesting a List of AMX object.
	 */
		protected static boolean
	isProxyListGetter(
		final Method	method,
		final int		argCount )
	{
		boolean	isProxyListGetter	= false;
		
		final String	name	= method.getName();
		if ( ( name.startsWith( GET ) || name.startsWith( QUERY ) ) &&
			name.endsWith( LIST_SUFFIX ) && 
			(! name.endsWith( OBJECT_NAME_LIST_SUFFIX )) && 
			argCount <= 1 &&
			List.class.isAssignableFrom( method.getReturnType() ) )
		{
			isProxyListGetter	= true;
		}
		
		return( isProxyListGetter );
	}
	
	/**
		Return true if the method is one that is requesting a single AMX object.
		Such methods are client-side methods and do not operate on the target MBean.
	 */
		protected static boolean
	isSingleProxyGetter( final Method method, final int argCount )
	{
		boolean	isProxyGetter	= false;
		
		final String	name	= method.getName();
		if ( ( name.startsWith( GET ) || name.startsWith( QUERY ) ) &&
			argCount <= 2 &&
			AMX.class.isAssignableFrom( method.getReturnType() ) )
		{
			isProxyGetter	= true;
		}
		
		return( isProxyGetter );
	}
	
	/**
		@return true if the method is one that is requesting a Set of AMX.
	 */
		protected static boolean
	isProxySetGetter( final Method method, final int argCount )
	{
		boolean	isProxySetGetter	= false;
		
		final String	name	= method.getName();
		if ( ( name.startsWith( GET ) || name.startsWith( QUERY ) ) &&
			name.endsWith( SET_SUFFIX ) && 
			!name.endsWith( OBJECT_NAME_SET_SUFFIX ) && 
			argCount <= 2 &&
			Set.class.isAssignableFrom( method.getReturnType() ) )
		{
			isProxySetGetter	= true;
		}
		
		return( isProxySetGetter );
	}
	
	
		private static String
	proxyGetterToObjectNameGetter( final String methodName )
	{
		return( methodName + OBJECT_NAME_SUFFIX );
	}
	
		private Object
	invokeTarget(
		final String	methodName,
		final Object[]	args,
		final String[]	sig )
		throws IOException, ReflectionException, InstanceNotFoundException, MBeanException,
		AttributeNotFoundException
	{
	    final int   numArgs = args == null ? 0 : args.length;
	    
	    Object  result  = null;
	    
	    if ( numArgs == 0 &&
	        methodName.startsWith( GET ) )
	    {
	        final String    attributeName   = StringUtil.stripPrefix( methodName, GET );
	        result  = getConnection().getAttribute( getTargetObjectName(), attributeName );
	    }
	    else
	    {
	        result  = getConnection().invoke( getTargetObjectName(), methodName, args, sig );
	    }
	    
		return result;
	}
	
		private String
	getJ2EEType( final Class c )
	{
		return( (String)ClassUtil.getFieldValue( c, "J2EE_TYPE" ) );
	}
	
	/**
		The method is one that requests a Proxy.  Create the proxy by asking the
		target MBean for the appropriate ObjectName.   The resulting Proxy will implement
		the interface given as the return type of the Method.
	 */
		AMX
	invokeSingleProxyGetter(
		final Object	myProxy,
    	final Method	method,
    	final Object[]	args )
		throws IOException, ReflectionException, InstanceNotFoundException, MBeanException,
		AttributeNotFoundException
	{
		// use the methodName as the key for the cache
		final String	methodName	= method.getName();
		final int		numArgs	= (args == null) ? 0 : args.length;
		
		final String	argString	= args == null ? "" : ArrayStringifier.stringify( args, "_" );
		final String 	cacheKey	= methodName + argString;
		
		AMX		proxy	= getCachedProxy( cacheKey );
	
		if ( proxy == null )
		{
			final Class		returnClass	= method.getReturnType();
			ObjectName		objectName	= null;
			final String	j2eeType	= getJ2EEType( returnClass );
				
			if ( numArgs == 0 )	// of the form getXXX() eg getSSLConfig()
			{
				final String newMethodName	= proxyGetterToObjectNameGetter( methodName );
				objectName	= (ObjectName) invokeTarget( newMethodName, null, EMPTY_SIG);
			}
			else if ( numArgs == 1 && args[ 0 ].getClass() == String.class )
			{
				final String newMethodName	= proxyGetterToObjectNameGetter( methodName );
				objectName	= (ObjectName) invokeTarget( newMethodName, args, STRING_SIG );
			}
			else if ( (methodName.equals( GET_SINGLETON_CONTAINEE )  ||
					methodName.equals( GET_CONTAINEE )) && numArgs == 2 )
			{
				// getContainee( j2eeType, name )
				final String newMethodName	= proxyGetterToObjectNameGetter( methodName );
				
				objectName	= (ObjectName)
					invokeTarget( newMethodName, args, GET_SINGLETON_CONTAINEE_OBJECT_NAME_SIG2 );
			}
			else
			{
				getProxyLogger().warning( "Unknown form of proxy getter: " + method );
				assert( false );
				throw new IllegalArgumentException();
			}
				
			if ( objectName != null )
			{
				proxy	= createProxy(  objectName );
			}
			
			// the underlying object may not exist, this occurs normally sometimes
			if ( proxy != null )
			{
				if ( cacheKey != null )
				{
					//debug( "CACHING: " + cacheKey + " => " + Util.getExtra( proxy ).getObjectName);
					cacheProxy( cacheKey, proxy );
				}
				else
				{
					//debug( "NOT CACHING: " + Util.getExtra( proxy ).getObjectName);
				}
			}
			else
			{
				getProxyLogger().fine( "invokeSingleProxyGetter: NULL ObjectName for: " +
				    methodName + "()" );
		    }
		}
		else
		{
			//debug( "FOUND CACHED using \"" + cacheKey  + "\": " + Util.getExtra( proxy ).getObjectName);
		}
		
		return( proxy );
	}
	
	
		protected static boolean
	isProxyCreator( final Method method )
	{
		final String	methodName	= method.getName();
		
		return( methodName.startsWith( CREATE ) &&
			AMX.class.isAssignableFrom( method.getReturnType() ) );
	}
	
	/**
	 */
		AMX
	invokeProxyCreator(
    	final Method	method,
    	final Object[]	args )
		throws IOException, ReflectionException, InstanceNotFoundException, MBeanException,
		AttributeNotFoundException
	{
		final String	methodName	= method.getName();
		
		final String[]	stringSig	= getStringSig( method );
		final ObjectName	objectName	= (ObjectName)invokeTarget( methodName, args, stringSig );
		assert( objectName != null ) :
			"received null ObjectName from: " + methodName + " on target " + getTargetObjectName();
		
		final AMX	proxy	= createProxy( objectName );
		assert( getProxyFactory().getProxy( Util.getExtra( proxy ).getObjectName(),AMX.class, false ) == proxy );
		
		return( proxy );
	}
	


	
		private static String
	toString( Object o )
	{
		String result  = o == null ? "null" : SmartStringifier.toString( o );
		
        final int   MAX_LENGTH  = 256;
        if ( result.length() > MAX_LENGTH )
        {
            result  = result.substring( 0, MAX_LENGTH - 1 ) + "...";
        }
        
        return result;
	}
	
		private static String[]
	getStringSig( final Method method )
	{
		final Class[]	sig	= method.getParameterTypes();
		final String[]	stringSig	= ClassUtil.classnamesFromSignature( sig );
		return( stringSig );
	}


		protected static String
	convertMethodName(
		final String srcName,
		final String srcSuffix,
		final String resultSuffix )
	{
		if ( ! srcName.endsWith( srcSuffix ) )
		{
			throw new IllegalArgumentException( srcName + " does not end with " + srcSuffix );
		}
		final String	baseName	= srcName.substring( 0, srcName.lastIndexOf( srcSuffix ) );
		
		return( baseName + resultSuffix );
	}
	
	private static final Map<String,AMX> EMPTY_String_AMX   = Collections.emptyMap();
	
		private Map<String,?>
	invokeProxyMapGetter(
		final Object	myProxy,
    	final Method	method,
    	final Object[]	args )
		throws java.io.IOException, ReflectionException, InstanceNotFoundException, MBeanException,
		ClassNotFoundException, AttributeNotFoundException, JMException
	{
		final int	argCount	= args == null ? 0 : args.length;
		
		// turn getXXXObjectNameMap() into getXXXMap()
		final String	methodName	= method.getName();
		final String	getObjectNameMapName	=
			convertMethodName( methodName, MAP_SUFFIX, OBJECT_NAME_MAP_SUFFIX );
		
		final MBeanServerConnection	conn	= getConnection();
		
		final Map<String,?> m = TypeCast.asMap( 
		    invokeTarget( getObjectNameMapName, args, getStringSig( method ) ) );
		assert( m != null ) :
			"mbean " + getTargetObjectName() + " returned null Map for " + getObjectNameMapName;
		
		/*
			The Map may be either a:
			- Map of <name>=<ObjectName>
			- Map of <j2eeType>=<Map of <name>=<ObjectName>
		 */
		Map<String,?>	result	= null;
		if ( m.keySet().size() != 0 )
		{
		    final ProxyFactory  proxyFactory    = getProxyFactory();
		
			final Object firstValue	= m.values().iterator().next();
			
			if ( firstValue instanceof ObjectName )
			{
				// it's <name>=<ObjectName>
				final Map<String,ObjectName>  onm = TypeCast.asMap( m );
				final Map<String,AMX> proxyMap	= proxyFactory.toProxyMap( onm );
				result  = proxyMap;
			}
			else if ( firstValue instanceof Map )
			{
				final Map<String,Map<String,ObjectName>> objectNameMaps = TypeCast.asMap( m );
				final Map<String,Map<String,AMX>> proxyMaps	= new HashMap<String,Map<String,AMX>>();
				
				for ( final String j2eeType : objectNameMaps.keySet() )
				{
					final Map<String,ObjectName> objectNameMap	= objectNameMaps.get( j2eeType );
					final Map<String,AMX> proxyMap	= proxyFactory.toProxyMap( objectNameMap );
					proxyMaps.put( j2eeType, proxyMap ); 	
				}
				
				result  = proxyMaps;
			}
			else
			{
			    throw new IllegalArgumentException();
			}
		}
		else
		{
		    result  = EMPTY_String_AMX;
		}
		
		return( result );
	}
	
		private List<AMX>
	invokeProxyListGetter(
		final Object	myProxy,
    	final Method	method,
    	final Object[]	args )
		throws java.io.IOException, ReflectionException, InstanceNotFoundException, MBeanException,
		ClassNotFoundException, AttributeNotFoundException, JMException
	{
		// get the List<ObjectName> from the MBean
		final String	remoteNAME	=
			convertMethodName( method.getName(), LIST_SUFFIX, OBJECT_NAME_LIST_SUFFIX );
		final List<ObjectName>	objectNames	= TypeCast.asList(
		    invokeTarget( remoteNAME, args, getStringSig( method ) ) );
		
		final List<AMX>	result	= getProxyFactory().toProxyList( objectNames );
		
		return( result );
	}
	
	/**
		The method is one that requests a Set of Proxies.  Create the proxies by asking the
		target MBean for the ObjectNames. Then generate proxies of the appropriate type
		for each resulting ObjectName.
	 */
		private Set<AMX>
	invokeProxySetGetter(
		final Object	myProxy,
    	final Method	method,
    	final Object[]	args )
		throws java.io.IOException, JMException, ClassNotFoundException
	{
		assert( Set.class.isAssignableFrom( method.getReturnType() ) );
	
		final String	methodName	= method.getName();
		
		final String	getObjectNamesName	=
			convertMethodName( methodName, SET_SUFFIX, OBJECT_NAME_SET_SUFFIX );
		
		final MBeanServerConnection	conn	= getConnection();
		
		final String[]	stringSig	= getStringSig( method );
		// ask the MBean for an ObjectName corresponding to an id (name)
		final Set<ObjectName>	objectNames	= TypeCast.asSet( invokeTarget( getObjectNamesName, args, stringSig ) );
		
		final Set<AMX>	proxies	= getProxyFactory().toProxySet( objectNames );
		
		return( proxies );
	}
	
	
	private final static Class[]	NOTIFICATION_LISTENER_SIG1	= new Class[]
	{
		NotificationListener.class
	};
	private final static Class[]	NOTIFICATION_LISTENER_SIG2	= new Class[]
	{
		NotificationListener.class,
		NotificationFilter.class,
		Object.class
	};
	
	
	
		private synchronized MBeanInfo
	_getMBeanInfo()
		throws IOException,
		InstanceNotFoundException, ReflectionException, IntrospectionException
	{
		MBeanInfo	mbeanInfo	= null;
        
		if ( ! mCheckedForInvariantMBeanInfo )
		{
			mCheckedForInvariantMBeanInfo	= true;
			
			// see if target has the boolean which tells us if caching is OK
			try
			{
				final Boolean	cacheIt	= (Boolean)
					getAttribute( AMXAttributes.ATTR_MBEAN_INFO_IS_INVARIANT );
				setMBeanInfoIsInvariant( cacheIt.booleanValue() );
				cacheMBeanInfo( cacheIt.booleanValue() );
				
			}
			catch( Exception e )
			{
				// not found, or other problem, have to assume we can't cache it
				cacheMBeanInfo( false );
				setMBeanInfoIsInvariant( false );
			}
		}

		mbeanInfo	= getMBeanInfo( getCacheMBeanInfo() );
		
		return( mbeanInfo );
	}
	
	
		public MBeanInfo
	getMBeanInfo()
	{
		try
		{
			return( _getMBeanInfo() );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
		public ObjectName
	getObjectName()
	{
		return( getTargetObjectName() );
	}
	
		public Map<String,Object>
	getAllAttributes( )
	{
		Map<String,Object>	result	= Collections.emptyMap();
		
		try
		{
			final String[]	names	= getAttributeNames();
			
			final AttributeList	attrs	= getAttributes(names );
			
			result	= JMXUtil.attributeListToValueMap( attrs );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
		return( result );
	}
	
		public String[]
	getAttributeNames()
	{
		final String	attrName	= "AttributeNames";
		
		Attribute	attr	= null;
		try
		{
			attr	= getCachedAttribute( attrName );
		}
		catch( AttributeNotFoundException e )
		{
			// it's supposed to be there!
			attr	= null;
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
		
		String[]	names	= null;
		if ( attr == null )
		{
			final MBeanInfo	mbeanInfo	= getMBeanInfo();
		
			names	= JMXUtil.getAttributeNames( mbeanInfo.getAttributes() );
			if ( getMBeanInfoIsInvariant() )
			{
				// only cache if MBeanInfo is invariant
				cacheAttribute( new Attribute( attrName, names ) );
			}
		}
		else
		{
			names	= (String[])attr.getValue();
		}
		
		return( names );
	}
	
	/**
		The values of these Attributes are cached forever.  Proxies are handled separately
		because the API will be getXXX() wherease the Attribute name will be XXXObjectName.
	 */
	private static final Set<String> CACHED_ATTRIBUTE_NAMES	= GSetUtil.newUnmodifiableStringSet(
				AMXAttributes.ATTR_MBEAN_INFO_IS_INVARIANT,
				AMXAttributes.ATTR_INTERFACE_NAME,
				AMXAttributes.ATTR_GROUP,
				AMXAttributes.ATTR_FULL_TYPE,
				CONTAINEE_J2EE_TYPES );

	private static final String	GET_MBEAN_INFO	= GET + MBEAN_INFO;
	private static final String	GET_J2EE_TYPE	= GET + J2EE_TYPE;
	private static final String	GET_J2EE_NAME	= GET + J2EE_NAME;
	private static final String	GET_ATTRIBUTE_NAMES	= GET + ATTRIBUTE_NAMES;
	private static final String	GET_CONTAINER	= GET + CONTAINER;
	private static final String	GET_EXTRA		= GET + "Extra";
	private static final String	GET_ALL_ATTRIBUTES		= GET + "AllAttributes";
	private static final String	GET_DOMAIN_ROOT	= GET + DOMAIN_ROOT;
	private static final String	GET_OBJECT_NAME	= GET + AMXAttributes.ATTR_OBJECT_NAME;
	
	/**
		These Attributes are handled specially.  For example, J2EE_TYPE and
		J2EE_NAME are part of the ObjectName.
	 */
	private static final Set<String> SPECIAL_METHOD_NAMES	= GSetUtil.newUnmodifiableStringSet(
				GET_MBEAN_INFO,
				GET_J2EE_TYPE,
				GET_J2EE_NAME,
				GET_ATTRIBUTE_NAMES,
				GET_CONTAINER,
				GET_DOMAIN_ROOT,
				GET_OBJECT_NAME,
				GET_EXTRA,
				GET_ALL_ATTRIBUTES,
				
				ADD_NOTIFICATION_LISTENER,
				REMOVE_NOTIFICATION_LISTENER
			);
	
	/**
		Handle a "special" method; one that requires special handling and/or can
		be dealt with on the client side and/or can be handled most efficiently
		by special-casing it.
	 */
		private Object
	handleSpecialMethod(
		final Object		myProxy,
    	final Method		method,
		final Object[]		args )
		throws ClassNotFoundException, JMException, IOException
	{
		final String	methodName	= method.getName();
		final int		numArgs	= args == null ? 0 : args.length;
		Object	result	= null;
		boolean	handled	= true;
		
		if ( numArgs == 0 )
		{
		    if ( methodName.equals( GET_CONTAINER )  )
			{
				result	= getContainer( Util.asAMX(myProxy) );
			}
			else if ( methodName.equals( GET_EXTRA ) )
			{
				assert( this instanceof Extra );
				result	= this;
			}
			else if ( methodName.equals( GET_OBJECT_NAME ) )
			{
				result	= getTargetObjectName();
			}
			else if ( methodName.equals( GET_DOMAIN_ROOT ) )
			{
				result	= getDomainRoot( );
			}
			else if ( methodName.equals( GET_ATTRIBUTE_NAMES ) )
			{
				result	= getAttributeNames();
			}
			else if ( methodName.equals( GET_J2EE_TYPE ) )
			{
				result	= Util.getJ2EEType( getTargetObjectName() );
			}
			else if ( methodName.equals( GET_J2EE_NAME ) )
			{
				result	= Util.getName( getTargetObjectName() );
			} 
			else if ( methodName.equals( GET_ALL_ATTRIBUTES ) )
			{
				result	= getAllAttributes();
			}
			else
			{
				handled	= false;
			}
		}
		else if ( numArgs == 1 && methodName.equals( "equals" ) )
		{
		    return equals( args[ 0 ] );
		}
		else
		{
			final Class[]	signature	= method.getParameterTypes();
		
			if ( methodName.equals( ADD_NOTIFICATION_LISTENER ) &&
						(	ClassUtil.sigsEqual( NOTIFICATION_LISTENER_SIG1, signature ) ||
							ClassUtil.sigsEqual( NOTIFICATION_LISTENER_SIG2, signature ) )
					)
			{
				addNotificationListener( args );
			}
			else if ( methodName.equals( REMOVE_NOTIFICATION_LISTENER ) &&
						(	ClassUtil.sigsEqual( NOTIFICATION_LISTENER_SIG1, signature ) ||
							ClassUtil.sigsEqual( NOTIFICATION_LISTENER_SIG2, signature ) )
					)
			{
				removeNotificationListener( args );
			}
			else
			{
				handled	= false;
			}
		}
		
		if ( ! handled )
		{
			assert( false );
			throw new RuntimeException( "unknown method: " + method );
		}
		
		return( result );
	}
	
		public final Object
	invoke(
		final Object		myProxy,
    	final Method		method,
		final Object[]		args )
   		throws java.lang.Throwable
   	{
   		try
   		{
   			final Object result = _invoke( myProxy, method, args );
   			
	   		assert( result == null ||
	   			ClassUtil.IsPrimitiveClass( method.getReturnType() ) ||
	   			method.getReturnType().isAssignableFrom( result.getClass() ) ) :
	   				method.getName() + ": result of type " + result.getClass().getName() +
	   				" not assignable to " + method.getReturnType().getName() + ", " +
	   				"interfaces: " + toString( result.getClass().getInterfaces() +
	   				", ObjectName = " + JMXUtil.toString( getTargetObjectName() ) );
	   				
	   	    return result;
   		}
   		catch( IOException e )
   		{
   			getProxyFactory().checkConnection();
   			throw e;
   		}
   		catch( InstanceNotFoundException e )
   		{
   			checkValid();
   			throw e;
   		}
   	}
   	
		protected Object
	_invoke(
		final Object		myProxy,
    	final Method		method,
		final Object[]		args )
   		throws java.lang.Throwable
   	{
		debugMethod( method.getName(), args );
		
   		// clients can retain proxies that go invalid if their corresponding
   		// MBeans are removed.
   		if ( ! targetIsValid() )
   		{
   			throw new InstanceNotFoundException( getTargetObjectName().toString() );
   		}
   		
   		Object	result	= null;
   		
		final String	methodName	= method.getName();
		final int		numArgs	= args == null ? 0 : args.length;
		
		boolean	handled	= false;
		
   		if ( SPECIAL_METHOD_NAMES.contains( methodName ) )
   		{
   			handled	= true;
   			result	= handleSpecialMethod( myProxy, method, args );
   		}
   		else if ( JMXUtil.isIsOrGetter( method ) )
   		{
   			assert( ! handled );
   		
   			final String	attrName	= JMXUtil.getAttributeName( method );
   			
   			if ( CACHED_ATTRIBUTE_NAMES.contains( attrName ) )
   			{
   				result	= getCachedAttributeValue( attrName );
   				handled	= true;
   			}
   		}
   		
   		if ( ! handled )
   		{
	   		if ( isSingleProxyGetter( method,  numArgs) )
	   		{
   				result	= invokeSingleProxyGetter( myProxy, method, args );
	   		}
	   		else if ( isProxySetGetter( method, numArgs ) )
	   		{
	   			result	= invokeProxySetGetter( myProxy, method, args );
	   		}
	   		else if ( isProxyMapGetter( method, numArgs ) )
	   		{
	   			result	= invokeProxyMapGetter( myProxy, method, args );
	   		}
	   		else if ( isProxyListGetter( method, numArgs ) )
	   		{
	   			result	= invokeProxyListGetter( myProxy, method, args );
	   		}
	   		else if ( isProxyCreator( method ) )
	   		{
	   			result	= invokeProxyCreator( method, args );
	   		}
	   		else
	   		{
   				result	= super.invoke( myProxy, method, args );
	   		}
   		}

        if ( getDebug() )
        {
    		debug( AMXDebug.methodString( methodName, args ) +
    		    " => " + toString( result ) );
		}
		
   		return( result );
   	}
   
   
   		protected void
   	addNotificationListener( final Object[] args )
   		throws IOException, InstanceNotFoundException
   	{
   		final NotificationListener	listener	= (NotificationListener)args[ 0 ];
   		final NotificationFilter	filter		= (NotificationFilter)(args.length <= 1 ? null : args[ 1 ]);
   		final Object				handback	= args.length <= 1 ? null : args[ 2 ];
   		
   		getConnection().addNotificationListener(
   			getTargetObjectName(), listener, filter, handback );
   	}
   	
   		protected void
   	removeNotificationListener( final Object[] args )
   		throws IOException, InstanceNotFoundException, ListenerNotFoundException
   	{
   		final NotificationListener	listener	= (NotificationListener)args[ 0 ];
   		
   		// important:
   		// this form removes the same listener registered with different filters and/or handbacks
   		if ( args.length == 1 )
   		{
   		    getConnection().removeNotificationListener( getTargetObjectName(), listener );
   		}
   		else
   		{
       		final NotificationFilter filter		= (NotificationFilter)args[ 1 ];
       		final Object             handback	= args[ 2 ];
       		
       		getConnection().removeNotificationListener(
       			getTargetObjectName(), listener, filter, handback );
   	    }
   	}
}





