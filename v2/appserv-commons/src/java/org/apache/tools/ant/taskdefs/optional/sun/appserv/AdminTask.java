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
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.tools.ant.taskdefs.optional.sun.appserv;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;

/**
 * This task enables arbitrary administrative commands and scripts to be 
 * executed on the Sun ONE Application Server 7.  This is useful for cases where 
 * a specific Ant task hasn't been developed or a set of related commands has 
 * been composed in a single script.
 *
 * In addition to the server-based attributes, this task introduces three 
 * attribute: 
 *   <ul>
 *     <li><i>command</i> -- The command to execute.  If the "user", "password", "passwordfile", 
 *                           "host", "port" or "instance" attributes are also 
 *                           specified, they will automatically be inserted into 
 *                           the command before it is exectuted.  If any of 
 *                           these options is specified in the command string, 
 *                           the corresponding attribute value will be ignored
 *     <li><i>commandfile</i> -- The command script to execute.  If the "user", 
 *                               "password", "passwordfile", "host", "port" or "instance" 
 *                               attributes are also specified, they will 
 *                               automatically be inserted into the command 
 *                               environment before the script is exectuted
 *     <li><i>explicitcommand</i> -- The exact command to execute.  No 
 *                                   processing of the command is done, and all 
 *                                   other attributes are ignored
 *   </ul>
 * <p>
 *
 * @see    AppServerAdmin
 * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
 */
public class AdminTask extends AppServerAdmin {
	private String command;
	private String explicitCommand;
	private File   commandFile;
    LocalStringsManager lsm = new LocalStringsManager();

	/**
	 * Sets the command to be executed.  The task will automatically add user,
	 * password or passwordfile, host, port, and instance parameters if they're not specified in
	 * the command.
	 *
	 * @param command The command to be executed
	 */
	public void setCommand(String command) {
		this.command = command.trim();
	}

	/**
	 * Sets the command to be executed.  The task will execute the command as
	 * specified and will NOT automatically add user, password or passwordfile, host, port and
	 * instance parameters
	 *
	 * @param command The command to be executed
	 */
	public void setExplicitcommand(String explicitCommand) {
		this.explicitCommand = explicitCommand;
	}

	/**
	 * Sets the command file which contains zero or more administrative commands
	 * which will be read and executed.  The user, password or passwordfile, host, port, and
	 * instance parameters will automatically be added to the environment when
	 * the command is executed
	 *
	 * @param commandFile The command file to be executed
	 */
	public void setCommandfile(File commandFile) {
            final String msg = lsm.getString("DeprecatedAttribute", 
                                                new Object[] {"commandfile", 
                                                              "- 'multimode --file <commandfile>'"});
            log(msg, Project.MSG_WARN);
            this.commandFile = commandFile;
	}

	public void execute() throws BuildException {
		checkCommandCount();

		if ((command != null) && (servers.size() == 0) && (server == null)) {
			// No other attributes or server elements -- treat as explicitCommand // FIXME -- this will no longer ever happen
			explicitCommand = command;
			command = null;
		}

		if (explicitCommand != null) {
			// Normal execution process is skipped and command is run immediately
			execAdminCommand(explicitCommand);
		} else {
			super.execute();
		}
	}

    /**
     * Verifies that one and only one of the command attributes (command,
	 * commandfile, and explicitcommand) has been set.
	 *
	 * @throws BuildException If none of the command attributes has been set or
	 *                        multiple command attributes has been set
     */
	private void checkCommandCount() throws BuildException {
		int commandCount = 0;
		
		if (command != null) { commandCount++; }
		if (explicitCommand != null) { commandCount++; }
		if (commandFile != null) { commandCount++; }

		if (commandCount != 1) {
            final String msg = lsm.getString("ExactlyOneCommandAttribute");
			throw new BuildException(msg, getLocation());
		}
	}

	protected void checkConfiguration(Server aServer) throws BuildException {
		// No error checking is done for this task
	}

	protected void execute(Server aServer) throws BuildException {
		/*
		 * "command" and "commandfile" attributes are processed in this method.
		 * See the execute() method for "explicitcommand" processing.
		 */

		final String userOption[] = {"--user ", "-u "};
		final String passwordOption[] = {"--password ", "-w ", "--passwordfile "};
		final String hostOption[] = {"--host ", "-H "};
		final String portOption[] = {"--port ", "-p "};
		final String instanceOption[] = {"--instance ", "-i "};
        final String secureOption[] = {"--secure ", "-s "};

		StringBuffer cmd;
		if (command != null) {
			cmd = new StringBuffer(command);
			if (!commandIncludes(cmd, userOption)) {
				insertCommandOption(cmd, " --user " + aServer.getUser());
			}
			if ((aServer.hasPassword()) 
								&& (!commandIncludes(cmd, passwordOption))) {
				insertCommandOption(cmd, aServer.getPasswordCommand());
			}
			if (!commandIncludes(cmd, hostOption)) {
				String theHost = aServer.getHost();
				if (theHost == null) {
					theHost = Server.DEFAULT_HOST;
				}
				insertCommandOption(cmd, " --host " + theHost);
			}
			if (!commandIncludes(cmd, portOption)) {
				String thePort = (aServer.getPort() == 0) ?
									Server.DEFAULT_PORT :
									String.valueOf(aServer.getPort());
				insertCommandOption(cmd, " --port " + thePort);
			}
			if ((aServer.getInstance() != null) 
								&& (!commandIncludes(cmd, instanceOption))) {
				insertCommandOption(cmd, " --instance " + aServer.getInstance());
			}
            if (aServer.getSecure() != null &&
                !commandIncludes(cmd, secureOption)) {
				insertCommandOption(cmd, " --secure=" + aServer.getSecure());
			}

		} else {
			String filename;
			try
			{
				filename = commandFile.getCanonicalPath();
			}
			catch(Exception e)
			{
				filename = commandFile.getAbsolutePath();
			}
			// passing in '\\' for Windows would make CLI choke!
			filename = filename.replace('\\', '/');

			cmd = new StringBuffer("multimode --file " + filename + " "); 
		}

		execAdminCommand(cmd.toString());
	}

	/**
	 * Utility method that determines if command options have already been
	 * specified in the command line
	 *
	 * @param cmd  The command line to search.
	 * @param options  The options to search for.
	 * @return <code>true</code> if one of the options is found in the command
	 *         line
	 */
	private boolean commandIncludes(StringBuffer cmd, String options[]) {
		for (int i = 0; i < options.length; i++) {
			if (cmd.indexOf(options[i]) > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility method that inserts a string into the current command line.  The
	 * string is inserted after the command name.
	 *
	 * @param cmdLine  The command line.
	 * @param commandOption  The string to insert into the command line.
	 */
	private void insertCommandOption(StringBuffer cmdLine, String commandOption) {
		int index = cmdLine.indexOf(" ");
		index = (index >= 0) ? index : cmdLine.length();
		cmdLine.insert(index, commandOption).append(' ');
	}
}
