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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/VersionCmd.java,v 1.7 2004/02/06 02:11:22 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/02/06 02:11:22 $
 */
 
package com.sun.cli.jcmd.framework;

import com.sun.cli.jcmd.framework.Cmd;
import com.sun.cli.jcmd.framework.CmdBase;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;


/**
	This command should be subclassed by the CLI making use of the framework.
 */
public abstract class VersionCmd extends CmdBase
{
		public
	VersionCmd( final CmdEnv env )
	{
		super( env );
	}
	
	static final class VersionCmdHelp extends CmdHelpImpl
	{
			public
		VersionCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS			= "display the version";
		static final String	VERSION_TEXT		= "Displays the version of this CLI.";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( VERSION_TEXT ); }
	}


		public CmdHelp
	getHelp()
	{
		return( new VersionCmdHelp() );
	}
	
	static final String	VERSION_NAME		= "version";
	
	
	private final static CmdInfo	VERSION_INFO		= new CmdInfoImpl( VERSION_NAME, OperandsInfoImpl.NONE);
	private final static CmdInfo	VERSION_LONG_INFO	= new CmdInfoImpl( VERSION_OPTION_LONG, OperandsInfoImpl.NONE);
	private final static CmdInfo	VERSION_SHORT_INFO	= new CmdInfoImpl( VERSION_OPTION_SHORT, OperandsInfoImpl.NONE);
		
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( VERSION_INFO, VERSION_LONG_INFO, VERSION_SHORT_INFO ) );
	}


	
	/**
		Print the version (preferably in the following format):
		
		<name> [(<package-name>)] <version>
		<copyright-notice>
		
	 */
	protected abstract void	printVersion();
	
	/**
		There usually will be a single operand which is the name of the subcommand (if any)
		that was specified.  This command could be invoked as:
		
		--version|-V
		<sub-command> --version|-V
	 */
		protected void
	executeInternal()
		throws Exception
	{
		printVersion();
	}
}
