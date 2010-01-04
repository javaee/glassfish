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



