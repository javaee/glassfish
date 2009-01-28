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
 * DeploymentUpgrade.java
 *
 * Created on August 15, 2003, 11:46 AM
 */
package com.sun.enterprise.tools.upgrade.deployment;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.*;

//- import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deploy.shared.FileArchive;
///////import com.sun.enterprise.deployment.deploy.shared.FileArchiveFactory;
///////import com.sun.enterprise.deployment.deploy.shared.JarArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.OutputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.tools.upgrade.transform.elements.*;
import com.sun.enterprise.deployment.node.SaxParserHandler;

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
import org.w3c.dom.NamedNodeMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.runtime.WebRuntimeDDFile;
import com.sun.enterprise.deployment.node.SaxParserHandler;
import com.sun.enterprise.tools.upgrade.logging.LogService;
import org.w3c.dom.Element;

import com.sun.enterprise.cli.framework.CLIMain;
import com.sun.enterprise.cli.framework.InputsAndOutputs;

/**
 * This class transfers the deployed application ears, jars, wars, lifecycle modules and libraries
 * @author Hans Hrasna
 */
public class DeploymentUpgrade implements com.sun.enterprise.tools.upgrade.common.BaseModule {
	
	private static String EAR_DIR = "j2ee-apps";
	private static String MODULE_DIR = "j2ee-modules";
	private static String SOURCE_LIBRARY_DIR = "lib";
	private static String TARGET_LIBRARY_DIR = "lib";
	private static String MEJB_APP = "MEjbApp";
	private static String TIMER_APP = "__ejb_container_timer";
	private static String JWS_APP = "__JWSappclients";
	private static Hashtable deployedModules = new Hashtable();
	
	File sourceDir;
	String targetDir;
	boolean success = true;
	boolean domainRunning = false; //assume domain is not running
	CommonInfoModel commonInfo;
	
	private StringManager stringManager = StringManager.getManager(DeploymentUpgrade.class);
	private Logger logger = CommonInfoModel.getDefaultLogger();
	
	/** Creates a new instance of DeploymentUpgrade */
	public DeploymentUpgrade() {
	}
	
	/**
	 * Method to start upgrade of deployment module
	 */
	public boolean upgrade(CommonInfoModel commonInfo) {
		this.commonInfo = commonInfo;
		String currentDomain = commonInfo.getTarget().getDomainName();
		String sourceDomainPath = commonInfo.getSource().getDomainDir();
		String targetDomainPath = commonInfo.getTarget().getDomainDir();
		UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
		
		//start CR 6396940
		DomainInfo dInfo = new DomainInfo(currentDomain, sourceDomainPath);
		String applicationRoot = dInfo.getDomainApplicationRoot(upgrUtils);
		
		
		if (deployedModules.get(currentDomain) == null) {
			deployedModules.put(currentDomain, new ArrayList());
		}
		logger.log(Level.INFO,
			stringManager.getString("upgrade.deployment.startMessage"));
		domainRunning = false;
		
		//Process lib directory under target domain path
		processLibraries(sourceDomainPath, targetDomainPath);
		
		sourceDir = new File(sourceDomainPath, applicationRoot);
		
		//Get temporary file directory of the system and set targetDir to it
		try {
			File tmp = File.createTempFile("upgrade", null);
			targetDir = tmp.getParent();
			tmp.delete();
		} catch (IOException ioe) {
			logger.severe(stringManager.getString("upgrade.deployment.ioExceptionMsg")+ioe.getMessage());
			return false;
		}
		
		//Process Lifecycle modules
		processLifecycles();
		
		//Process standalone modules
		File srcJarDir = new File(sourceDir, MODULE_DIR);
		if(srcJarDir != null) {
			if(srcJarDir.listFiles() != null && srcJarDir.listFiles().length > 0) {
				if (!domainRunning) {
					domainRunning = startDomain(commonInfo.getTarget().getDomainName());
				}
				if (domainRunning) {
					processStandaloneModules(srcJarDir);
				} else {
					return false;
				}
			}
		}
		
		//Process Applications
		File srcEarDir = new File(sourceDir, EAR_DIR);
		if(srcEarDir != null) {
			if (srcEarDir.listFiles() != null && srcEarDir.listFiles().length > 0) {
				domainRunning = startDomain(currentDomain);
				if (domainRunning) {
					processApplications(srcEarDir);
				} else {
					return false;
				}
			}
		}
		
		//Stop domain if already running, after processing all modules
		if (domainRunning) {
			stopDomain(commonInfo.getTarget().getDomainName());
		}
		return success;
	}
	
	public void recovery(CommonInfoModel commonInfo) {
		commonInfo.getSource().getDomainCredentials().deletePasswordFile();
	}
	
	/**
	 * Builds an ear out of each deployed application
	 * and deploys it on the target
	 */
	private void processApplications(File srcDir) {
		UpgradeUtils upgradeUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
		File [] earDirs = srcDir.listFiles();
		for (int i=0;i<earDirs.length;i++) {
			String earDirName = earDirs[i].getName();
			String earName;
			if (earDirName.lastIndexOf("_ear") > -1) {
				earName = earDirName.substring(0,earDirName.lastIndexOf("_ear"));
			} else {
				earName = earDirName;
			}
			String appName = earName;
			if (appName.startsWith(MEJB_APP) || appName.startsWith(TIMER_APP) ||
				appName.startsWith(JWS_APP)) {
				continue;
			}
			earName=earName+".ear";
			try {
				/////-  JarArchiveFactory jaf = new JarArchiveFactory();
                                OutputJarArchive jaf = new OutputJarArchive();
				/////-  OutputJarArchive targetJar = (OutputJarArchive)jaf.createArchive(new File(targetDir, earName).getAbsolutePath());
                                jaf.create(java.net.URI.create(new File(targetDir, earName).getAbsolutePath()));
                                OutputJarArchive targetJar = jaf;
				/////-  FileArchiveFactory faf = new FileArchiveFactory();
                                InputJarArchive faf = new InputJarArchive();
				/////-  FileArchive farc = (FileArchive)faf.openArchive((new File(srcDir, earDirName)).getAbsolutePath());
                                faf.create(java.net.URI.create((new File(srcDir, earDirName)).getAbsolutePath()));
                                InputJarArchive  farc = faf;
				Enumeration e = farc.entries();
				String lastModuleProcessed = "";
				while(e.hasMoreElements()) {
					String s = (String)e.nextElement();
					String moduleDir;
					try {
						moduleDir = s.substring(0, s.lastIndexOf('_')+4);
					} catch (StringIndexOutOfBoundsException sob) {
						moduleDir = "";
					}
					
					FileInputStream fis = null;
					OutputStream out = null;
					try {
						if (moduleDir.endsWith("_jar") || moduleDir.endsWith("_war") || moduleDir.endsWith("_rar")) {
							if(lastModuleProcessed.equals(moduleDir)) {
								continue;
							}
							File jar = processModule(EAR_DIR, earDirName, moduleDir);
							lastModuleProcessed = moduleDir;
							out = targetJar.putNextEntry(jar.getName());
							fis = new FileInputStream(jar);
						} else {
							if (!s.endsWith("Client.jar")) { // don't include *Client.jars generated by server
								out = targetJar.putNextEntry(s);
								fis = new FileInputStream(new File(new File(srcDir, earDirName), s));
							} else {
								continue;
							}
							
						}
						
						while(fis.available() > 0) {
							int ix = fis.read();
							out.write(ix);
						}
						
					} catch(java.util.zip.ZipException z) {
						logger.warning(stringManager.getString("upgrade.deployment.zipExceptionMsg")+z.getMessage());
					} catch (IOException ioe) {
						logger.severe(stringManager.getString("upgrade.deployment.ioExceptionMsg")+ioe.getMessage());
					}
					targetJar.closeEntry();
				}
				targetJar.close();
				
				// Build the parameters to be passed to the deploy command
				//////- String jarPath = new File(targetJar.getArchiveUri()).getAbsolutePath();
                                String jarPath = new File(targetJar.getURI()).getAbsolutePath();
				String fileName = new File(jarPath).getName();
				String moduleName = fileName.substring(0, fileName.lastIndexOf('.'));
				ArrayList parameters = upgradeUtils.buildAppDeploymentParameters(moduleName);
				
				//Should add the target parameter to the deploy command to deploy on specific targets
				String j2eeAppTarget = upgradeUtils.buildAppDeploymentTarget(moduleName);
				if(j2eeAppTarget != null) {
					//Target is either a cluster or stand alone instance
					parameters.add("--target");
					parameters.add(j2eeAppTarget);
				}
				//end - added
				
				if(deploy(jarPath, parameters)) {
					logger.info(stringManager.getString("upgrade.deployment.finishedProcessingMsg") + appName);
				} else {
					logger.warning(appName + " " + stringManager.getString("upgrade.deployment.errorProcessingMsg"));
				}
			} catch(Exception ex) {
				logger.severe(stringManager.getString("upgrade.deployment.generalExceptionMsg")+ ex.toString() + ": " + ex.getMessage());
			}
		}
	}
	
	
	private HashMap getAllWebModuleContextRoot(){
		
		HashMap contextRootsMap = new HashMap();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		factory.setNamespaceAware(true);
		factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",Boolean.FALSE);
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
				("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
			Document sourceDoc = builder.parse( new File(commonInfo.getSourceConfigXMLFile()) );
			
			NodeList nl = sourceDoc.getElementsByTagName("web-module");
			if(nl != null){
				for(int i =0; i < nl.getLength(); i++){
					Node node = nl.item(i);
					NamedNodeMap map = node.getAttributes();
					String contextRoot = map.getNamedItem("context-root").getNodeValue();
					String appName = map.getNamedItem("name").getNodeValue();
					contextRootsMap.put(appName,contextRoot);
				}
			}
		} catch (Exception ex){
			logger.log(Level.SEVERE, stringManager.getString("upgrade.deployment.generalExceptionMsg") + ex.toString());
		}
		return contextRootsMap;
	}
	
	
	private void processStandaloneModules(File srcModuleDir) {
		UpgradeUtils upgradeUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
		HashMap webModuleContextRootMap = getAllWebModuleContextRoot();
		
		File [] moduleDirs = srcModuleDir.listFiles();
		for (int i=0;i<moduleDirs.length;i++) {
			String moduleDirName = moduleDirs[i].getName();
			File jarFile = processModule(MODULE_DIR, "", moduleDirName);
			String moduleName = moduleDirName;
			if (moduleDirName.endsWith("_jar") || moduleDirName.endsWith("_war") || moduleDirName.endsWith("_rar")) {
				moduleName = moduleDirName.substring(0,moduleDirName.lastIndexOf('_'));
			} else {
				moduleName = moduleDirName;
			}
			
			// Build the parameters to be passed to the deploy command
			String jarPath = jarFile.getAbsolutePath();
			String fileName = new File(jarPath).getName();
			String modName = fileName.substring(0, fileName.lastIndexOf('.'));
			ArrayList parameters = upgradeUtils.buildAppDeploymentParameters(modName);
			
			
			String contextRoot = moduleDirName ;
			if(jarFile.getName().toLowerCase().endsWith(".war")){
				contextRoot = (String) upgradeUtils.buildWebModuleContextRoot(moduleDirName);
				
			}
			
			if(parameters == null) {
				parameters = new ArrayList(2);
				parameters.add("--contextroot");
				parameters.add(contextRoot);
			} else {
				parameters.add("--contextroot");
				parameters.add(contextRoot);
			}
			
			//Should add the target parameter to the deploy command to deploy on specific targets
			String webModuleTarget = upgradeUtils.buildAppDeploymentTarget(moduleName);
			if(webModuleTarget != null) {
				//Target is either a cluster or stand alone instance
				parameters.add("--target");
				parameters.add(webModuleTarget);
			}
			
			if(deploy(jarFile.getAbsolutePath(),parameters)) {
				logger.info(stringManager.getString("upgrade.deployment.finishedProcessingMsg") + moduleName );
			} else {
				logger.warning(moduleName + " " + stringManager.getString("upgrade.deployment.errorProcessingMsg"));
			}
		}
	}
	
	//process a module and return it as a jar file
	private File processModule(String appDir, String earDirName, String moduleDirName) {
		String moduleName = moduleDirName;
		if (moduleDirName.endsWith("_jar") || moduleDirName.endsWith("_war") || moduleDirName.endsWith("_rar")) {
			moduleName = moduleDirName.substring(0,moduleDirName.lastIndexOf('_'));
		}
		try {
			
			//////- JarArchiveFactory jaf = new JarArchiveFactory();
                        OutputJarArchive jaf = new OutputJarArchive();
			//////- FileArchiveFactory faf = new FileArchiveFactory();
                        InputJarArchive faf = new InputJarArchive();
			//////- FileArchive farc = (FileArchive)faf.openArchive(new File(new File(new File(sourceDir, appDir), earDirName), moduleDirName).getAbsolutePath());
			faf.open(java.net.URI.create(new File(new File(new File(sourceDir, appDir), earDirName), moduleDirName).getAbsolutePath()));
			InputJarArchive farc = faf;
                        
                        String suffix = ".jar"; //default to .jar
			//File temp;
			Enumeration e = farc.entries();
			//figure out what type of module this is by the existance of the standard dd's
			while(e.hasMoreElements()) {
				String entry = (String)e.nextElement();
				if (entry.equalsIgnoreCase("WEB-INF/web.xml")) {
					suffix = ".war";
				} else if (entry.equalsIgnoreCase("META-INF/ra.xml")) {
					suffix = ".rar";
				}
			}
			
			File tempJar = new File(targetDir, moduleName + suffix);
			String path = tempJar.getAbsolutePath();
			/////// OutputJarArchive targetModule = (OutputJarArchive)jaf.createArchive(path);
                        jaf.create(java.net.URI.create(path));
                        OutputJarArchive targetModule = jaf;
			////// logger.fine(stringManager.getString("upgrade.deployment.addingInfoMsg") + targetModule.getArchiveUri());
                        logger.fine(stringManager.getString("upgrade.deployment.addingInfoMsg") + targetModule.getURI().getPath());
			e = farc.entries();
			while(e.hasMoreElements()) {
				String entry = (String)e.nextElement();
                                ////java.util.zip.ZipEntry ze = farc.getEntry(entry);
				InputStream in = farc.getEntry(entry);
                                ////InputStream in = farc.getInputStream(ze);
				if (entry.equals("WEB-INF/web.xml")) {
					InputStream fixedDescriptor = fixWebServiceDescriptor(farc);
					if(fixedDescriptor != null) {
						in = fixedDescriptor;
					}
				}
				
				if(entry.equals("WEB-INF/sun-web.xml")) {
					checkDescriptors(farc, "sun-web.xml", "WEB-INF");
				}
				if(entry.equals("META-INF/sun-ejb-jar.xml")) {
					checkDescriptors(farc, "sun-ejb-jar.xml", "META-INF");
				}
				
				OutputStream out = null;
				try {
					out = targetModule.putNextEntry(entry);
					int i = in.read();
					while (i > -1) {
						out.write(i);
						i = in.read();
					}
				} catch(java.util.zip.ZipException z) {
					logger.warning(stringManager.getString("upgrade.deployment.zipExceptionMsg")+z.getMessage());
				}catch (IOException ioe) {
					logger.severe(stringManager.getString("upgrade.deployment.ioExceptionMsg")+ioe.getMessage());
				} finally {
					targetModule.closeEntry();
					if (in != null) in.close();
				}
				
			}
                        //-java.util.zip.ZipEntry ze = farc.getEntry(JarFile.MANIFEST_NAME);
			InputStream in = farc.getEntry(JarFile.MANIFEST_NAME);
                        //-InputStream in = farc.getInputStream(ze);
			OutputStream out = null;
			try {
				if(in != null){
					out = targetModule.putNextEntry(JarFile.MANIFEST_NAME);
					int i = in.read();
					while (i > -1) {
						out.write(i);
						i = in.read();
					}
				}
			} catch(java.util.zip.ZipException z) {
				logger.warning(stringManager.getString("upgrade.deployment.zipExceptionMsg")+z.getMessage());
			}catch (IOException ioe) {
				logger.severe(stringManager.getString("upgrade.deployment.ioExceptionMsg")+ioe.getMessage());
			}finally {
				targetModule.closeEntry();
				targetModule.close();
				if (in != null) in.close();
			}
			return tempJar;
		} catch(IOException ex) {
			logger.severe(stringManager.getString("upgrade.deployment.generalExceptionMsg")+ ex.toString() + ": " + ex.getMessage());
		}
		return null;
	}
	
	/**
	 * Lifecycle modules are processed based on the configuration entry.
	 */
	private void processLifecycles(){
		String sourceConfigXMLFile = commonInfo.getSourceConfigXMLFile();
		String targetConfigXMLFile = commonInfo.getTarget().getConfigXMLFile();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setAttribute(
			"http://apache.org/xml/features/nonvalidating/load-external-dtd",
			Boolean.FALSE);
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
				("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
			Document sourceDoc = builder.parse(new File(sourceConfigXMLFile));
			Document targetDoc = builder.parse(new File(targetConfigXMLFile));
			
			//Get lifecycle-modules from source
			NodeList nl = sourceDoc.getElementsByTagName("lifecycle-module");
			
			//Iterate through the list (if any)
			for(int i =0; i < nl.getLength(); i++){
				Node node = nl.item(i);
				Node newNode = targetDoc.importNode(node, true);
				NamedNodeMap attributes = newNode.getAttributes();
				String lcname = attributes.getNamedItem("name").getNodeValue();
				Node classpathNode = attributes.getNamedItem("classpath");
				try {
					String classpath = null;
					if (classpathNode != null) {
						classpath = classpathNode.getNodeValue();
						
						//- replace env var '${com.sun.aas.installRoot}' with value
						if (classpath.startsWith("${" + UpgradeConstants.AS_INSTALL_ROOT + "}")){
							String tmpVar = "\\$\\{" + UpgradeConstants.AS_INSTALL_ROOT + "}";
							String tmpC = classpath.replaceAll(tmpVar, commonInfo.getTarget().getInstallRootProperty());
							classpath = tmpC;
						}
						
						File testPath = new File(classpath);
						if(!testPath.exists()) {
							logger.warning( stringManager.getString(
								"upgrade.deployment.lifecycleErrorMsg") +
								lcname );
							logger.warning( stringManager.getString(
								"upgrade.deployment.lifecycleClasspathMsg",
								classpath + "  " + lcname));
							continue;
						}
					}
					
					//Check if the same lifecycle module already exists in target
					NodeList appNodeList = targetDoc.getElementsByTagName("applications");
					Node applicationsNode = appNodeList.item(0);
					NodeList applicationsList = applicationsNode.getChildNodes();
					boolean foundDup = false;
					for (int n=0; n < applicationsList.getLength(); n++) {
						Node appNode = (Node)applicationsList.item(n);
						if ( appNode.getNodeName().equals(newNode.getNodeName()) ) {
							NamedNodeMap appNodeAttrs = appNode.getAttributes();
							NamedNodeMap newNodeAttrs = newNode.getAttributes();
							Node appNodeName = appNodeAttrs.getNamedItem("name");
							Node newNodeName = appNodeAttrs.getNamedItem("name");
							String newNodeNameString = newNodeName.getNodeValue();
							//Already exists in target
							if (newNodeNameString.equals(appNodeName.getNodeValue())) {
								logger.warning(stringManager.getString(
									"upgrade.deployment.lifecycleExistsMsg",
									newNodeNameString ));
								foundDup = true;
								break;
							}
						}
					}
					if (!foundDup) {
						applicationsNode.appendChild(newNode);
					}
				} catch (SecurityException se) {
					logger.warning(stringManager.getString(
						"upgrade.deployment.lifecycleClasspathMsg",
						node.getNodeName()) + "\n" + se.getMessage());
				}
			}
			
			//Write into resultDoc
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			if (targetDoc.getDoctype() != null){
				String systemValue = targetDoc.getDoctype().getSystemId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
				String pubValue = targetDoc.getDoctype().getPublicId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pubValue);
			}
			DOMSource source = new DOMSource(targetDoc);
			StreamResult result = new StreamResult(new FileOutputStream(targetConfigXMLFile));
			transformer.transform(source, result);
			
		} catch (Exception ex){
			logger.log(Level.SEVERE,
				stringManager.getString("upgrade.deployment.generalExceptionMsg") + ex.toString());
		}
	}
	
	/**
	 * Method to recursively copy the contents of sourceDomainPath/lib to
	 * targetDomainPath/lib
	 */
	private void processLibraries(String sourceDomainPath, String targetDomainPath){
		File sourceDir = new File(sourceDomainPath, SOURCE_LIBRARY_DIR);
		File targetDir = new File(targetDomainPath, TARGET_LIBRARY_DIR);
		try {
			copyDir(sourceDir, targetDir);
		} catch(FileNotFoundException fnf) {
			logger.severe(stringManager.getString(
				"upgrade.deployment.generalExceptionMsg") + fnf.toString() +
				": " + fnf.getMessage());
		} catch(IOException ioe) {
			logger.severe(stringManager.getString(
				"upgrade.deployment.ioExceptionMsg") + ioe.getMessage());
		}
	}
	
	/**
	 * Copies contents of inputDir to outputDir recursively
	 */
	private void copyDir(File inputDir, File outputDir)
	throws FileNotFoundException, IOException {
		UpgradeUtils.copyDirectory(inputDir, outputDir);
	}
	
	/**
	 * Deploys a j2ee-module
	 */
	private boolean deploy(String modulePath, ArrayList parameters) {
		if(commonInfo.isEnterpriseEdition(commonInfo.getSource().getEdition())){
			ArrayList mods = (ArrayList)deployedModules.get(commonInfo.getTarget().getDomainName());
			String fileName = new File(modulePath).getName();
			String moduleName = fileName.substring(0, fileName.lastIndexOf('.'));
			if (mods.contains(moduleName)) {
				String trgDomainXMLFile = commonInfo.getTarget().getConfigXMLFile();
				String adminPort = DomainsProcessor.getTargetDomainPort(trgDomainXMLFile);
				String adminSecurity = DomainsProcessor.getTargetDomainSecurity(trgDomainXMLFile);
				
				String[] createAppRefCommand = {
					"create-application-ref",
					"--user", commonInfo.getSource().getDomainCredentials().getAdminUserName(),
					"--passwordfile ", "\"" + commonInfo.getSource().getDomainCredentials().getPasswordFile()+ "\"",
					"--port",adminPort,
					"--secure=" + adminSecurity,
					"--target", "",
					moduleName
				};
				try {
					return Commands.executeCommand(createAppRefCommand);
				} catch(CommandException ce) {
					logger.log(Level.SEVERE, stringManager.getString("upgrade.deployment.generalExceptionMsg") + ce.toString());
					return false;
				}
			} else {
				if (Commands.deploy(modulePath, commonInfo, parameters)) {
					mods.add(moduleName);
					return true;
				}
				return false;
			}
		}
		return Commands.deploy(modulePath, commonInfo, parameters);
	}
	
	private boolean startDomain(String domainName) {
		return Commands.startDomain(domainName, commonInfo);
	}
	
	private boolean stopDomain(String domainName) {
		return Commands.stopDomain(domainName, commonInfo);
	}
	
	
	public String getName() {
		return stringManager.getString("upgrade.deployment.moduleName");
	}
	
        ////// private void checkDescriptors(FileArchive farc, String fileName, String dirName) throws IOException {
	private void checkDescriptors(InputJarArchive farc, String fileName, String dirName) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setEntityResolver(new SaxParserHandler());
			//// String dirPath = farc.getArchiveUri();
                        String dirPath = farc.getURI().getPath();
			Document document = docBuilder.parse(dirPath + File.separatorChar + dirName + File.separatorChar + fileName);
			Element docEle = document.getDocumentElement();
			NodeList securityBindingList = docEle.getElementsByTagName("message-security-binding");
			for(int i=0; i<securityBindingList.getLength();i++) {
				Element element = (Element) securityBindingList.item(i);
				if(element != null) {
					if(element.getAttribute("provider-id") != null) {
						if(element.getAttribute("provider-id").equals("ClientProvider"))
							logger.log(Level.WARNING, stringManager.getString("upgrade.deployment.messageSecurityConfig",
								fileName, "ClientProvider"));
						else if(element.getAttribute("provider-id").equals("ServerProvider"))
							logger.log(Level.WARNING, stringManager.getString("upgrade.deployment.messageSecurityConfig",
								fileName, "ServerProvider"));
					}
				}
			}
		} catch(IOException ioe) {
			logger.log(Level.WARNING, stringManager.getString("upgrade.deployment.ioExceptionMsg") + ioe.getLocalizedMessage());
		} catch (Exception e) {
			logger.log(Level.WARNING, stringManager.getString("upgrade.deployment.generalExceptionMsg") + e.getLocalizedMessage());
		}
		
	}
	
	
        /////-private InputStream fixWebServiceDescriptor(FileArchive farc) throws IOException {
	private InputStream fixWebServiceDescriptor(InputJarArchive farc) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setEntityResolver(new SaxParserHandler());
			//- String dirPath = farc.getArchiveUri();
                        String dirPath = farc.getURI().getPath();
			Document document = docBuilder.parse(dirPath + File.separatorChar + "WEB-INF" + File.separatorChar + "web.xml");
			Element docEle = document.getDocumentElement();
			NodeList servletList = docEle.getElementsByTagName("servlet");
			for(int i=0; i<servletList.getLength();i++) {
				Node currentServletNode = servletList.item(i);
				NodeList nodeList = ((Element)currentServletNode).getElementsByTagName("servlet-name");
				String servletName = getTextNodeData(nodeList.item(0));
				nodeList = ((Element)currentServletNode).getElementsByTagName("servlet-class");
				Node servletClassNode = nodeList.item(0); //there is only one servlet-class element
				if(servletClassNode == null) {
					return null;
				}
				String servletClass = getTextNodeData(servletClassNode);
				if(servletClass.equals("com.sun.enterprise.webservice.JAXRPCServlet")) {
					Document sunWebXml = docBuilder.parse(dirPath + File.separatorChar + "WEB-INF" + File.separatorChar + "sun-web.xml");
					Element de = sunWebXml.getDocumentElement();
					NodeList sunServletList = de.getElementsByTagName("servlet");
					for(int x=0;x<sunServletList.getLength();x++){
						Node sunServletNode = sunServletList.item(x);
						NodeList list = ((Element)sunServletNode).getElementsByTagName("servlet-name");
						String sunServletName = getTextNodeData(list.item(0));
						if(sunServletName.equals(servletName)) {
							NodeList nList = ((Element)sunServletNode).getElementsByTagName("servlet-impl-class");
							Node servletImplNode = nList.item(0);
							String origServletClass = getTextNodeData(servletImplNode);
							setTextNodeData(servletClassNode, origServletClass);
						}
					}
				}
			}
			// write out the document to a temporary file.
			// Use a Transformer for output
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			if (document.getDoctype() != null){
				String systemValue = document.getDoctype().getSystemId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
				String pubValue = document.getDoctype().getPublicId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pubValue);
			}
			DOMSource source = new DOMSource(document);
			File webTempFile = File.createTempFile("web","xml");
			StreamResult result = new StreamResult(new FileOutputStream(webTempFile));
			transformer.transform(source, result);
			return new FileInputStream(webTempFile);
		} catch (IOException ioe) {
			logger.log(Level.WARNING, stringManager.getString("upgrade.deployment.ioExceptionMsg") + ioe.getLocalizedMessage());
		} catch (Exception e) {
			logger.log(Level.WARNING, stringManager.getString("upgrade.deployment.generalExceptionMsg") + e.getLocalizedMessage());
		}
		////java.util.zip.ZipEntry ze = farc.getEntry("WEB-INF/web.xml");
		return farc.getEntry("WEB-INF/web.xml");
                ////return farc.getInputStream(ze);
	}
	
	private String getTextNodeData(Node node){
		NodeList children = ((Element)node).getChildNodes();
		for(int index=0; index < children.getLength(); index++){
			if(children.item(index).getNodeType() == Node.TEXT_NODE){
				return children.item(index).getNodeValue();
			}
		}
		return "";
	}
	
	private void setTextNodeData(Node node, String text){
		NodeList children = ((Element)node).getChildNodes();
		for(int index=0; index < children.getLength(); index++){
			if(children.item(index).getNodeType() == Node.TEXT_NODE){
				children.item(index).setNodeValue(text);
			}
		}
	}
}
