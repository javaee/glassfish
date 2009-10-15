/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdEnvImpl.java,v 1.5 2005/11/15 20:21:41 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2005/11/15 20:21:41 $
 */
 

package com.sun.cli.jcmd.framework;

import java.util.Properties;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sun.cli.jcmd.util.misc.SafeSave;

public final class CmdEnvImpl implements CmdEnv
{
	private final Map<String,Object>	mEnv;
	private File			        mFile;
	private boolean			        mNeedsSave;
	
	private static class ValueData
	{
		Object		mValue;
		boolean		mAllowPersistence;
		
		ValueData( Object value  )
		{
			mValue				= value;
			mAllowPersistence	= false;
		}
		
		ValueData( Object value, boolean allowPersistence )
		{
			mValue					= value;
			mAllowPersistence		= allowPersistence;
		}
	}
	
		public
	CmdEnvImpl()
	{
		mEnv	= new HashMap<String,Object>();
		mFile	= null;
	}
	
		public synchronized void
	put( String key, Object value, boolean allowPersistence )
	{
		final ValueData	data	= new ValueData( value, allowPersistence);
		
		mEnv.put( key, data );
		
		if ( allowPersistence )
		{
			mNeedsSave	= true;
		}
	}
	
		public synchronized Object
	get( final String key )
	{
		final ValueData	data	= (ValueData)mEnv.get( key );
		final Object	value	= (data != null) ? data.mValue : null;
		
		return( value );
	}
	
		public synchronized void
	remove( final String key )
	{
		
		if ( isPersistable( key ) )
		{
			mNeedsSave	= true;
		}
		
		mEnv.remove( key );
	}
	
		public synchronized Set<String>
	getKeys()
	{
		return( mEnv.keySet( ) );
	}
		
		public synchronized boolean
	isPersistable( final String key )
	{
		final ValueData	data			= (ValueData)mEnv.get( key );
		final boolean	isPersistable	= data != null && data.mAllowPersistence;
		
		return( isPersistable );
	}
	
		public synchronized Properties
	exportProperties( )
	{
		final Properties	props	= new Properties();
		
		final Iterator iter	= getKeys().iterator();
		while( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			
			if ( isPersistable( key ) )
			{
				props.put( key, get( key ).toString() );
			}
		}
		
		return( props );
	}
	
		public synchronized void
	importProperties( final Properties props  )
	{
		final Enumeration propertyNames	= props.propertyNames();
		
		while ( propertyNames.hasMoreElements() )
		{
			final String	name	= (String)propertyNames.nextElement();
			final String	value	= props.getProperty( name );
			
			put( name, value, true );
		}
	}
	
	private final static String	HEADER	= "environment properties";
	
		public synchronized void
	load( final File file )
		throws IOException
	{
		boolean	saveSave	= mNeedsSave;
		
		mFile	= file;
		
		FileInputStream	inputStream	= null;
		
		try
		{
			inputStream	= new FileInputStream( file );
			
			final Properties	props	= new Properties();
			props.load( inputStream );
			inputStream.close();
		
			importProperties( props );
		}
		catch( FileNotFoundException e )
		{
			// ignore--OK if it does not exist
			if ( inputStream != null )
			{
				inputStream.close();
			}
		}
		
		mNeedsSave	= saveSave;
	}
	
		public synchronized void
	store( File file )
		throws FileNotFoundException, IOException
	{
		final Properties	props	= exportProperties( );
		
		FileOutputStream	outputStream	= null;
		
		try
		{
			final File	temp	= SafeSave.getTempFile( file );
			
		 	outputStream	= new FileOutputStream( temp );
			props.store( outputStream, HEADER );
			outputStream.close();
			outputStream	= null;
			
			SafeSave.replaceWithNew( file, temp );
		}
		catch( Exception e )
		{
			System.err.println( "can't store environment: " + e.getMessage() );
		}
		finally
		{
			if ( outputStream != null )
			{
				outputStream.close();
			}
		}
		
		mNeedsSave	= false;
	}
	
		public synchronized void
	store()
		throws FileNotFoundException, IOException
	{
		if ( mFile != null )
		{
			store( mFile );
		}
	}
	
	
		public boolean
	needsSave()
	{
		return( mNeedsSave );
	}
	
	/**
		Save the CmdEnv.
	 */
		public void
	save() throws FileNotFoundException, IOException
	{
		if ( needsSave() )
		{
			store();
		}
	}
};





	