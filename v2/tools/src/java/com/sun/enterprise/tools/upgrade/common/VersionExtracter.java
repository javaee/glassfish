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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionExtracter {

    private String installDir;

    private StringManager stringManager = StringManager.getManager(LogService.UPGRADE_COMMON_LOGGER);
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private CommonInfoModel common;

    /** Creates a new instance of VersionExtracter */
    public VersionExtracter(String iD,CommonInfoModel common) {
        this.installDir = iD;
        this.common = common;
    }
    
    /*
     * Returns a version no and edition info if 8.x
     * returns only version info if 9.1
     * Returns null, if version cannot be retrived.
     */
    public String getVersion() {
        String appserverVersion = null;
        
        //Determine version from asadmin version command.
        String asadminString = UpgradeConstants.ASADMIN_COMMAND;
        if(System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER).indexOf("indows") != -1){
            asadminString = UpgradeConstants.ASADMIN_BAT;
        }
        String asAdminFileStr = this.installDir + File.separator + 
                UpgradeConstants.AS_BIN_DIRECTORY + 
                File.separator + asadminString;
        if(new File(asAdminFileStr).exists()){
            String execCommand = asAdminFileStr+" version";
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
        
        //If version was not found by asadmin version, determine this from 
        //the config file.
        if(appserverVersion == null) {
              appserverVersion = extractVersionFromConfigFile();
        }
        return appserverVersion;
    }
    
    /**
     * Method to parse the asadmin version string and get the version/edition
     * Returns version info for 9.1
     */
    private String parseVersion(String versionString){
        String appservString = stringManager.getString("common.versionextracter.appserver.string");
        String app7String = stringManager.getString("common.versionextracter.appserver.7string");
        if((versionString.indexOf(appservString) != -1) && (versionString.indexOf(app7String) != -1)){
            // Try extracting the string from configFile.
            String verEdString = extractVersionFromConfigFile();
            if(verEdString != null)
                return verEdString;
            else
                return UpgradeConstants.VERSION_AS7X_PE;
        }
        String app80String = stringManager.getString("common.versionextracter.appserver.80string");
        String appPEString = stringManager.getString("common.versionextracter.appserver.platformEdition");
        String appSEString = stringManager.getString("common.versionextracter.appserver.standardEdition");
        String appEEString = stringManager.getString("common.versionextracter.appserver.enterpriseEdition");
        if((versionString.indexOf(appservString) != -1) && (versionString.indexOf(app80String) != -1)){
            if(versionString.indexOf(appPEString) != -1){
                return UpgradeConstants.VERSION_AS80_PE;
            }else if(versionString.indexOf(appSEString) != -1){
                // Do we have 80 SE?
                return UpgradeConstants.VERSION_AS81_SE;
            }else if(versionString.indexOf(appEEString) != -1){
                // Do we have 80 EE?
                return UpgradeConstants.VERSION_AS81_EE;
            }
            return null;
        }
        String app81String = stringManager.getString("common.versionextracter.appserver.81string");
        if((versionString.indexOf(appservString) != -1) && (versionString.indexOf(app81String) != -1)){
            if(versionString.indexOf(appPEString) != -1){
                return UpgradeConstants.VERSION_AS81_PE;
            }else if(versionString.indexOf(appSEString) != -1){
                // Do we have 81 SE?
                return UpgradeConstants.VERSION_AS81_SE;
            }else if(versionString.indexOf(appEEString) != -1){
                // Do we have 81 EE?
                return UpgradeConstants.VERSION_AS81_EE;
            }
            return null;
        }
        String app90String = stringManager.getString("common.versionextracter.appserver.90string");
        if((versionString.indexOf(appservString) != -1) && (versionString.indexOf(app90String) != -1)){
            if(versionString.indexOf(appPEString) != -1){
                return UpgradeConstants.VERSION_AS90_PE;
            }else if(versionString.indexOf(appSEString) != -1){
                // Do we have 82 SE?
                return UpgradeConstants.VERSION_AS90_SE;
            }else if(versionString.indexOf(appEEString) != -1){
                return UpgradeConstants.VERSION_AS90_EE;
            }
            return null;
        }
	
        String app91String = stringManager.getString("common.versionextracter.appserver.91string");
        if((versionString.indexOf(appservString) != -1) && (versionString.indexOf(app91String) != -1)){
            //Edition deprecated. Hence return only version
            return UpgradeConstants.VERSION_91;		
        }
	
        String app82String = stringManager.getString("common.versionextracter.appserver.82string");
        if((versionString.indexOf(appservString) != -1) && (versionString.indexOf(app82String) != -1)){
            if(versionString.indexOf(appPEString) != -1){
                return UpgradeConstants.VERSION_AS82_PE;
            }else if(versionString.indexOf(appSEString) != -1){
                return UpgradeConstants.VERSION_AS82_SE;
            }else if(versionString.indexOf(appEEString) != -1){
                return UpgradeConstants.VERSION_AS82_EE;
            }
            return null;
        }
        return null;
    }
    
    /**
     * Method to put together the version and edition (if any) in a simple format.
     */
    public String formatVersionEditionStrings(String[] verEd){
        if(verEd != null){
            if(verEd[0].equals(UpgradeConstants.VERSION_7X)){
                if(verEd[1].equals(UpgradeConstants.EDITION_PE)){
                    return UpgradeConstants.VERSION_AS7X_PE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_SE)){
                    return UpgradeConstants.VERSION_AS7X_SE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_EE)){
                    return UpgradeConstants.VERSION_AS7X_EE;
                }
            }else if(verEd[0].equals(UpgradeConstants.VERSION_80)){
                if(verEd[1].equals(UpgradeConstants.EDITION_PE)){
                    return UpgradeConstants.VERSION_AS80_PE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_SE)){
                    return UpgradeConstants.VERSION_AS80_SE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_EE)){
                    return UpgradeConstants.VERSION_AS81_EE;
                }
            }else if(verEd[0].equals(UpgradeConstants.VERSION_81)){
                if(verEd[1].equals(UpgradeConstants.EDITION_PE)){
                    return UpgradeConstants.VERSION_AS81_PE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_SE)){
                    return UpgradeConstants.VERSION_AS81_SE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_EE)){
                    return UpgradeConstants.VERSION_AS81_EE;
                }
            }else if(verEd[0].equals(UpgradeConstants.VERSION_90)){
                if(verEd[1].equals(UpgradeConstants.EDITION_PE)){
                    return UpgradeConstants.VERSION_AS90_PE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_SE)){
                    return UpgradeConstants.VERSION_AS90_SE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_EE)){
                    return UpgradeConstants.VERSION_AS90_EE;
                }
            }else if(verEd[0].equals(UpgradeConstants.VERSION_91)){
                //Edition strings deprecated. return just the version.		    
                return UpgradeConstants.VERSION_91;		    
            }else if(verEd[0].equals(UpgradeConstants.VERSION_82)){
                if(verEd[1].equals(UpgradeConstants.EDITION_PE)){
	                return UpgradeConstants.VERSION_AS82_PE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_SE)){
                    return UpgradeConstants.VERSION_AS82_SE;
                }if(verEd[1].equals(UpgradeConstants.EDITION_EE)){
                    return UpgradeConstants.VERSION_AS82_EE;
                }
            }
        }
        return null;
    }
    
    /**
     * Method to determine the version/edition information from the config file.
     */
    public String extractVersionFromConfigFile(){
        String versionString = null;
        String editionString = null;
        File configFile = getConfigFile();
        
        if(configFile == null || !(configFile.exists()) ) {
            //Domain does not exist
            return null;
        }
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(common);
        Document adminServerDoc = upgrUtils.getDomainDocumentElement(configFile.toString());

        try {
            String publicID = adminServerDoc.getDoctype().getPublicId();
            String systemID = adminServerDoc.getDoctype().getSystemId();
            String appservString = stringManager.getString("common.versionextracter.appserver.string");
            //MIGHT REMOVE 7X
            String app7xPESEString = stringManager.getString("common.versionextracter.appserver.7xPESEConfigString");
            String app70EEString = stringManager.getString("common.versionextracter.appserver.70EEConfigString");
            String app71EEString = stringManager.getString("common.versionextracter.appserver.71EEConfigString");
            if((publicID.indexOf(appservString) != -1) && ((systemID.indexOf(app7xPESEString) != -1))){
                versionString = UpgradeConstants.VERSION_7X;
                // This could be either 7.0PE, 7.0SE. Same binary bits.  Just the licence is upgraded
                editionString = this.getEditionFor70PEAnd70SE(configFile);
            }
            if((publicID.indexOf(appservString) != -1) && (systemID.indexOf(app70EEString) != -1)){
                // Either 7.0EE or 7.1 SE
                versionString = UpgradeConstants.VERSION_7X;
                //editionString = UpgradeConstants.EDITION_EE;
                editionString = this.getEditionFor70EEAnd71SE(adminServerDoc);
            }
            if((publicID.indexOf(appservString) != -1) && (systemID.indexOf(app71EEString) != -1)){
                // 7.1 EE
                versionString = UpgradeConstants.VERSION_7X;
                editionString = UpgradeConstants.EDITION_EE;
            }
            //END - MIGHT REMOVE 7X
            String app80String = stringManager.getString("common.versionextracter.appserver.80ConfigString");
            if((publicID.indexOf(appservString) != -1) && (systemID.indexOf(app80String) != -1)){
                versionString = UpgradeConstants.VERSION_80;
                // There is no SE or EE.  There is only 80 PE.
                editionString = UpgradeConstants.EDITION_PE;
            }
            String app81String = stringManager.getString("common.versionextracter.appserver.81ConfigString");
            if((publicID.indexOf(appservString) != -1) && (systemID.indexOf(app81String) != -1)){
                versionString = UpgradeConstants.VERSION_81;
            }
            String app90String = stringManager.getString("common.versionextracter.appserver.90ConfigString");
            if((publicID.indexOf(appservString) != -1) && (systemID.indexOf(app90String) != -1)){
                versionString = UpgradeConstants.VERSION_90;
            }
            String app91String = stringManager.getString("common.versionextracter.appserver.91ConfigString");
            if((publicID.indexOf(appservString) != -1) && (systemID.indexOf(app91String) != -1)){
                versionString = UpgradeConstants.VERSION_91;
            }
            // Get edition only in the case of 8.x
            if(editionString == null && 
                    !(UpgradeConstants.VERSION_91.equals(versionString))){
                NodeList taggedElements = adminServerDoc.getDocumentElement().
                        getElementsByTagName("jvm-options");
                for(int lh =0; lh < taggedElements.getLength(); lh++){
                    Element element = (Element)taggedElements.item(lh);
                    String jvmOptionsData = getTextNodeData(element);
                    //MIGHT REMOVE 7X
                    if(versionString.equals(UpgradeConstants.VERSION_7X)){
                        if((jvmOptionsData.indexOf("EEORBInitializer") != -1) || (jvmOptionsData.indexOf("EEIIOPSocketFactory") != -1)){
                            // for 7X EE is already found from its dtd version.  only need to find if it is SE
                            editionString = UpgradeConstants.EDITION_SE;
                            break;
                        }
                    }//END MIGHT REMOVE 7X
                    else{
                        if((jvmOptionsData.indexOf("EEPluggableFeatureImpl") != -1)){
                            editionString = UpgradeConstants.EDITION_EE;
                            break;
                        }
                    }
                }
                // if EE is not found set to PE by default.
                if(editionString == null){
                    editionString = UpgradeConstants.EDITION_PE;
                }
            }
            return formatVersionEditionStrings(new String[]{versionString,editionString});
        }catch (Exception ex){
            logger.log(Level.SEVERE, stringManager.getString("common.versionextracter.transform_start_failure_message"),ex);
        }
        return null;
    }
    
    private String getEditionFor70PEAnd70SE(File configFile){
        // Since binary bits are same and the only difference is lincence upgrade, check for multiple instances.  If there are multiple instances
        // then it is SE, otherwise assume PE.
        String editionString = UpgradeConstants.EDITION_PE;
        java.util.Hashtable domainMapping = this.extractDomainsMapping();
        if(domainMapping != null && !domainMapping.isEmpty()){
            for(java.util.Iterator dIt = domainMapping.values().iterator(); dIt.hasNext(); ){
                DomainInfo dInfo = (DomainInfo)dIt.next();
                // If this domain has more than one instance, then it should be SE.
                if(dInfo.getInstanceNames().size() > 2)
                    return UpgradeConstants.EDITION_SE;
            }
            return UpgradeConstants.EDITION_PE;
        }else{
            // This will be the case of inplace upgrade or sols 10 integration.,
            String domainsDir = getDomainAndConfigDirs()[0];
            File domains[] = new File(domainsDir).listFiles();
            if(domains != null){
                for(int i=0; i < domains.length; i++){
                    // check if the item is a directory.
                    if(domains[i].isDirectory()){
                        // Get no. of instances in the directory.
                        if(domains[i].list().length > 2 && common.isValid70Domain(domains[i].getPath()))
                            return UpgradeConstants.EDITION_SE;
                    }
                }
                return UpgradeConstants.EDITION_PE;
            }
        }
        return UpgradeConstants.EDITION_PE;
    }
    
    private String getEditionFor70EEAnd71SE(Document doc){
        // Both 70EE and 71 EE uses the same dtd.  The differences are 1. EE has availability-service element, 2. EE has jvm-option wiht hadb root, 3. EE has session-config element
        NodeList taggedElements = doc.getDocumentElement().getElementsByTagName("jvm-options");
        for(int lh =0; lh < taggedElements.getLength(); lh++){
            Element element = (Element)taggedElements.item(lh);
            String jvmOptionsData = getTextNodeData(element);
            if((jvmOptionsData.indexOf("com.sun.aas.hadbRoot") != -1)){
                return UpgradeConstants.EDITION_EE;
            }
        }
        NodeList availabilityEles = doc.getDocumentElement().getElementsByTagName("availability-service");
        if(availabilityEles.getLength() != 0)
            return UpgradeConstants.EDITION_EE;
        // This can be uncommented as and when needed.  Not necessary for now.
        //NodeList availabilityEles = doc.getDocumentElement().getElementsByTagName("session-config");
        //if(availabilityEles.getLength() != 0)
            //return UpgradeConstants.EDITION_EE;
        return UpgradeConstants.EDITION_SE;
    }
    
    /**
     * 7.0 specific method (REMOVE)
     */
    private java.util.Hashtable extractDomainsMapping(){
        // If the source is 7.x then it is not guaranteed that domains directory lives under <install_dir>
        // Should create the domainMapping and obtain the config file.
        //This ear is a 7.0 specific file (MIGHT REMOVE)
        File runtime70Jar = new File(this.installDir+File.separator+"lib"+File.separator+"admingui.ear");
        boolean notTargetInstallation7x = true;
        //This check is required because target is not given yet as CLI input and target can not be 7.x
        //Check to find if the target is 7.x (REMOVE)
        if(common.getTargetInstallDir() !=null)
            notTargetInstallation7x = !(common.getTargetInstallDir().equals(this.installDir));
        if(runtime70Jar.exists() &&
                !(UpgradeUtils.getUpgradeUtils(common).checkSourceInputAsDomainRoot(this.installDir))
                && notTargetInstallation7x ) {
            // Its a valid 7.x directory.
            Appserver70DomainNamesResolver as =new Appserver70DomainNamesResolver(this.installDir);
            java.util.Hashtable domainsMapping = as.getDomainNamesPathMapping();
            common.setInstallConfig70(as.getConfigDir70(this.installDir));
            return domainsMapping;
        }
        return null;
    }
    
    private File getConfigFile(){
        //This will be non-empty only in case of 7.x source.(MIGHT REMOVE)
        java.util.Hashtable domainMapping = extractDomainsMapping();
        if(domainMapping != null && !domainMapping.isEmpty()){
            DomainInfo dInfo = (DomainInfo)domainMapping.values().iterator().next();
            // admin-server has server.xml that uses sun_server_1_0.dtd where as server1 uses other dtds.  So, lets get server.xml from server1 or other
            String instanceName = null;
            for(java.util.Iterator instIt = dInfo.getInstanceNames().iterator();instIt.hasNext();){
                instanceName = (String)instIt.next();
                if(!instanceName.equals("admin-server"))
                    break;
            }
            return new File(dInfo.getInstancePath(instanceName)+File.separator+"config"+File.separator+"server.xml");
        }
        //END _REMOVE
        String[] dCDirs = getDomainAndConfigDirs();
        if(dCDirs == null)
            return null;
        //MIGHT REMOVE 7X support
        if(dCDirs[1].indexOf("server1") != -1){
            return new File(dCDirs[1]+File.separator+"server.xml");
        }//END - REMOVE
        else{
            return new File(dCDirs[1]+File.separator+"domain.xml");
        }
    }
    
    public String[] getDomainAndConfigDirs(){
        String domainsDir = this.installDir+File.separator+"domains";
        String configDir = null;
        boolean domainRootSame = false;
        
        if(! (new File(domainsDir).exists())){
            domainsDir = this.installDir + File.separator + "domains_bak";
            if(! (new File(domainsDir).exists())){
                String[] chList = new File(this.installDir).list();
                if((chList == null)||(chList.length <= 0)){
                    //If the domain directory is empty, it is invalid
                    domainsDir = null;
                    configDir = null;
                }else{
                    domainsDir = this.installDir + File.separator + chList[0];
                    if(! (new File(domainsDir + File.separator + 
                            UpgradeConstants.AS_CONFIG_DIRECTORY).exists())){
                        //Checking if source input directory is some domain
                        //by verifying if it has a config directory.
                        String sourceConfigDir = installDir + File.separator +
                                UpgradeConstants.AS_CONFIG_DIRECTORY;
                        if(new File(sourceConfigDir).isDirectory()){
                            configDir = sourceConfigDir;
                            domainsDir = installDir;
                        }else {
                            // not a valid directory.
                            domainsDir = null;
                            configDir = null;
                        }
                    }else{
                        String domainsDir2 = domainsDir + File.separator + 
                                "server1";
                        if(! (new File(domainsDir2).exists())){
                            configDir = domainsDir + File.separator + "config";
                        }else{
                            configDir = domainsDir + File.separator + "server1" +
                                    File.separator + "config";
                        }
                        // installDir directory itself is domains dir
                        domainsDir = this.installDir;
                    }
                }
            }else{
                String[] chList = new File(domainsDir).list();
                configDir = domainsDir + File.separator + chList[0] +
                        File.separator + "config";
            }
        }//MIGHT REMOVE 7.0 SUPPORT
        else{
            String[] chList = new File(domainsDir).list();
            if(chList != null){
                String domainsDir2 = domainsDir+File.separator+chList[0]+File.separator+"server1";
                if(! (new File(domainsDir2).exists())){
                    configDir = domainsDir+File.separator+chList[0]+File.separator+"config";
                }else{
                    configDir = domainsDir+File.separator+chList[0]+File.separator+"server1"+File.separator+"config";
                }
            }
        }//END MIGHT REMOVE
        if(configDir == null){
            return null;
        }
        return new String[]{domainsDir,configDir};
    }
    
    private String getTextNodeData(Element element){
        NodeList children = element.getChildNodes();
        for(int index=0; index < children.getLength(); index++){
            if(children.item(index).getNodeType() == Node.TEXT_NODE){
                return children.item(index).getNodeValue();
            }
        }
        return null;
    }

    public String getTargetDefaultProfile() {
        String defaultProfile= null;
        try {
            String path = this.installDir + File.separator + 
                    UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator + 
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
