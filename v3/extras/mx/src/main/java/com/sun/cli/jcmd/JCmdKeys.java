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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/JCmdKeys.java,v 1.6 2004/02/25 02:16:27 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/02/25 02:16:27 $
 */
 
package com.sun.cli.jcmd;

import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;

/**
	Keys for various things, mostly for things found in the CmdEnv, but also for
 */
public final class JCmdKeys
{
		private
	JCmdKeys()
	{
	}
	
	
	/**
		Name of the "command" which specifies the boot options
	 */
	public final static String	BOOT_OPTIONS_CMD	= "boot";
	
	
	/**
		Name of the boot option which determines the prefix to be used for
		the "env" file and the history file. For example JCMD_NAME set to "jcmd" would mean
		that the history file would be named "jcmd-history" and the env file would be named
		"jcmd-env.props".  It may also be used for the command prompt.
		<p>
		This is not intended for CLI-user use, as the author of the CLI will want a 
		particular CmdMgr to be run.  That is why it is a system property; it is not a meta
		option open for use by the user.
		<p>
		Command lines using the framework should follow this convention for any additional files
		the create.
	 */
	public final static OptionInfo	CLI_NAME_OPTION		= new OptionInfoImpl( "name", "n", new String[] { "cli-name" } );
	
	/**
		Name of the boot option which determines the CmdMgr class to be instantiated and run.
		By default, the CmdMgr will DEFAULT_CMD_MGR_CLASSNAME.
		<p>
		This is not intended for CLI-user use, as the author of the CLI will want a 
		particular CmdMgr to be run.  That is why it is a system property; it is not a meta
		option open for use by the user.
	 */
	public final static OptionInfo	CMD_MGR_NAME_OPTION	= new OptionInfoImpl( "cmd-mgr-classname", "c", "classname");
	
	/**
		The default CmdMgr to be used if none is specified.
	 */
	public final static String	DEFAULT_CMD_MGR_CLASSNAME	= "com.sun.cli.jcmd.framework.CmdMgrImpl";
	
	
	/**
		Name of the "command" which indicates that all options following it until the operands are
		meta-options.
		
	 */
	public final static String	META_OPTIONS_CMD	= "meta";
	
	/**
		Name of the meta-option which determines the directory in which to store the history and
		env files.  Unlike JCMD_NAME and JCMD_CMD_MGR, this property might be set by a user
		of the cli to control where things are stored in any particular invocation.  Also, it is
		*not* a system property; it is a meta option.  Sample usage:<br>
		jcmd --prefs-dir=/tmp setenv foo=bar
		<p>
		Note that meta options preceed the subcommand ("setenv"). An optional end-of-options marker
		may also be used to make a few special cases work:<br>
		jcmd --prefs-dir=/tmp -- --help
		
		Placed into the Map passed to the CmdMgr using its long-option-name.
	 */
	public final static OptionInfo	PREFS_DIR_META_OPTION	= new OptionInfoImpl( "prefs-dir", "s", "path");
	
	/**
		Name of the meta-option which determines whether debug mode is set or not.
		
		Placed into the Map passed to the CmdMgr using its long-option-name.
	 */
	public final static OptionInfo	DEBUG_META_OPTION	= new OptionInfoImpl( "debug", "d" );
	
	/**
		Name of the meta-option which indicates a properties file to be added to the PROPERTIES 
		property list.
	 */
	public final static OptionInfo	PROPERTIES_FILE_META_OPTION	= new OptionInfoImpl( "props-file", "f", "path");
	
	/**
		Name of the meta-option which indicates a property to be added to the PROPERTIES 
		property list.
	 */
	public final static OptionInfo	PROPERTY_META_OPTION	= new OptionInfoImpl( "prop", "p", "property-pair");
	
	
	/**
		Key for looking up all properties in the meta-options Map.  All the properties are placed into
		a single properties object accessible via this key.
	 */
	public final static String	PROPERTIES	= "PROPERTIES";
	
	
	/**
		Key for looking up the object which was the result of the last command.  Some
		commands set this, and some do not.
	 */
	public final static String	LAST_RESULT	= "LAST_RESULT";
};


