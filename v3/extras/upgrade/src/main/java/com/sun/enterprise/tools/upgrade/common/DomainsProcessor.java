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

//-import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
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
	private static StringManager stringManager = StringManager.getManager(DomainsProcessor.class);
	
	private boolean domainStarted = false;
	
	private UpgradeUtils _upgradeUtils;
	private String _srcDomainXMLFilename;
	private String _trgDomainXMLFilename;
	private TargetAppSrvObj tAppSrvObj;
	private SourceAppSrvObj sAppSrvObj;
	private Credentials sCredentials;
	
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
		
		_upgradeUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
		tAppSrvObj = commonInfo.getTarget();
		sAppSrvObj = commonInfo.getSource();
		sCredentials = sAppSrvObj.getDomainCredentials();
		_srcDomainXMLFilename = commonInfo.getSourceConfigXMLFile();
		_trgDomainXMLFilename = tAppSrvObj.getConfigXMLFile();
		String targetDomainRoot = tAppSrvObj.getInstallDir();
		setPortsFromXML(targetDomainRoot + "/" + "domain1" + "/" +
			"config" + "/" + "domain.xml");
		setPortsFromXML(targetDomainRoot + "/" + "samples" + "/" +
			"config" + "/" + "domain.xml");
	}
	
	private void setPortsFromXML(String domainXMLfile){
		if(domainXMLfile != null && new File(domainXMLfile).exists()){
			String shtSSP = getPortFromXML(domainXMLfile, "http-listener", "http-listener-2");
			if(shtSSP != null) httpSSLPorts.add(shtSSP);
			String siiSSP = getPortFromXML(domainXMLfile, "iiop-listener", "SSL");
			if(siiSSP != null) iiopSSLPorts.add(siiSSP);
			String siiMAP = getPortFromXML(domainXMLfile, "iiop-listener", "SSL_MUTUALAUTH");
			if(siiMAP != null) iiopMutualAuthPorts.add(siiMAP);
			String sadminJMX = getJMXPortFromXML(domainXMLfile, "jmx-connector", "system");
			if(sadminJMX != null) adminJMXPorts.add(sadminJMX);
			String siiP = getPortFromXML(domainXMLfile, "iiop-listener", "orb-listener-1");
			if(siiP != null) iiopPorts.add(siiP);
			String shttpP = getPortFromXML(domainXMLfile, "http-listener", "http-listener-1");
			if(shttpP != null) httpPorts.add(shttpP);
		}
	}
	
	public void processTargetDomain() throws HarnessException{
		String target = tAppSrvObj.getDomainRoot();
		String sourceDomainName = sAppSrvObj.getDomainName();
		String targetDomainName = tAppSrvObj.getDomainName();
		
		//Following lines are copied from IasAdminCommand
		ASenvPropertyReader reader = new ASenvPropertyReader();
		//////	System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
		//////reader.setSystemProperties();
		if(!new File(target).isDirectory()) {
			logger.log(Level.INFO,stringManager.getString(
				"upgrade.common.not_valid_target_install"));
			return;
		}
		
		boolean targetDomainDeleted = true;
		if(!commonInfo.isInPlace()) {
			//Delete the existing domain in a SBS upgrade
			File targetDomainUpgrade = new File(tAppSrvObj.getDomainDir());
			if(targetDomainUpgrade.isDirectory()) {
				targetDomainDeleted = _upgradeUtils.deleteDirectory(targetDomainUpgrade);
				if(targetDomainDeleted) {
					logger.log(Level.INFO, stringManager.getString(
						"upgrade.common.existing_target_domain_deleted"));
				} else {
					logger.log(Level.SEVERE, stringManager.getString(
						"upgrade.common.delete_existing_target_domain_failed"));
					UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
				}
			}
			
			if(targetDomainDeleted) {
				deleteTargetNodeAgents(target);
			}
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
				processUpgradeUpdateEvent((30));
		}
		//Set admin-listener port and security-enabled attribute
		this.setAdminPortAndSecurity();
		
		//Set orb-listener-1 port
		this.setServerIIOPPort();
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
		String target = tAppSrvObj.getInstallDir();
		String domainPath = sAppSrvObj.getInstallDir();
		String profile = sAppSrvObj.getEdition();
		
		//Get port information.
		String httpPort = getAFreePort(8080,10);
		String adminPort = getSourceAdminPort(_srcDomainXMLFilename);
		String jmsPort = "7676";
		String iiopPort = getSourceIIOPPort(_srcDomainXMLFilename);
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
		sCredentials.getAdminUserName(),
		"--passwordfile ",
		"\"" + sCredentials.getPasswordFile() +"\"",
		"--savemasterpassword=true",
		instancePortAttr,
		"--domainproperties",
		propertiesString,
		domainName80
		};
		return createDomainCommand;
	}
	
	
	// This method returns admin port from admin-server server.xml
	public static String getSourceAdminPort(String domainXmlFile) {
		String port;
		if(domainXmlFile != null && new File(domainXmlFile).exists()){
			port = getPortFromXML(domainXmlFile,"http-listener", "admin-listener");
			if(port == null)
				port = String.valueOf(adminPortToStartWith);
		} else {
			// return some default no....user should change it later.
			adminPortToStartWith +=10;
			port = String.valueOf(adminPortToStartWith);
		}
		return port;
	}
	
	public static String getSourceAdminSecurity(String domainXmlFile) {
		String securityEnabled = "true";
		if(domainXmlFile != null && new File(domainXmlFile).exists()){
			securityEnabled = getSecurityEnabledFromXML(domainXmlFile,
				"http-listener", "admin-listener");
			if(securityEnabled == null)
				securityEnabled = "true";
		}
		return securityEnabled;
	}
	
	
	/**
	 * Sets admin-listener port and security-enabled attribute from source domain
	 */
	private void setAdminPortAndSecurity(){
		try {
			String adminPort = getSourceAdminPort(_srcDomainXMLFilename);
			String securityEnabled = getSourceAdminSecurity(_srcDomainXMLFilename);
			
			Document resultDoc = _upgradeUtils.getDomainDocumentElement(_trgDomainXMLFilename);
			NodeList taggedElements = resultDoc.getDocumentElement().
				getElementsByTagName("http-listener");
			for(int lh =0; lh < taggedElements.getLength(); lh++){
				Element element = (Element)taggedElements.item(lh);
				// Compare id attribute of http-listener elements.
				if((element.getAttribute("id")).equals("admin-listener")){
					element.setAttribute("port",adminPort);
					if(!commonInfo.isEnterpriseEdition(sAppSrvObj.getEdition())) {
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
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, 
					systemValue);
				String pubValue = resultDoc.getDoctype().getPublicId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, 
					pubValue);
			}
			
			DOMSource source = new DOMSource(resultDoc);
			StreamResult result = new StreamResult(new FileOutputStream(
				_trgDomainXMLFilename));
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
	private static String getPortFromXML(String fileName, String tagName,String id) {
		String portValue = null;
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
					portValue = element.getAttribute("port");
					break;
				}
			}
		}catch (Exception ex){
			logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_port_from_XML"),ex);
		}
		return portValue;
	}
	
	/**
	 * Gets security-enabled attribute value from domain.xml for the
	 * particular http-listener
	 */
	private static String getSecurityEnabledFromXML(String fileName,
		String tagName, String id) {
		String sValue = null;
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
					sValue = element.getAttribute("security-enabled");
					break;
				}
			}
		}catch (Exception ex){
			logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_admin_security_from_XML"),ex);
		}
		return sValue;
	}
	
	/**
	 * This method is called only for 8.2ee -> 9.1ee upgrade.
	 * Retrieves the node-agent for the particular instance from the source domain.xml.
	 */
	private String getSourceNodeAgentForInstance(String serverName) {
		String nodeAgentName = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
				("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
			Document adminServerDoc = builder.parse( new File(_srcDomainXMLFilename));
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
			logger.log(Level.WARNING, stringManager.getString(
				"upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
		}
		return nodeAgentName;
	}

	private String getNodeAgentNameFromXML(String fileName) {
		String nameValue = null;
		if(new File(fileName).exists()) {
			String nodeAgentName = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
					("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
				Document adminServerDoc = builder.parse( new File(fileName));
				NodeList taggedElements = adminServerDoc.getDocumentElement().
					getElementsByTagName("node-agent");
				Element element = (Element)taggedElements.item(0);
				if(null == element) {
					logger.log(Level.WARNING, stringManager.getString(
						"upgrade.common.domain_processor_nodeAgent_from_XML"));
				} else {
					nameValue = element.getAttribute("name");
				}
			}catch (Exception ex){
				logger.log(Level.WARNING, stringManager.getString(
					"upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
			}
		}
		return nameValue;
	}
	
	private boolean nodeAgentExists(String agentName, String fileName){
		boolean flag = false;
		Document adminServerDoc = _upgradeUtils.getDomainDocumentElement(fileName);
		try {
			NodeList taggedElements = adminServerDoc.getDocumentElement().
				getElementsByTagName("node-agent");
			for(int lh =0; lh < taggedElements.getLength(); lh++){
				Element itElement = ((Element)taggedElements.item(lh));
				String attrName = itElement.getAttribute("name");
				if(attrName.equals(agentName)){
					flag = true;
					break;
				}
				// If the agentName contains .  example beacon.east, then should only compare to beacon.
				if(agentName.indexOf(".") != -1){
					if(agentName.substring(0,agentName.indexOf(".")).equals(attrName)){
						flag = true;
						break;
					}
				}
			}
		}catch (Exception ex){
			logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
		}
		return flag;
	}
	
	/**
	 * Gets admin-listener port from target domain.xml
	 */
	public static String getTargetDomainPort(String configFileName){
		return getPortFromXML(configFileName, "http-listener", "admin-listener");
	}
	
	/**
	 * Gets security-enabled attribute from target domain.xml
	 */
	public static String getTargetDomainSecurity(String configFile){
		return getSecurityEnabledFromXML(configFile, "http-listener",
			"admin-listener");
	}
	
	private String getAFreePort(int initPortNumber, int increment){
		// Do this only for 20 iterations.  If you cant find one in 10 iterations, then just assign some random number.
		int portNumber = initPortNumber;
		for(int i=0; i<20; i++){
			if(isPortNumberUsable(portNumber)){
				break;
			}
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
				logger.warning(stringManager.getString(
					"upgrade.common.domain_processor_could_not_resolve_client_hostname") + " " + clientHostName);
				clientAddress = "unknown"; //set it something that wont match and don't cerate a node-agent
			}
			if(myAddress == null)
				myAddress = "127.0.0.1"; //localhost
		}
		boolean flag = true;
		if(clientAddress.equals(myAddress) || clientHostName.equals("localhost")) {
			/////-String adminSecurity = getTargetDomainSecurity(domainName, commonInfo);
			String adminSecurity = getTargetDomainSecurity(_trgDomainXMLFilename);
			System.setProperty(INSTANCE_ROOT_PROPERTY, tAppSrvObj.getInstallRootProperty());
			String agentProperties = "remoteclientaddress=" + clientHostName;
			if(agentProperties != null && !agentProperties.equals("")) {
				agentProperties = "--agentproperties " + agentProperties;
			} else {
				agentProperties = "";
			}
			String[] command = {"create-node-agent", "--host", dasHost,
			"--port", dasPort, "--secure="+adminSecurity, "--user", dasuser,
			"--passwordfile ", "\"" + sCredentials.getPasswordFile() +"\"",
			agentProperties, agentName };
			flag = this.executeCommand(command);
		} 
		return flag;
	}
	
	
	public boolean createNodeAgentConfig(String agentName, String dasHost,
		String dasPort, String dasuser, String daspwd,
		String domainName, String clientHostname) {
		String adminSecurity = getTargetDomainSecurity(_trgDomainXMLFilename);
		System.setProperty(INSTANCE_ROOT_PROPERTY, tAppSrvObj.getInstallRootProperty());
		String[] command = {"create-node-agent-config", "--host", dasHost,
		"--port", dasPort, "--secure="+adminSecurity, "--user", dasuser,
		"--passwordfile ", "\"" + sCredentials.getPasswordFile() +"\"",
		agentName };
		return this.executeCommand(command);
	}
	
	/**
	 * Update the target node agent with the key values of the
	 * corresponding source node agent.
	 */
	private void transferNodeAgentSettings(String domainName, String agentName,
		String clientHostname)throws HarnessException{
		this.stopDomain(domainName);
		// now set the remote client property
		String targetDomainRoot = tAppSrvObj.getInstallDir();
		Document sourceDoc = _upgradeUtils.getDomainDocumentElement(_srcDomainXMLFilename);
		Document resultDoc = _upgradeUtils.getDomainDocumentElement(_trgDomainXMLFilename);
		
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
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, 
					systemValue);
				String pubValue = resultDoc.getDoctype().getPublicId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, 
					pubValue);
			}
			
			DOMSource source = new DOMSource(resultDoc);
			StreamResult resultStream = new StreamResult(
				new FileOutputStream(_trgDomainXMLFilename));
			transformer.transform(source, resultStream);
			resultStream.getOutputStream().close();
			
		}catch (Exception ex){
			logger.log(Level.WARNING, stringManager.getString("upgrade.common.domain_processor_port_from_XML"),ex);
		}
		this.startDomain(domainName);
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
			"--passwordfile ", "\"" + sCredentials.getPasswordFile() +"\"",
			serverName };
			return this.executeCommand(command);
		}else{
			String[] command = {"create-instance",
			"--nodeagent", agentName,
			"--user", userid,
			"--passwordfile ", "\"" + sCredentials.getPasswordFile() +"\"",
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
		boolean flag = false;
		if(!domainStarted) {
			if(Commands.startDomain(domainName, commonInfo)) {
				domainStarted = true;
				flag = true;
			} else {
				throw new HarnessException(stringManager.
					getString("upgrade.common.domain_start_failed",domainName));
			}
		}
		return flag;
	}
	
	public boolean stopDomain(String domainName) throws HarnessException {
		boolean flag = false;
		if(domainStarted) {
			if(Commands.stopDomain(domainName, commonInfo)) {
				domainStarted = false;
				flag = true;
			} else {
				throw new HarnessException(stringManager.getString(
					"upgrade.common.domain_stop_failed",domainName));
			}
		}
		return flag;
	}
	
	private boolean executeCommand(String[] commandStrings){
		boolean flag = false;
		try {
			flag = Commands.executeCommand(commandStrings);
		} catch (com.sun.enterprise.cli.framework.CommandException ce) {
			Throwable t = ce.getCause();
			logger.log(Level.SEVERE,stringManager.getString(
				"enterprise.tools.upgrade.generalException", ce.getMessage()),
				ce);
			if (t != null) {
				logger.log(Level.SEVERE,stringManager.getString(
					"enterprise.tools.upgrade.generalException", t.getMessage()));
			}
		}
		return flag;
	}
	
	/**
	 * Creates clusters and associated server instances
	 */
	public boolean processClusters() throws HarnessException {
		boolean flag = true;
		java.util.List clInfoList = ClustersInfoManager.getClusterInfoManager().
			getClusterInfoList();
		if((clInfoList == null) || (clInfoList.isEmpty())){
			flag = false;
		} else {
			// Create cluster for each clInfoList.
			for(Iterator it = clInfoList.iterator(); it.hasNext(); ){
				ClusterInfo clInfo = (ClusterInfo)it.next();
				String clusterName = clInfo.getClusterName();
				clInfo.setClusterName(clusterName);
				startDomain(clInfo.getDomainName());
			
				if(createCluster(clusterName,clInfo.getDomainName())) {					
					processClusterInstances(clInfo, clusterName);
					stopDomain(clInfo.getDomainName());
				} else {
					stopDomain(clInfo.getDomainName());
					flag = false;
					break;
				}
			}
		}
		return flag;
	}
	
	private void processClusterInstances(ClusterInfo clInfo, String clusterName)
		throws HarnessException {
		List clInstances = clInfo.getClusteredInstanceList();
		ClusteredInstance masterInstance = clInfo.getMasterInstance();
		String startedDomainName = null;
		String configName = clusterName+"_config";
		String adminPort = getTargetDomainPort(_trgDomainXMLFilename);
		String adminSecurity = getTargetDomainSecurity(_trgDomainXMLFilename);
		if(masterInstance == null){
			// Is it guaranteed that there will be one master instance always?
			for(Iterator clInstIt = clInstances.iterator(); clInstIt.hasNext();){
				ClusteredInstance clusteredInstance = (ClusteredInstance)clInstIt.next();
				startedDomainName = clusteredInstance.getDomain();
				/////-adminPort = getTargetDomainPort(_trgDomainXMLFilename);
				/////-adminSecurity = getTargetDomainSecurity(_trgDomainXMLFilename);
				//create clustered instance
				createClusteredInstance(clusteredInstance,clusterName,
					startedDomainName,configName,adminPort, adminSecurity);
			}
		}else{
			/////-String adminPort = this.getTargetDomainPort(_trgDomainXMLFilename);
			/////-String adminSecurity = this.getTargetDomainSecurity(_trgDomainXMLFilename);
			this.createClusteredInstance(masterInstance,clusterName,
				masterInstance.getDomain(),configName,adminPort, adminSecurity);
			for(Iterator clInstIt = clInstances.iterator(); clInstIt.hasNext();){
				ClusteredInstance clInst = (ClusteredInstance)clInstIt.next();
				if(!clInst.isMaster())
					this.createClusteredInstance(clInst,clusterName,
						masterInstance.getDomain(),configName,adminPort,
						adminSecurity);
			}
		}
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
		
		String user = sCredentials.getAdminUserName();
		String password = sCredentials.getAdminPassword();
		
		if(!nodeAgentExists(nodeAgentName,_trgDomainXMLFilename)){
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
		"--passwordfile ", "\"" + sCredentials.getPasswordFile() +"\"",
		"--nodeagent", nodeAgentName,
		"--port",adminPort,
		"--secure=" + adminSecurity,
		"--cluster", clusterName,
		clInstance.getInstanceName()};
		boolean success = this.executeCommand(command);
		
		//- create-instance creates the remote node-agent domain.xml config
		//- data for side-by-side install.  Wait until this point to update
		//- the agents data.
		transferNodeAgentSettings(domainName, nodeAgentName, clientHostName);
		
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
			adminPort = getTargetDomainPort(_trgDomainXMLFilename);
			adminSecurity = getTargetDomainSecurity(_trgDomainXMLFilename);
		}else{
			// This should be fixed.  This is not typical.  Domain name is a must.
		}
		String adminUser = sCredentials.getAdminUserName();
		String[] command = {"create-cluster","--port",adminPort,
		"--secure="+adminSecurity,"--user",adminUser,
		"--passwordfile ", "\"" + sCredentials.getPasswordFile() +"\"",
		clusterName};
		return this.executeCommand(command);
	}
	
	/**
	 * Creates stand-alone instances using asadmin command rather then
	 * transforming from source thereby reducing the overhead of introducing new
	 * elements in the dtd
	 */
	public void processStandAloneInstances() throws HarnessException {
		List<String> stdAloneInsts = _upgradeUtils.getStandAloneInstancesFromDomainXml();
		Vector<String> runningDomains = new Vector<String>();
		
		String domainName = sAppSrvObj.getDomainName();
		for(Iterator it = stdAloneInsts.iterator(); it.hasNext();){
			String serverName = (String)it.next();
			String adminPort = this.getTargetDomainPort(_trgDomainXMLFilename);
			String adminSecurity = this.getTargetDomainSecurity(_trgDomainXMLFilename);
			String agentName = getSourceNodeAgentForInstance(serverName);
			if (agentName == null || agentName.equals("")) continue;
			if(!runningDomains.contains(serverName)) {
				this.startDomain(domainName);
				runningDomains.add(serverName);
			}
			String hostName = null;
			try {
				hostName = (java.net.InetAddress.getLocalHost()).getHostName();
			} catch (java.net.UnknownHostException uhe) {
				hostName = "localhost";
			}
			String clientName = getNodeAgentClient(agentName);		
			if(!nodeAgentExists(agentName,_trgDomainXMLFilename)) {
				if(commonInfo.isInPlace()) {
					createNodeAgentConfig(agentName, hostName, adminPort,
						sCredentials.getAdminUserName(), sCredentials.getAdminPassword(),
						domainName, clientName);
				} else {
					createNodeAgent(agentName, hostName, adminPort,
						sCredentials.getAdminUserName(), sCredentials.getAdminPassword(),
						domainName, clientName);
				}
				transferNodeAgentSettings(domainName, agentName, clientName);
			}
			
			if(!serverName.equals("server")) {
				this.createServerInstance(serverName, agentName, null,
					sCredentials.getAdminUserName(), sCredentials.getAdminPassword(),
					adminPort, adminSecurity, domainName);
			}
			
		}
		this.stopDomain(domainName);
	}
	
	private static String getJMXPortFromXML(String fileName, String tagName, String name) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		String portValue = null;
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
				if((element.getAttribute("name")).equals(name)){
					portValue = element.getAttribute("port");
					break;
				}
			}
		}catch (Exception ex){
			logger.log(Level.WARNING, stringManager.getString(
				"upgrade.common.domain_processor_port_from_XML"),ex);
		}
		return portValue;
	}
	
	public static String getSourceIIOPPort(String domainXmlFile) {
		String port = null;
		if(domainXmlFile != null && new File(domainXmlFile).exists()){
			port = getPortFromXML(domainXmlFile,"iiop-listener","orb-listener-1");
		}
		
		// return some default no....user should change it later.
		if(port == null){
			iiopPortToStartWith =+10;
			port = String.valueOf(iiopPortToStartWith);
		}
		
		return port;
	}
	
	/**
	 * Sets orb-listener-1 port of the server-config from source
	 * domain.xml to target
	 */
	private void setServerIIOPPort(){
		Document resultDoc = _upgradeUtils.getDomainDocumentElement(_trgDomainXMLFilename);
		try {
			String iiopPort = getSourceIIOPPort(_srcDomainXMLFilename);
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
			StreamResult result = new StreamResult(new FileOutputStream(_trgDomainXMLFilename));
			transformer.transform(source, result);
			result.getOutputStream().close();
			
		}catch (Exception ex){
			logger.log(Level.WARNING, stringManager.getString(
				"upgrade.common.domain_processor_port_from_XML"),ex);
		}
		
	}
	
	private String getNodeAgentClient(String nodeAgentName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
				("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
			Document adminServerDoc = builder.parse(_srcDomainXMLFilename);
			NodeList taggedElements = adminServerDoc.getDocumentElement().
				getElementsByTagName("node-agent");
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
			logger.log(Level.WARNING, stringManager.getString(
				"upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
		}
		return null;
	}
	
	private String getNodeAgentRendezvousProperty(String nodeAgentName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
				("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
			Document adminServerDoc = builder.parse(_srcDomainXMLFilename);
			
			NodeList taggedElements = adminServerDoc.getDocumentElement().
				getElementsByTagName("node-agent");
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
			logger.log(Level.WARNING, stringManager.getString(
				"upgrade.common.domain_processor_nodeAgent_from_XML"),ex);
		}
		return null;
	}
	private void deleteTargetNodeAgents(String target){
		List<String> naList = _upgradeUtils.getNodeAgentList(_srcDomainXMLFilename);
		int cnt = naList.size();
		String tmpDir = target + "/" + "nodeagents";
		for (int j = 0; j < cnt; j++){
			String name = naList.get(j);
			File targetNodeagentsDir = new File(tmpDir, name);
			if (targetNodeagentsDir.exists()){
				if (_upgradeUtils.deleteDirectory(targetNodeagentsDir)) {
					logger.log(Level.INFO, stringManager.getString(
						"upgrade.common.existing_target_domain_nodeagent_deleted", name));
				} else {
					logger.warning("Unable to delete existing nodeagents config!");
				}
			}
		}
	}
}
