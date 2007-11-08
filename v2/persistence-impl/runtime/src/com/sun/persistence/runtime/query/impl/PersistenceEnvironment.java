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

import java.util.ResourceBundle;

import com.sun.persistence.runtime.query.ParameterSupport;
import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.utility.I18NHelper;

/**
 * An implementation of Environment for compilation of ejbql in an
 * EJB 3.x environment.
 *
 * @author Dave Bristor
 */
class PersistenceEnvironment extends EnvironmentImpl {
    private final QueryContext queryContext;
    
    private final ParameterSupport paramSupport;

    /**
     * I18N support.
     */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
            PersistenceEnvironment.class);
    
    PersistenceEnvironment(QueryContext queryContext, ParameterSupport paramSupport) {
        this.queryContext = queryContext;
        this.paramSupport = paramSupport;
    }
    
    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#checkQueryResultType(java.lang.Object)
     */
    public void checkQueryResultType(Object selectClauseTypeInfo) {
        // empty
    }
    
    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#requiresDistinct()
     */
    public boolean requiresDistinct() {
        // basically empty
        return false;
    }
    
    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#setAggregate(boolean)
     */
    public void setAggregate(boolean aggregate) {
        // empty
    }

    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#analyseParameter(EJBQLASTImpl, Object)
     */
    public Object analyseParameter(EJBQLASTImpl paramAST, Object type) {
        Object rc = type;
        if (semanticHelper.isInputParameter(paramAST)) {
            String ptext = paramAST.getText();
            
            // Check that query does not mix named and positional parameters.
            if (!paramSupport.isSuitableParameter(ptext)) {
                ErrorMsg.error(paramAST.getLine(), paramAST.getColumn(),
                    I18NHelper.getMessage(msgs, 
                        paramSupport.isNamedParameter(ptext)
                        ? "ERR_IllegalNamedParameter"
                            : "ERR_IllegalPositionalParameter",
                            ptext));
            }
            
            Object paramType = paramAST.getTypeInfo();
            if (queryContext.isUnknownType(paramType)) {
                // parameter doesn't yet have a type, so assign it the given type
                paramAST.setTypeInfo(type);
                paramSupport.setParameterType(ptext, type);
                
            } else if (paramType != type) {
                // paramType and given type don't match => error
                ErrorMsg.error(paramAST.getLine(), paramAST.getColumn(),
                    I18NHelper.getMessage(msgs, "FIXME")); // XXX FIME need real message 
                rc = queryContext.getErrorType();
            }
        }
        return rc;        
    }


    /**
     * @see com.sun.persistence.runtime.query.impl.Environment#getParameterType(String)
     */
    public Object getParameterType(String ejbqlParamDecl) {
        Object rc = paramSupport.getParameterType(ejbqlParamDecl);
        if (rc == null) {
            rc = queryContext.getUnknownType();
        }
        return rc;
    }
}
