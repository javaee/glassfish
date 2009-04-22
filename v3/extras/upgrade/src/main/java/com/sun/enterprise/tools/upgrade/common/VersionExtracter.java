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
 * VersionExtracter.java
 *
 * Created on March 8, 2004, 2:23 PM
 */

package com.sun.enterprise.tools.upgrade.common;


/**
 *
 * @author  prakash
 */
import java.io.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import java.util.logging.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionExtracter {

    private String installDir;

    private StringManager stringManager = StringManager.getManager(VersionExtracter.class);
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private CommonInfoModel common;

    /** Creates a new instance of VersionExtracter */
    public VersionExtracter(String iD,CommonInfoModel common) {
        this.installDir = iD;
        this.common = common;
    }
    
    public String getAsadminVersion() {
        String appserverVersion = null;
        
        //Determine version from asadmin version command.
        String asadminString = UpgradeConstants.ASADMIN_COMMAND;
        if(System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).indexOf("indows") != -1){
            asadminString = UpgradeConstants.ASADMIN_BAT;
        }
        String asAdminFileStr = this.installDir + "/" + 
                UpgradeConstants.AS_BIN_DIRECTORY +  "/" + asadminString;
        if(new File(asAdminFileStr).exists()){
            //-String execCommand = asAdminFileStr+" version";
            String execCommand = asAdminFileStr+" version --terse=true";

            try{
                java.lang.Process asadminProcess = 
                        Runtime.getRuntime().exec(execCommand);
                BufferedReader pInReader = 
                        new BufferedReader(new InputStreamReader(
                        asadminProcess.getInputStream()));
                String inLine = null;
                while((inLine = pInReader.readLine()) != null){
                    if((appserverVersion = this.parseVersion(inLine)) != null)
                        break;
                }
                asadminProcess.destroy();
            }catch(Exception ex){
                logger.log(Level.SEVERE, 
                        stringManager.getString("common.versionextracter.getVersionError"), 
                        ex);
            }
        }
        return appserverVersion;
    }
    
    /**
     * Method to parse the asadmin version string and get the version/edition
     * Returns version info for 9.1
     */
	private String parseVersion(String versionString){
		String v = null;
		String versionEqualsStr = stringManager.getString(
			"common.versionextracter.version_equals.string");
		if (versionString.startsWith(versionEqualsStr)){
			//- get all text as tokens in string.
			//- version number is always the last token
			String [] s = versionString.split(" ");
			v = s[s.length -1];
		}
		return v;
	}
 
    /**
     * Method to put together the version and edition (if any) in a simple format.
     */
	public String formatVersionEditionStrings(String v, String e ){
		return v + UpgradeConstants.DELIMITER  +e;
	}
  
    /**
     * Method to determine the version/edition information from the config file.
     */
    public String extractVersionFromConfigFile(String cfgFilename){
		String verEdStr = null;
        String versionString = null;
		File configFile = new File(cfgFilename);
		if (!configFile.exists() || !configFile.isFile()){
			return verEdStr;
		}
		
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(common);
        Document adminServerDoc = upgrUtils.getDomainDocumentElement(configFile.toString());

        try {
            String publicID = adminServerDoc.getDoctype().getPublicId();
            String appservString = stringManager.getString(
				"common.versionextracter.appserver.string");
			int indx = publicID.indexOf(appservString);
			if (indx > -1){
				//- product version is 1st token after the appserver text.
				String tmpS = publicID.substring(indx+appservString.length()).trim();
				String [] s = tmpS.split(" ");
				versionString = s[0];
			}

            verEdStr = formatVersionEditionStrings(versionString, UpgradeConstants.ALL_PROFILE);

        }catch (Exception ex){
            //- Very basic check that this contain V3 domain XML.
            Element rootElement = adminServerDoc.getDocumentElement();
            if (!"domain".equals(rootElement.getTagName())) {
                logger.log(Level.SEVERE, stringManager.getString("common.versionextracter.dtd_product_version_find_failured"), ex);
            } else {
                verEdStr=formatVersionEditionStrings(UpgradeConstants.VERSION_3_0, UpgradeConstants.ALL_PROFILE);
            }
        }
        return verEdStr;
    }
	
	public String getDTDFileName(String cfgFilename){
		String f = null;
		File configFile = new File(cfgFilename);
		if (!configFile.exists() || !configFile.isFile()){
			return f;
		}
		
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(common);
        Document adminServerDoc = upgrUtils.getDomainDocumentElement(configFile.toString());

        try {
            String systemID = adminServerDoc.getDoctype().getSystemId();
			String [] s= systemID.split("/");
			f = s[s.length-1];
		}catch (Exception ex){
            logger.log(Level.SEVERE, stringManager.getString("common.versionextracter.dtd_version_find_failured"),ex);
        }
        return f;
	}
    private String getTextNodeData(Element element){
		String c = null;
        NodeList children = element.getChildNodes();
        for(int index=0; index < children.getLength(); index++){
            if(children.item(index).getNodeType() == Node.TEXT_NODE){
                c = children.item(index).getNodeValue();
				break;
            }
        }
        return c;
    }

    public String getTargetDefaultProfile() {
        String defaultProfile= null;
        try {
            String path = this.installDir + "/" + 
                    UpgradeConstants.AS_CONFIG_DIRECTORY + "/" + 
                    UpgradeConstants.AS_ADMIN_ENV_CONF_FILE;
            File asadminenvFile = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(asadminenvFile));
            while( reader.ready() ) {
                String line = reader.readLine();
                if ( line.startsWith(UpgradeConstants.AS_PROPERTY_ADMIN_PROFILE) ) {
                    defaultProfile = line.substring(line.indexOf("=") + 1);
                    break;
                } else continue;
            }
            reader.close();
            if(defaultProfile == null) throw new Exception();
        } catch (Exception e) {
            logger.severe(stringManager.getString("upgrade.common.general_exception") + " " + e.getMessage());
            common.recover();	    
            System.exit(2);
        }
        return defaultProfile;
    }
	
    public static void main(String[] args){
    }
}
