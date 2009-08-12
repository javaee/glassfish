/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OptionsInfoImpl.java,v 1.7 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2005/11/08 22:39:19 $
 */
package com.sun.cli.jcmd.util.cmd;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class OptionsInfoImpl implements OptionsInfo
{
	final List<OptionInfo>		mOptionDescriptions;
	final OptionInfoValidator	mValidator;
	
	public final static OptionsInfoImpl	NONE	= new OptionsInfoImpl();
	
	/**
		Instantiate with an empty list of options, default validation.
	 */
		public
	OptionsInfoImpl(  )
	{
		mOptionDescriptions	= new ArrayList<OptionInfo>();
		mValidator			= OptionInfoCLIPValidator.INSTANCE;
	}
	
	/**
		Instantiate from another impl, copying its options data.
		
		@param src			src of existing options info
		@param validator	validator
	 */
		public
	OptionsInfoImpl(
		OptionsInfo			src,
		OptionInfoValidator	validator)
	{
		this( src.getOptionInfos(), validator );
	}
	
	/**
		Instantiate with an empty list of options, specified validator.
		
		@param validator	the validator for options
	 */
		public
	OptionsInfoImpl( OptionInfoValidator validator )
	{
		mOptionDescriptions	= new ArrayList<OptionInfo>();
		mValidator			= validator;
	}
	
	/**
		Instantiate with a list of options as given by a String. See
		addOptions() for a description of the format of the String. Use
		default validation.
		
		@param optionsString	 a String describing the available options
	 */
		public
	OptionsInfoImpl( String	optionsString )
		throws IllegalOptionException
	{
		this( optionsString, OptionInfoCLIPValidator.INSTANCE);
	}
	
	/**
		Instantiate with a list of options
		
		@param options	 	array of options
		@param validator	the validator for options
	 */
		public
	OptionsInfoImpl(
		OptionInfo[]		options,
		OptionInfoValidator	validator )
	{
		mOptionDescriptions	= new ArrayList<OptionInfo>();
		
		if ( options != null )
		{
			for( int i = 0; i < options.length; ++i )
			{
				mOptionDescriptions.add( options[ i ] );
			}
		}
		
		mValidator			= validator;
	}
	
	/**
		Instantiate with a list of options
		
		@param options	 	array of options
		@param validator	the validator for options
	 */
		public
	OptionsInfoImpl(
		List<OptionInfo>	options,
		OptionInfoValidator	validator )
	{
		this( (OptionInfo[])options.toArray( new OptionInfo[ options.size() ]), validator );
	}
	
	/**
		Instantiate with a list of options
		
		@param options	 	array of options
	 */
		public
	OptionsInfoImpl(
		OptionInfo[]			options)
	{
		this( options, OptionInfoCLIPValidator.INSTANCE );
	}
	
	/**
		Instantiate with a list of options as given by a String. See
		addOptions() for a description of the format of the String. 
		
		@param optionsString	a String describing the available options
		@param validator		the validator for options
	 */
		public
	OptionsInfoImpl(
		String				optionsString,
		OptionInfoValidator	validator )
		throws IllegalOptionException
	{
		this( validator );
		
		addOptions( optionsString );
	}
	
	/**
	 */
		public String
	tokenToOptionName( String token )
	{
		final int	delimIndex	= token.indexOf( '=' );
		
		String	name	= token;
		
		if ( delimIndex > 0 )
		{
			name	= token.substring( 0, delimIndex );
		}
		
		return( name );
	}
	
	/**
	 */
		public String
	tokenToOptionData( String token )
	{
		final int	delimIndex	= token.indexOf( '=' );
		
		String	data	= null;	// return null if no data eg "--foo"
		
		if ( delimIndex > 0 )
		{
			// note: form "--foo=" is valid and should result in an empty string
			data	= token.substring( delimIndex + 1, token.length() );
		}
		
		return( data );
	}
	
	
	
		public List<OptionInfo>
	getOptionInfos()
	{
		return( Collections.unmodifiableList( mOptionDescriptions ) );
	}
	
		public OptionInfo
	getOptionInfo( String name )
	{
		return( findInfo( name ) );
	}
	
	
	/**
		Lookup an option description corresponding to the token.
	 */
		protected OptionInfo
	findInfo( String token )
	{
		OptionInfo		info	= null;
		
		final String	optionName	= tokenToOptionName( ensurePrefix( token ) );
		
		final Iterator iter	= mOptionDescriptions.iterator();
		while( iter.hasNext() )
		{
			final OptionInfo	optionDesc	= (OptionInfo)iter.next();
			
			if ( optionDesc.matches( optionName ) )
			{
				info	= optionDesc;
				break;
			}
		}
		return( info );
	}
	
	/**
	 */
		public boolean
	isLegalOption( String token )
	{
		final boolean	isLegal	= token.startsWith( OptionInfo.SHORT_OPTION_PREFIX ) &&
										(findInfo( token ) != null);
		
		return( isLegal );
	}
	
	/**
		Throw an IllegalArgumentException if the token contains an illegal option.
	 */
		void
	checkLegalOption( String token )
	{
		if ( ! isLegalOption( token ) )
		{
			throw new IllegalArgumentException( "illegal option: " + token );
		}
	}
	
	/**
	 */
		public int
	getNumValues( String token )
	{
		checkLegalOption( token );
		
		final OptionInfo	info	= findInfo( token );
		
		return( info.getNumValues() );
	}
	
	/**
	 */
		public boolean
	isBoolean( String token )
	{
		checkLegalOption( token );
		
		final OptionInfo	info	= findInfo( token );
		
		return( info.isBoolean() );
	}
	
	/**
		Throw an IllegalOptionException
	 */
		protected void
	foundIllegalOption( String token  )
		throws IllegalOptionException
	{
		throw new IllegalOptionException( "illegal option: " + token );
	}
	
	
		private void
	checkAlreadyExists( String name )
	{
		if ( name != null && findInfo( name ) != null )
		{
			throw new IllegalArgumentException( "option already used: " + name );
		}
	}
	
		private void
	add( String longName, String shortName, String[] valueNames, boolean required )
		throws IllegalOptionException
	{
		add( longName, shortName, valueNames, new String[0], required );
	}
	
	
		private void
	add( String longName, String shortName, String[] valueNames, final String[] synonyms, boolean required )
		throws IllegalOptionException
	{
		checkAlreadyExists( longName );
		checkAlreadyExists( shortName );
		
		final OptionInfo	optionInfo	=
				new OptionInfoImpl( longName, shortName, valueNames, synonyms, required );
				
		mValidator.validateOption( optionInfo );
		
		mOptionDescriptions.add( optionInfo );
	}
	
	/**
		Add a Boolean option to the available legal options.
		
		The name must begin with "-" or "--".  Single-letter options should
		contain a single dash, and multiple-letter options should contain
		a double-dash for CLIP-compliance.
		
		@param longName		long name of the option
		@param shortName	short name of the option
		@param required		true if required option, false otherwise
		@throws IllegalOptionException
	 */
		public void
	addBoolean( String longName, String shortName, boolean required )
		throws IllegalOptionException
	{
		add( longName, shortName, null, required );
	}
	
	
	/**
		Add a non-Boolean option with the specified number of values, >= 1.
		
		The name must begin with "-" or "--".  Single-letter options should
		contain a single dash, and multiple-letter options should contain
		a double-dash for CLIP-compliance.
		
		@param longName		long name of the option
		@param shortName	short name of the option
		@param valueNames	names of values
		@param required		true if required option, false otherwise
		@throws IllegalOptionException
	 */
		public void
	addNonBoolean( String longName, String shortName, String[] valueNames, boolean required)
		throws IllegalOptionException
	{
		add( longName, shortName, valueNames, required );
	}
	
	/**
		Character signifying that the option is required.
	 */
	public final static char	REQUIRED_CHAR	= '+';
	
	/**
		Delimiter between option descriptions
	 */
	public final static char	OPTION_DELIM	= ' ';
	
	/**
		Delimiter between option names and the number of values
	 */
	public final static char	NUMVALUES_DELIM	= ',';
	
	/**
		Delimiter between the long option name and the short option name
	 */
	public final static char	OPTION_NAME_DELIM	= ':';
	
	
	/**
		Add all the option descriptions found in the specified String.  Each
		option description is delimited by a space from the others.
		
		The format of an option description is as follows:
		<p><code>
		[+]long-option-name:short-option-name[:synonym]*[,<num-values>]
		<p></code>
		The optional leading '+' signifies the option is required.
		<p>
		Example:
		"+count:c,1 help:H:? verbose:v"
		<p>
		This is interpreted as follows:
		<p>
		--count|-c 		takes a single value *and is a required option*
		--help|-H|-?	is a boolean option with synonym "-?"
		--verbose|-v	is a boolean option
		<p>
		<p>
		The form:
		<p><code>
		:c,1
		</code>
		<p>
		is interpreted to mean that there is only the short form "-c".
		The form:
		<p><code>
		count:,1 
		</code>
		<p>
		are interpreted to mean that there is a "--count" option, and no short-form equivalent.
		This form may be rejected, depending on the Validator being used.
		<p>
		Any option may be prefixed with its appropriate prefix ("-" or "--").  To specify
		a long option consisting of a single character, you must use the "--x" form.  By default
		an option is assumed to be a short option if it's name is a single character.
		
		@param listIn			String containing list of options to add
		@throws IllegalOptionException
	 */
		public void
	addOptions( final String listIn )
		throws IllegalOptionException
	{
		String	list	= listIn.trim();
		
		final String []	names	= list.split( "" + OPTION_DELIM );
		
		if ( names.length == 1 && names[ 0 ].length() == 0 )
		{
			return;
		}
		
		for( int i = 0; i < names.length; ++i )
		{
			final boolean	required	= names[ i ].startsWith( "" + REQUIRED_CHAR );
			String	remainder	= names[ i ];
			if ( required )
			{
				remainder	= remainder.substring( 1, remainder.length() );
			}
			
			final String []	data	= remainder.split( "" + NUMVALUES_DELIM );
		
			final String [] forms	= data[ 0 ].split( "" + OPTION_NAME_DELIM );
			if ( forms.length < 2 )
			{
				throw new IllegalArgumentException( "Illegal option specification: " + names[ i ] );
			}
			
			final String	longForm	= ensurePrefix( forms[ 0 ] );
			final String	shortForm	= ensurePrefix( forms[ 1 ] );
			
			// are there synonyms?
			final List<String>	synonyms	= new ArrayList<String>();
			for ( int s = 2; s < forms.length; ++s )
			{
				final String	synonym	= forms[ s ];
				
				// must be non-empty, and neither the short or long form
				if ( synonym.length() != 0 &&
					( ! ensurePrefix( synonym ).equals( longForm ) ) &&
					( ! ensurePrefix( synonym ).equals( shortForm ) ) )
				{
					synonyms.add( forms[ s ] );
				}
			}
			final String[]	synonymsArray	= new String[ synonyms.size() ];
			synonyms.toArray( synonymsArray );
			
			int	numValues	= 0;
			String[]	valueNames	= null;
			if ( data.length > 1 )
			{
				valueNames	= new String[ data.length - 1 ];
				for( int valueNameIndex = 1; valueNameIndex < data.length; ++valueNameIndex )
				{
					valueNames[ valueNameIndex - 1 ]	= data[ valueNameIndex ];
				}
			}
			add( longForm, shortForm, valueNames, synonymsArray, required );
		}
	}
	
	public final static char	REQUIRED_FLAG	= '+';
	public final static char	DISALLOWED_FLAG	= '!';
	
	/**
		Add a dependency for an option.  The option names must be in a list
		delimited by OPTION_NAME_DELIM, with the first option being the dependent
		option and subsequent ones being dependees.  Either the long or short option
		names may be used.
		
		@param optionName	any legal name for the option
		@param dependency 	a String denoting the dependency
	 */
		public void
	addDependency( String optionName, OptionDependency dependency )
		throws IllegalOptionException
	{
		final OptionInfo	dependeeInfo	= findInfo( optionName );
		
		dependeeInfo.addDependency( dependency );
	}
	
	/**
		Add a dependency for an option.  The option names must be in a list
		delimited by OPTION_NAME_DELIM, with the first option being the dependent
		option and subsequent ones being dependees.  Either the long or short option
		names may be used.
		
		@param dependencyString 	a String denoting the dependency
	 */
		public void
	addDependency( String dependencyString )
		throws IllegalOptionException
	{
		final String []	names	= dependencyString.trim().split( "" + OPTION_NAME_DELIM );
		
		if ( names.length >= 2 )
		{
			final String	dependeeName	= names[ 0 ];
			
			final OptionInfo	dependeeInfo	= findInfo( dependeeName );
			if ( dependeeInfo == null )
			{
				throw new IllegalOptionException( "Option: " + dependeeName + " does not exist" );
			}
			
			for( int i = 1; i < names.length; ++i )
			{
				String	name	= names[ i ];
				
				final char	prefixChar	= name.charAt( 0 );
				name	= name.substring( 1, name.length() );
				final OptionInfo	info	= findInfo( name );
				
				OptionDependency	dependency	= null;
				if ( prefixChar == REQUIRED_FLAG  )
				{
					dependency	= new RequiredOptionDependency( info );
				}
				else if ( prefixChar == DISALLOWED_FLAG )
				{
					dependency	= new DisallowedOptionDependency( info );
				}
				else
				{
					throw new IllegalOptionException( "Illegal dependency specification: " + name );
				}
			
				dependeeInfo.addDependency( dependency  );
			}
		}
	}
	
	/**
		Add 0 or more dependencies to the options.
		
		@param dependenciesString 	a String denoting the dependencies
	 */
		public void
	addDependencies( String dependenciesString )
		throws IllegalOptionException
	{
		final String []	dependencies	= dependenciesString.trim().split( "" + OPTION_DELIM );
		
		for( int i = 0; i < dependencies.length; ++i )
		{
			addDependency( dependencies[ i ] );
		}
	}
	
		
		static String
	ensurePrefix( String name )
	{
		if ( name.length() == 0 )
			return( null );
			
		return( OptionInfoImpl.ensurePrefix( name ) );
	}
	
	
		public String
	toString()
	{
		String	result	= "";
		
		if ( mOptionDescriptions.size() != 0 )
		{
			final Iterator		iter	= mOptionDescriptions.iterator();
			final StringBuffer	buf	= new StringBuffer();
		
			final String	SEPARATOR	= " ";
			while ( iter.hasNext() )
			{
				buf.append( ((OptionInfo)iter.next()).toString() );
				buf.append( SEPARATOR );
			}
			buf.setLength( buf.length() - SEPARATOR.length() );
			result	= buf.toString();
		}
		
		return( result );
	}
}




