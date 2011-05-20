/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * This will test The Config-006 Config validation using Bean validation feature
 * @author Bhakti Mehta
 */
public class ValidationTests extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Tests config validation";
    }

    public static void main(String[] args) {
        ValidationTests tests = new ValidationTests();
        tests.runTests();
    }

    private void runTests() {
        startDomain();
        testClusterValidation();
        testNodeInstanceValidation();
        stopDomain();
        stat.printSummary();
    }

    private void testClusterValidation() {
        final String cname = "^%^%";
        final String goodcl = "validcl";
        final String junksysprops = "$$$$=bar";

        report("create-cluster-" + cname , !asadmin("create-cluster", cname));

        report("create-cluster-" + goodcl + "-junksysprop",!asadmin("create-cluser", "--systemproperties", junksysprops, goodcl));

    }

    private void testNodeInstanceValidation() {
        final String iname = "@#^%^%";
        final String goodins = "validins";
        final String junksysprops = "$$$$=bar";
        final String goodconfig="goodconfig";
        final String goodnode = "goodnode";

        report("create-instance-" + iname , !asadmin("create-instance", iname));

        report("create-local-instance-junksysprops", !asadmin("create-local-instance",
                        "--systemproperties", junksysprops, goodins));

        report("copy-config",!asadmin("copy-config",  "--systemproperties", "!@*^*^=bar", "default-config", goodconfig));

        report ("create-node-ssh", !asadmin ("create-node-ssh", "--nodehost", "*%*", "--installdir","/tmp/bar" ,goodnode ));


        report("create-sysprops",!asadmin( "create-system-properties", "A%S%S=bar"));

        report("issue11200-create-message-sec-provider", !asadmin( "create-message-security-provider", "--classname", "com.sun.foo", "--layer" ,"SOAP" ,"<script>alert(\"x\")</script>"));

        report("create-message-sec-provider-invalid-classname", ! asadmin( "create-message-security-provider", "--layer", "SOAP", 
                "--classname", "com/sun", "ggg"));
        
        report("create-audit-module-invalid-classname",! asadmin( "create-audit-module",
                "--classname", "*fffs344:33",  "foo1"));

    }
}
