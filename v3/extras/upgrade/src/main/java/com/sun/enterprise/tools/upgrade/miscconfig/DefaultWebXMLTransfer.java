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
 * DefaultWebXMLTransfer.java
 *
 * Created on September 8, 2003, 10:56 AM
 */

package com.sun.enterprise.tools.upgrade.miscconfig;

/**
 *
 * @author  prakash
 */
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;  

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// For transformation.  Not really needed to retain.
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.dom.DOMSource;  
import javax.xml.transform.stream.StreamResult; 

import java.io.*;
import java.util.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.tools.upgrade.common.*;

public class DefaultWebXMLTransfer {
    private StringManager stringManager = StringManager.getManager(DefaultWebXMLTransfer.class);
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private CommonInfoModel commonInfo = null;
    
    /** 
     * Creates a new instance of DefaultWebXMLTransfer 
     */
    public DefaultWebXMLTransfer(CommonInfoModel commonInfo) {
        this.commonInfo = commonInfo;
    }
    
    /**
     * Method to transform default-web.xml file from source to target
     */
    public void transform(String targetFileName){
        logger.log(Level.INFO, stringManager.getString(
                "upgrade.configTransfers.defaultWebXML.startMessage"));
        File targetFile = new File(targetFileName);
        this.modifyContent(targetFile);
    }
    
    /**
     * Method to transfer file contents from source to target
     */
    private void transferFileContents(File source, File target) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (new FileInputStream(source)));
        PrintWriter writer = new PrintWriter(new FileOutputStream(target));
        String readLine = null;
        while((readLine = reader.readLine()) != null){
            writer.println(readLine);
        }
        writer.flush();
        writer.close();
        reader.close();
    }
    
    private void modifyContent(File xmlFile){
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(commonInfo);
        Document document = upgrUtils.getDomainDocumentElement(xmlFile.toString());
        try {
            Element docEle = document.getDocumentElement();
            // Need to obtain the servlet element named jsp
            NodeList servlets = docEle.getElementsByTagName("servlet");
            for(int lh =0; lh < servlets.getLength(); lh++){
                Element servlet = ((Element)servlets.item(lh));
                NodeList ssNames = servlet.getElementsByTagName("servlet-name");
                // There should be only one servlet-name
                if(this.getTextNodeData((Element)ssNames.item(0)).equals("jsp")){
                    NodeList ssClNames = servlet.getElementsByTagName("servlet-class");
                    this.setTextNodeData((Element)ssClNames.item(0), 
                            "org.apache.jasper.servlet.JspServlet");
                    break;
                }
            }
            this.addServletMapping(docEle);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
            transformer.transform(source, result);
            
        }catch (Exception ex){
            // Log error
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.defaultWebXML.startFailureMessage",
                    ex.getMessage()), ex);
        }
    }
    
    private String getTextNodeData(Element element){
        NodeList children = element.getChildNodes();
        for(int index=0; index < children.getLength(); index++){
            if(children.item(index).getNodeType() == Node.TEXT_NODE){
                return children.item(index).getNodeValue();
            }
        }
        return "";
    }    
    private void setTextNodeData(Element element, String text){
        NodeList children = element.getChildNodes();
        for(int index=0; index < children.getLength(); index++){
            if(children.item(index).getNodeType() == Node.TEXT_NODE){
                children.item(index).setNodeValue(text);
            }
        }        
    }    
    private void addServletMapping(Element documentElement){
        Element sMap = documentElement.getOwnerDocument().createElement("servlet-mapping");
        documentElement.appendChild(sMap);
        Element sName = documentElement.getOwnerDocument().createElement("servlet-name");
        Node sNameTextNode = documentElement.getOwnerDocument().createTextNode("jsp");
        sName.appendChild(sNameTextNode);
        sMap.appendChild(sName);
        
        Element urlPat = documentElement.getOwnerDocument().createElement("url-pattern");
        Node urlTextNode = documentElement.getOwnerDocument().createTextNode("*.jspx");
        urlPat.appendChild(urlTextNode);
        sMap.appendChild(urlPat);   
    }
}
