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

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * This class is used to compare if two DOM nodes are equal.
 */
public class XMLComparer {
    public XMLComparer() {
        super();
    }

    /**
     * Compare two DOM nodes.
     * @param control The first node in the comparison.
     * @param test The second node in the comparison.
     * @return Return true if the nodes are equal, else false.
     */
    public boolean isNodeEqual(Node control, Node test) {
        if (control == test) {
            return true;
        } else if ((null == control) || (null == test)) {
            return false;
        } else if (control.getNodeType() != test.getNodeType()) {
            return false;
        }
        switch (control.getNodeType()) {
        case (Node.ATTRIBUTE_NODE):
            return isAttributeEqual((Attr)control, (Attr)test);
        case (Node.CDATA_SECTION_NODE):
            return isTextEqual((Text)control, (Text)test);
        case (Node.COMMENT_NODE):
            return isCommentEqual((Comment)control, (Comment)test);
        case (Node.DOCUMENT_FRAGMENT_NODE):
            return isDocumentFragmentEqual((DocumentFragment)control, (DocumentFragment)test);
        case (Node.DOCUMENT_NODE):
            return isDocumentEqual((Document)control, (Document)test);
        case (Node.DOCUMENT_TYPE_NODE):
            return isDocumentTypeEqual((DocumentType)control, (DocumentType)test);
        case (Node.ELEMENT_NODE):
            return isElementEqual((Element)control, (Element)test);
        case (Node.ENTITY_NODE):
            return false;
        case (Node.ENTITY_REFERENCE_NODE):
            return isEntityReferenceEqual((EntityReference)control, (EntityReference)test);
        case (Node.NOTATION_NODE):
            return false;
        case (Node.PROCESSING_INSTRUCTION_NODE):
            return isProcessingInstructionEqual((ProcessingInstruction)control, (ProcessingInstruction)test);
        case (Node.TEXT_NODE):
            return isTextEqual((Text)control, (Text)test);
        default:
            return true;
        }
    }

    private boolean isAttributeEqual(Attr control, Attr test) {
        if (!isStringEqual(control.getNamespaceURI(), test.getNamespaceURI())) {
            return false;
        }
        if (!isStringEqual(control.getName(), test.getName())) {
            return false;
        }
        if (!isStringEqual(control.getNodeValue(), test.getNodeValue())) {
            return false;
        }
        return true;
    }

    private boolean isCommentEqual(Comment control, Comment test) {
        if (!isStringEqual(control.getNodeValue(), test.getNodeValue())) {
            return false;
        }
        return true;
    }

    private boolean isDocumentEqual(Document control, Document test) {
        if (!isDocumentTypeEqual(control.getDoctype(), test.getDoctype())) {
            return false;
        }

        Element controlRootElement = control.getDocumentElement();
        Element testRootElement = test.getDocumentElement();
        if (controlRootElement == testRootElement) {
            return true;
        } else if ((null == controlRootElement) || (null == testRootElement)) {
            return false;
        }
        return isElementEqual(controlRootElement, testRootElement);
    }

    private boolean isDocumentFragmentEqual(DocumentFragment control, DocumentFragment test) {
        return isNodeListEqual(control.getChildNodes(), test.getChildNodes());
    }

    private boolean isDocumentTypeEqual(DocumentType control, DocumentType test) {
        if (control == test) {
            return true;
        } else if ((null == control) || (null == test)) {
            return false;
        }

        if (!isStringEqual(control.getName(), test.getName())) {
            return false;
        }
        if (!isStringEqual(control.getPublicId(), test.getPublicId())) {
            return false;
        }
        if (!isStringEqual(control.getSystemId(), test.getSystemId())) {
            return false;
        }

        return true;
    }

    private boolean isElementEqual(Element control, Element test) {
        if (!isStringEqual(control.getNamespaceURI(), test.getNamespaceURI())) {
            return false;
        }
        if (!isStringEqual(control.getTagName(), test.getTagName())) {
            return false;
        }

        // COMPARE ATTRIBUTES    
        NamedNodeMap controlAttributes = control.getAttributes();
        NamedNodeMap testAttributes = test.getAttributes();
        int numberOfControlAttributes = controlAttributes.getLength();
        if (numberOfControlAttributes != testAttributes.getLength()) {
            return false;
        }
        Attr controlAttribute;
        Attr testAttribute;
        for (int x = 0; x < numberOfControlAttributes; x++) {
            controlAttribute = (Attr)controlAttributes.item(x);
            if (null == controlAttribute.getNamespaceURI()) {
                testAttribute = (Attr)testAttributes.getNamedItem(controlAttribute.getNodeName());
            } else {
                testAttribute = (Attr)testAttributes.getNamedItemNS(controlAttribute.getNamespaceURI(), controlAttribute.getLocalName());
            }
            if (null == testAttribute) {
                return false;
            } else if (!isAttributeEqual(controlAttribute, testAttribute)) {
                return false;
            }
        }

        // COMPARE CHILD NODES
        return isNodeListEqual(control.getChildNodes(), test.getChildNodes());
    }

    private boolean isEntityReferenceEqual(EntityReference control, EntityReference test) {
        if (!isStringEqual(control.getNodeName(), test.getNodeName())) {
            return false;
        }
        return true;
    }

    private boolean isProcessingInstructionEqual(ProcessingInstruction control, ProcessingInstruction test) {
        if (!isStringEqual(control.getTarget(), test.getTarget())) {
            return false;
        }
        if (!isStringEqual(control.getData(), test.getData())) {
            return false;
        }
        return true;
    }

    private boolean isTextEqual(Text control, Text test) {
        return isStringEqual(control.getNodeValue(), test.getNodeValue());
    }

    private boolean isNodeListEqual(NodeList control, NodeList test) {
        int numberOfControlNodes = control.getLength();
        if (numberOfControlNodes != test.getLength()) {
            return false;
        }
        for (int x = 0; x < numberOfControlNodes; x++) {
            if (!isNodeEqual(control.item(x), test.item(x))) {
                return false;
            }
        }
        return true;
    }

    private boolean isStringEqual(String control, String test) {
        if (control == test) {
            return true;
        } else if (null == control) {
            return false;
        } else {
            return control.equals(test);
        }
    }
}
