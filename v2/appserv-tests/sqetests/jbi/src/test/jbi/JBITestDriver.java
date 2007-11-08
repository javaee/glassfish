/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
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
