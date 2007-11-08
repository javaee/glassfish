/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServerConnection;
import com.sun.enterprise.util.net.NetUtils;

public class TestDriver {
    
    private final String adminUser;
    private final String adminPassword;
    private final String adminHost;
    private final String adminPort;
    private final String isSecure;
    private final File testFile;
    private List<RemoteAdminQuicklookTest> tests;
    private MBeanServerConnection mbsc;
    
    private static final String SCRIPT_COMMENT = "#"; //this is how comment is denoted, traditionally
    private static final SimpleReporterAdapter reporter = new SimpleReporterAdapter("appserv-tests");
    private static final String DESC = "Admin Infrastructure Tests";
    /** Creates a new instance of TestDriver */
    public TestDriver(final String[] env) throws Exception {
        /* This environment has to be as follows */
        if (env.length < 5)
            throw new RuntimeException("Can't continue, not enough environment parameters");
        adminUser       = env[0] != null ? env[0] : "admin";
        adminPassword   = env[1] != null ? env[1] : "admin123";
        adminHost       = env[2] != null ? env[2] : "localhost";
        adminPort       = env[3] != null ? env[3] : "4848";
        //isSecure      = env[4] != null ? env[4] : "false";
        isSecure        = new Boolean(NetUtils.isSecurePort(adminHost, Integer.parseInt(adminPort))).toString();
        testFile        = env[5] != null ? new File(env[5]) : new File("tests.list");
        tests           = new ArrayList<RemoteAdminQuicklookTest> ();
        initializeConnection();
        initializeTestClasses();
    }
    
    public static void main(final String[] env) throws Exception {
        final TestDriver t = new TestDriver(env);
        t.testAndReportAll();
    }

    ///// private methods /////
    private void initializeConnection() throws Exception {
        mbsc = MBeanServerConnectionFactory.getMBeanServerConnectionHTTPOrHTTPS(adminUser, adminPassword, adminHost, adminPort, isSecure);
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
                if (!failed) {
                    reporter.printSummary(getSummaryString(total));
                    System.out.println(getSummaryString(total));
                }
            }
        }
    }
    private String getSummaryString(final long time) {
        final String s = "Admin Tests: Time Taken = " + time + " milliseconds";
        return ( s );
    }
    private void testAndReportOne(final RemoteAdminQuicklookTest t) {
        final String status = t.test();
        //reporter.addStatus(t.getName(), status);
    }
    ///// private methods /////
}
