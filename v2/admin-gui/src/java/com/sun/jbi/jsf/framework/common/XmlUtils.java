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

package com.sun.jbi.jsf.framework.common;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XmlUtils.java
 *   a collection of useful XML parsing utilties
 * 
 * @author ylee
 */
public class XmlUtils {

    public XmlUtils() {
    }

    /**
     * parse and create a XML Document object from a XML descriptor
     * @param xmlText     xml descriptor text string
     * @return Document object
     */
    public static Document buildDomDocument(String xmlText) {
        StringReader reader = new StringReader(xmlText);
        return buildDomDocument(reader);
    }
    
    /**
     * parse and create a XML Document object from a XML descriptor* 
     * @param xmlReader     xml descriptor reader
     * @return Document object
     */    
    public static Document buildDomDocument(Reader xmlReader) {
        Document xmlDoc = null;
        DocumentBuilderFactory docBuilderFactory =
                DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setErrorHandler( new DefaultHandler() {
                public void fatalError(SAXParseException e)
                        throws SAXException {
                    throw new SAXException(e.getMessage());
                }
            });
            
            InputSource is = new InputSource(xmlReader);
            xmlDoc = docBuilder.parse(is);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return xmlDoc;
    }
    
    /** gets the element
     * @return Element with tagname
     * @param document Document for parent node
     * @param tagName String for tagname
     */
    public static Element getElement(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        return ( nodeList != null ) ? (Element) nodeList.item(0) : null;
    }
    
    /** get the child element
     * @param element parent element
     * @param tagName element to look for
     * @return child element of type tagName
     */
    public static Element getElement(Element element, String tagName) {
        Element childElement = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if ( nodeList!=null && nodeList.getLength()>0 ) {
            childElement = (Element)nodeList.item(0);
        }
        return childElement;
    }    
    
    /** get the child with the specified tag name
     * @param element Element for parent node
     * @param tagName String for tagname
     * @return NodeList for list of children with the tagname
     */
    public static Element getChildElement(Element element, String tagName) {
        NodeList list = getChildElements(element, tagName);
        Element childElement = null;
        if ( list != null && list.getLength() > 0 ) {
            childElement = (Element)list.item(0);
        }
        return childElement;
    }
    
    /** get the children of the same type element tag name
     * @param element Element for parent node
     * @param tagName String for tagname
     * @return NodeList for list of children with the tagname
     */
    public static NodeList getChildElements(Element element, String tagName) {
        NodeList nodeList = element.getChildNodes();
        NodeListImpl list = new NodeListImpl();
        int count = nodeList.getLength();
        for ( int i = 0; i < count; ++i ) {
            Node node = nodeList.item(i);
            if ( node instanceof Element ) {
                String elementName = getElementName((Element)node);
                if ( elementName.equals(tagName) ) {
                    list.add(node);
                }
            }
        }
        return list;
    }    
    
    /**
     * get Element Tag Name with striped prefix.
     * @param element Element object
     * @return String with stripped prefix
     */
    public static String getElementName(Element element) {
        String tagName = element.getTagName();
        return getName(tagName);
    }    
    
    /**
     * strips the prefix of the name if present
     * @param name String value of Name with prefix
     * @return String for name after striping prefix
     */
    public static String getName(String name) {
        int lastIdx = name.lastIndexOf(':');
        if ( lastIdx >= 0 ) {
            return name.substring(lastIdx + 1);
        }
        return name;
    }    
    
    /** retrieve a Text Data in a element
     * @param element Element for text node
     * @return String contains text
     */
    public static String getTextData(Element element) {
        return getText(element).getData();
    }
    
    /** create/retrieve a Text Node in a element
     * @param element Element node
     * @return Text node for text data
     */
    public static Text getText(Element element ) {
        Node node = null;
        element.normalize();
        node = element.getFirstChild();
        if ( node == null || !(node instanceof Text) ) {
            node = element.getOwnerDocument().createTextNode("");
            element.appendChild(node);
        }
        return (Text) node;
    }    
    
    /**
     * NodeListImpl
     *
     */
    public static class NodeListImpl extends ArrayList implements NodeList {
        /** Default Constructor
         */
        public NodeListImpl() {
            super();
        }
        
        /** nodelist length
         * @return int for number of nodes in nodelist
         */
        public int getLength() {
            return this.size();
        }
        
        /** return a node
         * @param aIndex int for the index of the node
         * @return Node at the index
         */
        public Node item(int aIndex) {
            return (Node) this.get(aIndex);
        }
    }   
    
}
