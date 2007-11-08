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
 * AgentManager.java
 *
 * Created on August 13, 2003, 8:34 PM
 */

package com.sun.enterprise.ee.admin.servermgmt;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.*;
import java.util.*;

import com.sun.enterprise.util.JvmInfoUtil;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.servermgmt.StringValidator;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.servermgmt.InstanceException;

import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.InvalidConfigException;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;

import com.sun.enterprise.ee.security.NssStore;

import com.sun.enterprise.admin.common.Status;

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.util.TokenValueSet;

/**
 *
 * @author  kebbs
 */
public class AgentManager extends EEInstancesManager {                
    
    private static final StringManager _strMgr = 
        StringManager.getManager(AgentManager.class);
    
    /** Creates a new instance of AgentManager */
    public AgentManager(AgentConfig config) {        
        super(config);              
        setMessages(new RepositoryManagerMessages(
            _strMgr,
            "illegalAgentName", 
            "agentName", "agentRoot", "agentExists", 
            "agentDoesntExist", "agentDirNotValid", 
            "cannotDeleteAgentDir", "invalidAgentDir",
            "listAgentElement", "cannotDeleteAgent_invalidState",
            "agentStartupException", "cannotStartAgent_invalidState",
            "startAgentTimeOut","portConflict","agentStartupFailed",
            "cannotStopAgent_invalidState",
            "cannotStopAgent", "agentTimeoutStarting"));
    }        
    
    public void validateNodeAgent(AgentConfig config, boolean agentExists)
        throws AgentException
    {
        try {
            checkRepository(config, agentExists, agentExists);
        } catch (RepositoryException ex) {
            throw new AgentException(ex);
        }
    }     
    
    public void validateAdminUserAndPassword(AgentConfig config) 
        throws AgentException
    {
        try {
            validateAdminUserAndPassword(config, (String)config.get(AgentConfig.K_DAS_USER), 
                (String)config.get(AgentConfig.K_DAS_PASSWORD));
        } catch (RepositoryException ex) {            
            if (ex.getCause() != null && ex.getCause() instanceof ConfigException) {
                //Do nothing here. A config exception indicates that domain.xml has 
                //not yet been synchronized in which case we have no way to validate 
                //the admin user and password against the admin-keyfile file realm
                ;
            } else {
                throw new AgentException(ex);
            }
        }
    }
    
    /**
     * Validate the master password
     * @param config
     * @param mustValidate indicates whether we are expecting that the password alias exists.
     * It will not exist on initial synchronization only.
     * @throws AgentException
     */    
    public void validateMasterPassword(AgentConfig config, boolean mustValidate) 
        throws AgentException
    {
        //We only want to attempt password validation if the password alias keystore exists.
        //When starting the node agent for the first time (i.e. before it has rendezvous'd)
        //it will not have a keystore against which to validate.
        if (mustValidate || getFileLayout().getPasswordAliasKeystore().exists()) {
            try {
                validateMasterPassword(config, getMasterPasswordClear(config));
            } catch (RepositoryException ex) {
                throw new AgentException(ex);
            }
        }
    }
    @Override
    public String getNativeName()
    {
        return "appservAgent";
    }
    
    public void createNodeAgent() throws AgentException 
    {   
        try {
            getFileLayout().createRepositoryRoot();        
            new AgentConfigValidator().validate(getAgentConfig()); 
            checkRepository(getAgentConfig(), false);
        } catch (Exception ex) {          
            throw new AgentException(ex);
        } 
        
        try {            
            AgentConfig config = getAgentConfig();
            getEEFileLayout().createNodeAgentDirectories();
            createDASConfiguration();
            createNodeAgentConfiguration();
            createStartAgent();
            createStopAgent();            
            createServerPolicyFile(config);
            if (saveMasterPassword(config)) {
                createMasterPasswordFile(config, 
                    getMasterPasswordClear(config));
            }
            setPermissions(config);            
        } catch (AgentException ex) {
            FileUtils.liquidate(getAgentDir());
            throw ex;            
        } catch (Exception ex) {
            FileUtils.liquidate(getAgentDir());
            throw new AgentException(ex);
        } 
    }       
    
    public void deleteNodeAgent() 
        throws AgentException
    {               
        try {
            deleteRepository(getAgentConfig(), false);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }           
    
    public String[] listNodeAgents()
        throws AgentException
    {        
        try {
            return listRepository(getAgentConfig());
        } catch (Exception e) {
            throw new AgentException(e);
        }        
    }    
    
    protected RepositoryConfig getConfigForRepositoryStatus(RepositoryConfig config, 
        String repository)
    {
        //The repository here corresponds to the node agent name        
        return new RepositoryConfig(repository, 
            config.getRepositoryRoot(), config.getInstanceName());
        
    }
   
    public String[] listNodeAgentsAndStatusAsString()
        throws AgentException
    {        
        try {
            //TODO: Rename listDomainsAndStatusAsString to listRepositoryAndStatusAsString
            //when the PE branch is open
            return listDomainsAndStatusAsString(getAgentConfig());
        } catch (Exception e) {
            throw new AgentException(e);
        }        
    }    
    
    
    /**
     * This method starts the nodeagent via the EE/PE InstancesManager
     *
     * @throws AgentException
     */
    public void startNodeAgent() throws AgentException {                        
        startNodeAgent(null, null);
    }
    
    /**
     * This method takes an overloaded argument to start manangeed instances
     * which is designed to override the attribute set in domain.xml for the node-agent element
     *
     * @param startinstances override option
     * @throws AgentException
     */
    public void startNodeAgent(String startInstancesOverride, 
        String syncInstancesOverride) throws AgentException {
        try {
            // commandline args to append to command
            ArrayList<String> commandLineArgs = new ArrayList();
            if (startInstancesOverride != null) {
                // add in prefix to commandline argument so it know what it is about
                commandLineArgs.add(
                    IAdminConstants.NODEAGENT_STARTINSTANCES_OVERRIDE + 
                    "=" +startInstancesOverride);
            }
            if (syncInstancesOverride != null) {
                commandLineArgs.add(
                    IAdminConstants.NODEAGENT_SYNCINSTANCES_OVERRIDE + 
                    "=" + syncInstancesOverride);
            }
            // set interativeOptions for security to hand to starting process from ProcessExecutor
            RepositoryConfig config=getConfig();
            String[] options = getInteractiveOptions(
                (String)config.get(AgentConfig.K_DAS_USER), 
                (String)config.get(AgentConfig.K_DAS_PASSWORD),
                (String)config.get(AgentConfig.K_MASTER_PASSWORD),
                (HashMap)config.get(AgentConfig.K_EXTRA_PASSWORDS));
            String[] commLineArgs = new String[commandLineArgs.size()];
            for (int i = 0; i < commandLineArgs.size(); i++)
                commLineArgs[i] = commandLineArgs.get(i);
            startInstance(options, commLineArgs, getEnvProps());            
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }
    
    
    public void stopNodeAgent() throws AgentException
    {                        
        try {              
            stopInstance();            
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }    

    public void stopNodeAgentForcibly(int timeout) throws AgentException
    {                        
        try {   
            
            boolean stopped = false;
            
            if(timeout > 0)
                stopped = stopInstanceWithinTime(timeout);
            
            if (!stopped) {
                System.out.println("Killing Forcibly...");
                killRelatedProcesses();                
            }            
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }    
    
    protected List<String> getPIDsToBeKilled() throws InstanceException 
    {
        List<String> pids = new ArrayList<String>();
        pids = super.getPIDsToBeKilled(); // we got the node agent's pid
        // Now let us get its managed servers' pids one by one

        // first get managed server list
        File agentDir = getAgentDir();
        File[] serverDirs = agentDir.listFiles(
            new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.equalsIgnoreCase("agent")) return false;
                    return true;
                }
            });

        for (File serverDir : serverDirs) {
            int serverPid = getServerPID(serverDir);
            if (serverPid != NO_PROCESS) pids.add("" + serverPid);
        }
        return pids;
    }

    protected int getServerPID(File serverDir) 
    {
        File pidFileDir = new File(serverDir, "config"); 
        File pidFile = new File(pidFileDir, SystemPropertyConstants.PID_FILE);

        // check if file has latest pid
        if (! isPIDFileValid(pidFileDir, pidFile)) return NO_PROCESS;

        return JvmInfoUtil.getPIDfromFileAndDelete(pidFile);
    }
    
    protected boolean isValidRepository(File f) 
    {
        return new File(new File(new File(f, AgentConfig.AGENT_INSTANCE_NAME),
            PEFileLayout.BIN_DIR), PEFileLayout.START_SERV_OS).exists();
    }
      
    protected boolean isValidRepository(RepositoryConfig config) {
        return getFileLayout(config).getStartServ().exists();
    }
              
    /**
     * Changes the master password for the node agent
     */    
    public void changeMasterPassword(AgentConfig config) throws AgentException
    {        
        String oldInstanceName = config.getInstanceName();
        try {       
            //Ensure that the entity is stopped
            final int status = getInstancesManager(config).getInstanceStatus();
            if (status != Status.kInstanceNotRunningCode) {
                throw new AgentException(
                   _strMgr.getString("cannotChangePassword_invalidState",
                        config.getDisplayName(), Status.getStatusString(status)));
            }
            
            String newPass = getNewMasterPasswordClear(config);
            String oldPass = getMasterPasswordClear(config);
            boolean saveMasterPassword = saveMasterPassword(config);                           
            
            //Change the password in the masterpassword file or delete the file if it is 
            //not to be saved.
            changeMasterPasswordInMasterPasswordFile(config, newPass, saveMasterPassword(config));
            
            //Change the password of the keystore alias file. This is necessary
            //because we validate that the master password is correct by 
            //opening this keystore.
            changePasswordAliasKeystorePassword(config, oldPass, newPass);
            
            //Change the password of the NSS databasee
            EEDomainsManager mgr = new EEDomainsManager();            
            mgr.changeSSLCertificateDatabasePassword(config, oldPass, newPass);
            
            //Now change the passwords for each of the server instances managed by the
            //node agent. This is necessary since the server instance do not synchronize
            //when the node agent starts. (They are only synchronized when explicitly 
            //started or stopped.)
            EEInstancesManager eeInstancesManager = (EEInstancesManager)mgr.getInstancesManager(config);
            String[] instances = eeInstancesManager.listInstances();               
            for (int i = 0; i < instances.length; i++) {                
                config.setInstanceName(instances[i]);
                mgr.changeSSLCertificateDatabasePassword(config, oldPass, newPass);
                changePasswordAliasKeystorePassword(config, oldPass, newPass);                                
            }        
        } catch (Exception ex) {        
            throw new AgentException(
                _strMgr.getString("masterPasswordNotChanged"), ex);
        } finally {
            //We have side effected the config passed in, and as such we must ensure to restore it.
            config.setInstanceName(oldInstanceName);
        }
    }

    private void createDASConfiguration() throws AgentException
    {
        final String dasHost = (String)getAgentConfig().get(AgentConfig.K_DAS_HOST);
        final String dasPort = (String)getAgentConfig().get(AgentConfig.K_DAS_PORT);
        final String dasUser = (String)getAgentConfig().get(AgentConfig.K_DAS_USER);
        final String dasPassword = (String)getAgentConfig().get(AgentConfig.K_DAS_PASSWORD);
        //DAS configuration is optional. Only write if host is exists
        if (dasHost != null) {            
            try {
                (new DASPropertyReader(getAgentConfig())).write();
            } catch (Exception ex) {
                throw new AgentException(_strMgr.getString("dasConfigurationNotCreated"), ex);
            }
        }
    }


    private void createNodeAgentConfiguration() throws AgentException
    {
        // node agent properties are required, so write 
        try {
            (new NodeAgentPropertyReader(getAgentConfig())).write();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AgentException(_strMgr.getString("nodeAgentConfigurationNotCreated"), ex);
        }
    }


    private void createStopAgent() throws AgentException
    {
        try {
            final TokenValueSet tokens = EEScriptsTokens.getTokenValueSet(getAgentConfig());
            final File stopAgentTemplate = getEEFileLayout().getStopAgentTemplate();
            final File stopAgent = getEEFileLayout().getStopAgent();
            generateFromTemplate(tokens, stopAgentTemplate, stopAgent);
            //final File killServ = getEEFileLayout().getKillServTemplate(); 
            //generateFromTemplate(new TokenValueSet(), 
            //                     getEEFileLayout().getKillServTemplate(), 
            //                     getEEFileLayout().getKillServ());                        
        } catch (Exception e) {
            throw new AgentException(
                _strMgr.getString("stopAgentNotCreated"), e);
        }
    }      
       
    private void createStartAgent() throws AgentException
    {
        try {
            final TokenValueSet tokens = EEScriptsTokens.getTokenValueSet(getAgentConfig());
            final File startAgentTemplate = getEEFileLayout().getStartAgentTemplate();
            final File startAgent = getEEFileLayout().getStartAgent();
            generateFromTemplate(tokens, startAgentTemplate, startAgent);
        } catch (Exception e) {
            throw new AgentException(
                _strMgr.getString("startAgentNotCreated"), e);
        }
    }      

    private String getAgentUser() 
    {
        return ((String)getAgentConfig().get(AgentConfig.K_USER));
    }
    
    /** Returns the domain user's password in cleartext from the domainConfig.
     *  @param Map that represents the domain configuration
     *  @return String representing the domain user password if the 
     *  given map contains it, null otherwise
    */

    private String getAgentPasswordClear() 
    {
        return ((String)getAgentConfig().get(AgentConfig.K_PASSWORD));
    }       
    
    protected File getRepositoryRootDir(RepositoryConfig config)
    {
        return getFileLayout(config).getRepositoryRootDir();
    }
    
    protected File getRepositoryDir(RepositoryConfig config)
    {
        return getFileLayout(config).getRepositoryDir().getParentFile();
    }

    private File getAgentDir()
    {        
        return getRepositoryDir(getAgentConfig());
    }
    
    private File getAgentRoot()
    {
        return getRepositoryRootDir(getAgentConfig());
    }
       
    private AgentConfig getAgentConfig()
    {
        return (AgentConfig)getConfig();
    }
    
    private String getMasterPasswordClear(AgentConfig config)
    {
        return ((String)config.get(AgentConfig.K_MASTER_PASSWORD));
    }
    
    private boolean saveMasterPassword(AgentConfig config) {
        Boolean b = (Boolean)config.get(AgentConfig.K_SAVE_MASTER_PASSWORD);
        return b.booleanValue();
    }
    protected static String getNewMasterPasswordClear(AgentConfig config)
    {
        return ((String)config.get(AgentConfig.K_NEW_MASTER_PASSWORD));
    }       
        
    public String[] getExtraPasswordOptions(AgentConfig config)
        throws AgentException
    {                
        final EEFileLayout layout = (EEFileLayout)getFileLayout(config);          
        File nssDb = layout.getNSSCertDBFile();
        if (nssDb.exists()) {
            try {                
                //We only want to make these calls if the Nss database already exists;
                //otherwise, this will cause an NSS database to be created 
                //potentially with a bad master password (i.e. a master password which                 
                //is different that that of the NSS database in the domain). This 
                //results in an unusable Node Agent.
                NssStore nssStore = NssStore.getInstance(
                    nssDb.getParentFile().getAbsolutePath(),
                    false, getMasterPasswordClear(config));
                String[] result = nssStore.getTokenNamesAsArray(); 
                //We must explicitly close the NSS database so that it is not in use during 
                //synchronization of the Node Agent.
                NssStore.closeInstance();

                return result;
            } catch (Exception ex) {
                throw new AgentException(ex);
            }     
        }
        return null;
    }
    
    @Override
    protected Properties getEnvProps() {
        // this is where we get the info that used to be buried in the startserv script.
        // this will eventually be passed to PEInstancesManager to set in System once a copy
        // of the existing SystemProps is saved...
        RepositoryConfig cfg = getConfig();
        Properties p = new Properties();
        
        p.setProperty("com.sun.aas.processName", "s1as8-nodeagent");
        p.setProperty("com.sun.aas.instanceName", cfg.getRepositoryName());
        p.setProperty("com.sun.aas.instanceRoot", cfg.getRepositoryRoot() + File.separator + cfg.getRepositoryName() + File.separator + "agent");

        return p;
    }
}




