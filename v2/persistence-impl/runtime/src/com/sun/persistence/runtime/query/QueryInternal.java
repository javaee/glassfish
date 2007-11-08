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


package com.sun.persistence.runtime.query;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;

/**
 * Provides access to data associated with a query that can be set via the
 * spec-provided methods setParameter, etc.
 * @author Dave Bristor
 */
public interface QueryInternal extends Query {
    
    /* 
     * Methods which are accessors corresponding to the setters on javax.persistence.query.
     */
    
    /**
     * @return maxResults as set by setMaxResults, or -1 if that was never
     * invoked on this query.
     */
    public int getMaxResults();
    
    /** 
     * @return firstResult as set by setFirstResult, or -1 if that was never
     * invoked on this query.
     */
    public int getFirstResult();
    
    /**
     * @return an Object corresponding to the given hintName as set by
     * setHint, or null if no such hint was provided.
     */
    public Object getHint(String hintName);

    /**
     * @return true if a hint was set with the given hintName, false
     * otherwise.  This allows clients to distinguish between hints that were
     * set to null and those that were not set.
     */
    public boolean isHint(String hintName);
    
    /**
     * @return an Object corresponding to the given name, or null if no such
     * name parameter was set on this query.  If the parameter was set
     * via one of the setParameter methods that takes a TemporalType, then
     * the returned value will be a TemporalParameter, otherwise it will be
     * the client-provided object.
     */
    public Object getParameter(String name);
    
    /**
     * @return an Object corresponding to the given position, or null if no
     * such positional parameter was set on this query.  If the parameter
     * was set  via one of the setParameter methods that takes a
     * TemporalType, then the returned value will be a TemporalParameter,
     * otherwise it will be the client-provided object.
     */
    public Object getParameter(int position);
    
    /**
     * @return the JavaType corresponding to the parameter, or null if no such
     * parameter was set on this query.  If the parameter was set
     * via one of the setParameter methods that takes a TemporalType, then
     * the returned value will be a TemporalParameter, otherwise it will be
     * the client-provided object.
     * The name can be either for a positional or named parameter, as
     * distinguished by the first character in the name: ? means positional,
     * : means named.
     */
    public JavaType getParameterType(String name, JavaModel jm);
    
    /*
     * Methods which are for appserver-internal consumption
     */
    
    /**
     * @return the FlushModeType as set by setFlushMode, or the default
     * FlushMode if never set on this query.
     */
    public FlushModeType getFlushMode();
    
    /**
     * @return the query string which this instance represents.
     */
    public String getQuery();
    
    /**
     * @return true if this query is written in EJBQL, false otherwise.
     */
    public boolean isEJBQLQuery();
    
    /**
     * @return the PersistenceManagerInternal with which the query was created.
     */
    public PersistenceManagerInternal getPersistenceManager();
    
    /**
     * Loads the instance specified by the given StateManager with values 
     * retrieved by executing this query. 
     * @param sm StateManager which specifies the instance to be given values.
     */
    public void load(StateManagerInternal sm);
}
