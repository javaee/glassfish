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

import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.ee.admin.servermgmt.AgentManager;
import com.sun.enterprise.ee.admin.servermgmt.EEFileLayout;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.HttpListener;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.commands.S1ASCommand;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;


/**
 *
 * @author  kebbs
 */
public class StartNodeAgentCommand extends BaseNodeAgentCommand {
    
    private static final StringManager _strMgr = 
        StringManager.getManager(StartNodeAgentCommand.class);
    private static final String DAS_PROPERTY_FILE_NAME = "das.properties";
       
    /** Creates a new instance of DeleteNodeAgentCommand */
    public StartNodeAgentCommand() {
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
            
            //validate node agent before prompting
            manager.validateNodeAgent(agentConfig, true);

            if (!isNotRunning(manager,agentConfig)) {
                CLILogger.getInstance().printDetailMessage(_strMgr.getString("TargetAlreadyRunning", 
                                                                             new Object[] {agentName}));
                return;
            }
                //call getPortAndHost before getPassword()
            getPortAndHost(agentConfig);
            agentConfig.put(AgentConfig.K_DAS_USER, getUser());            
            agentConfig.put(AgentConfig.K_DAS_PASSWORD, getPassword());
            agentConfig.put(AgentConfig.K_MASTER_PASSWORD,
                            getMasterPasswordWithDefaultPrompt(manager, agentConfig));

	    //Validation of admin password can only be done after synchronization, see 6304850
            manager.validateMasterPassword(agentConfig, false);
            if (getOption(S1ASCommand.PASSWORDFILE) != null ) {
                 final String adminPwdAlias =
                        RelativePathResolver.getAlias( (String)agentConfig.get(AgentConfig.K_DAS_PASSWORD));
                 
                 if (adminPwdAlias!=null) {
                     final String masterPwd= (String)agentConfig.get(AgentConfig.K_MASTER_PASSWORD);
                     final String clearPwd = manager.getClearPasswordForAlias(agentConfig,masterPwd,adminPwdAlias);
                     agentConfig.put(AgentConfig.K_DAS_PASSWORD, clearPwd);
                 }
            }

            String[] extraPasswordOptions = manager.getExtraPasswordOptions(agentConfig);
            if (extraPasswordOptions != null) {
                agentConfig.put(AgentConfig.K_EXTRA_PASSWORDS, getExtraPasswords(extraPasswordOptions));
            }
            
            //System.out.println("adminUser=" + agentConfig.get(AgentConfig.K_DAS_USER) + " adminPassword=" +
            //    agentConfig.get(AgentConfig.K_DAS_PASSWORD) + " masterPassword=" +
            //    agentConfig.get(AgentConfig.K_MASTER_PASSWORD));                  

            // get override for start managed start instances
            String startInstancesOverride=getOption(START_INSTANCES_OVERRIDE);
            String syncInstancesOverride = getOption(SYNC_INSTANCES_OVERRIDE);
            CLILogger.getInstance().printDebugMessage("startInstanceOverride = " 
                + startInstancesOverride);
            CLILogger.getInstance().printDebugMessage("syncInstanceOverride = " 
                + syncInstancesOverride);
	    // add map entries for --verbose start-node-agent
	    if ( getBooleanOption("verbose") ) {
                // use domain constant, because that is where PEInstancesManager looks, for now
            agentConfig.put("domain.verbose", Boolean.TRUE);
	    }            
                       
            manager.startNodeAgent(startInstancesOverride, syncInstancesOverride);
            
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


        //  This api will get the port from node-agent's domain.xml and host from
        //  das.properties files.
        //  If domain.xml does not exist then use the port from asadminenv.conf.
        //  Any values set in asadminenv.conf is global to all command so no need
        //  to read this file to get the port.
        //  If port/host are available in the optionsList, then getPassword() will
        //  get the admin password from .asadminpass file.
    private void getPortAndHost(final AgentConfig ac)
    {
        final EEFileLayout efl = new EEFileLayout(ac);
        try {
            final File xmlFile = efl.getDomainConfigFile();
            if (xmlFile.exists()) {
                final ConfigContext cc  = ConfigFactory.createConfigContext(xmlFile.getAbsolutePath());
                        //get port from HttpListener in domain.xml
                final HttpListener admin = ServerHelper.getHttpListener(cc,
                                                    SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME,
                                                    ServerHelper.ADMIN_HTTP_LISTNER_ID);
                if (admin != null) {
                    final String port = admin.getPort();
                        //set port and host options so that password can be retrieved from .asadminpass
                    
                    setOption("port", port);
                }
            }
        }
        catch (Exception e) {
                //Ignore the exception
                //if there are any issues with reading the port then the port specified in
                // asadminenv.conf will be used.
        }
        try {
                //get the host from das.properties
            final File dasProperties = new File(efl.getConfigRoot(), DAS_PROPERTY_FILE_NAME);
            if (dasProperties.exists()) {
                final Properties prop = new Properties();
                prop.load(new BufferedInputStream(new FileInputStream(dasProperties)));
                final String host = prop.getProperty(AgentConfig.K_DAS_HOST);
                setOption("host", host);
            }
        }
        catch (Exception e) {
                //Ignore the exception
                //if there are any issues with reading the host then prompt the user for the
                //passwords on command line.
        }
    }
}
