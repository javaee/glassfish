/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import sun.misc.BASE64Encoder;

class TestRoleAssignment {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private boolean result = true;
    private final String url;
    private final String username;
    private final String password;
    private final String role;
    private final boolean positiveTest;

    public TestRoleAssignment(String url, String username, String password, String role, boolean positiveTest) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.role = role;
        this.positiveTest = positiveTest;
    }

    public void doTest() {
        try {
            URL u = new URL(url);
            URLConnection uconn = u.openConnection();

            String up = username + ":" + password;
            BASE64Encoder be = new BASE64Encoder();
            up = be.encode(up.getBytes());
            uconn.setRequestProperty("authorization", "Basic " + up);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    uconn.getInputStream()));
            while (reader.readLine() != null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        stat.addDescription("Weblogic Role Assignment test for role: " + role);
        String testId = "Weblogic Role Assignment test for role: " + role;
        if (positiveTest) {
            if (result) {
                stat.addStatus(testId, stat.PASS);
            } else {
                stat.addStatus(testId, stat.FAIL);
            }
        } else { // negative test
            if (result) {
                stat.addStatus(testId, stat.FAIL);
            } else {
                stat.addStatus(testId, stat.PASS);
            }
        }
        stat.printSummary(testId);
    }
    public static final String URL_OPTION = "-url";
    public static final String USER_OPTION = "-user";
    public static final String PASS_OPTION = "-pass";
    public static final String ROLE_OPTION = "-role";
    public static final String NEGATIVE_TEST_OPTION = "-negative";

    public static void usage() {
        System.out.println("usage: java TestRoleAssignment -url <url> -user <user> -pass <pass> -role <role>");
    }

    public static void main(String[] args) {

        String url = null;
        String user = null;
        String pass = null;
        String role = null;
        boolean positiveTest = true;

        for (int i = 0; i < args.length; i++) {
            if (args[i].intern() == URL_OPTION.intern()) {
                url = args[++i];
            } else if (args[i].intern() == USER_OPTION.intern()) {
                user = args[++i];
            } else if (args[i].intern() == PASS_OPTION.intern()) {
                pass = args[++i];
            } else if (args[i].intern() == ROLE_OPTION.intern()) {
                role = args[++i];
            } else if (args[i].intern() == NEGATIVE_TEST_OPTION.intern()) {
                positiveTest = false;
            } else {
                usage();
                System.exit(1);
            }
        }

        if (url == null || user == null || pass == null || role == null) {
            usage();
            System.exit(1);
        }

        TestRoleAssignment test =
                new TestRoleAssignment(url, user, pass, role, positiveTest);
        test.doTest();
    }
}
