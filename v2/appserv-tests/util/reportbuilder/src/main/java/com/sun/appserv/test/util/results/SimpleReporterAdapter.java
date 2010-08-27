/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.appserv.test.util.results;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"StringContatenationInLoop"})
public class SimpleReporterAdapter implements Serializable {
    public static final String PASS = "pass";
    public static final String DID_NOT_RUN = "did_not_run";
    public static final String FAIL = "fail";
    private static final Pattern TOKENIZER;
    private final boolean debug = true;
    private final String ws_home;
    private Test test;
    private final String testSuiteName;
    private final TestSuite suite;
    private Reporter reporter;
    public static final String DUPLICATE = " -- DUPLICATE";

    static {
        String pattern = or(
            split("x", "X"),     // AbcDef -> Abc|Def
            split("X", "Xx"),    // USArmy -> US|Army
            //split("\\D","\\d"), // SSL2 -> SSL|2
            split("\\d", "\\D")  // SSL2Connector -> SSL|2|Connector
        );
        pattern = pattern.replace("x", "\\p{Lower}").replace("X", "\\p{Upper}");
        TOKENIZER = Pattern.compile(pattern);
    }

    @Deprecated
    public SimpleReporterAdapter() {
        this("appserv-tests", null);
    }

    @Deprecated
    public SimpleReporterAdapter(String ws_root) {
        this(ws_root, null);
    }

    public SimpleReporterAdapter(String ws_root, String suiteName) {
        ws_home = ws_root;
        if (suiteName == null) {
            testSuiteName = getTestSuiteName();
        } else {
            testSuiteName = suiteName;
        }
        suite = new TestSuite(testSuiteName);
        test = new Test(testSuiteName);
        suite.addTest(test);
    }

    public Reporter getReporter() {
        return reporter;
    }

    public TestSuite getSuite() {
        return suite;
    }

    public void addStatus(String testCaseName, String status) {
        addStatus(testCaseName, status, "");
    }

    public void addStatus(String testCaseName, String status, String message) {
        final TestCase testCase = new TestCase(testCaseName, message);
        testCase.setStatus(status);
        test.addTestCase(testCase);
    }

    public void addDescription(String description) {
        suite.setDescription(description);
    }

    @Deprecated
    public void printSummary(String s) {
        printSummary();
    }

    public void printSummary() {
        try {
            reporter = Reporter.getInstance(ws_home);
            if (debug) {
                System.out.println("Generating report at " + reporter.getResultFile());
            }
            reporter.setTestSuite(suite);
            int pass = 0;
            int fail = 0;
            int d_n_r = 0;
            System.out.println("\n\n-----------------------------------------");
            for (Test test : suite.getTests()) {
                for (TestCase testCase : test.getTestCases()) {
                    String status = testCase.getStatus();
                    if (status.equalsIgnoreCase(PASS)) {
                        pass++;
                    } else if (status.equalsIgnoreCase(DID_NOT_RUN)) {
                        d_n_r++;
                    } else {
                        fail++;
                    }
                    System.out.println(String.format("- %-37s -", testCase.getName() + ": " + status.toUpperCase()));
                }
            }
            if (pass == 0 && fail == 0 && d_n_r == 0) {
                d_n_r++;
                System.out.println(String.format("- %-37s -", testSuiteName + ": " + DID_NOT_RUN));
                final TestCase testCase = new TestCase(testSuiteName);
                testCase.setStatus(DID_NOT_RUN);
                test.addTestCase(testCase);
            }
            System.out.println("-----------------------------------------");
            result("PASS", pass);
            result("FAIL", fail);
            result("DID NOT RUN", d_n_r);
            System.out.println("-----------------------------------------");
            reporter.flushAll();
            createConfirmationFile();
        }
        catch (Throwable ex) {
            System.out.println("Reporter exception occurred!");
            if (debug) {
                ex.printStackTrace();
            }
        }
    }

    private void result(final String label, final int count) {
        System.out.println(String.format("- Total %-12s: %-17d -", label, count));
    }

    public void createConfirmationFile() {
        try {
            FileOutputStream fout = new FileOutputStream("RepRunConf.txt");
            try {
                fout.write("Test has been reported".getBytes());
            } finally {
                fout.close();
            }
        } catch (Exception e) {
            System.out.println("Exception while creating confirmation file!");
            if (debug) {
                e.printStackTrace();
            }
        }
    }

    private String getTestSuiteName() {
        List<StackTraceElement> list = new ArrayList<StackTraceElement>(
            Arrays.asList(Thread.currentThread().getStackTrace()));
        list.remove(0);
        File jar = locate(getClass().getName().replace('.', '/') + ".class");
        while (jar.equals(locate(list.get(0).getClassName().replace('.', '/') + ".class"))) {
            list.remove(0);
        }
        StackTraceElement element = list.get(0);
        File file = locate(element.getClassName().replace('.', '/') + ".class");
        StringBuilder buf = new StringBuilder(file.getName().length());
        for (String t : TOKENIZER.split(file.getName())) {
            if (buf.length() > 0) {
                buf.append('-');
            }
            buf.append(t.toLowerCase());
        }
        return buf.toString().trim();
    }

    public File locate(String resource) {
        String u = getClass().getClassLoader().getResource(resource).toString();
        File file = null;
        try {
            if (u.startsWith("jar:file:")) {
                file = new File(new URI(u.substring(4, u.indexOf("!"))));
            } else if (u.startsWith("file:")) {
                file = new File(new URI(u.substring(0, u.indexOf(resource))));
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return file;
    }

    private static String or(String... tokens) {
        StringBuilder buf = new StringBuilder();
        for (String t : tokens) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(t);
        }
        return buf.toString();
    }

    private static String split(String lookback, String lookahead) {
        return "((?<=" + lookback + ")(?=" + lookahead + "))";
    }

    public static String checkNA(final String value) {
        return value == null ? ReporterConstants.NA : value.trim();
    }
}
