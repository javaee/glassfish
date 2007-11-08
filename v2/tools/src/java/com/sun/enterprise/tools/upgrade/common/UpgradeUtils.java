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
 * Utils.java
 *
 * Created on April 19, 2004, 11:47 AM
 */

package com.sun.enterprise.tools.upgrade.common;

/**
 *
 * @author  prakash
 */

import java.io.*;
import java.util.*;
import java.lang.SecurityException;
import java.util.logging.*;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.cluster.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import com.sun.enterprise.tools.upgrade.transform.elements.BaseElement;
import sun.java2d.pipe.AATextRenderer;

public class UpgradeUtils {
    
    private StringManager stringManager = StringManager.getManager(LogService.UPGRADE_COMMON_LOGGER);
    
    private static Logger logger=LogService.getLogger(LogService.UPGRADE_LOGGER);
    private static UpgradeUtils upgradeUtils;
    private static CommonInfoModel common;
    
    /**
     * UpgradeUtils private constructor
     */
    private UpgradeUtils(CommonInfoModel common) {
        this.common = common;
    }
    
    /**
     * UpgradeUtils constructor
     */
    public static UpgradeUtils getUpgradeUtils(CommonInfoModel cim){
        if(upgradeUtils == null) {
            upgradeUtils = new UpgradeUtils(cim);
        } else {
            common = cim;
        }
        return upgradeUtils;
    }
    
    //NOT NEEDED SINCE DOMAINS ROOT IS NOT SUPPORTED
    /*public Hashtable enlistDomainsFromDomainsDirectory(String domainsDirStr){
        File domainsDir = new File(domainsDirStr);
        String [] dirs = domainsDir.list();
        return enlistDomains(dirs, domainsDirStr);
    }*/
    
    /**
     * Method to create backup directory if In-Place upgrade is done.
     * Also to build the domain mapping of the source.
     * @param cmi CommonInfoModel
     * @param dirs Contents of the domains root of source
     * @param domainRoot Domains root of the source
     */
    public Hashtable enlistDomains(String domainName, String domainRoot) {
        Hashtable domainMapping = new Hashtable();
        String domainPath = domainRoot + File.separator + domainName;
        String sourceDomainRoot = common.getSourceDomainRoot();
        String targetDomainRoot = common.getTargetDomainRoot();
        boolean domainRootSame =
                new File(sourceDomainRoot).equals(new File(targetDomainRoot));
        String profile = getProfileInfoFromSourceInput();
        
        //Inplace Upgrade if domain root is same for source and target.
        if(domainRootSame) {
            String latestdomain = findLatestDomainDir(sourceDomainRoot,domainName);
            String backupDomainVersion = "";
            String actualDomainVersion = "garbage";
            
            //Backup directory already exists, get version information
            if(latestdomain != null)  {
                backupDomainVersion =
                        (new VersionExtracter(latestdomain,common)).getVersion();
                String[] directories =
                        new File(common.getSourceDomainRoot()).list();
                String tempDomainName = null;
                for(int j =0;j<directories.length;j++) {
                    if(directories[j].equals("backup") ||
                            new File(common.getSourceDomainRoot() +
                            File.separator +
                            directories[j]).isFile())
                        continue;
                    else {
                        tempDomainName = directories[j];
                        break;
                    }
                }
                actualDomainVersion =  (new VersionExtracter(common.getSourceDomainRoot() +
                        File.separator +
                        domainName,common)).getVersion();
            }
            
            //Create backup of the source domain
            //Do only version-version checks since editions will remain same
            if(!actualDomainVersion.equals(common.getTargetVersion()) &&
                    !actualDomainVersion.equals(backupDomainVersion)) {
                common.setIsInPlace(true);
                String timestamp=""+System.currentTimeMillis();
                String newDomainName = domainName+"_"+timestamp;
                String backup = common.getTargetDomainRoot() + File.separator +
                        UpgradeConstants.BACKUP_DIR;
                File backupFile = new File(backup);
                
                //delete any existing backup directory and create a new one
                try {
                    deleteDirectory(backupFile);
                    if(!backupFile.mkdir()) {
                        throw new SecurityException();
                    }
                } catch(SecurityException se) {
                    logger.log(Level.SEVERE,
                            stringManager.getString("upgrade.common.make_directory_failed",
                            backupFile.getAbsolutePath()));
                    System.exit(1);
                }
                
                //Move the existing domain to backup directory
                File domainDir = new File(domainPath);
                String sourceDomainPath = backup + File.separator + newDomainName;
                //Copy and delete instead of rename because of Windows issue.
                try{
                    copyDirectory(domainDir, new File(sourceDomainPath));
                    if(!deleteDirectory(domainDir)) {
                        throw new SecurityException();
                    }
                }catch (Exception e) {
                    if(e instanceof IOException) {
                        logger.log(Level.SEVERE,
                                stringManager.getString("upgrade.common.copy_directory_failed",
                                domainDir.getAbsolutePath(), sourceDomainPath));
                        deleteDirectory(backupFile);
                        System.exit(1);
                    }
                    if(e instanceof SecurityException) {
                        logger.log(Level.WARNING,
                                stringManager.getString("upgrade.common.delete_backedup_domain_directory_failed",
                                domainDir.getAbsolutePath()));
                        System.exit(1);
                    }
                }
                
                //Add to domain mapping, the backed up source
                domainMapping.put(domainName,
                        new DomainInfo(domainName,sourceDomainPath,profile));
                common.addDomainName(domainName);
				common.setSourceInstallDir(sourceDomainPath);
                common.getTargetDomainNameMapping().put(domainName,domainName);
            } else {
                String latestDomain =
                        findLatestDomainDir(common.getTargetDomainRoot(),domainName);
                domainMapping.put(domainName,
                        new DomainInfo(domainName,latestDomain,profile));
                common.addDomainName(domainName);
				common.setSourceInstallDir(latestDomain);
                common.getTargetDomainNameMapping().put(domainName,domainName);
            }
        }
        //Side by Side Upgrade when domains root are different
        else {
            domainMapping.put(domainName,
                    new DomainInfo(domainName,domainPath,profile));
            common.addDomainName(domainName);
            common.getTargetDomainNameMapping().put(domainName,domainName);
        }
        return domainMapping;
    }
    
    //NOT NEEDED SINCE DOMAINS ROOT INPUT FOR SOURCE IS NOT VALID
    /*public Hashtable enlistDomainsFromUserDefinedDirectories(List srcDomainDirs){
        Hashtable domainMapping = null;
        for(Iterator dIt = srcDomainDirs.iterator(); dIt.hasNext();){
            if(domainMapping == null) domainMapping = new Hashtable();
            String domainDirStr = (String)dIt.next();
            Hashtable dMaps =
                 this.enlistDomainsFromDomainsDirectory(domainDirStr);
            if(dMaps != null){
                for(Enumeration domainsEnum =
                   dMaps.keys();domainsEnum.hasMoreElements();){
                    String dName = (String)domainsEnum.nextElement();
                    domainMapping.put(dName, dMaps.get(dName));
                }
            }
        }
        return domainMapping;
    }*/
    
    public String findLatestDomainDir(String domainRoot,String domainName) {
        String backupDirPath = domainRoot + File.separator +
                UpgradeConstants.BACKUP_DIR;
        File backupDir = new File(backupDirPath);
        String latestDomainPath = null;
        long latestTimestamp = 0;
        
        //If backup directory already exists return the directory name else null
        if(backupDir.isDirectory()) {
            String [] dirs = backupDir.list();
            for(int i=0;i<dirs.length;i++) {
                String time = dirs[i].substring(dirs[i].lastIndexOf("_")+1);
                if(domainName != null )
                    if(!(dirs[i].startsWith(domainName)))
                        continue;
                long timestamp = Long.parseLong(time);
                if(timestamp > latestTimestamp) {
                    latestTimestamp = timestamp;
                    latestDomainPath = backupDirPath + File.separator+dirs[i];
                }
            }
        }
        return  latestDomainPath;
    }
    
    /**
     * Method to find if the source input is a domain directory.
     */
    public boolean checkSourceInputAsDomain(){
        File domainXML = new File(common.getSourceInstallDir() +
                File.separator +
                "config" +
                File.separator +
                "domain.xml");
        if(domainXML.isFile()) {
            return true;
        }
        return false;
    }
    
    /**
     * Checks if the passed source directory is a domains root directory.
     */
    public boolean checkSourceInputAsDomainRoot(String source) {
        //If source is null, return false
        if (source == null) {
            return false;
        }
        File sourceDomain = new File(source);
        File biDir = new File(source + File.separator +
                UpgradeConstants.AS_BIN_DIRECTORY);
        
        //check for 7.x
        if(!biDir.isDirectory() && sourceDomain.isDirectory()) {
            //Now check wheather it is a domainRoot
            //by checking the existence of domain/server1/config/server.xml file
            String [] dirs = sourceDomain.list();
            if(dirs.length == 0) {
                common.setSourceDomainRootFlag(false);
                return false;
            }
            
            if(new File(source + File.separator +"backup").exists()) {
                common.setSourceDomainRootFlag(true);
                return true;
            }
            // Checking If it contains 8.0 domain
            for (int i=0;i<dirs.length;i++) {
                File domainXML = new File(source+
                        File.separator+dirs[i]+
                        File.separator+"config"+
                        File.separator+"domain.xml");
                if(domainXML.isFile()) {
                    common.setSourceDomainRootFlag(true);
                    return true;
                }
            }
            
            // Checking If it contains 7.0 domain
            //if(cmi.getSourceVersion().equals(UpgradeConstants.VERSION_7X)){
            // Get any 7.0 domain
            String domain = "";
            boolean domain70=false;
            for (int i=0;i<dirs.length;i++) {
                if(!(dirs[i].endsWith("8x")) &&
                        (new File(source +File.separator+ dirs[i]).isDirectory())&& this.isValid70Domain(source +File.separator+ dirs[i])) {
                    domain = dirs[i];
                    domain70 =true;
                    break;
                }
            }
            if(!domain70)
                return false;
            File domainDir = new File(source+File.separator+domain);
            String [] serverDirs = domainDir.list();
            String serverInstanceName ="";
            if(serverDirs[0].equals("admin-server"))
                serverInstanceName = serverDirs[1];
            else
                serverInstanceName = serverDirs[0];
            File serverXML = new File(source+File.separator+domain+
                    File.separator+serverInstanceName+
                    File.separator+"config"+
                    File.separator+"server.xml");
            if(serverXML.isFile())
                common.setSourceDomainRootFlag(true);
            else
                common.setSourceDomainRootFlag(false);
            /*}else {
                File domainXML = new File(source+File.separator+dirs[0]+File.separator+"config"+File.separator+"domain.xml");
                if(domainXML.isFile())
                    cmi.setSourceDomainRootFlag(true);
                else
                    cmi.setSourceDomainRootFlag(false);
            }  */
            
        }else {
            common.setSourceDomainRootFlag(false);
        }
        return common.getSourceDomainRootFlag();
    }
    
    public boolean isValid70Domain(String domainPath) {
        File domainDir = new File(domainPath);
        boolean domain70 = false;
        String [] serverDirs = domainDir.list();
        String serverInstanceName ="";
        if(serverDirs[0].equals("admin-server"))
            serverInstanceName = serverDirs[1];
        else
            serverInstanceName = serverDirs[0];
        File serverXML = new File(domainPath+File.separator+
                serverInstanceName+File.separator+
                "config"+File.separator+"server.xml");
        if(serverXML.isFile())
            domain70 = true;
        else
            domain70 = false;
        return domain70;
    }
    
    public String versionString(String versionEditionString){
        if(UpgradeConstants.VERSION_AS7X_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS7X_SE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS7X_EE.equals(versionEditionString)){
            return UpgradeConstants.VERSION_7X;
        }
        if(UpgradeConstants.VERSION_AS80_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS80_SE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS80_EE.equals(versionEditionString)){
            return UpgradeConstants.VERSION_80;
        }
        if(UpgradeConstants.VERSION_AS81_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS81_EE.equals(versionEditionString)){
            return UpgradeConstants.VERSION_81;
        }
        if(UpgradeConstants.VERSION_AS90_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS90_SE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS90_EE.equals(versionEditionString)){
            return UpgradeConstants.VERSION_90;
        }
        if(UpgradeConstants.VERSION_91.equals(versionEditionString)) {
            return UpgradeConstants.VERSION_91;
        }
        if(UpgradeConstants.VERSION_AS82_PE.equals(versionEditionString)||
                UpgradeConstants.VERSION_AS82_SE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS82_EE.equals(versionEditionString)){
            return UpgradeConstants.VERSION_82;
        }
        return null;
    }
    
    public String editionString(String versionEditionString){
        if(UpgradeConstants.VERSION_AS7X_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS80_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS81_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS82_PE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS90_PE.equals(versionEditionString)) {
            return UpgradeConstants.EDITION_PE;
        }
        if(UpgradeConstants.VERSION_AS7X_SE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS80_SE.equals(versionEditionString)||
                UpgradeConstants.VERSION_AS81_SE.equals(versionEditionString)||
                UpgradeConstants.VERSION_AS82_SE.equals(versionEditionString)||
                UpgradeConstants.VERSION_AS90_SE.equals(versionEditionString)){
            return UpgradeConstants.EDITION_SE;
        }
        if(UpgradeConstants.VERSION_AS7X_EE.equals(versionEditionString) ||
                UpgradeConstants.VERSION_AS81_EE.equals(versionEditionString)||
                UpgradeConstants.VERSION_AS81_EE.equals(versionEditionString)||
                UpgradeConstants.VERSION_AS90_EE.equals(versionEditionString)||
                UpgradeConstants.VERSION_AS82_EE.equals(versionEditionString)){
            return UpgradeConstants.EDITION_EE;
        }
        return null;
    }
    
    public boolean isValidSourcePath(String sourcePath){
        // If the directory does not exist throw an error instantly
        if(!(new File(sourcePath)).exists()) return false;
        File domainXML=null;
        // First check if it is regular 7.x install directory.
        String as7xInstallFileName = "admingui.ear"; //only exists in 7.x installations
        File runtime70Jar = new File(sourcePath+
                File.separator+
                "lib"+
                File.separator+
                as7xInstallFileName);
        if((runtime70Jar != null) && runtime70Jar.exists()) {
            // Its a valid 7.x directory.
            return true;
        }
        
        // Check if the source path is a domain directory that has config file
        domainXML = new File(sourcePath + File.separator + "config" +
                File.separator + "domain.xml");
        if(domainXML.exists() &&
                domainXML.isFile() ){
            return true;
        }
        
        // if none of the above match then its an invalid directory
        return false;
    }
    
    /**
     * Method to check if the target input is a domains root.
     * All other inputs are rejected.
     */
    public boolean isValidTargetPath(String targetPath) {
        // check if this path exists
        File targetPathDir = new File(targetPath);
        if( !targetPathDir.exists() ) {
            return false;
        }
        // check if this is an existing domain
        File domainXML = new File(targetPath + File.separator +
                "config" + File.separator +"domain.xml");
        if( domainXML.isFile() && domainXML.exists()) {
            return false;
        }
        // else its valid
        return true;
    }
    
    /**
     * Parses the domain.xml for the server elements
     * Returns the list of stand alone instances.
     */
    public List getStandAloneInstancesFromDomainXml() {
        Hashtable domainsMapping = common.getDomainMapping();
        List stdAloneList = new ArrayList();
        for(Iterator domIt = domainsMapping.values().iterator(); domIt.hasNext();) {
            DomainInfo dInfo = (DomainInfo) domIt.next();
            String domainName = dInfo.getDomainName();
            String sourceDomainPath = ((DomainInfo) common.getDomainMapping().get(domainName)).getDomainPath();
            String domainXmlFile = sourceDomainPath + File.separator +
                    "config" + File.separator + "domain.xml";
            
            Document adminServerDoc = getDomainDocumentElement(domainXmlFile);
            try {
                NodeList servers = adminServerDoc.getDocumentElement().
                        getElementsByTagName("servers");
                NodeList serverList = ((Element)servers.item(0)).
                        getElementsByTagName("server");
                for(int lh =0; lh < serverList.getLength(); lh++){
                    Element server = (Element)serverList.item(lh);
                    String instanceName = server.getAttribute("name");
                    String configRef = server.getAttribute("config-ref");
                    if((instanceName + "-config").equals(configRef)) {
                        Vector instDInfo = new Vector();
                        instDInfo.add(instanceName);
                        instDInfo.add(dInfo);
                        stdAloneList.add(instDInfo);
                    }
                }
            }catch (Exception ex){
                logger.log(Level.WARNING, stringManager.getString("upgrade.common.server_instances_from_XML"),ex);
            }
        }
        return stdAloneList;
    }
    
    public List getStandAloneInstances(Hashtable domainsMapping){
        // This method is called while upgradeing 7.x EE upgrading.
        //This compares with servers listed in clusters with domains mapping.
        List stdAloneList = new ArrayList();
        for(Iterator domIt=domainsMapping.values().iterator(); domIt.hasNext();){
            DomainInfo dInfo = (DomainInfo)domIt.next();
            for(Iterator instIt =
                    dInfo.getInstanceNames().iterator(); instIt.hasNext();){
                String instName = (String)instIt.next();
                if(this.isInstanceInCluster(instName)){
                    continue;
                }
                if( !instName.equals("admin-server") ) {
                    Vector instDInfo = new Vector();
                    instDInfo.add(instName);
                    instDInfo.add(dInfo);
                    stdAloneList.add(instDInfo);
                }
            }
        }
        return stdAloneList;
    }
    
    public boolean isInstanceInCluster(String instName){
        // This method is a utility method to check for the instnce clustered or not.
        List clList = ClustersInfoManager.getClusterInfoManager().getClusterInfoList();
        if(clList == null) return false;
        for(Iterator clIt = clList.iterator(); clIt.hasNext(); ){
            ClusterInfo clInfo = (ClusterInfo)clIt.next();
            for(Iterator instIt = clInfo.getClusteredInstanceList().iterator(); instIt.hasNext();){
                if(((ClusteredInstance)instIt.next()).getInstanceName().equals(instName)){
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean switchedIIOPPorts(String serverID,
            String portValue, Element documentElement){
        Element configEle =
                this.getConfigElementFromDocumentElement(documentElement,
                "server-config");
        NodeList iiopService = configEle.getElementsByTagName("iiop-service");
        NodeList iiopListeners =
                ((Element)iiopService.item(0)).getElementsByTagName("iiop-listener");
        Element iiopListener = null;
        for(int ii =0; ii < iiopListeners.getLength(); ii++){
            if(((Element)iiopListeners.item(ii)).
                    getAttribute("id").equals("orb-listener-1")){
                iiopListener = (Element)iiopListeners.item(ii);
                break;
            }
        }
        if(iiopListener != null){
            String serverIIOPPort =
                    this.getIIOPListenerPortForServer(
                    iiopListener.getAttribute("port"),
                    "server", documentElement);
            if(serverIIOPPort.equals("3700")){
                String portValueFromServer =
                        this.getIIOPListenerPortForServer(portValue,serverID,
                        documentElement);
                if(portValueFromServer != null)
                    iiopListener.setAttribute("port", portValueFromServer);
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
    
    public void updateDomainXMLWithIIOPCluster(CommonInfoModel cmnInfo,
            String domainXMLFile, IIOPCluster iiopCluster){
        String clusterName = iiopCluster.getClusterName();
        List servInstances = iiopCluster.getIIOPServerInstanceList();
        boolean domainFileChanged = false;
        Document domainDoc = this.getDomainDocumentElement(domainXMLFile);
        for(int servIt = 0; servIt < servInstances.size(); servIt++){
            List endPoints =
                    ((IIOPServerInstance)servInstances.get(servIt)).getIIOPEndPoints();
            String serverName =
                    ((IIOPServerInstance)servInstances.get(servIt)).getName();
            for(int endPt = 0; endPt < endPoints.size(); endPt++){
                IIOPEndPoint iiopEndPoint = (IIOPEndPoint)endPoints.get(endPt);
                // just use only one endpoint and transfer the port no.
                if(domainDoc != null){
                    String propertyName = "IIOP_LISTENER_PORT";
                    String propValue = iiopEndPoint.getPort();
                    NodeList servers =
                            domainDoc.getDocumentElement().
                            getElementsByTagName("servers");
                    NodeList serverList =
                            ((Element)servers.item(0)).
                            getElementsByTagName("server");
                    if(propValue.equals("3700")){
                        this.switchedIIOPPorts(serverName,
                                null, domainDoc.getDocumentElement());
                    }
                    // Passing baseElement as null.  "IIOP_LISTENER_PORT" should definitely exist.  Ssome times the create-instance does not add it.
                    // This base element is only a structural reference to invoke appendChild method.
                    BaseElement baseElement = null;
                    try{
                        baseElement =
                                com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper.getMapper().getElementObject(domainDoc.getDocumentElement().getTagName());
                    }catch(Exception ef){
                    }
                    this.addOrUpdateSystemPropertyToServer(serverName,
                            serverList, propertyName, propValue, baseElement);
                    domainFileChanged = true;
                }
                break;
            }
        }
        if(domainFileChanged && (domainDoc != null)){
            this.saveDocumentToDomainFile(domainXMLFile, domainDoc);
        }
    }
    
    /**
     * Builds the ClusteredInstance object for the particular cluster.
     * The source domain.xml is parsed for cluster and its associated instances.
     * Values are loaded in the ClusteredInstance object.
     */
    public void updateClusteredInstanceList(String domainXMLFile,
            String domainName,
            String clusterName,
            java.util.List clusteredInstanceList) {
        
        DomainInfo dInfo = (DomainInfo)common.getDomainMapping().get(domainName);
        String domainPath = dInfo.getDomainPath();
        //Get admin username
        String userName = common.getAdminUserName();
        
        DomainsProcessor dProcessor = new DomainsProcessor(common);
        //Get admin port
        String adminPort = dProcessor.getSourceAdminPort(domainPath);
        
        //Get domain.xml document
        Document domainDoc = this.getDomainDocumentElement(domainXMLFile);
        if(domainDoc != null){
            
            //Get the servers nodelist
            NodeList servers = domainDoc.getDocumentElement().
                    getElementsByTagName("servers");
            if(servers != null){
                
                //Get all the server elements under servers element
                NodeList serverList = ((Element)servers.item(0)).
                        getElementsByTagName("server");
                if((serverList != null) && (serverList.getLength() >0)){
                    
                    //Iterate through the server elements to get the attributes
                    for(int lh =0; lh < serverList.getLength(); lh++){
                        Element server = (Element) serverList.item(lh);
                        
                        //Get config-ref attribute
                        String configRefName = server.getAttribute("config-ref");
                        
                        //Check if the server instance belongs to a cluster
                        if((clusterName+"-config").equals(configRefName)) {
                            //Get server instance name
                            String instanceName = server.getAttribute("name");
                            //Get node-agent name
                            String nodeAgentName = server.getAttribute("node-agent-ref");
                            //Build clInstance
                            ClusteredInstance clInstance = new ClusteredInstance(instanceName);
                            clInstance.setHost(nodeAgentName);
                            clInstance.setDomain(domainName);
                            clInstance.setUser(userName);
                            clInstance.setPort(adminPort);
                            clusteredInstanceList.add(clInstance);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Builds the ClusterInfo object and loads the clusters in a list.
     * The domain.xml is parsed for all clusters.
     * Values are loaded in ClusterInfo object and clusterInfoList is updated.
     */
    public void updateClusterList(String domainXMLFile, String domainName,
            java.util.List clusterInfoList) {
        
        //Get the domain.xml document
        Document domainDoc = this.getDomainDocumentElement(domainXMLFile);
        if(domainDoc != null){
            
            //Get the clusters nodelist
            NodeList clusters = domainDoc.getDocumentElement().
                    getElementsByTagName("clusters");
            if(clusters != null){
                
                //Get the cluster elements under clusters element
                NodeList clusterList = ((Element)clusters.item(0)).
                        getElementsByTagName("cluster");
                if((clusterList != null) && (clusterList.getLength() >0)){
                    
                    //Iterate through the cluster elements to get details
                    for(int lh =0; lh < clusterList.getLength(); lh++){
                        
                        //Get cluster name
                        String clName = ((Element)clusterList.item(lh)).
                                getAttribute("name");
                        ClusterInfo clInfo = new ClusterInfo();
                        clInfo.setClusterName(clName);
                        
                        //Set domain name for this cluster
                        clInfo.setDomainName(domainName);
                        
                        //Update the instance list for this cluster
                        clInfo.updateClusteredInstanceList(domainXMLFile,
                                domainName, clName, this);
                        clusterInfoList.add(clInfo);
                    }
                }
            }
        }
    }
    
    public void recover() {
        List domainList = common.getDomainList();
        String sourceDir = common.getSourceDomainRoot();
        String targetDir = common.getTargetDomainRoot();
        if(domainList != null && sourceDir != null && targetDir != null) {
            Iterator itr = domainList.iterator();
            while(itr.hasNext()) {
                String dname = (String)itr.next();
                boolean domainRootSame = new File(sourceDir).equals(new File(targetDir));
                if(domainRootSame) {
                    if(new File(sourceDir + File.separator + "backup").isDirectory()) {
                        common.setCurrentDomain(dname);
                        String latestDomainPath = common.findLatestDomainDir(sourceDir);
                        if(latestDomainPath != null) {
                            //Copy and delete instead of rename because of Windows issue.
                            try{
                                copyDirectory(new File(latestDomainPath),
                                        new File(sourceDir + File.separator + dname));
                                File backupdir = new File(sourceDir + File.separator + "backup");
                                if(!deleteDirectory(backupdir)) {
                                    logger.log(Level.WARNING,
                                            stringManager.getString("upgrade.common.delete_directory_failed",
                                            backupdir.getAbsolutePath()));
                                }
                            }catch (IOException e) {
                                logger.log(Level.SEVERE,
                                        stringManager.getString("upgrade.common.copy_directory_failed",
                                        latestDomainPath, sourceDir));
                            }
                        }
                    }
                }
                
            }//while loop
        }//not null check of domainList
    }
    
    /**
     * Method to validate the admin credentials and
     * master password (if any provided)
     */
    public boolean validateUserDetails(String adminUserName,
            String adminPassword, String masterPassword){
        try{
            //RepositoryManager.validateAdminUserAndPassword method cannot validate credentials for 8.0 because of
            //changes in domain.xml and the file in which the credentials were stored has changed from keyfile
            // to adminkeyfile. Hence return true in this case without validating passwords.
            
            if(UpgradeConstants.VERSION_AS80_PE.equals(common.getSourceVersionAndEdition()))
                return true;
            
            com.sun.enterprise.admin.servermgmt.RepositoryManager rManager =
                    new com.sun.enterprise.admin.servermgmt.RepositoryManager();
            String domainName = "domain1";
            String newDomainName = null;
            String newDomainPath = null;
            List domainList = common.getDomainList();
            Iterator itr =  domainList.iterator();
            Hashtable domainMapping = common.getDomainMapping();
            String domainPath = null;
            String sourceInstallDir = common.getSourceInstallDir();
            String targetInstallDir = common.getTargetInstallDir();
            while(itr.hasNext()) {
                domainName = (String)itr.next();
            }
            if(domainMapping != null) {
                domainPath = ((DomainInfo) domainMapping.get(domainName)).getDomainPath();
            }
            //Added for SBS upgrade invoked from installer, where this method
            //is called before source directory is input
            if(sourceInstallDir == null) {
                return true;
            }
            if(domainPath != null) {
                newDomainName = domainPath.substring(domainPath.lastIndexOf(File.separator)+1);
                newDomainPath = domainPath.substring(0, domainPath.lastIndexOf(File.separator));
            }
            com.sun.enterprise.admin.servermgmt.RepositoryConfig repConf =
                    new com.sun.enterprise.admin.servermgmt.RepositoryConfig(
                    newDomainName, newDomainPath);
            rManager.validateAdminUserAndPassword(repConf,
                    adminUserName,adminPassword);
            if(masterPassword != null)
                rManager.validateMasterPassword(repConf,masterPassword);
        }catch(com.sun.enterprise.admin.servermgmt.RepositoryException re){
            logger.log(Level.SEVERE,
                    stringManager.getString("enterprise.tools.upgrade.generalException",
                    re.getMessage()), re);
            return false;
        }
        return true;
    }
    
    public void updateDomainXMLWithPersistenceStoreProps(java.util.Properties props){
        // TODO
        // Read each property and add it to appropriate place in domain.xml
        // Yet to hear back from Larry white.
    }
    
    public void updateListenerPortsForClusteredInstances(Element docElement, String propertyName, String portValue, BaseElement bElement){
        NodeList servers = docElement.getElementsByTagName("servers");
        NodeList serverList =
                ((Element)servers.item(0)).getElementsByTagName("server");
        String clusterName = this.common.getCurrentCluster();
        for(java.util.Iterator dItr =
                ClustersInfoManager.getClusterInfoManager().getClusterInfoList().iterator(); dItr.hasNext();){
            ClusterInfo cInfo = (ClusterInfo)dItr.next();
            if(cInfo.getClusterName().equals(clusterName)){
                for(java.util.Iterator clItr =
                        cInfo.getClusteredInstanceList().iterator();
                clItr.hasNext();){
                    ClusteredInstance clInstance = (ClusteredInstance)clItr.next();
                    if(portValue == null){
                        this.addOrUpdateSystemPropertyToServer(
                                clInstance.getInstanceName(),
                                serverList, propertyName,
                                clInstance.getInstancePort(),bElement);
                    }else{
                        this.addOrUpdateSystemPropertyToServer(
                                clInstance.getInstanceName(),
                                serverList, propertyName, portValue,bElement);
                    }
                }
            }
        }
    }
    
    public void addOrUpdateSystemPropertyToServer(String serverName,
            NodeList serverList, String propertyName,
            String propertyValue, BaseElement bElement){
        for(int lh =0; lh < serverList.getLength(); lh++){
            if(serverName.equals(((Element)serverList.item(lh)).
                    getAttribute("name"))){
                Element serverElement = (Element)serverList.item(lh);
                Element sysProp =
                        this.getSystemPropertyElement(propertyName,serverElement);
                if(sysProp != null){
                    sysProp.setAttribute("value", propertyValue);
                    return;
                }
                if(sysProp == null){
                    // bElement could be passed as null in case if this property need not be added.
                    if(bElement != null){
                        sysProp =
                                serverElement.getOwnerDocument().
                                createElement("system-property");
                        sysProp.setAttribute("name", propertyName);
                        sysProp.setAttribute("value", propertyValue);
                        bElement.appendElementToParent(serverElement,sysProp);
                    }
                }
                return;
            }
        }
    }
    
    public static void copyFile(String source, String target) throws IOException {
       FileUtils.copy(source, target);
    }
    
    //returns the value of the jvm-option or null if not found
    public String getJvmOptionValueFromSourceConfig(String optionName) {
        String sourceConfig = common.getSourceConfigXMLFile();
        Document sourceDoc =
                this.getDomainDocumentElement(common.getSourceConfigXMLFile());
        NodeList serverList = sourceDoc.getElementsByTagName("server");
        String serverName = common.getCurrentSourceInstance();
        if (serverName == null || serverName == "") {
            serverName = "server";
        }
        String configName = null;
        String password = null;
        for(int i=0;i<serverList.getLength();i++){
            Node serverNode = (Node)serverList.item(i);
            NamedNodeMap attributes = serverNode.getAttributes();
            String name = (attributes.getNamedItem("name")).getNodeValue();
            if(serverName.equals(name)){
                configName =
                        (attributes.getNamedItem("config-ref")).getNodeValue();
            }
        }
        if (configName != null) {
            NodeList jvmOptionList =
                    sourceDoc.getElementsByTagName("jvm-options");
            for(int j=0;j<jvmOptionList.getLength();j++) {
                Node jvmNode = jvmOptionList.item(j);
                String jvmOptions = getTextNodeData((Element)jvmNode);
                if (jvmOptions.indexOf(optionName) != -1) {
                    password = jvmOptions.substring(jvmOptions.indexOf("=")+1, jvmOptions.length());
                }
            }
        } else {
            logger.warning(stringManager.
                    getString("upgrade.common.config_not_found",
                    configName));
        }
        return password;
        
    }
    
    public String getTextNodeData(Element element){
        NodeList children = element.getChildNodes();
        for(int index=0; index < children.getLength(); index++){
            if(children.item(index).getNodeType() == Node.TEXT_NODE){
                return children.item(index).getNodeValue();
            }
        }
        return "";
    }
    
    /**
     * Copies the entire tree to a new location except the symbolic links
     * Invokes the FileUtils.java to do the same
     *
     * @param   sourceTree  File pointing at root of tree to copy
     * @param   destTree    File pointing at root of new tree
     *
     * If target directory does not exist, it will be created.
     *
     * @exception  IOException  if an error while copying the content
     */
    public static void copyDirectory(File sourceDir , File targetDir)
    throws IOException {
 
        /** 6597965 WBN Sept 17, 2007
         * The commented-out FileUtils call said that it does not copy empty directories
         * Not true.  It copies empty directories as of 9.1
         * The big hunk of commented-out code has a fat bug in it.
         * The fat bug: is marked inline in the commented-out code.  look for FATBUG.
         * The commented-out code should be deleted soon after thorough testing.
         */
        
        
        FileUtils.copyTree(sourceDir, targetDir, false);
 
        /***
        File [] srcFiles = sourceDir.listFiles();
        if (srcFiles != null) {
            for(int i=0; i< srcFiles.length; i++) {
                File dest = new File(targetDir, srcFiles[i].getName());
         // FATBUG if srcFiles[] is a sym link then this statement will be false,
         // and we will go into the else which mistakenly thinks it is a regular file
                if( srcFiles[i].isDirectory() && FileUtils.safeIsRealDirectory(srcFiles[i])) {
                    if (!dest.exists()) {
                        dest.mkdirs();
                    }
                    copyDirectory(srcFiles[i], dest);
                } else {
                    if (!dest.exists()) {
                        dest.createNewFile();
                    }
                    //FATBUG: The symlink will now get recursively copied, even though you don't
                    // want it to be.  FileUtils.copyFile() is just doing its job
                    copyFile(srcFiles[i].getAbsolutePath(), new File(targetDir, srcFiles[i].getName()).getAbsolutePath());
                    
                }
            }
        }
         ****/
        
    }
    
     /* Builds the application deployment parameters.
      * Builds an arraylist with the parameters enabled and virtual-servers
      * taken from the source domain.xml file.
      *
      */
    public ArrayList buildAppDeploymentParameters(String moduleName) {
        ArrayList appDeploymentParameters = null;
        String appEnabledStatus = null;
        String appVirtualServers = null;
        String sourceConfig = common.getSourceConfigXMLFile();
        Document sourceDoc =
                this.getDomainDocumentElement(common.getSourceConfigXMLFile());
        NodeList appRefList =
                sourceDoc.getDocumentElement().getElementsByTagName("application-ref");
        for(int i=0; i<appRefList.getLength(); i++) {
            Element element = ((Element)appRefList.item(i));
            if(null == element) {
            } else {
                if(element.getAttribute("ref").equals(moduleName)) {
                    appEnabledStatus = element.getAttribute("enabled");
                    appVirtualServers = element.getAttribute("virtual-servers");
                }
            }
        }
        if(appEnabledStatus != null || appVirtualServers != null) {
            appDeploymentParameters = new ArrayList();
        }
        if(appEnabledStatus != null) {
            appDeploymentParameters.add("--enabled=" + appEnabledStatus);
        }
        if(appVirtualServers != null && !(appVirtualServers.equals(""))) {
            appDeploymentParameters.add("--virtualservers");
            appDeploymentParameters.add(appVirtualServers);
        }
        return appDeploymentParameters;
    }
    
     /* Builds the web module deployment parameter context-root.
      * Returns a string with the parameters context-root value
      * taken from the source domain.xml file.
      *
      */
    public String buildWebModuleContextRoot(String moduleName) {
        String webModuleContextRoot = null;
        String actualModName = moduleName;
        if(common.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
            if(moduleName.lastIndexOf("_1") != -1) {
                actualModName = moduleName.substring(0,moduleName.lastIndexOf("_1"));
            } else {
                actualModName = moduleName;
            }
        }
        
        String sourceConfig = common.getSourceConfigXMLFile();
        Document sourceDoc =
                this.getDomainDocumentElement(common.getSourceConfigXMLFile());
        NodeList webModuleList =
                sourceDoc.getDocumentElement().getElementsByTagName("web-module");
        for(int i=0; i<webModuleList.getLength(); i++) {
            Element element = ((Element)webModuleList.item(i));
            if(null == element) {
            } else {
                if(element.getAttribute("name").equals(actualModName)) {
                    webModuleContextRoot = element.getAttribute("context-root");
                }
            }
        }
        return webModuleContextRoot;
    }
    
    public String getSourceApplicationRootDir() {
        String applicationRootDir = "applications";
        String applicationRoot = common.getSourceApplicationRoot();
        if(applicationRoot != null) {
            applicationRoot =
                    applicationRoot.replaceAll("/", "\\" + File.separator);
            String[] aryApplRoot = applicationRoot.split("\\" + File.separator);
            int indexApplRoot = (aryApplRoot.length) - 1;
            applicationRootDir = aryApplRoot[indexApplRoot];
        }
        
        return applicationRootDir;
    }
    
    /**
     * Builds the web module target.
     * Returns a string that is the target server for this web module.
     * It is taken from the source domain.xml file.
     * Used only for > 8.1/8.2EE domain.xmls
     */
    public String buildAppDeploymentTarget(String moduleName) {
        String appTarget = null;
        String actualModName = moduleName;
        
        String sourceConfig = common.getSourceConfigXMLFile();
        Document sourceDoc =
                this.getDomainDocumentElement(common.getSourceConfigXMLFile());
        Element sourceDocumentElement = sourceDoc.getDocumentElement();
        
        //Check for the occurence of the instance name in the clusters
        NodeList clusters = sourceDocumentElement.getElementsByTagName("clusters");
        if(clusters != null) {
            Element clustersEle = (Element)clusters.item(0);
            if(clustersEle != null) {
                NodeList clusterList = clustersEle.getElementsByTagName("cluster");
                if(clusterList != null) {
                    for(int k=0; k<clusterList.getLength(); k++) {
                        Element cluster = (Element)clusterList.item(k);
                        String clusterName = cluster.getAttribute("name");
                        NodeList clusterAppRefs = cluster.getElementsByTagName("application-ref");
                        for(int j = 0; j<clusterAppRefs.getLength(); j++) {
                            Element clusterAppRef = (Element) clusterAppRefs.item(j);
                            String appRef = clusterAppRef.getAttribute("ref");
                            if(actualModName.equals(appRef)) {
                                //The target is a cluster name
                                appTarget = clusterName;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(appTarget != null) {
            return appTarget;
        }
        NodeList servers = sourceDocumentElement.getElementsByTagName("servers");
        if(servers != null) {
            Element serversEle = (Element) servers.item(0);
            if(serversEle != null) {
                NodeList serverList = serversEle.getElementsByTagName("server");
                if(serverList != null) {
                    for(int lh =0; lh < serverList.getLength(); lh++){
                        Element server = (Element)serverList.item(lh);
                        String instanceName = server.getAttribute("name");
                        String configRef = server.getAttribute("config-ref");
                        //Get application refs for only stand alone servers
                        if((instanceName + "-config").equals(configRef)) {
                            NodeList applicationRefList = server.getElementsByTagName("application-ref");
                            for(int i = 0; i<applicationRefList.getLength(); i++) {
                                Element applicationRef = (Element) applicationRefList.item(i);
                                String ref = applicationRef.getAttribute("ref");
                                if(actualModName.equals(ref)) {
                                    //The target can be a stand alone instance name or the DAS itself.
                                    if(!("server".equals(instanceName))) {
                                        appTarget = instanceName;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return appTarget;
    }
    
    public static boolean deleteDirectory(File dir) {
        if(dir.isDirectory()) {
            String[] subDirs = dir.list();
            for(int i=0; i<subDirs.length; i++) {
                boolean success = deleteDirectory(new File(dir, subDirs[i]));
                if(!success) {
                    return false;
                }
            }
        }
        //Delete the empty directory
        return dir.delete();
    }
    
    public String getProfileInfoFromSourceInput() {
        String sourceEdition = common.getSourceEdition();
        if(UpgradeConstants.EDITION_PE.equals(sourceEdition)) {
            return UpgradeConstants.DEVELOPER_PROFILE;
        } else if(UpgradeConstants.EDITION_EE.equals(sourceEdition)) {
            return UpgradeConstants.ENTERPRISE_PROFILE;
        } else {
            //Source is SE
            return UpgradeConstants.DEVELOPER_PROFILE;
        }
    }
    
    private Element getConfigElementFromDocumentElement(Element documentElement, String lookUpConfigName){
        NodeList configEles = documentElement.getElementsByTagName("config");
        Element configEle = null;
        for(int lh =0; lh < configEles.getLength(); lh++){
            String configName = ((Element)configEles.item(lh)).getAttribute("name");
            if(configName.equals(lookUpConfigName)){
                configEle = (Element)configEles.item(lh);
                break;
            }
        }
        return configEle;
    }
    
    private String getIIOPListenerPortForServer(String portValue,
            String serverName, Element documentElement){
        try{
            if(portValue != null){
                int portIntValue = Integer.parseInt(portValue);
                return String.valueOf(portIntValue);
            }
        }catch(java.lang.NumberFormatException ne){
            // This shows the the portValue is not a string but ${IIOP_LISTENER_PORT}
        }
        NodeList servers = documentElement.getElementsByTagName("servers");
        NodeList serverList =
                ((Element)servers.item(0)).getElementsByTagName("server");
        String portValueFromServer = null;
        for(int lh =0; lh < serverList.getLength(); lh++){
            if(serverName.equals(((Element)serverList.item(lh)).getAttribute("name"))){
                Element serverElement = (Element)serverList.item(lh);
                Element sysProp =
                        getSystemPropertyElement("IIOP_LISTENER_PORT",serverElement);
                if(sysProp != null){
                    return sysProp.getAttribute("value");
                }else{
                    Element configElement =
                            this.getConfigElementFromDocumentElement(documentElement,
                            serverElement.getAttribute("config-ref"));
                    if(configElement != null){
                        sysProp = getSystemPropertyElement("IIOP_LISTENER_PORT",configElement);
                        if(sysProp != null)
                            return sysProp.getAttribute("value");
                    }
                }
            }
        }
        return null;
    }
    
    private Element getSystemPropertyElement(String propertyName,
            Element parentForSysProp){
        NodeList sysProps =
                parentForSysProp.getElementsByTagName("system-property");
        for(int sh =0; sh < sysProps.getLength(); sh++){
            if(propertyName.equals(((Element)sysProps.item(sh)).getAttribute("name"))){
                return ((Element)sysProps.item(sh));
            }
        }
        return null;
    }
    
    public Document getDomainDocumentElement(String domainFileName){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document resultDoc = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(
                    (org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            resultDoc = builder.parse( new File(domainFileName));
        }catch (Exception ex){
            logger.log(Level.WARNING,
                    stringManager.getString(
                    "upgrade.common.iiop_port_domain_doc"),ex);
        }
        return resultDoc;
    }
    
    private void saveDocumentToDomainFile(String domainFileName,
            Document resultDoc){
        // Use a Transformer for output
        try{
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            if (resultDoc.getDoctype() != null){
                String systemValue = resultDoc.getDoctype().getSystemId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                        systemValue);
                String pubValue = resultDoc.getDoctype().getPublicId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
                        pubValue);
            }
            DOMSource source = new DOMSource(resultDoc);
            StreamResult result =
                    new StreamResult(new FileOutputStream(domainFileName));
            transformer.transform(source, result);
            result.getOutputStream().close();
        }catch (Exception ex){
            logger.log(Level.WARNING,
                    stringManager.getString(
                    "upgrade.common.iiop_port_domain_save_fail"),ex);
        }
    }
    
    public static boolean deleteFile(String targetMasterPasswordFile) {
        File masterPasswordFile = new File(targetMasterPasswordFile);
        return masterPasswordFile.delete();
    }
}
