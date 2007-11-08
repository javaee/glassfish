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

import javax.persistence.Query;

import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOCollection;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.persistence.runtime.query.QueryInternal;

/**
 * Create Query instances.
 *
 * @author Dave Bristor
 */
// XXX TBD: Caching: need to locate cache; lookup, clone if found in cache.
public class QueryFactory {

    private static final QueryFactory instance = new QueryFactory();

    private final Monitor monitor;

    private QueryFactory() {
        this(new MonitorImpl());
    }

    QueryFactory(Monitor monitor) {
        assert monitor != null
            : "QueryFactory constructor: Given monitor must not be null"; // NOI18N
        this.monitor = monitor;
    }

    /** @return the sole instance of a QueryFactory. */
    public static QueryFactory getInstance() {
        return instance;
    }

    /**
     * Creates a new Query.
     * @param query the query text.
     * @param pm PersistenceManager for which the query is created.
     * @return the in-memory representation of the query.
     */
    public Query createQuery(String query, PersistenceManagerInternal pm) {
        return getQuery(query, pm);
    }

    /**
     * Creates a new Query.
     * @param query the query text.
     * @param pm PersistenceManager for which the query is created.
     * @return the in-memory representation of the query.
     */
    public Query createNativeQuery(String query, PersistenceManagerInternal pm) {
        return new NativeQueryImpl(query, pm);
    }

    /**
     * Creates a new Query.
     * @param query the query text.
     * @param resultClass Class of the resulting instance.
     * @param pm PersistenceManager
     * @return the in-memory representation of the query.
     */
    public Query createNativeQuery(String query, Class resultClass, PersistenceManagerInternal pm) {
        return new NativeQueryImpl(query, resultClass, pm);
    }

    /**
     * Creates a new Query.
     * @param query the query text.
     * @param pm PersistenceManager for which the query is created.
     * @return the in-memory representation of the query.
     */
    public Query createNativeQuery(String query, String resultSetMapping, PersistenceManagerInternal pm) {
        return new NativeQueryImpl(query, resultSetMapping, pm);
    }

    /**
     * Creates a new Query.
     * @param name name of the query that is looked up in the XXX TBD named query
     * @param pm PersistenceManager for which the query is created.
     * @return the in-memory representation of the query.
     */
    public Query createNamedQuery(String name, PersistenceManagerInternal pm) {
        throw new UnsupportedOperationException(
            "not implemented yet: no way to lookup query name");
    }

    /**
     * Returns a query appropriate for the given class and field numbers.  If
     * fieldNums is null or has length == 0, a finder will be returned.  If
     * fieldNums has a single element and it is a relationship, then a 
     * navigator will be returned.  Otherwise, a loader for all the fields
     * in fieldNums will be returned.
     * @see #getFinder(JDOClass, PersistenceManagerInternal)
     * @see #getNavigator(JDOClass, int, PersistenceManagerInternal)
     * @see #getLoader(JDOClass, int[], PersistenceManagerInternal)
     * @param jdoClass JDOClass of the element to be found, navigated from,
     * or loaded.
     * @param fieldNums Indicates which fields are to be loaded; if null or
     * empty then the query will be for loading an instance of the given class.
     * @param pm PersistenceManagerInternal context for creating the query.
     * @return A query suitable for parameter binding and execution.
     */
    public QueryInternal getQuery(
            JDOClass jdoClass, int fieldNums[], PersistenceManagerInternal pm) {
        QueryImpl rc = null;
        if (fieldNums == null || fieldNums.length == 0) {
            rc = getFinder(jdoClass, pm);
        } else {
            if (fieldNums.length == 1) {
                JDOField f = jdoClass.getField(fieldNums[0]);
                if (f.isRelationship()) {
                    rc = getNavigator(jdoClass, fieldNums[0], pm);
                } else {
                    rc = getLoader(jdoClass, fieldNums, pm);
                }
            } else {
                rc = getLoader(jdoClass, fieldNums, pm);
            }
        }
        return rc;
    }
    
    
    /* Private implementation. */
    
    /**
     * Creates a query that finds an instance of the given class
     * with the given primary key.  For example,
     * <pre>
     * select e from Employee e where e.empid = ?1
     * </pre>
     * @param jdoClass JDOClass of the instance to be found.
     * @param pm PersistenceManager for which the query is created.
     * @return the in-memory representation of the query.
     */
    private QueryImpl getFinder(JDOClass jdoClass, PersistenceManagerInternal pm) {
        QueryImpl rc = null;

        // XXX TBD Look up this finder in a cache.  That is, before even
        // creating EJBQL, look in a cache with the Class c as a key
        // for a Query.
        
        // Create the EJBQL query string
        StringBuffer qstr = new StringBuffer("select object (x) from "); // NOI18N
        qstr.append(jdoClass.getShortName()).append(" x where "); // NOI18N

        // Add PK fields
        JDOField[] pkFields = jdoClass.getPrimaryKeyFields();
        for (int i = 0; i < pkFields.length; i++) {
            if (i > 0) {
                qstr.append(" and "); // NOI18N
            }
            qstr.append("x.").append(pkFields[i].getName()); // NOI18N
            qstr.append(" = ?").append(Integer.toString(i + 1)); // NOI18N
        }

        rc = getQuery(qstr.toString(), pm);

        monitor.postGetFinder(jdoClass, rc);

        return rc;
    }

    /**
     * Creates a query that loads the specified field of the instance
     * identified by the class and primary key.  For example,
     * <pre>
     * select e.picture from Employee e where e = ?1
     * </pre>
     * @param jdoClass JDOClass of the instance into which the field's value is loaded.
     * @param fieldNums numbers of the fields to be loaded.
     * @param pm PersistenceManager for which the query is created.
     * @return the in-memory representation of the query.
     */
    private QueryImpl getLoader(
            JDOClass jdoClass, int fieldNums[], PersistenceManagerInternal pm) {

    	// XXX TBD Possibly cache Loaders

        QueryImpl rc = null;

        String rangeVar = "rv"; // NOI18N
        
        // "select rv.<f1> [, rv.<f2>]+"
        StringBuffer qstr = new StringBuffer("select "); // NOI18N
        for (int i = 0; i < fieldNums.length; i++) {
        	if (i > 0) {
        		qstr.append(", "); // NOI18N
        	}
        	JDOField f = jdoClass.getField(fieldNums[i]);
        	qstr.append(rangeVar).append(".")  // NOI18N
                .append(f.getJavaField().getName());
        }
        
        // " from <entity> rv  "       
        qstr.append(" from ").append(jdoClass.getShortName())  // NOI18N
            .append(" ").append(rangeVar); // NOI18N

        // "where rv = ?1"
        qstr.append(" where ").append(rangeVar).append(" = ?1"); // NOI18N
        
        rc = new EJBQLQueryImpl(qstr.toString(), pm);
        
        monitor.postGetLoader(jdoClass, rc);
        
        return rc;
    }

    /**
     * Creates a query that loads a related instance(s).  For example, with a
     * schema in which a Company has many Departments and each Department has
     * one and only one company, navigating from the one side (from Department
     * to Company):
     * <pre>
     * select c from Company c, Department d where d = ?1 and d.company = c
     * </pre>
     * or from the many side (from Company to Deparment):
     * <pre>
     * select d from Department d, Company c where c = ?1 and d member of c.departments
     * </pre>
     * @param jdoClass JDOClass of the instance which is being navigated
     * <em>from</em>.
     * @param fieldNum number of the field in the jdoClass which denotes
     * the related instance(s) that the query should return.
     * @param pm PersistenceManager for which the query is created.
     * @return the in-memory representation of the query.
     */
    private QueryImpl getNavigator(
            JDOClass jdoClass, int fieldNum, PersistenceManagerInternal pm) {

        // XXX TBD Possibly cache Navigators

        QueryImpl rc = null;

        JDOField f = jdoClass.getField(fieldNum);

        boolean onManySide = f.getRelationship().isJDOCollection();

        String rangeVar = "rv"; // NOI18N
        String idVar = "id"; // NOI18N
        
        // "select rv from "
        StringBuffer qstr = new StringBuffer("select object (") // NOI18N
            .append(rangeVar).append(") from "); // NOI18N
        
        String fieldEntityName = null;
        if (onManySide) {
            // Need to get element type of collection
            JDOCollection coll = (JDOCollection) f.getRelationship();
            fieldEntityName = coll.getElementType().getJDOClass().getShortName();
        } else {
            // "<from-entity> rv, "
            fieldEntityName = f.getType().getJDOClass().getShortName();
        }
        qstr.append(fieldEntityName).append(" ") // NOI18N
            .append(rangeVar).append(", "); // NOI18N
 
        // "<to-entity> id "
        String jdoClassName = jdoClass.getShortName();
        qstr.append(jdoClassName)
            .append(" ").append(idVar).append(" "); // NOI18N

        // "where id = ?1 and "
        qstr.append("where ").append(idVar).append(" = ?1 and "); // NOI18N
 
        String fieldName = f.getJavaField().getName();
        
        if (onManySide) {
            // "rv member of id.<fieldName>"
            qstr.append(rangeVar).append(" member of ") // NOI18N
                .append(idVar).append(".").append(fieldName); // NOI18N
        } else {
            // "id.<fieldName> = rv" 
            qstr.append(idVar).append(".").append(fieldName) // NOI18N
                .append(" = ").append(rangeVar); // NOI18N
        }
        
        rc = new EJBQLQueryImpl(qstr.toString(), pm);
        
        monitor.postGetNavigator(jdoClass, rc);
        
        return rc;
    }

    /**
     * Gets a query for the given string.  Checks a cache; if not found then
     * creates and caches a new query.
     * @param query EJBQL string of a query.
     * @param pm PersistenceManager for which the query is created.
     * @return Query implementation.
     */
    private QueryImpl getQuery(String query, PersistenceManagerInternal pm) {
        QueryImpl rc = null;
        // XXX TBD Need to check query cache.
        rc = new EJBQLQueryImpl(query, pm);
        return rc;
    }

    /**
     * Monitors the activity of creating queries
     */
    // XXX TBD Add postCreate{Loader,Navigator}
    interface Monitor {
        /**
         * Invoked by QueryFactory after a query is created with all parameters set.
         * @param jdoClass JDOClass representing the class for which a finder
         * is created.
         * @param q query implementation created by query string.
         */
        void postGetFinder(JDOClass jdoClass, QueryInternal q);

        /**
         * Invoked by QueryFactory after a query is created with all parameters set.
         * @param jdoClass JDOClass representing the class for which a finder
         * is created.
         * @param q query implementation created by query string.
         */
        void postGetLoader(JDOClass jdoClass, QueryInternal q);

        /**
         * Invoked by QueryFactory after a query is created with all parameters set.
         * @param jdoClass JDOClass representing the class for which a finder
         * is created.
         * @param q query implementation created by query string.
         */
        void postGetNavigator(JDOClass jdoClass, QueryInternal q);
}

    static class MonitorImpl implements Monitor {
        /** {@inheritDoc} */
        public void postGetFinder(JDOClass jdoClass, QueryInternal q) {
            // empty
        }

        /** {@inheritDoc} */
        public void postGetLoader(JDOClass jdoClass, QueryInternal q) {
            // empty
        }

        /** {@inheritDoc} */
        public void postGetNavigator(JDOClass jdoClass, QueryInternal q) {
            // empty
        }
    }
    
    /**
     * For testing.  To test QueryFactory's private getZZZ methods, create an
     * Accessor with an instance of a QueryFactory, and invoke on the Accessor
     * instance's methods.
     */
    static class Accessor {
        private final QueryFactory qf;
        
        Accessor(QueryFactory qf) {
            this.qf = qf;
        }
        
        QueryImpl getFinder(JDOClass jdoClass, PersistenceManagerInternal pm) {
            return qf.getFinder(jdoClass, pm); 
        }
        
        QueryImpl getLoader(JDOClass jdoClass, int fieldNums[], PersistenceManagerInternal pm) {
            return qf.getLoader(jdoClass, fieldNums, pm); 
        }
        
        QueryImpl getNavigator(JDOClass jdoClass, int fieldNum, PersistenceManagerInternal pm) {
            return qf.getNavigator(jdoClass, fieldNum, pm); 
        }
    }
}
