/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.platform.xml.jaxp;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oracle.toplink.essentials.platform.xml.XMLNamespaceResolver;
import oracle.toplink.essentials.platform.xml.XMLParser;
import oracle.toplink.essentials.platform.xml.XMLPlatform;
import oracle.toplink.essentials.platform.xml.XMLPlatformException;
import oracle.toplink.essentials.platform.xml.XMLTransformer;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;

public class JAXPPlatform implements XMLPlatform {
    public JAXPPlatform() {
        super();
    }

    /**
     * Execute advanced XPath statements that are required for TopLink EIS.
     * @param  contextNode the node relative to which the XPath
     *         statement will be executed.
     *         xPath the XPath statement
     *         namespaceResolver used to resolve namespace prefixes
     *         to the corresponding namespace URI
     * @return the XPath result
     * @throws XMLPlatformException
     */
    public NodeList selectNodesAdvanced(Node contextNode, String xPath, XMLNamespaceResolver xmlNamespaceResolver) throws XMLPlatformException {
        throw oracle.toplink.essentials.exceptions.ValidationException.operationNotSupported("selectNodesAdvanced");
    }

    /**
     * Execute advanced XPath statements that are required for TopLink EIS.
     * @param contextNode
     * @param xPath
     * @param xmlNamespaceResolver
     * @return
     * @throws XMLPlatformException
     */
    public Node selectSingleNodeAdvanced(Node contextNode, String xPath, XMLNamespaceResolver xmlNamespaceResolver) throws XMLPlatformException {
        throw oracle.toplink.essentials.exceptions.ValidationException.operationNotSupported("selectSingleNodeAdvanced");
    }

    public boolean isWhitespaceNode(Text text) {
        String value = text.getNodeValue();
        if (null == value) {
            return false;
        } else {
            return value.trim().equals("");
        }
    }

    public XMLParser newXMLParser() {
        return new JAXPParser();
    }

    public XMLTransformer newXMLTransformer() {
        return new JAXPTransformer();
    }

    public Document createDocument() throws XMLPlatformException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (Exception e) {
            throw XMLPlatformException.xmlPlatformCouldNotCreateDocument(e);
        }
    }

    public Document createDocumentWithPublicIdentifier(String name, String publicIdentifier, String systemIdentifier) throws XMLPlatformException {
        try {
            if (null == publicIdentifier) {
                return createDocumentWithSystemIdentifier(name, systemIdentifier);
            }

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            DOMImplementation domImpl = documentBuilder.getDOMImplementation();
            DocumentType docType = domImpl.createDocumentType(name, publicIdentifier, systemIdentifier);
            Document document = domImpl.createDocument(null, name, docType);
            return document;
        } catch (Exception e) {
            throw XMLPlatformException.xmlPlatformCouldNotCreateDocument(e);
        }
    }

    public Document createDocumentWithSystemIdentifier(String name, String systemIdentifier) throws XMLPlatformException {
        try {
            Document document = null;

            if (null == systemIdentifier) {
                document = createDocument();
                Element rootElement = document.createElement(name);
                document.appendChild(rootElement);
                return document;
            }

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            DOMImplementation domImpl = documentBuilder.getDOMImplementation();
            DocumentType docType = domImpl.createDocumentType(name, null, systemIdentifier);
            document = domImpl.createDocument(null, name, docType);
            return document;
        } catch (Exception e) {
            throw XMLPlatformException.xmlPlatformCouldNotCreateDocument(e);
        }
    }

    public String resolveNamespacePrefix(Node contextNode, String namespacePrefix) throws XMLPlatformException {
        if (namespacePrefix.equals(contextNode.getPrefix())) {
            return contextNode.getNamespaceURI();
        }

        if (contextNode.getNodeType() == Node.ELEMENT_NODE) {
            Element contextElement = (Element)contextNode;
            Attr namespaceDeclaration = contextElement.getAttributeNode("xmlns:" + namespacePrefix);
            if (null != namespaceDeclaration) {
                return namespaceDeclaration.getValue();
            }
        }

        Node parentNode = contextNode.getParentNode();
        if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
            return resolveNamespacePrefix((Element)parentNode, namespacePrefix);
        }

        return null;
    }

    public boolean validateDocument(Document document, URL xmlSchemaURL, ErrorHandler errorHandler) throws XMLPlatformException {
        return true;
    }
}
