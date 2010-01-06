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




