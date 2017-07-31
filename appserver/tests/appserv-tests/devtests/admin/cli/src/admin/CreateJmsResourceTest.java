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

package admin;

/*
 * @author David Zhao
 */
public class CreateJmsResourceTest extends AdminBaseDevTest {
    private static String QUEUE1 = "jms/unittest/queue1";
    private static String FACTORY1 = "jms/unittest/factory1";
    private static String QUEUE2 = "jms/unittest/queue2";
    private static String FACTORY2 = "jms/unittest/factory2";
    private static String FACTORY3 = "jms/unittest/factory3";
    private static String POOL1 = FACTORY3 + "-Connection-Pool";

    public static void main(String[] args) {
        new CreateJmsResourceTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for creating jms resource";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("delete-jms-resource", QUEUE1);
            asadmin("delete-jms-resource", FACTORY1);
            asadmin("delete-jms-resource", QUEUE2);
            asadmin("delete-jms-resource", FACTORY2);
            asadmin("delete-jms-resource", FACTORY3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
        startDomain();
        createJmsResourceWithoutForce();
        createJmsResourceWithForce();
        // The test - createJmsCFWithDupName is disabled temporarily
        // createJmsCFWithDupName();
        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void createJmsResourceWithoutForce() {
        report("createJmsResourceWithoutForce-0", asadmin("create-jms-resource", "--restype", "javax.jms.Queue", "--property", "imqDestinationName=myQueue", QUEUE1));
        checkResource("checkJmsResourceWithoutForce-0", QUEUE1);
        report("createJmsResourceWithoutForce-1", asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", FACTORY1));
        checkResource("checkJmsResourceWithoutForce-0", FACTORY1);
    }

    private void createJmsResourceWithForce() {
        report("createJmsResourceWithForce-0", asadmin("create-jms-resource", "--restype", "javax.jms.Queue", "--property", "imqDestinationName=myQueue", "--force", QUEUE1));
        checkResource("checkJmsResourceWithForce-0", QUEUE1);
        report("createJmsResourceWithForce-1", asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", "--force", FACTORY1));
        checkResource("checkJmsResourceWithForce-1", FACTORY1);
        report("createJmsResourceWithForce-2", !asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", "--force=xyz", FACTORY2));
        report("createJmsResourceWithForce-3", asadmin("create-jms-resource", "--restype", "javax.jms.Queue", "--property", "imqDestinationName=myQueue", "--force", QUEUE2));
        checkResource("checkJmsResourceWithForce-3", QUEUE1);
        report("createJmsResourceWithForce-4", asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", "--force", FACTORY2));
        checkResource("checkJmsResourceWithForce-4", FACTORY2);
    }

    // GLASSFISH-21655: When a CF is created with same JNDI name as that of an existing resource, there should not be a CF or a connection pool created
    private void createJmsCFWithDupName() {
        asadmin("create-jms-resource", "--restype", "javax.jms.Topic", FACTORY3);
        report("createJmsCFWithDupName-0", !asadmin("create-jms-resource", "--restype", "javax.jms.ConnectionFactory", FACTORY3));
        AsadminReturn result = asadminWithOutput("list-connector-connection-pools");
        report("createJmsCFWithDupName-1", !result.out.contains(POOL1));
    }

    private void checkResource(String testName, String expected) {
        AsadminReturn result = asadminWithOutput("list-jms-resources");
        report(testName, result.out.contains(expected));
    }
}
