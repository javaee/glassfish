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

package com.sun.enterprise.ee.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.DomainConfig;

import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.servermgmt.pe.PEInstancesManager;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.admin.servermgmt.pe.InstanceTimer;
import com.sun.enterprise.admin.servermgmt.pe.TimerCallback;
import com.sun.enterprise.ee.admin.servermgmt.EEScriptsTokens;

import com.sun.enterprise.admin.util.TokenValueSet;
import com.sun.enterprise.admin.util.TokenValue;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.common.Status;

import com.sun.enterprise.security.store.IdentityManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;

/**
 */
public class EEInstancesManager extends PEInstancesManager
{
    /**
     * i18n strings manager object
     */
    private static final StringManager _strMgr = 
        StringManager.getManager(EEInstancesManager.class);   
    private Properties _overridingProperties=null;
    
    public EEInstancesManager(RepositoryConfig config)
    {
        super(config);       
        _fileLayout = new EEFileLayout(config);
        setMessages(new RepositoryManagerMessages(
            _strMgr,
            "illegalInstanceName", 
            "instanceName", "instanceRoot", "instanceExists", 
            "instanceDoesntExist", "instanceDirNotValid", 
            "cannotDeleteInstanceDir", "invalidInstanceDir",
            "listInstanceElement", "cannotDeleteInstance_invalidState",
            "instanceStartupException", "cannotStartInstance_invalidState",
            "startInstanceTimeOut", "portConflict","instanceStartupFailed",
            "cannotStopInstance_invalidState",
            "cannotStopInstance", "instanceTimeoutStarting"));
    }

    public EEInstancesManager(RepositoryConfig config, 
        RepositoryManagerMessages messages)
    {
        this(config);
        setMessages(messages);
    }

    @Override
    public String getNativeName()
    {
        return "appserv";
    }

    protected InstanceConfig getInstanceConfig() {
        return (InstanceConfig)getConfig();
    }
    
    
    /**
    * This method is called internally from the startInstance method
    * and was needed so SE could execute a process that doesn't return
    */
    @Override
    protected ProcessExecutor startInstanceExecute(String[] command, String[] interativeOptions) throws InstanceException {
        try
        {
            ProcessExecutor exec=new ProcessExecutor(command, interativeOptions);
            // call execute so no output lines are returned and
            // process does not have a time limit to start
            exec.execute(false, false);
            return exec;
        }
        catch (Exception e)
        {
            throw new InstanceException(_strMgr.getString("procExecError"), e);
        }
    }

    
    
    public void createInstance() throws InstanceException
    {
        try {
            checkRepository(getInstanceConfig(), false);            
        } catch (RepositoryException ex) {
            throw new InstanceException(ex);
        }
        
        try {
            getEEFileLayout().createServerInstanceDirectories();       
            createStartInstance();
            createStopInstance();
            setPermissions(getConfig());
        } catch (InstanceException ex) {
            FileUtils.liquidate(getInstanceDir());
            throw ex;
        } catch (Exception ex) {
            FileUtils.liquidate(getInstanceDir());
            throw new InstanceException(ex);
        }                
    }

    private String getJbiInstanceName() {
        InstanceConfig config = getInstanceConfig();
        if (config.getInstanceName() == null) {
            return config.getRepositoryName();
        } else {
            return config.getInstanceName();
        }
    }
    
    public void deleteInstance() 
        throws InstanceException
    {               
        deleteInstance(true);
    }   
    
    
        public void deleteInstance(boolean deleteJMSProvider) 
        throws InstanceException
    {               
        try {
            deleteRepository(getInstanceConfig(), deleteJMSProvider);
        } catch (Exception e) {
            throw new InstanceException(e);
        }
    }                  
    
    public String[] listInstances() 
        throws InstanceException    
    {        
        try {
            return listRepository(getConfig());
        } catch (Exception e) {
            throw new InstanceException(e);
        }        
    }    
    
    protected RepositoryConfig getConfigForRepositoryStatus(RepositoryConfig config, 
        String repository)
    {
        //The repository here corresponds to the instance name                
        return new RepositoryConfig(config.getRepositoryName(), 
            config.getRepositoryRoot(), repository);        
    }
       
    public String[] listInstancesAndStatusAsString() throws InstanceException
    {
        try {
            //TODO: Rename listDomainsAndStatusAsString to listRepositoryAndStatusAsString
            //when the PE branch is open
            return listDomainsAndStatusAsString(getConfig());
        } catch (Exception e) {
            throw new InstanceException(e);
        }     
    }           
    
    public Process startInstance() throws InstanceException
    {                        
        try {
            checkRepository(getConfig());
            
            String[] commandLineArgs=null;
            // if the nodeagent is running in verbose mode, pass verbose as a argument to the startserv script
            
            /* WBN April 2007 Issue 2605 -- do NOT start the instance in verbose mode.
            String verboseMode=System.getProperty("com.sun.aas.verboseMode", "false");
             if (verboseMode.equals("true")) {
                 // add in verbose command line
                 commandLineArgs=new String[]{"verbose"};
             }
             */
             // set interativeOptions for security to hand to starting process from ProcessExecutor
            // since SE/EE and IndentityManager is already populated, use it for passing values to the instance
            return super.startInstance(IdentityManager.getIdentityArray(), commandLineArgs, getEnvProps());            
        } catch (Exception e) {
            throw new InstanceException(e);
        }
    }

    public void stopInstance() throws InstanceException
    {                        
        try {
            checkRepository(getConfig());            
            super.stopInstance();            
        } catch (Exception e) {
            throw new InstanceException(e);
        }
    }    

    protected EEFileLayout getEEFileLayout()
    {
        return (EEFileLayout)_fileLayout;
    }
    
    protected PEFileLayout getFileLayout()
    {
        return _fileLayout;
    }
    
    protected PEFileLayout getFileLayout(RepositoryConfig config)
    {        
        return _fileLayout;
    }        
        
    private void createStartInstance() throws InstanceException
    {
        try {
            final TokenValueSet tokens = EEScriptsTokens.getTokenValueSet(getInstanceConfig());
            
            // add/overwrite tokens for domain and cluster tokens to appropriate value for instance
            Properties props=getOverridingProperties();
            if (props != null) {
                Iterator iter=props.keySet().iterator();
                String key="";
                TokenValue tv=null;
                while(iter.hasNext()) {
                    key=(String)iter.next();
                    tv = new TokenValue(key, props.getProperty(key));
                    tokens.add(tv);            
                }
            }
            
            generateFromTemplate(tokens, getEEFileLayout().getStartInstanceTemplate(), 
                getEEFileLayout().getStartInstance());
        } catch (Exception e) {
            throw new InstanceException(
                _strMgr.getString("startInstanceNotCreated"), e);
        }
    }  
    
    private void createStopInstance() throws InstanceException
    {
        try {
            final TokenValueSet tokens = 
                EEScriptsTokens.getTokenValueSet(getInstanceConfig());            
            generateFromTemplate(tokens, 
                getEEFileLayout().getStopInstanceTemplate(), 
                getEEFileLayout().getStopInstance());
            //final File killServ = getEEFileLayout().getKillServTemplate(); 
            //generateFromTemplate(new TokenValueSet(), 
            //                     getEEFileLayout().getKillServTemplate(), 
            //                     getEEFileLayout().getKillServ());            
        } catch (Exception e) {
            throw new InstanceException(
                _strMgr.getString("stopInstanceNotCreated"), e);
        }
    }  
        
    protected boolean isValidRepository(File f) 
    {        
        if (f.getName().equals(AgentConfig.AGENT_INSTANCE_NAME)) {
            return false;
        } else {
            return super.isValidRepository(f);
        }
    }

    protected boolean isValidRepository(RepositoryConfig config) {
        // instance name will be null on the start of the domain
        String instanceName=config.getInstanceName();
        if (instanceName != null && instanceName.equals(AgentConfig.AGENT_INSTANCE_NAME)) {
            return false;
        } else {            
            return super.isValidRepository(config);
        }
    }
    
    public InstancesManager getInstancesManager(RepositoryConfig config) 
    {
        return new EEInstancesManager(config);
    } 
    
    
    /**
     * setOverridingProperties - set properties to replace tokens in scripts
     */
    public void setOverridingProperties(Properties props) {
        _overridingProperties=props;
    }
    public Properties getOverridingProperties() {
        return _overridingProperties;
    }

    
    
    private File getInstanceDir()
    {        
        return getFileLayout().getRepositoryDir();
    }       
    
    protected File getRepositoryDir(RepositoryConfig config)
    {
        return getFileLayout(config).getRepositoryDir();
    }  
     
    protected File getRepositoryRootDir(RepositoryConfig config)
    {
        return getFileLayout(config).getRepositoryDir().getParentFile();
    }
    
    protected Properties getEnvProps()
    {
        // this is where we get the info that used to be buried in the startserv script.
        // this will eventually be passed to PEInstancesManager to set in System once a copy
        // of the existing SystemProps is saved...
        RepositoryConfig cfg = getConfig();
        Properties p = new Properties();

        p.setProperty("com.sun.aas.instanceRoot", cfg.getRepositoryRoot() + File.separator + cfg.getRepositoryName() + File.separator + cfg.getInstanceName());
        p.setProperty("com.sun.aas.launcherReturn", "return");
        p.setProperty("com.sun.aas.instanceName", cfg.getInstanceName());
        p.setProperty("com.sun.aas.processName", "as9-server");
        p.setProperty("com.sun.aas.processLauncher", "SE");
        p.setProperty("com.sun.aas.limitedCommamdExecution", "true"); 
        
        return p;
    }
   
}
