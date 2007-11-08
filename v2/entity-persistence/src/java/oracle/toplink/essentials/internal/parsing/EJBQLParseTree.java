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
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL
 * <p><b>Purpose</b>: This represents an EJBQL parse tre
 * <p><b>Responsibilities</b>:<ul>
 * <li> Maintain the context for the expression generation
 * <li> Build an initial expression
 * <li> Return a reference class for the expression
 * <li> Maintain the root node for the query
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class EJBQLParseTree extends ParseTree {

    /**
     * EJBQLParseTree constructor comment.
     */
    public EJBQLParseTree() {
        super();
    }

    /**
     * INTERNAL
     * Build the context to be used when generating the expression from the parse tree
     */
    public GenerationContext buildContext(ReadQuery readQuery, AbstractSession session) {
        GenerationContext contextForGeneration = super.buildContext(readQuery, session);

        contextForGeneration.setBaseQueryClass(readQuery.getReferenceClass());
        return contextForGeneration;
    }

    /**
     * INTERNAL
     * Build the context to be used when generating the expression from the
     * subquery parse tree.
     */
    private GenerationContext buildSubqueryContext(ReadQuery readQuery, GenerationContext outer) {
        GenerationContext context = new SelectGenerationContext(outer, this);
        context.setBaseQueryClass(readQuery.getReferenceClass());
        return context;
    }

    /**
     * Add all of the relevant query settings from an EJBQLParseTree to the given
     * database query.
     * @param query The query to populate
     * @param outer the GenerationContext of the outer EJBQL query.
     * @return the GenerationContext for the subquery
     */
    public GenerationContext populateSubquery(ObjectLevelReadQuery readQuery, GenerationContext outer) {
        GenerationContext innerContext = buildSubqueryContext(readQuery, outer);
        populateReadQueryInternal(readQuery, innerContext);
        return innerContext;
    }
    
    /**
     * Add all of the relevant query settings from an EJBQLParseTree to the given
     * database query.
     * @param query The query to populate
     * @param session The sessionto use to information such as descriptors.
     */
    public void populateQuery(DatabaseQuery query, AbstractSession session) {
        if (query.isObjectLevelReadQuery()) {
            ObjectLevelReadQuery objectQuery = (ObjectLevelReadQuery)query;
            GenerationContext generationContext = buildContext(objectQuery, session);
            populateReadQueryInternal(objectQuery, generationContext);
        } else if (query.isUpdateAllQuery()) {
            UpdateAllQuery updateQuery = (UpdateAllQuery)query;
            GenerationContext generationContext = buildContext(updateQuery, session);
            populateModifyQueryInternal(updateQuery, generationContext);
            addUpdatesToQuery(updateQuery, generationContext);
        } else if (query.isDeleteAllQuery()) {
            DeleteAllQuery deleteQuery = (DeleteAllQuery)query;
            GenerationContext generationContext = buildContext(deleteQuery, session);
            populateModifyQueryInternal(deleteQuery, generationContext);
        }
    }

    private void populateReadQueryInternal(ObjectLevelReadQuery objectQuery, 
                                           GenerationContext generationContext) {
        // Get the reference class if it does not exist.  This is done
        // for dynamic queries in EJBQL 3.0
        if (objectQuery.getReferenceClass() == null) {
            // Adjust the reference class if necessary
            adjustReferenceClassForQuery(objectQuery, generationContext);
        }

        // Initialize the base expression in the generation context
        initBaseExpression(objectQuery, generationContext);
        
        // Validate parse tree
        validate(generationContext.getSession(), getClassLoader());

        // Apply the query node to the query (this will be a SelectNode for a read query)
        applyQueryNodeToQuery(objectQuery, generationContext);
        
        // Verify the SELECT is valid (valid alias, etc)
        verifySelect(objectQuery, generationContext);
        
        // This is what it's all about...
        setSelectionCriteriaForQuery(objectQuery, generationContext);
        
        // Add any ordering
        addOrderingToQuery(objectQuery, generationContext);

        // Add any grouping
        addGroupingToQuery(objectQuery, generationContext);
        
        // Add having
        addHavingToQuery(objectQuery, generationContext);
        
        // Add non fetch joined variables
        addNonFetchJoinAttributes(objectQuery, generationContext);
    }

    private void populateModifyQueryInternal(ModifyAllQuery query, 
                                             GenerationContext generationContext) {
        if (query.getReferenceClass() == null) {
            // Adjust the reference class if necessary
            adjustReferenceClassForQuery(query, generationContext);
        }
        query.setSession(generationContext.getSession());            

        // Initialize the base expression in the generation context
        initBaseExpression(query, generationContext);

        // Validate parse tree
        validate(generationContext.getSession(), getClassLoader());

        // Apply the query node to the query
        applyQueryNodeToQuery(query, generationContext);
        
        setSelectionCriteriaForQuery(query, generationContext);
    }

}
