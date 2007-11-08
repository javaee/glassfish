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

import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * INTERNAL:
 * ModifyNode is the superclass for UpdateNode and DeleteNode
 */
public abstract class ModifyNode extends QueryNode {

    private String abstractSchemaIdentifier;
    private String abstractSchemaName;

    /**
     * INTERNAL
     * Apply this node to the passed query.  This node does not change the query.
     */
    public void applyToQuery(DatabaseQuery theQuery, GenerationContext context) {
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        // If defined use the abstractSchemaIdentifier as the base variable,
        // otherwise use the abstractSchemaName 
        String baseVariable = getCanonicalAbstractSchemaIdentifier();
        context.setBaseVariable(baseVariable);
        super.validate(context);
    }
    
    /**
     * INTERNAL
     */
    public Expression generateExpression(GenerationContext context) {
        return null;
    }

    /**
     * INTERNAL
     */
    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    /**
     * INTERNAL
     */
    public void setAbstractSchemaName(String abstractSchemaName) {
        this.abstractSchemaName = abstractSchemaName;
    }

    /**
     * INTERNAL
     */
    public String getAbstractSchemaIdentifier() {
        return abstractSchemaIdentifier;
    }

    /**
     * INTERNAL
     */
    public void setAbstractSchemaIdentifier(String identifierName) {
        abstractSchemaIdentifier = identifierName;
    }

    /**
     * INTERNAL:
     * Returns the canonical name of abstract schema identifier. 
     * If the identifier is not specified(unqualified attribute scenario),
     * the canonical name of abstract schema is returned. 
     */
    public String getCanonicalAbstractSchemaIdentifier() {
        String variable = abstractSchemaIdentifier != null ?
                abstractSchemaIdentifier : abstractSchemaName;
        return IdentificationVariableDeclNode.calculateCanonicalName(variable);
    }

    /**
     * resolveClass: Answer the class which corresponds to my variableName. This is the class for
     * an alias, where the variableName is registered to an alias.
     */
    public Class resolveClass(GenerationContext context) {
        String alias = abstractSchemaName;
        ClassDescriptor descriptor = context.getSession().getDescriptorForAlias(alias);
        if (descriptor == null) {
            throw EJBQLException.unknownAbstractSchemaType2(
                context.getParseTreeContext().getQueryInfo(), 
                getLine(), getColumn(), alias);
        }
        Class theClass = descriptor.getJavaClass();
        if (theClass == null) {
            throw EJBQLException.resolutionClassNotFoundException2(
                context.getParseTreeContext().getQueryInfo(), 
                getLine(), getColumn(), alias);
        }
        return theClass;
    }
}
