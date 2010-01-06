/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdReader.java,v 1.9 2004/10/14 19:06:17 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2004/10/14 19:06:17 $
 */
 
package com.sun.cli.jcmd.framework;


import com.sun.cli.jcmd.util.misc.TokenizerImpl;
import com.sun.cli.jcmd.util.misc.TokenizerParams;
import com.sun.cli.jcmd.util.misc.TokenizerException;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.LineReader;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;



/**
	Takes care of reading commands from the input and passing them along to a
	CmdRunner.
 */
public class CmdReader 
{
	final CmdOutput	mOutput;
	String			mPrompt;
	LineHook		mLineHook;
	
		public
	CmdReader( CmdOutput output, LineHook lineHook )
	{
		this( output, "> ", lineHook );
	}
	
		public
	CmdReader( CmdOutput output, String prompt, LineHook lineHook )
	{
		mOutput		= output;
		mPrompt		= prompt;
		mLineHook	= lineHook;
	}
	
	public interface LineHook
	{
		public String	processLine( String line );
	}
	
	/**
		Read a line from the input, trim white space.
		
		@param lineReader	the source of the line.
	 */
		public String
	readLine( LineReader lineReader )
		throws Exception
	{
		String	line	= lineReader.readLine( mPrompt );
		
		if ( line != null )
		{
			line	= line.trim();
		}
		
		return( line );
	}
	
	private final static String	DELIM_CHARS		= " \t";
		static String []
	lineToTokens( String line ) throws TokenizerException
	{
		final TokenizerParams	params	= new TokenizerParams();
		params.mDelimiters	= DELIM_CHARS;
		params.ensureDelimitersEscapable();
		
		final TokenizerImpl tk	= new TokenizerImpl( line, params);
		
		return( tk.getTokens( ) );
	}
	
		private String
	runLineHook( final String lineIn )
	{
		String	lineOut	= lineIn;
		
		if ( mLineHook != null )
		{
			// keep calling the hook until the line no longer changes
			int		depth	= 0;
			while ( true )
			{
				final String	tempLine	= mLineHook.processLine( lineOut );
				final boolean	changed		= lineOut != tempLine || ! tempLine.equals( lineOut );
				
				if ( ! changed )
				{
					break;
				}
				
				lineOut	= tempLine;
				++depth;
				if ( depth > 100 )
				{
					throw new IllegalArgumentException( "command aliasing too deep" );
				}
			}
		}
		
		return( lineOut );
	}
	
	/**
		Tokenize the line and execute the appropriate command.
		
		@param lineIn		a command line as read from the input
		@param cmdRunner	object which executes the commands
		@return	-1 if an exception occured, 0 otherwise
	 */
		public int
	processLine( final String lineIn, final CmdRunner cmdRunner )
		throws TokenizerException
	{
		int	errorCode	= 0;
		
		final String	trimmedLine	= lineIn.trim();
		
		if ( trimmedLine.length() != 0 && ! isCommentLine( trimmedLine ) )
		{
			final String	finalLine	= runLineHook( trimmedLine );

			final String [] tokens	= lineToTokens( finalLine );
			mOutput.printDebug( "CmdReader.processLine: \"" + finalLine + "\" = {"+
				ArrayStringifier.stringify( tokens, "," ) + "}" );
			
			final String cmdName	= tokens[ 0 ];

			errorCode	= cmdRunner.execute( cmdName, tokens );
		}
		
		return( errorCode );
	}
	
	/**
		Tokenize the line and execute the appropriate command.
		
		@param line			a command line as read from the input
		@param cmdRunner	object which executes the commands
	 */
		public int
	processCmd( final String line, final CmdRunner cmdRunner )
		throws TokenizerException
	{
		return( processLine( line, cmdRunner ) );
	}

		boolean
	isCommentLine( String line )
	{
		return( line.trim().startsWith( "#" ) );
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
	
	/**
		Enter interactive mode reading commands until a quit command is reached.
		
		@param greeting		optional greeting to display once
		@param lineReader	used to read commands
		@param cmdRunner	object which executes the commands
	 */
		public void
	goInteractive(
		final String		greeting,
		final LineReader	lineReader,
		final CmdRunner		cmdRunner )
		throws Exception
	{
		if ( greeting != null )
		{
			mOutput.print( greeting );
		}
		
		while ( true )
		{
			final String	lineIn	= readLine( lineReader );
			
			if ( isQuitLine( lineIn ) )
			{
				mOutput.println( "Quitting..." );
				break;
			}
		
			if ( isCommentLine( lineIn ) || lineIn.length() == 0 )
				continue;
			
			try
			{
				final int	errorCode	= processLine( lineIn, cmdRunner );
			}
			catch( Exception e )
			{
				mOutput.printError( "Error processing line " +
					StringUtil.quote( lineIn ) + ": " +
					e.getMessage() );
				mOutput.printDebug( ExceptionUtil.getStackTrace( e ) );
			}
		}
	}
}

