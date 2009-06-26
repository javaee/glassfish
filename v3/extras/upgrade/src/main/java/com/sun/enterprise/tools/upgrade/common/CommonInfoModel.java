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

import java.util.logging.Logger;
import com.sun.enterprise.tools.upgrade.logging.LogService;
import com.sun.enterprise.util.i18n.StringManager;

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
    private static final StringManager stringManager =
            StringManager.getManager(CommonInfoModel.class);
    private static final Logger logger = LogService.getLogger();
    
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
    private CommonInfoModel(){
    }
    
	public SourceAppSrvObj getSource(){
		return sAppSrvObj;
	}
	public TargetAppSrvObj getTarget(){
		return tAppSrvObj;
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
		} else {
            UpgradeUtils.getUpgradeUtils(this).cloneDomain(
                sAppSrvObj.getInstallDir(), tAppSrvObj.getDomainDir());
        }
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
        String targetVersion = tAppSrvObj.getVersion();

        if (!sourceVersion.equals(UpgradeConstants.VERSION_91) &&
            !sourceVersion.equals(UpgradeConstants.VERSION_3_0)){
            logger.info(stringManager.getString("upgrade.common.upgrade_not_supported",
			sourceVersion, sAppSrvObj.getEdition(), targetVersion, tAppSrvObj.getEdition()));
            flag = false;
        }
		return flag;
    }
	
    public String findLatestDomainDirBackup(String domainRoot) {
		return UpgradeUtils.getUpgradeUtils(this).findLatestDomainBackup(
			domainRoot, sAppSrvObj.getDomainName());
    }
  
    public void recover() {
        if (isInPlace()){
            UpgradeUtils.getUpgradeUtils(this).recover();
        }
    }
}
