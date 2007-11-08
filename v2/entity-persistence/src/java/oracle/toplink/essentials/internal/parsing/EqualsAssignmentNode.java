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
package oracle.toplink.essentials.internal.parsing;

import oracle.toplink.essentials.exceptions.EJBQLException;

/**
 * INTERNAL:
 * EqualsAssignmentNode is implemented to distinguish nodes that hold updates in an update
 * query from other BinaryOperatorNodes
 */
public class EqualsAssignmentNode extends BinaryOperatorNode {

    /**
     * INTERNAL
     * Validate the current node and calculates its type.
     */
    public void validate(ParseTreeContext context) {
        // look for any field access that is not qualified with an variable
        super.validate(context);
        validateTarget(left, context);
    }

    /** */
    private void validateTarget(Node node, ParseTreeContext context) {
        if (node.isDotNode()) {
            TypeHelper typeHelper = context.getTypeHelper();
            Node path = node.getLeft();
            Object type = path.getType();
            AttributeNode attributeNode = (AttributeNode)node.getRight();
            String attribute = attributeNode.getAttributeName();
            if (typeHelper.isSingleValuedRelationship(type, attribute) || 
                typeHelper.isSimpleStateAttribute(type, attribute)) {
                validateNavigation(path, context);
            } else {
                throw EJBQLException.invalidSetClauseTarget(
                    context.getQueryInfo(), attributeNode.getLine(), 
                    attributeNode.getColumn(), path.getAsString(), attribute);
            }
        }
    }

    /** */
    private void validateNavigation(Node qualifier, ParseTreeContext context) {
        if (qualifier.isDotNode()) {
            TypeHelper typeHelper = context.getTypeHelper();
            Node left = qualifier.getLeft();
            AttributeNode attributeNode = (AttributeNode)qualifier.getRight();
            String attribute = attributeNode.getAttributeName();
            Object type = left.getType();
            if (!typeHelper.isEmbeddedAttribute(type, attribute)) {
                throw EJBQLException.invalidSetClauseNavigation(
                    context.getQueryInfo(), attributeNode.getLine(), 
                    attributeNode.getColumn(), qualifier.getAsString(), attribute);
            }
            validateNavigation(left, context);
        }
    }
    
}
