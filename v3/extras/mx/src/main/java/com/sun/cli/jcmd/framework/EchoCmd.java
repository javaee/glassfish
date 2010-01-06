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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/EchoCmd.java,v 1.6 2003/12/19 01:48:07 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2003/12/19 01:48:07 $
 */
 
package com.sun.cli.jcmd.framework;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
	Echo operands to stdout
 */
public class EchoCmd extends CmdBase
{
		public
	EchoCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class EchoCmdHelp extends CmdHelpImpl
	{
			public
		EchoCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "print output to stdout";
		static final String	SOURCE_TEXT		=
		"Prints each operand to stdout, separated by white-space.  If the first operand starts with " +
		"the '-' character, then you must preceed it with the end-of-operands token '--'";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SOURCE_TEXT ); }
	}
		public CmdHelp
	getHelp()
	{
		return( new EchoCmdHelp() );
	}

	
	
	public final static String	NAME	= "echo";
	
	private final static CmdInfo	CMD_INFO	=
		new CmdInfoImpl( NAME, null, new OperandsInfoImpl( "message", 0) );
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( CMD_INFO )  );
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		
		println( ArrayStringifier.stringify( operands, " " ) );
	}
}






