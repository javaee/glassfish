/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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


