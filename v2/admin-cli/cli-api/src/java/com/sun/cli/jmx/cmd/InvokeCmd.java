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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/InvokeCmd.java,v 1.3 2005/12/25 03:45:37 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:37 $
 */
 
package com.sun.cli.jmx.cmd;


import com.sun.cli.jmx.support.ResultsForGetSet;
import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.jmx.support.InvokeResult;
import com.sun.cli.util.stringifier.*;

public class InvokeCmd extends JMXCmd
{
		public
	InvokeCmd( final CmdEnv env )
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
		return( CmdStrings.INVOKE_HELP.toString() );
	}
	
	public static String	FORMAL_NAME	= "invoke";
	
		public static String []
	getNames( )
	{
		return( new String [] { FORMAL_NAME, CmdFactory.DEFAULT_CMD_NAME} );
	}
	
	/*
		Allow two forms of invocation:
		doIt:[args] [targets]
		alias.doIt:[args] 
	 */
		void
	executeInternal()
		throws Exception
	{
		final String	cmdString	= getCmdNameAsInvoked();
		String [] 		targets		= getTargets();
		
		final int		colonPos	= cmdString.indexOf( ":" );
		
		if ( cmdString.equals( FORMAL_NAME ) )
		{
			printUsage();
			return;
		}
		else if ( colonPos < 0  )
		{
			printError( CmdStrings.NO_SUCH_COMMAND + cmdString );
			return;
		}
		
		
		String	argList		= null;
		String methodName	= null;
		final int	dotIndex	= cmdString.indexOf( '.' );
		if ( dotIndex > 0 && dotIndex < colonPos )
		{
			requireNumOperands( 0 );
			targets		= new String [] { cmdString.substring( 0, dotIndex ) };
			methodName	= cmdString.substring( dotIndex + 1, colonPos );
		}
		else
		{
			
			methodName		= cmdString.substring( 0, colonPos );
		}
		
		if ( colonPos + 1 != cmdString.length() )
		{
			argList	= cmdString.substring( colonPos + 1, cmdString.length() );
		}
		else
		{
			argList	= null;
		}
		
		
		//p( "INVOKE: " + methodName + "(" + argList + ") on " + ArrayStringifier.stringify( targets, " " ) );
		
		establishProxy();

		final InvokeResult []	results	= invoke( methodName, argList, targets );
		
		getProxy().mbeanInvoke( methodName, argList, targets );
		
		if ( results.length == 0 )
		{
			println( "Invocation failed: no targets found that match the expression" );
		}
		else
		{
			displayResults( results );
		}
	}
	
		void
	displayResults( final InvokeResult []	results )
	{
		println( new SmartStringifier( "\n", false ).stringify( results ) );
	}
	
		InvokeResult []
	invoke( String operationName, final String argList, final String [] targets )
		throws Exception
	{
		final InvokeResult []	results	= getProxy().mbeanInvoke( operationName, argList, targets );
		
		return( results );
	}
}



