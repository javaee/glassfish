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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/CountCmd.java,v 1.6 2004/06/09 03:26:48 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/06/09 03:26:48 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import javax.management.ObjectName;

import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
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



/**
	Counts the number of registered MBeans.
 */
public class CountCmd extends JMXCmd
{
		public
	CountCmd( final CmdEnv env )
	{
		super( env );
	}
	
	static final class CountCmdHelp extends CmdHelpImpl
	{
		public	CountCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS		= "count the number of registered MBeans";
		private final static String	COUNT_TEXT		=
		"Counts the number of registered MBeans.\n" +
		"If no operands are supplied, all MBeans are counted, " +
		"otherwise the expression is evaluated and the number of matches is counted.";
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( COUNT_TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new CountCmdHelp() );
	}
	
	final static String	NAME		= "count";
	
	private final static CmdInfo	CMD_INFO	=
		new CmdInfoImpl( NAME, TARGETS_OPERAND_INFO);
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( CMD_INFO ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		
		establishProxy();
		
		int	numMBeans	= 0;
		if ( operands.length != 0 )
		{
			final CLISupportMBeanProxy	proxy	= getProxy();
			final ObjectName[]	objectNames	= resolveTargets( proxy, operands );
			
			numMBeans	= objectNames.length;
		}
		else
		{
			numMBeans	= getProxy().mbeanCount();
		}
		
		println( "" + numMBeans );
	}
}






