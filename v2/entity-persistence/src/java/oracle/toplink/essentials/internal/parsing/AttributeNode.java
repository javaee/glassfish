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

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.exceptions.EJBQLException;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.mappings.DatabaseMapping;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a attribute.
 *
 * <p><b>Responsibilities</b>:<ul>
 * </ul>
 */
public class AttributeNode extends Node {

    /** The attribute name. */
    private String name;

    /** Flag indicating outer join */
    private boolean outerJoin;

    /** */
    private boolean requiresCollectionAttribute;

    /** */
    private DatabaseMapping mapping;

    /**
     * Create a new AttributeNode
     */
    public AttributeNode() {
        super();
    }

    /**
     * Create a new AttributeNode with the passed name
     * @param name the attribute name
     */
    public AttributeNode(String name) {
        setAttributeName(name);
    }

    /** 
     * INTERNAL 
     * If called this AttributeNode represents an unqualified field access. 
     * The method returns a DotNode representing a qualified field access with
     * the base variable as left child node and the attribute as right child
     * node. 
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        return (Node)context.getNodeFactory().newQualifiedAttribute(
            getLine(), getColumn(), context.getBaseVariable(), name); 
    }
    
    /**
     * INTERNAL
     * Validate the current node and calculates its type.
     */
    public void validate(ParseTreeContext context) {
        // The type is calculated in the parent DotNode.
    }

    /** */
    public Expression addToExpression(Expression parentExpression, GenerationContext context) {
        if (isCollectionAttribute()) {
            //special case for NOT MEMBER OF
            if (context.hasMemberOfNode()) {
                return parentExpression.noneOf(name, new ExpressionBuilder().equal(context.getMemberOfNode().getLeftExpression()));
            }
            return outerJoin ? parentExpression.anyOfAllowingNone(name) : 
                parentExpression.anyOf(name);
        } else {
            // check whether collection attribute is required
            if (requiresCollectionAttribute()) {
                throw EJBQLException.invalidCollectionMemberDecl(
                    context.getParseTreeContext().getQueryInfo(), 
                    getLine(), getColumn(), name);
            }

            if (context.shouldUseOuterJoins() || isOuterJoin()) {
                return parentExpression.getAllowingNull(name);
            } else {
                return parentExpression.get(name);
            }
        }
    }

    /**
     * INTERNAL
     * Is this node an AttributeNode
     */
    public boolean isAttributeNode() {
        return true;
    }

    /** */
    public String getAttributeName() {
        return name;
    }

    /** */
    public void setAttributeName(String name) {
        this.name = name;
    }

    /** */
    public boolean isOuterJoin() {
        return outerJoin;
    }

    /** */
    public void setOuterJoin(boolean outerJoin) {
        this.outerJoin = outerJoin;
    }

    /** */
    public boolean requiresCollectionAttribute() {
        return requiresCollectionAttribute;
    }

    /** */
    public void setRequiresCollectionAttribute(boolean requiresCollectionAttribute) {
        this.requiresCollectionAttribute = requiresCollectionAttribute;
    }

    /** */
    public DatabaseMapping getMapping() {
        return mapping;
    }

    /** */
    public void setMapping(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    /** */
    public boolean isCollectionAttribute() {
        DatabaseMapping mapping = getMapping();
        return (mapping != null) && mapping.isCollectionMapping();
    }

    /**
     * resolveMapping: Answer the mapping which corresponds to my variableName.
     */
    public DatabaseMapping resolveMapping(GenerationContext context, Class ownerClass) {
        ClassDescriptor descriptor = context.getSession().getDescriptor(ownerClass);
        return (descriptor==null) ? null : descriptor.getMappingForAttributeName(getAttributeName());
    }

    /**
     * resolveClass: Answer the class for the mapping associated with the my variableName in the ownerClass.
     * Answer null if the node represents a mapping that doesn't exist
     */
    public Class resolveClass(GenerationContext context, Class ownerClass) {
        DatabaseMapping mapping;

        mapping = resolveMapping(context, ownerClass);

        // if we are working with a direct-to-field, or the mapping's null,
        // return the owner class
        // Returning the ownerClass when the mapping is null delegates error handling
        // to the query rather than me
        if ((mapping == null) || (mapping.isDirectToFieldMapping())) {
            return ownerClass;
        }

        ClassDescriptor descriptor = mapping.getReferenceDescriptor();
        return (descriptor==null) ? null : descriptor.getJavaClass();
        //return mapping.getReferenceDescriptor().getJavaClass();
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer();
        toStringIndent(indent, buffer);
        buffer.append(toStringDisplayName() + "[" + getAttributeName() + "]");
        return buffer.toString();
    }

    /**
     * INTERNAL
     * Get the string representation of this node.
     */
    public String getAsString() {
        return getAttributeName();
    }
}
