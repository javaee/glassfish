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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/GetCmd.java,v 1.9 2004/01/30 07:58:10 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2004/01/30 07:58:10 $
 */
 
package com.sun.cli.jmxcmd.cmd;


import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import com.sun.cli.jcmd.framework.IllegalUsageException;


/**
	Get MBean Attributes.
 */
public class GetCmd extends GetSetCmd
{
	final static String	GET				= "get";
	
		public
	GetCmd( final CmdEnv env )
	{
		super( env );
	}
	
	
	static final class GetCmdHelp extends CmdHelpImpl
	{
		public	GetCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	GET_NAME		= "get";
		private final static String	GET_SYNOPSIS	= "get: display one or more attributes on the specified target(s).";
		private final static String	GET_TEXT		= 
	"Specify the attributes in a comma-separated list. " +
	"The following special targets are also available:\n"+
	"*  all attributes\n" +
	"*r all read-only attributes\n" +
	"*w all writeable attributes\n" +
	"\n'get' Examples: \n" +
	"get * *                    -- gets all attributes on all MBeans\n" +
	"get Count,Timeout MyMBean  -- gets the Count and Timeout attributes on the MBean 'MyMBean'\n";
		
		
		public String	getSynopsis()	{	return( GET_SYNOPSIS ); }
		public String	getText()		{	return( GET_TEXT ); }
	}


		public CmdHelp
	getHelp()
	{
		return( new GetCmdHelp() );
	}
	
	
	public static final OptionInfo	VERBOSE_OPTION = createVerboseOption();
	
	private static final CmdInfo	GET_INFO	= new CmdInfoImpl( GET,
		new OptionsInfoImpl( new OptionInfo[] { VERBOSE_OPTION } ),
		new OperandsInfoImpl( ATTR_LIST_ARG + " " + TARGET_LIST_ARG, 1));
			
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( GET_INFO ) );
	}

	
		protected void
	executeInternal()
		throws Exception
	{
		final String			cmd	= getSubCmdNameAsInvoked();
		
		String  	attributes	= null;
		String []	targets		= null;
		
		final boolean verbose	= getVerbose();
			
		if ( cmd.equalsIgnoreCase( GET )  )
		{
			attributes	= getAttributes();
			targets		= getTargets();
			
			if ( targets == null )
			{
				throw new IllegalUsageException( cmd, "no targets have been specified" );
			}
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
		
		printDebug( "Getting attributes: " + attributes );
		printDebug( "Against targets: " + ArrayStringifier.stringify( targets, "\n" ) );
		
		establishProxy();
		final ResultsForGetSet []	results	= getProxy().mbeanGet( attributes, targets );
		
		for ( int i = 0; i < results.length; ++i )
		{
			final ResultsForGetSet	result	= results[ i ];
			
			if ( result.getAttributes().size() != 0 || verbose )
			{
				println( SmartStringifier.toString( result ) );
				println( "" );
			}
		}
		
		envPut( JMXCmdEnvKeys.GET_RESULT, results, false );
	}
}

