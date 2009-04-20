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
package org.glassfish.admin.amx.core.proxy;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.util.AMXDebug;

import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Extra;
import org.glassfish.admin.amx.core.Util;
import static org.glassfish.admin.amx.core.AMXConstants.*;

import org.glassfish.admin.amx.core.proxy.ProxyFactory;

import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.MBeanProxyHandler;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.StringUtil;

import javax.management.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import org.glassfish.admin.amx.core.PathnameParser;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
	Extends MBeanProxyHandler by also supporting the functionality required of an AMX.
 */
public final class AMXProxyHandler extends MBeanProxyHandler
	implements AMXProxy, Extra
{
    private static void sdebug( final String s ) { System.out.println(s); }
    
    private final ObjectName    mParentObjectName;
    private final String        mName;
	
    public static AMXProxyHandler getInvocationHandler(final AMXProxy amx)
    {
        return (AMXProxyHandler)Proxy.getInvocationHandler(amx);
    }

        public <T extends AMXProxy> T
    as(final Class<T> intf)
    {
        final Class<?> thisClass = this.getClass();
        
        if ( this.getClass().isAssignableFrom(intf) )
        {
            return intf.cast( this );
        }

        return getProxyFactory().getProxy( getObjectName(), getMBeanInfo(), intf);
        
        //throw new IllegalArgumentException( "Cannot convert " + getObjectName() + 
        // " to interface " + intf.getName() + ", interfaceName from Descriptor = " + interfaceName());
    }
        
		public Extra
	extra()
	{
		return this;
	}

		public AMXProxyHandler
	handler()
	{
		return this;
	}

	/**
		Create a new AMX proxy.
		
		@param connectionSource	the connection
		@param proxiedMBeanObjectName	the ObjectName of the proxied MBean
		@param mbeanInfo    will be fetched if null
	 */
		protected
	AMXProxyHandler(
		final MBeanServerConnection	conn,
		final ObjectName			objectName,
        final MBeanInfo             mbeanInfo )
		throws IOException
	{
		super( conn, objectName, mbeanInfo);
		
        mParentObjectName = (ObjectName)getAttributeNoThrow(ATTR_PARENT);
        mName             = (String)getAttributeNoThrow(ATTR_NAME);
	}
	
	private static final String		CREATE	= "create";
	private static final String		GET	= "get";
	
	private static final String		MAP_SUFFIX	= "Map";
	private static final String		SET_SUFFIX	= "Set";
	private static final String		LIST_SUFFIX	= "List";
//	private static final String		OBJECT_NAME_MAP_SUFFIX= "ObjectName" + MAP_SUFFIX;
//	private static final String		OBJECT_NAME_SET_SUFFIX= "ObjectName" + SET_SUFFIX;
//	private static final String		OBJECT_NAME_LIST_SUFFIX= "ObjectName" + LIST_SUFFIX;
	
//	private static final String		OBJECT_NAME_SUFFIX	= "ObjectName";
	
	private static final String	DOMAIN_ROOT		= "DomainRoot";
	private static final String	ATTRIBUTE_NAMES	= "AttributeNames";
	
	public final static String	ADD_NOTIFICATION_LISTENER		= "addNotificationListener";
	public final static String	REMOVE_NOTIFICATION_LISTENER	= "removeNotificationListener";
	
	private final static String	QUERY	= "query";
	
		public ProxyFactory
	getProxyFactory()
	{
		return( ProxyFactory.getInstance( getMBeanServerConnection() ) );
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
	
	private static final String[]	GET_OBJECT_NAMES_SIG_EMPTY	= EMPTY_SIG;
	private static final String[]	GET_OBJECT_NAMES_SIG_STRING	= STRING_SIG;
	

        protected <T extends AMXProxy> T
	getProxy(final ObjectName	objectName, final Class<T> intf)
	{
   		return( getProxyFactory().getProxy( objectName, intf ) );
	}
    
        protected AMXProxy
	getProxy(final ObjectName	objectName )
	{
   		return getProxy( objectName, AMXProxy.class );
	}
    
	
// 	/**
// 		Return true if the method is one that is requesting a Map of AMX object.
// 	 */
// 		protected static boolean
// 	isProxyMapGetter(
// 		final Method	method,
// 		final int		argCount )
// 	{
// 		boolean	isProxyMapGetter	= false;
// 		
// 		final String	name	= method.getName();
// 		if ( name.startsWith( GET ) &&
// 			name.endsWith( MAP_SUFFIX ) && 
// 			(! name.endsWith( OBJECT_NAME_MAP_SUFFIX )) && 
// 			argCount <= 1 &&
// 			Map.class.isAssignableFrom( method.getReturnType() ) )
// 		{
// 			isProxyMapGetter	= true;
// 		}
// 		
// 		return( isProxyMapGetter );
// 	}
// 	
// 	/**
// 		Return true if the method is one that is requesting a List of AMX object.
// 	 */
// 		protected static boolean
// 	isProxyListGetter(
// 		final Method	method,
// 		final int		argCount )
// 	{
// 		boolean	isProxyListGetter	= false;
// 		
// 		final String	name	= method.getName();
// 		if ( ( name.startsWith( GET ) || name.startsWith( QUERY ) ) &&
// 			name.endsWith( LIST_SUFFIX ) && 
// 			(! name.endsWith( OBJECT_NAME_LIST_SUFFIX )) && 
// 			argCount <= 1 &&
// 			List.class.isAssignableFrom( method.getReturnType() ) )
// 		{
// 			isProxyListGetter	= true;
// 		}
// 		
// 		return( isProxyListGetter );
// 	}
// 	/**
// 		@return true if the method is one that is requesting a Set of AMX.
// 	 */
// 		protected static boolean
// 	isProxySetGetter( final Method method, final int argCount )
// 	{
// 		boolean	isProxySetGetter	= false;
// 		
// 		final String	name	= method.getName();
// 		if ( ( name.startsWith( GET ) || name.startsWith( QUERY ) ) &&
// 			name.endsWith( SET_SUFFIX ) && 
// 			!name.endsWith( OBJECT_NAME_SET_SUFFIX ) && 
// 			argCount <= 2 &&
// 			Set.class.isAssignableFrom( method.getReturnType() ) )
// 		{
// 			isProxySetGetter	= true;
// 		}
// 		
// 		return( isProxySetGetter );
// 	}
	
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
			AMXProxy.class.isAssignableFrom( method.getReturnType() ) )
		{
			isProxyGetter	= true;
		}
		
		return( isProxyGetter );
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
	        result  = getMBeanServerConnection().getAttribute( getObjectName(), attributeName );
	    }
	    else
	    {
	        result  = getMBeanServerConnection().invoke( getObjectName(), methodName, args, sig );
	    }
	    
		return result;
	}
	
	/**
		The method is one that requests a Proxy.  Create the proxy by asking the
		target MBean for the appropriate ObjectName.   The resulting Proxy will implement
		the interface given as the return type of the Method.
	 */
		AMXProxy
	invokeSingleProxyGetter(
		final Object	myProxy,
    	final Method	method,
    	final Object[]	args )
		throws IOException, ReflectionException, InstanceNotFoundException, MBeanException,
		AttributeNotFoundException
	{
		final String	methodName	= method.getName();
		final int		numArgs	= (args == null) ? 0 : args.length;
		
        final Class<? extends AMXProxy>	returnClass	= method.getReturnType().asSubclass(AMXProxy.class);
        ObjectName		objectName	= null;
        
        if ( numArgs == 0 )
        {
    //System.out.println( "invokeSingleProxyGetter: intf = " + returnClass.getName() );
            if ( AMXProxy.class.isAssignableFrom(returnClass) && returnClass != AMXProxy.class )
            {
                final String	type	= Util.deduceType( returnClass );
    
    //System.out.println( "invokeSingleProxyGetter: type = " + type );
            
                // must be accessed as an Attribute, deduce type from return type
                final AMXProxy childProxy = child(type);
                objectName = childProxy == null ? null : childProxy.extra().objectName();
            }
            else
            {
                // try to get it as an Attribute
                objectName	= (ObjectName) invokeTarget( methodName, null, EMPTY_SIG);
            }
        }
        else if ( numArgs == 1 && args[ 0 ].getClass() == String.class )
        {
            objectName	= (ObjectName) invokeTarget( methodName, args, STRING_SIG );
        }
        else
        {
            getProxyLogger().warning( "Unknown form of proxy getter: " + method );
            assert( false );
            throw new IllegalArgumentException();
        }
            
        return objectName == null ? null : getProxy(objectName, returnClass).as(returnClass);
    }
	
	
	/**
        Does the method create a new MBean?
	 */
		protected static boolean
	isProxyCreator( final Method method )
	{
		final String	methodName	= method.getName();
		
		return( methodName.startsWith( CREATE ) &&
			AMXProxy.class.isAssignableFrom( method.getReturnType() ) );
	}
	
	/**
        Invoke a method that returns an ObjectName, creating a proxy for it.
	 */
		AMXProxy
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
			"received null ObjectName from: " + methodName + " on target " + getObjectName();
		
		final AMXProxy	proxy	= getProxy( objectName );
		
		return( proxy );
	}
	


	
		private static String
	toString( Object o )
	{
		//String result  = o == null ? "null" : SmartStringifier.toString( o );
        String result = "" + o;
		
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
	
	private static final Map<String,AMXProxy> EMPTY_String_AMX   = Collections.emptyMap();
	

	
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
		
	
    /** Cached forever, parent ObjectName */
	private static final String	GET_PARENT          = GET + ATTR_PARENT;
    
    /** proxy method */
	private static final String	METHOD_NAME          = "name";

    /** proxy method */
	private static final String	METHOD_CHILDREN      = "children";
    
    /** proxy method */
	private static final String	METHOD_CHILDREN_MAP  = "childrenMap";
    
    /** proxy method */
	private static final String	METHOD_CHILDREN_MAPS  = "childrenMaps";
    
    /** proxy method */
	private static final String	METHOD_CHILDREN_SET  = "childrenSet";
    
    /** proxy method */
	private static final String	METHOD_CHILD         = "child";
    
    /** proxy method */
	private static final String	METHOD_PARENT        = "parent";
    
    /** proxy method */
	private static final String	METHOD_OBJECTNAME    = "objectName";
    
    /** proxy method */
	private static final String	METHOD_EXTRA         = "extra";
    
    /** proxy method */
	private static final String	METHOD_AS            = "as";

    /** proxy method */
	private static final String	METHOD_VALID            = "valid";
    
    /** proxy method */
	private static final String	METHOD_ATTRIBUTES_MAP    = "attributesMap";
    
    /** proxy method */
	private static final String	METHOD_PATHNAME         = "path";
	
	/**
		These Attributes are handled specially.  For example, J2EE_TYPE and
		J2EE_NAME are part of the ObjectName.
	 */
	private static final Set<String> SPECIAL_METHOD_NAMES	= SetUtil.newUnmodifiableStringSet(
                GET_PARENT,
                
                METHOD_NAME,
                METHOD_PARENT,
				METHOD_CHILDREN_SET,
				METHOD_CHILDREN_MAP,
				METHOD_CHILDREN_MAPS,
                METHOD_CHILD,
                METHOD_OBJECTNAME,
                METHOD_EXTRA,
                METHOD_AS,
                METHOD_VALID,
                
                METHOD_ATTRIBUTES_MAP,
                METHOD_PATHNAME,
				
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
		boolean	handled	= false;
		
		if ( numArgs == 0 )
		{
            handled = true;
		    if ( methodName.equals( METHOD_PARENT )  )
			{
				result	= parent();
			}
			else if ( methodName.equals( GET_PARENT ) )
            {
                result = parent().extra().objectName();
            }
			else if ( methodName.equals( METHOD_CHILDREN_SET ) )
            {
                result = childrenSet();
            }
			else if ( methodName.equals( METHOD_CHILDREN_MAPS ) )
            {
                result = childrenMaps();
            }
            else if ( methodName.equals( METHOD_EXTRA ) )
			{
				result	= this;
			}
            else if ( methodName.equals( METHOD_OBJECTNAME ) )
			{
				result	= getObjectName();
			}
		    else if ( methodName.equals( METHOD_NAME )  )
			{
				result	= getObjectName().getKeyProperty( NAME_KEY );
			}
		    else if ( methodName.equals( METHOD_ATTRIBUTES_MAP )  )
			{
				result	= attributesMap();
			}
		    else if ( methodName.equals( METHOD_VALID)  )
			{
				result	= valid();
			}
		    else if ( methodName.equals( METHOD_PATHNAME)  )
			{
				result	= path();
			}
            else
			{
				handled	= false;
			}
		}
		else if ( numArgs == 1  )
		{
            handled = true;
            final Object arg = args[0];
            
            if ( methodName.equals( "equals" ) )
            {
                result =  equals( arg );
            }
			else if ( methodName.equals( METHOD_CHILDREN_MAP ) )
            {
                if ( arg instanceof String )
                {
                    result = childrenMap( (String)arg );
                }
                else if ( arg instanceof Class )
                {
                    result = childrenMap( (Class)arg );
                }
                else
                {
                    handled = false;
                }
            }
            else if ( methodName.equals( METHOD_CHILD ) )
			{
                if ( arg instanceof String )
                {
                    result = child( (String)arg );
                }
                else if ( arg instanceof Class )
                {
                    result = child( (Class)arg );
                }
                else
                {
                    handled = false;
                }
			}
            else if ( methodName.equals(METHOD_AS) && (arg instanceof Class) )
            {
                result = as((Class)arg);
            }
            else
            {
                handled = false;
            }
		}
        /*
        else if ( numArgs == 2 )
        {
            final Object arg1 = args[0];
            final Object arg2 = args[0];
            
            handled = true;
            if ( methodName.equals( METHOD_CHILD ) && (arg1 instanceof String) && (arg2 instanceof String) )
			{
				result	= child( (String)arg1, (String)arg2, null) ;
			}
            else
            {
                handled = false;
            }

        }
        */
		else
		{
            handled = true;
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
	   				"interfaces: " + toString( result.getClass().getInterfaces() ) +
	   				", ObjectName = " + getObjectName();
	   				
	   	    return result;
   		}
   		catch( IOException e )
   		{
   			getProxyFactory().checkConnection();
   			throw e;
   		}
   		catch( InstanceNotFoundException e )
   		{
   			isValid();
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
		
   		Object	result	= null;
   		
		final String	methodName	= method.getName();
		final int		numArgs	= args == null ? 0 : args.length;
		boolean	handled	= false;
//System.out.println( "_invoke: " + methodName + " on " + objectName() );
		
   		if ( SPECIAL_METHOD_NAMES.contains( methodName ) )
   		{
   			handled	= true;
   			result	= handleSpecialMethod( myProxy, method, args );
   		}
   		else if ( JMXUtil.isIsOrGetter( method ) )
   		{
   			assert( ! handled );
   		
   			final String	attrName	= JMXUtil.getAttributeName( method );
   		}
   		
   		if ( ! handled )
   		{
//System.out.println( "_invoke: (not handled): " + methodName + " on " + objectName() );
	   		if ( isSingleProxyGetter( method,  numArgs) )
	   		{
   				result	= invokeSingleProxyGetter( myProxy, method, args );
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
        
        // compatibility and convenience
        final Class<?> returnType = method.getReturnType();
        if ( result != null &&
            result instanceof ObjectName[] )
        {
            final ObjectName[] items = (ObjectName[])result;
            
            Class<? extends AMXProxy>  proxyClass = AMXProxy.class;
            if ( method.getGenericReturnType() instanceof ParameterizedType )
            {
                proxyClass = getProxyClass( (ParameterizedType)method.getGenericReturnType() );
            }
            
            if ( Set.class.isAssignableFrom(returnType) )
            {
               result = getProxyFactory().toProxySet( items, proxyClass);
            }
            else if ( List.class.isAssignableFrom(returnType) )
            {
               result = getProxyFactory().toProxyList( items, proxyClass );
            }
            else if ( Map.class.isAssignableFrom(returnType) )
            {
               result = getProxyFactory().toProxyMap( items, proxyClass );
            }
        }

        if ( getDebug() )
        {
    		debug( AMXDebug.methodString( methodName, args ) + " => " + toString( result ) );
		}
		
   		return( result );
   	}


        Class<? extends AMXProxy>
    getProxyClass( final ParameterizedType  pt)
    {
        Class<? extends AMXProxy> intf = null;
         
        final Type[] argTypes = pt.getActualTypeArguments();
        if ( argTypes.length >= 1 )
        {
            final Type argType = argTypes[ argTypes.length - 1];
            if ( (argType instanceof Class) && AMXProxy.class.isAssignableFrom( (Class)argType) )
            {
                intf = ((Class)argType).asSubclass(AMXProxy.class);
            }
        }
        if (intf == null )
        {
            intf = AMXProxy.class;
        }
        return intf;
    }
   
   
   		protected void
   	addNotificationListener( final Object[] args )
   		throws IOException, InstanceNotFoundException
   	{
   		final NotificationListener	listener	= (NotificationListener)args[ 0 ];
   		final NotificationFilter	filter		= (NotificationFilter)(args.length <= 1 ? null : args[ 1 ]);
   		final Object				handback	= args.length <= 1 ? null : args[ 2 ];
   		
   		getMBeanServerConnection().addNotificationListener(
   			getObjectName(), listener, filter, handback );
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
   		    getMBeanServerConnection().removeNotificationListener( getObjectName(), listener );
   		}
   		else
   		{
       		final NotificationFilter filter		= (NotificationFilter)args[ 1 ];
       		final Object             handback	= args[ 2 ];
       		
       		getMBeanServerConnection().removeNotificationListener(
       			getObjectName(), listener, filter, handback );
   	    }
   	}
    
//-----------------------------------    
    public static String getInterfaceName(final MBeanInfo info)
    {
        final Object value = info.getDescriptor().getFieldValue( DESC_STD_INTERFACE_NAME );
        return (String)value;
    }
    
    public static String getGenericInterfaceName(final MBeanInfo info)
    {
        final Object value = info.getDescriptor().getFieldValue( DESC_GENERIC_INTERFACE_NAME );
        return (String)value;
    }
    
    @Override
    public String interfaceName()
    {
        String name = super.interfaceName();
        if ( name == null )
        {
            name = AMXProxy.class.getName();
        }
        
        return name;
    }

    public boolean valid() { return isValid(); }

    public ProxyFactory proxyFactory()
    {
        return getProxyFactory();
    }

    public MBeanServerConnection mbeanServerConnection()
    {
        return getMBeanServerConnection();
    }

    public ObjectName objectName()
    {
        return getObjectName();
    }

    public String name() {
        // name as found in the ObjectName
        return getObjectName().getKeyProperty( NAME_KEY );
    }

    public String getName()
    {
        // internal *unquoted* name, but we consider it invariant once fetched
        return mName;
    }

    public ObjectName getParent()
    {
        return mParentObjectName;
    }
    
    
    public AMXProxy parent()
    {
        return mParentObjectName == null ? null : getProxyFactory().getProxy(mParentObjectName);
    }
    
    public String path()
    {
        // special case DomainRoot, which has no parent
        if ( getParent() == null )
        {
            return DomainRoot.PATH;
        }
        
        final ObjectName on = getObjectName();
        final String parentPath = Util.getParentPathProp(on);
        
        final String type = Util.getTypeProp( on );
        return PathnameParser.path( parentPath, type, singleton() ?  null : Util.getNameProp( on ) );
    }
    
    
    public ObjectName[] getChildren()
    {
        ObjectName[] objectNames = null;
        try
        {
            objectNames = (ObjectName[])getAttributeNoThrow(ATTR_CHILDREN);
        }
        catch( final Exception e )
        {
            final Throwable t = ExceptionUtil.getRootCause(e);
            if ( ! (t instanceof AttributeNotFoundException) )
            {
                throw new RuntimeException( "Could not get Children attribute", e );
            }
        }
        return objectNames;
    }
    
    /**
       Returns an array of children, including an empty array if there are none, but children
       are possible.  Returns null if children are not possible.
     */
    public Set<AMXProxy> childrenSet() {
        return childrenSet( getChildren() );
    }
    
     public Set<AMXProxy> childrenSet( final ObjectName[] objectNames) {
        return objectNames == null ? null : SetUtil.newSet( getProxyFactory().toProxy(objectNames) );
    }
    
    public Set<String> childrenTypes( final ObjectName[] objectNames)
    {
        final Set<String> types = new HashSet<String>();
        for( final ObjectName o : objectNames )
        {
            final String type = Util.getTypeProp(o);
            types.add(type);
        }
        return types;
    }


    public Map<String,AMXProxy> childrenMap( final String type) {
        return childrenMap(type, AMXProxy.class);
    }
    
    public <T extends AMXProxy> Map<String,T> childrenMap( final Class<T> intf) {
        if ( ! intf.isInterface() )
        {
            throw new IllegalArgumentException( "" + intf );
        }
        return childrenMap( Util.deduceType(intf), intf);
    }
    
    public <T extends AMXProxy> Map<String,T> childrenMap( final String type, final Class<T> intf) {
        final ObjectName[] objectNames = getChildren();
        if ( objectNames == null ) return null;

        final Map<String,T> m = new HashMap<String,T>();
        
        for( final ObjectName objectName : objectNames )
        {
            if ( Util.getTypeProp(objectName).equals(type) )
            {
                m.put( Util.getNameProp(objectName), getProxy(objectName, intf) );
            }
        }
        return m;
    }
    
    public Map<String, Map<String,AMXProxy>> childrenMaps() {
        final ObjectName[] children = getChildren();
        if ( children == null ) return null;
        
        final Set<AMXProxy> childrenSet = childrenSet(children);
        
        final Map<String,Map<String,AMXProxy>> maps = new HashMap<String,Map<String,AMXProxy>>();
        final Set<String>   types = childrenTypes(children);
        for( final String type : types )
        {
            maps.put( type, new HashMap<String,AMXProxy>() );
        }
        
        for( final AMXProxy proxy : childrenSet )
        {
            final Map<String,AMXProxy> m = maps.get( Util.getTypeProp(proxy) );
            m.put( proxy.name(), proxy);
        }
        return maps;
    }
    
    
    public <T extends AMXProxy> Set<T> childrenSet(final String type, final Class<T> intf) {
        final Map<String,T> m = childrenMap(type, intf);
        return m == null ? null : new HashSet<T>(m.values());
    }

    public AMXProxy child( final String type ) {
        return child( type, AMXProxy.class );
    }
    
    
    public <T extends AMXProxy> T child( final Class<T> intf ) {
        final String type = Util.deduceType(intf);
        //sdebug( "Deduced type of " + intf.getName() + " = " + type );
        return child( type, intf );
    }
    
    public <T extends AMXProxy> T child( final String type, final Class<T> intf) {
        //sdebug( "Child " + type + " has interface " + intf.getName() );
        final Map<String,T> children = childrenMap(type, intf);
        if ( children.size() == 0)
        {
            return null;
        }
        if ( children.size() > 1 ) {
            throw new IllegalArgumentException("Not a singleton: " + type);
        }
        
        final T child = children.values().iterator().next();
        if ( ! child.extra().singleton() ) {
            throw new IllegalArgumentException("Not a singleton: " + type);
        }

        return child;
    }

    public <T extends AMXProxy> T  child( final String type, final String name, final Class<T> intf) {
        final Set<AMXProxy> children = childrenSet();
        if ( children == null ) return null;

        T child = null;
        for( final AMXProxy c : children )
        {
            final ObjectName objectName = c.extra().objectName();
            if ( Util.getTypeProp(objectName).equals(type) && Util.getNameProp(objectName).equals(name) )
            {
                child = c.as(intf);
                break;
            }
        }
        return child;
    }
    
    public final MBeanInfo mbeanInfo() { return getMBeanInfo(); }
    
		public Map<String,Object>
	attributesMap( )
	{
		Map<String,Object>	result	= Collections.emptyMap();
		
		try
		{
			final Set<String>	names	= attributeNames();
			
            final String[] namesArray = names.toArray( new String[names.size()] );
			final AttributeList	attrs	= getAttributes(namesArray);
			
			result	= JMXUtil.attributeListToValueMap( attrs );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
		return( result );
	}

        public Map<String,Object>
	attributesMap( final String... args)
	{
        final Map<String,Object> all = attributesMap();
        final Map<String,Object> m = new HashMap<String,Object>();
        for( final String name : args )
        {
            if ( all.containsKey(name) )
            {
                m.put(name, all.get(name));
            }
        }
        return m;
    }
	
		public Set<String>
	attributeNames()
	{
		final String[] names	= JMXUtil.getAttributeNames( getMBeanInfo().getAttributes() );
        
        return SetUtil.newUnmodifiableStringSet(names);
	}
    

    protected <T> T getDescriptorField(final String name, final T defaultValue)
    {
        T value = (T)getMBeanInfo().getDescriptor().getFieldValue( name );
        if ( value == null )
        {
            value = defaultValue;
        }
        return value;
    }
               
    public boolean singleton()
    {
        return getDescriptorField(DESC_IS_SINGLETON, Boolean.FALSE);
    }
    
    public String pathPart()
    {
        return getDescriptorField(DESC_PATH_PART, (String)null);
    }
    
    public String group()
    {
        return getDescriptorField(DESC_GROUP, GROUP_OTHER);
    }
    
    public boolean supportsAdoption()
    {
        return getDescriptorField(DESC_SUPPORTS_ADOPTION, Boolean.FALSE);
    }
    
    private static final String[] EMPTY_STRINGS = new String[0];
    
    public String[] subTypes()
    {
        return getDescriptorField(DESC_SUB_TYPES, EMPTY_STRINGS);
    }
}





