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

// Java imports
import java.util.*;

import oracle.toplink.essentials.exceptions.EJBQLException;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a constructor node (NEW)
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for a constructor 
 * </ul>
 */
public class ConstructorNode extends Node {

    /** The name of the constructor class. */
    private String className = null;
    
    /** The list of constructor call argument nodes */
    public List constructorItems = new ArrayList();

    /**
     * Return a new ConstructorNode
     */
    public ConstructorNode(String className) {
        this.className = className;
    }

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
        if (theQuery instanceof ReportQuery) {
            SelectGenerationContext selectContext = (SelectGenerationContext)context;
            ReportQuery reportQuery = (ReportQuery)theQuery;
            reportQuery.beginAddingConstructorArguments(
                getConstructorClass(context.getParseTreeContext()));
            for (Iterator i = constructorItems.iterator(); i.hasNext();) {
                Node node = (Node)i.next();
                if (selectingRelationshipField(node, context)) {
                    selectContext.useOuterJoins();
                }
                node.applyToQuery(reportQuery, context);
                selectContext.dontUseOuterJoins();
            }
            reportQuery.endAddingToConstructorItem();
        }
    }
    
    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        for (Iterator i = constructorItems.iterator(); i.hasNext();) {
            Node item = (Node)i.next();
            item.validate(context);
        }

        // Resolve constructor class
        TypeHelper typeHelper = context.getTypeHelper();
        Object type = typeHelper.resolveTypeName(className);
        if (type == null) {
            String name = className;
            // check for inner classes
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(0, index) + '$' + name.substring(index+1);
                type = typeHelper.resolveTypeName(name);
            }
        }
        setType(type);
    }
    
    /**
     * INTERNAL
     * Is this node a ConstructorNode
     */
    public boolean isConstructorNode() {
        return true;
    }

    /**
     * INTERNAL
     * Add an Order By Item to this node
     */
    public void addConstructorItem(Object theNode) {
        constructorItems.add(theNode);
    }

    /**
     * INTERNAL
     * Set the list of constructor items of this node.
     */
    public void setConstructorItems(List items) {
        this.constructorItems = items;
    }

    /**
     * INTERNAL
     * Get the list of constructor items of this node.
     */
    public List getConstructorItems() {
        return this.constructorItems;
    }

    /**
     * Check the specifid constructor class and return its class instance. 
     * @exception EJBQLException if the specified constructor class could not
     * be found.
     */
    private Class getConstructorClass(ParseTreeContext context) {
        Object type = getType();
        if (type == null) {
            throw EJBQLException.constructorClassNotFound(
                context.getQueryInfo(), getLine(), getColumn(), className);
        }
        return (Class)type;
    }

    /**
     * INTERNAL
     */
    private boolean selectingRelationshipField(Node node, GenerationContext context) {
        if ((node == null) || !node.isDotNode()) {
            return false;
        }
        return !((DotNode)node).endsWithDirectToField(context);
    }

    /**
     * INTERNAL
     * Get the string representation of this node.
     */
    public String getAsString() {
        StringBuffer repr = new StringBuffer();
        repr.append("NEW ").append(className);
        repr.append("(");
        for (Iterator i = constructorItems.iterator(); i.hasNext();) {
            Node node = (Node)i.next();
            repr.append(node.getAsString());
            if (i.hasNext()) {
                repr.append(", ");
            }
        }
        repr.append(")");
        return repr.toString();
    }
}
