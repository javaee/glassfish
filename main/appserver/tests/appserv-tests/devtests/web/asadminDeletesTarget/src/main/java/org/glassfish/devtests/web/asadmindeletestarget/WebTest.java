/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.devtests.web.asadmindeletestarget;

import java.io.FileNotFoundException;

import com.sun.appserv.test.BaseDevTest;

/*
 * Unit test for asadmin deletes.
 */
public class WebTest extends BaseDevTest {
    private final String name = System.currentTimeMillis() + "";
    private static final boolean DEBUG = false;

    public static void main(String[] args) throws FileNotFoundException {
        new WebTest().run(args[0]);
    }

    @Override
    protected String getTestName() {
        return "asadmin-deletes-target";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for deleting referenced domain.xml entities";
    }

    public void run(String clusterName) {
        final String port = "" + (Integer.valueOf(antProp("http.port")) + 20);
        report("create-threadpool", asadmin("create-threadpool", "--target", clusterName, name));
        report("create-transport", asadmin("create-transport", "--target", clusterName, name));
        report("create-protocol", asadmin("create-protocol", "--target", clusterName, name));
        report("create-http", asadmin("create-http", "--target", clusterName, "--default-virtual-server", "server", name));
        report("create-network-listener", asadmin("create-network-listener",
            "--target", clusterName,
            "--listenerport", port,
            "--protocol", name,
            "--threadpool", name,
            "--transport", name,
            name));
        report("delete-referenced-threadpool", !asadmin("delete-threadpool", "--target", clusterName, name));
        report("delete-referenced-transport", !asadmin("delete-transport", "--target", clusterName, name));
        report("delete-referenced-protocol", !asadmin("delete-protocol", "--target", clusterName, name));
        report("delete-network-listener", asadmin("delete-network-listener", "--target", clusterName, name));
        report("delete-unreferenced-protocol", asadmin("delete-protocol", "--target", clusterName, name));
        report("delete-unreferenced-threadpool", asadmin("delete-threadpool", "--target", clusterName, name));
        report("delete-unreferenced-transport", asadmin("delete-transport", "--target", clusterName, name));
        stat.printSummary();
    }
}
