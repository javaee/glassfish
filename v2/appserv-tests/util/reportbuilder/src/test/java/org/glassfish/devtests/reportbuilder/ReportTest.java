/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import com.sun.appserv.test.util.results.HtmlReportProducer;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;
import com.sun.appserv.test.util.results.Test;
import com.sun.appserv.test.util.results.TestCase;
import org.testng.Assert;

@org.testng.annotations.Test
public class ReportTest {
    public void stats() {
        SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "report");
        stat.addStatus("first test", SimpleReporterAdapter.FAIL);
        stat.addStatus("second test", SimpleReporterAdapter.PASS);
        stat.addStatus("second test", SimpleReporterAdapter.FAIL);
        stat.printSummary();

        final Map<String, Test> map = stat.getSuite().getTests();
        Assert.assertEquals(map.size(), 1, "Should be only 1 Test");
        final Map<String, List<TestCase>> testCases = map.values().iterator().next().getTestCases();
        int count = 0;
        final Collection<List<TestCase>> list = testCases.values();
        for (List<TestCase> cases : list) {
            count += cases.size();
        }
        Assert.assertEquals(count, 3, "Should have 3 test cases");

        // first test
        final Iterator<List<TestCase>> iterator = list.iterator();
        List<TestCase> testCase = iterator.next();
        Assert.assertEquals(testCase.get(0).getStatus(), SimpleReporterAdapter.FAIL, "Should have failed.");

        // second test
        testCase = iterator.next();
        Assert.assertEquals(testCase.get(0).getStatus(), SimpleReporterAdapter.PASS, "Should have passed.");
        Assert.assertEquals(testCase.get(1).getStatus(), SimpleReporterAdapter.FAIL, "Should have failed.");
    }

    public void htmlReporter() throws IOException, XMLStreamException {
        HtmlReportProducer producer = new HtmlReportProducer("target/test-classes/ejb_devtests_test_resultsValid.xml", false);
        producer.produce();
    }
}
