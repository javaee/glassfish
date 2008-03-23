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

package org.glassfish.admin.amx.monitor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.monitor.Monitor;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.monitor.JMXMonitorMgr;

import org.glassfish.admin.amx.mbean.AMXNonConfigImplBase;
import org.glassfish.admin.amx.mbean.Delegate;

import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;


/**
*/
public class JMXMonitorBase extends AMXNonConfigImplBase
	// implements AMXStringMonitor
{
	final Monitor	mMonitor;
	
	/**
		Because we want our implementation to be an AMX, we will extend appropriately,
		but this means that we must delegate javax.management.StringMonitor functionality
		to an instance of that class.
	 */

		protected
	JMXMonitorBase( final Monitor mon,
        final String j2eeType,
        final String fullType,
        final ObjectName parentObjectName,
		final Class<? extends AMX> theInterface,
        final Delegate delegate)
	{
		super( j2eeType, fullType, parentObjectName, theInterface, delegate );
        
		mMonitor	= mon;
	}
	
	
	
		public String
	getGroup()
	{
		return( AMX.GROUP_UTILITY );
	}
	
	
		protected Object
	getAttributeManually( String name )
		throws AttributeNotFoundException
	{
		final MBeanAttributeInfo	attrInfo	= (MBeanAttributeInfo)getAttributeInfos().get( name );
		assert( attrInfo != null );	// getAttributeManually() should not have been called otherwise
		
		final String	prefix	= attrInfo.isIs() ? JMXUtil.IS : JMXUtil.GET;
		final String	operationName	= prefix + name;
		
		Object	result	= null;
		try
		{
			result	= invokeManually( operationName, null, null );
		}
		catch( Exception e )
		{
			throw new AttributeNotFoundException( name );
		}
		
		return( result );
	}
	
		protected void
	setAttributeManually( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		final String	operationName	= JMXUtil.SET + attr.getName();
		final MBeanAttributeInfo	attrInfo	= (MBeanAttributeInfo)getAttributeInfos().get( attr.getName() );
		
		Object	result	= null;
		try
		{
			final Object	value	= attr.getValue();
			// won't work to get the class from the value; must use MBeanInfo
			final Class	theClass	= ClassUtil.getClassFromName( attrInfo.getType() );
			
			result	= invokeSig( operationName,
				new Object[] { value }, new Class[] { theClass } );
			assert( result == null );
		}
		catch( Exception e )
		{
			throw new AttributeNotFoundException( attr.getName() );
		}
	}
	
		private Object
	invokeSig(
		String 		operationName,
		Object[]	args,
		Class[]		sig )
		throws MBeanException, ReflectionException, NoSuchMethodException
	{
		Object	result	= null;
		
		try
		{
			final Method	m	= mMonitor.getClass().getMethod( operationName, sig );
			
			result	= m.invoke( mMonitor, args );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}

		return( result );
	}
	
	
		protected Object
	invokeManually(
		String 		operationName,
		Object[]	args,
		String[]	types )
		throws MBeanException, ReflectionException, NoSuchMethodException
	{
		Object	result	= null;
		
		try
		{
			final Class[]	sig	= ClassUtil.signatureFromClassnames( types );
			
			result	= invokeSig( operationName, args, sig );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException( e );
		}

		return( result );
	}
	
		public void
	preRegisterDone()
		throws Exception
	{
		final ObjectName	x	= mMonitor.preRegister( getMBeanServer(), getObjectName() );
	}
	
		public void
	postRegisterHook( Boolean registrationDone )
	{
		super.postRegisterHook( registrationDone );
		
		mMonitor.postRegister( registrationDone );
	}
	
		public void
	preDeregisterHook()
	{
		super.preDeregisterHook( );
		
		try
		{
		    mMonitor.preDeregister( );
		}
		catch( Exception e )
		{
		    throw new RuntimeException( e );
		}
	}
	
		public void
	postDeregisterHook()
	{
		super.postDeregisterHook( );
		
		mMonitor.postDeregister( );
	}
	
	
	
}












