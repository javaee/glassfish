/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package test.jbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.sun.enterprise.util.net.NetUtils;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


/**
 * The test driver class that decides the list of tests to be executed 
 * by reading tests.list and executes them one by one
 */
public class JBITestDriver {
    
    
    private final String adminUser;
    
    private final String adminPasswordFile;
    
    private final String adminHost;
    
    private final String adminPort;
    
    private final String isSecure;
    
    private final File testFile;
    
    private List<JBIQuicklookTest> tests;
    
    private static final String SCRIPT_COMMENT = "#"; 
    
    private static final SimpleReporterAdapter reporter = 
            new SimpleReporterAdapter("appserv-tests");
    
    private static final String DESC = "JBI QuickLook Tests";
    
    private AdminCli adminCli = null;
    
    /** 
     * Creates a new instance of JBITestDriver 
     */
    public JBITestDriver(final String[] args) throws Exception {

        if (args.length < 5) {
            throw new 
                    RuntimeException(
                        "Can't continue, not enough environment parameters");
        }
        
        adminUser       = args[0] != null ? args[0] : "admin";
        adminPasswordFile   = args[1];
        adminHost       = args[2] != null ? args[2] : "localhost";
        adminPort       = args[3] != null ? args[3] : "4848";

        isSecure        = new Boolean(
                NetUtils.isSecurePort(
                    adminHost, 
                    Integer.parseInt(adminPort))).toString();
        adminCli = new AdminCli(
                adminUser, 
                adminPasswordFile,
                adminHost,
                adminPort,
                isSecure);
        
        testFile        = args[5] != null ? new File(args[5]) : new File("tests.list");
        
        tests           = new ArrayList<JBIQuicklookTest> ();
        
        initializeTestClasses();
    }
    
    
    /**
     * The main method is invoked from sqetests/jbi/build.xml to
     * run the tests
     */
    public static void main(String[] env) throws Exception {
        JBITestDriver driver = new JBITestDriver(env);
        driver.testAndReportAll();
    }
    
    /**
     * This method is used to initialize the test classes
     * based on the contents of the tests.list file
     */

    private void initializeTestClasses() throws Exception 
    {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(testFile));
            
            String line = null;
            
            while ((line = br.readLine()) != null) 
            {
                if (line.startsWith(SCRIPT_COMMENT)) 
                {
                    continue;
                }
                JBIQuicklookTest test = getTestClass(line);
                tests.add(test);
            }
        } finally {
            try {
                br.close();
            } catch(final Exception e) {}
        }
    }
    
    
    /**
     * This method is used to convert the name of the test in tests.list
     * file into the class name
     */
    private JBIQuicklookTest getTestClass(String testClass) 
        throws RuntimeException 
    {
    
        try {
            //append the package name
            testClass = "test.jbi." + testClass;
            
            Class c   = Class.forName(testClass);
            
            JBIQuicklookTest test  = (JBIQuicklookTest) c.newInstance();
            
            return ( test );
            
        } catch (final Exception e) {
            
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /**
     * This method is used to execute all the tests
     * one by one
     */
    private void testAndReportAll() {
        
        reporter.addDescription(DESC);
        long total = 0;
        
        for (JBIQuicklookTest t : tests) 
        {
            boolean failed = false;
            try {
                
                String status = testAndReportOne(t);
                if (status.equals(reporter.PASS)) {
                    reporter.addStatus(t.getName(), reporter.PASS);
                } else {
                    reporter.addStatus(t.getName(), reporter.FAIL);                       
                }
                
            } catch(final Exception e) {
                
                e.printStackTrace();
                reporter.addStatus(t.getName(), reporter.FAIL);   
               
            } finally {

                total += t.getExecutionTime();

                reporter.printSummary(getSummaryString(total));
            }
        }
    }
    
    
    /**
     * Utility method to present the time taken for tests
     */
    
    private String getSummaryString(final long time) {
        final String s = "JBI Tests: Time Taken = " + time + " milliseconds";
        return ( s );
    }
    
    
    /**
     * This method is used to invoke a single test
     */
    private String testAndReportOne(JBIQuicklookTest t) {
        return t.test(adminCli);
    }

}
