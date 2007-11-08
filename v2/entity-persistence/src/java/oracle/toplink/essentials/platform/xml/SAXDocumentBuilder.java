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

import java.util.Stack;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class SAXDocumentBuilder implements ContentHandler {
    private Document document;
    private Stack nodes = new Stack();
    private XMLPlatform xmlPlatform;

    public SAXDocumentBuilder() {
        super();
        nodes = new Stack();
        xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
    }

    public Document getDocument() {
        return document;
    }

    public Document getInitializedDocument() throws SAXException {
        if (document == null) {
            try {
                document = xmlPlatform.createDocument();
                nodes.push(document);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
        return document;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
        try {
            document = xmlPlatform.createDocument();
            nodes.push(document);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public void endDocument() throws SAXException {
        nodes.pop();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if ((null != namespaceURI) && ("".equals(namespaceURI))) {
            namespaceURI = null;
        }
        Element element = getInitializedDocument().createElementNS(namespaceURI, qName);
        Node parentNode = (Node)nodes.peek();
        parentNode.appendChild(element);
        nodes.push(element);

        int numberOfAttributes = atts.getLength();
        Attr attribute;
        for (int x = 0; x < numberOfAttributes; x++) {
            element.setAttributeNS(atts.getURI(x), atts.getQName(x), atts.getValue(x));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        nodes.pop();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String characters = new String(ch, start, length);
        if (characters.trim().length() == 0) {
            return;
        }
        Text text = getInitializedDocument().createTextNode(characters);
        Node parentNode = (Node)nodes.peek();
        parentNode.appendChild(text);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
        ProcessingInstruction pi = getInitializedDocument().createProcessingInstruction(target, data);
        Node parentNode = (Node)nodes.peek();
        parentNode.appendChild(pi);
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
