/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package listjndi;

import admin.AdminBaseDevTest;

/*
 * Dev test for list-jndi-entries
 * @author Cheng Fang
 */
public class ListJndiEntriesTest extends AdminBaseDevTest {

    public static final String[] EXPECTED_TOKENS = 
    {"UserTransaction:", "java:global:", "ejb:", "com.sun.enterprise.naming.impl.TransientContext"};

    public static final String INSTANCE_RESOURCE_NAME = "INSTANCE_RESOURCE_NAME";
    public static final String CLUSTER_RESOURCE_NAME = "CLUSTER_RESOURCE_NAME";
    public static final String CLUSTER_NAME = "cluster1";
    public static final String INSTANCE1_NAME = "instance1";
    public static final String INSTANCE2_NAME = "instance2";
    public static final String STANDALONE_INSTANCE_NAME = "instance3";

    public static void main(String[] args) {
        new ListJndiEntriesTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for list-jndi-entries";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("stop-local-instance", STANDALONE_INSTANCE_NAME);
            asadmin("delete-local-instance", STANDALONE_INSTANCE_NAME);
            asadmin("stop-local-instance", INSTANCE1_NAME);
            asadmin("stop-local-instance", INSTANCE2_NAME);
            asadmin("stop-cluster", CLUSTER_NAME);
            asadmin("delete-local-instance", INSTANCE1_NAME);
            asadmin("delete-local-instance", INSTANCE2_NAME);
            asadmin("delete-cluster", CLUSTER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
     //   asadmin("start-domain");
        asadmin("create-cluster", CLUSTER_NAME);

        asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);

        asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);

        asadmin("create-local-instance", STANDALONE_INSTANCE_NAME);

        asadmin("start-cluster", CLUSTER_NAME);
        asadmin("start-local-instance", STANDALONE_INSTANCE_NAME);
        //TODO create a resource in STANDALONE_INSTANCE_NAME only
        //TODO create a resource in CLUSTER_NAME only

        testListJndiEntries();
        testListJndiEntriesTargetServer();
        testListJndiEntriesTargetDomain();
        testListJndiEntriesTargetCluster();
        testListJndiEntriesTargetInstance1();
        testListJndiEntriesTargetInstance2();
        testListJndiEntriesTargetStandaloneInstance();

        cleanup();
      //  asadmin("stop-domain");
        stat.printSummary();
    }

    public void testListJndiEntries() {
        String testName = "testListJndiEntries";
        AsadminReturn result = asadminWithOutput("list-jndi-entries");
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }

    public void testListJndiEntriesTargetServer() {
        String testName = "testListJndiEntriesTargetServer";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", "server");
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }

    public void testListJndiEntriesTargetDomain() {
        String testName = "testListJndiEntriesTargetDomain";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", "domain");
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
    }

    public void testListJndiEntriesTargetCluster() {
        String testName = "testListJndiEntriesTargetCluster";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    public void testListJndiEntriesTargetInstance1() {
        String testName = "testListJndiEntriesTargetInstance1";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", INSTANCE1_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, INSTANCE1_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE2_NAME);
    }

    public void testListJndiEntriesTargetInstance2() {
        String testName = "testListJndiEntriesTargetInstance2";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", INSTANCE2_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE1_NAME);
    }

    public void testListJndiEntriesTargetStandaloneInstance() {
        String testName = "testListJndiEntriesTargetStandaloneInstance";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
        reportUnexpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME, CLUSTER_NAME);
    }

    private void reportResultStatus(String testName, AsadminReturn result) {
        report(testName + "-returnValue", result.returnValue);
        report(testName + "-isEmpty", result.err.isEmpty());
    }

    private void reportExpectedResult(String testName, AsadminReturn result, String... expected) {
        if (expected.length == 0) {
            expected = EXPECTED_TOKENS;
        }
        for (String token : expected) {
            report(testName + "-expected", result.out.contains(token));
        }
    }

    private void reportUnexpectedResult(String testName, AsadminReturn result, String... unexpected) {
        for (String token : unexpected) {
            report(testName + "-unexpected", !result.out.contains(token));
        }
    }
}
