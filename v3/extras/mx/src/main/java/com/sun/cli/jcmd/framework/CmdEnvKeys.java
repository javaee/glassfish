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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdEnvKeys.java,v 1.6 2004/01/15 20:33:27 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/01/15 20:33:27 $
 */
 

package com.sun.cli.jcmd.framework;



/**
	Keys for options supported/required by the framework.
 */
public final class CmdEnvKeys
{
	private CmdEnvKeys()	{/*disallow*/}
	
	/**
		CmdEnv key for the tokens of the command being executed.  The creator
		of the instance must place the tokens into the environment.
		
		Tokens must be a String[]
	 */
	public final static String	TOKENS		= "TOKENS";
	
	/**
		CmdEnv key for the CmdHistory
	 */
	public final static String	CMD_HISTORY	= "CMD_HISTORY";
	
	/**
		CmdEnv key for the CmdAliasMgr
	 */
	public final static String	CMD_ALIAS_MGR	= "CMD_ALIAS_MGR";
	
	/**
		CmdEnv key for the CmdRunner
	 */
	public final static String	CMD_RUNNER	= "CMD_RUNNER";
	
	/**
		CmdEnv key for the CmdFactory
	 */
	public final static String	CMD_FACTORY	= "CMD_FACTORY";
	
	/**
		CmdEnv key for the list of CmdSource classes (String)
	 */
	public final static String	COMMAND_SOURCES	= "COMMAND_SOURCES";
	
	
	/**
		CmdEnv key for the CmdOutput that should be used when emitting
		output.
	 */
	public final static String	CMD_OUTPUT	= "CMD_OUTPUT";
	
	
	/**
		CmdEnv key which sets debug status
	 */
	public final static String	DEBUG		= "debug";
	
	/**
		CmdEnv key which sets verbose status.
	 */
	public final static String	VERBOSE		= "verbose";
	
	
	/**
		CmdEnv key for the CmdEventListenerList
	 */
	public final static String	CMD_EVENT_MGR	= "CMD_EVENT_MGR";
	
	/**
		CmdEnv key for classpath of additional classes.
	 */
	public final static String	ADDITIONAL_CLASSPATH	= "ADDITIONAL_CLASSPATH";
	
	
	/**
		CmdEnv key for UnknownCmdHelper
	 */
	public final static String	UNKNOWN_CMD_HELPER	= "UNKNOWN_CMD_HELPER";
	
	
	/**
		CmdEnv key for optional startup file. If present, the script will be 
		executed upon startup.
	 */
	public final static String	STARTUP_SCRIPT	= "STARTUP_SCRIPT";
};
	
