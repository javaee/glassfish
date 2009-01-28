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
 * TransformManager.java
 *
 * Created on August 4, 2003, 12:47 PM
 */

package com.sun.enterprise.tools.upgrade.transform;

import com.sun.enterprise.tools.upgrade.transform.elements.*;
import java.io.File;
import java.io.IOException;

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

// For transformation.  Not really needed to retain.
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import java.io.*;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.appserv.management.client.prefs.MemoryHashLoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfo;
import java.util.logging.*;
import java.util.Vector;
import java.util.Enumeration;
import com.sun.enterprise.tools.upgrade.common.Credentials;
import com.sun.enterprise.tools.upgrade.logging.LogService;

/**
 *
 * @author  prakash
 */
public class TransformManager implements BaseModule{
    
    private static TransformManager transManager;
    private Document sourceDocument;
    private Document resultDocument;
    private StringManager stringManager = StringManager.getManager(TransformManager.class);
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private Vector recoveryList = new Vector();
    
    /** Creates a new instance of TransformManager */
    public TransformManager() {
    }
    
    public static TransformManager getTransformManager(){
        if(transManager == null)
            transManager = new TransformManager();
        return transManager;
    }
    
    public void transform(Document source, Document result){
        this.sourceDocument = source;
        this.resultDocument = result;
        try{
            Element docEle = sourceDocument.getDocumentElement();
            BaseElement baseElement = ElementToObjectMapper.getMapper().getElementObject(docEle.getTagName());
            baseElement.transform(docEle, source.getDocumentElement(), result.getDocumentElement());
        }catch(Exception ex){
            // ****** LOG MESSAGE *************
            logger.log(Level.SEVERE, stringManager.getString("upgrade.transform.startFailureMessage",ex.getMessage()),ex);
        }
    }
    
    /**
     * Method to start upgrade of the transformation module
     */
    public boolean upgrade(CommonInfoModel commonInfo) {
        logger.log(Level.INFO, 
                stringManager.getString("upgrade.transform.startMessage"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        factory.setNamespaceAware(true);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",Boolean.FALSE);
 
        
        try {
            String sourceConfigXMLFile = commonInfo.getSourceConfigXMLFile();
            String targetConfigXMLFile = commonInfo.getTarget().getConfigXMLFile();
            
            //Backup the existing domain.xml in target domain directory
            backup(targetConfigXMLFile, commonInfo);
            
            //Build the document with target domain.xml
            DocumentBuilder builder = factory.newDocumentBuilder();
            DocumentBuilder builderDomainXml = factory.newDocumentBuilder();
            builderDomainXml.setEntityResolver(
                    (org.xml.sax.helpers.DefaultHandler)Class.forName(
                    "com.sun.enterprise.config.serverbeans.ServerValidationHandler").
                    newInstance());
            Document resultDoc = builderDomainXml.parse(new File(targetConfigXMLFile));
            
            //Set commonInfo in BaseElement
            BaseElement.setCommonInfoModel(commonInfo);
            
            //Get source doc and transform
            Document sourceDoc = builder.parse( new File(sourceConfigXMLFile));
            if(sourceDoc.getDocumentElement() != null && 
                    resultDoc.getDocumentElement() != null)
                this.transform(sourceDoc, resultDoc);
            else
                return false;
            
            //Write out the resultDoc to destination file.            
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            
            //DOCTYPE transformation
            if (resultDoc.getDoctype() != null){
                String systemValue = resultDoc.getDoctype().getSystemId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
                String pubValue = resultDoc.getDoctype().getPublicId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pubValue);
            }
            
            DOMSource source = new DOMSource(resultDoc);
            StreamResult result = new StreamResult(
                    new FileOutputStream(targetConfigXMLFile));
            transformer.transform(source, result);
            result.getOutputStream().close();
            
        }catch (Exception ex){
            UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.transform.startFailureMessage",ex.getMessage()),ex);
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.transform.startFailureCheckAccessMessage"));
            return false;
        }
        //login after transformation in case admin port changed
        final File dir = new File (System.getProperty("user.home"));
        File store = new File(dir, MemoryHashLoginInfoStore.DEFAULT_STORE_NAME);
        try {
            MemoryHashLoginInfoStore adminpass = new MemoryHashLoginInfoStore();
			String adminPort = DomainsProcessor.getSourceAdminPort(
				commonInfo.getSourceConfigXMLFile());
            final int port = new Integer(adminPort).intValue();
			Credentials c = commonInfo.getSource().getDomainCredentials();
            final String user = c.getAdminUserName();
            final String pwd = c.getAdminPassword();
            final LoginInfo login = new LoginInfo("localhost", port, user, pwd );
            adminpass.store(login,true);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return true;
    }
    
    private void backup(String filePath, CommonInfoModel commonInfo) 
            throws IOException {
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("com.sun.aas.installRoot", "C:\\Softwares\\Sun\\j2eesdk1.4_beta3");
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document sourceDoc = builder.parse( new File("C:\\temp\\server.xml") );
            Document resultDoc = builder.parse( new File("C:\\temp\\domain.xml") );
            TransformManager transMan = TransformManager.getTransformManager();
            transMan.transform(sourceDoc, resultDoc);
            
            // write out the resultDoc to destination file.
            
            // Use a Transformer for output
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            
            DOMSource source = new DOMSource(resultDoc);
            StreamResult result = new StreamResult(new FileOutputStream("c:\\temp\\domainModified.xml"));
            transformer.transform(source, result);
            result.getOutputStream().close();
            
        } catch (SAXParseException spe) {
            spe.printStackTrace();
        } catch (SAXException sxe) {
            sxe.printStackTrace();
            
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    public String getName() {
        return stringManager.getString("upgrade.transform.moduleName");
    }
}
