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
