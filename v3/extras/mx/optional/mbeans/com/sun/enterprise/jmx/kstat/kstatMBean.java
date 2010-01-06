/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/optional/mbeans/com/sun/enterprise/jmx/kstat/kstatMBean.java,v 1.3 2005/05/19 20:35:57 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/05/19 20:35:57 $
 */
package com.sun.enterprise.jmx.kstat;

import javax.management.*;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;


/**
	Solaris-specific MBean to support kstat
 */
public class kstatMBean implements DynamicMBean
{
	private final kstat			mSource;
	private final kstatMgrMBean	mMgr;
	private MBeanInfo			mMBeanInfo;
	
		public
	kstatMBean( kstatMgrMBean mgr, final kstat source )
	{
		mSource	= source;
		mMgr	= mgr;
	}

		public AttributeList
	getAttributes( String [] names )
	{
		final AttributeList	list	= new AttributeList();
		
		for( int i = 0; i < names.length; ++i )
		{
			final Object	value	= _getAttribute( names[ i ] );
			if ( value != null )
			{
				list.add( new Attribute( names[ i ], value ) );
			}
		}

		return( list );
	}
	
		private Object
	_getAttribute( String name )
	{
		final Object value	= mSource.getValue( name );
		
		return( value );
	}
	
		public Object
	getAttribute( String name )
	{
		return( _getAttribute( name ) );
	}
	
		public AttributeList
	setAttributes( AttributeList names )
	{
		throw new java.lang.UnsupportedOperationException( "can't set kstat value" );
	}
	
		public void
	setAttribute( Attribute attr )
	{
		throw new java.lang.UnsupportedOperationException( "can't set kstat value" );
	}
	
		private Object
	internalInvoke( String actionName, Object[] params, String[] signature )
		throws Exception
	{
		Object	result	= null;
		
		if ( actionName.equals( "refresh" ) )
		{
			refresh();
		}
		else
		{
			throw new OperationsException( "No such method: " + actionName );
		}
		
		return( result );
	}
	
	
		public Object
	invoke( String actionName, Object[] params, String[] signature )
		throws MBeanException, ReflectionException
	{
		Object	result	= null;
		
		try
		{
			result	= internalInvoke( actionName, params, signature );
		}
		catch( MBeanException e )
		{
			throw e;
		}
		catch( ReflectionException e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new MBeanException( e );
		}
		
		return( result );
	}
	
		private MBeanAttributeInfo
	createAttrInfo( String attrName )
	{
		final MBeanAttributeInfo	info	= new MBeanAttributeInfo(
			attrName,
			mSource.getAttributeType( attrName ).getName(),
			"",
			true, false, false );
		
		return( info );
	}
	
		private MBeanAttributeInfo []
	createAttrInfos()
	{
		final ArrayList	list	= new ArrayList();
		
		final Set	attrNames	= mSource.getAttributeNames();
		final Iterator	iter	= attrNames.iterator();
		while ( iter.hasNext() )
		{
			final String	attrName	= (String)iter.next();
			try
			{
				final MBeanAttributeInfo	info	= createAttrInfo( attrName );
				
				list.add( info );
			}
			catch( IllegalArgumentException e )
			{
				// don't add it, but continue
				//System.out.println( "createAttrInfos: illegal Attribute name: " + attrNames[ i ]);
			}
		}
		
		final MBeanAttributeInfo []	infos = new MBeanAttributeInfo [ list.size() ];
		list.toArray( infos );
		
		return( infos );
	}
	
		public void
	refresh()
		throws Exception
	{
		mMgr.refresh( mSource.getScopedName() );
	}
	
	
		private MBeanOperationInfo []
	createOperationInfos()
	{
		final MBeanOperationInfo []	infos	= new MBeanOperationInfo[ 1 ];
		
		try
		{
			final Method	m	= this.getClass().getDeclaredMethod( "refresh", (Class[])null);
			infos[ 0 ] 	= new MBeanOperationInfo(
				"refresh all attributes for " + mSource.getScopedName(),
				m );
		}
		catch( NoSuchMethodException e )
		{
			assert( false );
		}
		
		return( infos );
	}
	
		public synchronized MBeanInfo
	getMBeanInfo(  )
	{
		if ( mMBeanInfo == null )
		{
			final MBeanAttributeInfo []		attrInfo		= createAttrInfos();
			final MBeanOperationInfo []		operationInfo	=  createOperationInfos();
			final MBeanConstructorInfo []	constructorInfo	=  new MBeanConstructorInfo[0];
			final MBeanNotificationInfo []	notificationInfo	=  new MBeanNotificationInfo[0];
			
			final MBeanInfo		info = new MBeanInfo(
				this.getClass().getName(),
				mSource.getScopedName(),
				attrInfo,
				constructorInfo,
				operationInfo,
				notificationInfo );
			
			mMBeanInfo	= info;
		}
		
		return( mMBeanInfo );
	}
};


