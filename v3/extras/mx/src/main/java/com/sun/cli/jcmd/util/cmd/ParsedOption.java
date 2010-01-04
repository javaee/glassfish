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

/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/ParsedOption.java,v 1.4 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:19 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import org.glassfish.admin.amx.util.ArrayUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


/**
	Retains the value(s) of a parsed option and provides accessor methods
	to get its name and values.
 */
public final class ParsedOption
{
	private final String		mName;
	private final String[]		mValues;
	
	/**
	 */
	ParsedOption( String name, String [] values )
	{
		if ( ! name.startsWith( OptionInfo.SHORT_OPTION_PREFIX ) )
		{
			throw new IllegalArgumentException( "Option name must start with '-'" );
		}
		
		mName	= name;
		mValues	= values;
	}
	
	/**
		Strip the leading "-" or "--" prefix.
	 */
		private String
	trimPrefix( String name )
	{
		String	prefix	= "";
		
		if ( name.startsWith( OptionInfo.LONG_OPTION_PREFIX ) )
		{
			prefix	= OptionInfo.LONG_OPTION_PREFIX;
		}
		else if ( name.startsWith( OptionInfo.SHORT_OPTION_PREFIX ) )
		{
			prefix	= OptionInfo.SHORT_OPTION_PREFIX;
		}
		
		final String	strippedName	= name.substring( prefix.length(), name.length() );
		
		return( strippedName );
	}
	
	/**
		Get the name, but without the leading "-" or "--"
	 */
		public String
	getNoPrefixName()
	{
		return( trimPrefix( mName ) );
	}
	
	/**
		Get the name, including the leading "-" or "--"
	 */
		public String
	getName()
	{
		return( mName );
	}
	
	/**
	 */
		public int
	getNumValues()
	{
		return( mValues.length );
	}
	
	/**
	 */
		public String[]
	getValues()
	{
		return( mValues );
	}
	
		public String
	getValue()
		throws IllegalOptionException
	{
		if ( mValues.length != 1 )
		{
			throw new IllegalOptionException( "expecting to get a single value, not " + mValues.length );
		}

		return( mValues[ 0 ] );
	}
	
	/**
	 */
		public Boolean
	getBoolean( )
		throws IllegalOptionException
	{
		return( new Boolean( getValue( ) ) );
	}
	
	
	/**
	 */
		public Integer
	getInteger( )
		throws IllegalOptionException
	{
		return( new Integer( getValue() ) );
	}
	
	/**
	 */
		public String
	toString()
	{
		return( mName + "=" + ArrayStringifier.stringify( mValues, "," ) );
	}
	
	/**
	 */
		public boolean
	equals( Object rhs )
	{
		if ( ! (rhs instanceof ParsedOption) )
			return( false );
			
		final ParsedOption	other	= (ParsedOption)rhs;
		boolean	equalsSoFar	= mName.equals( other.mName ) &&
								mValues.length == other.mValues.length &&
								ArrayUtil.arraysEqual( mValues, other.mValues );
		
		return( equalsSoFar );
	}
}

	

