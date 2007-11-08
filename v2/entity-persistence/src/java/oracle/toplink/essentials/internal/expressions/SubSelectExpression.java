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
package oracle.toplink.essentials.internal.expressions;

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.queryframework.*;

/**
 * This is used to support subselects.
 * The subselect represents a mostly independent (has own expression builder) query using a report query.
 * Subselects can be used for, in (single column), exists (empty or non-empty), comparisons (single value).
 */
public class SubSelectExpression extends Expression {
    protected Expression baseExpression;
    protected ReportQuery subQuery;

    public SubSelectExpression() {
        super();
    }

    public SubSelectExpression(ReportQuery query, Expression baseExpression) {
        this();
        this.subQuery = query;
        this.baseExpression = baseExpression;
    }

    /**
     * INTERNAL:
     * Used in debug printing of this node.
     */
    public String descriptionOfNodeType() {
        return "SubSelect";
    }

    public Expression getBaseExpression() {
        return baseExpression;
    }

    /**
     * Return the builder from the defining expression.
     */
    public ExpressionBuilder getBuilder() {
        return getBaseExpression().getBuilder();
    }

    public ReportQuery getSubQuery() {
        return subQuery;
    }

    /**
     * INTERNAL:
     * For iterating using an inner class
     */
    public void iterateOn(ExpressionIterator iterator) {
        super.iterateOn(iterator);
        if (baseExpression != null) {
            baseExpression.iterateOn(iterator);
        }

        // For Flashback: It is now possible to create iterators that will span
        // the entire expression, even the where clause embedded in a subQuery.
        if (iterator.shouldIterateOverSubSelects()) {
            if (getSubQuery().getSelectionCriteria() != null) {
                getSubQuery().getSelectionCriteria().iterateOn(iterator);
            } else {
                getSubQuery().getExpressionBuilder().iterateOn(iterator);
            }
        }
    }

    /**
     * INTERNAL:
     * The subquery must be normalized with the knowledge of the outer statement for outer references and correct aliasing.
     * For CR#4223 it will now be normalized after the outer statement is, rather than
     * somewhere in the middle of the outer statement's normalize.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
        //has no effect but validateNode is here for consistency
        validateNode();
        // Defer normalization of this expression until later.
        normalizer.addSubSelectExpression(this);
        normalizer.getStatement().setRequiresAliases(true);
        return this;
    }

    /**
     * INTERNAL:
     * Normalize this expression now that the parent statment has been normalized.
     * For CR#4223
     */
    public Expression normalizeSubSelect(ExpressionNormalizer normalizer, Dictionary clonedExpressions) {
        // Anonymous subqueries: The following is to support sub-queries created
        // on the fly by OSQL Expressions isEmpty(), isNotEmpty(), size().
        if (!getSubQuery().isCallQuery() && (getSubQuery().getReferenceClass() == null)) {
            ReportQuery subQuery = getSubQuery();
            Expression criteria = subQuery.getSelectionCriteria();

            // The criteria should be of form builder.equal(exp), where exp belongs
            // to the parent statement and has already been normalized, hence it
            // knows its reference class.
            if (criteria instanceof LogicalExpression) {
                criteria = ((LogicalExpression)criteria).getFirstChild();
            }
            if (criteria instanceof RelationExpression) {
                Expression rightChild = ((RelationExpression)criteria).getSecondChild();
                if (rightChild instanceof QueryKeyExpression) {
                    subQuery.setReferenceClass(((QueryKeyExpression)rightChild).getDescriptor().getJavaClass());
                }
            }
        }

        //has no effect but validateNode is here for consistency
        validateNode();
        getSubQuery().prepareSubSelect(normalizer.getSession(), null, clonedExpressions);
        if (!getSubQuery().isCallQuery()) {
            SQLSelectStatement statement = (SQLSelectStatement)((StatementQueryMechanism)getSubQuery().getQueryMechanism()).getSQLStatement();

            // setRequiresAliases was already set for parent statement.
            statement.setRequiresAliases(true);
            statement.setParentStatement(normalizer.getStatement());
            statement.normalize(normalizer.getSession(), getSubQuery().getDescriptor(), clonedExpressions);
        }
        return this;
    }

    /**
     * The query must be cloned, and the sub-expression must be cloned using the same outer expression identity.
     */
    protected void postCopyIn(Dictionary alreadyDone) {
        super.postCopyIn(alreadyDone);
        setBaseExpression(getBaseExpression().copiedVersionFrom(alreadyDone));
        ReportQuery clonedQuery = (ReportQuery)getSubQuery().clone();
        if ((!clonedQuery.isCallQuery()) && (clonedQuery.getSelectionCriteria() != null)) {
            clonedQuery.setSelectionCriteria(getSubQuery().getSelectionCriteria().copiedVersionFrom(alreadyDone));

            // If we are building/cloning a selection criteria for a batch query, a little extra work
            // needs to be done (see bug 2812185).
            if (alreadyDone.get(alreadyDone) != null) {
                clonedQuery.copyReportItems(alreadyDone);
            }
        }
        setSubQuery(clonedQuery);
    }

    /**
     * Print the sub query to the printer.
     */
    protected void printCustomSQL(ExpressionSQLPrinter printer) {

        /*
         * modified for bug#2658466.  This fix ensures that Custom SQL sub-queries are translated
         * and have variables substituted with values correctly.
         */
        SQLCall call = (SQLCall)getSubQuery().getCall();
        call.translateCustomQuery();
        printer.getCall().getParameters().addAll(call.getParameters());
        printer.getCall().getParameterTypes().addAll(call.getParameterTypes());
        printer.printString(call.getCallString());
    }

    /**
     * Print the sub query to the printer.
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        ReportQuery query = getSubQuery();
        printer.printString("(");
        if (query.isCallQuery()) {
            printCustomSQL(printer);
        } else {
            SQLSelectStatement statement = (SQLSelectStatement)((ExpressionQueryMechanism)query.getQueryMechanism()).getSQLStatement();
            boolean isFirstElementPrinted = printer.isFirstElementPrinted();
            printer.setIsFirstElementPrinted(false);
            boolean requiresDistinct = printer.requiresDistinct();
            statement.printSQL(printer);
            printer.setIsFirstElementPrinted(isFirstElementPrinted);
            printer.setRequiresDistinct(requiresDistinct);
        }
        printer.printString(")");
    }

    /**
     * Should not rebuild as has its on expression builder.
     */
    public Expression rebuildOn(Expression newBase) {
        return this;
    }

    protected void setBaseExpression(Expression baseExpression) {
        this.baseExpression = baseExpression;
    }

    public void setSubQuery(ReportQuery subQuery) {
        this.subQuery = subQuery;
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(String.valueOf(getSubQuery()));
    }

    /**
     * INTERNAL:
     * Used in SQL printing.
     */
    public void writeSubexpressionsTo(BufferedWriter writer, int indent) throws IOException {
        if (getSubQuery().getSelectionCriteria() != null) {
            getSubQuery().getSelectionCriteria().toString(writer, indent);
        }
    }
}
