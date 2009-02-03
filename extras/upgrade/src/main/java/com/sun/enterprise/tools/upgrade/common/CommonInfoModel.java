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

package com.sun.enterprise.tools.upgrade.common;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.cluster.*;

/**
 *
 * author : Gautam Borah
 *
 */

public class CommonInfoModel{
	
	private static CommonInfoModel _commonInfoModel = null;
	private TargetAppSrvObj tAppSrvObj = new TargetAppSrvObj();
	private SourceAppSrvObj sAppSrvObj = new SourceAppSrvObj();
    private boolean isInPlace = false; //- inplace or side-by-side upgrade process
	private boolean nopromptMode = false; //- user required noprompt CLI mode	
    private String osName; // machine OS type

    //Logging fields
    private StringManager stringManager = 
            StringManager.getManager(CommonInfoModel.class);
    private static Logger logger=LogService.getLogger(LogService.UPGRADE_LOGGER);
	// has default logger been created yet?
	private boolean isUpgradeLogFile = false;
    
	//- make this a singleton.
	public static CommonInfoModel getInstance(){
		if (_commonInfoModel == null){
			_commonInfoModel = new CommonInfoModel();
		}
		return _commonInfoModel;
	}
    /**
     * CommonInfoModel constructor
     */
    //////- public CommonInfoModel(){
    private CommonInfoModel(){
    }
    
	public SourceAppSrvObj getSource(){
		return sAppSrvObj;
	}
	public TargetAppSrvObj getTarget(){
		return tAppSrvObj;
	}
	
	
    ////-public void setTargetDomainRoot(String targetDomainsRoot) {
	public void createUpgradeLogFile(String targetDomainRoot) {	
        //If target domains root is already set, avoid overwriting.
		if (targetDomainRoot != null && !isUpgradeLogFile)
        try {
            String logPath = targetDomainRoot + "/" +  UpgradeConstants.ASUPGRADE_LOG;
            logger.info(stringManager.getString("upgrade.common.log_redirect") + logPath);
            LogService.initialize(logPath);
			isUpgradeLogFile = true;
        } catch(Exception e) {
            logger.warning(e.getLocalizedMessage());
        }
    }
	
	/**
     * @return a logger to use in the Module implementation classes
     */
    public static Logger getDefaultLogger() {
        if (logger==null) {
            logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
        }
        return logger;
    }

	public void setupTasks() throws Exception {
		String domainName = sAppSrvObj.getDomainName();
		String srcDomainDir = sAppSrvObj.getDomainDir();
		
		//- identify target domain to upgrade
		tAppSrvObj.setDomainName(domainName);
		setIsInPlace(srcDomainDir.equals(tAppSrvObj.getDomainDir()));

		if (isInPlace()){
            //- Not all target appServer versions allow in-place upgrades
            if (tAppSrvObj.isInPlaceUpgradeAllowed()) {
                String backupDomainPath = UpgradeUtils.getUpgradeUtils(this).backupDomain(
                        domainName, sAppSrvObj.getInstallDir(), tAppSrvObj.getInstallDir());
                sAppSrvObj.setBackupDomainDir(backupDomainPath);
            } else {
                throw new Exception(stringManager.getString("upgrade.common.inplace_upgrade_not_supported"));
            }
		}
	}
	
	/**
	 * The appropriate source domain.xml file.
	 * It is in the backup area for an inplace upgrade.
	 * It is the original file location for a side-by-side upgrade.
	 */
	 public String getSourceConfigXMLFile(){
		 return (isInPlace())? sAppSrvObj.getBackupConfigXMLFile() :
			 sAppSrvObj.getConfigXMLFile();
	 }
  
    public String getOSName() {
        return osName;
    }
    
    public void setOSName(String osName){
        this.osName = osName;
    }

    public boolean isInPlace() {
        return isInPlace;
    }
    
    private void setIsInPlace(boolean b) {
        this.isInPlace = b;
    }

	//- Must know when in noprompt mode
	public boolean isNoprompt(){
		return nopromptMode;
	}
	public void setNoprompt(boolean flag){
		nopromptMode = flag;
	}
	
    public boolean isUpgradeSupported(){
		boolean flag = true;
        String sourceVersion = sAppSrvObj.getVersion();
        String sourceEdition = sAppSrvObj.getEdition();
        String targetVersion = tAppSrvObj.getVersion();
        String targetEdition = tAppSrvObj.getEdition();
		
        //Check if the Source version and edition supports this version upgrade.	
        if(!((java.util.HashSet)UpgradeConstants.supportMap.get(
                sourceVersion)).contains(targetVersion)) {			
			logger.info(stringManager.getString(
			"upgrade.common.upgrade_not_supported",
			sourceVersion, sourceEdition, targetVersion, targetEdition));
            flag = false;
        } else {
			if(UpgradeConstants.CLUSTER_PROFILE.equals(sourceEdition) &&
				!targetEdition.equals(UpgradeConstants.ENTERPRISE_PROFILE)){
				logger.log(Level.INFO, stringManager.getString(
					"upgrade.common.cluster_profile_created"));
			} else if (!sourceEdition.equals(targetEdition)){
				logger.info(stringManager.getString(
					"upgrade.common.upgrade_not_supported",
					sourceVersion, sourceEdition,
					targetVersion, targetEdition));
				flag = false;
			}
		}
		return flag;
    }
	
    public String findLatestDomainDirBackup(String domainRoot) {
		return UpgradeUtils.getUpgradeUtils(this).findLatestDomainBackup(
			domainRoot, sAppSrvObj.getDomainName());
    }
  
    public void recover() {
        UpgradeUtils.getUpgradeUtils(this).recover();
    }
 
    /**
      * Method to build the ClusterInfo object required for processing clusters.
      */	  
    public void processDomainXmlForClusters() {
        ClustersInfoManager.getClusterInfoManager().gatherClusterInfo(this);
    }   
	
	/**
	 * Consolidate identification of "ee" (pre-as9.1) and "enterprise"
	 * (post as8.2) string designators for product edition.
	 */
	public boolean isEnterpriseEdition(String s){
		boolean flag = false;
		if(UpgradeConstants.ENTERPRISE_PROFILE.equals(s)){
			flag = true;
		}
		return flag;
	}
	
	/**
	 * Consolidate identification of "pe" (pre-as9.1) and "developer"
	 * (post as8.2) string designators for product edition.
	 */
	public boolean isPlatformEdition(String s){
		boolean flag = false;
		if(UpgradeConstants.DEVELOPER_PROFILE.equals(s)||
			UpgradeConstants.CLUSTER_PROFILE.equals(s)){
			flag = true;
		}
		return flag;
	}
}
