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

import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.cli.commands.BaseLifeCycleCommand;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;

import com.sun.enterprise.ee.admin.servermgmt.EEDomainsManager;
import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.ee.admin.servermgmt.AgentManager;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.common.Status;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;

/**
 *  This is a local command that creates a new domain
 *  @version  $Revision: 1.4 $
 */
public abstract class BaseNodeAgentCommand extends BaseLifeCycleCommand
{    
    private static final StringManager _strMgr = 
        StringManager.getManager(BaseNodeAgentCommand.class);
    
    protected static final String AGENTDIR = "agentdir";
    private static final String NODE_AGENT_DIRECTORY_NAME = "nodeagents";       
    protected static final String START_INSTANCES_OVERRIDE = "startinstances";       
    protected static final String SYNC_INSTANCES_OVERRIDE = "syncinstances";
    

    /** Creates new CreateDomainCommand */
    public BaseNodeAgentCommand()
    {
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
     * Return the node agent root path. This can either be specified by the --path 
     * option on the command line, or can default. For now, we default to 
     * the parent directory of com.sun.aas.domainsRoot ($AS_DEF_DOMAINS_PATH) under 
     * a subdirectory named "agents".
     */
    protected String getAgentPath() throws CommandException
    {
        String path = getOption(AGENTDIR);
        if (path == null) {
            path = System.getProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
            File f = (new File(path)).getParentFile();
            if (f != null) {
                try {
                    path = (new File(f, NODE_AGENT_DIRECTORY_NAME)).getCanonicalPath();
                } catch (Exception ex) {
                    throw new CommandException(_strMgr.getString("InvalidNodeAgentPath"), ex);
                }
            }
        }
        if (path == null) {
            throw new CommandException(_strMgr.getString("InvalidNodeAgentPath"));
        }
        return path;
    }      
  
    protected String[] getAgents() throws CommandException 
    {
       try {                            
            AgentConfig agentConfig = new AgentConfig(null, getAgentPath());
            AgentManager manager = getAgentManager(agentConfig); 
            return manager.listNodeAgents();
       } catch (Exception ex) {
           throw new CommandException(ex);
       }
    }

    protected String getAgentName() throws CommandException
    {
	    String agentName = null;
        if (operands.isEmpty()) {
		    final String[] agents = getAgents();
		    if (agents.length == 0) {
		        throw new CommandException(_strMgr.getString("NoAgents", 
				    new Object[] {getAgentPath()}));
            } else if (agents.length > 1) {
		        throw new CommandException(_strMgr.getString("NoDefaultAgent",
					new Object[] {getAgentPath()}));
            } else {
		        agentName = agents[0];  //assign the only domain
	        }
        } else {
	        agentName = (String)operands.firstElement();
        }
        CLILogger.getInstance().printDebugMessage("agentName =" + agentName);
        return agentName;
    }

    /**
     * Converts the port string to port int
     * @param port - the port number
     * @return 
     * @throws CommandValidationExeption if port string is not numeric
     */
    protected int convertPortStr(final String port) 
	throws CommandValidationException
    {
        try {
            return Integer.parseInt(port);
        } catch(Exception e) {
            throw new CommandValidationException(e);
        }
    }
    
    protected AgentManager getAgentManager(AgentConfig agentConfig) {
        EEDomainsManager domainsManager = (EEDomainsManager)getFeatureFactory().getDomainsManager();
        return domainsManager.getAgentManager(agentConfig);                        
    }

    
    boolean isNotRunning(AgentManager mgr, AgentConfig cfg) throws Exception
    {
        InstancesManager im = mgr.getInstancesManager(cfg);
        int state = im.getInstanceStatus();
        return state == Status.kInstanceNotRunningCode;
    }


    /**
     * Get MasterPassword from passwordfile.  If not specified in passwordfile and
     * interactive is true and mastern_password file does not exist then prompt the
     * user for the masterpassword.
     * If masterpassword is an empty string (by typing return/enter key)
     * then set masterpassword to the default value.
     * @return masterpassword
     * @throws CommandValidationException is interactive is false and the option
     * value is null or if the two values entered do not match.
     */
    String getMasterPasswordWithDefaultPrompt(RepositoryManager mgr, RepositoryConfig config)
           throws CommandValidationException, CommandException
    {
        String mpassword = getPassword(MASTER_PASSWORD, "MasterPasswordWithDefaultPrompt",
                                       "", false, false, false, true, mgr, config, true, false,
                                       false, false);
        if (mpassword != null && mpassword.length()>0)
            return mpassword;
        return DEFAULT_MASTER_PASSWORD;            
    }


    
}
