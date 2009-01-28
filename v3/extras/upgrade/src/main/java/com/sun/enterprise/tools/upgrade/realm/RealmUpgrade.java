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
 * RealmUpgrade.java
 *
 * Created on September 3, 2003, 4:30 PM
 */

package com.sun.enterprise.tools.upgrade.realm;

import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import com.sun.enterprise.tools.upgrade.transform.elements.*;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;

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

import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;
import com.sun.enterprise.tools.upgrade.common.UpdateProgressManager;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.tools.upgrade.logging.*;
import java.util.logging.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 *
 * @author  Hans Hrasna
 */
public class RealmUpgrade implements com.sun.enterprise.tools.upgrade.common.BaseModule {
    
    private String AS7_FILE_REALM = "com.iplanet.ias.security.auth.realm.file.FileRealm";
    private String AS7_LDAP_REALM = "com.iplanet.ias.security.auth.realm.ldap.LDAPRealm";
    private String AS7_CERTIFICATE_REALM = "com.iplanet.ias.security.auth.realm.certificate.CertificateRealm";
    private String AS7_SOLARIS_REALM = "com.iplanet.ias.security.auth.realm.solaris.SolarisRealm";
    private String AS8_FILE_REALM = "com.sun.enterprise.security.auth.realm.file.FileRealm";
    private String AS8_LDAP_REALM = "com.sun.enterprise.security.auth.realm.ldap.LDAPRealm";
    private String AS8_CERTIFICATE_REALM = "com.sun.enterprise.security.auth.realm.certificate.CertificateRealm";
    private String AS8_SOLARIS_REALM = "com.sun.enterprise.security.auth.realm.solaris.SolarisRealm";
    
    private StringManager stringManager = StringManager.getManager(RealmUpgrade.class);
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private CommonInfoModel commonInfo = null;
    private Vector recoveryList = new Vector();
    
    /** Creates a new instance of RealmUpgrade */
    public RealmUpgrade() {
    }
    
    public boolean upgrade(CommonInfoModel commonInfoModel) {
        logger.log(Level.INFO, stringManager.getString("upgrade.realm.startMessage"));
        this.commonInfo = commonInfoModel;
		boolean flag = true;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",Boolean.FALSE);
        String realmname ="";
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
			Document sourceDoc = builder.parse( new File(commonInfo.getSourceConfigXMLFile()));
            NodeList nl = sourceDoc.getElementsByTagName("auth-realm");
            for(int i =0; i < nl.getLength(); i++){
                Node node = nl.item(i);
                NamedNodeMap attributes = node.getAttributes();
				
                //check for file realm
                realmname = (attributes.getNamedItem("name")).getNodeValue();
                String classname = (attributes.getNamedItem("classname")).getNodeValue();
                if ( classname.equals(AS8_FILE_REALM) ) {
                    NodeList props = node.getChildNodes();
                    for( int j=0; j < props.getLength(); j++ ) {
                        Node propnode = props.item(j);
                        if(propnode.getNodeName().equals("property")) { //skip #text children
                            NamedNodeMap attrs = propnode.getAttributes();
                            if (attrs != null && (attrs.getNamedItem("name").getNodeValue()).equals("file")) {
                                Node valueNode = attrs.getNamedItem("value");
                                ////-System.setProperty("com.sun.aas.instanceRoot", commonInfo.getSourceInstancePath());
								System.setProperty("com.sun.aas.instanceRoot", commonInfo.getSource().getInstallDir());                                
								String rawSourceRealmPath = valueNode.getNodeValue();
                                String sourceRealmPath = RelativePathResolver.resolvePath(rawSourceRealmPath);
                                File sourceRealmFile = new File(sourceRealmPath);
                                String targetRealmPath = commonInfo.getTarget().getDomainDir() + "/" + 
									UpgradeConstants.AS_CONFIG_DIRECTORY + "/" + sourceRealmFile.getName();
								
                                File targetRealmFile = new File(targetRealmPath);
                                backup(targetRealmPath); // backup target keyfile
                                transferKeys(sourceRealmFile, targetRealmFile, builder);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception ex){
            logger.log(Level.SEVERE, stringManager.getString("upgrade.realm.migrationFailureMessage",ex.getMessage()),new Object [] {realmname,ex});
            ex.printStackTrace();
            UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
            flag = false;
        }
        return flag;
    }
    
    private void backup(String filePath) throws IOException {
        String backupFilePath = filePath + ".bak";
        UpgradeUtils.copyFile(filePath, backupFilePath);
        recoveryList.add(filePath);
    }
    
    public void recovery(CommonInfoModel commonInfo) {
        Enumeration e = recoveryList.elements();
        while(e.hasMoreElements()){
            String recoverPath = (String)e.nextElement();
            String backupPath = recoverPath + ".bak";
            try {
                UpgradeUtils.copyFile(backupPath, recoverPath);
                new File(backupPath).delete();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, stringManager.getString("upgrade.realm.recoveryFailureMessage",ioe.getMessage()),new Object[]{recoverPath,ioe});
            }
        }
        
    }
    
    
    //find the named class file or jar that includes the named class file
    //and copy it to the parallel corresponding AS8 directory then append the target
    // server-classpath with the path to the copied jar or class file
    private void migrateClass(String classname, String classpath) {
        boolean found = false;
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator, false);
        String [] fileList = new String [st.countTokens()];
        for(int i=0;i<fileList.length;i++) {
            fileList[i] = st.nextToken();
        }
        String targetDir = commonInfo.getTarget().getDomainDir() + "/" + "lib" + "/" + "ext";
        String file = classname;
        for(int i =0;i<fileList.length;i++) {
            String fileName = fileList[i];
            if(fileName.endsWith(".jar")) {
                try {
                    JarFile jarFile = new JarFile(fileName);
                    Enumeration ee = jarFile.entries();
                    while(ee.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry)ee.nextElement();
                        String entryString = entry.toString();
                        String className = entryString.replaceAll("/",".");
                        if(className.equals(file)) {
                            String target = targetDir + File.pathSeparatorChar + jarFile.getName();
                            UpgradeUtils.copyFile(fileName,target);
                            updateDomainClassPath(jarFile.getName());
                            found = true;
                            break;
                        }
                    }
                } catch (IOException ioe) {
                    (commonInfo.getDefaultLogger()).info(stringManager.getString("upgrade.realm.customRealmClassMessage")
                    + fileName + stringManager.getString("upgrade.realm.IOExceptionMessage"));
                }
            }
            
        }
        if(!found) {
            (commonInfo.getDefaultLogger()).info(stringManager.getString("upgrade.realm.customRealmClassMessage")
            + classname + stringManager.getString("upgrade.realm.manuallyRelocatedMessage"));
        }
    }
    
    private void updateDomainClassPath(String fileName){
        // The method expects the file is put under installRoot/lib/ext
        logger.log(Level.INFO, stringManager.getString("upgrade.realm.updateDomainClassPathMessage"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builderDomainXml = factory.newDocumentBuilder();
            builderDomainXml.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                    ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document resultDoc = builderDomainXml.parse( new File(commonInfo.getTarget().getConfigXMLFile()) );
            this.updateClassPathString(resultDoc,fileName);
            
            // write out the resultDoc to destination file.
            
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
            StreamResult result = new StreamResult(new FileOutputStream(commonInfo.getTarget().getConfigXMLFile()));
            transformer.transform(source, result);
        }catch (Exception ex){
            logger.log(Level.SEVERE, stringManager.getString("upgrade.realm.updateDomainFailureMessage"),ex);
        }
    }
    private void updateClassPathString(Document domainXML, String fileName){
        // The method expects the file is put under installRoot/lib/ext
        Element docEle = domainXML.getDocumentElement();
        NodeList configList = docEle.getElementsByTagName("java-config");
        // There should be only one java-config element.
        Element javaConfElement = (Element)configList.item(0);
        javaConfElement.setAttribute("server-classpath", javaConfElement.getAttribute("server-classpath")+"${path.separator}${com.sun.aas.installRoot}"+
                "/"+"lib"+"/"+"ext"+"/"+fileName);
    }
    
    public String getName() {
        return stringManager.getString("upgrade.realm.moduleName");
    }
    
    private void transferKeys(File sourceRealmFile, File targetRealmFile, DocumentBuilder builder) throws FileNotFoundException, IOException, SAXException {
        Document sourceDoc = builder.parse( new File(commonInfo.getTarget().getConfigXMLFile()));
        BufferedReader reader = new BufferedReader(new FileReader(sourceRealmFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(targetRealmFile));
        String entry;
        while(reader.ready()) {
            entry = reader.readLine();
            if( entry.startsWith("admin")) {
                // 8.1 holds the admin key in a seperate file for the admin-realm
                // previous versions kept it in the default keyfile
                //find the admin-realm file, back it up and replace with a keyfile containing the source admin key entry
                NodeList nl = sourceDoc.getElementsByTagName("auth-realm");
                for(int i =0; i < nl.getLength(); i++){
                    Node node = nl.item(i);
                    NamedNodeMap attributes = node.getAttributes();
                    String name = (attributes.getNamedItem("name")).getNodeValue();
                    if (name.equals("admin-realm")) {
                        //get the name of the keyfile for the admin-realm
                        NodeList props = node.getChildNodes();
                        for( int j=0; j < props.getLength(); j++ ) {
                            Node propnode = props.item(j);
                            if(propnode.getNodeName().equals("property")) { //skip #text children
                                NamedNodeMap attrs = propnode.getAttributes();
                                if (attrs != null && (attrs.getNamedItem("name").getNodeValue()).equals("file")) {
                                    Node valueNode = attrs.getNamedItem("value");
                                    System.setProperty("com.sun.aas.instanceRoot", commonInfo.getTarget().getDomainDir());
                                    String rawSourceRealmPath = valueNode.getNodeValue();
                                    String adminRealmPath = RelativePathResolver.resolvePath(rawSourceRealmPath);
                                    File adminRealmFile = new File(adminRealmPath);
                                    backup(adminRealmPath); // backup admin-realm keyfile
                                    BufferedWriter adminRealmWriter = new BufferedWriter(new FileWriter(adminRealmFile));
                                    adminRealmWriter.write(entry);
                                    adminRealmWriter.newLine();
                                    adminRealmWriter.write("# Domain User and Password - Do Not Delete Entry Above");
                                    adminRealmWriter.close();
                                }
                            }
                        }
                    }
                }
            } else {
                if(!entry.startsWith("#")) { // don't transfer comments
                    writer.write(entry);
                    writer.newLine();
                }
            }
        }
        writer.close();
        reader.close();
    }
}
