/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.devtests.reportbuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLStreamException;

import com.sun.appserv.test.util.results.HtmlReportProducer;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;
import com.sun.appserv.test.util.results.Test;
import com.sun.appserv.test.util.results.TestCase;
import com.sun.appserv.test.util.results.TestSuite;
import org.testng.Assert;

@org.testng.annotations.Test
public class ReportTest {
    public void stats() {
        SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "report");
        stat.addStatus("first test", SimpleReporterAdapter.FAIL);
        stat.addStatus("second test", SimpleReporterAdapter.PASS);
        stat.addStatus("second test", SimpleReporterAdapter.FAIL);
        stat.printSummary();
        final List<Test> tests = stat.getSuite().getTests();
        Assert.assertEquals(tests.size(), 1, "Should be only 1 Test");
        final List<TestCase> testCases = tests.iterator().next().getTestCases();
        Assert.assertEquals(testCases.size(), 3, "Should have 3 test cases");
        // first test
        final Iterator<TestCase> iterator = testCases.iterator();
        Assert.assertEquals(iterator.next().getStatus(), SimpleReporterAdapter.FAIL, "Should have failed.");
        // second test
        Assert.assertEquals(iterator.next().getStatus(), SimpleReporterAdapter.PASS, "Should have passed.");
        Assert.assertEquals(iterator.next().getStatus(), SimpleReporterAdapter.FAIL, "Should have failed.");
    }

    public void htmlReporter() throws IOException, XMLStreamException {
        HtmlReportProducer producer = new HtmlReportProducer("target/test-classes/ejb_devtests_test_resultsValid.xml",
            false);
        producer.produce();
    }

    public void duplicates() {
        TestSuite suite = new TestSuite("suite");
        Test test = new Test("test", "i have duplicates");
        suite.addTest(test);
        final String name = "case 1";
        final TestCase case1 = new TestCase(name);
        final TestCase case2 = new TestCase(name);
        final TestCase case3 = new TestCase(name);
        test.addTestCase(case1);
        test.addTestCase(case2);
        Assert.assertEquals(2, test.getTestCases().size());
        Assert.assertEquals(case1.getName(), name);
        Assert.assertEquals(case2.getName(), name + SimpleReporterAdapter.DUPLICATE);
        test.addTestCase(case3);
        Assert.assertEquals(case3.getName(), name + SimpleReporterAdapter.DUPLICATE + SimpleReporterAdapter.DUPLICATE);
    }
}
