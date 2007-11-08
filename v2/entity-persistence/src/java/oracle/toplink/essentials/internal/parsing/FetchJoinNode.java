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
 * INTERNAL
 * <p><b>Purpose</b>: Represent a FETCH JOIN declaration as part of the FROM
 * clause: FETCH JOIN o.customer.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Manage the path node and the outer join flag of the FETCH JOIN clause. 
 * </ul>
 */
public class FetchJoinNode extends Node {

    private Node path;
    private boolean outerJoin;

    /** */
    public Node getPath() {
        return path;
    }
    
    /** */
    public void setPath(Node node) {
        path = node;
    }
    
    /** */
    public boolean isOuterJoin() {
        return outerJoin;
    }

    /** */
    public void setOuterJoin(boolean outerJoin) {
        this.outerJoin = outerJoin;
    }

    /** 
     * INTERNAL 
     * Check the path child node for an unqualified field access and if so,
     * replace it by a qualified field access.
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        if (path != null) {
            path = path.qualifyAttributeAccess(context);
        }
        return this;
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        if (path != null) {
            path.validate(context);
            setType(path.getType());

            // join of embedded attribute is not supported.
            if (path.isDotNode()) {
                TypeHelper typeHelper = context.getTypeHelper();
                VariableNode left = (VariableNode)path.getLeft();
                AttributeNode right = (AttributeNode)path.getRight();
                if ((left != null) && (right != null)) {
                    if (typeHelper.isEmbeddedAttribute(left.getType(), right.getAttributeName()))
                        throw EJBQLException.unsupportJoinArgument(
                            context.getQueryInfo(), getLine(), getColumn(), 
                            "Fetch Join", getType().toString());
                }
            }
        }
    }
}
