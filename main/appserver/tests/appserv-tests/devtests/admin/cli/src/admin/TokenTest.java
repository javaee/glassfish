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

package admin;

/**
 *
 * @author Jennifer Chou
 */
public class TokenTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Tests token support.";
    }

    public static void main(String[] args) {
        new TokenTest().runTests();
    }

    private void runTests() {
        startDomain();
        testDAS();
        //testCluster();
        stopDomain();
        stat.printSummary();
    }

    private void testDAS() {
        report("create-system-properties-domain", asadmin("create-system-properties", "--target", "domain", "jenport=1010"));
        report("create-network-listener", asadmin("create-network-listener", "--listenerport", "${jenport}", "--protocol", "http-listener-1", "jenlistener"));
        report("create-virtual-server", asadmin("create-virtual-server", "--hosts", "localhost", "--networklisteners", "jenlistener", "jenvs"));
        AsadminReturn ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        boolean success = ret.outAndErr.indexOf("1010") >= 0;
        report("port-set-create-domain-sysprop", success);

        // Commented out until 12318, 12330 is fixed
        //report("create-system-properties-config-ISSUE-12330", asadmin("create-system-properties", "--target", "server-config", "jenport=2020"));
        //ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        //success = ret.outAndErr.indexOf("2020") >= 0;
        //report("port-change-create-config-sysprop-ISSUE-12318", success);

        report("create-system-properties-server", asadmin("create-system-properties", "jenport=3030"));
        ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        success = ret.outAndErr.indexOf("3030") >= 0;
        report("port-change-create-server-sysprop", success);

        report("delete-system-property-server", asadmin("delete-system-property", "jenport"));
        ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        success = ret.outAndErr.indexOf("1010") >= 0; //Change back to 2020 when 12330 is fixed
        report("port-change-delete-server-sysprop", success);

        // Commented out until 12318, 12330 is fixed
        //report("delete-system-property-config-ISSUE-12330", asadmin("delete-system-property","--target", "server-config", "jenport"));
        //ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        //success = ret.outAndErr.indexOf("1010") >= 0;
        //report("port-change-delete-config-sysprop-ISSUE-12318", success);

        report("delete-virtual-server", asadmin("delete-virtual-server", "jenvs"));
        report("delete-network-listener", asadmin("delete-network-listener", "jenlistener"));
        report("delete-system-property-domain", asadmin("delete-system-property","--target", "domain", "jenport"));
    }

    private void testCluster() {
        //uncomment when issue 12312 is fixed
        //report("create-system-properties-domain", asadmin("create-system-properties", "--target", "domain", "HTTP_LISTENER_PORT=1010"));
        report("create-cluster-with-syspropport", asadmin("create-cluster", "--systemproperties", "HTTP_LISTENER_PORT=3030", "cluster1"));
        report("create-instance-with-syspropport", asadmin("create-local-instance", "--cluster", "cluster1", "--systemproperties", "HTTP_LISTENER_PORT=4040", "instance1"));

        report("start-local-instance-syspropport", asadmin("start-local-instance", "instance1"));
        String str = getURL("http://localhost:4040");
        boolean success = !str.isEmpty();
        report("check-url-at-server-port", success);

        report("stop-local-instance-syspropport", asadmin("stop-local-instance", "instance1"));
        report("delete-local-instance-syspropport", asadmin("delete-local-instance", "instance1"));
        report("delete-cluster-syspropport", asadmin("delete-cluster", "cluster1"));
    }

}
