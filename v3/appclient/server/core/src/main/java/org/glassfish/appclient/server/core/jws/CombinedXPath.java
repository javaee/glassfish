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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
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
 *
 * @author tjquinn
 */
abstract class CombinedXPath {

    /** xpath expression for the target node in the DOM for the developer's XML */
    private final XPathExpression targetExpr;
    
    /** if developer didn't provide the target, this is the parent where we'll
     * create a new child.
     */
    private final XPathExpression parentExpr;

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
            NodeList developerNodes = (NodeList) targetExpr().evaluate(developerDOM, XPathConstants.NODESET);
            NodeList generatedNodes = (NodeList) targetExpr().evaluate(generatedDOM, XPathConstants.NODESET);

            /*
             * Replace each original developer child (if any) with the counterpart
             * generated one.
             */
            for (int i = 0; i < generatedNodes.getLength(); i++) {
                final Node generatedNode = generatedNodes.item(i);
                Node developerParent;
                if (developerNodes.getLength() > 0) {
                    developerParent = developerNodes.item(1).getParentNode();
                    developerParent.replaceChild(
                            developerNodes.item(i),
                            developerDOM.adoptNode(generatedNode));
                } else {
                    developerParent = (Node) parentExpr().evaluate(developerDOM, XPathConstants.NODE);
                    developerParent.appendChild(developerDOM.adoptNode(
                            generatedNode));
                }
            }
        }
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

            Node firstOriginalChild;
            Node developerParent;
            if (developerNodes.getLength() > 0) {
                firstOriginalChild = developerNodes.item(1);
                developerParent = firstOriginalChild.getParentNode();
            } else {
                firstOriginalChild = null;
                developerParent = (Node) parentExpr().evaluate(developerDOM, XPathConstants.NODE);
            }

            /*
             * Insert each generated node in front of the developer-provided
             * one(s).
             */
            for (int i = 0; i < generatedNodes.getLength(); i++) {
                final Node generatedNode = generatedNodes.item(i);
                developerParent.insertBefore(developerDOM.adoptNode(generatedNode),
                        firstOriginalChild);
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
                developerParent.appendChild(developerDOM.adoptNode(generatedNodes.item(i)));
            }
        }
    }
}
