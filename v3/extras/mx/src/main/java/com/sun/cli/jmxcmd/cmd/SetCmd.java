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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/SetCmd.java,v 1.6 2004/02/06 02:11:23 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/02/06 02:11:23 $
 */
 
package com.sun.cli.jmxcmd.cmd;


import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.*;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

/**
	Set MBean Attributes.
 */
public class SetCmd extends GetSetCmd
{
		public
	SetCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class SetCmdHelp extends CmdHelpImpl
	{
		public	SetCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS		= "set one or more attributes on the specified target(s)";
		private final static String	SET_TEXT		= 
	"Specify a comma-separated list of name-value pairs.\n" +
	"\n'set' Examples: \n" +
	"set Timeout=10,Count=20 MyMBean  -- sets the Timeout attribute to 10 and Count attribute to 20 " +
	"on the MBean 'MyMBean'\n";
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SET_TEXT ); }
	}

	private final static String	SET_NAME		= "set";	


		public CmdHelp
	getHelp()
	{
		return( new SetCmdHelp() );
	}
	
		
	private static final CmdInfo	SET_INFO	= new CmdInfoImpl( SET_NAME,
		new OperandsInfoImpl( "<name>=<value>[,<name>=<value>]* [target [target]*]", 1));
			
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( SET_INFO ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String  attributes	= getAttributes();
		final String []	targets		= getTargets();
		
		establishProxy();
		final ResultsForGetSet []	results	= getProxy().mbeanSet( attributes, targets );
		println( ArrayStringifier.stringify( results, "\n\n" ) );
		
		envPut( JMXCmdEnvKeys.SET_RESULT, results, false );
	}
}

