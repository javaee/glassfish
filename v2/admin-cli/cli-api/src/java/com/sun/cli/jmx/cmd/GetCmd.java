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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/GetCmd.java,v 1.3 2005/12/25 03:45:35 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:35 $
 */
 
package com.sun.cli.jmx.cmd;


import com.sun.cli.jmx.support.ResultsForGetSet;
import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.util.stringifier.*;

public class GetCmd extends GetSetCmd
{
	final static String	GET				= "get";
	final static String	GET_ABBREV		= "g";
	
		public
	GetCmd( final CmdEnv env )
	{
		super( env );
	}
	
		public String
	getUsage()
	{
		return( CmdStrings.GET_HELP.toString() );
	}
	

		public static String []
	getNames( )
	{
		return( new String [] { GET, GET_ABBREV } );
	}
	
	
		void
	executeInternal()
		throws Exception
	{
		final String			cmd	= getCmdNameAsInvoked();
		
		String  	attributes	= null;
		String []	targets		= null;
			
		if ( cmd.equalsIgnoreCase( GET ) || cmd.equalsIgnoreCase( GET_ABBREV ) )
		{
			attributes	= getAttributes();
			targets		= getTargets();
		}
		
		println( "Getting attributes: " + attributes );
		println( "Against targets: " + ArrayStringifier.stringify( targets, "\n" ) );
		
		establishProxy();
		final ResultsForGetSet []	results	= getProxy().mbeanGet( attributes, targets );
		
		println( ArrayStringifier.stringify( results, "\n\n" ) );
	}
}

