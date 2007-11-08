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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/util/MBeanServerConnection_Hook.java,v 1.3 2005/12/25 03:45:55 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:55 $
 */
package com.sun.cli.jmx.util;

import javax.management.*;
import java.io.IOException;
import java.util.Set;


/*
	This class wraps an MBeanServerConnection and provides hooks on each method call
	via its Hook interface.
	
	Typical used would be for a subclass to 
 */
public class MBeanServerConnection_Hook implements MBeanServerConnection
{
	private final MBeanServerConnection		mConn;
	
	/*
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
		long		preHook( String methodName );
		long		preHook( String nmethodName, Object [] args );
		void		postHook( long id, String methodName );
		void		postHook( long id, String methodName, Object [] args );
		void		postHook( long id, String methodName, Object [] args, Object result );
		ObjectName	nameHook( long id, ObjectName methodName ) throws IOException;
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
	}
	
		Hook
	getHook()
	{
		return( HookImpl.HOOK );
	}

		public
	MBeanServerConnection_Hook(
		MBeanServerConnection		conn  )
	{
		mConn	= conn;
		
		assert( getConn() != null );
		
	}
	
		MBeanServerConnection
	getConn()
	{
		return( mConn );
	}
	
		long
	callPreHook( String name, Object [] args )
	{
		return( getHook().preHook( name, args ) );
	}
	
		long
	callPreHook( String name )
	{
		return( getHook().preHook( name, null ) );
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
	
	public ObjectInstance createMBean(String className, ObjectName name)
		throws ReflectionException, InstanceAlreadyExistsException,
		   MBeanRegistrationException, MBeanException,
		   NotCompliantMBeanException, IOException
	{
		final Object []	args	= new Object [] { className, name };
		
		final long id = callPreHook( "createMBean", args );
		
		final ObjectInstance	result	= getConn().createMBean( className, name );
		
		callPostHook( id, "createMBean", args, result );
		
		return( result );
	}

	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) 
		throws ReflectionException, InstanceAlreadyExistsException,
		   MBeanRegistrationException, MBeanException,
		   NotCompliantMBeanException, InstanceNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { className, name, loaderName };
		final long id = callPreHook( "createMBean", args );
		
		final ObjectInstance	result	= getConn().createMBean( className, name, loaderName );
		
		callPostHook( id, "createMBean", args, result );
		
		return( result );
	}



	public ObjectInstance createMBean(String className, ObjectName name,
					  Object params[], String signature[]) 
		throws ReflectionException, InstanceAlreadyExistsException,
			   MBeanRegistrationException, MBeanException,
			   NotCompliantMBeanException, IOException
	{
		final Object []	args	= new Object [] { className, name, params, signature };
		final long id = callPreHook( "createMBean", args );
		
		final ObjectInstance	result	= getConn().createMBean( className, name, params, signature );
		
		callPostHook( id, "createMBean", args, result );
		
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
		final long id = callPreHook( "createMBean", args );
		
		final ObjectInstance	result	=
			getConn().createMBean( className, name, loaderName, params, signature);
		
		callPostHook( id, "createMBean", args, result );
		
		return( result );
	}


	public void unregisterMBean(ObjectName name)
		throws InstanceNotFoundException, MBeanRegistrationException,
			   IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( "unregisterMBean", args );
		
		getConn().unregisterMBean( callNameHook( id, name ) );
		
		callPostHook( id, "unregisterMBean", args );
	}


	public ObjectInstance getObjectInstance(ObjectName name)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( "getObjectInstance", args );
		
		final ObjectInstance	result	= getConn().getObjectInstance( callNameHook( id, name ) );
		
		callPostHook( id, "getObjectInstance", args, result );
		
		return( result );
	}


	public Set queryMBeans(ObjectName name, QueryExp query)
		throws IOException
	{
		final Object []	args	= new Object [] { name, query };
		final long id = callPreHook( "queryMBeans", args );
		
		final Set	result	= getConn().queryMBeans( name, query );
		
		callPostHook( id, "queryMBeans", args, result );
		
		return( result );
	}


	public Set queryNames(ObjectName name, QueryExp query)
		throws IOException
	{
		final Object []	args	= new Object [] { name, query };
		final long id = callPreHook( "queryNames", args );
		
		final Set	result	= getConn().queryMBeans( name, query );
		
		callPostHook( id, "queryNames", args, result );
		
		return( result );
	}




	public boolean isRegistered(ObjectName name)
		throws IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( "isRegistered", args );
		
		boolean	registered	= false;
		
		registered	= getConn().isRegistered( callNameHook( id, name ) );
		
		callPostHook( id, "isRegistered", args, registered ? Boolean.TRUE : Boolean.FALSE );
		
		return( registered );
	}



	public Integer getMBeanCount()
		throws IOException
	{
		final long id = callPreHook( "getMBeanCount", null );
		
		final Integer	result	= getConn().getMBeanCount( );
		
		callPostHook( id, "getMBeanCount", null, result );
		
		return( result );
	}


	public Object getAttribute(ObjectName name, String attribute)
		throws MBeanException, AttributeNotFoundException,
			   InstanceNotFoundException, ReflectionException,
			   IOException
	{
		final Object []	args	= new Object [] { name, attribute };
		final long id = callPreHook( "getAttribute", args );
		
		final Object	result	= getConn().getAttribute( callNameHook( id, name ), attribute );
		
		callPostHook( id, "getAttribute", args, result );
		
		return( result );
	}



	public AttributeList getAttributes(ObjectName name, String[] attributes)
		throws InstanceNotFoundException, ReflectionException,
		   IOException
	{
		final Object []	args	= new Object [] { name, attributes };
		final long id = callPreHook( "getAttributes", args );
		
		final AttributeList	result	= getConn().getAttributes( callNameHook( id, name ), attributes );
		
		callPostHook( id, "getAttributes", args, result );
		
		return( result );
	}


	public void setAttribute(ObjectName name, Attribute attribute)
		throws InstanceNotFoundException, AttributeNotFoundException,
		   InvalidAttributeValueException, MBeanException, 
		   ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name, attribute };
		final long id = callPreHook( "setAttribute", args );
		
		getConn().setAttribute( callNameHook( id, name ), attribute );
		
		callPostHook( id, "setAttribute", args );
	}




	public AttributeList setAttributes(ObjectName name, AttributeList attributes)
		throws InstanceNotFoundException, ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name, attributes };
		final long id = callPreHook( "setAttributes", args );
		
		final AttributeList result	= getConn().setAttributes( callNameHook( id, name ), attributes );
		
		callPostHook( id, "setAttributes", args );
		
		return( result );
	}


	public Object invoke(ObjectName name, String operationName,
			 Object params[], String signature[])
		throws InstanceNotFoundException, MBeanException,
		   ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name, operationName, params, signature };
		final long id = callPreHook( "invoke", args );
		
		final Object	result	= getConn().invoke( callNameHook( id, name ), operationName, params, signature);
	
		callPostHook( id, "invoke", args );
		return( result );
	}

 

  
	public String getDefaultDomain()
		throws IOException
	{
		final long id = callPreHook( "getDefaultDomain" );
		
		final String	result	= getConn().getDefaultDomain();
			
		callPostHook( id, "getDefaultDomain" );
		
		return( result );
	}


	public String[] getDomains()
		throws IOException
	{
		final long id = callPreHook( "getDomains" );
		
		final String []	result	= getConn().getDomains( );
			
		callPostHook( id, "getDefaultDomain", result );
		
		return( result );
	}


	public void addNotificationListener(ObjectName name,
					NotificationListener listener,
					NotificationFilter filter,
					Object handback)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback  };
		final long id = callPreHook( "addNotificationListener", args );
		
		getConn().addNotificationListener( callNameHook( id, name ), listener, filter, handback );
		
		callPostHook( id, "addNotificationListener", args );
	}



	public void addNotificationListener(ObjectName name,
					ObjectName listener,
					NotificationFilter filter,
					Object handback)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback  };
		final long id = callPreHook( "addNotificationListener", args );
		
		getConn().addNotificationListener( callNameHook( id, name ), listener, filter, handback );
		
		callPostHook( id, "addNotificationListener", args );
	}



	public void removeNotificationListener(ObjectName name,
					   ObjectName listener) 
	throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { listener };
		final long id = callPreHook( "removeNotificationListener", args );
		
		getConn().removeNotificationListener( callNameHook( id, name ), listener );
		
		callPostHook( id, "removeNotificationListener", args );
	}


	public void removeNotificationListener(ObjectName name,
					   ObjectName listener,
					   NotificationFilter filter,
					   Object handback)
		throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback };
		final long id = callPreHook( "removeNotificationListener", args );
		
		getConn().removeNotificationListener( callNameHook( id, name ), listener, filter, handback );
		
		callPostHook( id, "removeNotificationListener", args );
	}



	public void removeNotificationListener(ObjectName name,
					   NotificationListener listener)
		throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { name, listener };
		final long id = callPreHook( "removeNotificationListener", args );
		
		getConn().removeNotificationListener( callNameHook( id, name ), listener  );
		
		callPostHook( id, "removeNotificationListener", args );
	}


	public void removeNotificationListener(ObjectName name,
					   NotificationListener listener,
					   NotificationFilter filter,
					   Object handback)
		throws InstanceNotFoundException, ListenerNotFoundException,
		   IOException
	{
		final Object []	args	= new Object [] { name, listener, filter, handback };
		final long id = callPreHook( "removeNotificationListener", args );
		
		getConn().removeNotificationListener( callNameHook( id, name ), listener, filter, handback );
		
		callPostHook( id, "removeNotificationListener", args );
	}


	public MBeanInfo getMBeanInfo(ObjectName name)
		throws InstanceNotFoundException, IntrospectionException,
			   ReflectionException, IOException
	{
		final Object []	args	= new Object [] { name };
		final long id = callPreHook( "getMBeanInfo", args );
		
		final MBeanInfo	result	= getConn().getMBeanInfo( callNameHook( id, name ) );
		
		callPostHook( id, "getMBeanInfo", args );
		
		return( result );
	}


 
	public boolean isInstanceOf(ObjectName name, String className)
		throws InstanceNotFoundException, IOException
	{
		final Object []	args	= new Object [] { name, className };
		final long id = callPreHook( "isInstanceOf", args );
		
		final boolean	isInstance	= getConn().isInstanceOf( callNameHook( id, name ), className );
		
		callPostHook( id, "isInstanceOf", args, isInstance ? Boolean.TRUE : Boolean.FALSE );
		
		return( isInstance );
	}

};

