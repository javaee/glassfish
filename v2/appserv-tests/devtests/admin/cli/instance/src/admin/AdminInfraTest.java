/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package admin;

import com.sun.appserv.test.BaseDevTest;
import java.io.*;
import java.net.*;

/*
 * Dev test for create/delete/list instance
 * @author Bhakti Mehta
 */
public class AdminInfraTest extends BaseDevTest {
    public AdminInfraTest() {
        String host0 = null;
        try {
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch(Exception e) {
            host0 = "localhost";
        }
        host = host0;
        String home = System.getenv("S1AS_HOME");

        if(home == null)
            throw new IllegalStateException("No S1AS_HOME set!");

        File f = new File(home);

        try {
            f = f.getCanonicalFile();
        }
        catch(Exception e) {
            f = f.getAbsoluteFile();
        }
        glassFishHome = f;

        if(!glassFishHome.isDirectory())
            throw new IllegalStateException("S1AS_HOME is not poiting at a real directory!");

        // it does NOT need to exist -- do not insist!
        instancesHome = new File( new File(glassFishHome, "nodeagents"), host);
    }

    public static void main(String[] args) {
        new AdminInfraTest().run();
    }

    @Override
    protected String getTestName() {
        return "instance";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for create/delete/list instance";
    }

    public void run() {
        bhakti();
        byron();
        stat.printSummary();
    }

    private void bhakti() {
        report("create-instance", asadmin("create-instance",
                "--nodeagent", "localhost",
                "ins1"));

        //list-instances
        report("list-instances", asadmin("list-instances"));

        report("delete-instance", asadmin("delete-instance", "ins1"));
    }

    private void byron() {
        // pidgin English because the strings get truncated.
        report("i1 dir not exists", !checkInstanceDir(I1));
        report("create-local-instance", asadmin("create-local-instance", "--nodeagent", host, I1));
        report("list-instances", asadmin("list-instances"));
        report("i1 dir created", checkInstanceDir(I1));
        printf("Awesome -- the directory was created!!");
        report("delete-local-instance", asadmin("delete-local-instance", "i1"));
        report("i1 dir destroyed", !checkInstanceDir(I1));
    }

    private boolean checkInstanceDir(String name) {
        File inf = new File(instancesHome, name);
        boolean exists = inf.isDirectory();
        String existsString = exists ? "DOES exist" : "does NOT exist";
        printf("The instance-dir, %s, %s\n", inf.toString(), existsString);
        return exists;
    }

    private void printf(String fmt, Object... args) {
        if(DEBUG)
            System.out.printf("**** DEBUG MESSAGE ****  " + fmt + "\n", args);
    }

    private final String host;
    private final String I1 = "i1";
    private final File glassFishHome;
    private final File instancesHome;

    private final static boolean DEBUG = false;
}
