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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sun.org.apache.jdo.pm.MockPersistenceManagerInternal;

import com.sun.persistence.runtime.model.CompanyMappingModel;
import com.sun.persistence.runtime.query.CompilationMonitor;
import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.query.QueryContext;

/**
 * Tests the EJBQLC compiler.
 * 
 * @author Dave Bristor
 */
public class EJBQLCTest extends PropTestBase {
    /** Set of suites for testing. */
    private static Properties testsuite = new Properties();
    
    /** Set of named testcases */
    private static Map<String, PropTestCase> testcases = new LinkedHashMap<String, PropTestCase>();

    /** Indicator for use in getting query out of property file. */
    static final String QUERY_STRING = "queryString";
    static final String ERROR_MSG = "errorMessage";
    
    /** Means of determining if a test passes or fails. */
    private final EJBQLCTestCompilationMonitor monitor = new EJBQLCTestCompilationMonitor();


    public EJBQLCTest(String testName) {
        super(testName);
        EJBQLASTImpl.setOmitLineInfo(true);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(EJBQLCTest.class);

        return wrap(suite, EJBQLCTest.class, testsuite, testcases);
    }
    
    /**
     * @return the static instance from <em>this</em> class.  So, each
     * subclass of EJBQLCTest <em>does</em> need to override getTestcases.
     */
    protected Map<String, PropTestCase> getTestcases() {
        return testcases;
    }
    
    /*
     * test for void compile(QueryInternal, QueryContext, CompilationMonitor)
     */
    public void testCompile() {        
        QueryContext qc = new PersistenceQueryContext(
            CompanyMappingModel.getInstance());

        Map<String, Diff> failures = new LinkedHashMap<String, Diff>();

        for (PropTestCase ptc : getTestcases().values()) {
            monitor.setup(getClass(), ptc);
            String qstring = ptc.get(QUERY_STRING);
            if (qstring == null) {
                failures.put(ptc.getName(),
                    new Diff("Cannot test anything, because...",
                        "...no EJBQL query text given."));
                continue;
            }
            
            String error = ptc.get(ERROR_MSG);
            try {
                getCompilerDriver().compile(
                    new EJBQLQueryImpl(qstring,
                        new MockPersistenceManagerInternal()), qc, monitor);
                if (error != null) {
                    // Expected error did not occur, so fail the test.
                    failures.put(ptc.getName(),
                        new Diff(qstring,
                            "Did not get the following error, but should have: " + error));
                }
            } catch (EJBQLException ex) {
                // For negative testcases, check that the expected error occurs
                String msg = getErrorDetail(ex.getMessage());
                if (error != null) {
                    // We expect an error, so this is a negative test
                    if (msg != null && !msg.equals(error)) {
                        failures.put(ptc.getName(), new Diff(msg, error));
                    }
                } else {
                    // We got an unexpected error
                    failures.put(ptc.getName(),
                        new Diff(ex.toString(),
                            "Didn't expect an exception, this test should pass!"));
                }
            } catch (Exception ex) {
                failures.put(ptc.getName(),
                    new Diff(ex.toString(),
                        "Didn't expect an exception, or at least not that one."));
            }
        }
            
        /* Report errors */

        boolean mustFail = false;

        if (!failures.isEmpty()) {
            System.err.println("Query compilation exceptions for "
                + this.getClass().getName() + ": \n");
            for (Iterator<String> i = failures.keySet().iterator(); i.hasNext();) {
                String k = i.next();
                Diff d = failures.get(k);
                System.err.println(">Query:     " + k);
                System.err.println(">Generated: '" + d.getGenerated() + "'");
                System.err.println(">Expected:  '" + d.getExpected() + "'");
                System.err.println();
            }
            System.err.println("\n");
        }

        Map<String, Diff> diffs = monitor.getDiffs();
        if (!diffs.isEmpty()) {
            System.err.println("Query compilation diffs for "
                + this.getClass().getName() + ": \n");
            for (Iterator<String> i = diffs.keySet().iterator(); i.hasNext();) {
                String k = i.next();
                Diff d = diffs.get(k);
                System.err.println(">Test, phase: " + k);
                System.err.println(">Generated:   '" + d.getGenerated() + "'");
                System.err.println(">Expected:    '" + d.getExpected() + "'");
                System.err.println();
            }
            mustFail = true;
        }

        if (mustFail) {
            fail("");
        }
    }

    /* Protected implementation. */

    protected EJBQLC getCompilerDriver() {
        return EJBQLC.getInstance();
    }

    /** Return the substring of <code>msg</code> that is after "Error:". */
    private String getErrorDetail(String msg) {
        String rc = msg;
        String token = "Error:";
        int index = msg.indexOf(token);
        if (index != -1) {
            rc = msg.substring(index + token.length());
        }
        return rc.trim();
    }

    /**
     * Monitors compilations by comparing generated with expected results. The
     * expected results are in a directory whose name is based on the name of
     * the calling class, appended with "-results", and each test results file
     * is the same as the name of the test, with an appropriate extension:
     * <ul>
     * <li>.syntax for syntax trees</li>
     * <li>.semantic for semantic trees</li>
     * </ul>
     */
    public class EJBQLCTestCompilationMonitor implements  CompilationMonitor {
        /** Class that is using this monitor. */
        private Class caller;

        /** Provides access to expected result. */
        private PropTestCase ptc;

        /**
         * Indicates which tests failed. If a test failed because it was not in the
         * <code>gold</code> properties given to a <code>postABC</code>, the
         * value will be <code>null</code>, else it will be a string form of the
         * failing AST. Keys are as per {@link getName}.
         */
        private final Map<String, Diff> diffs = new LinkedHashMap<String, Diff>();
        
        void setup(Class caller, PropTestCase ptc) {
            this.caller = caller;
            this.ptc = ptc;
        }

        /* Methods from CompilationMonitor */

        /** @see com.sun.persistence.runtime.query.CompilationMonitor#preSyntax(java.lang.String) */
        public void preSyntax(String qstr) {
        }

        /** @see com.sun.persistence.runtime.query.CompilationMonitor#postSyntax(com.sun.persistence.runtime.query.impl.EJBQLAST) */
        public void postSyntax(String qstr, EJBQLAST ast) {
            validate(ast.getTreeRepr(qstr), "syntax");
        }

        /** @see com.sun.persistence.runtime.query.CompilationMonitor#preSemantic(java.lang.String) */
        public void preSemantic(String qstr) {
        }

        /**@see com.sun.persistence.runtime.query.CompilationMonitor#postSemantic(com.sun.persistence.runtime.query.impl.EJBQLAST) */
        public void postSemantic(String qstr, EJBQLAST ast) {
            validate(ast.getTreeRepr(qstr), "semantic");
        }

        /** @see com.sun.persistence.runtime.query.CompilationMonitor#preOptimize(java.lang.String) */
        public void preOptimize(String qstr) {
        }

        /** @see com.sun.persistence.runtime.query.CompilationMonitor#postOptimize(com.sun.persistence.runtime.query.impl.EJBQLAST) */
        public void postOptimize(String qstr, EJBQLAST ast) {
            validate(ast.getTreeRepr(qstr), "optimize");
        }

        /* EJBQLCTestCompilationMonitor implementation. */

        /** Returns the tests which failed.  If no tests failed, will be empty. */
        public Map<String, Diff> getDiffs() {
            return diffs;
        }

        private String getResultsDirectoryName(String testname) {
            return "test" + File.separator + "src" + File.separator
                + caller.getName().replace('.', File.separatorChar)
                + "-results";
        }

        private String getExpectedFileName(String testname, String pass) {
            return getResultsDirectoryName(testname) 
                + File.separator + testname + "." + pass;
        }

        /**
         * Check or save the result of a test, depending on <code>runTest</code>
         * @param generated result of processing that query text
         * @param phase name of compiler pass for which test was run
         */
        private void validate(String generated, String phase) {

            assert ptc != null : "EJBQLCTestCompilationMonitor.validate: you forgot to setup()";

            String expectedFileName = getExpectedFileName(ptc.getName(), phase);
            String expected = null;
            BufferedReader r = null;
            String name = ptc.getName() + ", " + phase;
            try {
                r = new BufferedReader(
                        new FileReader(expectedFileName));
                
                StringBuffer sb = new StringBuffer(r.readLine());
                String s;
                while ((s = r.readLine()) != null) {
                    sb.append('\n').append(s);
                }
                expected = sb.toString();
                int diffpos = generated.compareTo(expected);
                if (generated == null || !generated.equals(expected)) {
                    diffs.put(name, new Diff(generated, expected));
                }
            } catch (Exception e) {
                // If ptc does not have an ERROR_MSG, then we must load results files.
                if (ptc.get(ERROR_MSG) == null) {
                    String msg = "Couldn't load expected results for "
                        + ptc.getName() + " from file "
                        + expectedFileName + ".";

                    try {
                        String dirName = getResultsDirectoryName(ptc.getName());
                        File d = new File(dirName);
                        if (!d.exists()) {
                            d.mkdir();
                        }
                        PrintWriter p = new PrintWriter(
                            new BufferedWriter(
                                new FileWriter(expectedFileName)));
                        p.print("generated result=" + generated);
                        p.close();
                        msg += "\nCheck generated results in that file";
                    } catch (IOException e1) {
                        msg += "\nCould not write generated results in that file";
                    }
                    diffs.put(name, new Diff(generated, msg));
                }
            }
        }
    }


    /**
     * Provides the generated and expected text when there is a difference.
     * Actually, sometimes we put a user-helpful string into
     * <code>expected</code> when no expected text was found.
     */
    class Diff {
        private final String expected;
        private final String generated;

        Diff(String generated, String expected) {
            this.generated = generated;
            this.expected = expected;
        }

        String getExpected() {
            return expected;
        }

        String getGenerated() {
            return generated;
        }
    }
}
