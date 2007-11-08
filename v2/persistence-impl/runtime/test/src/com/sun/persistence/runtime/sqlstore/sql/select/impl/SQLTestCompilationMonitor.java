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


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.persistence.runtime.query.CompilationMonitor;
import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.query.impl.EJBQLTreeWalker;
import com.sun.persistence.runtime.sqlstore.sql.select.impl.SQLVisitor;
import com.sun.persistence.runtime.sqlstore.sql.select.SelectExecutor;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;
import com.sun.persistence.runtime.query.impl.EJBQLASTFactory;
import com.sun.persistence.runtime.query.impl.EJBQLASTImpl;
import antlr.RecognitionException;

public class SQLTestCompilationMonitor implements CompilationMonitor {
    private RuntimeMappingModel model;
    private DBVendorType dbVendor;
    private String statementText;

    /**
     */
    public SQLTestCompilationMonitor(RuntimeMappingModel model, 
            DBVendorType dbVendor) {
        this.model = model;
        this.dbVendor = dbVendor;
    }

    /* Methods from CompilationMonitor */

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#preSyntax(String)
     */
    public void preSyntax(String qstr) {
    }
    
    /**
     */
    public void postSyntax(String qstr, EJBQLAST ast) {
    }

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#preSemantic(String)
     */
    public void preSemantic(String qstr) {
    }

    /**
     */
    public void postSemantic(String qstr, EJBQLAST ast) {
        test(ast, "semantic");
    }
    
    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#preOptimize(String)
     */
    public void preOptimize(String qstr) {
    }
    
    /**
     */
    public void postOptimize(String qstr, EJBQLAST ast) {
        test(ast, "optimize");
    }

    /* TestCompilationMonitor implementation. */

    /** Test against a semantically annotated tree. */
    private void test(EJBQLAST ast, String pass) {
        SQLVisitor v = new SQLVisitor(model, dbVendor);

        EJBQLTreeWalker tw = new EJBQLTreeWalker();
        tw.init(v);
        tw.setASTFactory(EJBQLASTFactory.getInstance());
        SelectExecutor sqe = null;
        try {
            tw.query((EJBQLASTImpl) ast);
        } catch (RecognitionException e) {
            e.printStackTrace();
        }
        SelectExecutorImpl executor = (SelectExecutorImpl)v.getSelectExecutor();
        statementText = executor.getStatementText();
        
    }
    
    /** get sql statement text */
    public String getStatementText() {
        return statementText;
    }
}
