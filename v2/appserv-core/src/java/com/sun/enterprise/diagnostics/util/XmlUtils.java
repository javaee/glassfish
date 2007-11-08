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
package com.sun.enterprise.diagnostics.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.logging.Level;
import java.util.logging.Logger;
        
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;

import com.sun.logging.LogDomains;

/**
 * Collection of helper methods to manipulate XML files.
 * @author Manisha Umbarje
 */
public class XmlUtils {

    /**
     * Load an XML file from disk
     *
     * @param srcXmlFile Absolute path of the XML file to be loaded
     *
     * @return XmlDocument representation of the source XML file
     */
    public static Document loadXML(String srcXmlFile, String dtdFileName)
    throws SAXException, IOException, ParserConfigurationException {
	Document doc = null;
	FileInputStream in = null;
      
	in = new FileInputStream(srcXmlFile);
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().
                newDocumentBuilder();
        docBuilder.setEntityResolver(new NOOPHandler(dtdFileName));
	doc = docBuilder.parse( in );
	if(in!=null)
	    in.close();
	return doc;
    }

    public static void getAttributes(Node element, String srchStr, 
            List list) {
    
        if(element != null && srchStr != null) {
            if(list == null)
                list = new ArrayList(5);
            NodeList children = element.getChildNodes();
            int noOfChildren = children.getLength();

	    for (int i = 0; i < noOfChildren; i++) {
		Node child = children.item(i);
                String childName = child.getLocalName();
		NamedNodeMap attrs = null;
		attrs = child.getAttributes();
		if (attrs != null) {
		    for (int j=0; j<attrs.getLength(); j++) {
			Attr attr = (Attr)attrs.item(j);
			if (attr.getName().toLowerCase().indexOf(srchStr) != -1) {
                            String attrName =getParentNodeName(child);
                            list.add( attrName +  File.separator + attr);
                        }
		    }//for
		}//if

		getAttributes(child,srchStr,list);
            }
        }
    }
    
    private static String getParentNodeName(Node child) {
        if(child != null) {
            String name = child.getNodeName() ;
            //String value = child.getNodeValue();
            Node parent = child.getParentNode();
            if(parent != null &&  (parent.getNodeType() == Node.ELEMENT_NODE))
                name = getParentNodeName(parent) + File.separator+ name ;
            NamedNodeMap attrs =  child.getAttributes();
            if (attrs != null) {
                String nameAttribute = null;
                for (int j=0; j<attrs.getLength(); j++) {
                    Attr attr = (Attr)attrs.item(j);
                    if(attr.getName().toLowerCase().indexOf("name") != -1) {
                        nameAttribute = attr.getValue();
                        continue;
                    }    
                }
                if (nameAttribute != null)
                    name = name + "=" + nameAttribute ;
            }
            return name;
        }
        return "";
    }
    /**
     * Search and replace attributes within the specified XmlDocument
     *
     * @param doc XmlDocument in whicch to perform the search and replace.
     * @param srchStr String within the document to be replaced.
     * @param rplStr String to replace srchStr with.
     *
     */
    public static void attrSearchReplace(Document doc, String srchStr, 
					String rplStr){
	Node m_root = null;
	Node child = null;
	m_root = doc.getDocumentElement();
	child = m_root.getFirstChild();
	while (child != null) {
	    NamedNodeMap attrs = null;
	    attrs = child.getAttributes();
	    if (attrs != null)
		for (int j=0; j<attrs.getLength(); j++) {
		    Attr attr = (Attr)attrs.item(j);
		    if (attr.getName().toLowerCase().indexOf(srchStr) != -1)
			attr.setValue(rplStr);
		}
	    child = child.getNextSibling();
	}
    }

     
    /**
     * Search and replace attributes within the specified XmlDocument
     *
     * @param doc XmlDocument in whicch to perform the search and replace.
     * @param srchStr String within the document to be replaced.
     * @param rplStr String to replace srchStr with.
     *
     */
    public static void attrSearchReplace(Node element, String srchStr, 
					String replaceString) {
	if (element != null) {
	    NodeList children = element.getChildNodes();
	    int noOfChildren = children.getLength();

	    for (int i = 0; i < noOfChildren; i++) {
		Node child = children.item(i);

		NamedNodeMap attrs = null;
		attrs = child.getAttributes();
		if (attrs != null) {
		    for (int j=0; j<attrs.getLength(); j++) {
			Attr attr = (Attr)attrs.item(j);
			if (attr.getName().toLowerCase().indexOf(srchStr) != -1)
			    attr.setValue(replaceString);
		    }//for
		}//if

		attrSearchReplace(child,srchStr,replaceString);
	    }//for
	}//if (element != null)
    }


    /**
     * Copy the specified Document to the specified file
     *
     * @param srcDoc The source XMLDocument
     * @param destFile Absolute path of the destination file
     *
     */
    public static void copyXMLFile(Document srcDoc, String destFile)
    throws IOException, TransformerConfigurationException, TransformerException {
	// Use a Transformer for output
	TransformerFactory tFactory =
		TransformerFactory.newInstance();
	Transformer transformer = tFactory.newTransformer();

	DOMSource source = new DOMSource(srcDoc);
        File destFileObj = new File(destFile);
        if(!destFileObj.getParentFile().exists())
            destFileObj.getParentFile().mkdirs();
	FileWriter destFileWriter = new FileWriter(destFile);
	StreamResult result = new StreamResult(destFileWriter);
	transformer.transform(source, result);
    }
}

class NOOPHandler extends DefaultHandler {

    private String _dtdFileName;
    NOOPHandler(String dtdFileName) {
	super();
	_dtdFileName = dtdFileName;
        //logger.log(Level.INFO, "diagnostic-service.dtd_file_name", 
        //        new Object[] {dtdFileName});
    }

    public InputSource resolveEntity(String publicId,
	 String systemId) throws SAXException
    {
     	InputSource is = null;
	try {
            is = new InputSource(new FileInputStream(_dtdFileName));
	} catch(Exception e) {
            throw new SAXException("cannot resolve dtd", e);
	}
        return is;
    }

}


