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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/FindCmd.java,v 1.3 2005/12/25 03:45:35 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:35 $
 */
 
package com.sun.cli.jmx.cmd;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

import javax.management.ObjectName;

import com.sun.cli.jmx.support.ResultsForGetSet;
import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.util.stringifier.*;
import com.sun.cli.util.TokenizerImpl;

/*
	Find MBeans by name (alias, full or partial name).
 */
public class FindCmd extends JMXCmd
{
	static final String	ENV_CURRENT_SET	= "FIND_COMMAND_CURRENT_SET";
	
		public
	FindCmd( final CmdEnv env )
	{
		super( env );
	}
	
		int
	getNumRequiredOperands()
	{
		// require 1, by default
		return( 0 );
	}
	
		public String
	getUsage()
	{
		return( CmdStrings.FIND_HELP.toString() );
	}
		public static String []
	getNames( )
	{
		return( new String [] { "find", "f" } );
	}
	
	
	static private final String	OPTIONS_INFO	=
		"current add remove regex,1 java-regex,1";
		
	
		ArgHelper.OptionsInfo
	getOptionInfo()
		throws ArgHelper.IllegalOptionException
	{
		return( new ArgHelperOptionsInfo( OPTIONS_INFO ) );
	}
	
	
		String []
	objectNamesToStrings( final ObjectName []	objectNames )
	{
		// sorting doesn't work on returned array, so convert to Strings first,then sort
		final String []	resultStrs	= new String [ objectNames.length ];
		for( int i = 0; i < resultStrs.length; ++i )
		{
			resultStrs[ i ]	= objectNames[ i ].toString();
		}
		
		return( resultStrs );
	}
	
		void
	display( final Set	names )
	{
		println( IteratorStringifier.stringify( names.iterator(), "\n" ) );
	}
	
		Set
	getCurrent()
	{
		return( (Set)envGet( ENV_CURRENT_SET ) );
	}
	
	
		Set
	arrayToSet( final Object []  names )
		throws Exception
	{
		final TreeSet	theSet	= new TreeSet();
		
		for( int i = 0; i < names.length; ++i )
		{
			theSet.add( names[ i ] );
		}

		return( theSet );
	}
	
	private final static char BACKSLASH	= '\\';
	
	/*
		We support only '*" in a simplified form.
	 */
		static String
	convertToJavaRegex( String input )
	{
		String	converted	= input;
		
		if ( input != null )
		{
			// first run the tokenizer on it specifying no delimiters so as to eliminate any escape constructs
			final String []	tokens	= new TokenizerImpl( input, "", '\\', "*").getTokens();
			assert( tokens.length == 1 );
			// now any '\' or '*' characters are to be taken literally
			
			final String	unescapedInput	= tokens[ 0 ];
			
			final int 			length	= unescapedInput.length();
			final StringBuffer	buf	= new StringBuffer();
			
			for( int i = 0; i < length; ++i )
			{
				final char	theChar	= unescapedInput.charAt( i );
				
				if ( theChar == '.' )
				{
					buf.append( "[.]" );
				}
				else if ( theChar == '*' )
				{
					buf.append( ".*" );
				}
				else if ( theChar == BACKSLASH )
				{
					buf.append( "" + BACKSLASH + BACKSLASH );
				}
				else
				{
					buf.append( theChar );
				}
			}
			
			converted	= buf.toString();
			
		}
		return( converted );
	}

		void
	executeInternal()
		throws Exception
	{
		String [] targets	= getOperands();
		
		if ( targets.length == 0 )
		{
			targets	= new String [] { "all" };
		}
		
		final boolean add				= getBoolean( "add", Boolean.FALSE ).booleanValue();
		final boolean remove			= getBoolean( "remove", Boolean.FALSE ).booleanValue();
		final boolean displayCurrent	= getBoolean( "current", Boolean.FALSE ).booleanValue();
		final String regex				= getString( "regex", null );
		final String javaregex			= getString( "java-regex", null );
		
		final Set	currentSet	= getCurrent();
		
		if ( displayCurrent )
		{
			if ( currentSet == null || currentSet.size() == 0 )
			{
				println( "Nothing in current set." );
			}
			else
			{
				display( currentSet  );
			}
		}
		else
		{
			establishProxy();
			
			final String	actualRegex	= (javaregex != null) ?
								javaregex : convertToJavaRegex( regex );
			
			ObjectName []	objectNames	= getProxy().mbeanFind( targets, actualRegex);
			
			final String []		objectStrings	= objectNamesToStrings( objectNames );
			Set		resultSet	= arrayToSet( objectStrings );
			
			if ( currentSet != null )
			{
				if ( add )
				{
					currentSet.addAll( resultSet );
					resultSet	= currentSet;
				}
				else if ( remove )
				{
					currentSet.removeAll( resultSet );
					resultSet	= currentSet;
				}
			}
			
			envPut( ENV_CURRENT_SET, resultSet, false );
			
			if ( resultSet.size() == 0 )
			{
				println( "No objects match the targets " + SmartStringifier.toString( targets ) );
			}
			else
			{
				
				display( resultSet );
			}
		}
	}
}





