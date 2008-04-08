/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OptionInfoCLIPValidator.java,v 1.3 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:19 $
 */
package com.sun.cli.jcmd.util.cmd;


public class OptionInfoCLIPValidator
	implements OptionInfoValidator
{
	public static final OptionInfoCLIPValidator	INSTANCE	=
				new OptionInfoCLIPValidator();
	
	/**
		Set of legal characters that may be used in an option name.
	 */
	public static final String	LEGAL_ANY_OPTION_CHARS	=
		"abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789";
		
	public static final String	LEGAL_LONG_OPTION_CHARS	=
		LEGAL_ANY_OPTION_CHARS + 
		"-._";	// any others?
		
	public static final String	LEGAL_SHORT_OPTION_CHARS	=
		LEGAL_ANY_OPTION_CHARS + 
		"?";
		
		protected void
	OptionInfoCLIPValidator()
	{
		// should use INSTANCE or subclass
	}
	
	/**
		Validate the option, ensuring it is CLIP-compliant
	 */
		public void
	validateOption( OptionInfo optionInfo )
		throws IllegalOptionException
	{
		// verify that both long and short names exist
		final String	longName	= optionInfo.getLongName();
		final String	shortName	= optionInfo.getShortName();
		
		validateLongName( longName );
		validateShortName( shortName );
		
		// check for single-character long option name; it must be the same
		// character as the short option
		final int	firstNameCharIdx	= OptionInfo.LONG_OPTION_PREFIX.length();
		if ( longName.length() == 1 + firstNameCharIdx )
		{
			final int	shortIdx	= OptionInfo.SHORT_OPTION_PREFIX.length();
			
			if ( longName.charAt( firstNameCharIdx ) !=
				shortName.charAt( shortIdx ) )
			{
				throw new IllegalOptionException( 
					"single-character long option must have same character as short option '" +
					shortName.charAt( shortIdx ) + "'" );
			}
		}
	}
	
	/**
		Verify that the option name consists of legal characters
	 */
		void
	validateNameChars( String name, int startIndex )
		throws IllegalOptionException
	{
		final int		length	= name.length();
		final boolean	isLongOption	= name.startsWith( OptionInfo.LONG_OPTION_PREFIX );
		
		final String legalChars	= isLongOption ?
						LEGAL_LONG_OPTION_CHARS : LEGAL_SHORT_OPTION_CHARS;
		
		for( int i = startIndex; i < length; ++i )
		{
			final char	theChar	= name.charAt( i );
			
			if ( legalChars.indexOf( theChar ) < 0 )
			{
				throw new IllegalOptionException(
						"invalid character in option name: " +
						"'" + theChar + "'" );
			}
		}
	}
	
	
	/**
		Verify that the option name is a legal long option name
	 */
		void
	validateLongName( String name )
		throws IllegalOptionException
	{
		if ( name == null || name.length() <= OptionInfo.LONG_OPTION_PREFIX.length() )
		{
			throw new IllegalOptionException(
				"long option name must have at least one character: " + name );
		}
		
		if ( ! name.startsWith( OptionInfo.LONG_OPTION_PREFIX ) )
		{
			throw new IllegalOptionException( "invalid long option name: " + name );
		}
		validateNameChars( name, 2 );
	}
	
	/**
		Verify that the option name is a legal short option name
	 */
		void
	validateShortName( String name )
		throws IllegalOptionException
	{
		if (  name == null ||
			name.length() != 1 + OptionInfo.SHORT_OPTION_PREFIX.length() )
		{
			throw new IllegalOptionException(
				"short option name must be exactly one character: " + name );
		}
		
		if ( ! name.startsWith( OptionInfo.SHORT_OPTION_PREFIX ) )
		{
			throw new IllegalOptionException( "invalid short option name: " + name );
		}
		
		if ( name.startsWith( OptionInfo.LONG_OPTION_PREFIX ) )
		{
			throw new IllegalOptionException( "invalid short option name: " + name );
		}
		validateNameChars( name, 1);
	}
	
};




