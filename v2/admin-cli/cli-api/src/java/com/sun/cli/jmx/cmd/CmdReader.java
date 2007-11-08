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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/CmdReader.java,v 1.3 2005/12/25 03:45:31 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:31 $
 */
 
package com.sun.cli.jmx.cmd;


import com.sun.cli.util.stringifier.SmartStringifier;
import com.sun.cli.util.LineReader;
import com.sun.cli.util.TokenizerImpl;


public class CmdReader implements CmdOutput
{
		public
	CmdReader( )
	{
	}
	
			public void
	print( Object o )
	{
		System.out.print( o.toString() );
	}
	
		public void
	println( Object o )
	{
		System.out.println( o.toString() );
	}
	
		public void
	printError( Object o )
	{
		System.err.println( o.toString() );
	}
	
		public void
	printDebug( Object o )
	{
		println( o );
	}
	
	
		private static void
	p( Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}
	
	
	private final static String ADVISORY =
	"Type 'help' for help, 'quit' to quit.\n";
		
	private final static String USAGE = initUsage();
	
		private static String
	initUsage()
	{
		String	usage	= "*** Available commands ***\n\n";
		
		final CmdStrings.CmdHelp []	allHelp	= CmdStrings.getAllHelp();
		
		for( int i = 0; i < allHelp.length; ++i )
		{
			usage	= usage + allHelp[ i ] + "\n\n";
		}
		
		return( usage );
	}
	
	
		public String
	readLine( LineReader lineReader )
		throws Exception
	{
		String	line	= lineReader.readLine( "> ");
		
		if ( line != null )
		{
			line	= line.trim();
		}
		
		return( line );
	}
	
	private final static String	DELIM_CHARS		= " \t";
	private final static char	ESCAPE_CHAR		= '\\';
	private final static String	ESCAPABLE_CHARS	= DELIM_CHARS + ESCAPE_CHAR + "\"";
		static String []
	lineToTokens( String line )
	{
		final TokenizerImpl tk	= new TokenizerImpl( line, "" + DELIM_CHARS,
										ESCAPE_CHAR, ESCAPABLE_CHARS);
		
		return( tk.getTokens( ) );
	}
	
		public static int
	processLine( final String line, final CmdRunner cmdRunner )
	{
		int	errorCode	= 0;
		
		final String [] tokens	= lineToTokens( line );
		
		try
		{
			final String cmdName	= tokens[ 0 ];
			
			cmdRunner.execute( cmdName, tokens );
		}
		catch (Exception e )
		{
			errorCode	= -1;
		}
		
		return( errorCode );
	}
	
		public void
	processCmd( final String line, final CmdRunner cmdRunner )
	{
		final String [] tokens	= lineToTokens( line );
		
		try
		{
			final String cmdName	= tokens[ 0 ];
			
			cmdRunner.execute( cmdName, tokens );
		}
		catch (Exception e )
		{
		}
	}

		boolean
	isQuitLine( String line )
	{
		final boolean	isQuit	= 
			line == null  ||
			line.equalsIgnoreCase( "quit" ) ||
			line.equalsIgnoreCase( "q" ) ||
			line.equalsIgnoreCase( "exit" );
			line.equalsIgnoreCase( "exit" );
			
		return( isQuit );
	}
	
		public void
	goInteractive(
		final LineReader	lineReader,
		final CmdRunner		cmdRunner )
		throws Exception
	{
		p( ADVISORY );
		
		while ( true )
		{
			final String	line	= readLine( lineReader );
			
			if ( isQuitLine( line ) )
			{
				println( "Quitting..." );
				break;
			}
			
			if ( line.length() == 0 )
				continue;
			
			processLine( line, cmdRunner );
		}
		
	}
}

