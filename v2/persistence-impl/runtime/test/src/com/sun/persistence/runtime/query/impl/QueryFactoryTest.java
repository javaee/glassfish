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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import junit.framework.*;

import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOModel;

import com.sun.org.apache.jdo.pm.MockPersistenceManagerInternal;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;

import com.sun.persistence.runtime.model.CompanyMappingModel;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.runtime.query.QueryInternal;

/**
 * Tests the QueryFactory.
 * 
 * @author db13166
 */
public class QueryFactoryTest extends PropTestBase {
    /** Set of suites for testing. */
    private static Properties testSuite = new Properties();
    
    private static Map<String, PropTestCase> testcases = new HashMap<String, PropTestCase>();
    
    private static final String TYPE = "type";
    private static final String EJBQL = "ejbql";
    private static final String ENTITY = "entity";
    private static final String FIELD = "field";
    private static final String FIELDS = "fields";
    
    private static final String FINDER = "finder";
    private static final String LOADER = "loader";
    private static final String NAVIGATOR = "navigator";
    
    /** Unit under test. */
    private QueryFactory queryFactory;
    
    /** Provides access to private methods of QueryFactory. */
    private QueryFactory.Accessor accessor;
    
    private PersistenceManagerInternal pm
        = new MockPersistenceManagerInternal();

    /** Means of determining if a test passes or fails. */
    private QueryFactoryMonitor monitor;
    
    private static final RuntimeMappingModel companyMappingModel
        = CompanyMappingModel.getInstance();

    public QueryFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();

        monitor = new QueryFactoryMonitor();
        queryFactory = new QueryFactory(monitor);
        accessor = new QueryFactory.Accessor(queryFactory);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(QueryFactoryTest.class);

        return wrap(suite, QueryFactoryTest.class, testSuite, testcases);
    }
    
    public void testGetFinders() {
        boolean success = true;
        for (PropTestCase ptc : testcases.values()) {
            if (FINDER.equals(ptc.get(TYPE))) {
                monitor.reset(ptc);

                JDOClass c = getTestClass(ptc.get(ENTITY));
                QueryImpl q = accessor.getFinder(c, pm);
                validate("testGetFinders");
                compile(q, "testGetFinders", false);
            }
        }
        if (!success) {
            fail("testGetFinders");
        }
    }
    
    public void testGetNavigators() {
        boolean success = true;
        for (PropTestCase ptc : testcases.values()) {
            if (NAVIGATOR.equals(ptc.get(TYPE))) {
                monitor.reset(ptc);

                JDOClass c = getTestClass(ptc.get(ENTITY));
                JDOField f = c.getField(ptc.get(FIELD));
                int fieldNum =  f.getFieldNumber();

                QueryImpl q = accessor.getNavigator(c, fieldNum, pm);
                success &= validate("testGetNavigators");
                compile(q, "testGetNavigators", false);
            }
        }
        if (!success) {
            fail("testGetNavigators");
        }
    }
    
    /**
     * Test navigation from the MANY side of an M-N relationship.
     * E.g., each Project has many member Employees, and each Employee
     * works on many Projects.
     */
    public void testGetNavigatorManyToMany() {
        // XXX TBD test getNavigator M-N when model allows
    }
    
    public void testGetLoaders() {
        boolean success = true;
        for (PropTestCase ptc : testcases.values()) {
            if (LOADER.equals(ptc.get(TYPE))) {
                monitor.reset(ptc);

                JDOClass c = getTestClass(ptc.get(ENTITY));
                List<String> fieldNames = stringToList(ptc.get(FIELDS));
                
                int fieldNums[] = new int[fieldNames.size()];
                int i = 0;
                for (String name : fieldNames) {
                    JDOField f = c.getField(name);
                    fieldNums[i++] =  f.getFieldNumber();
                }

                QueryImpl q = accessor.getLoader(c, fieldNums, pm);
                validate("testGetLoaders");
                compile(q, "testGetLoaders", true);
            }
        }
        if (!success) {
            fail("testGetLoaders");
        }
    }

    /**
     * This makes sure that we can get queries via getQuery.  This is basically
     * a rehash of other test methods, but via the public API.  <em>Note:</em>
     * This test presumes the existence of tests named
     * {Finder,Loader,Navigator}001 in QueryFactoryTest-Testcases.properties.
     */    
    public void testGetSomeQueries() {
        QueryFactory qf = new QueryFactory(monitor);
        
        /* getQuery with null fieldNums. */
        PropTestCase ptc = testcases.get("Finder001");
        monitor.reset(ptc);
        JDOClass c = getTestClass(ptc.get(ENTITY));
        QueryInternal q = qf.getQuery(c, null, pm);
        validate("testGetSomeQueries");
        compile(q, "testGetSomeQueries", false);
        
        /* getQuery with fieldNums.length == 0. */
        q = qf.getQuery(c, new int[] { }, pm);
        validate("testGetSomeQueries");
        compile(q, "testGetSomeQueries", false);
        
        /* Navigator. */
        ptc = testcases.get("Navigator001");
        monitor.reset(ptc);

        c = getTestClass(ptc.get(ENTITY));
        JDOField f = c.getField(ptc.get(FIELD));
        int fieldNum =  f.getFieldNumber();
        q = qf.getQuery(c, new int[] { fieldNum }, pm);
        validate("testGetSomeQueries");
        compile(q, "testGetSomeQueries", false);
        
        /* Loader. */
        ptc = testcases.get("Loader001");
        monitor.reset(ptc);

        c = getTestClass(ptc.get(ENTITY));
        List<String> fieldNames = stringToList(ptc.get(FIELDS));
        
        int fieldNums[] = new int[fieldNames.size()];
        int i = 0;
        for (String name : fieldNames) {
            f = c.getField(name);
            fieldNums[i++] =  f.getFieldNumber();
        }

        q = qf.getQuery(c, fieldNums, pm);
        validate("testGetSomeQueries");
        compile(q, "testGetSomeQueries", true);
    }
    
    
    /* Private implementation. */
    
    /**
     * Returns a JDOClass for the given short name of a class.
     */
    private static JDOClass getTestClass(String shortName) {
        JDOModel jdoModel = companyMappingModel.getJDOModel();
        return jdoModel.getJDOClassForShortName(shortName);
    }
    /**
     * Compiles the given query, reporting diffs (if any).
     * @param q Query to compile.
     * @param testname XXX TODO
     * @param requiresEJB3 TODO
     */
    private void compile(QueryInternal q, String testname, boolean requiresEJB3) {        
        QueryContext qc = new PersistenceQueryContext(companyMappingModel);
        EJBQLC compiler = requiresEJB3
            ? EJBQLC3.getInstance() : EJBQLC.getInstance();
        try {
            compiler.compile(q, qc, null);
        } catch(Exception ex) {
            System.err.println("Testcase: " + testname 
                + ": Caught exception while compiling " + q.getQuery());
            ex.printStackTrace(System.err);
            System.err.println();
            fail(testname);
        }
    }
    
    /**
     * Check for errors in running a test.
     * @param testname XXX TODO
     */
    private boolean validate(String testname) {
        boolean rc = true;
        Map<String, String> diffs = monitor.getDiffs();
        if (!diffs.isEmpty()) {
            for (Iterator<String> i = diffs.keySet().iterator(); i.hasNext();) {
                String k = i.next();
                System.err.println("Testcase:  " + k 
                    + ", failed when running " + testname);
                PropTestCase ptc = testcases.get(k);
                assert ptc != null : "Can't get PropTestCase for " + k;
                System.err.println("Expected:  " + ptc.get(EJBQL));
                System.err.println("Generated: " + diffs.get(k));
            }
            System.err.println();
            rc = false;
        }
        return rc;
    }

    
    /**
     * Check that the QueryFactory is producing expected results.
     */
    public class QueryFactoryMonitor implements QueryFactory.Monitor {
        /** Provides access to expected result. */
        private PropTestCase ptc;

        /** Maps name of failed test to the generated/incorrect results. */
        private final Map<String, String> diffs = new HashMap<String, String>();

        /** Creates a new instance of QueryFactoryMonitor */
        public QueryFactoryMonitor() {
        }
        
        /* Methods from QueryFactory.Monitor */

        /** {@inheritDoc} */
        public void postGetFinder(JDOClass c, QueryInternal q) {
            validate(q);
        }

        /** {@inheritDoc} */
        public void postGetLoader(JDOClass c, QueryInternal q) {
            validate(q);
        }

        /** {@inheritDoc} */
        public void postGetNavigator(JDOClass c, QueryInternal q) {
            validate(q);
        }

        /* Methods from QueryFactoryMonitor. */
        
        Map<String, String> getDiffs() {
            return diffs;
        }
        
        /**
         * Set <code>ptc</code> to that given, and set clear
         * <code>diffs</code>. 
         */
        void reset(PropTestCase ptc) {
            this.ptc = ptc;
            diffs.clear();
            
        }

        /* Private implementation */

        /**
         * fail if the previously saved query, identified by <code>qstr</code>
         * does not match the given query.
         */
        private void validate(QueryInternal q) {
            assert ptc != null : "QueryFactoryMonitor.validate: you forgot to setPropTestCase";
            String expected = ptc.get(EJBQL);
            String generated = q.getQuery();
            if (generated == null || !generated.equals(expected)) {
                diffs.put(ptc.getName(), generated);
            }
        }
    }
}
