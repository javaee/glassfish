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

package com.sun.enterprise.tools.upgrade;

import java.util.*;
import java.io.*;
import java.util.logging.*;

import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.cluster.*;
import com.sun.enterprise.tools.upgrade.miscconfig.ConfigTransfers;
import com.sun.enterprise.tools.upgrade.transform.TransformManager;

public class UpgradeHarness {
    private static Logger logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private StringManager stringManager = 
            StringManager.getManager(LogService.UPGRADE_LOGGER);
    private List moduleList;
    private CommonInfoModel commonInfo;
    private ArrayList doNotCopyList;

    public UpgradeHarness() {
        moduleList = new LinkedList();
    }
    
    public void setCommonInfoModel(CommonInfoModel commonInfo){
        this.commonInfo=commonInfo;
    }
    
    public void startUpgrade(){
        DomainsProcessor dProcessor = new DomainsProcessor(commonInfo);

        try {
            //Load the modules to be invoked for upgrade
            loadModules();

            System.setProperty("com.sun.aas.configRoot", 
                    commonInfo.getTargetInstallDir() + File.separator + "config");
            
            //Delete/Create new domains and set the ports required
            dProcessor.processTargetDomains();
            
            //If domains are created, then 30% done
            UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(30);	
            
            //Process clusters and stand-alone instances if EE
            if(commonInfo.getSourceEdition().equals(UpgradeConstants.EDITION_EE)){
                if(!dProcessor.processClusters()) {
                    dProcessor.processStandAloneInstances();
                }
            }
            
            //If domains/clusters (if any) are processed then 50% done
            UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(50);					

            //Invoke Modules
            int totalIterations = this.getTotalIterationsFromDomainMapping();
            int currentIteration =0;
            for(java.util.Iterator dItr = 
                    commonInfo.getDomainMapping().keySet().iterator(); 
                    dItr.hasNext();){
                DomainInfo dInfo = (DomainInfo)commonInfo.getDomainMapping().get(dItr.next());
                commonInfo.setCurrentDomain(dInfo.getDomainName());
                List instanceList = this.getProcessableInstanceList(dInfo);
                logger.log(Level.INFO, 
                        stringManager.getString("enterprise.tools.upgrade.currentlyProcessingDomain",
                        dInfo.getDomainName()));
                for(int instIndex = 0; instIndex < instanceList.size(); instIndex++){
                    currentIteration++;
                    String currentInstanceName = (String)instanceList.get(instIndex);
                    if(! currentInstanceName.equals("")) {
                        commonInfo.setCurrentSourceInstance(currentInstanceName);
                        logger.log(Level.INFO, stringManager.getString(
                                "enterprise.tools.upgrade.currentlyProcessingInstance",
                            currentInstanceName));
                    }
                    if(!invokeModules(currentIteration,totalIterations)) {
                        return;
                    }
                }
                //copy admin-keyfile to preserve admin credentials
                if(copyAdminKeyFile(dInfo)) {
                    logger.log(Level.INFO, stringManager.getString(
                            "enterprise.tools.upgrade.copykeyfile"));
                }
                
                //copy domain-passwords file to preserve master password
                if(copyDomainPasswordsFile(dInfo)) {
                    logger.log(Level.INFO, stringManager.getString(
                        "enterprise.tools.upgrade.copydomain_passwords_file"));
                }
                
                //Restore master password from source
                if(restoreMasterPassword(dInfo)) {
                    logger.log(Level.INFO, stringManager.getString(
                        "enterprise.tools.upgrade.restored_master_password"));                    
                }
                
                //copy MQ repository
                if(copyMqRepository(dInfo)) {
                    logger.log(Level.INFO, stringManager.getString(
                        "enterprise.tools.upgrade.copy_mq_repository"));
                }
            }		
            //End of upgrade process
            logger.log(Level.INFO, stringManager.getString(
                    "enterprise.tools.upgrade.finishedUpgrade"));
            UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(100); 
			
			//- for noprompt mode remove predefined credentials
			removeAsadminpass();
			
        } catch(HarnessException he) {
            logger.log(Level.SEVERE,stringManager.getString(
                    "enterprise.tools.upgrade.generalException" ,
                    he.getMessage()));
            UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(-1);
            commonInfo.recover();		
            return;                
        }
    }

    /** 
     * Method to find out the number of instances that need to be processed on 
     * then whole.
     */
    private int getTotalIterationsFromDomainMapping(){
        int totalIt = 1;
        //Iterate through the list of domains and the processable instance list
        for(java.util.Iterator dItr = 
                commonInfo.getDomainMapping().keySet().iterator(); dItr.hasNext();){
            DomainInfo dInfo = (DomainInfo)commonInfo.getDomainMapping().get(dItr.next());
            List instanceList = this.getProcessableInstanceList(dInfo);
            totalIt += instanceList.size();
        }
        return totalIt;
    }
    
    private boolean invokeModules(int currentIteration, int totalIterations){
        String moduleName = "Default";
        List successfulModuleList = new ArrayList();
        int moduleSize = moduleList.size();
        int nthModule = 0;
        int progress =0 ;
        
        //Iterate through the list of modules to be invoked
        for(int i=0; i<moduleSize; i++){            
            nthModule++;
            
            //If one of the modules have failed, Upgrade should not continue
            if(!UpdateProgressManager.getProgressManager().canContinueUpgrade()){
                logger.log(Level.SEVERE, stringManager.getString(
                        "enterprise.tools.upgrade.didNotfinishUpgrade", 
                        commonInfo.getTargetDomainRoot()));
                UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(-1);
                return false;
            }
            
            //Get the module to be processed
            BaseModule baseModule = (BaseModule)this.moduleList.get(i);
            moduleName = baseModule.getName();
            if(baseModule.upgrade(commonInfo)) {
                successfulModuleList.add(baseModule);
            } else { 
                // Rollback all previous successful operations.
                logger.log(Level.INFO,
                        stringManager.getString("enterprise.tools.upgrade.module_upgrade_failed",
                        moduleName));
                UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
                baseModule.recovery(commonInfo);
                for(int k=0; k<successfulModuleList.size(); k++){
                    BaseModule successModule = 
                            (BaseModule)successfulModuleList.get(k);
                    logger.log(Level.INFO,
                            stringManager.getString("enterprise.tools.upgrade.module_rollback",
                            successModule.getName(),commonInfo.getCurrentDomain()));
                    successModule.recovery(commonInfo);
                }
            }
            logger.log(Level.INFO, stringManager.getString(
                    "enterprise.tools.upgrade.finishedModule") + moduleName);
            
            //Update the progress
            int pFirst = (50 * currentIteration) / totalIterations ;
            int pLast = (50/totalIterations) - (50*nthModule)/(moduleSize*totalIterations);
            progress = 50 + pFirst - pLast;
            UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(progress);
        }
        return true;
    }
    
    private void upgradeEESource(){        
        // Add 1 for post updateMiscellaneousClusterInfo
        int totalIterations = ClustersInfoManager.getClusterInfoManager().getClusterInfoList().size()+
                                UpgradeUtils.getUpgradeUtils(commonInfo).getStandAloneInstances(commonInfo.getDomainMapping()).size()+1;
        int currentIteration =0;
        // Start with upgrading clustered instances
        for(java.util.Iterator dItr = ClustersInfoManager.getClusterInfoManager().getClusterInfoList().iterator(); dItr.hasNext();){
            ClusterInfo cInfo = (ClusterInfo)dItr.next();
            ClusteredInstance clInstance = cInfo.getMasterInstance();
            if(clInstance == null){
                clInstance = (ClusteredInstance)cInfo.getClusteredInstanceList().get(0);
            }
            commonInfo.setCurrentCluster(cInfo.getClusterName());
            commonInfo.setCurrentDomain(clInstance.getDomain());
            commonInfo.setCurrentSourceInstance(clInstance.getInstanceName());
            currentIteration++;
            logger.log(Level.INFO, stringManager.getString("enterprise.tools.upgrade.currentlyProcessingCLUSTER",cInfo.getClusterName()));
            if(!invokeModules(currentIteration,totalIterations))
                return;            
        }
        commonInfo.setCurrentCluster(null);
        // now upgrade stand alone instances
        for(java.util.Iterator sItr = UpgradeUtils.getUpgradeUtils(commonInfo).getStandAloneInstances(commonInfo.getDomainMapping()).iterator(); sItr.hasNext();){
            Vector instDInfo = (Vector)sItr.next();
            commonInfo.setCurrentDomain(((DomainInfo)instDInfo.elementAt(1)).getDomainName());
            commonInfo.setCurrentSourceInstance((String)instDInfo.elementAt(0));
            currentIteration++;
            logger.log(Level.INFO, stringManager.getString("enterprise.tools.upgrade.currentlyProcessingInstance",(String)instDInfo.elementAt(0)));
            if(!invokeModules(currentIteration,totalIterations))
                return;            
        }
        // Migrate iiop-cluster info saved in clustered info manager.
        this.updateMiscellaneousClusterInfo();
        logger.log(Level.INFO, stringManager.getString("enterprise.tools.upgrade.finishedUpgrade"));
        //logger.log(Level.CONFIG, Integer.toString(100));
        UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(100);
    }
    
    private void updateMiscellaneousClusterInfo(){
        // Get IIOPClusters from clusterinfo manager and process them
        Hashtable iiopMapping = ClustersInfoManager.getClusterInfoManager().getIIOPClustersMapping();
        if(iiopMapping != null){
            for(Iterator it = iiopMapping.keySet().iterator(); it.hasNext();){
                String domainName = (String)it.next();
                List iCls = (List)iiopMapping.get(domainName);
                String domainXMLfile = this.commonInfo.getDestinationDomainPath(domainName)+File.separator+"config"+File.separator+"domain.xml";
                for(int i=0; i<iCls.size(); i++){
                    IIOPCluster iiopCluster = (IIOPCluster)iCls.get(i);
                    UpgradeUtils.getUpgradeUtils(this.commonInfo).updateDomainXMLWithIIOPCluster(this.commonInfo, domainXMLfile, iiopCluster);                    
                }
            }
        }
        // Get PersistenceStore info from cluster info manager and process them
        Hashtable persistenceStoreMapping = ClustersInfoManager.getClusterInfoManager().getPersistenceStorePropertiesMapping();
        if(persistenceStoreMapping != null){
            for(Iterator pit = persistenceStoreMapping.keySet().iterator(); pit.hasNext();){
                String domainName = (String)pit.next();
                java.util.Properties props = (java.util.Properties)persistenceStoreMapping.get(domainName);
                String domainXMLfile = this.commonInfo.getDestinationDomainPath(domainName)+File.separator+"domain.xml";
                UpgradeUtils.getUpgradeUtils(this.commonInfo).updateDomainXMLWithPersistenceStoreProps(props);
            }
        }
    }
    
    private List getProcessableInstanceList(DomainInfo dInfo){
        List instanceList = new ArrayList();
        
        //Return empty instance list if source is not 7X
        String sourceVersion = commonInfo.getSourceVersion();
        if(!UpgradeConstants.VERSION_7X.equals(sourceVersion)) {
            instanceList.add(new String(""));
            return instanceList;
        }
        
        //Source is 7X, get the domainInstanceList 
        List domainInstanceList = dInfo.getInstanceNames();
        for(int i=0; i<domainInstanceList.size(); i++){
            String instanceName = (String)domainInstanceList.get(i);
            // For 7x remove admin server from the list
            if(instanceName.equals("admin-server"))
                continue;
            instanceList.add(instanceName);
            if(commonInfo.getSourceEdition().equals(UpgradeConstants.EDITION_PE)){
                // If PE only return the first element  fromt the list
                if (domainInstanceList.size() > 2) {
                    logger.log(Level.INFO,
                            stringManager.getString(
                            "enterprise.tools.upgrade.more_thanone_instance",
                            instanceName));
                }
                break;
            }
        }
        if(instanceList.isEmpty())
            logger.log(Level.WARNING, stringManager.getString(
                    "enterprise.tools.upgrade.no_server_instance", 
                    dInfo.getDomainName()));
        return instanceList;
    }
    
    private void loadModules(){
        List list = CommonProperties.getRegisteredModules(commonInfo);
        int size = list.size();
        for(int i=0; i<size; i++){
            String moduleClassName = (String)list.get(i);
            try{
                Class cls = Class.forName(moduleClassName);
                BaseModule baseModule = (BaseModule)cls.newInstance();
                moduleList.add(baseModule);
            }catch(ClassNotFoundException e){
                logger.log(Level.SEVERE,stringManager.getString("enterprise.tools.upgrade.load_instantiate_error", e.getMessage()),e);
            }catch(InstantiationException e){
                logger.log(Level.SEVERE,stringManager.getString("enterprise.tools.upgrade.load_instantiate_error", e.getMessage()),e);
            }catch(IllegalAccessException e){
                logger.log(Level.SEVERE,stringManager.getString("enterprise.tools.upgrade.load_instantiate_error", e.getMessage()),e);
            }
        }
    }

    public void copyDomainsFromBackup(String domainName) {
        DomainInfo domainInfo = (DomainInfo)commonInfo.getDomainMapping().get(domainName);
        String domainPath = domainInfo.getDomainPath();
        // FIX ME : Need to find a better way to copy over the directory.I hate to do this.
        try {

            UpgradeUtils.getUpgradeUtils(commonInfo).copyDirectory(new File(domainPath),
                new File(commonInfo.getTargetDomainRoot()+File.separator+domainName));
        } catch(IOException ioe) {
            logger.log(Level.WARNING, stringManager.getString("IOException thrown",
                    domainPath));
        }
        // now change permissions of startserv and stopserv
        if(System.getProperty("os.name").equals("Unix") ||
              System.getProperty("os.name").equals("Linux") ||
              System.getProperty("os.name").equals("SunOS")) {
            setExecutePermissions(commonInfo.getTargetDomainRoot()+
                      File.separator+domainName);
        } 
       
        
    }
    
    public void setExecutePermissions(String domainPath) {
        String binDir = domainPath + File.separator + "bin";
        String generatedDir = domainPath + File.separator + "generated" +
                File.separator + "tmp";
        logger.log(Level.INFO,"bin dir= "+binDir);
        try {
            final String[] fileList = new File(binDir).list();
            for (int i = 0 ; i < fileList.length ; i ++) {
                logger.log(Level.INFO,"File List = "+binDir+ File.separator + fileList[i]);
                Runtime.getRuntime().exec("/bin/chmod a+x " +
                        binDir + File.separator + fileList[i]);
            }
            // this tmp directory needs to be rwx------
            Runtime.getRuntime().exec("/bin/chmod 700 " +
                        generatedDir );
         } catch (Exception e) {
             logger.log(Level.WARNING, 
                    stringManager.getString("domainsProcessor.IOException",
                    domainPath));               
         }
    }
    
    /** 
     * Method to copy admin-keyfile which is the default store of the
     * admin credentials. 
     * 
     * @param dInfo the DomainInfo object for the domain being processed
     */     
    public boolean copyAdminKeyFile(DomainInfo dInfo) {
        // do this only for 8.xPE/EE->9.1 EE and 9.0PE->9.1EE
        if(commonInfo.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
            return false;
        }	
        try {
            String sourceKeyFilePath = dInfo.getDomainPath() + File.separator +
                    "config";
            String targetKeyFilePath = commonInfo.getTargetDomainRoot() +
                    File.separator + dInfo.getDomainName() + File.separator + 
                    "config";
            String sourceKeyFile = sourceKeyFilePath + File.separator + 
                    "admin-keyfile";
            String targetKeyFile = targetKeyFilePath + File.separator + 
                    "admin-keyfile";
            if(!(new File(sourceKeyFile).exists())) 
                return false;
            
            //Copy the file from source location to target
            UpgradeUtils.getUpgradeUtils(commonInfo).copyFile(sourceKeyFile, 
                    targetKeyFile);
            
        } catch (IOException ioe ) {
            logger.log(Level.WARNING, 
                    stringManager.getString("domainsProcessor.IOException",
                     ioe.getMessage()));  
             return false;
        }
        return true;
     }
    
    public void copyDomain(String sourceDomain, String targetDomainRoot ) {
        // copy selected directories
        File srcDomain = new File(sourceDomain);
        // target domain is the target domain root + domain name
        File targetDomain = new File(targetDomainRoot,
                             (String)commonInfo.getDomainList().get(0));
        String[] srcDomainListing = srcDomain.list();
        for( int i=0; i< srcDomainListing.length; i++ ){
            // if this directory should be copied
            //Fix for 6444308
            if(!doNotCopyList.contains(srcDomainListing[i])) {
                File srcDomainSubDirectory = new File(sourceDomain, srcDomainListing[i]);
                File targetDomainSubDirectory = new File(targetDomain, srcDomainListing[i]);
                try {
                    UpgradeUtils.copyDirectory(srcDomainSubDirectory,
                       targetDomainSubDirectory);
                //Fix for 6444308
                } catch (IOException ioe) {
                    logger.log(Level.WARNING, 
                        stringManager.getString("domainsProcessor.IOException",
                            ioe.getMessage()));                     
                }
            } else {
                continue;
            }
        }
        // now change permissions of startserv and stopserv
        if(System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).equals("Unix") ||
                System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).equals("Linux")  ||
                //Fix for CR 6463376
                System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).equals("SunOS")) {
            String domainName = (String)commonInfo.getDomainList().get(0);
            setExecutePermissions(commonInfo.getTargetDomainRoot()+
                    File.separator+domainName);
        }
    }

    /** 
     * Method to copy domain-passwords file to preserve master password from 
     * source to target
     * 
     * @param dInfo the DomainInfo object for the domain being processed
     */         
    public boolean copyDomainPasswordsFile(DomainInfo dInfo) {        
        // do this only for 8.xPE/EE->9.1 EE and 9.0PE->9.1EE
        if(commonInfo.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
            return false;
        }	
        String sourcePasswordsFilePath = dInfo.getDomainPath() + File.separator +
                "config";
        String targetPasswordsFilePath = commonInfo.getTargetDomainRoot() +
                File.separator + dInfo.getDomainName() + File.separator + 
                "config";
        String sourcePasswordsFile = sourcePasswordsFilePath + File.separator + 
                "domain-passwords";
        String targetPasswordsFile = targetPasswordsFilePath + File.separator + 
                "domain-passwords";
        try {

            if(!(new File(sourcePasswordsFile).exists())) 
                return false;
            
            //Copy the file from source location to target
            UpgradeUtils.getUpgradeUtils(commonInfo).copyFile(
                    sourcePasswordsFile, targetPasswordsFile);
            
        } catch (IOException ioe ) {
            logger.log(Level.WARNING, 
                    stringManager.getString(
                    "enterprise.tools.upgrade.copy_domain_passwords_file_failed",
                     sourcePasswordsFilePath, targetPasswordsFilePath));  
             return false;
        }
        return true;
    }

    /** 
     * Method to restore master password from source to target domain.
     * 
     * @param dInfo the DomainInfo object for the domain being processed
     */         
    private boolean restoreMasterPassword(DomainInfo dInfo) {
        // do this only for 8.xPE/EE->9.1 EE and 9.0PE->9.1EE
        if(commonInfo.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
            return false;
        }	
        String targetMasterPasswordFile = commonInfo.getTargetDomainRoot() +
            File.separator + dInfo.getDomainName() + File.separator + 
            "master-password";
        String sourceMasterPasswordFile = dInfo.getDomainPath() +
            File.separator + "master-password";
        try {
            //If source domain has a master-password file, copy it over to the target
            if(new File(sourceMasterPasswordFile).exists()) {
                UpgradeUtils.copyFile(sourceMasterPasswordFile, 
                        targetMasterPasswordFile);
            } else {
                //Remove from target if it exists
                if(new File(targetMasterPasswordFile).exists()) {
                    if(!UpgradeUtils.deleteFile(targetMasterPasswordFile)) {
                        throw new SecurityException();
                    }
                }
            }
        } catch (Exception e) {
            if(e instanceof IOException) {
                logger.log(Level.WARNING, 
                    stringManager.getString(
                        "enterprise.tools.upgrade.copy_master_password_file_failed", 
                        sourceMasterPasswordFile, targetMasterPasswordFile));
            } 	    		    
            if(e instanceof SecurityException) {
                logger.log(Level.WARNING, 
                    stringManager.getString(
                        "enterprise.tools.upgrade.delete_master_password_file_failed",
                        targetMasterPasswordFile));
            }
            return false;
        }
        return true;
    }

    private boolean copyMqRepository(DomainInfo dInfo) {
         String sourcePath = dInfo.getDomainPath() + File.separator +
                "imq";
        String targetPath = commonInfo.getTargetDomainRoot() +
                File.separator + dInfo.getDomainName() + File.separator + 
                "imq";
        try {
            File sourceDir = new File(sourcePath);
            File targetDir = new File(targetPath);

            if(!sourceDir.exists())
                return false;
            
            //Copy the file from source location to target
            UpgradeUtils.getUpgradeUtils(commonInfo).copyDirectory(
                    sourceDir, targetDir);
            
        } catch (IOException ioe ) {
            logger.log(Level.WARNING, 
                    stringManager.getString(
                    "enterprise.tools.upgrade.copy_mq_repository_failed",
                     sourcePath, targetPath));  
             return false;
        }
        return true;
    
    }
	
	/**
	 * Remove the {user.home}/.asadminpass file when default credential
	 * for noprompt processing has been used.
	 */
	private void removeAsadminpass(){
			//- removed isNoprompt check cr6598202
			File f = new File(System.getProperty("user.home"), ".asadminpass");
			if (f.exists()){
				f.delete();
				logger.log(Level.INFO, stringManager.getString(
                    "enterprise.tools.upgrade.removed.asadminpass", f.getAbsolutePath()));
			}
	}
}
