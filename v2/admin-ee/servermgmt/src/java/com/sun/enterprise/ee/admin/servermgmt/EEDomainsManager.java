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
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;
import java.security.SecureRandom;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.OS;

import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.InstancesManager;

import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.admin.util.TokenValueSet;
import com.sun.enterprise.admin.util.LineTokenReplacer;
import com.sun.enterprise.admin.util.IAdminConstants;


import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.NodeAgent;

import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.ExecException;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.ee.security.NssStore;


/**
 * The EEDomainsManager is used for SE/EE domain creation. Currently, the
 * only differentiation from PE is that templates (e.g. for domain.xml,
 * asadmin, etc) differ from PE and as such reside in a different directory.
 */

public class EEDomainsManager extends PEDomainsManager implements IAdminConstants
{  
    private static final String TMP_FILENAME = "certutiltmp";
    
    /**
     * i18n strings manager object
     */
    private static final StringManager _strMgr = 
        StringManager.getManager(EEDomainsManager.class);   

    
    protected class CertutilExecutor extends ProcessExecutor {            
                
        public CertutilExecutor(String[] args, long timeoutInSeconds, File workingDir)
        {
            super(args, timeoutInSeconds, null, null, workingDir);
            setExecutionRetentionFlag(true);
            addCertutilCommand();
        }        
        
        private void addCertutilCommand() {
            if (!mCmdStrings[0].equals(CERTUTIL_CMD)) {
                String[] newArgs = new String[mCmdStrings.length + 1];
                newArgs[0] = CERTUTIL_CMD;
                System.arraycopy(mCmdStrings, 0, newArgs, 1, mCmdStrings.length);
                mCmdStrings = newArgs;
            }
        }
        
        public void execute(String keystoreErrorMsg, File keystoreName) throws DomainException
        {
            try {
                super.execute();                    
                if (getProcessExitValue() != 0) {
                    throw new DomainException(_strMgr.getString(keystoreErrorMsg, keystoreName) +
                        getLastExecutionError() + " " +  getLastExecutionOutput());
                }
            } catch (ExecException ex) {                        
                throw new DomainException(_strMgr.getString(keystoreErrorMsg, 
                    keystoreName) + getLastExecutionError() + " " +  getLastExecutionOutput(), ex);
            }               
        }
    }
    
    /**
     * Constructor for EEDomainsManager
     */
    public EEDomainsManager()
    {
        super();
    }

     //PE does not require that an admin user / password is available at start-domain time.
    //SE/SEE does require it.
    public BitSet getDomainFlags()
    {
        BitSet bs = new BitSet();        
        bs.set(DomainConfig.K_FLAG_START_DOMAIN_NEEDS_ADMIN_USER, true);
        return bs;
    }
    
    public void deleteDomain(DomainConfig domainConfig) 
        throws DomainException
    {               
        try {
            //Validate that the domain contains no node agents
            ConfigContext configContext = getConfigContext(domainConfig);
            NodeAgent[] agents = NodeAgentHelper.getNodeAgentsInDomain(configContext);
            if (agents.length > 0) {
                throw new DomainException(_strMgr.getString("nodeAgentsExist"));
            }
        } catch (DomainException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DomainException(ex);
        }
        super.deleteDomain(domainConfig);
    }

    /**
     *  creates an EE Domain
     */    
    public synchronized void createDomain(DomainConfig domainConfig) 
            throws DomainException
    {
        super.createDomain(domainConfig);
        try {            
            String oldConfigName = domainConfig.getConfigurationName();
            domainConfig.setConfigurationName(DEFAULT_CONFIGURATION_NAME);
            createConfiguration(domainConfig);
            domainConfig.setConfigurationName(oldConfigName);
        } catch (DomainException de) {
            FileUtils.liquidate(getDomainDir(domainConfig));
            throw de;
        } catch (Exception ex) {
            FileUtils.liquidate(getDomainDir(domainConfig));
            throw new DomainException(ex);
        }
    }
   
    public synchronized void copyConfigururation(RepositoryConfig config, 
        String sourceConfig, String targetConfig) throws IOException
    {        
        EEFileLayout layout = (EEFileLayout)getFileLayout(config);
        File configRoot = layout.getConfigRoot();
        File sourceDir = new File(configRoot, sourceConfig);
        File targetDir = new File(configRoot, targetConfig);        
        FileUtils.copyTree(sourceDir, targetDir);
    }
    
    public synchronized void deleteConfigururation(RepositoryConfig config, 
        String configName) throws IOException
    {        
        EEFileLayout layout = (EEFileLayout)getFileLayout(config);
        File configRoot = layout.getConfigRoot();
        File configDir = new File(configRoot, configName);        
        FileUtils.liquidate(configDir);
    }
    
    public synchronized void createConfiguration(DomainConfig domainConfig) throws RepositoryException
    {       
        EEFileLayout layout = (EEFileLayout)getFileLayout(domainConfig);
        layout.createConfigurationDirectories();                     
    }     

    /**
     * Creates the NSS database and everything associated with it. This method
     * overrides that defined in PEDomainsManager.
     * @param config
     * @param masterPassword
     * @throws RepositoryException
     */    
    protected void createSSLCertificateDatabase(RepositoryConfig config, 
        String masterPassword) throws RepositoryException    
    {
        String msg = null;
        if (domainUsesNSS(config)) {
            if (! isNSSSupportAvailable()) {
                reportMissingNss();
            }
            msg = _strMgr.getString("securityStoreType", "NSS");
            System.out.println(msg);
            createNSSCertDB(config, masterPassword);
            initializeNSSCertDB(config, masterPassword);
            addCertToAsadminKeystore(config);
        }
        else {
            msg = _strMgr.getString("securityStoreType", "JKS");
            System.out.println(msg);
            super.createSSLCertificateDatabase(config, masterPassword);   
            //no need to add certificate in .asadmintrustore as 
            //the listeners are configured to be insecure in this case.
        }
    }

    /**
     * Creates a temporary password file containing the master password and suitable
     * for consumption by certutil with permission 600 (at least on Unix systems)
     * @param domainConfig -- the K_MASTER_PASSWORD entry must exist
     * @param configDirectory -- the directory where the temp file will be created.
     * @throws DomainException
     * @return The File corresponding to the temporary password file.
     */    
    private File createTemporaryPasswordFile(RepositoryConfig config,
        String masterPassword, File configDirectory) throws DomainException
    {
        FileWriter writer = null;
        File pwdFile = null;        
        try {
            pwdFile = File.createTempFile(TMP_FILENAME, null, configDirectory);                        
            pwdFile.deleteOnExit();            
            writer = new FileWriter(pwdFile);
            writer.write(masterPassword);
            writer.close();
            writer = null;
            chmod("600", pwdFile);
            return pwdFile;
        } catch (IOException ex) {
            // ensure that we delete the file should any exception occur
            if (pwdFile != null) {
                try {
                    pwdFile.delete();
                } catch (Exception ex2) {
                    //ignore we are cleaning up on error
                }
            }
            throw new DomainException(_strMgr.getString("tempFileNotCreated", pwdFile),
                ex);
        } finally {
            //ensure that we close the file no matter what.
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex2) {
                    //ignore we are cleaning up on error
                }                
            }
        }
    }       
    
    /**
     * Creates a temporary noise file containing the master password and suitable
     * for consumption by certutil with permission 600 (at least on Unix systems).
     * The noise file contains random data and is used for key generation.
     * @param configDirectory -- the directory where the noise file will be created.
     * @throws DomainException
     * @return
     */    
    private File createTemporaryNoiseFile(File configDirectory) 
        throws DomainException
    {        
        File noiseFile = null;
        BufferedOutputStream writer = null;
        //Generate a random buffer
        SecureRandom r = new SecureRandom();
        byte[] buffer = new byte[2048];
        r.nextBytes(buffer);        
        try {            
            noiseFile = File.createTempFile(TMP_FILENAME, null, configDirectory);                    
            noiseFile.deleteOnExit();            
            writer = new BufferedOutputStream(new FileOutputStream(noiseFile));            
            writer.write(buffer);
            writer.flush();
            writer.close();
            writer = null;
            chmod("600", noiseFile);
            return noiseFile;
        } catch (IOException ex) {
            // ensure that we delete the file should any exception occur
            if (noiseFile != null) {
                try {
                    noiseFile.delete();
                } catch (Exception ex2) {
                    //ignore we are cleaning up on error
                }
            }
            throw new DomainException(_strMgr.getString("tempFileNotCreated", noiseFile),
                ex);
        } finally {
            //ensure that we close the file no matter what.
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex2) {
                    //ignore we are cleaning up on error
                }
            }
        }
    }
       
    /**
     * Create the initial NSS certificate db consisting of (cert8.db, key3.db). 
     * This is copied from the templates directory and contains verisign certs which
     * are initially trusted.
     * @param domainConfig
     * @throws DomainException
     */    
    protected void createNSSCertDB(RepositoryConfig config,
        String masterPassword) throws DomainException
    {        
        final EEFileLayout layout = (EEFileLayout)getFileLayout(config);           
        try {
            FileUtils.copy(layout.getNSSCertDBTemplate(), layout.getNSSCertDBFile());
            FileUtils.copy(layout.getNSSKeyDBTemplate(), layout.getNSSKeyDBFile());
            NssStore store = NssStore.getInstance(
                layout.getNSSCertDBFile().getParentFile().getAbsolutePath(),
                false, DEFAULT_MASTER_PASSWORD);            
            store.changePassword(DEFAULT_MASTER_PASSWORD, masterPassword);  
            NssStore.closeInstance();
        } catch (Exception e) {
            throw new DomainException(
                _strMgr.getString("certDBInitializationFailed", layout.getConfigRoot()), 
                    e);
        }       
    }       
    
    protected void addCertToAsadminKeystore(RepositoryConfig config) 
        throws DomainException
    {
        final EEFileLayout layout = (EEFileLayout)getFileLayout(config);                
        final File configDir = layout.getConfigRoot();        
        File certFile = new File(configDir, CERTIFICATE_ALIAS + ".cer");   
        try {                        
            //Export the s1as cert to a temporary file.
            final String[] certutilCmd = {                
                "-L",                
                "-a", 
                "-n", CERTIFICATE_ALIAS,  
                "-d", configDir.getAbsolutePath(),
                "-o", certFile.getAbsolutePath(),
            };                                  
            
            //Note: we must use configDir as the current working directory since certutil 
            //requires that the current directory be writeable.
            CertutilExecutor p = new CertutilExecutor(certutilCmd, 60, configDir);              
            p.execute("certDBInitializationFailed", configDir);                        
            
            //import the newly created certificate into the asadmin truststore
            addToAsadminTrustStore(config, certFile);
            
            certFile.delete();
            certFile = null;            
        } catch (RepositoryException ex) {                        
            throw new DomainException(_strMgr.getString("certDBInitializationFailed", configDir),
                ex);
        } finally {
            if (certFile != null) {                
                try {
                    certFile.delete();
                } catch (Exception ex2) {
                    //ignore cleaning up.
                }
            }
        }
    }
    
    /**
     * Initialize the NSS certificate database by generating a self signed certificate
     * for the "s1as" alias.
     * @param domainConfig
     * @throws DomainException
     */    
    protected void initializeNSSCertDB(RepositoryConfig config, String masterPassword) 
        throws DomainException
    {        
        File pwdFile = null;
        File noiseFile = null;
        final EEFileLayout layout = (EEFileLayout)getFileLayout(config);                
        final File configDir = layout.getConfigRoot();        
        try {
            
            pwdFile = createTemporaryPasswordFile(config, masterPassword, configDir);     
            noiseFile = createTemporaryNoiseFile(configDir);            
            final String[] certutilCmd = {                
                "-S",                
                "-x", 
                "-n", CERTIFICATE_ALIAS,
                "-t", "u,u,u",
                "-v", "120", 
                "-s", getCertificateDN(config.getDisplayName()),
                "-d", configDir.getAbsolutePath(),            
                "-f", pwdFile.getAbsolutePath(),
                "-z", noiseFile.getAbsolutePath()
            };                                  
            
            //Note: we must use configDir as the current working directory since certutil 
            //requires that the current directory be writeable.
            CertutilExecutor p = new CertutilExecutor(certutilCmd, 60, configDir);              
            p.execute("certDBInitializationFailed", configDir);
            pwdFile.delete();
            pwdFile = null;                        
        } finally {
            if (pwdFile != null) {                
                try {
                    pwdFile.delete();
                } catch (Exception ex2) {
                    //ignore cleaning up.
                }
            }
            if (noiseFile != null) {                
                try {
                    noiseFile.delete();
                } catch (Exception ex2) {
                    //ignore cleaning up.
                }
            }
        }
    }
    
    // This method starts the domain for SE/EE.  This method was overridden so the DAS would act exactly
    // like PE.  This DomainManager overrides the getInstancesManager method to replace the PEInstancesManager
    // with the EE one, which is correct behavoir except when starting the DAS.
    public void startDomain(DomainConfig domainConfig) throws DomainException
    {                        
        try {
            checkRepository(domainConfig);
            // set interativeOptions for security to hand to starting process from ProcessExecutor
            String[] options = getInteractiveOptions(
                (String)domainConfig.get(DomainConfig.K_USER), 
                (String)domainConfig.get(DomainConfig.K_PASSWORD),
                (String)domainConfig.get(DomainConfig.K_MASTER_PASSWORD),
                (HashMap)domainConfig.get(DomainConfig.K_EXTRA_PASSWORDS));            
            // keeping to the thinking that DAS is PE, use PEInstancesManager settings to start DAS
            // SE/EE server instances need to synchronize, which take more time to start, so new timeouts
            // where needed, which makes the asadmin command wait a long time if a startup error occurs
            // like a bad jvm option is entered
            super.getInstancesManager(domainConfig).startInstance(options, (String[])null, getEnvProps(domainConfig));            
        } catch (Exception e) {
            throw new DomainException(e);
        }
    }
    
    
    /**
     * Changes the SSL certificate database (NSS) password. This method overrides that 
     * defined in PEDomainsManager since SE/EE use NSS and PE use Java keystores.
     * @param config
     * @param oldPassword
     * @param newPassword
     * @throws RepositoryException
     */    
    protected void changeSSLCertificateDatabasePassword(RepositoryConfig config,
        String oldPassword, String newPassword) throws RepositoryException
    {
        final EEFileLayout layout = (EEFileLayout)getFileLayout(config); 
        try {      
            if(!domainUsesNSS(config)) {
                super.changeSSLCertificateDatabasePassword(config, oldPassword, newPassword);   
            } else {
                if (! isNSSSupportAvailable()) {
                    reportMissingNss();
                }
                String dbdir = layout.getNSSCertDBFile().getParentFile().getAbsolutePath();            
                NssStore store = NssStore.getInstance(dbdir, false, oldPassword);      
                store.changePassword(oldPassword, newPassword);
                NssStore.closeInstance();
            }
        } catch (Exception ex) {
            throw new DomainException(_strMgr.getString("masterPasswordNotChanged"), ex);
        }
    }
    
    /**
     * Changes the master password for the domain
     */    
    public void changeMasterPassword(DomainConfig config) throws DomainException
    {          
        super.changeMasterPassword(config);        
    }
    
    // overidden method for RepositorManager needs exact signature
    public InstancesManager getInstancesManager(RepositoryConfig config) 
    {
        return new EEInstancesManager(config);
    }   
 
 
    public AgentManager getAgentManager(AgentConfig agentConfig) {
        return new AgentManager(agentConfig);
        
    }
    
    protected PEFileLayout getFileLayout(RepositoryConfig config)
    {
        if (_fileLayout == null) {            
            _fileLayout = new EEFileLayout(config);                
        }
        return _fileLayout;        
    }            

    protected TokenValueSet getDomainXmlTokens(DomainConfig domainConfig) {
        return EEDomainXmlTokens.getTokenValueSet(domainConfig);
    }

    public String[] getExtraPasswordOptions(DomainConfig config)
        throws DomainException
    {
         //return if we are using JKS. Extra password options not applicable
         //in this case.
         if(!domainUsesNSS(config)) {
             return null;
         }
         if (! isNSSSupportAvailable()) {
             reportMissingNss();
         }
         final EEFileLayout layout = (EEFileLayout)getFileLayout(config); 
         try {
            NssStore nssStore = NssStore.getInstance(
                layout.getNSSCertDBFile().getParentFile().getAbsolutePath(),
                false, getMasterPasswordClear(config));
            String[] result = nssStore.getTokenNamesAsArray();                      
            return result;
         } catch (Exception ex) {
             throw new DomainException(ex);
         }
     }        

    private void reportMissingNss() {
	final String nssp = SystemPropertyConstants.NSS_DB_PROPERTY;
	final String[] args = new String[]{"NSS", nssp, CERTUTIL_CMD};
	final String msg = _strMgr.getString("nssmismatch", args);
	//throw new RepositoryException(msg); //UNCOMMENT THIS TODO
	//System.out.println("IF YOU SEE THIS MESSAGE, BUG: 6482063 IS NOT FIXED");
	//System.out.println("FALLING BACK TO USING JKS, BUT DOMAIN CREATION SHOULD FAIL IN THIS CASE");
    }
}
