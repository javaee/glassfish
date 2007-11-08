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

package com.sun.enterprise.webservice.monitoring;

import java.util.Iterator;
import java.util.Vector;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
/**
 * This class is a name space resolver for the XPath expressions.
 *
 * @author Jerome Dochez
 */
public class NamespaceContextImpl implements NamespaceContext {
    
    /** Creates a new instance of NamespaceContextImpl */
    public NamespaceContextImpl(Document wsdlDoc) {
        // get all the namespaces declarations 
       NamedNodeMap attributes = wsdlDoc.getAttributes();
       if (attributes==null) 
           return;
       for (int i=0;i<attributes.getLength();i++) {
           Node attribute = attributes.item(i);
           System.out.println("attribute " + attribute.getNodeName() + " value " + attribute.getNodeValue());
       }
    }
    
    /**
     * <p>
     * Get Namespace URI bound to a prefix in the current scope
     * </p>
     * @param prefix to look up
     * @return Namespace URI
     */
    public String getNamespaceURI(String prefix) {
        if (prefix==null) {
            throw new IllegalArgumentException("prefix is null");
        }
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            return "http://schemas.xmlsoap.org/wsdl/";
        } 
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        } 
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        if ("soap".equals(prefix)) {
            return "http://schemas.xmlsoap.org/wsdl/soap/";
        }
        if ("soap12".equals(prefix)) {
            return "http://schemas.xmlsoap.org/wsdl/soap12/";
        }
        return XMLConstants.NULL_NS_URI;
    }
    
    /**
     * <p>
     * Get Prefix bound to Namespace URI in the current scope
     * </p>
     * @param URI of Namespace to look up
     * @return bound prefix
     */
    public String getPrefix(String namespace) {
        if (namespace==null) {
            throw new IllegalArgumentException("namespace is null");
        }
        if ("http://schemas.xmlsoap.org/wsdl/".equals(namespace)) {
            return XMLConstants.DEFAULT_NS_PREFIX;            
        } 
        if (XMLConstants.XML_NS_URI.equals(namespace)) {
            return XMLConstants.XML_NS_PREFIX;
        } 
        if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        if ("http://schemas.xmlsoap.org/wsdl/soap/".equals(namespace)) {
            return "soap";
        }
        if(("http://schemas.xmlsoap.org/wsdl/soap12/").equals(namespace)) {
            return "soap12";
        }
        return null;        
    }
     
    /**
     * <p>
     * Get all prefixes bound to a Namespace URI in the current scope
     * </p>
     * @param Namespace URI to look up
     * @return Iterator on all the prefixes
     */
    public Iterator getPrefixes(String namespaceURI) {
        Vector v = new Vector();
        String prefix = getPrefix(namespaceURI);
        if (prefix!=null) {
            v.add(prefix);
        }
        return v.iterator();
    }
}
