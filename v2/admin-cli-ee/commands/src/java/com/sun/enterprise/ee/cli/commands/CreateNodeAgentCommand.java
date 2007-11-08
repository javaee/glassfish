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

import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.ee.admin.servermgmt.AgentManager;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.security.store.IdentityManager;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sun.enterprise.ee.nodeagent.NodeAgent;

/**
 *  This is a local command that creates a new domain
 *  @version  $Revision: 1.5 $
 */
public class CreateNodeAgentCommand extends BaseNodeAgentCommand
{    
    private static final String AGENT_USER = "agentuser";
    private static final String AGENT_PASSWD = "agentpassword";
    private static final String AGENT_PORT = "agentport";       
       
    private static final String ADMIN_HOST = "adminhost";       

    private static final String AGENT_PROPERTIES = "agentproperties";
 
    private static final StringManager _strMgr = 
        StringManager.getManager(CreateNodeAgentCommand.class);
    
    /** Creates new CreateDomainCommand */
    public CreateNodeAgentCommand()
    {
    }
    
    private void rendezvousWithDAS(AgentConfig config, Properties props)
        throws Exception
    {                                                
        //By default, we attempt to rendezvous with the remote DAS. The DAS must be running
        //for this to succeed.
        String rendezvous = (String)props.getProperty(AgentConfig.NODEAGENT_ATTEMPT_RENDEZVOUS, 
            "true");
        //By default we do not attempt to rendezvous with the local DAS. The DAS need not
        //be running for this to succeed but must be on the same machine.
        String localRendezvous = (String)props.getProperty(AgentConfig.NODEAGENT_ATTEMPT_LOCAL_RENDEZVOUS, 
            "false");
        boolean bRendezvous = new Boolean(rendezvous).booleanValue();
        boolean bLocalRendezvous = new Boolean(localRendezvous).booleanValue();
        if (bRendezvous || bLocalRendezvous) {
            boolean bDidRendezvous = false;
            NodeAgent agent = new NodeAgent(config);            
            // We need to disable any logging emitted by the NodeAgent unless -DDebug is set.
            Logger naLogger = agent.getLogger();            
            Level naLevel = naLogger.getLevel();            
            if (!CLILogger.isDebug())  {                                
                naLogger.setLevel(Level.OFF);                
            } else {                
                naLogger.setLevel(Level.FINEST);
            }
            try {
                IdentityManager.setUser((String)config.get(AgentConfig.K_DAS_USER));
                IdentityManager.setPassword((String)config.get(AgentConfig.K_DAS_PASSWORD));
                IdentityManager.setMasterPassword((String)config.get(AgentConfig.K_MASTER_PASSWORD));
                if (bRendezvous) {
                    try {                                  
                        agent.rendezvousWithDAS();
                    } catch (Exception ex) {
                        //An IOException indicates that the DAS is not reachable and as such 
                        //should not be considered an error.
                        if (!(ex instanceof IOException)) {
                            throw ex;
                        } else {
                            bDidRendezvous = false;
                        }
                    }
                }
                if (!bDidRendezvous && bLocalRendezvous) {                    
                    agent.localRendezvousWithDAS();
                }        
            } finally {
                //Re-establish the old debugging levels                
                naLogger.setLevel(naLevel);                
            }
        }
    }
    
    /**
     * Returns the admin port specified by the --adminport option. If unspecified a
     * random free port is chosen. We ensure that the admin port is not in use or
     * a CommandValidationException is thrown.
     */
    protected int getAdminPort() throws CommandValidationException
    {
        //verify admin port is not in use
        int adminPort;
        final String adminPortStr = getOption(AGENT_PORT);
        if (adminPortStr == null) {
            adminPort = NetUtils.getFreePort();            
        } else {
            adminPort = convertPortStr(adminPortStr);
        }
        if (!NetUtils.isPortFree(adminPort)) {
            throw new CommandValidationException(_strMgr.getString("AdminPortInUse", 
                adminPortStr));
        }
        CLILogger.getInstance().printDebugMessage("agentPort =" + adminPort);
        return adminPort;
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

        //verify for no-spaces in agentdir option on non-windows platform
        String agentDirValue = getOption(AGENTDIR);
        if ((agentDirValue != null) && !isWindows() && isSpaceInPath(agentDirValue))
            throw new CommandException(getLocalizedString("SpaceNotAllowedInPath",
                                                                        new Object[]{AGENTDIR}));
        //domain validation upfront (i.e. before we prompt)
        String agentName=null;
        try {                                     
            // check to see in the nodeagent name was supplied
            if (operands.isEmpty()) {
                // no name, default to hostname
                agentName=NetUtils.getHostName();
            } else {
                // name supplied, use it
                agentName=(String)operands.firstElement();
            }

            // log agentname for debug
            CLILogger.getInstance().printDebugMessage("agentName = " + agentName);
            
            AgentConfig agentConfig = new AgentConfig(agentName, 
                getAgentPath());
            AgentManager manager = getAgentManager(agentConfig);              
            manager.validateNodeAgent(agentConfig, false);
        } catch (Exception e) {
            if (e.getLocalizedMessage() != null)
                CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());

            throw new CommandException(getLocalizedString(
                "CommandUnSuccessful", new Object[] {name} ), e);
        }
        
        try {                       
            Properties agentProperties = getAgentProperties(getOption(AGENT_PROPERTIES));
            final String adminUser = getUser();
            final String adminPassword = getPassword();
            
                //need to call setOption before calling getMasterPassword() since
                //getMasterPassword() needs to get ADMIN_PASSWORD first.
            setOption(ADMIN_PASSWORD, adminPassword);
            
            boolean saveMasterPasswordSpecified = getBooleanOption(SAVE_MASTER_PASSWORD);
            String masterPassword = null;
            if (saveMasterPasswordSpecified) 
            {
               masterPassword = getMasterPasswordWithDefaultPrompt(null, null);
            }
            
            final String adminPort = String.valueOf(getPort());
            final String adminHost = getHost();
            
            Boolean saveMasterPassword = getSaveMasterPassword(masterPassword);  
            
            //The --secure option is a synonymn for --agentproperties agent.das.isSecure=false            
            if (!getBooleanOption(SECURE)) {                
                agentProperties.setProperty(AgentConfig.AGENT_DAS_IS_SECURE, "false");
            }                       

            //System.out.println("adminPassword=" + adminPassword + " masterPassword=" + masterPassword + 
            //    " saveMasterPassword=" + saveMasterPassword);            
            
            if (adminHost == null || adminPort == null || adminUser == null || adminPassword == null) {
                throw new CommandValidationException(_strMgr.getString("InvalidDASConfiguration"));
            }
        
            // defaulted protocol and clientHostName until moved to cli if deemed appropriate ???
            // temporary fix to remove agent user & password, for now map them to das user & password???
            AgentConfig agentConfig = new AgentConfig(agentName, getAgentPath(), adminUser, 
                adminPassword, new Integer(getAdminPort()), 
                adminHost, adminPort, adminUser, adminPassword, 
                masterPassword, 
                saveMasterPassword, agentProperties);                                                
            
            AgentManager manager = getAgentManager(agentConfig);                        
            
            //Create the node agent
            manager.createNodeAgent();                                               
            
            try {
                //If the DAS and Node Agent were created together on the same machine (i.e. by the 
                //installer), the we attempt to pre-register the new node agent's config directly 
                //in the domain.xml of the domain.
                rendezvousWithDAS(agentConfig, agentProperties);
            } catch (Exception ex) {
                try {
                    //if we could not register, then  cleanup
                    manager.deleteNodeAgent();
                } catch (Exception ex2) {
                    //eat any exceptions occuring during cleanup time.                    
                }
                throw ex;
            }
            
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
        } catch (Exception e) {                        
            if (e.getLocalizedMessage() != null)
                CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());

            throw new CommandException(getLocalizedString(
                "CommandUnSuccessful", new Object[] {name} ), e);
        }
    }

    protected Properties getAgentProperties(final String propertyValues)
        throws CommandException, CommandValidationException 
    {
        Properties propertyList = new Properties();
        
        if (propertyValues == null) return propertyList;
        StringTokenizer st = new StringTokenizer(propertyValues, DELIMITER);
        while (st.hasMoreTokens()) {
            String propertyString = st.nextToken();
            while (st.hasMoreTokens() && propertyString.endsWith(Character.toString(ESCAPE_CHAR))) {
                //Escaped tokens such as d\:\Sun will become d:\Sun
                propertyString = propertyString.substring(0, propertyString.length() - 1);
                propertyString = propertyString.concat(DELIMITER + st.nextToken());
            }
            final int index = propertyString.indexOf(Character.toString(EQUAL_SIGN));
            if (index == -1)
                throw new CommandValidationException(_strMgr.getString("InvalidPropertySyntax"));
            final String propertyName = propertyString.substring(0, index);
            final String propertyValue = propertyString.substring(index+1);
            propertyList.put(propertyName, propertyValue);
        }
        CLILogger.getInstance().printDebugMessage("agent properties = " + propertyList);
        return propertyList;
    }
}
