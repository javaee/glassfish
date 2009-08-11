/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/ArgHelperImpl.java,v 1.9 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;


import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.ListIterator;

import com.sun.cli.jcmd.util.misc.TokenizerImpl;
import com.sun.cli.jcmd.util.misc.TokenizerParams;
import com.sun.cli.jcmd.util.misc.TokenizerException;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;


/**
	Implements ArgHelper in a SUSv3/CLIP-compliant fashion.
	<p>
	Supports multiple values for an option as a <i>comma-separated list</i>. For example,
	if the option "--foo" is described as having 3 values, then:
	<p>
	<code>--foo=1,2,3</code>
	will be parsed as "--foo" with 3 values {1,2,3} and can be fetched via getArgValues().
	If the values themselves contain a ',' character, it may be escaped via the '\'
	character.The use of a space as a delimiter is not supported.
	<p>
	
 */
public final class ArgHelperImpl implements ArgHelper
{
	final ParsedOption []		mOptions;
	final String []				mOperands;
	final OptionsInfo	mOptionsInfo;
	
	
	
		static String
	ensurePrefix( String name )
	{
		if ( name.length() == 0 )
			return( null );
			
		return( OptionInfoImpl.ensurePrefix( name ) );
	}

		private static void
	numValuesFailure( final String optionName, final int numValues )
		throws IllegalOptionException
	{
		throw new IllegalOptionException( "option \"" + optionName +
			"\" requires " + numValues + " values" );
	}
	
	
		private static void
	booleanExpectedFailure( final String optionName )
		throws IllegalOptionException
	{
		throw new IllegalOptionException( "option \"" + optionName +
			"\" is a boolean option but has value(s)" );
	}
	
	
	/**
		Extract the requisite number of values from the String. If the requested number
		is 1, then do not interpret the value at all.
	 */
		private static String []
	extractValues(
		final String	optionName,
		final String	valueList,
		final int		numValuesExpected )
		throws IllegalOptionException, TokenizerException
	{
		String[]	tokens	= null;
		
		if ( numValuesExpected == 1 )
		{
			tokens	= new String[ 1 ];
			tokens[ 0 ]	= valueList;
		}
		else
		{
			final TokenizerParams	params	= new TokenizerParams();
			params.mDelimiters					= "" + MULTI_VALUE_DELIM;
			params.mMultipleDelimsCountAsOne	= false;
			params.ensureDelimitersEscapable();
			
			final TokenizerImpl tk	= new TokenizerImpl( valueList, params);
			
			tokens	= tk.getTokens();
			
			if ( tokens.length != numValuesExpected )
			{
				numValuesFailure( optionName, numValuesExpected );
			}
		}

		return( tokens );
	}
	
	// values are in then tokens
		private static String []
	extractValues( final ParseState ps, final String optionName, final int numValuesExpected )
		throws IllegalOptionException, TokenizerException
	{
		String	nextToken	= null;
		
		try
		{
			nextToken	= ps.nextToken();
		}
		catch( Exception e )
		{
			throw new IllegalOptionException( "Option: " + optionName + " requires a value" );
		}
		
		return( extractValues( optionName, nextToken, numValuesExpected ));
	}
	
		private static String
	extractBoolean( final String optionName, final String value )
		throws IllegalOptionException
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
	
		private static void
	handleOption( final ParseState ps )
		throws IllegalOptionException, TokenizerException
	{
		final String				token	= ps.nextToken();
		final OptionsInfo	optionsInfo	= ps.mOptionInfo;
		
		final String	optionName	= optionsInfo.tokenToOptionName( token );
		final String	optionExtra	= optionsInfo.tokenToOptionData( token );
		
		// short option names are not allowed to use "-x=value" form (SUSv3/CLIP 6)
		if ( isShortOptionName( optionName ) && optionExtra != null )
		{
			throw new IllegalOptionException(
				"Form -x=value not allowed for short options (" +  token + ")" );
		}
		
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
				// values follow
				values	= extractValues( optionName, optionExtra, numValues );
			}
			else
			{
				// values are in token list
				values	= extractValues( ps, optionName, numValues );
			}
		}
		
		final String	name = ensurePrefix( optionName );
		
		ps.mOptions.add( new ParsedOption( name, values ) );
	}
	
	/**
		Construct a new ArgHelperImpl using the specified tokens and option info.
		<p>
		The caller must first break up the command line into tokens.  If a subcommand
		is present in the command line, it should be omitted; only the portions of
		the command line following command or subcommand should be provided within
		'tokens'.
		<p>
		All parsing is done within this constructor.  Successfully instantation
		means that the tokens were successfully parsed according to the information
		provided by 'optionInfo'.
		
		@param tokens		tokens to parse
		@param optionsInfo	an OptionsInfo implementation
	 */
		public
	ArgHelperImpl(
		final ListIterator<String>	tokens,
		final OptionsInfo	optionsInfo )
			throws IllegalOptionException, TokenizerException
	{
		final ParseState	ps	= new ParseState( tokens, optionsInfo );
		
		mOptionsInfo	= optionsInfo;
		
		// put all the options into one list
		while( ! ps.done() )
		{
			final String	token	= ps.peekToken();
			
			final boolean	isOperand				= ! token.startsWith( OPTION_PREFIX );
			final boolean	isEndOfOptionsMarker	= (! isOperand) &&  token.equals( END_OPTIONS );
			if ( isEndOfOptionsMarker || isOperand )
			{
				// beginning of operands..
				if ( isEndOfOptionsMarker )
				{
					ps.nextToken();	// eat the marker	
				}
				
				ps.setDone();
				break;
			}
			
			// it's an option
			if ( optionsInfo.isLegalOption( token ) )
			{
				handleOption( ps );
			}
			else if ( isGroupedBooleanOption( token ) )
			{
				handleGroupedBooleans( optionsInfo, ps );
			}
			else
			{
				throw new IllegalOptionException( "Illegal option: " + token );
			}
		}
		mOptions	= ps.getParsedOptions();
		
		mOperands	= ps.getOperands();
	}
	
	static private final String []	TRUE_VALUE	= new String [] { "true" };
		
		private ParsedOption []
	groupedBooleansToParsedOptions( final String token )
	{
		assert( isGroupedBooleanOption( token ) );
		
		// strip leading "-"
		final String	booleans	= token.substring( 1, token.length() );
		final int length	= booleans.length();
		assert( length >= 2 );
		
		final ParsedOption[]	options	= new ParsedOption[ length ];
		for( int i = 0; i < length; ++i )
		{
			final String	boolName	= "" + OPTION_PREFIX + booleans.charAt(i );
			
			options[ i ]	= new ParsedOption( boolName, TRUE_VALUE );
		}
		
		return( options );
	}
	
		void
	handleGroupedBooleans(
		final OptionsInfo	optionInfo,
		final ParseState			ps )
		throws IllegalOptionException
	{
		final String	token	= ps.nextToken();
		
		final ParsedOption[]	options	= groupedBooleansToParsedOptions( token );
		
		for( int i = 0; i < options.length; ++i )
		{
			final ParsedOption	option	= options[ i ];
			
			if ( ! optionInfo.isLegalOption( option.getName() ) )
			{
				throw new IllegalOptionException(
					"Illegal boolean option: " + option.getName() + " found in: " + token );
			}
			
			ps.mOptions.add( option );
		}
	}
	
	/*
		Grouped booleans must:
			- start with "-" 
			- not start with "--"
			- have a length >= 3 ("-" + at
			- have no values
	 */
		boolean
	isGroupedBooleanOption( String name )
	{
		final int	minLength	= 1 + 1 + 1;	// "-" + at least two chars
		
		return( ! name.startsWith( LONG_OPTION_PREFIX ) &&
				name.startsWith( SHORT_OPTION_PREFIX ) &&
				name.length() >= 3 );
	}
	
		Boolean
	findGroupedBoolean(
		String		name,
		Boolean		defaultValue )
	{
		Boolean	result	= defaultValue;
		
		assert( isShortOptionName( name ) );
		final char	optionLetter	= ensurePrefix( name ).charAt( 1 );
		
		/*
			Look through all options for grouped boolean and see if any
			contain the one in question

			Must be interpreted in order specified--SUS/CLIP11
		 */
		for( int i = 0; i < mOptions.length; ++i )
		{
			final ParsedOption	option			= mOptions[ i ];
			final String		candidateName	= option.getName();
			
			if ( option.getNumValues() == 0 &&
				isGroupedBooleanOption( candidateName ) )
			{
				// does the group of letters contain this option?
				if ( candidateName.indexOf( "" + optionLetter ) >= 1)
				{
					// if specified, it's true
					result	= Boolean.TRUE;
					break;
				}
			}
		}
		
		return( result );
	}
	
		
	/**
	 */
		public int
	countOptionInstances( String name )
		 throws IllegalOptionException
	{
		return( getOptionInstancesList( name ).size() );
	}
	
	/**
	 */
		private List<ParsedOption>
	getOptionInstancesList( String name )
		 throws IllegalOptionException
	{
		final String	prefixedName	= ensurePrefix( name );
		if ( ! mOptionsInfo.isLegalOption( prefixedName ) )
		{
			throw new IllegalOptionException( "request for non-existent option: " + prefixedName );
		}
		
		final OptionInfo info	= mOptionsInfo.getOptionInfo( name );
		if ( info == null )
		{
			throw new IllegalOptionException( "Option does not exist: " + prefixedName );
		}
		
		final List<ParsedOption>	list	= new ArrayList<ParsedOption>();
		for( int i = 0; i < mOptions.length; ++i )
		{
			if ( info.matches( mOptions[ i ].getName() ) )
			{
				list.add( mOptions[ i ] );
			}
		}
		
		return( list );
	}
	
	/**
	 */
		public ParsedOption[]
	getOptionInstances( String name )
		 throws IllegalOptionException
	{
		final List<ParsedOption>	list	= getOptionInstancesList( name );
		
		final ParsedOption[]	instances	= new ParsedOption[ list.size() ];
		list.toArray( instances );
		
		return( instances );
	}
	
	/**
	 */
		public OptionInfo[]
	getMissingOptions( )
	{
		final Set<OptionInfo>	missingSet	= new HashSet<OptionInfo>();
		
		for( final OptionInfo info : mOptionsInfo.getOptionInfos() )
		{
			if ( info.isRequired() )
			{
				try
				{
					if ( getOptionValues( info.getShortName() ) == null )
					{
						missingSet.add( info );
					}
				}
				catch( IllegalOptionException e )
				{
					// shouldn't be possible; info must contain legal options
					assert( false );
				}
			}
		}
		
		final OptionInfo[]	missingArray = new OptionInfo[ missingSet.size() ];
		missingSet.toArray( missingArray );
		
		return( missingArray );
	}
	
	class OptionInfoStringifier implements Stringifier
	{
		OptionInfoStringifier()	{}
		
			public String
		stringify( Object o )
		{
			return( ((OptionInfo)o).getLongName() );
		}
	}
	
		public void
	checkMissing( )
		throws IllegalOptionException
	{
		OptionInfo[]	missing	= getMissingOptions();
		
		if ( missing.length != 0 )
		{
			throw new IllegalOptionException( "The following required options are missing: " +
				ArrayStringifier.stringify( missing, ", ", new OptionInfoStringifier()) );
		}
	}
	
	
		public void
	checkDependencies( )
		throws IllegalOptionException
	{
		final ParsedOption[]	options	= mOptions;
		
		for( int i = 0; i < options.length; ++i )
		{
			final ParsedOption			option	= options[ i ];
			final OptionInfo	info	= mOptionsInfo.getOptionInfo( option.getName() );
			
			for( final OptionDependency	dependency : info.getDependencies() )
			{
				if ( dependency instanceof RequiredOptionDependency )
				{
					checkRequiredDependency( option, dependency.getRefs() );
				}
				else if ( dependency instanceof DisallowedOptionDependency )
				{
					checkDisallowedDependency( option, dependency.getRefs() );
				}
				else if ( dependency instanceof OperandsOptionDependency )
				{
					final int	numOperands	= getOperands().length;
					final OperandsOptionDependency		d	= (OperandsOptionDependency)dependency;
					
					final int	min	= d.getMin();
					final int	max	= d.getMax();
					
					if ( numOperands < min || numOperands > max )
					{
						if ( max == 0 )
						{
							throw new IllegalOptionException( "Option " + option.getName() + 
								" cannot accept operands");
						}
						else
						{
							throw new IllegalOptionException( "Option " + option.getName() + 
								"requires between " + min + " and " + max + " operands." );
						}
					}
				}
			}
		}
	}
	
	
		public void
	checkRequiredDependency( ParsedOption option, OptionInfo[]	requiredOptions )
		throws IllegalOptionException
	{
		for( int i = 0; i < requiredOptions.length; ++i )
		{
			if ( getOptionInstances( requiredOptions[ i ].getShortName() ).length == 0 )
			{
				throw new IllegalOptionException( "Option " + option.getName() +
					" requires option: " + requiredOptions[ i ].getLongName() );
			}
		}
	}
	
		public void
	checkDisallowedDependency( ParsedOption option, OptionInfo[]	disallowedOptions )
		throws IllegalOptionException
	{
		for( int i = 0; i < disallowedOptions.length; ++i )
		{
			if ( getOptionInstances( disallowedOptions[ i ].getShortName() ).length != 0 )
			{
				throw new IllegalOptionException( "Option " + option.getName() +
					" disallows option: " + disallowedOptions[ i ].getLongName() );
			}
		}
	}
	
		public void
	checkRequirements( )
		throws IllegalOptionException
	{
		checkMissing();
		
		checkDependencies();
	}
	

	/**
	 */
		public ParsedOption
	getOption( final String name )
		throws IllegalOptionException
	{
		final ParsedOption[]	options	= getOptionInstances( name );
		ParsedOption	last	= null;
		
		if ( options.length >= 1 )
		{
			last	= options[ options.length - 1 ];
		}
		
		return( last );
	}

		public String
	getOptionValue( final String name )
		throws IllegalOptionException
	{
		final ParsedOption	option	= getOption( name );
		
		final String	value	= option == null ? null : option.getValue();
		
		return( value );
	}
	
		public String[]
	getOptionValues( final String name )
		throws IllegalOptionException
	{
		final ParsedOption	option	= getOption( name );
		
		final String[]	values	= option == null ? null : option.getValues();
		
		return( values );
	}
	
	
	/**
	 */
		public int
	countOptions()
	{
		return( mOptions.length );
	}

	
	/**
	 */
		public String
	getStringValue( final String name, final String defaultValue)
		throws IllegalOptionException
	{
		String	result	= getOptionValue( name  );
		if ( result == null )
		{
			result	= defaultValue;
		}
		
		return( result );
	}
	
	
	/**
	 */
		public Integer
	getIntegerValue( final String name, final Integer defaultValue )
		throws IllegalOptionException
	{
		final String	value	= getOptionValue( name );
		Integer			result	= defaultValue;
		
		if ( value != null )
		{
			result	= new Integer( value );
		}
		
		return( result );
	}
	
	
		public static boolean
	isShortOptionName( String name )
	{
		boolean			isShortOption	= false;
		final String	prefixedName	= ensurePrefix( name );
		
		if ( prefixedName.length() == 2 &&
			prefixedName.charAt( 0 ) == '-' &&
			prefixedName.charAt( 1 ) != '-' )
		{
			isShortOption	= true;
		}
		
		return( isShortOption );
	}
	
	
	/**
	 */
		public Boolean
	getBooleanValue( final String name, final Boolean defaultValue)
		throws IllegalOptionException
	{
	
		final String	value	= getOptionValue( name );
		
		Boolean			result	= null;
		
		if ( value == null )
		{
			// not found
			result	= defaultValue == null ? Boolean.FALSE : defaultValue;
			
			if ( isShortOptionName( name ) )
			{
				result	= findGroupedBoolean( name, defaultValue);
			}
		}
		else
		{
			// the --name=value form
			result	= new Boolean( value );
		}
		
		return( result );
	}
	
	/**
	 */
		public String []
	getOperands( )
	{
		return( mOperands );
	}
}




/**
	Used internally to maintain the current parsing state.
 */
final class ParseState
{
	final ListIterator<String>	mTokens;
	final List<ParsedOption>	mOptions;
	boolean			mDone;
	
	final OptionsInfo mOptionInfo;
	
	ParseState(
		final ListIterator<String> tokens,
		final OptionsInfo	optionInfo)
	{
		assert( tokens != null );
		
		mTokens				= tokens;
		mOptions			= new ArrayList<ParsedOption>();
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
		final List<String>	list	= new ArrayList<String>();
		
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
		final String	result	= mTokens.next();
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
	

