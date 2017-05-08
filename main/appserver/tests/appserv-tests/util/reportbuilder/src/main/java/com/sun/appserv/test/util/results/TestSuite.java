/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

/**
 * @Class: TestSuite
 * @Description: Class holding One TestSuite info.
 * @Author : Ramesh Mandava
 * @Last Modified : By Ramesh on 10/24/2001
 * @Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of tests 		used a separate testIdVector
 * @Last Modified : By Justin Lee on 10/05/2009
 */
public class TestSuite {
    private String id;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private List<Test> tests = new ArrayList<Test>();
    int pass;
    int fail;
    int didNotRun;
    int total;
    public int number;
    private boolean written;

    public TestSuite() {
    }

    public TestSuite(String name) {
        this();
        this.name = SimpleReporterAdapter.checkNA(name);
        id = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = SimpleReporterAdapter.checkNA(description);
    }

    public List<Test> getTests() {
        return tests;
    }

    public void addTest(Test test) {
        for (Test aTest : tests) {
            if(aTest.getName().equals(test.getName())) {
                test.setName(test.getName() + SimpleReporterAdapter.DUPLICATE);
            }
        }
        tests.add(test);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TestSuite");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", tests=").append(tests);
        sb.append('}');
        return sb.toString();
    }

    public String toXml() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<testsuite>\n");
        buffer.append("<id>" + id.trim() + "</id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        buffer.append("<tests>\n");
        for (Test myTest : tests) {
            buffer.append(myTest.toXml());
        }
        buffer.append("</tests>\n");
        buffer.append("</testsuite>\n");
        return buffer.toString();
    }

    public String toHtml() {
        StringBuilder table = new StringBuilder(
            "<div id=\"table" + number + "\" class=\"suiteDetail\"><table width=\"40%\">"
                + ReportHandler.row(null, "td", "Testsuite Name", getName())
                + ReportHandler.row(null, "td", "Testsuite Description", getDescription())
                + ReportHandler.row(null, "th", "Name", "Status"));
        for (Test test : getTests()) {
            for (TestCase testCase : test.getTestCases()) {
                final String status = testCase.getStatus();
                table.append(String.format("<tr><td>%s</td>%s", testCase.getName(),
                    ReportHandler.cell(status.replaceAll("_", ""), 1, status)));
            }
        }
        return table
            + "<tr class=\"nav\"><td colspan=\"2\">"
            + "[<a href=#DetailedResults>Detailed Results</a>"
            + "|<a href=#Summary>Summary</a>"
            + "|<a href=#TOP>Top</a>]"
            + "</td></tr>"
            + "</table></div><p>";

    }

    public boolean getWritten() {
        return written;
    }

    public void setWritten(final boolean written) {
        this.written = written;
    }
}
