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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;

import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;
import com.sun.persistence.runtime.sqlstore.impl.SQLPersistenceManagerFactory;
import com.sun.persistence.runtime.sqlstore.sql.select.SelectExecutor;
import com.sun.persistence.support.Transaction;

import com.sun.persistence.utility.I18NHelper;

/**
 * 
 * @author Dave Bristor
 */
public class EJBQLQueryImpl extends QueryImpl {
    
    /** I18N support. */
    private final static ResourceBundle msgs
        = I18NHelper.loadBundle(EJBQLQueryImpl.class);
    
    /** The compiled form of the query. */
    private EJBQLAST ast = null;
    
    /**
     * Create a query for the given String.
     * @param query
     * @param pm PersistenceManager for which the query is created.
     */
    EJBQLQueryImpl(String query, PersistenceManagerInternal pm) {
        super(query, pm);
    }
    
    /** Return a copy that shares the same query string and AST. */
    EJBQLQueryImpl(EJBQLQueryImpl q) {
        this(q.getQuery(), q.getPersistenceManager());
        this.ast = q.getAST();
    }    
    
    
    /* Methods from Query */
 
    /**
     * @see javax.persistence.Query#getResultList()
     */
    public List getResultList() {
        RuntimeMappingModel model = getModel();
        DBVendorType dbVendor = ((SQLPersistenceManagerFactory)
                pm.getPersistenceManagerFactory()).getDBVendorType();
        SQLCompilationMonitor scm = new SQLCompilationMonitor(model, dbVendor);
        QueryContext qc = new PersistenceQueryContext(model);
        EJBQLC3.getInstance().compile(this, qc, scm);
        flush();
        SelectExecutor se = scm.getExecutor();
        return se.execute(pm, this);
    }

    /**
     * @see javax.persistence.Query#getSingleResult()
     */
    public Object getSingleResult() {
        List result = getResultList();
        if (result == null || result.size() == 0) {
            throw new EntityNotFoundException(); // spec-required
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            assert result.size() > 1; // spec-required
            throw new NonUniqueResultException();
        }
    }

    /**
     * @see javax.persistence.Query#executeUpdate()
     */
    public int executeUpdate() {
        return 0;
    }

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.lang.Object)
     */
    public Query setParameter(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "XXX I18N-me setParameter: name must not be null");
        }
        if (isPositionalParamSet()) {
            throw new IllegalArgumentException(
                I18NHelper.getMessage(
                    msgs, "ERR_IllegalNamedParameter", name)); // NOI18                    
        }
        namedParams.put(name, value);
        return this;
    }

    /* 
     * Note: If the Date does not match the TemporalType (e.g. java.sql.Date,
     * TIMESTAMP), an exception can be thrown.  Not clear yet if we will do
     * so.
     */

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Date, javax.persistence.Query.TemporalType)
     */
    public Query setParameter(String name, Date date, TemporalType tt) {
        setParameter(name, new TemporalParameterImpl(date, tt));
        return this;
    }

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Calendar, javax.persistence.Query.TemporalType)
     */
    public Query setParameter(String name, Calendar calendar, TemporalType tt) {
        setParameter(name, new TemporalParameterImpl(calendar, tt));
        return this;
    }

    /* Methods from QueryInternal */
    
    /**
     * @return true
     */
    public boolean isEJBQLQuery() {
        return true;
    }
    
    /* Other implementation */
    

    protected EJBQLAST compile() {
//        if (ast == null) {
//            ast = cache.get(getQuery());
//            if (ast == null) {
//                ast = EJBQLC.instance().compile(getQuery(), queryContext);
//            }
//        }
        return ast;
    }
    
    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#load(StateManagerInternal)
     */
    public void load(StateManagerInternal sm) {
        // XXX TBD implement me
    }
    
    
    /* Implementation methods */
    EJBQLAST getAST() {
        return ast;
    }

    private RuntimeMappingModel getModel() {
        return ((SQLPersistenceManagerFactory)
                pm.getPersistenceManagerFactory()).getRuntimeMappingModel();
    }

    private void flush() {
        Transaction tx = pm.currentTransaction();
        // TODO: flag ignoreCache is not checked for now. Verify if we need to
        // flush for optimistic transaction

        if ((tx != null) && tx.isActive() && !tx.getOptimistic()  ) {
            pm.flush();
        }
    }


}
