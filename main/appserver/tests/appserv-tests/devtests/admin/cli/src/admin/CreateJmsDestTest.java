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
public class CreateJmsDestTest extends AdminBaseDevTest {
    private static String DEST1 = "jmsdest1";
    private static String DEST2 = "jmsdest2";
    private static String DEST3 = "jmsdest3";
    private static String DEST4 = "jmsdest4";
    private static String QUEUE = "queue";
    private static String TOPIC = "topic";

    public static void main(String[] args) {
        new CreateJmsDestTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for creating jms physical destination";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("delete-jmsdest", "--desttype", "queue", DEST1);
            asadmin("delete-jmsdest", "--desttype", "topic", DEST2);
            asadmin("delete-jmsdest", "--desttype", "queue", DEST3);
            asadmin("delete-jmsdest", "--desttype", "topic", DEST4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
        startDomain();
        createJmsHostWithoutForce();
        createJmsHostWithForce();
        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void createJmsHostWithoutForce() {
        report("createJmsHostWithoutForce-0", asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, DEST1));
        checkResource("checkJmsHostWithoutForce-0", QUEUE, DEST1);
        report("createJmsHostWithoutForce-1", asadmin("create-jmsdest", "--target", "server", "--desttype", TOPIC, DEST2));
        checkResource("checkJmsHostWithoutForce-1", TOPIC, DEST2);
    }

    private void createJmsHostWithForce() {
        report("createJmsHostWithForce-0", asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, "--force", DEST1));
        checkResource("checkJmsHostWithForce-0", QUEUE, DEST1);
        report("createJmsHostWithForce-1", asadmin("create-jmsdest", "--target", "server", "--desttype", TOPIC, "--force", DEST2));
        checkResource("checkJmsHostWithForce-1", TOPIC, DEST2);
        report("createJmsHostWithForce-2", !asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, "--force=xyz", DEST3));
        report("createJmsHostWithForce-3", asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, "--force", DEST3));
        checkResource("checkJmsHostWithForce-3", QUEUE, DEST3);
        report("createJmsHostWithForce-4", asadmin("create-jmsdest", "--target", "server", "--desttype", TOPIC, "--force", DEST4));
        checkResource("checkJmsHostWithForce-4", TOPIC, DEST4);
    }

    private void checkResource(String testName, String destType, String expected) {
        AsadminReturn result = asadminWithOutput("list-jmsdest", "--destType", destType);
        report(testName, result.out.contains(expected));
    }
}
