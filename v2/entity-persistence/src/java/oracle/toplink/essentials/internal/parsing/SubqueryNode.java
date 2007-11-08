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

import java.util.Set;

import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.ReportQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a subquery.
 */
public class SubqueryNode extends Node {

    private EJBQLParseTree subqueryParseTree;

    /** Set of names of variables declared in an outer scope and used in teh
     * subquery. */
    private Set outerVars;

    /**
     * Return a new SubqueryNode.
     */
    public SubqueryNode() {
        super();
    }

    /** */
    public ReportQuery getReportQuery(GenerationContext context) {
        ReportQuery innerQuery = new ReportQuery();
        GenerationContext innerContext = 
            subqueryParseTree.populateSubquery(innerQuery, context);
        Expression joins = innerContext.joinVariables(outerVars);
        if (joins != null) {
            Expression where = innerQuery.getSelectionCriteria();
            where = appendExpression(where, joins);
            innerQuery.setSelectionCriteria(where);
        }
        return innerQuery;
    }

    /** 
     * INTERNAL 
     * If called the subquery is part of the WHERE clause of an UPDATE or
     * DELETE statement that does not define an identification variable. 
     * The method checks the clauses of the subquery for unqualified fields
     * accesses. 
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        subqueryParseTree.getFromNode().qualifyAttributeAccess(context);
        subqueryParseTree.getQueryNode().qualifyAttributeAccess(context);
        if (subqueryParseTree.getWhereNode() != null) {
            subqueryParseTree.getWhereNode().qualifyAttributeAccess(context);
        }
        if (subqueryParseTree.getGroupByNode() != null) {
            subqueryParseTree.getGroupByNode().qualifyAttributeAccess(context);
        }
        if (subqueryParseTree.getHavingNode() != null) {
            subqueryParseTree.getHavingNode().qualifyAttributeAccess(context);
        }
        return this;
    }
    
    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        subqueryParseTree.validate(context);
        outerVars = context.getOuterScopeVariables();
        SelectNode selectNode = (SelectNode)subqueryParseTree.getQueryNode();
        // Get the select expression, subqueries only have one
        Node selectExpr = (Node)selectNode.getSelectExpressions().get(0);
        setType(selectExpr.getType());
    }

    /**
     * INTERNAL
     * Generate the TopLink expression for this node
     */
    public Expression generateExpression(GenerationContext context) {
        Expression base = context.getBaseExpression();
        ReportQuery innerQuery = getReportQuery(context);
        return base.subQuery(innerQuery);
    }
    
    /**
     * INTERNAL
     * Is this node a SubqueryNode
     */
    public boolean isSubqueryNode() {
        return true;
    }

    /** */
    public void setParseTree(EJBQLParseTree parseTree) {
        this.subqueryParseTree = parseTree;
    }

    /** */
    public EJBQLParseTree getParseTree() {
        return subqueryParseTree;
    }
    
}

