/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ReportHandler {
    int pass;
    int fail;
    int didNotRun;
    int suiteCount;
    Configuration config;
    String date;
    int testCaseCount;
    private StringBuilder detail = new StringBuilder();
    private StringBuilder summary = new StringBuilder();
    private PrintWriter writer;

    public ReportHandler(final File file) throws FileNotFoundException {
        writer = new PrintWriter(file);
    }

    void process(TestSuite suite) {
        suite.number = suiteCount++;
        summary.append(buildSummary(suite));
    }

    private String buildSummary(final TestSuite suite) {
        for (Test test : suite.getTests()) {
            for (TestCase testCase : test.getTestCases()) {
                process(suite, testCase);
            }
        }
        return String.format("<tr class=\"%s\"><td width=\"50%%\">%s</td>%s%s%s</tr>",
            resultCssClass(suite),
            String.format("<a href=\"javascript:showHide('table%s')\">%s</a>", suite.number, suite.getName()) + suite
                .toHtml(),
            cell("pass", suite.pass, suite.pass),
            cell("fail", suite.fail, suite.fail),
            cell("didnotrun", suite.didNotRun, suite.didNotRun));
    }

    private String resultCssClass(TestSuite suite) {
        StringBuilder css = new StringBuilder();
        if (suite.pass != 0) {
            css.append("pass ");
        }
        if (suite.fail != 0) {
            css.append("fail ");
        }
        if (suite.didNotRun != 0) {
            css.append("didnotrun ");
        }
        return css.toString();
    }

    void process(final TestSuite suite, final TestCase test) {
        testCaseCount++;
        suite.total++;
        if (ReporterConstants.PASS.equals(test.getStatus())) {
            suite.pass++;
            pass++;
        } else if (ReporterConstants.FAIL.equals(test.getStatus())) {
            fail++;
            suite.fail++;
        } else if (ReporterConstants.DID_NOT_RUN.equals(test.getStatus())) {
            didNotRun++;
            suite.didNotRun++;
        }
    }

    private void print(final String text) {
        writer.write(text + "\n");
    }

    void printHtml() {
        print(String.format(
            "<html><head>"
                + "\n<script>\n%s\n</script>"
                + "\n<style>\n%s\n</style>"
                + "\n<title>GlassFish devtests results</title></head>"
                + "\n<body onLoad=\"toggleResults()\">\n<a name=\"TOP\"/>"
                + "\n%s"
                + "\n<h3>Execution Date: %s</h3>"
                + "%s"
                + "<hr>"
                + "%s"
                + "<hr>"
                + "%s"
                + "\n</body>"
                + "\n</html>\n"
            , readFile("TestResults.js"), readFile("TestResults.css"), header(), date,
            config == null ? "" : config.toHtml(), testSuiteSummary(), detailedResults()));
        writer.flush();
        writer.close();
    }

    private String readFile(final String name) {
        StringBuilder builder = new StringBuilder();
        try {
            final InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            try {
                while (reader.ready()) {
                    builder.append(reader.readLine() + "\n");
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return builder.toString();
    }

    private String detailedResults() {
        StringBuilder builder = new StringBuilder("<a name=\"DetailedResults\"/>\n"
            + "\t<h2>Detailed Results</h2>\n"
            + "\t<table>\n"
            + row(null, "th", "Test Suite", "Passed", "Failed", "Did Not Run")
            + summary);
        return builder.toString() + "</table>" + "<hr>" + detail;
    }

    private String header() {
        return "<center><br/><h2>GlassFish devtests results</h2></center>";
    }

    private String testSuiteSummary() {
        final String passCell = checkbox(pass, false, "pass");
        final String failCell = checkbox(fail, true, "fail");
        final String didntCell = checkbox(didNotRun, true, "didnotrun");
        return "\t<a name=\"Summary\"/><h3>Summary Test Results</h3><form id=\"summary\"><table>"
            + row(null, "th", "Item", "Total", "Pass", "Fail", "Did Not Run")
            + row(null, "td", "Test Suites", suiteCount, "", "", "")
            + String.format("<tr><td>Test Cases</td><td>%s</td>%s%s%s</tr>",
            testCaseCount, cell("pass", pass, passCell), cell("fail", fail, failCell),
            cell("didnotrun", didNotRun, didntCell))
            + "</table></form>";
    }

    public static String cell(final String klass, final int count, final Object content) {
        return String.format("<td%s>%s</td>", count > 0 ? " class=\"" + klass + "\"" : "", content);
    }

    private String checkbox(final int count, final boolean selected, final String affected) {
        return String.format("%s Show: <input type=\"checkbox\" name=\"%s\"%s onchange=\"toggleResults()\">",
            count, affected, selected ? " checked" : "");
    }

    public static String row(String css, final String element, final Object... values) {
        StringBuilder builder = new StringBuilder(String.format("<tr%s>", css == null ? "" : " class=\"" + css + "\""));
        for (Object value : values) {
            builder.append(String.format("<%s>%s</%s>", element, value, element));
        }
        return builder + "</tr>";
    }
}
