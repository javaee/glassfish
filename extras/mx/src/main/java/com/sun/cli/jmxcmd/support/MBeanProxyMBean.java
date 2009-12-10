/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/MBeanProxyMBean.java,v 1.10 2004/09/09 20:04:43 llc Exp $
 * $Revision: 1.10 $
 * $Date: 2004/09/09 20:04:43 $
 */
 

/*
	The MBean interface for CLI support.
 */
 
package com.sun.cli.jmxcmd.support;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import javax.management.*;
import java.io.IOException;

import org.glassfish.admin.amx.util.SetUtil;


public final class MBeanProxyMBean implements DynamicMBean, MBeanRegistration
{
	private final MBeanServerConnection	mProxiedMBeanConnection;
	private final ObjectName			mProxiedMBeanName;
	private ObjectName					mSelfObjectName;
	
	private MBeanInfo					mCachedMBeanInfo;
	private long						mCachedMBeanInfoTime;
	
	private Map<String,Attribute>		mCachedAttributes;
	private long						mCachedAttributesTime;
	
	private final int					mMBeanInfoRefreshMillis;
	private final int					mAttributeRefreshMillis;
	
	private boolean						mPreRegisterDone;
	
		void
	dm( Object o )
	{
		System.out.println( o.toString() );
	}
	
		public
	MBeanProxyMBean(
		final MBeanServerConnection		proxiedMBeanConn,
		final ObjectName				proxiedMBeanName,
		int								mbeanInfoRefreshMillis,
		int								attributeRefreshMillis )
		throws IntrospectionException, ReflectionException, InstanceNotFoundException,
		IOException
	{
		mProxiedMBeanConnection	= proxiedMBeanConn;
		mProxiedMBeanName		= proxiedMBeanName;
		
		mMBeanInfoRefreshMillis	= mbeanInfoRefreshMillis;
		mCachedMBeanInfo		= null;
		
		mAttributeRefreshMillis	= attributeRefreshMillis;
		mCachedAttributes		= mAttributeRefreshMillis != 0 ? new HashMap<String,Attribute>() : null;
		
		mPreRegisterDone		= false;
		mSelfObjectName			= null;
	}
	
		public ObjectName
	preRegister(
		final MBeanServer server,
		final ObjectName name)
	{
		//System.out.println( "\n### MBeanProxyMBean.preRegister: " + name  );
		mSelfObjectName	= name;
		
		mPreRegisterDone	= true;
		return( name );
	}
	
		public void
	postRegister( Boolean registrationDone )
	{
		//System.out.println( "### MBeanProxyMBean.postRegister: " + mSelfObjectName  );
		if ( ! registrationDone.booleanValue()  )
		{
			System.err.println( "MBeanProxyMBean: registration FAILED for : " + mSelfObjectName );
		}
	}
	
		public void
	preDeregister()
	{
		// nothing to do
	}
		public void
	postDeregister(  )
	{
		// nothing to do
	}
	
		public MBeanInfo
	_getMBeanInfo()
		throws IntrospectionException, ReflectionException, InstanceNotFoundException,
		IOException
	{
		MBeanInfo	info	= null;
		
		try
		{
			info	= mProxiedMBeanConnection.getMBeanInfo( mProxiedMBeanName );
		}
		catch (InstanceNotFoundException e )
		{
			// when unregistering, we arrive here; for some reason the MBeanServer
			// insists on calling getMBeanInfo() prior to unregistering the MBean.
			// Typically the reason we're being unregistered is because the remote
			// mbean has gone away.

			info	= mCachedMBeanInfo;
			//dm( "caught InstanceNotFoundException while trying to get MBeanInfo for " +
				//mProxiedMBeanName.toString() + ", exception = " + e.getClass().getName() );
		}
		
		return( info );
	}
	
	private static final MBeanInfo EMPTY_MBEAN_INFO	=
		new MBeanInfo( "Temp", "", null, null, null, null );
	
		private final boolean
	mbeanInfoNeedsRefresh()
	{
		return(		(! shouldCacheMBeanInfo() ) ||
					mCachedMBeanInfo == null ||
					mbeanInfoIsStale() );
	}
	
		private boolean
	mbeanInfoIsStale()
	{
		final long	now	= System.currentTimeMillis();
		
		final boolean	isStale	= (now - mCachedMBeanInfoTime) > mMBeanInfoRefreshMillis;
		
		return( isStale );
	}
	
		private final boolean
	shouldCacheMBeanInfo()
	{
		return( mMBeanInfoRefreshMillis != 0 );
	}
	
	
		private boolean
	attributesAreStale()
	{
		final long		now	= System.currentTimeMillis();
		final boolean	isStale	=  (now - mCachedAttributesTime) > mAttributeRefreshMillis;
		
		return( isStale );
	}
	
		private final boolean
	shouldCacheAttributes()
	{
		return( mAttributeRefreshMillis != 0 );
	}
	
		public MBeanInfo
	getMBeanInfo()
	{
		MBeanInfo	info	= null;
		
		if ( mPreRegisterDone )
		{
			if ( mbeanInfoNeedsRefresh() )
			{
				try
				{
					//dm( "GETTING REAL MBEANINFO: " + mProxiedMBeanName );
					//final long	start	= System.currentTimeMillis();
					info	= _getMBeanInfo();
					//final long	elapsed	= System.currentTimeMillis() - start;
					//System.out.println( "getMBeanInfo() time: " + elapsed );
				}
				catch( Exception e )
				{
					throw new RuntimeException( e );
				}
				
				if ( shouldCacheMBeanInfo() )
				{
					//dm( "CACHING REAL MBEANINFO: "  + mProxiedMBeanName );
					mCachedMBeanInfoTime	= System.currentTimeMillis();
				}
				mCachedMBeanInfo		= info;
			}
			else
			{
				info	= mCachedMBeanInfo;
				//dm( "USING CACHED MBEANINFO: " + mProxiedMBeanName );
			}
		}
		else
		{
			//dm( "RETURNING EMPTY MBEANINFO (preregister not yet done): " + mProxiedMBeanName );
			// don't artificially get it for MBeanServer during registration;
			// it is doing so
			// without a real need for info
			info	= EMPTY_MBEAN_INFO;
		}
		
		return( info );
	}
	
		private MBeanException
	instanceNotFound( InstanceNotFoundException e )
	{
		return( new MBeanException( e, "proxied MBean \"" +
			mProxiedMBeanName + "\" no longer available" ) );
	}
	
		private MBeanException
	ioException( IOException e )
	{
		return(
			new MBeanException( e, "connection to \"" +
			mProxiedMBeanName + "\" not accessible" ) );
	}

		private MBeanException
	handleException( Exception e )
	{
		MBeanException	result;
		
		e.printStackTrace();
		if ( e instanceof InstanceNotFoundException )
		{
			result	= instanceNotFound( (InstanceNotFoundException)e );
		}
		else if ( e instanceof IOException )
		{
			result	= ioException( (IOException)e );
		}
		else
		{
			result	= new MBeanException( e );
		}
		
		return( result );
	}	
	
		private void
	checkStaleAttributes()
	{
		if ( shouldCacheAttributes() )
		{
			if ( attributesAreStale()  || mCachedAttributes == null )
			{
				mCachedAttributes		= new HashMap<String,Attribute>();
				mCachedAttributesTime	= System.currentTimeMillis();
			}
		}
	}
	
		private Attribute
	getCachedAttribute( final String name )
	{
		assert( shouldCacheAttributes() );
		return( mCachedAttributes.get( name ) );
	}
	
		private void
	cacheAttribute( final String name, final Object value )
	{
		cacheAttribute( new Attribute( name, value ) );
	}
	
		private void
	cacheAttribute( final Attribute attr )
	{
		assert( shouldCacheAttributes() );
		mCachedAttributes.put( attr.getName(), attr );
	}
	
		private void
	cacheAll( final AttributeList attrs )
	{
		final int	count	= attrs.size();
		for( int i = 0; i < count; ++i )
		{
			final Attribute	attr	= (Attribute)attrs.get( i );
			//System.out.println( "cacheAll: caching: " + attr.getName() );
			cacheAttribute( attr.getName(), attr );
		}
	}
				
		public Object
	getAttribute( String	name )
		throws AttributeNotFoundException, MBeanException, ReflectionException
	{
		checkStaleAttributes();
		
		Object	result	= null;
		
		final Attribute	cachedAttr	= shouldCacheAttributes() ?
						getCachedAttribute( name ) : null;
		
		if ( cachedAttr != null )
		{
			result	= cachedAttr.getValue();
			//System.out.println( "returning cached: " + cachedAttr.getName() );
		}
		else
		{
			try
			{
				result	= mProxiedMBeanConnection.getAttribute( mProxiedMBeanName, name );
				
				if ( shouldCacheAttributes() )
				{
					//System.out.println( "getAttribute: caching: " + name );
					cacheAttribute( name, result );
				}
			}
			catch( InstanceNotFoundException e )
			{
				throw instanceNotFound( e );
			}
			catch( IOException e )
			{
				throw ioException( e );
			}
		}
		return( result );
	}
	
	
		private AttributeList
	getAttributesNoThrow( final String[] names )
	{
		AttributeList	results	= null;
		
		try
		{
			results	= mProxiedMBeanConnection.getAttributes( mProxiedMBeanName, names );
		}
		catch( Exception e )
		{
			handleException( e );
			results	= new AttributeList();
		}
		return( results );
	}
	
		public AttributeList
	getAttributes( String[]	names )
	{
		AttributeList	results	= null;
		
		if ( shouldCacheAttributes() )
		{
			results	= new AttributeList();
			
			final Set<String>	missingNames	= new HashSet<String>();
			for( int i = 0; i < names.length; ++i )
			{
				final Attribute attr	= getCachedAttribute( names[ i ] );
				
				if ( attr == null )
				{
					//System.out.println( "getAttributes: missing: " + attr.getName() );
					missingNames.add( names[ i ] );
				}
				else
				{
					//System.out.println( "getAttributes: found: " + attr.getName() );
					results.add( attr );
				}
			}
			
			if ( missingNames.size() != 0 )
			{
				final AttributeList	temp	= getAttributesNoThrow( SetUtil.toStringArray( missingNames ) );
					
				final int	numResults	= temp.size();
				for( int i = 0; i < numResults; ++i )
				{
					final Attribute	attr	= (Attribute)temp.get( i );
					//System.out.println( "getAttributes: caching: " + attr.getName() );
					cacheAttribute( attr );
					results.add( attr );
				}
			}
		}
		else
		{
			results	= getAttributesNoThrow( names );
		}

		return( results );
	}
	
	
		public void
	setAttribute( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		try
		{
			mProxiedMBeanConnection.setAttribute( mProxiedMBeanName, attr );
			if ( shouldCacheAttributes() )
			{
				//System.out.println( "setAttribute: caching: " + attr.getName() );
				cacheAttribute( attr );
			}
		}
		catch( InstanceNotFoundException e )
		{
			handleException( e );
		}
		catch( IOException e )
		{
			handleException( e );
		}
		catch( ReflectionException e )
		{
			handleException( e );
		}
		catch( MBeanException e )
		{
			handleException( e );
		}
	}
	
		public AttributeList
	setAttributes( AttributeList attributes )
	{
		AttributeList	results	= null;
		
		try
		{
			results	= mProxiedMBeanConnection.setAttributes( mProxiedMBeanName, attributes );
			if ( shouldCacheAttributes() )
			{
				cacheAll( results );
			}
		}
		catch( Exception e )
		{
			handleException( e );
			results	= new AttributeList();
		}
		return( results );
	}
	
	
		public Object
	invoke(
		String		operationName,
		Object[]	params,
		String[]	types)
		throws MBeanException, ReflectionException
	{
		Object	result	= null;
		
		try
		{
			result	= mProxiedMBeanConnection.invoke( mProxiedMBeanName, operationName, params, types );
		}
		catch( IOException e  )
		{
			throw handleException( e );
		}
		catch( InstanceNotFoundException e )
		{
			throw handleException( e );
		}
		
		return( result );
	}
}

