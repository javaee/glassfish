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
package oracle.toplink.essentials.platform.xml;

import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;

public interface XMLPlatform {

    /**
     * Creates a new document.
     * @return the new document
     * @throws XMLPlatformException
     */
    public Document createDocument() throws XMLPlatformException;

    /**
     * Creates a new document with the specified public and system
     * identifiers in the DOCTYPE, and adds a root element with the
     * specified name.
     * @param  name the name of the root element
     *         publicIdentifier the public identifier
     *         systemIdentifier the system identifier
     * @return the new document
     * @throws XMLPlatformException
     */
    public Document createDocumentWithPublicIdentifier(String name, String publicIdentifier, String systemIdentifier) throws XMLPlatformException;

    /**
     * Creates a new document with the specified system identifier in
     * the DOCTYPE, and adds a root element with the specified name.
     * @param  name the name of the root element
     *         systemIdentifier the system identifier
     * @return the new document
     * @throws XMLPlatformException
     */
    public Document createDocumentWithSystemIdentifier(String name, String systemIdentifier) throws XMLPlatformException;

    /**
     * Check to see if the text node represents a whitespace node.
     * @param text a potential whitespace node
     * @return if the text node represents a whitespace node.
     */
    public boolean isWhitespaceNode(Text text);

    /**
     * Return the namespace URI for the specified namespace prefix
     * relative to the context node.
     * @param  contextNode the node to be looking for the namespace
     *         URI
     *         namespacePrefix the namespace prefix
     * @return the namespace URI for the specified prefix
     * @throws XMLPlatformException
     */
    public String resolveNamespacePrefix(Node contextNode, String namespacePrefix) throws XMLPlatformException;

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
    public NodeList selectNodesAdvanced(Node contextNode, String xPath, XMLNamespaceResolver xmlNamespaceResolver) throws XMLPlatformException;

    /**
     * Execute advanced XPath statements that are required for TopLink EIS.
     * @param contextNode
     * @param xPath
     * @param xmlNamespaceResolver
     * @return
     * @throws XMLPlatformException
     */
    public Node selectSingleNodeAdvanced(Node contextNode, String xPath, XMLNamespaceResolver xmlNamespaceResolver) throws XMLPlatformException;

    /**
     * Return a concrete implementation of the XML parser abstraction that is
     * compatible with the XML Platform.
     * @return a platform specific XML parser
     */
    public XMLParser newXMLParser();

    /**
     * Return a concrete implementation of the XML transformer abstraction that is
     * compatible with the XML Platform.
     * @return a platform specific XML transfomer
     */
    public XMLTransformer newXMLTransformer();

    /**
     * Validate the document against the XML Schema
     * @param  document the document to be validated
     *         xmlSchemaURL the XML Schema
     *         errorHandler a mechanism for selectively ignoring errors
     * @return true if the document is valid, else false
     * @throws XMLPlatformException
     */
    public boolean validateDocument(Document document, URL xmlSchemaURL, ErrorHandler errorHandler) throws XMLPlatformException;
}
