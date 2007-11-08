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
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * Used for wrapping literal values.
 */
public class LiteralExpression extends Expression {
    protected String value;
    protected Expression localBase;

    public LiteralExpression() {
        super();
    }

    public LiteralExpression(String newValue, Expression baseExpression) {
        super();
        value = newValue;
        localBase = baseExpression;
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Literal";
    }

    /**
     * Return the expression builder which is the ultimate base of this expression, or
     * null if there isn't one (shouldn't happen if we start from a root)
     */
    public ExpressionBuilder getBuilder() {
        return getLocalBase().getBuilder();
    }

    protected Expression getLocalBase() {
        return localBase;
    }

    public String getValue() {
        return value;
    }

    public boolean isLiteralExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * Used for cloning.
     */
    protected void postCopyIn(Dictionary alreadyDone) {
        super.postCopyIn(alreadyDone);
        localBase = localBase.copiedVersionFrom(alreadyDone);
    }

    /**
     * INTERNAL:
     * Print SQL onto the stream, using the ExpressionPrinter for context
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        printer.printString(value);
    }

    /**
     * INTERNAL:
     * Print SQL, this is called from functions, so must not be converted through the mapping.
     */
    public void printSQLWithoutConversion(ExpressionSQLPrinter printer) {
        printSQL(printer);
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        Expression result = (LiteralExpression)clone();
        result.setLocalBase(getLocalBase().rebuildOn(newBase));
        return result;
    }

    public void setLocalBase(Expression e) {
        localBase = e;
    }

    /**
     * INTERNAL:
     * Rebuild myself against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist
     * See the comment there for more details"
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        return (Expression)this.clone();
    }

    /**
     * INTERNAL:
     * Return the value for in memory comparison.
     * This is only valid for valueable expressions.
     */
    public Object valueFromObject(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        return getLocalBase().getFieldValue(getValue());
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(getValue().toString());
    }
}
