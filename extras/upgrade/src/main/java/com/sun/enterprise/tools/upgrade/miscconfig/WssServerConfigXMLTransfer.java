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
 * WssServerConfigXMLTransfer.java
 *
 */

package com.sun.enterprise.tools.upgrade.miscconfig;

/**
 *
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
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;

public class WssServerConfigXMLTransfer {

    private StringManager stringManager = StringManager.
            getManager(WssServerConfigXMLTransfer.class);
    private Logger logger = CommonInfoModel.getDefaultLogger();

    /** Creates a new instance of WssServerConfigXMLTransfer */
    public WssServerConfigXMLTransfer() {}

    public void transform(String targetFileName){
        logger.log(Level.INFO, stringManager.
            getString("upgrade.configTransfers.wssServerConfig.startMessage"));
        File targetFile = new File(targetFileName);
        this.modifyContent(targetFile);
    }

    private void modifyContent(File xmlFile){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
            Document document = builder.parse( xmlFile );
            Element docEle = document.getDocumentElement();
            // Need to obtain the element named xwss:Encrypt
            NodeList encryptElements = docEle.getElementsByTagName("xwss:Encrypt");
            for(int lh =0; lh < encryptElements.getLength(); lh++){
                Element encryptElement = ((Element)encryptElements.item(lh));
                String encryptAttr = encryptElement.getAttribute("keyEncryptionAlgorithm");
                if(encryptAttr != null) {
                    if(encryptAttr.equals("RSA_v1dot5")) {
                        //Remove this attribute
                        encryptElement.removeAttribute("keyEncryptionAlgorithm");
                    }
                }
            }
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
            transformer.transform(source, result);

        }catch (Exception ex){
            // Log error
            logger.log(Level.SEVERE, stringManager.
                    getString("upgrade.configTransfers.wssServerConfig.startFailureMessage",ex.getMessage()), ex);
        }
    }
}
