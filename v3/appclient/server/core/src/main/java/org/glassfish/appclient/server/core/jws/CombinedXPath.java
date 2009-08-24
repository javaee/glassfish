/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.server.core.jws;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XPath-based logic to combine generated JNLP elements with the developer-
 * provided JNLP.
 * <p>
 * Three types of combinations:
 * <ul>
 * <li>owned - the generated content overrides the developer's (we "own" the content)
 * <li>merged - the generated content is merged in with the developer's content
 * <li>defaulted - the generated content is used only if no corresponding developer content exists
 * </ul>
 * <p>
 * This is the abstract superclass for the various types of combinations of
 * generated and developer-provided elements.
 * <p>
 * The client-jnlp-config.properties file contains properties which define
 * which JNLP elements will be combined, one property for each of the types of
 * combinations supported.  Each property's value is a comma-separated list of
 * this form:
 * <p>
 * <code>parent-path:path-within-parent</code>
 * <p>
 * Both the parent-path and the path-within-parent are valid XPath expressions.
 * We need to separate them like this because if the node is not present we
 * need to insert it into the parent, so we need the parent piece separate.
 * For example, the setting
 * <p>
 * <code>/jnlp:/@codebase</code>
 * <p>
 * refers to the codebase attribute within the jnlp element, while
 * <p><code>/jnlp/resources:/property</code><p>
 * refers to the property element within the resources element within the
 * jnlp element.
 *
 * @author tjquinn
 */
abstract class CombinedXPath {

    /** property names for the types of combined JNLP content */
    private static final String OWNED_PROPERTY_NAME = "owned";
    private static final String DEFAULTED_PROPERTY_NAME = "defaulted";
    private static final String MERGED_PROPERTY_NAME = "merged";
    
    private final static XPathFactory xPathFactory = XPathFactory.newInstance();

    private final static XPath xPath = xPathFactory.newXPath();
    
    
    /** xpath expression for the target node in the DOM for the developer's XML */
    private final XPathExpression targetExpr;
    
    /** if developer didn't provide the target, this is the parent where we'll
     * create a new child.
     */
    private final XPathExpression parentExpr;

    private static enum Type {
        OWNED(OWNED_PROPERTY_NAME),
        DEFAULTED(DEFAULTED_PROPERTY_NAME),
        MERGED(MERGED_PROPERTY_NAME);
        
        private String propertyName;
        
        Type(final String propName) {
            propertyName = propName;
        }
        
    }

    static List<CombinedXPath> parse(final Properties p) {
        List<CombinedXPath> result = new ArrayList<CombinedXPath>();
        result.addAll(CombinedXPath.parse(p, CombinedXPath.Type.OWNED));
        result.addAll(CombinedXPath.parse(p, CombinedXPath.Type.DEFAULTED));
        result.addAll(CombinedXPath.parse(p, CombinedXPath.Type.MERGED));
        return result;
    }

    /**
     * For the given combination type fetch the corresponding property value
     * from the config properties and then parse it into the separate
     * parent:with-parent pairs, creating for each pair the correct type of
     * CombinedXPath object and returning a List of them.
     * @param p
     * @param type
     * @return
     */
    private static List<CombinedXPath> parse(
                final Properties p,
                Type type) {
            
            final List<CombinedXPath> result = new
                    ArrayList<CombinedXPath>();
            final String refs = p.getProperty(type.propertyName);
            for (String ref : refs.split(",")) {
                final String paths[] = ref.split(":");
                if (paths.length != 2) {
                    throw new IllegalArgumentException(ref);
                }
                switch (type) {
                    case OWNED:
                        result.add(new OwnedXPath(xPath, paths[0], paths[1]));
                        break;

                    case MERGED:
                        result.add(new MergedXPath(xPath, paths[0], paths[1]));
                        break;

                    case DEFAULTED:
                        result.add(new DefaultedXPath(xPath, paths[0], paths[1]));
                        break;
                }
            }
            return result;
        }

    /**
     * Creates a new combined XPath.
     * 
     * @param xPath XPath available for searching
     * @param parentPath path to parent for new child (if developer's document lacks the target)
     * @param targetRelativePath path relative to the parent for the target node in the developer DOM
     */
    CombinedXPath(
            final XPath xPath,
            final String parentPath,
            final String targetRelativePath) {
        try {
            parentExpr = xPath.compile(parentPath);
            targetExpr = xPath.compile(parentPath + targetRelativePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    XPathExpression targetExpr() {
        return targetExpr;
    }

    XPathExpression parentExpr() {
        return parentExpr;
    }

    /**
     * Processes the given combination: replaces, defaults, or merges.
     *
     * @param developerDOM
     * @param generatedDOM
     * @throws XPathExpressionException
     */
    abstract void process(final Document developerDOM, final Document generatedDOM) throws XPathExpressionException;

    protected void insert(
            final Document originalDOM,
            final Node insertionPoint,
            final Node newNode) throws XPathExpressionException {
        if (newNode instanceof Attr) {
            setAttr(originalDOM, insertionPoint, (Attr) newNode);
        } else {
            insertNode(originalDOM, insertionPoint, newNode);
        }
    }

    private void setAttr(
            final Document originalDOM,
            final Node insertionPoint,
            final Attr newAttr) throws XPathExpressionException {
        final Element parent = (insertionPoint == null) ?
            (Element) parentExpr().evaluate(originalDOM, XPathConstants.NODE) :
            ((Attr) insertionPoint).getOwnerElement();

        parent.setAttribute(newAttr.getName(), newAttr.getValue());
    }

    private void insertNode(
            final Document originalDOM,
            final Node insertionPoint,
            final Node newNode) throws XPathExpressionException {
        final Node parentNode = (insertionPoint == null) ?
            (Node) parentExpr().evaluate(originalDOM, XPathConstants.NODE) :
            insertionPoint.getParentNode();
        parentNode.insertBefore(originalDOM.adoptNode(newNode), insertionPoint);
    }

    /**
     * Represents a node in the document which we completely determine,
     * overriding any corresponding node from the developer's DOM.
     */
    static class OwnedXPath extends CombinedXPath {

        OwnedXPath(
                final XPath xPath,
                final String parentPath,
                final String targetRelativePath) {
            super(xPath, parentPath, targetRelativePath);
        }

        @Override
        void process(Document developerDOM, Document generatedDOM) throws XPathExpressionException {
            final NodeList originalNodes = (NodeList) targetExpr().evaluate(developerDOM, XPathConstants.NODESET);
            final NodeList replacementNodes = (NodeList) targetExpr().evaluate(generatedDOM, XPathConstants.NODESET);

            /*
             * Replace all the matching original children (if any) with the
             * replacement ones.
             */
            final Node insertionPoint = (originalNodes.getLength() > 0) ?
                originalNodes.item(0).getPreviousSibling() : null;

            /*
             * Remove the old nodes first.  They could be attributes and, if so,
             * we need to remove them first before setting them with the
             * replacement values.  Otherwise, if we removed the old nodes
             * after setting the new ones, we could accidentally erase new
             * settings that were intended to replace old settings.
             */

            for (int i = 0; i < originalNodes.getLength(); i++) {
                remove(originalNodes.item(i));
            }
            
            for (int i = 0; i < replacementNodes.getLength(); i++) {
                insert(developerDOM, insertionPoint, replacementNodes.item(i));
            }
        }

        private void remove(final Node originalNode) {
            if (originalNode instanceof Attr) {
                removeAttr((Attr) originalNode);
            } else {
                removeNode(originalNode);
            }
        }

        private void removeNode(final Node originalNode) {
            originalNode.getParentNode().removeChild(originalNode);
        }

        private void removeAttr(final Attr originalAttr) {
            final Element parent = originalAttr.getOwnerElement();
            parent.removeAttribute(originalAttr.getName());
        }

        
//        private void replaceNode(final Node existingNode, final Node replacementNode) {
//            /*
//             * If the node is an attribute we have to use different APIs.
//             */
//            if (existingNode instanceof Attr) {
//                replace((Attr) existingNode, (Attr) replacementNode);
//            } else {
//                replace(existingNode, replacementNode);
//            }
//        }
//
//        private void replace(final Attr existingAttr, final Attr replacementAttr) {
//            Element parent = existingAttr.getOwnerElement();
//            parent.setAttribute(existingAttr.getName(), replacementAttr.getValue());
//        }
//
//        private void replace(final Node existingNode, final Node replacementNode) {
//            Node developerParent = existingNode.getParentNode();
//            developerParent.replaceChild(
//                    existingNode,
//                    existingNode.getOwnerDocument().adoptNode(replacementNode));
//        }
    }

    /**
     * Represents a combination of the two XML documents resulting from
     * merging the two input documents.
     */
    static class MergedXPath extends CombinedXPath {

        MergedXPath(
                final XPath xPath,
                final String parentPath,
                final String targetRelativePath) {
            super(xPath, parentPath, targetRelativePath);
        }

        @Override
        void process(Document developerDOM, Document generatedDOM) throws XPathExpressionException {
            NodeList developerNodes = (NodeList) targetExpr().evaluate(developerDOM, XPathConstants.NODESET);
            NodeList generatedNodes = (NodeList) targetExpr().evaluate(generatedDOM, XPathConstants.NODESET);

            final Node insertionPoint = (developerNodes.getLength() > 0) ?
                developerNodes.item(0) : null;

            for (int i = 0; i < generatedNodes.getLength(); i++) {
                insert(developerDOM, insertionPoint, generatedNodes.item(i));
            }
        }
        
    }

    /**
     * Represents a combination in which the developer's setting is used if
     * present; otherwise the generated document's setting is used.
     */
    static class DefaultedXPath extends CombinedXPath {

        DefaultedXPath(
                final XPath xPath,
                final String parentPath,
                final String targetRelativePath) {
            super(xPath, parentPath, targetRelativePath);
        }

        @Override
        void process(Document developerDOM, Document generatedDOM) throws XPathExpressionException {
            NodeList developerNodes = (NodeList) targetExpr().evaluate(developerDOM, XPathConstants.NODESET);

            if (developerNodes.getLength() > 0) {
                return;
            }

            NodeList generatedNodes = (NodeList) targetExpr().evaluate(generatedDOM, XPathConstants.NODESET);

            Node developerParent = (Node) parentExpr().evaluate(developerDOM, XPathConstants.NODE);

            for (int i = 0; i < generatedNodes.getLength(); i++) {
                append(developerParent, developerDOM.adoptNode(generatedNodes.item(i)));
            }
        }

        private void append(final Node parent, final Node newNode) {
            if (newNode instanceof Attr) {
                ((Element) parent).setAttributeNode((Attr) newNode);
            } else {
                parent.appendChild(parent.getOwnerDocument().importNode(newNode, true /* deep */));
            }
        }
    }
}
