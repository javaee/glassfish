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

import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;

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
	
	private StringManager stringManager = StringManager.getManager(UpgradeUtils.class);
	
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
	
	/**
	 * Method to create backup directory if In-Place upgrade is done.
	 * Also to build the domain mapping of the source.
	 * @param cmi CommonInfoModel
	 * @param dirs Contents of the domains root of source
	 * @param domainRoot Domains root of the source
	 */
	public String backupDomain(String domainName, String domainPath,
		String targetDomainRoot ){
		String timestamp=""+System.currentTimeMillis();
		String newDomainName = domainName+"_"+timestamp;
		String backup = targetDomainRoot + "/" + UpgradeConstants.BACKUP_DIR;
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
		String sourceDomainPath = backup + "/" + newDomainName;
		//Copy and delete instead of rename because of Windows issue.
		try{
			File backupDomainDir = new File(sourceDomainPath);
			new File(sourceDomainPath).mkdirs();
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
		return sourceDomainPath;
	}
	
	public String findLatestDomainBackup(String domainRoot,String domainName) {
		String latestDomainPath = null;
		if (domainName != null){
			String backupDirPath = domainRoot + "/" + UpgradeConstants.BACKUP_DIR;
			File backupDir = new File(backupDirPath);
			long latestTimestamp = 0;
			
			//If backup directory already exists return the directory name else null
			if(backupDir.isDirectory()) {
				String tmpN = domainName + "_";
				String [] dirs = backupDir.list();
				for(int i=0;i<dirs.length;i++) {
					if(dirs[i].startsWith(tmpN)){
						String time = dirs[i].substring(tmpN.length());
						long timestamp = Long.parseLong(time);
						if(timestamp > latestTimestamp) {
							latestTimestamp = timestamp;
							latestDomainPath = backupDirPath + "/"+dirs[i];
						}
					}else {
						continue;
					}
				}
			}
		}
		return  latestDomainPath;
	}
	
	/**
	 * Parses the domain.xml for the server elements
	 * Returns the list of stand alone instances.
	 */
	public List<String> getStandAloneInstancesFromDomainXml() {
		List<String> stdAloneList = new ArrayList<String>();
		Document adminServerDoc = getDomainDocumentElement(
			common.getSourceConfigXMLFile());
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
					stdAloneList.add(instanceName);
				}
			}
		}catch (Exception ex){
			logger.log(Level.WARNING, stringManager.getString("upgrade.common.server_instances_from_XML"),ex);
		}
		return stdAloneList;
	}
	
	public boolean switchedIIOPPorts(String serverID,
		String portValue, Element documentElement){
		boolean flag = false;
		Element configEle = this.getConfigElementFromDocumentElement(
			documentElement,"server-config");
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
			String serverIIOPPort =this.getIIOPListenerPortForServer(
				iiopListener.getAttribute("port"),"server", documentElement);
			if(serverIIOPPort.equals("3700")){
				String portValueFromServer =this.getIIOPListenerPortForServer(
					portValue,serverID,documentElement);
				if(portValueFromServer != null)
					iiopListener.setAttribute("port", portValueFromServer);
				flag = true;
			}
		}
		return flag;
	}
	
	/**
	 * Builds the ClusteredInstance object for the particular cluster.
	 * The source domain.xml is parsed for cluster and its associated instances.
	 * Values are loaded in the ClusteredInstance object.
	 */
	public void updateClusteredInstanceList(String domainXMLFile, String domainName,
		String clusterName, java.util.List clusteredInstanceList) {
		String domainPath = common.getSource().getInstallDir();
		//Get admin username
		String userName = common.getSource().getDomainCredentials().getAdminUserName();
		
		DomainsProcessor dProcessor = new DomainsProcessor(common);
		//Get admin port
		String adminPort = dProcessor.getSourceAdminPort(common.getSourceConfigXMLFile());
		
		//Get domain.xml document
		Document domainDoc = this.getDomainDocumentElement(domainXMLFile);
		if(domainDoc != null){
			//Get the servers nodelist
			NodeList servers = domainDoc.getDocumentElement().
				getElementsByTagName("servers");
			if(servers != null && servers.getLength() >0){
				//Get all the server elements under servers element
				NodeList serverList = ((Element)servers.item(0)).
					getElementsByTagName("server");
				if((serverList != null) && (serverList.getLength() >0)){
					//Iterate through the server elements to get the attributes
					for(int lh =0; lh < serverList.getLength(); lh++){
						Element server = (Element) serverList.item(lh);
						String configRefName = server.getAttribute("config-ref");
						
						//Check if the server instance belongs to a cluster
						if((clusterName+"-config").equals(configRefName)) {
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
		String sourceDir = common.getSource().getDomainRoot();
		String targetDir = common.getTarget().getInstallDir();
		if(sourceDir != null && targetDir != null) {
			String dname = common.getSource().getDomainName();
			if(sourceDir.equals(targetDir)) {
				File backupdir = new File(sourceDir + "/" + UpgradeConstants.BACKUP_DIR);
				if(backupdir.isDirectory()) {
					String latestDomainPath = common.findLatestDomainDirBackup(sourceDir);
					if(latestDomainPath != null) {
						//Copy and delete instead of rename because of Windows issue.
						try{
							copyDirectory(new File(latestDomainPath),
								new File(sourceDir + "/" + dname));
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
		}//not null check of domainList
	}
	
	/**
	 * Method to validate the admin credentials and
	 * master password (if any provided)
	 */
	public boolean validateUserDetails(String adminUserName, String adminPassword,
		String masterPassword){
		boolean flag = true;
		
		if (common.getSource().getInstallDir() != null) {
			try{
				RepositoryManager rManager = new RepositoryManager();
				RepositoryConfig repConf = new RepositoryConfig(
					common.getSource().getDomainName(), common.getSource().getDomainRoot());
				rManager.validateAdminUserAndPassword(repConf, adminUserName,adminPassword);
				if(masterPassword != null)
					rManager.validateMasterPassword(repConf,masterPassword);
			}catch(com.sun.enterprise.admin.servermgmt.RepositoryException re){
				logger.log(Level.SEVERE,
					stringManager.getString("enterprise.tools.upgrade.generalException",
					re.getMessage()), re);
				flag = false;
			}
		}
		return flag;
	}
	
	public void addOrUpdateSystemPropertyToServer(String serverName, NodeList serverList,
		String propertyName, String propertyValue, BaseElement bElement){
		for(int lh =0; lh < serverList.getLength(); lh++){
			Element serverElement = (Element)serverList.item(lh);
			if(serverName.equals(serverElement.getAttribute("name"))){
				Element sysProp = this.getSystemPropertyElement(propertyName,
					serverElement);
				
				if(sysProp == null){
					// bElement could be passed as null in case if this property need not be added.
					if(bElement != null){
						sysProp = serverElement.getOwnerDocument().createElement(
							"system-property");
						sysProp.setAttribute("name", propertyName);
						sysProp.setAttribute("value", propertyValue);
						bElement.appendElementToParent(serverElement,sysProp);
					}
				} else {
					sysProp.setAttribute("value", propertyValue);
				}
				return;
			}
		}
	}
	
	public static void copyFile(String source, String target) throws IOException {
		FileUtils.copy(source, target);
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
	public static void copyDirectory(File sourceDir , File targetDir) throws IOException {
		File [] srcFiles = sourceDir.listFiles();
		if (srcFiles != null) {
			for(int i=0; i< srcFiles.length; i++) {
				File dest = new File(targetDir, srcFiles[i].getName());
				if( srcFiles[i].isDirectory() && FileUtils.safeIsRealDirectory(srcFiles[i])) {
					if (!dest.exists()) {
						dest.mkdirs();
					}
					copyDirectory(srcFiles[i], dest);
				} else {
					if (!dest.exists()) {
						dest.createNewFile();
					}
					copyFile(srcFiles[i].getAbsolutePath(),new File(targetDir, 
						srcFiles[i].getName()).getAbsolutePath());					
				}
			}
		}
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
		Document sourceDoc =this.getDomainDocumentElement(
			common.getSourceConfigXMLFile());
		NodeList appRefList = sourceDoc.getDocumentElement().
			getElementsByTagName("application-ref");
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
	
	/**
	 * Builds the web module target.
	 * Returns a string that is the target server for this web module.
	 * It is taken from the source domain.xml file.
	 * Used only for > 8.1/8.2EE domain.xmls
	 */
	public String buildAppDeploymentTarget(String moduleName) {
		String appTarget = null;
		String actualModName = moduleName;
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
		String value = null;
		for(int lh =0; lh < serverList.getLength(); lh++){
			if(serverName.equals(((Element)serverList.item(lh)).getAttribute("name"))){
				Element serverElement = (Element)serverList.item(lh);
				Element sysProp =
					getSystemPropertyElement("IIOP_LISTENER_PORT",serverElement);
				if(sysProp != null){
					value = sysProp.getAttribute("value");
					break;
				}else{
					Element configElement =
						this.getConfigElementFromDocumentElement(documentElement,
						serverElement.getAttribute("config-ref"));
					if(configElement != null){
						sysProp = getSystemPropertyElement("IIOP_LISTENER_PORT",
							configElement);
						if(sysProp != null){
							value = sysProp.getAttribute("value");
							break;
						}
					}
				}
			}
		}
		return value;
	}
	
	private Element getSystemPropertyElement(String propertyName,
		Element parentForSysProp){
		Element e = null;
		NodeList sysProps =
			parentForSysProp.getElementsByTagName("system-property");
		for(int sh =0; sh < sysProps.getLength(); sh++){
			if(propertyName.equals(((Element)sysProps.item(sh)).getAttribute("name"))){
				e = ((Element)sysProps.item(sh));
				break;
			}
		}
		return e;
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
	
	public static boolean deleteFile(String targetMasterPasswordFile) {
		File masterPasswordFile = new File(targetMasterPasswordFile);
		return masterPasswordFile.delete();
	}
	
	public List<String> getNodeAgentList(String domainXMLFile) {
		Vector<String> l = new Vector<String>();
		Document domainDoc = this.getDomainDocumentElement(domainXMLFile);
		if(domainDoc != null){
			NodeList nas = domainDoc.getDocumentElement().
				getElementsByTagName("node-agents");
			if(nas != null && nas.getLength() > 0){
				NodeList naList = ((Element)nas.item(0)).
					getElementsByTagName("node-agent");
				if((naList != null) && (naList.getLength() >0)){
					for(int lh =0; lh < naList.getLength(); lh++){
						String name = ((Element)naList.item(lh)).getAttribute("name");
						l.add(name);
					}
				}
			}
		}
		return l;
	}
}
