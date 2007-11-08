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
 * <p><b>Purpose</b>: Represent a range identification variable
 * declaration as part of the FROM clause FROM Order o.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Manage the abstract schema name range variable declaration. 
 * </ul>
 */
public class RangeDeclNode extends IdentificationVariableDeclNode {

    private String abstractSchemaName;
    
    /** */
    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }
    
    /** */
    public void setAbstractSchemaName(String name) {
        abstractSchemaName = name;
    }
    
    /** 
     * INTERNAL 
     * Check for an unqualified field access. If abstractSchemaName does not
     * define a valid abstract schema name treat it as unqualified field
     * access. Then method qualifies the field access and use it as the path
     * expression of a new join variable declaration node returned by the
     * method. 
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        String name = abstractSchemaName;
        if (typeHelper.resolveSchema(name) == null) {
            // not a known abstract schema name => make it a join node with a
            // qualified attribute access as path expression 
            context.unregisterVariable(getCanonicalVariableName());
            NodeFactory factory = context.getNodeFactory();
            Node path = (Node)factory.newQualifiedAttribute(
                getLine(), getColumn(), context.getBaseVariable(), name);
            return (Node)factory.newVariableDecl(
                getLine(), getColumn(), path, getVariableName());
        }
        return this;
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        super.validate(context);
        TypeHelper typeHelper = context.getTypeHelper();
        Object type = typeHelper.resolveSchema(abstractSchemaName);
        if (type == null) {
            throw EJBQLException.unknownAbstractSchemaType2(
                context.getQueryInfo(), getLine(), getColumn(), abstractSchemaName);
        }
        setType(type);
    }
}
