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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * INTERNAL
 * Utility class for finding XML nodes using XPath expressions.
 */
public class XPathEngine {
    private static final String ATTRIBUTE = "@";
    private static final String TEXT = "text()";
    private static final String ALL_CHILDREN = "child::*";
    private static final String NAMESPACE_URI = "http://java.sun.com/xml/ns/persistence/orm";
    private static XPathEngine instance = null;

    private XPathEngine() {
        super();
    }

    /**
     * Return the <code>XPathEngine</code> singleton.
     */
    public static XPathEngine getInstance() {
        if (instance == null) {
            instance = new XPathEngine();
        }
        return instance;
    }

    /**
     * Execute the XPath statement relative to the context node.
     *
     * @param contextNode the node relative to which the XPath statement will be executed
     * @param xmlField the field containing the XPath statement to be executed
     * @param namespaceResolver used to resolve namespace prefixes to the corresponding namespace URI
     * @return the first node located matching the XPath statement
     * @throws XMLPlatformException
     */
    public Node selectSingleNode(Node contextNode, String[] xPathFragments) {
        if (contextNode == null) {
            return null;
        }

        return selectSingleNode(contextNode, xPathFragments, 0);
    }

    private Node selectSingleNode(Node contextNode, String[] xPathFragments, int index) {
        Node resultNode = getSingleNode(contextNode, xPathFragments[index]);
        if ((resultNode == null) || (xPathFragments.length == (index + 1))) {
            return resultNode;
        }

        return selectSingleNode(resultNode, xPathFragments, index + 1);
    }

    /**
     * Execute the XPath statement relative to the context node.
     *
     * @param contextNode the node relative to which the XPath statement will be executed
     * @param xmlField the field containing the XPath statement to be executed
     * @param namespaceResolver used to resolve namespace prefixes to the corresponding namespace URI
     * @return a list of nodes matching the XPath statement
     * @throws XMLPlatformException
     */
    public NodeList selectNodes(Node contextNode, String[] xPathFragments) {
        if (contextNode == null) {
            return null;
        }

        return selectNodes(contextNode, xPathFragments, 0);
    }

    private NodeList selectNodes(Node contextNode, String[] xPathFragments, int index) {
    
        NodeList resultNodes = getNodes(contextNode, xPathFragments[index]);

        if (xPathFragments.length != index + 1) {
            Node resultNode;
            XMLNodeList result = new XMLNodeList();
            int numberOfResultNodes = resultNodes.getLength();
            for (int x = 0; x < numberOfResultNodes; x++) {
                resultNode = resultNodes.item(x);
                result.addAll(selectNodes(resultNode, xPathFragments, index + 1));
            }
            return result;
        }

        return resultNodes;
    }

    private Node getSingleNode(Node contextNode, String xPathFragment) {
        if (xPathFragment.startsWith(ATTRIBUTE)) {
            return selectSingleAttribute(contextNode, xPathFragment);
        } else if (TEXT.equals(xPathFragment)) {
            return selectSingleText(contextNode);
        }
        return selectSingleElement(contextNode, xPathFragment);
    }

    private NodeList getNodes(Node contextNode, String xPathFragment) {
    	if (xPathFragment.startsWith(ATTRIBUTE)) {
            return selectAttributeNodes(contextNode, xPathFragment);
        } else if (TEXT.equals(xPathFragment)) {
            return selectTextNodes(contextNode);
        } else if (xPathFragment.equals(ALL_CHILDREN)) {
        	return selectChildElements(contextNode);
        }
        return selectElementNodes(contextNode, xPathFragment);

    }

    private Node selectSingleAttribute(Node contextNode, String xPathFragment) {
        Element contextElement = (Element)contextNode;
        return contextElement.getAttributeNode(xPathFragment.substring(1));
    }

    private NodeList selectAttributeNodes(Node contextNode, String xPathFragment) {
        XMLNodeList xmlNodeList = new XMLNodeList();

        Node child = selectSingleAttribute(contextNode, xPathFragment);
        if (null != child) {
            xmlNodeList.add(child);
        }
        return xmlNodeList;
    }

    private Node selectSingleElement(Node contextNode, String xPathFragment) {
        Node child = contextNode.getFirstChild();
        while (null != child) {
            if ((child.getNodeType() == Node.ELEMENT_NODE) && sameName(child, xPathFragment) && sameNamespaceURI(child, NAMESPACE_URI)) {
                return child;
            }

            child = child.getNextSibling();
        }
        return null;
    }

    private NodeList selectChildElements(Node contextNode) {
        XMLNodeList xmlNodeList = new XMLNodeList();
        Node child = contextNode.getFirstChild();

        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                xmlNodeList.add(child);
            }
            child = child.getNextSibling();
        }

        return xmlNodeList;
    }

    private NodeList selectElementNodes(Node contextNode, String xPathFragment) {
        XMLNodeList xmlNodeList = new XMLNodeList();
        Node child = contextNode.getFirstChild();

        while (null != child) {
            if ((child.getNodeType() == Node.ELEMENT_NODE) && sameName(child, xPathFragment) && sameNamespaceURI(child, NAMESPACE_URI)) {
                xmlNodeList.add(child);
            }

            child = child.getNextSibling();
        }

        return xmlNodeList;
    }

    private Node selectSingleText(Node contextNode) {
        NodeList childrenNodes = contextNode.getChildNodes();

        if (childrenNodes.getLength() == 0) {
            return null;
        }

        if (childrenNodes.getLength() == 1) {
            Node child = childrenNodes.item(0);
            if (child.getNodeType() == Node.TEXT_NODE) {
                return child;
            }
            return null;
        }

        String returnVal = null;
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node next = childrenNodes.item(i);
            if (next.getNodeType() == Node.TEXT_NODE) {
                String val = ((Text)next).getNodeValue();
                if (val != null) {
                    if (returnVal == null) {
                        returnVal = new String();
                    }
                    returnVal += val;
                }
            }
        }

        //bug#4515249 a new text node was being created when null should have been returned
        //case where contextNode had several children but no Text children
        if (returnVal != null) {
            return contextNode.getOwnerDocument().createTextNode(returnVal);
        }
        return null;
    }

    private NodeList selectTextNodes(Node contextNode) {
        Node n = selectSingleText(contextNode);

        XMLNodeList xmlNodeList = new XMLNodeList();
        if (n != null) {
            xmlNodeList.add(n);
        }
        return xmlNodeList;
    }

    private boolean sameNamespaceURI(Node node, String namespaceURI) {
        // HANDLE THE NULL CASE
        String nodeNamespaceURI = node.getNamespaceURI();
        if (nodeNamespaceURI == namespaceURI) {
            return true;
        }

        if ((nodeNamespaceURI == null) && namespaceURI.equals("")) {
            return true;
        }

        if ((namespaceURI == null) && nodeNamespaceURI.equals("")) {
            return true;
        }

        // HANDLE THE NON-NULL CASE
        return (null != nodeNamespaceURI) && nodeNamespaceURI.equals(namespaceURI);
    }

    private boolean sameName(Node node, String name) {
        return name.equals(node.getLocalName()) || name.equals(node.getNodeName());
    }
}
