/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdFactoryIniter.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 
package com.sun.cli.jcmd.framework;

import java.lang.reflect.Method;


/**
	Helper object for initializing a CmdFactory using a CmdSource, other CmdFactory, or a
	Cmd class.
 */
public final class CmdFactoryIniter
{
	final CmdFactory	mFactory;
	
	/**
		Create a new initer referencing the factory
		
		@param factory	the CmdFactory to use
	 */
		public
	CmdFactoryIniter( CmdFactory factory)
		throws Exception
	{
		mFactory	= factory;
	}
	
	/**
		Create a new initer referencing the factory.  Add the commands in 'cmds'
		to the factory.
		
		@param factory	the CmdFactory to use
		@param cmds		the CmdSource to initialize the factory with
	 */
		public
	CmdFactoryIniter( CmdFactory factory, CmdSource cmds )
		throws Exception
	{
		mFactory	= factory;
		addMappings( cmds );
	}

	
	/**
		Add mappings for all Cmd objects found with the CmdSource.
		
		@param cmds		the CmdSource supplying the Cmd classes
	 */
		public String[][]
	addMappings( CmdSource cmds )
		throws Exception
	{
		final Class []		commandClasses	= cmds.getClasses();
		final String[][]	names	= new String[ commandClasses.length ][];
		
		for( int i = 0; i < commandClasses.length; ++i )
		{
			names[ i ]	= addMappingsForClass( commandClasses[ i ] );
		}
		
		return( names );
	}
	
	/**
		Add mappings for a specific Cmd class.
		
		@param theClass		Class object for a Cmd class
	 */
		public String[]
	addMappingsForClass( Class theClass )
		throws Exception
	{
		final String []	names	= CmdBase.getCmdNames( theClass );
		
		for( int i = 0; i < names.length; ++i )
		{
			mFactory.addCmdMapping( names[ i ], theClass );
		}
		
		return( names );
	}
	
	/**
		Remove mappings for a specific Cmd class.
		
		@param theClass		Class object for a Cmd class
	 */
		public void
	removeMappingsForClass( Class theClass )
		throws Exception
	{
		final String []	names	= CmdBase.getCmdNames( theClass );
		
		for( int i = 0; i < names.length; ++i )
		{
			mFactory.removeCmdMapping( names[ i ] );
		}
	}
	
	/**
		Remove mappings for a Cmd classes provided by the CmdSource
		
		@param cmds		the CmdSource supplying the Cmd classes 
	 */
		public void
	removeMappings( CmdSource cmds )
		throws Exception
	{
		final Class []	classes	= cmds.getClasses();
		
		for( int i = 0; i < classes.length; ++i )
		{
			removeMappingsForClass( classes[ i ] );
		}
	}
}

