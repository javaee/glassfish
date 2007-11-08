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

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;

import com.sun.persistence.runtime.query.ParameterSupport;
import com.sun.persistence.runtime.query.QueryInternal;

import com.sun.persistence.utility.I18NHelper;

/**
 * Represents an in-memory query, as well as parameters, etc. which the user
 * can set on the query.
 *
 * @author Dave Bristor
 */
// Tested by EJBQLQueryImplTest
abstract class QueryImpl implements Query, QueryInternal {
    
    /** I18N support. */
    private final static ResourceBundle msgs
        = I18NHelper.loadBundle(QueryImpl.class);    

    /** String form of query provided by client. */
    private final String qstr;

    /** Max number of results.  -1 means "never set by client" */
    private  int maxResults = -1;

    /** First result.  -1 means "never set by client" */
    private int firstResult = -1;

    /** How/when a flush will be done with respect to query execution. " */
    private FlushModeType flushMode = FlushModeType.AUTO;

    /** Hints given by client." */
    private final Map<String, Object> hints = new HashMap<String, Object>();

    /** Positional parameters set by client */
    private Object positionalParams[] = null;

    /** Named parameters set by client */
    protected final Map<String, Object> namedParams =
        new HashMap<String, Object>();

    /** Once set, do not change. */
    private ParameterSupport paramSupport;
    
    /** PersistenceManager in whose context this query is built and will run. */
    protected final PersistenceManagerInternal pm;

    /** Create a query that executes the given query string. 
     * @param pm PersistenceManager for which the query is created.
     */
    protected QueryImpl(String qstr, PersistenceManagerInternal pm) {
        assert qstr != null
            : "QueryImpl with null query"; // NOI18N
        assert pm != null
            : "QueryImpl with null pm"; // NOI18N
        this.qstr = qstr;
        this.pm = pm;
    }


    /* Methods from Query */

    /**
     * @see javax.persistence.Query#getResultList()
     */
    abstract public List getResultList();

    /**
     * @see javax.persistence.Query#getSingleResult()
     */
    abstract public Object getSingleResult();

    /**
     * @see javax.persistence.Query#executeUpdate()
     */
    abstract public int executeUpdate();

    /**
     * @see javax.persistence.Query#setMaxResults(int)
     */
    public Query setMaxResults(int maxResults) {
        if (maxResults < 0) {
            throw new IllegalArgumentException(
                "XXX I18N-me setMaxResults: value given must be > 0 (given "
                + maxResults + ").");
        }
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @see javax.persistence.Query#setFirstResult(int)
     */
    public Query setFirstResult(int firstResult) {
        if (firstResult < 0) {
            throw new IllegalArgumentException(
                "XXX I18N-me setFirstResult: value given must be > 0 (given "
                + firstResult + ").");
        }
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @see javax.persistence.Query#setHint(java.lang.String, java.lang.Object)
     */
    public Query setHint(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "XXX I18N-me setHint: name must not be null");
        }
        hints.put(name, value);
        return this;
    }

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.lang.Object)
     */
    abstract public Query setParameter(String name, Object value);

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Date, javax.persistence.Query.TemporalType)
     */
    abstract public Query setParameter(String name, Date date, TemporalType tt);

    /**
     * @see javax.persistence.Query#setParameter(java.lang.String, java.util.Calendar, javax.persistence.Query.TemporalType)
     */
    abstract public Query setParameter(String name, Calendar calendar, TemporalType tt);

    /**
     * @see javax.persistence.Query#setParameter(int, java.lang.Object)
     */
    public Query setParameter(int pos, Object value) {
        if (pos < 1) {
            throw new IllegalArgumentException(
                "XXX I18N-me setParameter(int): pos must be > 0 (given "
                + pos + ").");
        }  
        
        if (!namedParams.isEmpty()) {
            throw new IllegalArgumentException(
                I18NHelper.getMessage(
                    msgs, "ERR_IllegalPositionalParameter", pos)); // NOI18N
        }        
        
        // Make sure the given paramater fits in the array
        if (positionalParams == null) {
            positionalParams = new Object[pos];
            
        } else if (pos > positionalParams.length) {
            Object p[] = new Object[pos];
            for (int i = 0; i < positionalParams.length; i++) {
                p[i] = positionalParams[i];
            }
            positionalParams = p;
        }

        pos -= 1; // Adjust EJBQL values -> Java values
        positionalParams[pos] = value;
        return this;
    }

    /**
     * @see javax.persistence.Query#setParameter(int, java.util.Date, javax.persistence.Query.TemporalType)
     */
    public Query setParameter(int pos, Date date, TemporalType tt) {
        setParameter(pos, new TemporalParameterImpl(date, tt));
        return this;
    }

    /**
     * @see javax.persistence.Query#setParameter(int, java.util.Calendar, javax.persistence.Query.TemporalType)
     */
    public Query setParameter(int pos, Calendar calendar, TemporalType tt) {
        setParameter(pos, new TemporalParameterImpl(calendar, tt));
        return this;
    }

    /**
     * @see javax.persistence.Query#setFlushMode(javax.persistence.FlushModeType)
     */
    public Query setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return this;
    }


    /* Methods from QueryInternal */

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#getMaxResults()
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#getFirstResult()
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#getHint(java.lang.String)
     */
    public Object getHint(String name) {
        return hints.get(name);
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#isHint(java.lang.String)
     */
    public boolean isHint(String name) {
        return hints.containsKey(name);
    }

    /**
     * If the name starts with ':', strip that off and recurse.  If the name
     * starts with '?', strip that off, treat the rest of the string as
     * an int, and use that value as a parameter index to get a value.  Else
     * try to use the name as a parameter name in its own right.
     * @see com.sun.persistence.runtime.query.QueryInternal#getParameter(java.lang.String)
     */
    public Object getParameter(String name) {
        Object rc = null;
        if (name.startsWith(":")) {
            rc = namedParams.get(name.substring(1));
        } else if (name.startsWith("?")) {
            rc = getParameter(new Integer(name.substring(1)).intValue());
        } else {
            rc = namedParams.get(name);
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#getParameter(int)
     */
    public Object getParameter(int pos) {
        if (pos < 1) {
            throw new IllegalArgumentException(
                "XXX I18N-me getParameter(int): pos must be > 0 (given "
                + pos + ").");
        } else if (positionalParams == null) {
            throw new IllegalArgumentException(
                "XXX I18N-me getParameter(int): no positional parameters set on this query");
        } else if (pos > positionalParams.length) {
            throw new IllegalArgumentException(
                "XXX I18N-me getParameter(int): pos must be <= "
                + positionalParams.length + "(given "
                    + pos + ").");
        }
        return positionalParams[pos-1];// Adjust EJBQL values -> Java values
    }

    /**
     *  @see com.sun.persistence.runtime.query.QueryInternal#getParameterType(String, JavaModel)
     */
    public JavaType getParameterType(String name, JavaModel model) {
        JavaType rc = null;
        Object value = null;
        if (name.startsWith(":")) {
            value = getParameter(name.substring(1));
        } else if (name.startsWith("?")) {
            value = getParameter(new Integer(name.substring(1)).intValue());
        } else {
            throw new IllegalArgumentException("XXX should not happen");
            // XXX The parser should disallow getting to this state.
        }
        if (value != null) {
            rc = model.getJavaType(value.getClass());
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#getFlushMode()
     */
    public FlushModeType getFlushMode() {
        return flushMode;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#getQuery()
     */
     public String getQuery() {
         return qstr;
     }

    abstract public boolean isEJBQLQuery();
    
    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#getPersistenceManager()
     */
    public PersistenceManagerInternal getPersistenceManager() {
        return pm;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryInternal#load(StateManagerInternal)
     */
    public void load(StateManagerInternal sm) {
        // empty
    }
    
    /*
     * Methods for use by query.impl package only.
     */

    /**
     * Associates a ParameterSupport instance with this query.  Once set, it
     * should not be changed.
     * @param paramSupport
     */
    public void setParameterSupport(ParameterSupport paramSupport) {
        this.paramSupport = paramSupport;
    }
    
    /**
     * @return true if any positional parameters are set, false otherwise.
     */
    protected boolean isPositionalParamSet() {
        return positionalParams != null;
    }
}
