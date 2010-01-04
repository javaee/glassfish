/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdFactory.java,v 1.3 2005/11/08 22:39:16 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:16 $
 */
 
package com.sun.cli.jcmd.framework;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

/**
	Factory for registering Cmd classes and creating instances of them.
 */
public class CmdFactory
{
	private final Map<String,Class<? extends Cmd>>			mCmds;
	private UnknownCmdClassGetter	mCmdClassGetter;
	
	public static final String	DEFAULT_CMD_NAME	= "DEFAULT_CMD";
	
		private static void
	p( Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}
	
	/**
		When a command cannot be found for a given name, this is called
		to get one.
	 */
	public interface UnknownCmdClassGetter
	{
		public Class<? extends Cmd>	getCmdClass( String name );
	}
	
	private class MyCmdClassGetter implements UnknownCmdClassGetter
	{
			public
		MyCmdClassGetter()
		{
		}
		
			public Class<? extends Cmd>
		getCmdClass( String name )
		{
			return( mCmds.get( DEFAULT_CMD_NAME ) );
		}
	}

	
	/**
	 */
		public
	CmdFactory()
	{
		mCmds = new HashMap<String,Class<? extends Cmd>>();
		mCmdClassGetter	= new MyCmdClassGetter();
	}
	
		public void
	setUnknownCmdClassGetter( UnknownCmdClassGetter	getter )
	{
		mCmdClassGetter	= getter;
	}
	
		public UnknownCmdClassGetter
	getUnknownCmdClassGetter( )
	{
		return( mCmdClassGetter );
	}
	
	/**
		Add a mapping between a command name and an implementing class.
		
		The class must have a constructor that takes a CmdEnv, though it is not required
		to do anything with it.
	 */
		public void
	addCmdMapping( final String name, final Class<? extends Cmd> theClass )
	{
		if ( ! Cmd.class.isAssignableFrom( theClass ) )
		{
			throw new IllegalArgumentException( "Command " + theClass.getName() +
				" must implement " + Cmd.class.getName() );
		}
		
		mCmds.put( name, theClass );
	}
	
	/**
		Remove the mapping from this command name to an implementing class.
		
		@param name	name of the command
	 */
		public void
	removeCmdMapping( final String name )
	{
		mCmds.remove( name );
	}
	
	
	
	
	/**
		Get the implementing class for a Cmd.
		
		@param cmdName	name of the command
		@return the class associated with the Cmd
	 */
		public Class<? extends Cmd>
	getClass( String cmdName  )
	{
		final Class<? extends Cmd>	cmdClass	= mCmds.get( cmdName );
		
		return( cmdClass );
	}
	
	
	/**
		Get all Cmd Classes registered.
		
		@return a Class[] of all command classes
	 */
		public List<Class<? extends Cmd>>
	getClasses(  )
	{
		final List<Class<? extends Cmd>>	s	= new ArrayList<Class<? extends Cmd>>();
		
		final String[]	names	= getNames();
		for( int i = 0; i < names.length; ++i )
		{
			s.add( getClass( names[ i ] ) );
		}
		
		
		return s;
	}
	
	
	/**
		Get all names currently registered
		
		@return a String[] of all associated names
	 */
		public String []
	getNames(  )
	{
		final String[]	names	= new String[ mCmds.keySet().size() ];
		
		mCmds.keySet().toArray( names );
		return( names );
	}
	
	
	/**
		Get all names associated with an implementing class.
		
		@param theClass	the implementing class
		@return a String[] of all associated names
	 */
		public String []
	getNames( Class theClass )
	{
		final List<String>	list	= new ArrayList<String>();
		
		final String[]	names	= getNames();
		for( int i = 0; i < names.length; ++i )
		{
			final String	key	= names[ i ];
			
			final Class	thisClass	= (Class)mCmds.get( key );
			if ( thisClass == theClass ) 
			{
				list.add( key );
			}
		}
		
		final String []	namesArray	= new String [ list.size() ];
		list.toArray( namesArray );
		
		return( namesArray );
	}
	
	/**
		Create an instance of the Cmd.
		
		@param cmdName	name of the command
		@param env	a CmdEnv for use by the Cmd
		@return an instance of the Cmd
	 */
		public Cmd
	createCmd( String cmdName, CmdEnv env )
		throws Exception
	{
		final String	cmdString	= cmdName;
		
		final Cmd cmd = createCmd( cmdName, mCmds.get( cmdName ), env );
		
		return( cmd );
	}
		Cmd
	createCmd( String cmdName, Class<? extends Cmd> cmdClass, CmdEnv env )
		throws Exception
	{
		if ( cmdClass == null )
		{
			cmdClass	= mCmdClassGetter.getCmdClass( cmdName );
			if ( cmdClass == null )
			{
				return( null );
			}
		}

		final Object []	args	= new Object [] { env };

		Cmd cmd	= null;
		try
		{
			cmd	= ClassUtil.InstantiateObject( cmdClass, args );
		}
		catch( Exception e )
		{
			p( e.getMessage() );
			e.printStackTrace();
			throw e;
		}
		
		return( cmd );
	}
}

