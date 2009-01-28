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
 * TargetAppSrvObj.java
 *
 * Created on November 29, 2007, 4:22 PM
 *
 */

package com.sun.enterprise.tools.upgrade.common;

import java.io.File;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
/**
 *
 * @author rebeccas
 */
public class TargetAppSrvObj extends BaseDomainInfoObj{
	private static final String TARGET_WSS_SERVER_CONFIG_XML = "wss-server-config-1.0.xml";
    static final String DEFAULT_WEB_XML_FILE = "default-web.xml";
	private String dtdFilename = null;
	
	/** Creates a new instance of TargetAppSrvObj */
	public TargetAppSrvObj() {
	}
	
	public boolean isValidPath(String s){
		boolean flag = false;
		File targetPathDir = new File(s);
		if(targetPathDir.exists()) {
			// check if this is an existing domain
			File domainXML = new File(s + "/" +
				super.CONFIG_DOMAIN_XML_FILE);
			if(!domainXML.isFile() || !domainXML.exists()) {
				flag = true;
			}
		}
		return flag;
	}
	
	public void setInstallDir(String s){ 
		super.installDir = s;
		if (s != null){
			super.domainRoot = super.extractDomainRoot(s);
		}
		CommonInfoModel.getInstance().createUpgradeLogFile(installDir);
	}
	
	public String getDomainDir(){
		return getInstallDir() + "/" + super.domainName;
	}	
	
	public String getConfigXMLFile(){
		return getDomainDir() + "/" + super.CONFIG_DOMAIN_XML_FILE;
	}
	
	public String getVersionEdition(){
		if (super.versionEdition == null){
			VersionExtracter v = new VersionExtracter(super.domainRoot,
				CommonInfoModel.getInstance());
			super.version = v.getAsadminVersion();
			super.edition = v.getTargetDefaultProfile();
			super.versionEdition = v.formatVersionEditionStrings(
				super.version, super.edition);
		}
		return super.versionEdition;
	}
	
	
	//- target specific ---------------------
	public String getDTDFilename(){
		if (dtdFilename == null){
			VersionExtracter v = new VersionExtracter(super.domainRoot,
				CommonInfoModel.getInstance());
			dtdFilename = v.getDTDFileName(getConfigXMLFile());
		}
		return dtdFilename;
	}
	
	public String getWssServerConfigXML(){
		return getDomainDir() + "/" +
			UpgradeConstants.AS_CONFIG_DIRECTORY
			+ "/" + TARGET_WSS_SERVER_CONFIG_XML;
	}
	
	public String getDefaultWebXMLFileName(){
		return getDomainDir() + "/" +UpgradeConstants.AS_CONFIG_DIRECTORY + 
			"/" + DEFAULT_WEB_XML_FILE;
	}
	
	public String getInstallRootProperty(){
		return System.getProperty(UpgradeConstants.AS_INSTALL_ROOT);
	}
}
