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
package admin;

import com.sun.appserv.test.BaseDevTest;
import java.io.*;
import java.net.*;

/*
 * Dev test for create/delete/list instance
 * @author Bhakti Mehta
 * @author Byron Nevins
 */
public class AdminInfraTest extends BaseDevTest {
    public AdminInfraTest() {

        printf("DEBUG is turned **ON**");
        String host0 = null;
        try {
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch(Exception e) {
            host0 = "localhost";
        }
        host = host0;
        System.out.println("Host= " + host);
        glassFishHome = getGlassFishHome();
        // it does NOT need to exist -- do not insist!
        instancesHome = new File(new File(glassFishHome, "nodeagents"), host);
        printf("GF HOME = " + glassFishHome);
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
        try {
            startDomain();
            create();
            delete();
            stat.printSummary();
        }
        finally {
            stopDomain();
        }
    }

    private void startDomain() {
        domain1WasRunning = asadmin("start-domain", "domain1");

        if(domain1WasRunning)
            printf("\n*******  IGNORE THE SCARY ERROR ABOVE !!!!!!\n"
                    + "domain1 was already running.  It will not be stopped "
                    + "at the end of the tests.\n******\n");
        else
            printf("domain1 was started.");

    }

    private void stopDomain() {
        if(!domain1WasRunning)
            asadmin("stop-domain", "domain1");
    }

    private void create() {
        // pidgin English because the strings get truncated.
        printf("Create " + instanceNames.length + " instances");
        for(String iname : instanceNames) {
            report(iname + "-nodir", !checkInstanceDir(iname));
            report(iname + "-create", asadmin("create-local-instance", iname));
            report(iname + "-list", asadmin("list-instances"));
            report(iname + "-yesdir", checkInstanceDir(iname));
        }
    }

    private void delete() {
        printf("Delete " + instanceNames.length + " instances");
        for(String iname : instanceNames) {
            report(iname + "-yes-dir", checkInstanceDir(iname));
            report(iname + "-delete", asadmin("delete-local-instance", iname));
            report(iname + "-no-dir", !checkInstanceDir(iname));
        }
    }

    private boolean checkInstanceDir(String name) {
        File inf = new File(instancesHome, name);
        boolean exists = inf.isDirectory();
        String existsString = exists ? "DOES exist" : "does NOT exist";
        //printf("The instance-dir, %s, %s\n", inf.toString(), existsString);
        return exists;
    }

    private void printf(String fmt, Object... args) {
        if(DEBUG)
            System.out.printf("**** DEBUG MESSAGE ****  " + fmt + "\n", args);
    }
    private final String host;
    private final File glassFishHome;
    private final File instancesHome;
    private boolean domain1WasRunning;
    private final static boolean DEBUG;
    private static final String[] instanceNames = new String[]{
        "i0", "i1", "i2", "i3", "i4", "i5", "i6", "i7", "i8", "i9"
    };

    static {
        String name = System.getProperty("user.name");

        if(name != null && name.equals("bnevins"))
            DEBUG = true;
        else
            DEBUG = false;
    }
}
