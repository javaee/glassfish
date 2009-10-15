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
 
package com.sun.cli.jmxcmd.util;

import javax.management.*;
import java.io.IOException;
import java.util.Set;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


/**
	This class wraps an MBeanServerConnection and provides hooks on each method call
	via its Hook interface.
	
	Typical used would be for a subclass to 
 */
public class MBeanServerConnection_Hook implements MBeanServerConnection
{
	private final MBeanServerConnection		mConn;
	
	/**
		Prior to a method being called, preHook() is called; the 2 variants
		correspond to either no arguments or 1 or more arguments.
		
		After a method is called, postHook() is called; the 3 variants
		correspond to no-arguments-no-return-value, arguments-no-return-value, and
		arguments-and-return-value methods.
		
		The callNameHook() is supplied to see just the ObjectName being used for
		methods that accept a fully-qualifed ObjectName (but not methods that take
		a pattern).  The callNameHook() may return a different ObjectName which will
		be used for the invocation.
		
		preHook() must return a unique call ID, which will be passed to nameHook() and
		postHook().  The id identifies the particular calls (useful in a threaded
		environment).
	 */
	public interface Hook
	{
		public long		preHook( String methodName );
		public long		preHook( String methodName, Object [] args );
		public void		postHook( long id, String methodName );
		public void		postHook( long id, String methodName, Object [] args );
		public void		postHook( long id, String methodName, Object [] args, Object result );
		public ObjectName	nameHook( long id, ObjectName methodName ) throws IOException;
		

		/**
			Should throw the exception 'e'
		 */
		public void		IOExceptionHook( long id, IOException e,
							String operationName, ObjectName objectName, Object[] allArgs )
							throws IOException;
							
		public void		InstanceNotFoundExceptionHook( String methodName, long id, InstanceNotFoundException e)
							throws InstanceNotFoundException;
	}
	
	static public class HookImpl
		implements Hook
	{
		public static final  HookImpl	HOOK	= new HookImpl();
		
		long	mID;
			public 
		HookImpl()
		{
			mID	= 0;
		}
		
			synchronized long
		getNewID()
		{
			return( mID++ );
		}

		public long		preHook( String methodName )	{ return( getNewID() ); }
		public long		preHook( String methodName, Object [] args )	{ return( getNewID() ); }
		public void		postHook( long id, String methodName ) {}
		public void		postHook( long id, String methodName, Object [] args )  {}
		public void		postHook( long id, String methodName, Object [] args, Object result )  {}
		public ObjectName	nameHook( long id, ObjectName methodName ) throws IOException { return( methodName ); }
		
		public void		IOExceptionHook( long id, IOException e,
							String operationName, ObjectName objectName, Object[] allArgs )
							throws IOException
								{ throw e; }
								
		public void		InstanceNotFoundExceptionHook( String methodName, long id, InstanceNotFoundException e)
							throws InstanceNotFoundException
						{ throw e; }
						
			
			String
		getInvocationString( String methodName, final Object [] args )
		{
			assert( methodName != null );
			
			String	msg	= methodName + "(";
			
			if ( args != null )
			{
				for( int i = 0; i < args.length; ++i )
				{
					final Object	arg	= args[ i ];
					
					String	s	= "";
					if ( arg == null )
					{
						s	= "null";
					}
					else if ( arg instanceof String || arg instanceof ObjectName )
					{
						s	= "\"" + arg + "\"";
					}
					else
					{
						s	= arg.toString();
					}
					
					msg	= msg + s;
					if ( i != args.length - 1 )
					{
						msg	= msg + ",";
					}
				}
			}
			
			msg	= msg + ")";
			
			return( msg );
		}
		
			String
		getInvocationString( final long id, String methodName, final Object [] args )
		{
			return "" + id + getInvocationString( methodName, args );
		}
	}
	
		Hook
	getHook()
	{
		return( HookImpl.HOOK );
	}

		public
	MBeanServerConnection_Hook( MBeanServerConnection conn  )
	{
		mConn	= conn;
		
		assert( getConn() != null );
		
	}
	
		MBeanServerConnection
	getConn()
	{
		return( mConn );
	}
	
		void
	callIOExceptionHook(
		long		id,
		IOException	e,
		String		operationName,
		ObjectName	objectName,
		Object[]	allArgs ) throws IOException
	{
		getHook().IOExceptionHook( id, e, operationName, objectName, allArgs );
	}
	
		long
	callPreHook( String name, Object [] args )
	{
		return( getHook().preHook( name, args ) );
	}
	
		long
	callPreHook( String name )
	{
		return( getHook().preHook( name ) );
	}
	
		void
	callPostHook( long id, String name, Object [] args, Object result )
	{
		getHook().postHook( id, name, args, result );
	}
	
		void
	callPostHook( long id, String name, Object [] args )
	{
		getHook().postHook( id, name, args );
	}
	
		void
	callPostHook( long id, String name  )
	{
		getHook().postHook( id, name );
	}
	
		ObjectName
	callNameHook( long id, ObjectName	objectName )
		throws IOException
	{
		return( getHook().nameHook( id, objectName ) );
	}
	
	public static final String	CREATE_MBEAN			= "createMBean";
	public static final String	UNREGISTER_MBEAN		= "unregisterMBean";
	public static final String	REGISTER_MBEAN			= "registerMBean";
	public static final String	GET_OBJECT_INSTANCE		= "getObjectInstance";
	public static final String	QUERY_MBEANS			= "queryMBeans";
	public static final String	QUERY_NAMES				= "queryNames";
	public final static String	GET_DOMAINS				= "getDomains";
	public static final String	IS_REGISTERED			= "isRegistered";
	public static final String	GET_MBEAN_COUNT			= "getMBeanCount";
	public static final String	GET_ATTRIBUTE			= "getAttribute";
	public static final String	GET_ATTRIBUTES			= "getAttributes";
	public static final String	SET_ATTRIBUTE			= "setAttribute";
	public static final String	SET_ATTRIBUTES			= "setAttributes";
	public static final String	INVOKE					= "invoke";
	public static final String	GET_DEFAULT_DOMAIN		= "getDefaultDomain";
	public final static String	ADD_NOTIFICATION_LISTENER	= "addNotificationListener";
	public final static String	REMOVE_NOTIFICATION_LISTENER	= "removeNotificationListener";
	public final static String	GET_MBEAN_INFO	= "getMBeanInfo";
	public final static String	IS_INSTANCE_OF	= "isInstanceOf";

	
	
	public ObjectInstance createMBean(String className, ObjectName name)
		throws ReflectionException, InstanceAlreadyExistsException,
		   MBeanRegistrationException, MBeanException,
		   NotCompliantMBeanException, IOException
	{
		final Object []	args	= new Object [] { className, name };
		
		final long id = callPreHook( CREATE_MBEAN, args );
		
		ObjectInstance	result	= null;
		try
		{
			result	= getConn().createMBean( className, name );
			callPostHook( id, CREATE_MBEAN, args, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, CREATE_MBEAN, name, args );
		}
		
		return( result );
	}

	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) 
		throws ReflectionException, InstanceAlreadyExistsException,
		   MBeanRegistrationException, MBeanException,
		   NotCompliantMBeanException, InstanceNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { className, name, loaderName };
		final long id = callPreHook( CREATE_MBEAN, args );
		
		ObjectInstance	result	= null;
		try
		{
			result	= getConn().createMBean( className, name, loaderName );
			callPostHook( id, CREATE_MBEAN, args, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, CREATE_MBEAN, name, args );
		}
		catch( InstanceNotFoundException e )
		{
			getHook().InstanceNotFoundExceptionHook( CREATE_MBEAN, id, e );
		}
		
		return( result );
	}



	public ObjectInstance createMBean(String className, ObjectName name,
					  Object params[], String signature[]) 
		throws ReflectionException, InstanceAlreadyExistsException,
			   MBeanRegistrationException, MBeanException,
			   NotCompliantMBeanException, IOException
	{
		final Object []	args	= new Object [] { className, name, params, signature };
		final long id = callPreHook( CREATE_MBEAN, args );
		
		ObjectInstance	result	= null;
		try
		{
			result	= getConn().createMBean( className, name, params, signature );
			callPostHook( id, CREATE_MBEAN, args, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, CREATE_MBEAN, name, args );
		}
		
		return( result );
	}


	public ObjectInstance createMBean(String className, ObjectName name,
					  ObjectName loaderName, Object params[],
					  String signature[]) 
		throws ReflectionException, InstanceAlreadyExistsException,
			   MBeanRegistrationException, MBeanException,
			   NotCompliantMBeanException, InstanceNotFoundException,
			   IOException
	{
		final Object []	args	= new Object [] { className, name, loaderName, params, signature };
		final long id = callPreHook( CREATE_MBEAN, args );
		
		ObjectInstance	result	= null;
		try
		{
			result	= getConn().createMBean( className, name, loaderName, params, signature);
			
			callPostHook( id, CREATE_MBEAN, args, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, CREATE_MBEAN, name, args );
		}
		catch( InstanceNotFoundException e )
		{
			getHook().InstanceNotFoundExceptionHook( CREATE_MBEAN, id, e );
		}
		
		return( result );
	}

	
	public void unregisterMBean(ObjectName name)
		throws InstanceNotFoundException, MBeanRegistrationException,
			   IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( UNREGISTER_MBEAN, args );
		
		try
		{
			getConn().unregisterMBean( callNameHook( id, name ) );
			
			callPostHook( id, UNREGISTER_MBEAN, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, UNREGISTER_MBEAN, name, args );
		}
		catch( InstanceNotFoundException e )
		{
			getHook().InstanceNotFoundExceptionHook( UNREGISTER_MBEAN, id, e );
		}
	}


	public ObjectInstance getObjectInstance(ObjectName name)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( GET_OBJECT_INSTANCE, args );
		
		ObjectInstance	result	= null;
		try
		{
			result	= getConn().getObjectInstance( callNameHook( id, name ) );
			
			callPostHook( id, GET_OBJECT_INSTANCE, args, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, GET_OBJECT_INSTANCE, name, args );
		}
		catch( InstanceNotFoundException e )
		{
			getHook().InstanceNotFoundExceptionHook( GET_OBJECT_INSTANCE, id, e );
		}
		
		return( result );
	}


	public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
		throws IOException
	{
		final Object []	args	= new Object [] { name, query };
		final long id = callPreHook( QUERY_MBEANS, args );
		
		Set<ObjectInstance>	result	= null;
		try
		{
			result	= getConn().queryMBeans( name, query );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, QUERY_MBEANS, name, args );
		}
		
		callPostHook( id, QUERY_MBEANS, args, result );
		
		return( result );
	}


	public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
		throws IOException
	{
		final Object []	args	= new Object [] { name, query };
		final long id = callPreHook( QUERY_NAMES, args );
		
		Set<ObjectName>	result	= null;
		try
		{
			result	= getConn().queryNames( name, query );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, QUERY_NAMES, name, args );
		}
		
		callPostHook( id, QUERY_NAMES, args, result );
		
		return( result );
	}


	
	public boolean isRegistered(ObjectName name)
		throws IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( IS_REGISTERED, args );
		
		boolean	registered	= false;
		
		try
		{
			registered	= getConn().isRegistered( callNameHook( id, name) );
			
			callPostHook( id, IS_REGISTERED, args, registered ? Boolean.TRUE : Boolean.FALSE );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, IS_REGISTERED, name, args );
		}
		
		return( registered );
	}



	public Integer getMBeanCount()
		throws IOException
	{
		final long id = callPreHook( GET_MBEAN_COUNT, null );
		
		Integer	result	= null;
		try
		{
			result	= getConn().getMBeanCount( );
			
			callPostHook( id, GET_MBEAN_COUNT, null, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, GET_MBEAN_COUNT, null, null );
		}
		
		return( result );
	}


	public Object getAttribute(ObjectName name, String attribute)
		throws MBeanException, AttributeNotFoundException,
			   InstanceNotFoundException, ReflectionException,
			   IOException
	{
		final Object []	args	= new Object [] { name, attribute };
		final long id = callPreHook( GET_ATTRIBUTE, args );
		
		Object result	= null;
		try
		{
			result	= getConn().getAttribute( callNameHook( id, name ), attribute );
			
			callPostHook( id, GET_ATTRIBUTE, args, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, GET_ATTRIBUTE, name, args );
		}
		
		return( result );
	}



	public AttributeList getAttributes(ObjectName name, String[] attributes)
		throws InstanceNotFoundException, ReflectionException,
		   IOException
	{
		final Object []	args	= new Object [] { name, attributes };
		final long id = callPreHook( GET_ATTRIBUTES, args );
		
		AttributeList result	= null;
		try
		{
			final ObjectName	actualName	= callNameHook( id, name );
			
			result	= getConn().getAttributes(actualName , attributes );
			
			callPostHook( id, GET_ATTRIBUTES, args, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, GET_ATTRIBUTES, name, args );
		}
		
		return( result );
	}


	public void setAttribute(ObjectName name, Attribute attribute)
		throws InstanceNotFoundException, AttributeNotFoundException,
		   InvalidAttributeValueException, MBeanException, 
		   ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name, attribute };
		final long id = callPreHook( SET_ATTRIBUTE, args );
		
		try
		{
			getConn().setAttribute( callNameHook( id, name ), attribute );
			
			callPostHook( id, SET_ATTRIBUTE, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, SET_ATTRIBUTE, name, args );
		}
	}




	public AttributeList setAttributes(ObjectName name, AttributeList attributes)
		throws InstanceNotFoundException, ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name, attributes };
		final long id = callPreHook( SET_ATTRIBUTES, args );
		
		AttributeList result	= null;
		
		try
		{
			result	= getConn().setAttributes( callNameHook( id, name ), attributes );
		
			callPostHook( id, SET_ATTRIBUTES, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, SET_ATTRIBUTES, name, args );
		}
		
		return( result );
	}

	public Object invoke(ObjectName name, String operationName,
			 Object params[], String signature[])
		throws InstanceNotFoundException, MBeanException,
		   ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name, operationName, params, signature };
		final long id = callPreHook( INVOKE, args );
		
		Object	result	= null;
		try
		{
			result	= getConn().invoke( callNameHook( id, name ), operationName, params, signature);
		
			callPostHook( id, INVOKE, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, INVOKE, name, args );
		}
		return( result );
	}

 

  
	public String getDefaultDomain()
		throws IOException
	{
		final long id = callPreHook( GET_DEFAULT_DOMAIN );
		
		String	result	= null;
		try
		{
			result	= getConn().getDefaultDomain();
				
			callPostHook( id, GET_DEFAULT_DOMAIN );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, GET_DEFAULT_DOMAIN, null, null );
		}
		
		return( result );
	}


	public String[] getDomains()
		throws IOException
	{
		final long id = callPreHook( GET_DOMAINS );
		
		String[]	result	= null;
		try
		{
			result 	= getConn().getDomains( );
				
			callPostHook( id, GET_DOMAINS, result );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, GET_DOMAINS, null, null );
		}
		
		return( result );
	}


	public void addNotificationListener(ObjectName name,
					NotificationListener listener,
					NotificationFilter filter,
					Object handback)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback  };
		final long id = callPreHook( ADD_NOTIFICATION_LISTENER, args );
		
		try
		{
			getConn().addNotificationListener( callNameHook( id, name ), listener, filter, handback );
			
			callPostHook( id, ADD_NOTIFICATION_LISTENER, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, ADD_NOTIFICATION_LISTENER, name, args );
		}
	}



	public void addNotificationListener(ObjectName name,
					ObjectName listener,
					NotificationFilter filter,
					Object handback)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback  };
		final long id = callPreHook( ADD_NOTIFICATION_LISTENER, args );
		
		try
		{
			getConn().addNotificationListener( callNameHook( id, name ), listener, filter, handback );
			
			callPostHook( id, ADD_NOTIFICATION_LISTENER, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, ADD_NOTIFICATION_LISTENER, name, args );
		}
	}



	public void removeNotificationListener(ObjectName name,
					   ObjectName listener) 
	throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { listener };
		final long id = callPreHook( REMOVE_NOTIFICATION_LISTENER, args );
		
		try
		{
			getConn().removeNotificationListener( callNameHook( id, name ), listener );
			
			callPostHook( id, REMOVE_NOTIFICATION_LISTENER, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, REMOVE_NOTIFICATION_LISTENER, name, args );
		}
	}


	public void removeNotificationListener(ObjectName name,
					   ObjectName listener,
					   NotificationFilter filter,
					   Object handback)
		throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback };
		final long id = callPreHook( REMOVE_NOTIFICATION_LISTENER, args );
		
		try
		{
			getConn().removeNotificationListener( callNameHook( id, name ), listener, filter, handback );
			
			callPostHook( id, REMOVE_NOTIFICATION_LISTENER, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, REMOVE_NOTIFICATION_LISTENER, name, args );
		}
	}



	public void removeNotificationListener(ObjectName name,
					   NotificationListener listener)
		throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { name, listener };
		final long id = callPreHook( REMOVE_NOTIFICATION_LISTENER, args );
		
		try
		{
			getConn().removeNotificationListener( callNameHook( id, name ), listener  );
			
			callPostHook( id, REMOVE_NOTIFICATION_LISTENER, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, REMOVE_NOTIFICATION_LISTENER, name, args );
		}
	}


	public void removeNotificationListener(ObjectName name,
					   NotificationListener listener,
					   NotificationFilter filter,
					   Object handback)
		throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback };
		final long id = callPreHook( REMOVE_NOTIFICATION_LISTENER, args );
		
		try
		{
			getConn().removeNotificationListener( callNameHook( id, name ), listener, filter, handback );
			
			callPostHook( id, REMOVE_NOTIFICATION_LISTENER, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, REMOVE_NOTIFICATION_LISTENER, name, args );
		}
	}

	
	public MBeanInfo getMBeanInfo(ObjectName name)
		throws InstanceNotFoundException, IntrospectionException,
			   ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( GET_MBEAN_INFO, args );
		
		MBeanInfo	result	= null;
		try
		{
			result	= getConn().getMBeanInfo( callNameHook( id, name ) );
			
			callPostHook( id, GET_MBEAN_INFO, args );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id,  e, GET_MBEAN_INFO, name, args );
		}
		
		return( result );
	}


 
	public boolean isInstanceOf(ObjectName name, String className)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name, className };
		final long id = callPreHook( IS_INSTANCE_OF, args );
		
		boolean	isInstance	= false;
		try
		{
			isInstance	= getConn().isInstanceOf( callNameHook( id, name ), className );
			
			callPostHook( id, IS_INSTANCE_OF, args, isInstance ? Boolean.TRUE : Boolean.FALSE );
		}
		catch( IOException e )
		{
			callIOExceptionHook( id, e, IS_INSTANCE_OF, name, args );
		}
		
		return( isInstance );
	}

};

