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

import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;
import com.sun.persistence.runtime.sqlstore.sql.select.impl.SQLVisitor;
import com.sun.persistence.runtime.sqlstore.sql.select.SelectExecutor;
import com.sun.persistence.support.JDOFatalInternalException;

import antlr.RecognitionException;

/**
 * This CompilationMonitor enables creating an executor that can execute the
 * given query.
 * @author Mitesh Meswani
 */
public class SQLCompilationMonitor extends CompilationMonitorImpl {
    private RuntimeMappingModel model;
    private SelectExecutor sqe;
    private DBVendorType dbVendor;

    public SQLCompilationMonitor(RuntimeMappingModel model, DBVendorType dbVendor) {
        assert model != null : "The model instance passed should never be null";
        this.model = model;
        this.dbVendor = dbVendor;
    }

    /**
     * @inheritDoc
     */
    public void postSemantic(String qstr, EJBQLAST ast) {
        //XXX TODO: Move this code to postOptimize once it is implemented
        sqe = null;
        // XXX TODO: Must change the null in following call to actual DBVendorType once
        // Marina's code for SQLSTorePMF is checked in
        SQLVisitor v = new SQLVisitor(model, dbVendor);
        EJBQLTreeWalker tw = new EJBQLTreeWalker();
        tw.init(v);
        //XXX TODO: Check with Dave why can't this be done in EJBQLTreeWalker.init()
        tw.setASTFactory(EJBQLASTFactory.getInstance());
        try {
            tw.query((EJBQLASTImpl) ast);
            sqe = (SelectExecutor)v.getSelectExecutor();
        } catch (RecognitionException e) {
            //XXX TODO Need to get the message from a bundle
            throw new JDOFatalInternalException("Fatal interal error while "
                    + "walking the query tree to generate SQL", e);
        }
    }

    /**
     * The executor that can execute this query
     * @return The executor that can execute this query.
     */
    public SelectExecutor getExecutor() {
        assert sqe != null : "No valid executor available. " //NOI18N
                + "This is because either postOptimize was not called for this query " //NOI18N
                + "or an exception was thrown from postOptimize"; //NOI18N
        return sqe;
    }

}
