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

import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.ee.admin.servermgmt.AgentManager;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;

import com.sun.enterprise.util.i18n.StringManager;

/**
 *
 * @author  kebbs
 */
public class StopNodeAgentCommand extends BaseNodeAgentCommand {
    
    private static final StringManager _strMgr = 
        StringManager.getManager(StopNodeAgentCommand.class);
       
    /** Creates a new instance of StopNodeAgentCommand */
    public StopNodeAgentCommand() {
    }
    
    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  @return true if successfull
     */
    public boolean validateOptions() throws CommandValidationException
    {
        return super.validateOptions();
    }
    
       /**
     *  An abstract method that executes the command
     *  @throws CommandException
     */
    public void runCommand() 
            throws CommandException, CommandValidationException
    {
	    validateOptions();

        try {
            final String agentName = getAgentName();
            AgentConfig agentConfig = new AgentConfig(agentName, 
                getAgentPath());
            AgentManager manager = getAgentManager(agentConfig);
                //validate node-agent to make sure it exists
            manager.validateNodeAgent(agentConfig, true);
            if (isNotRunning(manager,agentConfig)) {
                CLILogger.getInstance().printDetailMessage(_strMgr.getString("TargetAlreadyStopped", 
                                                                             new Object[] {agentName}));
                return;
            }
            stopNodeAgent(manager, agentConfig);
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
        } catch (Exception e) {            
                if (e.getLocalizedMessage() != null)
                    CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
                throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                              new Object[] {name} ), e);
	    }
    }

    private void stopNodeAgent(AgentManager mgr, AgentConfig cfg) 
    throws AgentException {
        String forceOption = getCLOption(KILL);
        if (forceOption == null) {
            mgr.stopNodeAgent();
        } else {
            int timeout = getIntegerOption(KILL);    
            mgr.stopNodeAgentForcibly(timeout);
        }
    }        
}
