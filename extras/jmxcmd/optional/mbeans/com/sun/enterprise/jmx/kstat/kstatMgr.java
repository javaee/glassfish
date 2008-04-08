/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/mbeans/com/sun/enterprise/jmx/kstat/kstatMgr.java,v 1.2 2003/11/12 02:07:23 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 02:07:23 $
 */
package com.sun.enterprise.jmx.kstat;

import javax.management.*;

import java.lang.Runtime;
import java.lang.Process;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sun.cli.jcmd.util.cmd.LineReaderImpl;

import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;

final class kstatModule
{
	final String	mName;
	final HashMap	mStats;
	
		public
	kstatModule( String name )
	{
		mName	= name;
		mStats	= new HashMap();
	}
	
		public void
	add( kstat stat )
	{
		mStats.put( stat.getName(), stat );
	}
	
		public kstat
	getkstat( String name )
	{
		return( (kstat)mStats.get( name ) );
	}
	
		public String
	getName()
	{
		return( mName );
	}
	
		public Set
	getNames()
	{
		return( mStats.keySet() );
	}
}


class kstatCache implements kstatRepository
{
	long	mRefreshMillis;
	long	mLastRefreshMillis;
	
	final HashMap	mkstats;
	final HashMap	mModules;
	
		private static void
	dm( Object o )
	{
		System.out.println( o.toString() );
	}

		public
	kstatCache()
	{
		mRefreshMillis		= 30 * 1000;
		mLastRefreshMillis	= 0;
		mkstats				= new HashMap();
		mModules			= new HashMap();
	}
	
		public void
	clear()
	{
		mkstats.clear();
		mModules.clear();
	}
	
		private kstat
	parsekstatAttr( final String line )
	{
		// parse first line for module name and instance number
		String []	tok	= line.split( ":" );
		
		// kstat -p spits out malformed lines occassionally, so check for the correct
		// number of tokens
		kstat	stat	= null;
		if ( tok.length == 4 )
		{
			//dm( ArrayStringifier.stringify( tok, "," ) );
			final String	moduleName		= tok[ 0 ];
			final int		instanceNumber	= Integer.parseInt( tok[ 1 ] );
			final String	name			= tok[ 2 ];
			final String	attrAll			= tok[ 3 ];
			
			tok	= attrAll.split( "[ \t]+" );
			
			if ( tok.length == 2 )
			{
				final String	attrName	= tok[ 0 ].trim();
				final String	attrValue	= tok[ 1 ].trim();
				
				final kstat.kstatAttribute attr	= new kstat.kstatAttribute( attrName, attrValue );
				
				final String	scopedName	=
									kstat.getScopedName( moduleName, instanceNumber, name );
				stat	= (kstat)mkstats.get( scopedName );
				if ( stat == null )
				{
					stat	= new kstat( moduleName, instanceNumber, name );
				}
				stat.addAttribute( attr );
			}
		}
		
		return( stat );
	}
	
		private void
	add( final kstat	stat )
	{
		mkstats.put( stat.getScopedName( ), stat );
		
		final String	moduleName	= stat.getModuleName();
		
		kstatModule	module	= (kstatModule)mModules.get( moduleName );
		if ( module == null )
		{
			module	= new kstatModule( moduleName );
			mModules.put( moduleName, module );
		}
		
		module.add( stat );
	}

		private void
	processResults( String [] lines )
		throws java.io.IOException
	{
		final ListIterator	iter	= Arrays.asList( lines ).listIterator( );
		
		while( iter.hasNext() )
		{
			final String	line	= ((String)iter.next()).trim();
			
			final kstat	stat = parsekstatAttr( line );
			if ( stat != null )
			{
				add( stat );
			}
		}
	}

		private Process
	invoke_kstat( String args )
		throws java.io.IOException
	{
		final String	execString	= "kstat -p " + ((args == null) ? "" : args);
		
		dm( "invoking kstat as: " + execString );
		// invoke kstat with -p option, which is machine parseable output
		return( Runtime.getRuntime().exec( execString ) );
	}
	

	
		private String []
	readResults( final InputStream resultsStream )
		throws java.io.IOException
	{
		final InputStreamReader reader	= new InputStreamReader( resultsStream );
		
		final StringBuffer	sbuf	= new StringBuffer();
		int	count;
		final char []	cbuf	= new char [ 256 * 1024 ];
		while ( (count = reader.read( cbuf, 0, cbuf.length)) >= 0 )
		{
			sbuf.append( cbuf, 0, count );
		}
		
		return( sbuf.toString().split( "\n" ) );
	}
	
		public void
	refresh( String scopedName )
		throws java.io.IOException
	{
		if ( scopedName == null || scopedName.equals( "" )  )
		{
			clear();
		}

		final Process	proc	= invoke_kstat( scopedName );
		
		final InputStream	resultsStream	= proc.getInputStream();
		
		final String []	outputLines	= readResults( resultsStream );
		
		processResults( outputLines );
		
		try
		{
			proc.waitFor();
		}
		catch( InterruptedException e )
		{
			System.err.println( "Interrupted: " + e.toString() );
		}
		
		mLastRefreshMillis	= System.currentTimeMillis();
	}
	
		public void
	refresh()
		throws java.io.IOException
	{
		refresh( "" );
	}
	
		void
	maybeRefresh()
	{
		if ( ( System.currentTimeMillis() - mLastRefreshMillis ) > mRefreshMillis )
		{
			try
			{
				refresh();
			}
			catch( java.io.IOException e )
			{
				System.err.println("couldn't refresh kstat" );
			}
		}
	}

		public void
	setRefreshMillis( long	millis)
	{
		mRefreshMillis	= millis;
		maybeRefresh();
	}
	
		public Set
	getModuleNames()
	{
		maybeRefresh();
		
		return( mModules.keySet() );
	}
	
		public Set
	getNamesInModule( String moduleName )
	{
		maybeRefresh();
			
		Set	names	= null;
		
		final kstatModule	module	= (kstatModule)mModules.get( moduleName );
		
		if ( module != null )
		{
			names	= module.getNames();
		}
		return( names );
	}

		public kstat
	getkstat( String moduleName, String name )
	{
		maybeRefresh();
		
		final kstatModule	module	= (kstatModule)mModules.get( moduleName );
		kstat	stat	= null;
		if ( module != null )
		{
			stat	= module.getkstat( name );
		}
		
		return( stat  );
	}
	
		public String
	query_kstatAttribute( String module, int instance, String name, String attributeName )
	{
		maybeRefresh();
		return( null );
	}
};


/*
	Solaris specific MBean to support kstat
 */
public final class kstatMgr implements kstatMgrMBean, MBeanRegistration
{
	MBeanServer			mServer;
	ObjectName			mMyName;
	final kstatCache	mCache;
	
	public final static String	KSTAT_DOMAIN	= "kstat";
	
		private static void
	dm( Object o )
	{
		System.out.println( o.toString() );
	}
	
		public
	kstatMgr()
	{
		mCache		= new kstatCache();
		mServer		= null;
		mMyName		= null;
		// we get this in preRegister()
	}
	
	
		public ObjectName
	preRegister(MBeanServer server, ObjectName name)
	{
		mServer		= server;
		mMyName		= name;
		
		return( mMyName );
	}
	
		public void
	postRegister( Boolean registrationDone )
	{
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
	
	
	private static final String	TYPE_PROPERTY		= "type=kstat";
	private static final String	NAME_PROPERTY= "name";
	private static final String	MODULE_PROPERTY		= "kstat-module";
	private static final String	INSTANCE_PROPERTY	= "kstat-instance";
	private static final String	KSTAT_NAME_PROPERTY		= "kstat-name";
	private static final String	CLASS_PROPERTY		= "kstat-class";
	private static final char	PROPERTY_DELIM		= ',';
	private static final char	PROPERTY_VALUE_DELIM		= '=';
	
	
		private String
	mapkstatName( final String name)
	{
		String	result	= name;
		
		if ( name.indexOf( ',' ) >= 0 )
		{
			final char []	chars	= name.toCharArray();
			
			for( int i = 0; i < chars.length; ++i )
			{
				if ( chars[ i ] == ',' )
				{
					chars[ i ]	= '.';
				}
			}
			
			result	= new String( chars );
			
		}
		return( result );
	}

		private String
	createObjectNameString( String domain, final kstat stat )
	{
		// kstat names can contain the "," character, which we can't use in an ObjectName
		final String kstatName	= mapkstatName( stat.getName() );
		
		final String nameProperty	= stat.getModuleName() + "." + kstatName;
		
		final String name	= domain + ":" + 
			TYPE_PROPERTY + PROPERTY_DELIM +
			NAME_PROPERTY + PROPERTY_VALUE_DELIM + nameProperty + PROPERTY_DELIM +
			KSTAT_NAME_PROPERTY + PROPERTY_VALUE_DELIM + kstatName + PROPERTY_DELIM +
			MODULE_PROPERTY + PROPERTY_VALUE_DELIM + stat.getModuleName();
			
			/* + PROPERTY_DELIM + 
			INSTANCE_PROPERTY + PROPERTY_VALUE_DELIM + stat.getInstanceNumber() + PROPERTY_DELIM +
			KSTAT_NAME_PROPERTY + PROPERTY_VALUE_DELIM + kstatName + PROPERTY_DELIM +
			CLASS_PROPERTY + PROPERTY_VALUE_DELIM + stat.getkstatClass(); */
		
		return( name );
	}
	
		private void
	addMBeanFor_kstat( final kstat stat )
	{
		String	objectNameString	= createObjectNameString( KSTAT_DOMAIN, stat );
		
		try
		{
			final kstatMBean	mb	= new kstatMBean( this, stat );
			
			final ObjectName	objectName	= new ObjectName( objectNameString );
		
			unregisterMBean( objectName );
			mServer.registerMBean( mb, objectName );
		}
		catch( Exception e )
		{
			System.err.println( "Can't add object named: " + objectNameString );
			
			System.err.println( e.getMessage() );
		}
	}

		private void
	addMBeansForModule( final String moduleName )
	{
		final Set	names	= mCache.getNamesInModule( moduleName );
		final Iterator	iter	= names.iterator();
		
		while ( iter.hasNext() )
		{
			addMBeanFor_kstat( mCache.getkstat( moduleName, (String)iter.next() ) );
		}
	}
	
		private void
	addMBeansForModules( final Set moduleNames )
		throws Exception
	{
		final Iterator	iter	= moduleNames.iterator();
		
		while ( iter.hasNext() )
		{
			addMBeansForModule( (String)iter.next() );
		}
	}
	
		private void
	unregisterMBean( ObjectName name )
	{
		try
		{
			mServer.unregisterMBean( name );
		}
		catch( InstanceNotFoundException e )
		{
			// that's fine, we wanted it unregistered anyway
		}
		catch( MBeanRegistrationException e )
		{
			// that's fine, we wanted it unregistered anyway
		}
	}
	
		private void
	unregisterAll( Iterator objectNames )
		throws MBeanRegistrationException
	{
		while ( objectNames.hasNext() )
		{
			unregisterMBean( (ObjectName)objectNames.next() );
		}
	}
	
//---------------------------------------------------

		public synchronized void
	initkstats()
		throws Exception
	{
		refresh();
	}
	
		public synchronized void
	clearkstats()
	{
		mCache.clear();
		
		try
		{
			final ObjectName	pattern	= new ObjectName( KSTAT_DOMAIN + ":type=kstat,*" );
			final Set allkstatMBeans	= mServer.queryNames( pattern, null );
			
			unregisterAll( allkstatMBeans.iterator() );
		}
		catch( MalformedObjectNameException e )
		{
			// a bug
			assert( false );
		}
		catch( MBeanRegistrationException e )
		{
			// a bug
			assert( false );
		}
	}
	
	
		public void
	refresh( String scopedName )
		throws Exception
	{
		mCache.refresh( scopedName );
		
		final Set moduleNames	= mCache.getModuleNames();
		addMBeansForModules( moduleNames );
	}
	
		public void
	refresh(  )
		throws Exception
	{
		clearkstats();
		
		refresh( null );
	}
};


