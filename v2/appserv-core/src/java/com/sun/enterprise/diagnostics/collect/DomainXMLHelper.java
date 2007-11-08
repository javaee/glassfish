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
package com.sun.enterprise.diagnostics.collect;

import com.sun.enterprise.diagnostics.util.DiagnosticServiceHelper;
import com.sun.enterprise.diagnostics.util.XmlUtils;
import org.w3c.dom.*;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.diagnostics.*;
/**
 * A helper which reads/loads domain.xml and provides information such as 
 * list of confidential attributes.
 * @author Manisha Umbarje
 */
public class DomainXMLHelper {
   
    private Document configDoc;
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static Logger logger = 
    LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
     
    private String repositoryDir;
    /** Creates a new instance of DomainXMLHelper */
    public DomainXMLHelper(String repositoryDir) {
        this.repositoryDir = repositoryDir;
    }
    
    public List<String> getAttrs() throws DiagnosticException {
        if(configDoc == null)
            loadXML();
        ArrayList list = new ArrayList(5);
        XmlUtils.getAttributes(configDoc.getDocumentElement(),
            PASSWORD, list);
        return list;
    }
    
    /**
     * Loads XML - domain.xml in local mode
     */
    private void loadXML() throws DiagnosticException {
	try {
	    String configFile = repositoryDir + 
                    Constants.DOMAIN_XML;
            String configDtdFile = DiagnosticServiceHelper.getInstallationRoot()+
                    Constants.DOMAIN_XML_DTD;
	    logger.log(Level.FINE,
                    "diagnostic-service.loadxml_configfile", configFile);
	    configDoc = XmlUtils.loadXML(configFile , configDtdFile);
            
	} catch (SAXException ex) {
	    logger.log(Level.SEVERE,
            	      "diagnostic-service.error_loading_xml",ex.getMessage());
	    throw new DiagnosticException (ex.getMessage());
	} catch (IOException ioe) {
	    logger.log(Level.SEVERE,
	          "diagnostic-service.error_loading_xml",ioe.getMessage());
	    throw new DiagnosticException (ioe.getMessage());
	} catch (ParserConfigurationException pce) {
	    logger.log(Level.SEVERE,
	          "diagnostic-service.error_loading_xml",pce.getMessage());
	    throw new DiagnosticException (pce.getMessage());
	}
        
    }
    /**
     * Returns specified xml node
     * @param tagName name of tag
     * @param elementName value of tag
     * @return element corresponding to combination of tagName and elementName
     */
    public Element getElement(String tagName, String elementName)
    throws DiagnosticException {
        if( tagName == null && elementName == null)
            return null;
        
	if (configDoc == null)
	    loadXML();

        NodeList list = configDoc.getDocumentElement().getElementsByTagName(tagName);
        if (list != null) {
            int length = list.getLength();
            Element element = null;
            for (int i = 0; i < length ; i++) {
                element = (Element) list.item(i);
                if(element.getAttribute(NAME).equals(elementName))
                    return element;
            }
        }// if (list != null)

	return null;
    }//getElement

    /**
     * Assumes that there is only one child with given tag name
     * @param element element
     * @param tagName name of child element
     * @return first child matching the tagName
     */
    public Element getElement(Element element, String tagName) {
	NodeList list = element.getElementsByTagName(tagName);

	if (list != null) {
	    return (Element)list.item(0);
	}//if
	return null;
    }//getChildByTagName
    
    /**
     * Gets attribute value
     * @param element element
     * @param attrName name of attribute
     * @return value of attribute
     */
    public String getAttribute(Element element , String attrName) {
        if(element != null) 
            return element.getAttribute(attrName); //old value was config-ref    
        return null;
    }
    
}
