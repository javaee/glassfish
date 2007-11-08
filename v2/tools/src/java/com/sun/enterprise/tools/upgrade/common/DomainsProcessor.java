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
 * DomainsProcessor.java
 *
 * Created on November 20, 2003, 10:45 AM
 */

package com.sun.enterprise.tools.upgrade.common;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;

import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.i18n.StringManager;

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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.enterprise.tools.upgrade.cluster.*;

/**
 *
 * @author  prakash
 * @author  hans
 */


public class DomainsProcessor {
    private CommonInfoModel commonInfo;
    private java.util.Vector httpSSLPorts;
    private java.util.Vector iiopSSLPorts;
    private java.util.Vector iiopMutualAuthPorts;
    private java.util.Vector sourceXMLCorePorts;
    private java.util.Vector adminJMXPorts;
    private java.util.Vector iiopPorts;
    private java.util.Vector httpPorts;
    private static int iiopPortToStartWith = 1071;
    private static int adminPortToStartWith = 4858;
    
    private static Logger logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private static StringManager stringManager = StringManager.getManager(LogService.UPGRADE_COMMON_LOGGER);
    
    private boolean domainStarted = false;
    
    /** Creates a new instance of DomainsProcessor */
    public DomainsProcessor(CommonInfoModel ci) {
        this.commonInfo = ci;
        httpSSLPorts = new java.util.Vector();
        iiopSSLPorts = new java.util.Vector();
        iiopMutualAuthPorts = new java.util.Vector();
        sourceXMLCorePorts = new java.util.Vector();
        adminJMXPorts = new java.util.Vector();
        iiopPorts = new java.util.Vector();
        httpPorts = new java.util.Vector();
        String targetDomainRoot = commonInfo.getTargetDomainRoot();
        // Prefill with ports used in default domain and samples domain.
        File domainDir =  new File(targetDomainRoot + File.separator + "domain1");
        if(domainDir.exists()){
            String targetDomainFile = targetDomainRoot + File.separator +
                    "domain1" + File.separator + "config" +
                    File.separator + "domain.xml";
            if(domainDir.isDirectory() &&
                    //MIGHT REMOVE
                    !(ci.isValid70Domain(targetDomainRoot + File.separator + "domain1")) &&
                    (new File(targetDomainFile)).exists())  {
                //END MIGHT REMOVE
                String htSSP = getPortFromXML(targetDomainFile, "http-listener", "http-listener-2");
                if(htSSP != null) httpSSLPorts.add(htSSP);
                String iiSSP = getPortFromXML(targetDomainFile, "iiop-listener", "SSL");
                if(iiSSP != null) iiopSSLPorts.add(iiSSP);
                String iiMAP = getPortFromXML(targetDomainFile, "iiop-listener", "SSL_MUTUALAUTH");
                if(iiMAP != null) iiopMutualAuthPorts.add(iiMAP);
                String adminJMX = getJMXPortFromXML(targetDomainFile, "jmx-connector", "system");
                if(adminJMX != null) adminJMXPorts.add(adminJMX);
                String iiP = getPortFromXML(targetDomainFile, "iiop-listener", "orb-listener-1");
                if(iiP != null) iiopPorts.add(iiP);
                String httpP = getPortFromXML(targetDomainFile, "http-listener", "http-listener-1");
                if(httpP != null) httpPorts.add(httpP);
            }
        }
        
        File samplesDir =  new File(targetDomainRoot + File.separator + "samples");
        if(samplesDir.isDirectory() &&
                !(ci.isValid70Domain(targetDomainRoot + File.separator + "samples")) )   {
            // Now add sample domain ports if there is one
            String sampleDomainFile = targetDomainRoot + File.separator +
                    "samples" + File.separator + "config" + File.separator + "domain.xml" ;
            if(new File(sampleDomainFile).exists()){
                String shtSSP = getPortFromXML(sampleDomainFile, "http-listener", "http-listener-2");
                if(shtSSP != null) httpSSLPorts.add(shtSSP);
                String siiSSP = getPortFromXML(sampleDomainFile, "iiop-listener", "SSL");
                if(siiSSP != null) iiopSSLPorts.add(siiSSP);
                String siiMAP = getPortFromXML(sampleDomainFile, "iiop-listener", "SSL_MUTUALAUTH");
                if(siiMAP != null) iiopMutualAuthPorts.add(siiMAP);
                String sadminJMX = getJMXPortFromXML(sampleDomainFile, "jmx-connector", "system");
                if(sadminJMX != null) adminJMXPorts.add(sadminJMX);
                String siiP = getPortFromXML(sampleDomainFile, "iiop-listener", "orb-listener-1");
                if(siiP != null) iiopPorts.add(siiP);
                String shttpP = getPortFromXML(sampleDomainFile, "http-listener", "http-listener-1");
                if(shttpP != null) httpPorts.add(shttpP);
            }
        }
    }
    
    public void processTargetDomains() throws HarnessException{
        List domainNames = commonInfo.getDomainList();
        String target = commonInfo.getTargetInstallDir();
        String sourceEdition = commonInfo.getSourceEdition();
        UpgradeUtils upgradeUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
        
        //Following lines are copied from IasAdminCommand
        ASenvPropertyReader reader = new ASenvPropertyReader(
                System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
        reader.setSystemProperties();
        
        //Build the ClusterInfo object for use.
        if(UpgradeConstants.EDITION_EE.equals(sourceEdition)) {
            commonInfo.processDomainXmlForClusters();
        }
        for(int i=0; i<domainNames.size();i++) {
            String sourceDomainName = (String)domainNames.get(i);
            String targetDomainName=sourceDomainName;
            String targetDomainRoot = commonInfo.getTargetDomainRoot();
            
            if(!new File(targetDomainRoot).isDirectory()) {
                logger.log(Level.INFO,stringManager.getString(
                        "upgrade.common.not_valid_target_install"));
                return;
            }
            
            //Delete existing domain in the target domains root
            File targetDomainUpgrade = new File(targetDomainRoot +
                    File.separator + targetDomainName);
            boolean targetDomainDeleted = false;
            if(!commonInfo.isInPlace()) {
                //Delete the existing domain in a SBS upgrade
                if(targetDomainUpgrade.isDirectory()) {
                    targetDomainDeleted = upgradeUtils.deleteDirectory(targetDomainUpgrade);
                    if(targetDomainDeleted) {
                        logger.log(Level.INFO, stringManager.getString(
                                "upgrade.common.existing_target_domain_deleted"));
                    } else {
                        logger.log(Level.SEVERE, stringManager.getString(
                                "upgrade.common.delete_existing_target_domain_failed"));
                        UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
                    }
                } else {
                    targetDomainDeleted = true;
                }
                if(targetDomainDeleted) {
                    //delete target nodeagents contents
                    File targetNodeagentsDir = new File(target, "nodeagents");
                    String sourceDomainDir = commonInfo.getSourceInstallDir();
                    String sourceInstallDir = sourceDomainDir.substring(0,sourceDomainDir.lastIndexOf("domains"));
                    File sourceNodeagentsDir = new File(sourceInstallDir, "nodeagents");
                    
                    // Existing nodeagents dir is remove and recreated
                    // non existant nodeagent dir is created cr6593332
                    if (targetNodeagentsDir.exists()){
                        if (upgradeUtils.deleteDirectory(targetNodeagentsDir)) {
                            targetNodeagentsDir.mkdir();
                        } else {
                            logger.warning("Unable to delete existing nodeagents config!");
                        }
                    } else {
                        targetNodeagentsDir.mkdir();
                    }
                }
            } else {
                targetDomainDeleted = true;
            }
            
            //Create domain after domain deletion
            if(targetDomainDeleted) {
                String[] createDomainCommand = this.getCreateDomainCommand(
                        sourceDomainName,targetDomainName);
                boolean canContinue = executeCommand(createDomainCommand);
                UpdateProgressManager.getProgressManager().setContinueUpgrade(canContinue);
                if(!canContinue) {
                    throw new HarnessException(stringManager.getString(
                            "upgrade.common.domain_creation_error",
                            targetDomainName));
                }
                UpdateProgressManager.getProgressManager().
                        processUpgradeUpdateEvent((i*30)/domainNames.size());
            }
            
            //Set admin-listener port and security-enabled attribute
            this.setAdminPortAndSecurity(sourceDomainName,targetDomainName);
            
            //Set orb-listener-1 port
            this.setServerIIOPPort(sourceDomainName, targetDomainName);
        }
    }
    
    /**
     * Builds the create-domain command with all its options
     */
    private String[] getCreateDomainCommand(String domainName, String domainName80) {
        /**
         * Port type                     Appserver 8.0                  Appserver 7.0
         * 1. Http                        8080  http-listener-1          80 or 1024- http-listener-1
         * 2. http_ssl                    1043   - http-listener-2       -------- dont know --------
         * 3. JMS                         7676  - default_jms_host       7676 - jms-service
         * 4. IIOP                        3700  - orb-listener-1         3700 - orb-listener-1
         * 5. iiop_ssl                    1060 - ssl                     ---------- dont know -------
         * 6. iiop_mutual_auth            1061 -ssl-math                 ----------   dont know -----
         * 7. admin port                  4848  - admin-listener (httplistener)    4848 - stored in admin-server server.xml
         *
         * http, jms, iiop, and admin ports are transferred from appserver 7.0.
         * Need to pick up appropriate ports for http_ssl, iiop_ssl and iiop_mutual_auth.
         */
        
        //Get domain information
        String target = commonInfo.getTargetDomainRoot();
        DomainInfo dInfo = (DomainInfo)this.commonInfo.getDomainMapping().get(domainName);
        String domainPath = dInfo.getDomainPath();
        String profile = dInfo.getProfile();
        
        //Get port information.
        String httpPort = getAFreePort(8080,10);
        String adminPort = getSourceAdminPort(domainPath);
        String jmsPort = "7676";
        String iiopPort = getSourceIIOPPort(domainPath);
        String httpSSLPort = getAFreePort(8081,10);
        String iiopSSLPort = getAFreePort(3820,10);
        String iiopSSLMutualAuth = getAFreePort(3920,10);
        String adminJMXPort = getAFreePort(8686,10);
        
        //Store these ports in the respective maps to prevent duplication
        iiopPorts.add(iiopPort);
        adminJMXPorts.add(adminJMXPort);
        httpSSLPorts.add(httpSSLPort);
        iiopSSLPorts.add(iiopSSLPort);
        iiopMutualAuthPorts.add(iiopSSLMutualAuth);
        String instancePortAttr = "";
        if(httpPort != null) {
            instancePortAttr = "--instanceport " + httpPort;
        }
        
        //Build properties string with the port values derived
        String propertiesString = "http.ssl.port=" + httpSSLPort + ":orb.ssl.port=" +
                iiopSSLPort + ":orb.mutualauth.port=" + iiopSSLMutualAuth +
                ":jms.port=" + jmsPort + ":orb.listener.port=" + iiopPort;
        propertiesString = propertiesString + ":domain.jmxPort=" + adminJMXPort;
        
        String[] createDomainCommand = {"create-domain",
        "--profile",
        "\"" + profile + "\"",
        "--domaindir",
        "\"" + target +"\"",
        "--adminport",
        adminPort,
        "--user",
        commonInfo.getAdminUserName(),
        "--passwordfile ",
        "\"" + commonInfo.getPasswordFile() +"\"",
        "--savemasterpassword=true",
        instancePortAttr,
        "--domainproperties",
        propertiesString,
        domainName80
        };
        return createDomainCommand;
    }
    
    /*public static String getSourceAdminPort() {
        return getSourceAdminPort(commonInfo.getSourceDomainPath());
    }*/
    
    // This method returns admin port from admin-server server.xml
    public static String getSourceAdminPort(String domainPath) {
        String domainXmlFile = domainPath + File.separator + "config" + File.separator + "domain.xml";
        if(new File(domainXmlFile).exists()){
            String port = getPortFromXML(domainXmlFile,"http-listener", "admin-listener");
            if(port != null)
                return port;
        } else {
            // return some default no....user should change it later.
            adminPortToStartWith =+10;
        }
        return String.valueOf(adminPortToStartWith);
    }
    
    public static String getSourceAdminSecurity(String domainPath) {
        String domainXmlFile = domainPath + File.separator +
                UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator +
                "domain.xml";
        if(new File(domainXmlFile).exists()){
            String securityEnabled = getSecurityEnabledFromXML(
                    domainXmlFile,"http-listener", "admin-listener");
            if(securityEnabled != null)
                return securityEnabled;
        }
        return "true"; // default to true
    }
    
    /*public static String getSourceAdminSecurity() {
        String domainPath = commonInfo.getSourceDomainPath();
        return getSourceAdminSecurity(domainPath);
    }*/
    
    /**
     * Sets admin-listener port and security-enabled attribute from source domain
     */
    private void setAdminPortAndSecurity(String sourceDomainName,
            String targetDomainName){
        DomainInfo dInfo = (DomainInfo)this.commonInfo.
                getDomainMapping().get(sourceDomainName);
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
        String domainPath = dInfo.getDomainPath();
        String sourceEdition = commonInfo.getSourceEdition();
        String targetDomainRoot = commonInfo.getTargetDomainRoot();
        String adminPort = getSourceAdminPort(domainPath);
        String securityEnabled = getSourceAdminSecurity(domainPath);
        String domainFileName = targetDomainRoot + File.separator +
                targetDomainName + File.separator +
                UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator +
                "domain.xml";
        
        Document resultDoc = upgrUtils.getDomainDocumentElement(domainFileName);
        
        try {
            NodeList taggedElements = resultDoc.getDocumentElement().
                    getElementsByTagName("http-listener");
            for(int lh =0; lh < taggedElements.getLength(); lh++){
                Element element = (Element)taggedElements.item(lh);
                // Compare id attribute of http-listener elements.
                if((element.getAttribute("id")).equals("admin-listener")){
                    element.setAttribute("port",adminPort);
                    if(!UpgradeConstants.EDITION_EE.equals(sourceEdition)) {
                        element.setAttribute("security-enabled", securityEnabled);
                    }
                    break;
                }
            }
            
            //Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            if (resultDoc.getDoctype() != null){
                String systemValue = resultDoc.getDoctype().getSystemId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
                String pubValue = resultDoc.getDoctype().getPublicId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pubValue);
            }
            
            DOMSource source = new DOMSource(resultDoc);
            StreamResult result = new StreamResult(new FileOutputStream(domainFileName));
            transformer.transform(source, result);
            result.getOutputStream().close();
            
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_port_from_XML"),ex);
        }
    }
    
    /**
     * Gets port value from domain.xml
     * @param fileName domain.xml from which port is to be got
     * @param tagName Element in which attribute is found
     * @param id Attribute that is to be retrieved
     */
    private static String getPortFromXML(String fileName, String tagName,
            String id) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse( new File(fileName));
            
            NodeList taggedElements = adminServerDoc.getDocumentElement().
                    getElementsByTagName(tagName);
            for(int lh =0; lh < taggedElements.getLength(); lh++){
                Element element = (Element)taggedElements.item(lh);
                // Compare id attribute of http-listener elements.
                if((element.getAttribute("id")).equals(id)){
                    return element.getAttribute("port");
                }
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_port_from_XML"),ex);
        }
        return null;
    }
    
    /**
     * Gets security-enabled attribute value from domain.xml for the
     * particular http-listener
     */
    private static String getSecurityEnabledFromXML(String fileName,
            String tagName, String id) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse( new File(fileName));
            
            NodeList taggedElements = adminServerDoc.getDocumentElement().
                    getElementsByTagName(tagName);
            for(int lh =0; lh < taggedElements.getLength(); lh++){
                Element element = (Element)taggedElements.item(lh);
                // Compare id attribute of http-listener elements.
                if((element.getAttribute("id")).equals(id)){
                    return element.getAttribute("security-enabled");
                }
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_admin_security_from_XML"),ex);
        }
        return null;
    }
    
    /**
     * This method is called only for 8.2ee -> 9.1ee upgrade.
     * Retrieves the node-agent for the particular instance from the source domain.xml.
     */
    private String getSourceNodeAgentForInstance(String domainName,
            String serverName) {
        String sourceDomainPath = ((DomainInfo)this.commonInfo.getDomainMapping().get(domainName)).getDomainPath();
        String domainXmlFile = sourceDomainPath + File.separator +
                "config" + File.separator + "domain.xml";
        String nodeAgentName = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse( new File(domainXmlFile));
            NodeList servers = adminServerDoc.getDocumentElement().getElementsByTagName("servers");
            NodeList serverList =
                    ((Element)servers.item(0)).getElementsByTagName("server");
            for(int lh =0; lh < serverList.getLength(); lh++){
                Element server = (Element)serverList.item(lh);
                if(serverName.equals(server.getAttribute("name"))) {
                    //Get the node-agent ref for the specific stand-alone server
                    nodeAgentName = server.getAttribute("node-agent-ref");
                    break;
                }
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
        }
        return nodeAgentName;
    }
    
    private String getTargetNodeAgentName(String domainName,
            CommonInfoModel commonInfoMod){
        String configFileName = commonInfoMod.getTargetDomainRoot() + File.separator
                + domainName + File.separator +"config" + File.separator + "domain.xml";
        return getNodeAgentNameFromXML(configFileName);
    }
    
    private String getNodeAgentNameFromXML(String fileName) {
        if(commonInfo.getSourceVersion().equals(UpgradeConstants.VERSION_7X) || ! new File(fileName).exists()) {
            return null;
        }
        String nodeAgentName = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse( new File(fileName));
            NodeList taggedElements = adminServerDoc.getDocumentElement().getElementsByTagName("node-agent");
            Element element = (Element)taggedElements.item(0);
            if(null == element) {
                logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_nodeAgent_from_XML"));
                return null;
            } else {
                return element.getAttribute("name");
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
        }
        return null;
    }
    
    private boolean nodeAgentExists(String agentName, String fileName){
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
        Document adminServerDoc = upgrUtils.getDomainDocumentElement(fileName);
        try {
            NodeList taggedElements = adminServerDoc.getDocumentElement().
                    getElementsByTagName("node-agent");
            for(int lh =0; lh < taggedElements.getLength(); lh++){
                Element itElement = ((Element)taggedElements.item(lh));
                String attrName = itElement.getAttribute("name");
                if(attrName.equals(agentName))
                    return true;
                // If the agentName contains .  example beacon.east, then should only compare to beacon.
                if(agentName.indexOf(".") != -1){
                    if(agentName.substring(0,agentName.indexOf(".")).equals(attrName)){
                        return true;
                    }
                }
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
        }
        return false;
    }
    
    /**
     * Gets admin-listener port from target domain.xml
     */
    public static String getTargetDomainPort(String domainName,
            CommonInfoModel commonInfoMod){
        String configFileName = commonInfoMod.getTargetDomainRoot() +
                File.separator + domainName + File.separator +
                UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator +
                "domain.xml";
        return getPortFromXML(configFileName, "http-listener", "admin-listener");
    }
    
    /**
     * Gets security-enabled attribute from target domain.xml
     */
    public static String getTargetDomainSecurity(String domainName,
            CommonInfoModel commonInfoMod){
        String configFile = commonInfoMod.getTargetDomainRoot() +
                File.separator + domainName + File.separator +
                UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator +
                "domain.xml";
        return getSecurityEnabledFromXML(configFile, "http-listener",
                "admin-listener");
    }
    
    private String getAFreePort(int initPortNumber, int increment){
        // Do this only for 20 iterations.  If you cant find one in 10 iterations, then just assign some random number.
        int portNumber = initPortNumber;
        for(int i=0; i<20; i++){
            if(isPortNumberUsable(portNumber))
                return Integer.toString(portNumber);
            portNumber +=increment;
        }
        // In 20 iterations, if we cant find a free port, just return the last port number itereated.
        return Integer.toString(portNumber);
    }
    
    private boolean isPortNumberUsable(int portNumber){
        // First check if this port exists in any of the lists we have
        String portString = Integer.toString(portNumber);
        for(int i=0; i<httpSSLPorts.size(); i++){
            if(((String)httpSSLPorts.get(i)).equals(portString))
                return false;
        }
        for(int i=0; i<iiopSSLPorts.size(); i++){
            if(((String)iiopSSLPorts.get(i)).equals(portString))
                return false;
        }
        for(int i=0; i<iiopMutualAuthPorts.size(); i++){
            if(((String)iiopMutualAuthPorts.get(i)).equals(portString))
                return false;
        }
        for(int i=0; i<sourceXMLCorePorts.size(); i++){
            if(((String)sourceXMLCorePorts.get(i)).equals(portString))
                return false;
        }
        for(int i=0; i<adminJMXPorts.size(); i++){
            if(((String)adminJMXPorts.get(i)).equals(portString))
                return false;
        }
        for(int i=0; i<iiopPorts.size(); i++){
            if(((String)iiopPorts.get(i)).equals(portString))
                return false;
        }
        for(int i=0; i<httpPorts.size(); i++){
            if(((String)httpPorts.get(i)).equals(portString))
                return false;
        }
        // This NetUtils class is in appserv-commons module.
        return com.sun.enterprise.util.net.NetUtils.isPortFree(portNumber);
    }
    
    private java.util.List getInstanceNamesWithoutAdminServerAndServer1(java.util.List instanceList){
        java.util.List newList = new java.util.ArrayList();
        for(java.util.Iterator it = instanceList.iterator(); it.hasNext();){
            String serverName = (String)it.next();
            if(serverName.equals("admin-server"))
                continue;
            newList.add(serverName);
        }
        return newList;
    }
    
    public boolean createNodeAgent(String agentName, String dasHost,
            String dasPort, String dasuser, String daspwd,
            String domainName, String clientHostName) throws HarnessException {
        //only create agents for the instance we are running on
        String myAddress = null;
        String clientAddress = null;
        try {
            myAddress = InetAddress.getByName(dasHost).getHostAddress();
            clientAddress = InetAddress.getByName(clientHostName).getHostAddress();
        } catch (Exception e) {
            if(clientAddress == null) {
                logger.warning(stringManager.getString("upgrade.common.domain_processor_could_not_resolve_client_hostname") + " " + clientHostName);
                clientAddress = "unknown"; //set it something that wont match and don't cerate a node-agent
            }
            if(myAddress == null)
                myAddress = "127.0.0.1"; //localhost
        }
        if(clientAddress.equals(myAddress) || clientHostName.equals("localhost")) {
            String adminSecurity = getTargetDomainSecurity(domainName, commonInfo);
            System.setProperty(INSTANCE_ROOT_PROPERTY, commonInfo.getTargetInstallDir());
            String agentProperties = "remoteclientaddress=" + clientHostName;
            if(agentProperties != null && !agentProperties.equals("")) {
                agentProperties = "--agentproperties " + agentProperties;
            } else {
                agentProperties = "";
            }
            String[] command = {"create-node-agent", "--host", dasHost,
            "--port", dasPort, "--secure="+adminSecurity, "--user", dasuser,
            "--passwordfile ", "\"" + commonInfo.getPasswordFile() +"\"",
            agentProperties, agentName };
            return this.executeCommand(command);
        } else {
            return true;
        }
    }
    
    
    public boolean createNodeAgentConfig(String agentName, String dasHost,
            String dasPort, String dasuser, String daspwd,
            String domainName, String clientHostname) throws HarnessException {
        boolean result;
        String adminSecurity = getTargetDomainSecurity(domainName, commonInfo);
        System.setProperty(INSTANCE_ROOT_PROPERTY, commonInfo.getTargetInstallDir());
        String[] command = {"create-node-agent-config", "--host", dasHost,
        "--port", dasPort, "--secure="+adminSecurity, "--user", dasuser,
        "--passwordfile ", "\"" + commonInfo.getPasswordFile() +"\"",
        agentName };
        result = this.executeCommand(command);
        this.stopDomain(domainName);
        // now set the remote client property
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
        String targetDomainRoot = commonInfo.getTargetDomainRoot();
        String targetDomainFileName = targetDomainRoot + File.separator +
                domainName + File.separator +
                UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator +
                "domain.xml";
        String sourceDomainPath = commonInfo.getSourceInstallDir();
        String sourceDomainFileName = sourceDomainPath + File.separator +
                    "config" + File.separator + "domain.xml";
        Document sourceDoc = upgrUtils.getDomainDocumentElement(sourceDomainFileName);
        Document resultDoc = upgrUtils.getDomainDocumentElement(targetDomainFileName);
        
        try {
            NodeList agentElements = resultDoc.getDocumentElement().
                    getElementsByTagName("node-agents");
            Element nodeAgents = (Element)agentElements.item(0);
            NodeList taggedElements = nodeAgents.getElementsByTagName("node-agent");
            for(int lh =0; lh < taggedElements.getLength(); lh++){
                Element element = (Element)taggedElements.item(lh);
                // Compare id attribute of http-listener elements.
                if((element.getAttribute("name")).equals(agentName)){
                    NodeList jmxConnectorElements = element.getElementsByTagName("jmx-connector");
                    Element jmxConnectorElement = (Element)jmxConnectorElements.item(0); // only one jmx-connector per node-agent
                    
                    //transfer the jmx-connector source port
                    String port;
                    NodeList sourceAgentElements = sourceDoc.getDocumentElement().
                            getElementsByTagName("node-agents");
                    Element sourceNodeAgents = (Element)sourceAgentElements.item(0);
                    NodeList sourceTaggedElements = sourceNodeAgents.getElementsByTagName("node-agent");
                    for(int ii =0; ii < sourceTaggedElements.getLength(); ii++){
                        Element sourceElement = (Element)sourceTaggedElements.item(ii);
                        // Compare id attribute of http-listener elements.
                        if((sourceElement.getAttribute("name")).equals(agentName)){
                            NodeList sourceJmxConnectorElements = sourceElement.getElementsByTagName("jmx-connector");
                            Element sourceJmxConnectorElement = (Element)sourceJmxConnectorElements.item(0); // only one jmx-connector per node-agent
                            port = sourceJmxConnectorElement.getAttribute("port");
                            jmxConnectorElement.setAttribute("port", port);
                            break;
                        }
                    }
                    
                    //transfer client-hostname
                    NodeList propertyElements = jmxConnectorElement.getElementsByTagName("property");
                    for(int pi=0;pi < propertyElements.getLength();pi++) {
                        Element propertyElement = (Element)propertyElements.item(pi);
                        if(propertyElement.getAttribute("name").equals("client-hostname")) {
                            propertyElement.setAttribute("value",clientHostname);
                        }
                    }
                    
                    //transfer the source node-agent rendezvousOccurred property value
                    String rendezvousOccurredValue = getNodeAgentRendezvousProperty(agentName);
                    propertyElements = element.getElementsByTagName("property");
                    for(int pi=0;pi < propertyElements.getLength();pi++) {
                        Element propertyElement = (Element)propertyElements.item(pi);
                        if(propertyElement.getAttribute("name").equals("rendezvousOccurred")) {
                            propertyElement.setAttribute("value",rendezvousOccurredValue);
                        }
                    }
                    break;
                }
            }
            
            //Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            if (resultDoc.getDoctype() != null){
                String systemValue = resultDoc.getDoctype().getSystemId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
                String pubValue = resultDoc.getDoctype().getPublicId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pubValue);
            }
            
            DOMSource source = new DOMSource(resultDoc);
            StreamResult resultStream = new StreamResult(new FileOutputStream(targetDomainFileName));
            transformer.transform(source, resultStream);
            resultStream.getOutputStream().close();
            
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_port_from_XML"),ex);
        }
        this.startDomain(domainName);
        return result;
    }
    
    /**
     * Creates server instances in the particular config
     * under the specified node-agent
     */
    public boolean createServerInstance(String serverName, String agentName,
            String configName, String userid, String pwd, String adminPort,
            String adminSecurity, String domainName){
        // if  configName is null, then the command creates serverName_config
        // if nodeAgent does not exist, will create nodeAgent implicitely
        
        if(configName != null){
            String[] command = {"create-instance",
            "--nodeagent", agentName,
            "--port", adminPort,
            "--secure=" + adminSecurity,
            "--config", configName,
            "--user", userid,
            "--passwordfile ", "\"" + commonInfo.getPasswordFile() +"\"",
            serverName };
            return this.executeCommand(command);
        }else{
            String[] command = {"create-instance",
            "--nodeagent", agentName,
            "--user", userid,
            "--passwordfile ", "\"" + commonInfo.getPasswordFile() +"\"",
            "--port", adminPort,
            "--secure="+adminSecurity,
            serverName };
            return this.executeCommand(command);
        }
    }
    
    private boolean startDomain(String domainName) throws HarnessException {
        return startDomain(domainName, this.commonInfo);
    }
    
    public boolean startDomain(String domainName, CommonInfoModel commonInfo) throws HarnessException {
        if(!domainStarted) {
            if(Commands.startDomain(domainName, commonInfo)) {
                domainStarted = true;
                return true;
            } else {
                throw new HarnessException(stringManager.
                        getString("upgrade.common.domain_start_failed",domainName));
            }
        }
        return false;
    }
    
    public boolean stopDomain(String domainName) throws HarnessException {
        if(domainStarted ) {
            if(Commands.stopDomain(domainName, commonInfo)) {
                domainStarted = false;
                return true;
            } else {
                throw new HarnessException(stringManager.getString("upgrade.common.domain_stop_failed",domainName));
            }
        }
        return false;
    }
    
    private boolean executeCommand(String[] commandStrings){
        try {
            return Commands.executeCommand(commandStrings);
        } catch (com.sun.enterprise.cli.framework.CommandException ce) {
            Throwable t = ce.getCause();
            logger.log(Level.SEVERE,
                    stringManager.getString("enterprise.tools.upgrade.generalException", ce.getMessage()),
                    ce);
            if (t != null) {
                logger.log(Level.SEVERE,
                        stringManager.getString("enterprise.tools.upgrade.generalException", t.getMessage()));
            }
        }
        return false;
    }
    
    /**
     * Creates clusters and associated server instances
     */
    public boolean processClusters() throws HarnessException {
        String sourceVersion = commonInfo.getSourceVersion();
        
        //Get clusterInfoList
        java.util.List clInfoList = ClustersInfoManager.getClusterInfoManager().
                getClusterInfoList();
        if((clInfoList == null) || (clInfoList.isEmpty()))
            return false;
        
        // Create cluster for each clInfoList.
        int clusterNo = 0;
        for(Iterator it = clInfoList.iterator(); it.hasNext(); ){
            ClusterInfo clInfo = (ClusterInfo)it.next();
            
            //cluster name got from ClusterInfo object
            String clusterName = null;
            if(UpgradeConstants.VERSION_7X.equals(sourceVersion)) {
                clusterName = "cluster_"+Integer.toString(clusterNo++);
            } else {
                clusterName = clInfo.getClusterName();
            }
            clInfo.setClusterName(clusterName);
            startDomain(clInfo.getDomainName());
            
            //Create Cluster
            boolean clusterCreated = createCluster(clusterName,
                    clInfo.getDomainName());
            if(!clusterCreated) {
                stopDomain(clInfo.getDomainName());
                return false;
            }
            
            //Get server instances for this cluster
            List clInstances = clInfo.getClusteredInstanceList();
            ClusteredInstance masterInstance = clInfo.getMasterInstance();
            String startedDomainName = null;
            String configName = configName = clusterName+"_config";
            if(masterInstance == null){
                // Is it guaranteed that there will be one master instance always?
                String adminPort = null;
                String adminSecurity = null;
                for(Iterator clInstIt = clInstances.iterator(); clInstIt.hasNext();){
                    ClusteredInstance clusteredInstance = (ClusteredInstance)clInstIt.next();
                    startedDomainName = clusteredInstance.getDomain();
                    adminPort = getTargetDomainPort(startedDomainName,commonInfo);
                    adminSecurity = getTargetDomainSecurity(startedDomainName,commonInfo);
                    //create clustered instance
                    createClusteredInstance(clusteredInstance,clusterName,
                            startedDomainName,configName,adminPort, adminSecurity);
                }
            }else{
                String adminPort = this.getTargetDomainPort(masterInstance.getDomain(),this.commonInfo);
                String adminSecurity = this.getTargetDomainSecurity(masterInstance.getDomain(),this.commonInfo);
                this.createClusteredInstance(masterInstance,clusterName,masterInstance.getDomain(),configName,adminPort, adminSecurity);
                for(Iterator clInstIt = clInstances.iterator(); clInstIt.hasNext();){
                    ClusteredInstance clInst = (ClusteredInstance)clInstIt.next();
                    if(!clInst.isMaster())
                        this.createClusteredInstance(clInst,clusterName,masterInstance.getDomain(),configName,adminPort, adminSecurity);
                }
            }
            stopDomain(clInfo.getDomainName());
        }
        // Set currentCluster to null;
        commonInfo.setCurrentCluster(null);
        processStandAloneInstances();
        return true;
    }
    
    private boolean createClusteredInstance(ClusteredInstance clInstance,
            String clusterName, String domainName, String configName,
            String adminPort, String adminSecurity) throws HarnessException {
        String serverName = clInstance.getInstanceName();
        String nodeAgentName = clInstance.getHost();
        String clientHostName = getNodeAgentClient(nodeAgentName);
        String dasHostName = null;
        try {
            dasHostName = (java.net.InetAddress.getLocalHost()).getHostName();
        } catch (java.net.UnknownHostException uhe) {
            dasHostName = "localhost";
        }
        String configFileName = commonInfo.getTargetDomainRoot() +
                File.separator + domainName + File.separator +
                "config" + File.separator + "domain.xml";
        String user = commonInfo.getAdminUserName();
        String password = commonInfo.getAdminPassword();
        
        if(!nodeAgentExists(nodeAgentName,configFileName)){
            if(commonInfo.isInPlace()) {
                createNodeAgentConfig(nodeAgentName, dasHostName, clInstance.getPort(),
                        user, password, domainName, clientHostName);
            } else {
                createNodeAgent(nodeAgentName, dasHostName, clInstance.getPort(),
                        user, password, domainName, clientHostName);
            }
        }
        
        String[] command = {"create-instance",
        "--user", user,
        "--passwordfile ", "\"" + commonInfo.getPasswordFile() +"\"",
        "--nodeagent", nodeAgentName,
        "--port",adminPort,
        "--secure=" + adminSecurity,
        "--cluster", clusterName,
        clInstance.getInstanceName()};
        boolean success = this.executeCommand(command);
        return success;
    }
    
    /**
     * Creates cluster using create-cluster asadmin command
     */
    private boolean createCluster(String clusterName, String domainName)
    throws HarnessException{
        String adminSecurity = null;
        String adminPort = null;
        if(domainName != null){
            adminPort = getTargetDomainPort(domainName, commonInfo);
            adminSecurity = getTargetDomainSecurity(domainName, commonInfo);
        }else{
            // This should be fixed.  This is not typical.  Domain name is a must.
        }
        String adminUser = commonInfo.getAdminUserName();
        String[] command = {"create-cluster","--port",adminPort,
        "--secure="+adminSecurity,"--user",adminUser,
        "--passwordfile ", "\"" + commonInfo.getPasswordFile() +"\"",
        clusterName};
        boolean returnStatus = this.executeCommand(command);
        return returnStatus;
    }
    
    /**
     * Creates stand-alone instances using asadmin command rather then
     * transforming from source thereby reducing the overhead of introducing new
     * elements in the dtd
     */
    public void processStandAloneInstances() throws HarnessException {
        List stdAloneInsts = null;
        UpgradeUtils upgradeUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
        String sourceVersion = commonInfo.getSourceVersion();
        stdAloneInsts = upgradeUtils.getStandAloneInstancesFromDomainXml();
        if(stdAloneInsts == null) {
            return;
        }
        Vector runningDomains = new Vector();
        for(Iterator it = stdAloneInsts.iterator(); it.hasNext();){
            Vector instDInfo = (Vector)it.next();
            String serverName = (String)instDInfo.elementAt(0);
            DomainInfo dInfo = (DomainInfo)instDInfo.elementAt(1);
            String adminPort = this.getTargetDomainPort(dInfo.getDomainName(),this.commonInfo);
            String adminSecurity = this.getTargetDomainSecurity(dInfo.getDomainName(),this.commonInfo);
            String domainName = dInfo.getDomainName();
            String agentName = getSourceNodeAgentForInstance(domainName, serverName);
            if (agentName == null || agentName.equals("")) continue;
            if(!runningDomains.contains(dInfo)) {
                this.startDomain(dInfo.getDomainName());
                runningDomains.add(dInfo);
            }
            String hostName = null;
            try {
                hostName = (java.net.InetAddress.getLocalHost()).getHostName();
            } catch (java.net.UnknownHostException uhe) {
                hostName = "localhost";
            }
            String clientName = getNodeAgentClient(agentName);
            String configFileName = commonInfo.getTargetDomainRoot() +
                    File.separator + dInfo.getDomainName() + File.separator +
                    "config" + File.separator + "domain.xml";
            if(!nodeAgentExists(agentName,configFileName)) {
                if(commonInfo.isInPlace()) {
                    createNodeAgentConfig(agentName, hostName, adminPort,
                            commonInfo.getAdminUserName(), commonInfo.getAdminPassword(),
                            dInfo.getDomainName(), clientName);
                } else {
                    createNodeAgent(agentName, hostName, adminPort,
                            commonInfo.getAdminUserName(), commonInfo.getAdminPassword(),
                            dInfo.getDomainName(), clientName);
                }
            }
            
            if(!serverName.equals("server")) {
                boolean status = this.createServerInstance(serverName, agentName, null,
                        commonInfo.getAdminUserName(), commonInfo.getAdminPassword(),
                        adminPort, adminSecurity, dInfo.getDomainName());
            }
            
        }
        for (ListIterator runningDomainsList = runningDomains.listIterator();runningDomainsList.hasNext();) {
            this.stopDomain(((DomainInfo)runningDomainsList.next()).getDomainName());
        }
    }
    
    private static String getJMXPortFromXML(String fileName, String tagName, String name) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse( new File(fileName));
            
            NodeList taggedElements = adminServerDoc.getDocumentElement().getElementsByTagName(tagName);
            for(int lh =0; lh < taggedElements.getLength(); lh++){
                Element element = (Element)taggedElements.item(lh);
                // Compare id attribute of http-listener elements.
                if((element.getAttribute("name")).equals(name)){
                    return element.getAttribute("port");
                }
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_port_from_XML"),ex);
        }
        return null;
    }
    
    public static String getSourceIIOPPort(String domainPath) {
        String domainXmlFile = domainPath + File.separator +
                UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator +
                "domain.xml";
        if(new File(domainXmlFile).exists()){
            String port = getPortFromXML(domainXmlFile,"iiop-listener",
                    "orb-listener-1");
            if(port != null)
                return port;
        }
        // return some default no....user should change it later.
        iiopPortToStartWith =+10;
        return String.valueOf(iiopPortToStartWith);
    }
    
    /**
     * Sets orb-listener-1 port of the server-config from source
     * domain.xml to target
     */
    private void setServerIIOPPort(String sourceDomainName,
            String targetDomainName){
        DomainInfo dInfo = (DomainInfo)this.commonInfo.getDomainMapping().
                get(sourceDomainName);
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
        String domainPath = dInfo.getDomainPath();
        String targetDomainRoot = commonInfo.getTargetDomainRoot();
        String iiopPort = getSourceIIOPPort(domainPath);
        
        String domainFileName = targetDomainRoot + File.separator +
                targetDomainName + File.separator +
                UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator +
                "domain.xml";
        
        Document resultDoc = upgrUtils.getDomainDocumentElement(domainFileName);
        try {
            NodeList configElements = resultDoc.getDocumentElement().
                    getElementsByTagName("configs");
            NodeList configList = ((Element)configElements.item(0)).
                    getElementsByTagName("config");
            
            //Iterate through the config elements
            for(int i=0; i<configList.getLength(); i++) {
                Element configElement = (Element)configList.item(i);
                
                //If this is a server-config element, then transfer the iiop ports
                if((configElement.getAttribute("name")).equals("server-config")) {
                    NodeList iiopListenerElements = configElement.
                            getElementsByTagName("iiop-listener");
                    for(int lh =0; lh < iiopListenerElements.getLength(); lh++){
                        Element element = (Element)iiopListenerElements.item(lh);
                        // Compare id attribute of iiop-listener elements.
                        if((element.getAttribute("id")).equals("orb-listener-1")){
                            element.setAttribute("port",iiopPort);
                            break;
                        }
                    }
                }
            }
            
            
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            if (resultDoc.getDoctype() != null){
                String systemValue = resultDoc.getDoctype().getSystemId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
                String pubValue = resultDoc.getDoctype().getPublicId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pubValue);
            }
            
            DOMSource source = new DOMSource(resultDoc);
            StreamResult result = new StreamResult(new FileOutputStream(domainFileName));
            transformer.transform(source, result);
            result.getOutputStream().close();
            
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_port_from_XML"),ex);
        }
        
    }
    
    private String getNodeAgentClient(String nodeAgentName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            String sourceDomainPath = commonInfo.getSourceInstallDir();
            String domainXmlFile = sourceDomainPath + File.separator +
                    "config" + File.separator + "domain.xml";
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse(domainXmlFile);
            NodeList taggedElements = adminServerDoc.getDocumentElement().getElementsByTagName("node-agent");
            for(int i=0;i<taggedElements.getLength();i++) {
                Element element = (Element)taggedElements.item(i);
                if (nodeAgentName.equals(element.getAttribute("name"))) {
                    NodeList jmxConnectorList = element.getElementsByTagName("jmx-connector");
                    Element jmxConnectorElement = (Element)jmxConnectorList.item(0); //one connector per node-agent
                    NodeList properties = jmxConnectorElement.getElementsByTagName("property");
                    for(int ii=0;ii<properties.getLength();ii++) {
                        Element client = (Element)properties.item(ii);
                        if ("client-hostname".equals(client.getAttribute("name"))) {
                            return client.getAttribute("value");
                        }
                    }
                }
            }
            
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
        }
        return null;
    }
    
    private String getNodeAgentRendezvousProperty(String nodeAgentName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            String sourceDomainPath = commonInfo.getSourceInstallDir();
            String domainXmlFile = sourceDomainPath + File.separator +
                    "config" + File.separator + "domain.xml";
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document adminServerDoc = builder.parse(domainXmlFile);
            NodeList taggedElements = adminServerDoc.getDocumentElement().getElementsByTagName("node-agent");
            for(int i=0;i<taggedElements.getLength();i++) {
                Element element = (Element)taggedElements.item(i);
                if (nodeAgentName.equals(element.getAttribute("name"))) {
                    NodeList properties = element.getElementsByTagName("property");
                    for(int ii=0;ii<properties.getLength();ii++) {
                        Element client = (Element)properties.item(ii);
                        if ("rendezvousOccurred".equals(client.getAttribute("name"))) {
                            return client.getAttribute("value");
                        }
                    }
                }
            }
            
        }catch (Exception ex){
            logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
        }
        return null;
    }
}
