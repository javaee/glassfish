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
		StringManager.getManager(UpgradeHarness.class);
	private List moduleList = new LinkedList();
	private CommonInfoModel commonInfo = CommonInfoModel.getInstance();
	private TargetAppSrvObj  _target = commonInfo.getTarget();
	private SourceAppSrvObj  _source = commonInfo.getSource();
	private ArrayList doNotCopyList;
	
	public UpgradeHarness() {
	}
	
	public void setCommonInfoModel(CommonInfoModel commonInfo){
		//-this.commonInfo=commonInfo;
	}
	
	/**
	 * This method is the control for the complete upgrade process.
	 * All upgrade actions emanate from here.
	 */
	public void startUpgrade(){
		try {
			System.setProperty("com.sun.aas.configRoot",
				_target.getInstallRootProperty() + "/" + "config");
			
			// Create a new domain
			DomainsProcessor dProcessor = new DomainsProcessor(commonInfo);
			dProcessor.processTargetDomain();
			UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(30);
			
			// Create clusters, node agents and instances
			String sourceEdition = _source.getEdition();
			if (commonInfo.isEnterpriseEdition(sourceEdition) ||
				UpgradeConstants.CLUSTER_PROFILE.equals(sourceEdition)){
				commonInfo.processDomainXmlForClusters();
				dProcessor.processClusters();
				dProcessor.processStandAloneInstances();
			}
			UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(50);
			logger.log(Level.INFO,stringManager.getString(
				"enterprise.tools.upgrade.currentlyProcessingDomain", _source.getDomainName()));
			
			
			//Load and exec all other upgrade actions
			loadModules();
			invokeModules();
			
			// Move misc password files to target
			copyPasswdFiles();
		} catch(HarnessException he) {
			logger.log(Level.SEVERE,stringManager.getString(
				"enterprise.tools.upgrade.generalException" ,he.getMessage()));
			UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(-1);
			commonInfo.recover();
		}
	}
	
	private boolean invokeModules(){
		List successfulModuleList = new ArrayList();
		
		//Iterate through the list of modules to be invoked
		int cnt = moduleList.size();
		for(int i=0; i<cnt; i++){
			//If one of the modules have failed, Upgrade should not continue
			if(!UpdateProgressManager.getProgressManager().canContinueUpgrade()){
				logger.log(Level.SEVERE, stringManager.getString(
					"enterprise.tools.upgrade.didNotfinishUpgrade", _target.getInstallDir()));
				UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(-1);
				return false;
			}
			
			//Get the module to be processed
			BaseModule baseModule = (BaseModule)this.moduleList.get(i);
			String moduleName = baseModule.getName();
			if(baseModule.upgrade(commonInfo)) {
				successfulModuleList.add(baseModule);
			} else {
				// Rollback all previous successful operations.
				logger.log(Level.INFO,stringManager.getString(
					"enterprise.tools.upgrade.module_upgrade_failed", moduleName));
				UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
				baseModule.recovery(commonInfo);
				for(int k=0; k<successfulModuleList.size(); k++){
					BaseModule successModule =
						(BaseModule)successfulModuleList.get(k);
					logger.log(Level.INFO,
						stringManager.getString("enterprise.tools.upgrade.module_rollback",
						successModule.getName(),_target.getDomainName()));
					successModule.recovery(commonInfo);
				}
			}
			logger.log(Level.INFO, stringManager.getString(
				"enterprise.tools.upgrade.finishedModule") + moduleName);
			
			//Update the progress
			int pFirst = 50;
			int pLast = 50 - (50*(i+1))/cnt;
			int progress = 50 + pFirst - pLast;
			UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(progress);
		}
		return true;
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
				logger.log(Level.SEVERE,stringManager.getString(
					"enterprise.tools.upgrade.load_instantiate_error", e.getMessage()),e);
			}catch(InstantiationException e){
				logger.log(Level.SEVERE,stringManager.getString(
					"enterprise.tools.upgrade.load_instantiate_error", e.getMessage()),e);
			}catch(IllegalAccessException e){
				logger.log(Level.SEVERE,stringManager.getString(
					"enterprise.tools.upgrade.load_instantiate_error", e.getMessage()),e);
			}
		}
	}
	
	public void copyDomainsFromBackup(String domainName) {
		String domainPath = _source.getInstallDir();
		try {
			UpgradeUtils.getUpgradeUtils(commonInfo).copyDirectory(new File(domainPath),
				new File(_target.getDomainDir()));
		} catch(IOException ioe) {
			logger.log(Level.WARNING, stringManager.getString("IOException thrown",
				domainPath));
		}
		// now change permissions of startserv and stopserv
		if(System.getProperty("os.name").equals("Unix") ||
			System.getProperty("os.name").equals("Linux") ||
			System.getProperty("os.name").equals("SunOS")) {
			setExecutePermissions(_target.getInstallDir()+ "/"+domainName);
		}
	}
	
	public void setExecutePermissions(String domainPath) {
		String binDir = domainPath + "/" + "bin";
		String generatedDir = domainPath + "/" + "generated" + "/" + "tmp";
		logger.log(Level.INFO,"bin dir= "+binDir);
		try {
			final String[] fileList = new File(binDir).list();
			for (int i = 0 ; i < fileList.length ; i ++) {
				logger.log(Level.INFO,"File List = "+binDir+ "/" + fileList[i]);
				Runtime.getRuntime().exec("/bin/chmod a+x " + binDir + "/" + fileList[i]);
			}
			// this tmp directory needs to be rwx------
			Runtime.getRuntime().exec("/bin/chmod 700 " + generatedDir );
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
	 */
	public boolean copyAdminKeyFile(String srcPath, String trgPath) {
		boolean flag = false;
		try {
			String sourceKeyFile = srcPath + "/" + "config" + "/" +  "admin-keyfile";
			String targetKeyFile = trgPath + "/" + "config" + "/" +"admin-keyfile";
			if((new File(sourceKeyFile).exists())){
				//Copy the file from source location to target
				UpgradeUtils.getUpgradeUtils(commonInfo).copyFile(sourceKeyFile,
					targetKeyFile);
				logger.log(Level.INFO, stringManager.getString(
					"enterprise.tools.upgrade.copykeyfile"));
				flag = true;
			}
		} catch (IOException ioe ) {
			logger.log(Level.WARNING, stringManager.getString(
				"domainsProcessor.IOException", ioe.getMessage()));
		}
		return flag;
	}
	
	public void copyDomain(String sourceDomain, String targetDomainRoot ) {
		// copy selected directories
		File srcDomain = new File(sourceDomain);
		String domainName = _target.getDomainName();
		File targetDomain = new File(targetDomainRoot,domainName);
		String[] srcDomainListing = srcDomain.list();
		for( int i=0; i< srcDomainListing.length; i++ ){
			// if this directory should be copied
			if(!doNotCopyList.contains(srcDomainListing[i])) {
				File srcDomainSubDir = new File(sourceDomain, srcDomainListing[i]);
				File targetDomainSubDir = new File(targetDomain,srcDomainListing[i]);
				try {
					UpgradeUtils.copyDirectory(srcDomainSubDir, targetDomainSubDir);
				} catch (IOException ioe) {
					logger.log(Level.WARNING,stringManager.getString(
						"domainsProcessor.IOException", ioe.getMessage()));
				}
			} else {
				continue;
			}
		}
		// now change permissions of startserv and stopserv
		if(System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).equals("Unix") ||
			System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).equals("Linux")  ||
			System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).equals("SunOS")) {
			setExecutePermissions(targetDomain.getAbsolutePath());
		}
	}
	
	/**
	 * Method to copy domain-passwords file to preserve master password from
	 * source to target
	 *
	 */
	public boolean copyDomainPasswordsFile(String srcPath, String trgPath) {
		boolean flag = false;
		String sourcePasswordsFile = srcPath + "/" + "config" + "/" + "domain-passwords";
		String targetPasswordsFile = trgPath +  "/" + "config" + "/" +  "domain-passwords";
		try {
			
			if((new File(sourcePasswordsFile).exists())){
				//Copy the file from source location to target
				UpgradeUtils.getUpgradeUtils(commonInfo).copyFile(
					sourcePasswordsFile, targetPasswordsFile);
				logger.log(Level.INFO, stringManager.getString(
					"enterprise.tools.upgrade.copydomain_passwords_file"));
				flag = true;
			}
		} catch (IOException ioe ) {
			logger.log(Level.WARNING,stringManager.getString(
				"enterprise.tools.upgrade.copy_domain_passwords_file_failed",
				sourcePasswordsFile, targetPasswordsFile));
		}
		return flag;
	}
	
	/**
	 * Method to restore master password from source to target domain.
	 *
	 */
	private boolean restoreMasterPassword(String srcPath, String trgPath) {
		boolean flag = false;
		String targetMasterPasswordFile = trgPath + "/" + "master-password";
		String sourceMasterPasswordFile = srcPath + "/" + "master-password";
		try {
			// Keep sources' masterpasswd
			if(new File(sourceMasterPasswordFile).exists()) {
				UpgradeUtils.copyFile(sourceMasterPasswordFile, targetMasterPasswordFile);
			} else {
				//Remove target's if exists
				if(new File(targetMasterPasswordFile).exists()) {
					if(!UpgradeUtils.deleteFile(targetMasterPasswordFile)) {
						throw new SecurityException();
					}
				}
			}
			logger.log(Level.INFO, stringManager.getString(
				"enterprise.tools.upgrade.restored_master_password"));
			flag = true;
		} catch (Exception e) {
			if(e instanceof IOException) {
				logger.log(Level.WARNING, stringManager.getString(
					"enterprise.tools.upgrade.copy_master_password_file_failed",
					sourceMasterPasswordFile, targetMasterPasswordFile));
			}
			if(e instanceof SecurityException) {
				logger.log(Level.WARNING,  stringManager.getString(
					"enterprise.tools.upgrade.delete_master_password_file_failed",
					targetMasterPasswordFile));
			}
		}
		
		return flag;
	}
	
	
	private boolean copyMqRepository(String srcPath, String trgPath) {
		boolean flag = false;
		String sourcePath = srcPath + "/" + "imq";
		String targetPath = trgPath + "/" + "imq";
		try {
			File sourceDir = new File(sourcePath);
			File targetDir = new File(targetPath);
			
			if(sourceDir.exists()){
				//Copy the file from source location to target
				UpgradeUtils.getUpgradeUtils(commonInfo).copyDirectory(
					sourceDir, targetDir);
				logger.log(Level.INFO, stringManager.getString(
					"enterprise.tools.upgrade.copy_mq_repository"));
				flag = true;
			}
		} catch (IOException ioe ) {
			logger.log(Level.WARNING, stringManager.getString(
				"enterprise.tools.upgrade.copy_mq_repository_failed",
				sourcePath, targetPath));
		}
		return flag;
	}
	
	private void copyPasswdFiles(){
		String trgPath = _target.getDomainDir();
		String srcPath = _source.getInstallDir();
		copyAdminKeyFile(srcPath,trgPath);
		copyDomainPasswordsFile(srcPath,trgPath);
		restoreMasterPassword(srcPath,trgPath);
		copyMqRepository(srcPath,trgPath);
		
		//End of upgrade process
		logger.log(Level.INFO, stringManager.getString(
			"enterprise.tools.upgrade.finishedUpgrade"));
		UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(100);
		
		//- for noprompt mode remove predefined credentials
		removeAsadminpass();
	}
	
	/**
	 * Remove the {user.home}/.asadminpass file when default credential
	 * for noprompt processing has been used.
	 */
	private void removeAsadminpass(){
		File f = new File(System.getProperty("user.home"), ".asadminpass");
		if (f.exists()){
			f.delete();
			logger.log(Level.INFO, stringManager.getString(
				"enterprise.tools.upgrade.removed.asadminpass", f.getAbsolutePath()));
		}
	}
}
