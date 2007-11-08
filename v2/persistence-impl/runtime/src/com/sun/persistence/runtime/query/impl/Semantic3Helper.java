/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.persistence.runtime.query.impl;

import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.utility.I18NHelper;

/**
 * Helper class to support semantic analysis.
 * 
 * @author Michael Bouschen
 * @author Dave Bristor
 */
public class Semantic3Helper extends SemanticHelper {

    /** */
    protected Semantic3Helper(QueryContext queryContext, Environment env) {
        super(queryContext, env);
    }

    /** 
     * Returns <code>true</code> if ast denotes a input parameter access.
     */
    public boolean isInputParameter(EJBQLASTImpl ast)
    {
        int tokenType = ast.getType();
        return tokenType == EJBQL3TokenTypes.POSITIONAL_PARAMETER ||
            tokenType == EJBQL3TokenTypes.NAMED_PARAMETER;
    }

    /**
     * Returns <code>true</code> if trimDef is a single character and
     * expr denotes a input parameter access.
     */
    public Object analyseTrimFunction(EJBQLASTImpl trimDef, EJBQLASTImpl expr) {
        Object rc = queryContext.getErrorType();
        Object exprType = expr.getTypeInfo();

        if (!analyseStringExpr(expr)) {
            ErrorMsg.error(expr.getLine(), expr.getColumn(), 
                I18NHelper.getMessage(
                    msgs, "ERR_StringExprExpected", // NOI18N
                    queryContext.getTypeName(exprType)));
        } else {
            EJBQLASTImpl trimCharNode = (EJBQLASTImpl) trimDef.getNextSibling();
            String c = trimCharNode.getText();
            if (!isSingleCharacterStringLiteral(c)) {
                ErrorMsg.error(expr.getLine(), expr.getColumn(), 
                    I18NHelper.getMessage(
                        msgs, "ERR_InvalidTrimChar", c)); // NOI18N
            } else {
                rc = queryContext.getStringType();
            }
        }
        return rc;
    }
}
