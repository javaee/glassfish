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
 *  $Id: ChangeMasterPasswordCommand.java,v 1.2 2007/03/29 01:32:10 janey Exp $
 */

package com.sun.enterprise.ee.cli.commands;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;


import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.ee.admin.servermgmt.AgentManager;

import com.sun.enterprise.util.i18n.StringManager;

/**
 *  This is a local command that creates a new domain
 *  @version  $Revision: 1.2 $
 */
public class ChangeMasterPasswordCommand extends BaseNodeAgentCommand
{            
    private String newMasterPassword = null;
    private String masterPassword = null;

    private static final StringManager _strMgr = 
        StringManager.getManager(ChangeMasterPasswordCommand.class);

    /** Creates new CreateDomainCommand */
    public ChangeMasterPasswordCommand()
    {
    }

    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  @return true if successfull
     */
    public boolean validateOptions() throws CommandValidationException
    {
        super.validateOptions();

        //verify adminpassword is greater than 8 characters	
        if (!isPasswordValid(newMasterPassword)) {
            throw new CommandValidationException(_strMgr.getString("PasswordLimit",
                new Object[]{ADMIN_PASSWORD}));
        }
                
        return true;
    }

    /**
     *  An abstract method that executes the command
     *  @throws CommandException
     */
    public void runCommand() 
        throws CommandException, CommandValidationException
    {   
        setLoggerLevel();
        String domainName = getDomainName();         
        String agentName = null;        
        try {
            DomainConfig config = getDomainConfig(domainName);                         
            DomainsManager mgr = getFeatureFactory().getDomainsManager();    
            //domain validation upfront (i.e. before we prompt)                      
            mgr.validateDomain(config, true);
        } catch (Exception ex) {
            //any exception validating the domain name, and we will 
            //assume that a node agent has been specified
            domainName = null;
        }
        try {                 
            if (domainName != null) {  
                /**
                com.sun.enterprise.cli.commands.ChangeMasterPasswordCommand cmd = 
                    new com.sun.enterprise.cli.commands.ChangeMasterPasswordCommand();
                cmd.changeMasterPassword(domainName);             
                 ***/
                //WARNING!!! The code below is duplicated in admin-cli ChangeMasterPasswordCommand.java.
                //I tried to share the code as illustrated above, but ran into issues.
                DomainConfig config = getDomainConfig(domainName);                         
                DomainsManager mgr = getFeatureFactory().getDomainsManager();    
                //domain validation upfront (i.e. before we prompt)                      
                mgr.validateDomain(config, true);

                masterPassword = getMasterPassword(new RepositoryManager(), config);
                //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, 
                //readMasterPasswordFile, mgr, config,
                //promptUser, confirm, validate)
                config.put(DomainConfig.K_MASTER_PASSWORD, masterPassword);
                mgr.validateMasterPassword(config);
                
                newMasterPassword = getNewMasterPassword();
                validateOptions();            
                Boolean saveMasterPassword = getSaveMasterPassword(null);        
                config.put(DomainConfig.K_NEW_MASTER_PASSWORD, newMasterPassword);
                config.put(DomainConfig.K_SAVE_MASTER_PASSWORD, saveMasterPassword);
                mgr.changeMasterPassword(config);    
                CLILogger.getInstance().printDetailMessage(_strMgr.getString("DomainPasswordChanged",
                    new Object[] {domainName})); 
                //END WARNING!!!
            } else {
                agentName = getAgentName();            
                AgentConfig config = new AgentConfig(agentName, 
                    getAgentPath());           
                AgentManager manager = getAgentManager(config);
                //Ensure that the node agent exists
                manager.validateNodeAgent(config, true); 
                masterPassword = getMasterPassword(manager, config);

                config.put(AgentConfig.K_MASTER_PASSWORD, masterPassword);
                manager.validateMasterPassword(config, true);

                //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, 
                //readMasterPasswordFile, mgr, config,
                //promptUser, confirm, validate)
                newMasterPassword = getNewMasterPassword();
                Boolean saveMasterPassword = getSaveMasterPassword(null);                
                config.put(AgentConfig.K_NEW_MASTER_PASSWORD, newMasterPassword);
                config.put(AgentConfig.K_SAVE_MASTER_PASSWORD, saveMasterPassword);
                manager.changeMasterPassword(config);     
                CLILogger.getInstance().printDetailMessage(_strMgr.getString("NodeAgentPasswordChanged",
                    new Object[] {agentName})); 
            }
        } catch (Exception e) {
            displayExceptionMessage(e);            
        }
    }
}
