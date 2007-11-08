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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 */

package com.sun.enterprise.management.support;

import java.io.IOException;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanInfo;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;

import com.sun.appserv.management.util.jmx.stringifier.MBeanInfoStringifier;

/**
	Delegate which delegates to another MBean.
 */
public class DelegateToMBeanDelegate extends DelegateBase
{
	private final ObjectName			mTarget;
	private final MBeanServerConnection	mConn;
	private final MBeanInfo				mTargetMBeanInfo;
	
		public
	DelegateToMBeanDelegate(
		final MBeanServerConnection	conn,
		final ObjectName			target)
		throws  InstanceNotFoundException, IntrospectionException, ReflectionException,
		IOException
	{
		super( "DelegateToMBeanDelegate." + target.toString(), null );
		
		mConn	= conn;
		mTarget	= target;
		
		mTargetMBeanInfo	= mConn.getMBeanInfo( target );
	}
	
		public
	DelegateToMBeanDelegate(
		final MBeanServer			server,
		final ObjectName			target)
		throws  InstanceNotFoundException, IntrospectionException, ReflectionException
	{
		super( "DelegateToMBeanDelegate." + target.toString(), null);
		
		mConn	= server;
		mTarget	= target;
		
		mTargetMBeanInfo	= server.getMBeanInfo( target );
		
		//trace( "\nMBeanInfo for: " + target + ":\n" +
			//MBeanInfoStringifier.DEFAULT.stringify( mTargetMBeanInfo ) );
	}
    
		public final ObjectName
	getTarget()
	{
		return( mTarget );
	}
	
		public final MBeanServerConnection
	getMBeanServerConnection()
	{
		return( mConn );
	}
	
		public final Object
	getAttribute( final String attrName )
		throws AttributeNotFoundException
	{
		try
		{
			final Object value	=
				getMBeanServerConnection().getAttribute( mTarget, attrName );
			return( value );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException( e );
		}
	}
    
    private static final String[]   SINGLE_STRING_SIG   = new String[] { String.class.getName() };
    
    @Override
        protected final String
    _getDefaultValue( final String name )
        throws AttributeNotFoundException
    {
        return (String)invoke( "getDefaultAttributeValue", new String[] { name }, SINGLE_STRING_SIG );
    }
	
		public void
	setAttribute( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		try
		{
			getMBeanServerConnection().setAttribute( mTarget, attr );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
		public MBeanInfo
	getMBeanInfo()
	{
		return( mTargetMBeanInfo );
	}
	
	
    
		private void
	delegateFailed( final Throwable t )
	{
		if ( getOwner() != null )
		{
			getOwner().delegateFailed( t );
		}
	}

	/**
	 */
		public final Object
	invoke(
		String 		operationName,
		Object[]	args,
		String[]	types )
	{
		try
		{
			final Object	result	= getMBeanServerConnection().invoke( getTarget(),
										operationName, args, types );
			
			return( result );
		}
		catch ( InstanceNotFoundException e )
		{
			delegateFailed( e );
			throw new RuntimeException( e );
		}
		catch ( IOException e )
		{
			try
			{
				getMBeanServerConnection().isRegistered( getTarget() );
			}
			catch( IOException ee )
			{
				delegateFailed( e );
			}
			throw new RuntimeException( e );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}








