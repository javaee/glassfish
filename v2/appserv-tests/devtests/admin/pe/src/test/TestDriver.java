package test;
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

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.util.*;
//import com.sun.enterprise.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.management.MBeanServerConnection;

public class TestDriver {
    
    private String adminUser;
    private String adminPassword;
    private String adminHost;
    private String adminPort;
    private String isSecure;
    private final File testFile;
    private boolean useRmi;
    private List<RemoteAdminQuicklookTest> tests;
    private MBeanServerConnection mbsc;
    private String      testfileName;
    
    private static final String SCRIPT_COMMENT = "#"; //this is how comment is denoted, traditionally
    private static final SimpleReporterAdapter reporter = new SimpleReporterAdapter("devtests");
    private static final String DESC = "Admin Infrastructure Devtests";
    /** Creates a new instance of TestDriver */
    public TestDriver() throws Exception {
        tests           = new ArrayList<RemoteAdminQuicklookTest> ();
        //loadProperties();
        loadRmiProperties();
	testFile = new File(testfileName);
        initializeConnection();
        initializeTestClasses();
    }

    public static void main(final String[] env) throws Exception {
        TestDriver t = new TestDriver();
        t.testAndReportAll();
    }

    ///// private methods /////
    private void initializeConnection() throws Exception {
        System.out.println("Connection Properties: " + adminUser + " " + adminPassword + " " + adminHost + " " + adminPort + " " + isSecure);
        if (useRmi) {
            mbsc = MBeanServerConnectionFactory.getMBeanServerConnectionRMI(adminUser, adminPassword, adminHost, adminPort, isSecure);
            System.out.println("Using RMI: " + mbsc.toString());
        }
        else {
            mbsc = MBeanServerConnectionFactory.getMBeanServerConnectionHTTPOrHTTPS(adminUser, adminPassword, adminHost, adminPort, isSecure);
            System.out.println("Using HTTP: " + mbsc.toString());
        }
    }
    private void initializeTestClasses() throws Exception {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(testFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(SCRIPT_COMMENT))
                    continue;
                System.out.println(line);
                final RemoteAdminQuicklookTest t = c2T(line);
                tests.add(t);
            }
        } finally {
            try {
                br.close();
            } catch(final Exception e) {}
        }
    }
    
    private RemoteAdminQuicklookTest c2T(final String testClass) throws RuntimeException {
        try {
            final Class c                       = Class.forName(testClass);
            final RemoteAdminQuicklookTest t    = (RemoteAdminQuicklookTest) c.newInstance();
            System.out.println("mbsc.... "  + mbsc.getDefaultDomain());
            t.setMBeanServerConnection(this.mbsc);
            return ( t );
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void testAndReportAll() {
        reporter.addDescription(DESC);
        long total = 0;
        for (RemoteAdminQuicklookTest t : tests) {
            boolean failed = false;
            try {
                testAndReportOne(t);
                reporter.addStatus(t.getName(), reporter.PASS);
            } catch(final Exception e) {
                e.printStackTrace();
                reporter.addStatus(t.getName(), reporter.FAIL);   
                total += t.getExecutionTime();
                reporter.printSummary(getSummaryString(total));
                System.out.println(getSummaryString(total));
                failed = true;
            } finally {
                total += t.getExecutionTime();
            }
        }
        reporter.printSummary(getSummaryString(total));
        System.out.println(getSummaryString(total));
    }
    private String getSummaryString(final long time) {
        final String s = "Admin Tests: Time Taken = " + time + " milliseconds";
        return ( s );
    }
    private void testAndReportOne(final RemoteAdminQuicklookTest t) {
        final String status = t.test();
        //reporter.addStatus(t.getName(), status);
    }
    

      private void loadRmiProperties() throws Exception {
        final Properties rmip = new Properties();
        final String rmipf    = "rmi.properties";
        rmip.load(new BufferedInputStream(new FileInputStream(rmipf)));
        useRmi = Boolean.valueOf(rmip.getProperty("useRmi"));
        adminUser = rmip.getProperty("adminUser");
        adminPassword = rmip.getProperty("adminPassword");
        adminHost = rmip.getProperty("adminHost");
        adminPort = rmip.getProperty("adminPort");
        isSecure = rmip.getProperty("isSecure");
        testfileName = rmip.getProperty("testFile");
    }
/*
    private void loadProperties()
    { 
        LocalStringsImpl lsi    = new LocalStringsImpl();
        useRmi              = lsi.getBoolean("useRmi",          true);
        adminUser           = lsi.getString("adminUser",        "admin");
        adminPassword       = lsi.getString("adminPassword",    "adminadmin");
        adminHost           = lsi.getString("adminHost",        "localhost");
        adminPort           = lsi.getString("adminPort",        "4848");
        Boolean bisSecure   = lsi.getBoolean("isSecure",        false);
        testfileName        = lsi.getString("testfile",         "tests.list");
        isSecure            = bisSecure.toString();
    }
 */   
    ///// private methods /////
}
