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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import com.sun.persistence.runtime.model.CompanyMappingModel;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This serves as a base class for properties-based tests.  It
 * provides for test to have a .properties file representing a suite of tests,
 * another properties file representing parameters for each of the tests in
 * a suite, and for golden files representing the results of each test.  In
 * all cases, the name of the class derived from this one which implements an
 * actual JUnit test is the prefix of all other files relate to that test.
 *
 * @author Dave Bristor
 */
public abstract class PropTestBase extends TestCase {
    private static final String propFileBeginning =
        "test" + File.separator + "src" + File.separator;
    
    /** End of the name of file where queries and results are stored. */
    private static final String suiteFileEnding = "-Suite.properties";
    
    /** End of the name of file where queries and results are stored. */
    private static final String testCaseFileEnding = "-Testcases.properties";
    
    /** Represents each suite which has been loaded. */
    private static Map<Class, Properties> suites = new HashMap<Class, Properties>();
    
    /** Model used for tests. */
    private static final RuntimeMappingModel companyMappingModel
        = CompanyMappingModel.getInstance();

    public PropTestBase(String testName) {
        super(testName);
    }
    
    /**
     * Subclasses should invoke this method from their <code>suite()</code>
     * method. It will cause <code>oneTimeSetup</code> to run once before all
     * of the subclass's tests.
     * @param suite suite of tests to be run
     * @param c class of the subclass which has the actual tests
     * @param testsuite set of lists of tests to run
     * @param testcases set of all possible tests that can be run
     * @return Test that can be returned from the subclass's
     * <code>suite()</code> method.
     */
    protected static Test wrap(TestSuite suite,
            final Class c, final Properties testsuite, 
            final Map<String, PropTestCase> testcases) {

        // access Department JDOClass which causes package.jdo to be read
        String DEPT_CLASSNAME = 
            CompanyMappingModel.COMPANY_PACKAGE + "Department";
        companyMappingModel.getJDOModel().getJDOClass(DEPT_CLASSNAME);

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {
                oneTimeSetUp(c, testsuite, testcases);
            }
        };
        return wrapper;
    }
    
    /**
     * Runs once before all tests in a class.  Loads the test suite and
     * testcases for this test.
     * @param c Class for which this setup is being done
     * @param testsuite properties that will be filled in with the suites
     * for this class.
     * @param testcases set of <code>PropTestCase</code> instances.
     */
    private static void oneTimeSetUp(Class c, Properties testsuite, 
            Map<String, PropTestCase> testcases) {
        
        if (suites.get(c) == null) {
            loadProps(getSuiteFileName(c), testsuite, "test suite");
            suites.put(c, testsuite);
            
            Properties testProps = new Properties();
            loadProps(getTestcasesFileName(c), testProps, "test cases");
            
            for (Iterator i = testsuite.entrySet().iterator(); i.hasNext();) {
                Entry e = (Entry) i.next();
                List<String> testcaseNames = stringToList((String) e.getValue());
                
                // Add each named testcase to set of all testcases
                for (String testcaseName : testcaseNames) {
                    PropTestCase ptc = PropTestCase.create(testcaseName, testProps);
                    testcases.put(testcaseName, ptc);
                }
            }
        }
    }
    
    /**
     * Convert a space-separated string to a list
     */
    protected static List<String> stringToList(String s) {
        List<String> rc = new ArrayList<String>();
        for (StringTokenizer st = new StringTokenizer(s); st.hasMoreElements();) {
            rc.add((String) st.nextElement());  
        }
        return rc;
    }

    /**
     * Loads into <code>props</code> from <code>fileName</code>.  If there's
     * an error, fails the test execution and uses <code>kind</code> to
     * describe the kind of file that could not be found.
     * @param fileName
     * @param props
     * @param kind
     */
    private static void loadProps(String fileName, Properties props, String kind ) {
        try {
            BufferedInputStream is =
                new BufferedInputStream(
                    new FileInputStream(fileName));
            props.load(is);
            is.close();
        } catch (IOException e) {
            fail("Couldn't load " + kind + "  " + fileName);
        }
    }

    /**
     * Gets the absolute filename of the file containing test suite
     * properties for the test specified by <code>c</code>. 
     * @param c class for which the name of a test suite file is returned.
     * @return absolute filename of test suite file.
     */
    private static String getSuiteFileName(Class c) {
        return getFileName(c, suiteFileEnding);
    }
    
    /**
     * Gets the absolute filename of the file containing test case
     * properties for the test specified by <code>c</code>. 
     * @param c class for which the name of a test cases file is returned.
     * @return absolute filename of test cases file.
     */
    private static String getTestcasesFileName(Class c) {
        return getFileName(c, testCaseFileEnding);
    }
    
    /**
     * Gets the absolute filename to the file based on the given class' name,
     * and ending with the given ending.
     * @param c class for which a filename is needed.
     * @param ending last characters of the needed file name.
     * @return absolute filename.
     */
    private static String getFileName(Class c, String ending) {
        return propFileBeginning
        + c.getName().replace('.', File.separatorChar)
        + ending;
    }
}
