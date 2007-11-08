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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/CmdBase.java,v 1.3 2005/12/25 03:45:28 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:28 $
 */
 

package com.sun.cli.jmx.cmd;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.sun.cli.util.stringifier.SmartStringifier;
import com.sun.cli.util.stringifier.Stringifier;
import com.sun.cli.util.stringifier.StringifierRegistry;
import com.sun.cli.util.stringifier.ArrayStringifier;
import com.sun.cli.util.DebugState;

import com.sun.cli.jmx.cmd.ArgHelper;

public abstract class CmdBase implements Cmd, CmdOutput, DebugState
{
	public final static String	ENV_TOKENS		= "TOKENS";
	public final static String	ENV_CMD_RUNNER	= "CMD_RUNNER";
	public final static String	ENV_CMD_FACTORY	= "CMD_FACTORY";
	public final static String	ENV_COMMANDS	= "COMMANDS";
	
	// user accessible
	public final static String	ENV_DEBUG		= "debug";
	
	private final CmdEnv		mEnv;
	final String []				mTokens;
	private ArgHelper			mArgHelper;
	
	private CmdOutput			mOutput;
	
	Stringifier					mStringifier	=
		new SmartStringifier( StringifierRegistry.DEFAULT, "\n", false );
	
	// debug message
		void
	dm( Object o )
	{
		mOutput.println( mStringifier.stringify( o ) );
	}
	
	CmdBase( final CmdEnv env )
	{
		mEnv		= env;
		
		mOutput	= new CmdOutputImpl( System.out, System.err );
		
		mTokens		= (String [])env.get( "TOKENS" );
		
		// can't instantiate here; need the result of virtual method getOptionInfo()
		mArgHelper	= null;
	}
	
	/*
		Wrapper around the cmd-supplied OptionsInfo which allows us to layer 
		additional global options onto the command, such as -h, --help.
	 */
	private static class OptionsInfoWrapper implements ArgHelper.OptionsInfo
	{
		final ArgHelper.OptionsInfo	mInfo;
		
		OptionsInfoWrapper( ArgHelper.OptionsInfo info )
		{
			mInfo	= info;
		}
		
			static boolean
		isHelp( String token )
		{
			return( 	token.equalsIgnoreCase( "-h" ) ||
						token.equalsIgnoreCase( "--help" ) ||
						token.equalsIgnoreCase( "-help" ) );
		}
		
		public String		tokenToOptionName( String token )
		{ return( mInfo.tokenToOptionName( token ) ); }
			
		public String		tokenToOptionData( String token )
		{ return( mInfo.tokenToOptionData( token ) ); }
			
		public boolean		isLegalOption( String token )
		{
			return( mInfo.isLegalOption( token ) || isHelp( token ) );
		}
			
		public boolean		isBoolean( String token )
		{ return( isHelp( token ) ? true : mInfo.isBoolean( token ) ); }
			
		public int			getNumValues( String token )
		{ return( isHelp( token ) ? 0 : mInfo.getNumValues( token ) ); }
	}
	
		void
	initArgHelper()
		throws ArgHelper.IllegalOptionException
	{
		if ( mArgHelper == null )
		{
			final java.util.ListIterator	iter	=
				Arrays.asList( mTokens ).listIterator( 1 );
			
			final ArgHelper.OptionsInfo		optionsInfo	= getOptionInfo();
			
			mArgHelper	= new ArgHelperImpl( iter, new OptionsInfoWrapper( optionsInfo ) );
		}
	}
	
		CmdFactory
	getCmdFactory()
	{
		return( (CmdFactory)envGet( ENV_CMD_FACTORY ) );
	}
	
	
		void
	envRemove( String key)
	{
		mEnv.remove( key );
	}
	
		Object
	envGet( String key)
	{
		return( mEnv.get( key ) );
	}
	
		Object
	envGet( String key, Object defaultValue )
	{
		Object value	= envGet( key );
		
		if ( value == null )
		{
			value	= defaultValue;
		}

		return( value );
	}
	
		java.util.Set
	getEnvKeys(  )
	{
		return( mEnv.getKeys() );
	}
	
		java.util.Set
	getEnvKeys( String regExp )
	{
		final Iterator	iter	= getEnvKeys().iterator();
		final java.util.Set		filteredSet	= new java.util.HashSet();
		
		final Pattern	 pattern	= Pattern.compile( regExp );
		
		while ( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			
			final Matcher	m	= pattern.matcher( key );
			
			if ( m.matches() )
			{
				filteredSet.add( key );
			}
			
		}

		return( filteredSet );
	}
	
		void
	envPut( String key, Object value, boolean allowPersistence )
	{
		mEnv.put( key, value, allowPersistence);
	}
	
		ArgHelper
	getArgHelper()
	{
		assert( mArgHelper != null );
		
		return( mArgHelper );
	}
	
		int
	countOptions( )
		throws ArgHelper.IllegalOptionException
	{
		return( getArgHelper().countOptions( ) );
	}
	
		String
	getString( String name, String defaultValue)
		throws ArgHelper.IllegalOptionException
	{
		return( getArgHelper().getString( name, defaultValue ) );
	}
	
	
		Integer
	getInteger( String name)
		throws ArgHelper.IllegalOptionException
	{
		return( getArgHelper().getInteger( name ) );
	}
	
		Boolean
	getBoolean( String name, Boolean defaultValue)
		throws ArgHelper.IllegalOptionException
	{
		return( getArgHelper().getBoolean( name, defaultValue ) );
	}
	
		String []
	getOperands( )
	{
		return( getArgHelper().getOperands( ) );
	}
	
		String
	getCmdNameAsInvoked( )
	{
		return( mTokens[ 0 ] );
	}
	
		void
	requireNumOperands( final int numRequiredOperands)
		 throws WrongNumberOfOperandsException
	{
		final String []	operands	= getOperands();
		
		if ( operands.length < numRequiredOperands )
		{
			requireNumOperandsFailed( operands.length, numRequiredOperands, null);
		}
	}	
	
		void
	requireNumOperands( final int numRequiredOperands, String msg)
		 throws WrongNumberOfOperandsException
	{
		final String []	operands	= getOperands();
		
		if ( operands.length < numRequiredOperands )
		{
			requireNumOperandsFailed( operands.length, numRequiredOperands, msg);
		}
	}
	
		int
	getNumRequiredOperands()
	{
		// require 1, by default
		return( 1 );
	}
	
	class WrongNumberOfOperandsException extends Exception
	{
		WrongNumberOfOperandsException( String msg )
		{
			super( msg );
		}
	}
	
		void
	requireNumOperandsFailed( int numSupplied, int numRequired, String msg)
		 throws WrongNumberOfOperandsException
	{
		printError( "ERROR: " + numRequired + " arguments required, " + numSupplied + " supplied" );
		if ( msg != null )
		{
			println( msg );
		}
		else
		{
			printUsage();
		}
		throw new WrongNumberOfOperandsException( "illlegal number of operands" );
	}
	
	
		ArgHelper.OptionsInfo
	getOptionInfo()
		throws ArgHelper.IllegalOptionException
	{
		// default to none
		return( new ArgHelperOptionsInfo( ) );
	}
	
	
		public void
	print( Object o )
	{
		mOutput.print( o );
	}
	
		public void
	println( Object o )
	{
		mOutput.println( o );
	}
	
		public void
	printError( Object o )
	{
		mOutput.printError( o );
	}
	
		public void
	printDebug( Object o )
	{
		mOutput.printDebug( o );
	}
	
		public boolean
	getDebug()
	{
		boolean	isDebug	= false;
		final String	value	= (String)envGet( ENV_DEBUG );
		if ( value != null )
		{
			isDebug	=	value.equalsIgnoreCase( "true" ) ||
						value.equalsIgnoreCase( "t" ) ||
						value.equalsIgnoreCase( "yes" )||
						value.equalsIgnoreCase( "y" );
		}
		return( isDebug );
	}
	
		void
	preExecute()
		throws Exception
	{
		initArgHelper();
		requireNumOperands( getNumRequiredOperands() );
	}
	
		void
	handleException( final Exception e )
	{
		if ( e instanceof WrongNumberOfOperandsException )
		{
			// already reported
		}
		else if ( e instanceof ArgHelper.IllegalOptionException )
		{
			printError( "ERROR: " + e.getMessage() );
			printUsage();
		}
		else if ( e instanceof java.io.IOException)
		{
			printError( "ERROR: " + e.getMessage() );
		}
		else
		{
			printError( "ERROR: exception of type: " +
				e.getClass().getName() + ", msg = " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
		public final void
	execute( )
		throws Exception
	{
		try
		{
			preExecute();
		
			if ( getBoolean( "help", Boolean.FALSE ).booleanValue() ||
					getBoolean( "h", Boolean.FALSE ).booleanValue() )
			{
				printUsage();
			}
			else
			{
				executeInternal( );
			}
		}
		catch( Exception e )
		{
			handleException( e );
			e.printStackTrace();
		}
	}
	
	abstract void		executeInternal( ) throws Exception;
	abstract String		getUsage( );
		
	private static final Class []	EMPTY_SIG	= new Class [ 0 ];
	private static final Object []	EMPTY_ARGS	= new Object [ 0 ];
	
		public static String []
	getCmdNames( final Class theClass )
		throws java.lang.NoSuchMethodException,
			java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException
	{
		String []	names	= null;
		
		final java.lang.reflect.Method	m	= theClass.getDeclaredMethod( "getNames", EMPTY_SIG );
	
		names	= (String [])m.invoke( theClass, EMPTY_ARGS);
		
		return( names );
	}
	
		String
	getAlsoKnownAs( Class theClass )
	{
		String	aka	= "";
		
		try
		{
			aka	= ArrayStringifier.stringify( getCmdNames( theClass ), " " );
		}
		catch( Exception e )
		{
			// Hmmm..
		}
		
		return( "Also known as: " + aka );
	}
	
		void
	printUsage()
	{
		println( getUsage() );
		
		
		println( getAlsoKnownAs( this.getClass() ) );
	}
}

