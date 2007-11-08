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
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.databaseaccess.*;

/**
 * Abstract class for expression that have exactly two children, such as and/or and relations.
 */
public abstract class CompoundExpression extends Expression {
    protected ExpressionOperator operator;
    protected transient ExpressionOperator platformOperator;
    protected Expression firstChild;
    protected Expression secondChild;

    public CompoundExpression() {
        super();
    }

    /**
     * INTERNAL:
     * Find the alias for a given table from the first or second child in the additionalOuterJoinCriteria
     */
    public DatabaseTable aliasForTable(DatabaseTable table) {
        DatabaseTable alias = null;
        if (getFirstChild() != null) {
            alias = getFirstChild().aliasForTable(table);
        }

        if ((alias == null) && (getSecondChild() != null)) {
            alias = getSecondChild().aliasForTable(table);
        }

        return alias;
    }

    /**
     * INTERNAL:
     */
    public Expression create(Expression base, Object singleArgument, ExpressionOperator operator) {
        setFirstChild(base);
        Expression argument = Expression.from(singleArgument, base);
        setSecondChild(argument);
        setOperator(operator);
        return this;
    }

    /**
     * INTERNAL:
     */
    public Expression create(Expression base, Vector arguments, ExpressionOperator operator) {
        setFirstChild(base);
        if (!arguments.isEmpty()) {
            setSecondChild((Expression)arguments.firstElement());
        }
        setOperator(operator);
        return this;
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Compound Expression";
    }

    /**
     * Return the expression builder which is the ultimate base of this expression, or
     * null if there isn't one (shouldn't happen if we start from a root)
     */
    public ExpressionBuilder getBuilder() {
        ExpressionBuilder builder = getFirstChild().getBuilder();
        if (builder == null) {
            return getSecondChild().getBuilder();
        } else {
            return builder;
        }
    }

    public Expression getFirstChild() {
        return firstChild;
    }

    public ExpressionOperator getOperator() {
        return operator;
    }

    public ExpressionOperator getPlatformOperator(DatabasePlatform platform) {
        if (platformOperator == null) {
            initializePlatformOperator(platform);
        }
        return platformOperator;
    }

    public Expression getSecondChild() {
        return secondChild;
    }

    /**
     * INTERNAL:
     */
    public void initializePlatformOperator(DatabasePlatform platform) {
        if (getOperator().isComplete()) {
            platformOperator = getOperator();
            return;
        }
        platformOperator = platform.getOperator(getOperator().getSelector());
        if (platformOperator == null) {
            throw QueryException.invalidOperator(getOperator().toString());
        }
    }

    public boolean isCompoundExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * For iterating using an inner class
     */
    public void iterateOn(ExpressionIterator iterator) {
        super.iterateOn(iterator);
        if (getFirstChild() != null) {
            getFirstChild().iterateOn(iterator);
        }
        if (getSecondChild() != null) {
            getSecondChild().iterateOn(iterator);
        }
    }

    /**
     * INTERNAL:
     * Normalize into a structure that is printable.
     * Also compute printing information such as outer joins.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
        validateNode();
        if (getFirstChild() != null) {
            //let's make sure a session is available in the case of a parallel expression
            ExpressionBuilder builder = getFirstChild().getBuilder();
            if (builder != null){
                builder.setSession(normalizer.getSession().getRootSession(null));
            }
            setFirstChild(getFirstChild().normalize(normalizer));
        }
        if (getSecondChild() != null) {
            //let's make sure a session is available in the case of a parallel expression
             ExpressionBuilder builder = getSecondChild().getBuilder();
             if (builder != null){
                 builder.setSession(normalizer.getSession().getRootSession(null));
             }
            setSecondChild(getSecondChild().normalize(normalizer));
        }

        // For CR2456, it is now possible for normalize to remove redundant
        // conditions from the where clause.
        if (getFirstChild() == null) {
            return getSecondChild();
        } else if (getSecondChild() == null) {
            return getFirstChild();
        }
        return this;
    }
    
    /**
     * Do any required validation for this node. Throw an exception if it's incorrect.
     * Ensure that both sides are not data expressions.
     */
    public void validateNode() {
        if (getFirstChild() != null) {
            if (getFirstChild().isDataExpression() || getFirstChild().isConstantExpression()) {
                throw QueryException.invalidExpression(this);
            }
        }
        if (getSecondChild() != null) {
            if (getSecondChild().isDataExpression() || getSecondChild().isConstantExpression()) {
                throw QueryException.invalidExpression(this);
            }
        }
    }

    /**
     * INTERNAL:
     * Used for cloning.
     */
    protected void postCopyIn(Dictionary alreadyDone) {
        super.postCopyIn(alreadyDone);
        if (getFirstChild() != null) {
            setFirstChild(getFirstChild().copiedVersionFrom(alreadyDone));
        }
        if (getSecondChild() != null) {
            setSecondChild(getSecondChild().copiedVersionFrom(alreadyDone));
        }
    }

    /**
     * INTERNAL:
     * Print SQL
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        ExpressionOperator realOperator = getPlatformOperator(printer.getPlatform());
        printer.printString("(");
        realOperator.printDuo(getFirstChild(), getSecondChild(), printer);
        printer.printString(")");
    }

    /**
     * INTERNAL:
     * Print java for project class generation
     */
    public void printJava(ExpressionJavaPrinter printer) {
        ExpressionOperator realOperator = getPlatformOperator(printer.getPlatform());
        realOperator.printJavaDuo(getFirstChild(), getSecondChild(), printer);
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        Vector arguments;

        Expression first = getFirstChild().rebuildOn(newBase);
        if (getSecondChild() == null) {
            arguments = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(0);
        } else {
            arguments = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
            arguments.addElement(getSecondChild().rebuildOn(newBase));
        }
        return first.performOperator(getOperator(), arguments);
    }

    protected void setFirstChild(Expression firstChild) {
        this.firstChild = firstChild;
    }

    public void setOperator(ExpressionOperator newOperator) {
        operator = newOperator;
    }

    protected void setSecondChild(Expression secondChild) {
        this.secondChild = secondChild;
    }

    /**
     * INTRENAL:
     * Used to change an expression off of one base to an expression off of a different base.
     * i.e. expression on address to an expression on an employee's address.
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        Vector arguments;

        if (getSecondChild() == null) {
            arguments = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(0);
        } else {
            arguments = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
            arguments.addElement(getSecondChild().twistedForBaseAndContext(newBase, context));
        }

        Expression first = getFirstChild().twistedForBaseAndContext(newBase, context);
        return first.performOperator(getOperator(), arguments);
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(operator.toString());
    }

    /**
     * INTERNAL:
     * Used for toString for debugging only.
     */
    public void writeSubexpressionsTo(BufferedWriter writer, int indent) throws IOException {
        if (getFirstChild() != null) {
            getFirstChild().toString(writer, indent);
        }
        if (getSecondChild() != null) {
            getSecondChild().toString(writer, indent);
        }
    }
}
