/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jcmd.util.cmd;

import java.util.Arrays;
import java.util.ListIterator;

import com.sun.cli.jcmd.util.misc.TokenizerImpl;
import com.sun.cli.jcmd.util.misc.TokenizerParams;
import com.sun.cli.jcmd.util.misc.TokenizerException;

/**
	Sample main() which uses ArgHelper.
	
	java com.sun.cli.jcmd.util.cmd.ArgHelperSampleMain
 */
public class ArgHelperSampleMain
{
		private
	ArgHelperSampleMain(  )
	{
		// disallow
	}
	
	/**
		If you read your own line, use lineToTokens() to tokenize the line.
	 */
	static private final String		DELIM_CHARS	= " \t";

		static String []
	lineToTokens( String line ) throws TokenizerException
	{
		final TokenizerParams	params	= new TokenizerParams();
		params.mDelimiters	= DELIM_CHARS;
		params.ensureDelimitersEscapable();
		final TokenizerImpl tk	= new TokenizerImpl( line, params );
		
		return( tk.getTokens( ) );
	}
	
	static private final String OPTIONS	= "help:? verbose:V version:v host:h,1 port:p,1";
		public static void
	main(String args[])
	{
		try
		{
			final OptionsInfo	optionsInfo	= new OptionsInfoImpl( OPTIONS );
			
			final ListIterator<String>	list	= Arrays.asList( args ).listIterator();
			final ArgHelper	helper = new ArgHelperImpl( list, optionsInfo );
			
			displayArgs( helper );
			
		}
		catch( Exception e )
		{
			System.err.println( e );
		}
	}

		private static void
	displayBoolean( final ArgHelper	helper, String name )
		throws IllegalOptionException
	{
		final Boolean	value	= helper.getBooleanValue( name, null );
		
		System.out.println( name + ": " + ( value == null ? "<not specified>" : value.toString() ) );
	}
	
		private static void
	displayInteger( final ArgHelper	helper, String name )
		throws IllegalOptionException
	{
		final Integer	value	= helper.getIntegerValue( name, null );
		
		System.out.println( name + ": " + ( value == null ? "<not specified>" : value.toString()) );
	}
	
		private static void
	displayString( final ArgHelper	helper, String name )
		throws IllegalOptionException
	{
		final String	value	= helper.getStringValue( name, "<not specified>");
		
		System.out.println( name + ": " + value );
	}
	
		private static void
	usage()
	{
		final String usage	= "[--help|-?] [--verbose|-v] [--version|-V] [--host|-h=<host>] [--port|-p=<port-number>]";
		
		System.out.println( "Usage: " + usage );
	}
	
		private static void
	displayOperands( final String []	operands )
		throws IllegalOptionException
	{
		System.out.println( "Operands:" );
		for( int i = 0; i < operands.length; ++i )
		{
			System.out.println( operands[ i ] );
		}
	}


		private static void
	displayArgs( final ArgHelper	helper )
		throws IllegalOptionException
	{
		if ( helper.countOptions() != 0 )
		{
			displayBoolean( helper, "h" );
			displayBoolean( helper, "help" );
			displayBoolean( helper, "verbose" );
			displayBoolean( helper, "version" );
			
			displayInteger( helper, "port" );
			displayString( helper, "host" );
		}
		else
		{
			System.out.println( "No options specified" );
			if ( helper.getOperands().length != 0 )
			{
				displayOperands( helper.getOperands() );
			}
			else
			{
				usage();
			}
		}
	}
};



