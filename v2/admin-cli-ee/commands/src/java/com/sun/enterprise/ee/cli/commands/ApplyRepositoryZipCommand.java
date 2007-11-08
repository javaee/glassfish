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
package com.sun.enterprise.ee.cli.commands;

import java.io.File;
import com.sun.enterprise.ee.util.zip.Unzipper;
import com.sun.enterprise.cli.commands.GenericCommand;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.cli.framework.CLILogger;

import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;

/**
 * Renders a repository zip to the server instance or node agent. 
 *
 * <pre>
 * Example Usage:
 * For a node-agent agent1, the CLI would look like the following:
 * ./asadmin --target agent1 --rootdir <install>/nodeagents/agent1/agent 
 *             agent1-repository.zip
 *
 * Where,
 *   target - name of the node-agent
 *   rootdir - the install root of the agent
 *   agent1-repository.zip - zip created by create-repository-zip command
 *     
 * Similarly, for server instance server,
 * ./asadmin --target server --rootdir <install>/nodeagents/agent1/server 
 *             server-repository.zip
 * </pre>
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class ApplyRepositoryZipCommand extends GenericCommand {    
    
    /** 
     * Constructor.
     */
    public ApplyRepositoryZipCommand() {
    }
    
    /**
     * An abstract method that validates the options on the specification
     * in the xml properties file
     *
     *  @return true if successfull
     */
    public boolean validateOptions() throws CommandValidationException
    {
        return super.validateOptions();      
    }

 
    /**
     * Assembles the repository zip. 
     *
     * @throws CommandException if an error during zip creation
     */
    public void runCommand() 
            throws CommandException, CommandValidationException {

        validateOptions();

        String rootDir  = null;
        String target   = null;
        String zip      = null;

        try {                       
            rootDir = getOption(ROOT_DIR);
            target = getOption(TARGET);
            if (!operands.isEmpty()) {
                zip = (String)operands.firstElement();
            }

            if (rootDir==null || target==null || zip==null) {

                throw new CommandValidationException(_strMgr.getString(
                    "InvalaidApplyRepositoryCommandConfiguration"));
            }

            // target name will be part of root dir
            if (rootDir.indexOf(target) > 0) {

                Unzipper z = new Unzipper(rootDir);
                z.writeZipFile(zip);

                // successful msg
                CLILogger.getInstance().printDetailMessage(
                    _strMgr.getString("ApplyRepositoryZipCommandSuccessful", 
                    new Object[] {name, zip}));
            } else {
                throw new CommandValidationException(_strMgr.getString(
                    "InvalaidRootDir", new Object[] {rootDir, target}));
            }

        } catch (Exception e) {                        
            e.printStackTrace();

            CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());            
            throw new CommandException(
                _strMgr.getString("ApplyRepositoryZipCommandFailed", 
                new Object[] {name}), e);
        }

    }

    // ---- VARIABLES - PRIVATE ----------------------------------------
    private static final String ROOT_DIR    = "rootdir";
    private static final String TARGET      = "target";

    private static final StringManager _strMgr = 
        StringManager.getManager(ApplyRepositoryZipCommand.class);
}
