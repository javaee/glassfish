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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/ArgHelperImpl.java,v 1.4 2006/11/10 21:14:44 dpatil Exp $
 * $Revision: 1.4 $
 * $Date: 2006/11/10 21:14:44 $
 */
 
package com.sun.cli.jmx.cmd;


import java.util.ArrayList;
import java.util.ListIterator;
import com.sun.cli.util.stringifier.SmartStringifier;

final class ParsedOption
{
	final String		mName;
	final String	[]	mValues;
	
	ParsedOption( String name, String [] values )
	{
		assert( name.startsWith( "-" ) );
		
		mName	= name;
		mValues	= values;
	}
}


final class ParseState
{
	final ListIterator	mTokens;
	final ArrayList	mOptions;
	boolean			mDone;
	
	final ArgHelper.OptionsInfo mOptionInfo;
	
	ParseState(
		final ListIterator				tokens,
		final ArgHelper.OptionsInfo	optionInfo)
	{
		assert( tokens != null );
		
		mTokens				= tokens;
		mOptions			= new ArrayList();
		mDone				= ! tokens.hasNext();
		mOptionInfo			= optionInfo;
	}
	
		ParsedOption []
	getParsedOptions()
	{
		assert( done() );
		final ParsedOption []	options	= new ParsedOption [ mOptions.size() ];
		mOptions.toArray( options );
		
		return( options );
	}
	
		String []
	getOperands()
	{
		// put all the operands into one list
		final ArrayList	list	= new ArrayList();
		
		while( mTokens.hasNext() )
		{
			list.add( mTokens.next() );
		}
		
		String []	operands	= new String [ list.size() ];
		list.toArray( operands );
		
		return( operands );
	}
	
		String
	nextToken( )
	{
		return( (String)mTokens.next() );
	}
	
		String
	peekToken( )
	{
		final String	result	= (String)mTokens.next();
		mTokens.previous();
		
		return( result );
	}
	
		void
	setDone()
	{
		mDone	= true;
	}
	
		boolean
	done()
	{
		return( mDone || ! mTokens.hasNext() );
	}
}
	

public final class ArgHelperImpl implements ArgHelper
{
	final ParsedOption []	mOptions;
	final String []			mOperands;
	
	private final static String	ARG_PREFIX	= "--";
	
		private static void
	dm( Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}
	

		static void
	numValuesFailure( final String optionName, final int numValues )
		throws ArgHelper.IllegalOptionException
	{
		throw new ArgHelper.IllegalOptionException( "option \"" + optionName +
			"\" requires " + numValues + " values" );
	}
	
	
		static void
	booleanExpectedFailure( final String optionName )
		throws ArgHelper.IllegalOptionException
	{
		throw new ArgHelper.IllegalOptionException( "option \"" + optionName +
			"\" is a boolean option but has value(s)" );
	}
	
	// values are comma-separated
		static String []
	extractValues( final String optionName, final String valueList, final int numValues )
		throws ArgHelper.IllegalOptionException
	{
		// comma-separated list of args
		// FIX: need to parse the line, not just split it
		final String []	values	= valueList.split( "," );
		
		if ( values.length != numValues )
		{
			numValuesFailure( optionName, numValues );
		}

		return( values );
	}
	
	// values are in then tokens
		static String []
	extractValues( final ParseState ps, final String optionName, final int numValues )
		throws ArgHelper.IllegalOptionException
	{
		final String [] values	= new String [ numValues ];
		
		try
		{
			for( int valueIndex = 0; valueIndex < numValues; ++valueIndex )
			{
				values[ valueIndex ]	= ps.nextToken( );
			}
		}
		catch( java.util.NoSuchElementException e )
		{
			numValuesFailure( optionName, numValues );
		}
		catch( ArrayIndexOutOfBoundsException e )
		{
			numValuesFailure( optionName, numValues );
		}
		
		return( values );
	}
	
		private static String
	extractBoolean( final String optionName, final String value )
		throws ArgHelper.IllegalOptionException
	{
		// presence of a boolean option implies true
		String	result	= "true";
		
		if ( value != null )
		{
			// OK, check the value string is appropriate for a boolean
			if ( value.equalsIgnoreCase( "true" ) ||
				value.equalsIgnoreCase( "false" ) )
			{
				// OK
				result	= value;
			}
			else
			{
				booleanExpectedFailure( optionName );
			}
			
		}
		return( result );
	}
	
		static void
	handleOption( final ParseState ps )
		throws ArgHelper.IllegalOptionException
	{
		final String				token	= ps.nextToken();
		final ArgHelper.OptionsInfo	optionsInfo	= ps.mOptionInfo;
		
		final String	optionName	= optionsInfo.tokenToOptionName( token );
		final String	optionExtra	= optionsInfo.tokenToOptionData( token );
		
		final boolean	isBooleanOption		= optionsInfo.isBoolean( optionName );
		final int		numValues			= optionsInfo.getNumValues( optionName );
		
		String [] values	= null;
		if ( isBooleanOption )
		{
			values	= new String [] { extractBoolean( optionName, optionExtra ) };
		}
		else
		{
			if ( optionExtra != null )
			{
				// comma-separated list of args
				values	= extractValues( optionName, optionExtra, numValues );
			}
			else
			{
				// values are in token list
				values	= extractValues( ps, optionName, numValues );
			}
		}
		
		final String	name = ArgHelperOptionsInfo.OptionDesc.mapName( optionName );
		
		ps.mOptions.add( new ParsedOption( name, values ) );
	}
	
		void
	handleNonOption( final ParseState ps )
		throws ArgHelper.IllegalOptionException
	{
		final String	token	= ps.peekToken();
		
		if ( token.equals( ARG_PREFIX ) )
		{
			// "--" signifies the end of options
			ps.setDone();
		}
		else if ( token.startsWith( "-" ) )
		{
			throw new ArgHelper.IllegalOptionException( "illegal option: " + token );
		}
		else
		{
			// it's our first operand
			ps.setDone();
		}
	}
	
	
		public
	ArgHelperImpl(
		final ListIterator			tokens,
		final ArgHelper.OptionsInfo	optionInfo )
			throws ArgHelper.IllegalOptionException
	{
		final ParseState	ps	= new ParseState( tokens, optionInfo );
		
		// put all the options into one list
		while( ! ps.done() )
		{
			if ( optionInfo.isLegalOption( ps.peekToken() ) )
			{
				handleOption( ps );
			}
			else
			{
				handleNonOption( ps );
			}
		}
		mOptions	= ps.getParsedOptions();
		
		mOperands	= ps.getOperands();
		
		//p( "options: " + SmartStringifier.DEFAULT.stringify( mOptions ) );
		//p( "operands: " + SmartStringifier.DEFAULT.stringify( mOperands ) );
	}
	
	
	
		private ParsedOption
	findOption( final String name  )
	{
		ParsedOption	result	= null;
		
		for( int i =0; i < mOptions.length; ++i )
		{
			if ( mOptions[ i ].mName.startsWith( name ) )
			{
				result	= mOptions[ i ];
				break;
			}
		}
		return( result );
	}
	
	
	/*
		Return the values associated with the option
	 */
		public String []
	getArgValues( final String name )
		 throws ArgHelper.IllegalOptionException
	{
		String []	results	= null;
		
		String	prefixedName	= name;
		// allow name to be either with or without leading "--" or "-"
		// a single-letter name is assumed to be a single dash
		if ( ! name.startsWith( "-" ) )
		{
			prefixedName	= ArgHelperOptionsInfo.OptionDesc.mapName( name );
		}
		
		
		final ParsedOption	option	= findOption( prefixedName );
		if ( option != null )
		{
			results	= option.mValues;
		}
		
		return( results );
	}
	
		public String
	getArgValue( final String name )
		throws ArgHelper.IllegalOptionException
	{
		String		value	= null;
		
		final String []	values	= getArgValues( name );
		if ( values != null )
		{
			if ( values.length != 1 )
			{
				throw new ArgHelper.IllegalOptionException( "option has more than one value: " + values.length );
			}
			
			value	= values[ 0 ];
			assert( value != null );
		}
		
		return( value );
	}
	
	
		public int
	countOptions()
	{
		return( mOptions.length );
	}
	
	
		public String
	getString( final String name, final String defaultValue)
		throws ArgHelper.IllegalOptionException
	{
		String	result	= getArgValue( name  );
		if ( result == null )
		{
			result	= defaultValue;
		}
		
		return( result );
	}
	
	
		public Integer
	getInteger( final String name )
		throws ArgHelper.IllegalOptionException
	{
		final String	value	= getArgValue( name );
		Integer			result	= null;
		
		if ( value != null )
		{
			result	= Integer.valueOf(value);
		}
		
		return( result );
	}
	
		public Boolean
	getBoolean( final String name, final Boolean defaultValue)
		throws ArgHelper.IllegalOptionException
	{
		final String	value	= getArgValue( name );
		
		Boolean			result	= null;
		
		if ( value == null )
		{
			// not found
			result	= defaultValue;
		}
		else
		{
			// the --name=value form
			result	= Boolean.valueOf(value);
		}
		
		return( result );
	}
	
		public String []
	getOperands( )
	{
		return( mOperands );
	}
}

