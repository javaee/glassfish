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

import java.util.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * This is used during the normalization process to allow for a single main expression traversal.
 */
public class ExpressionNormalizer {

    /** A new root expression can be made from joins being added to the original expression. */
    protected Expression additionalExpression;

    /** The statement being normalized. */
    protected SQLSelectStatement statement;

    /** Subselect expressions found in the course of normalization. */
    protected Vector subSelectExpressions;

    /** The session being normalized in. */
    protected AbstractSession session;

    public ExpressionNormalizer(SQLSelectStatement statement) {
        this.statement = statement;
    }

    public void addAdditionalExpression(Expression theExpression) {
        // This change puts a null check into every call, but is printing additional
        // expressions in a meaningfull order worth it?
        additionalExpression = (additionalExpression == null) ? theExpression : additionalExpression.and(theExpression);
    }

    /**
     * INTERNAL:
     * Remember this subselect so that it can be normalized after the enclosing
     * select statement is.
     */
    public void addSubSelectExpression(SubSelectExpression subSelectExpression) {
        if (subSelectExpressions == null) {
            subSelectExpressions = new Vector(1);
        }
        subSelectExpressions.add(subSelectExpression);
    }

    public Expression getAdditionalExpression() {
        return additionalExpression;
    }

    public AbstractSession getSession() {
        return session;
    }

    public SQLSelectStatement getStatement() {
        return statement;
    }

    /**
     * INTERNAL:
     * Were subselect expressions found while normalizing the selection criteria?
     * Assumes underlying collection is initialized on first add.
     */
    public boolean encounteredSubSelectExpressions() {
        return (subSelectExpressions != null);
    }

    /**
     * INTERNAL:
     * Normalize all subselect expressions found in the course of normalizing the
     * enclosing query.
     * This method allows one to completely normalize the parent statement first
     * (which should treat its sub selects as black boxes), and then normalize the
     * subselects (which require full knowledge of the enclosing statement).
     * This should make things clearer too,
     * Assumes encounteredSubSelectExpressions() true.
     * For CR#4223.
     */
    public void normalizeSubSelects(Dictionary clonedExpressions) {
        for (Enumeration enumtr = subSelectExpressions.elements(); enumtr.hasMoreElements();) {
            SubSelectExpression next = (SubSelectExpression)enumtr.nextElement();
            next.normalizeSubSelect(this, clonedExpressions);
        }
    }

    public void setAdditionalExpression(Expression additionalExpression) {
        this.additionalExpression = additionalExpression;
    }

    public void setSession(AbstractSession session) {
        this.session = session;
    }

    public void setStatement(SQLSelectStatement statement) {
        this.statement = statement;
    }
}
