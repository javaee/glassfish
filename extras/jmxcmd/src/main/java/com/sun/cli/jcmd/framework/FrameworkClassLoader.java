/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jcmd.framework;

import java.lang.ClassLoader;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import com.sun.cli.jcmd.framework.CmdOutput;


/**
	Classload to support additional jars and folders, set up as the system
	classloader.
 */
public final class FrameworkClassLoader extends URLClassLoader
{
	final List<URL>		mSources;
	CmdOutput			mCmdOutput;
	
	static private FrameworkClassLoader	INSTANCE	= null;
	
		private synchronized static void
	setInstance( FrameworkClassLoader	instance )
	{
		// the first one that's created will be forever the value of 'INSTANCE'
		// we can't create statically, because we must wait for the system to create us
		if ( INSTANCE == null )
		{
			INSTANCE	= instance;
		}
		else
		{
			System.out.println( "WARNING: more than one FrameworkClassLoader is being created." );
		}
	}
	
		public static synchronized FrameworkClassLoader
	getInstance()
	{
		return( INSTANCE );
	}
	
		public
	FrameworkClassLoader( ClassLoader parent )
		throws java.net.MalformedURLException
	{
		super( new URL[0], parent );
		
		mSources		= new ArrayList<URL>();
		mCmdOutput		= new CmdOutputNull();
		
		setInstance( this );
		Thread.currentThread().setContextClassLoader( this );
	}
	
		public void
	setCmdOutput( CmdOutput	cmdOutput )
	{
		if ( cmdOutput == null )
		{
			throw new IllegalArgumentException( "CmdOutput must not be null" );
		}

		mCmdOutput	= cmdOutput;
	}
	
		public void
	addURL( final URL	url )
	{
		if ( mSources.indexOf( url ) < 0 )
		{
			super.addURL( url );
			mSources.add( url );
			mCmdOutput.printDebug( "FrameworkClassLoader.addURL: " + url );
		}
		else
		{
			throw new IllegalArgumentException( "Item \"" + url + "\" already in list" );
		}
	}
}

