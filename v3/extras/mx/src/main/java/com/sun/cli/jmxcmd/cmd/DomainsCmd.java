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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/DomainsCmd.java,v 1.7 2004/02/06 02:11:22 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/02/06 02:11:22 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import com.sun.cli.jcmd.util.cmd.ArgHelper;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;




/**
	Displays the available JMX domains.
 */
public class DomainsCmd extends JMXCmd
{
		public
	DomainsCmd( final CmdEnv env )
	{
		super( env );
	}
	
	static final class DomainsCmdHelp extends CmdHelpImpl
	{
		private final static String	DOMAINS_SYNOPSIS	= "display the available domains";
		private final static String	DOMAINS_TEXT		= "Displays the availabled domains.";

		public	DomainsCmdHelp()	{ super( getCmdInfos() ); }
		
		public String	getSynopsis()	{	return( formSynopsis( DOMAINS_SYNOPSIS ) ); }
		public String	getText()		{	return( DOMAINS_TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new DomainsCmdHelp() );
	}
	
	final static String	DOMAINS_NAME				= "domains";
	final static String	GET_DEFAULT_DOMAIN_NAME		= "get-default-domain";
	
	private final static CmdInfo	DOMAINS_INFO			=
				new CmdInfoImpl( DOMAINS_NAME );
	private final static CmdInfo	GET_DEFAULT_DOMAIN_INFO	=
				new CmdInfoImpl( GET_DEFAULT_DOMAIN_NAME );
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( DOMAINS_INFO, GET_DEFAULT_DOMAIN_INFO ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd	= getSubCmdNameAsInvoked();
		
		establishProxy();
		if ( cmd.equals( DOMAINS_NAME ) )
		{
			final String [] domains	= getProxy().mbeanDomains();
			println( ArrayStringifier.stringify( domains, "\n" ) );
		}
		else if ( cmd.equals( GET_DEFAULT_DOMAIN_NAME ) )
		{
			final String domain	= getProxy().mbeanGetDefaultDomain();
			println( domain );
		}
	}
}






