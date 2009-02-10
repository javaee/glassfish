/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/cmd/com/sun/enterprise/s1as8/mbeans/AllDottedNames.java,v 1.1 2003/11/22 02:12:31 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/22 02:12:31 $
 */
 
package com.sun.enterprise.s1as8.mbeans;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import javax.management.InvalidAttributeValueException;
import javax.management.AttributeNotFoundException;
import javax.management.ServiceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Attribute;

import com.sun.enterprise.s1as8.cmd.DottedNameProxy;
import com.sun.enterprise.s1as8.cmd.DottedNameProxyFactory;


/**
	This MBean exposes all S1AS8 dotted name values as Attributes. 
 */
public class AllDottedNames implements DynamicMBean
{
	final MBeanServerConnection	mConn;
	final DottedNameProxy		mProxy;
	final boolean				mMonitoring;
	MBeanInfo					mMBeanInfo;
	Set							mDottedNames;
	
		public
	AllDottedNames( MBeanServerConnection conn, boolean monitoring )
	{
		mConn			= conn;
		mMonitoring		= monitoring;
		mDottedNames	= null;
		mMBeanInfo		= null;
		
		mProxy	= DottedNameProxyFactory.createProxy( mConn );
		refresh();
	}
	
	
		public MBeanInfo
	getMBeanInfo()
	{
		ensureMBeanInfo();
		
		return( mMBeanInfo );
	}
	
		String[]
	filterLegalNames( String[]	names )
	{
		final HashSet	s	= new HashSet();
		
		for( int i = 0; i < names.length; ++i )
		{
			if ( isLegalAttributeName( names[ i ] ) )
			{
				s.add( names[ i ] );
			}
		}
		
		return( (String[])s.toArray( new String[ s.size() ] ) );
	}
	
		public AttributeList
	getAttributes( String[]	names )
	{
		Object[]	results	= null;
		
		final String[]	legalNames	= filterLegalNames( names );

		if ( mMonitoring )
		{
			results	= mProxy.dottedNameMonitoringGet( legalNames );
		}
		else
		{
			results	= mProxy.dottedNameGet( legalNames );
		}
		
		final AttributeList	attributeList	= new AttributeList();
		for( int i = 0; i < results.length; ++i )
		{
			if ( results[ i ] instanceof Attribute )
			{
				attributeList.add( results[ i ] );
			}
			else
			{
				assert( results[ i ] instanceof Exception );
			}
		}
		
		return( attributeList );
	}
	
		public Object
	getAttribute( String	name )
		throws AttributeNotFoundException
	{
		checkLegalName( name );
		
		final AttributeList	resultList	= getAttributes( new String[] { name } );
		if ( resultList.size() == 0 )
		{
			throw new AttributeNotFoundException( name );
		}

		final Attribute		attr		= (Attribute)resultList.get( 0 );
		return( attr.getValue() );
	}
	
	
		public void
	setAttribute( Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		checkLegalName( attr.getName() );
		
		final AttributeList		inList	= new AttributeList();
		inList.add( attr );
		final AttributeList	result	= setAttributes( inList );
		if ( result.size() != 1)
		{
			throw new InvalidAttributeValueException( attr.getName() );
		}
	}
	
		public AttributeList
	setAttributes( AttributeList attributes )
	{
		checkIfWriteable();
		
		/*
			Convert each attribute to a name/value pair.
			Omit any attributes that don't have a legal attribute name
		 */
		final int	numAttrsIn	= attributes.size();
		final ArrayList	legalPairs	= new ArrayList();
		for( int i = 0; i < numAttrsIn; ++i )
		{
			final Attribute	attr	= (Attribute)attributes.get( i );
			
			if ( isLegalAttributeName( attr.getName() ) )
			{
				legalPairs.add( attributeToNamePair( attr ) );
			}
		}
		
		final String[]	pairs	= (String[])legalPairs.toArray( new String[ legalPairs.size() ] );

		final Object[] results	= mProxy.dottedNameSet( pairs );
		
		final AttributeList	attributeList	= new AttributeList();
		for( int i = 0; i < results.length; ++i )
		{
			if ( results[ i ] instanceof Attribute )
			{
				attributeList.add( results[ i ] );
			}
			else
			{
				assert( results[ i ] instanceof Exception );
				// it's an exception
			}
		}
		
		return( attributeList );
	}
	
	
		public Object
	invoke(
		String		operationName,
		Object[]	params,
		String[]	types)
	{
		final boolean	noParams	= params == null || params.length == 0;
		final Object	result	= null;
		
		if ( operationName.equals( "refresh" ) && noParams )
		{
			refresh();
		}
		else
		{
			throw new IllegalArgumentException( "unknown operation: " + operationName );
		}
		return( result );
	}
		
		void
	checkIfWriteable()
	{
		if ( mMonitoring )
		{
			throw new IllegalArgumentException( "monitoring attributes are read-only and may not be set" );
		}
	}
	
		private synchronized void
	ensureMBeanInfo()
	{
		if ( mMBeanInfo == null )
		{
			refresh();
			assert( mMBeanInfo != null );
		}
	}
	
	
		public MBeanAttributeInfo[]
	buildAttributeInfos()
	{
		final String[]			names	= (String[])
			mDottedNames.toArray( new String[ mDottedNames.size() ] );
		
		final MBeanAttributeInfo[]	infos = new MBeanAttributeInfo[ names.length ];
		for( int i = 0; i < names.length; ++i )
		{
			infos[ i ]	= new MBeanAttributeInfo( names[ i ], "java.lang.String", "",
				true, ! mMonitoring, false );
		}

		return( infos );
	}
	
		public MBeanOperationInfo[]
	buildOperationInfos()
	{
		final MBeanOperationInfo	refreshInfo	= new MBeanOperationInfo( "refresh",
			"update MBeanInfo to reflect all available dotted names",
			null,
			Void.class.getName(),
			MBeanOperationInfo.ACTION );
		
		final MBeanOperationInfo[]	infos	= new MBeanOperationInfo[ 1 ];
		infos[ 0 ]	= refreshInfo;
		
		return( infos );
	}
	
		public MBeanInfo
	buildMBeanInfo()
	{
		final MBeanAttributeInfo[]		attributeInfos		= buildAttributeInfos();
		final MBeanOperationInfo[]		operationInfos		= buildOperationInfos();
		final MBeanConstructorInfo[]	constructorInfos	= new MBeanConstructorInfo[ 0 ];
		final MBeanNotificationInfo[]	notificationInfos	= new MBeanNotificationInfo[ 0 ];
		
		final MBeanInfo	info	= new MBeanInfo( this.getClass().getName(),
									"exposes dotted-names as Attributes",
									attributeInfos,
									constructorInfos,
									operationInfos,
									notificationInfos );
		
		return( info );
	}
	
	
	
	
		Attribute
	namePairToAttribute( String pair )
	{
		final int			delimIndex	= pair.indexOf( "=" );
		assert( delimIndex >= 1 );
		final String		name	= pair.substring( 0, delimIndex );
		final String		value	= pair.substring( delimIndex + 1, pair.length() );
		
		return( new Attribute( name, value ) );
	}
	
		String
	attributeToNamePair( Attribute attr )
	{
		return( attr.getName() + "=" + attr.getValue() );
	}
	
		boolean
	isLegalAttributeName( String name )
	{
		return( mDottedNames.contains( name ) );
	}
	
		private void
	checkLegalName( String name )
		throws AttributeNotFoundException
	{
		if ( ! isLegalAttributeName( name ) )
		{
			throw new AttributeNotFoundException( "illegal attribute name: " + name );
		}
	}
	

	/**
		Refresh the MBeanInfo to reflect the currently available attributes.
	 */
	private static final String	ALL	= "*";
		void
	refreshAttributeNames()
	{
		Object	result	= null;
		
		if ( mMonitoring )
		{
			result	= mProxy.dottedNameMonitoringGet( ALL );
		}
		else
		{
			result	= mProxy.dottedNameGet( ALL );
		}
		
		// results is an array of length 1.  It should contain an Object[] containining
		// everything obtained from "ALL"
		final Attribute[]	values	= (Attribute[])result;
		
		// extract the name of each attribute
		final HashSet	tempSet	= new HashSet();
		for( int i = 0; i < values.length; ++i )
		{
			tempSet.add( values[ i ].getName() );
		}
		
		mDottedNames	= tempSet;
	}
	
		void
	refresh()
	{
		refreshAttributeNames();
		mMBeanInfo	= buildMBeanInfo();
	}
	
		void
	dm( Object o )
	{
		System.out.println( o.toString() );
	}
}







