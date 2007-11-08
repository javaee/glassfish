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
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.Query;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;

import com.sun.persistence.utility.I18NHelper;

/**
 * Represents an non-EJBQL query.
 *
 * @author Dave Bristor
 */
public class NativeQueryImpl extends QueryImpl {
    /**
     * I18N support.
     */
    private final static ResourceBundle msgs
        = I18NHelper.loadBundle(NativeQueryImpl.class);

    /** Class of the resulting instances. */
    private final Class resultClass;

    /** Name of the result set mapping. */
    private final String resultSetMapping;

    /**
     * Create a query for the given String.
     * @param query text of the query
     * @param pm PersistenceManager for which the query is created.
     */
    NativeQueryImpl(String query, PersistenceManagerInternal pm) {
        this(query, null, null, pm);
    }

    /**
     * Create a query for the given String.
     * @param query text of the query
     * @param resultClass class of the resulting instance
     * @param pm PersistenceManager for which the query is created.
     */
    NativeQueryImpl(String query, Class resultClass, PersistenceManagerInternal pm) {
        this(query, resultClass, null, pm);
    }

    /**
     * Create a query for the given String.
     * @param query text of the query.
     * @param resultSetMapping name of the result set mapping.
     * @param pm PersistenceManager for which the query is created.
     */
    NativeQueryImpl(String query, String resultSetMapping, PersistenceManagerInternal pm) {
        this(query, null, resultSetMapping, pm);
    }

    /**
     * Create a query for the given String.
     * @param query Text of the query
     * @param resultClass class of the resulting instance
     * @param pm PersistenceManager for which the query is created.
     * @param resultSetMapping name of the result set mapping.
     */
    private NativeQueryImpl(String query, Class resultClass, String resultSetMapping, PersistenceManagerInternal pm) {
        super(query, pm);
        this.resultClass = resultClass;
        this.resultSetMapping = resultSetMapping;
    }


    /* Methods from Query */

    /**
     * @see javax.persistence.Query#getResultList()
     */
    public List getResultList() {
        return null; // XXX TBD implement me
    }

    /**
     * @see javax.persistence.Query#getSingleResult()
     */
    public Object getSingleResult() {
        return null; // XXX TBD implement me
    }

    /**
     * @see javax.persistence.Query#executeUpdate()
     */
    public int executeUpdate() {
        return 0; // XXX TBD implement me, or not: what is correct?
    }

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.lang.Object)
     */
    public Query setParameter(String name, Object value) {
        invalidParameter(name);
        return null; // Silence compiler
    }

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Date, javax.persistence.Query.TemporalType)
     */
    public Query setParameter(String name, Date date, TemporalType tt) {
        invalidParameter(name);
        return null; // Silence compiler
    }

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Calendar, javax.persistence.Query.TemporalType)
     */
    public Query setParameter(String name, Calendar calendar, TemporalType tt) {
        invalidParameter(name);
        return null; // Silence compiler
    }

        
    /* Methods from QueryInternal */

    /**
     * @return false
     */
    public boolean isEJBQLQuery() {
        return false;
    }

    private void invalidParameter(String name) {
        throw new IllegalArgumentException(
            I18NHelper.getMessage(
                    msgs, "ERR_InvalidNamedParameterForNativeQuery", name)); // NOI18N
    }
}
