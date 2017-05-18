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

package admin;

import java.io.*;
import java.net.*;

public class ZombieTests extends AdminBaseDevTest {

    public ZombieTests() {
        glassFishHome = getGlassFishHome();
        zombieWarFile = new File("apps/Zombie/target/Zombie.war").getAbsoluteFile();
        System.out.println("GF HOME = " + glassFishHome);
        System.out.println("CWD: " + System.getProperty("user.dir"));
        System.out.println("Zombie War File = " + zombieWarFile);
    }

    public static void main(String[] args) {
        new ZombieTests().run();
    }

    @Override
    public String getTestName() {
        return "Testing Forced Server Shutdown";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for forced shutdown of Undead servers";
    }

    @Override
    public void subrun() {
        validate();
        startDomain();
        undeploy(true);
        deploy();
        try {
            makeDasUndead();
            report("RunZombieApp", true);
        }
        catch (Exception e) {
            report("RunZombieApp", false);
        }
        undeploy(false);
        report("failed-stop-domain", !asadmin("stop-domain"));
        // TODO -- add common-utils so I can use ProcessManager etc.
        stopDomainForce();
        stat.printSummary();
    }

    private void validate() {
        report("Zombie War File exists", zombieWarFile.isFile());
    }

    private void undeploy(boolean ignoreError) {
        boolean success = asadmin("undeploy", "Zombie"); // it probably isn't there

        if (ignoreError)
            success = true;

        report("Undeploy Zombie", success);
    }

    private void deploy() {
        report("deploy-zombie", asadmin("deploy", zombieWarFile.getAbsolutePath()));
    }

    private void makeDasUndead() throws MalformedURLException, IOException {
        URL zombieUrl = new URL("http://localhost:8080/Zombie");
        URLConnection conn = zombieUrl.openConnection();
        conn.getInputStream().close();
    }

    private void stopDomainForce() {
        report("forced-stop", asadmin("stop-domain", "--kill"));
    }
    private final File glassFishHome;
    private final File zombieWarFile;
}
