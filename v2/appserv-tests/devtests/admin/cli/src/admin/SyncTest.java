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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author tmueller
 */
public class SyncTest extends AdminBaseDevTest {

    SyncTest() {
        String host0 = null;
        try {
            host0 = InetAddress.getLocalHost().getHostName();
            host0 = "localhost";  //when DAS and instance are co-located use localhost
        }
        catch (Exception e) {
            host0 = "localhost";
        }
        host = host0;
        System.out.println("Host= " + host);
        instancesHome = new File(new File(getGlassFishHome(), "nodes"), host);

    }
    @Override
    protected String getTestDescription() {
        return "Tests Start-up Synchronization between the DAS and Instances.";
    }

    public static void main(String[] args) {
        new SyncTest().runTests();
    }

    private void runTests() {
        startDomain();
        testCleanupofStaleFiles();
        testStartWithDASDown();
        stopDomain();
        stat.printSummary();
    }

    /*
     * This is a test for requirement SYNC-002
     */
    void testCleanupofStaleFiles() {
        final String tn = "stalefiles";
        final String cname = "syncc1";
        final String i1url = "http://localhost:18080/";
        final String i1murl = "http://localhost:14848/management/domain/";
        final String i1name = "synci1";

        // create a cluster and an instance
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:" +
                "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:" +
                "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));

        // deploy an application to the cluster (before the instance is started)
        /*
         File webapp = new File("resources", "helloworld.war");
        report(tn + "deploy", asadmin("deploy", "--target", cname,
                "--name", "helloworld1", webapp.getAbsolutePath()));
        */
        // create a file in docroot
        File foo = new File(getGlassFishHome(), "domains/domain1/docroot/foo.html");
        try {
            FileWriter fw = new FileWriter(foo);
            fw.write("<html><body>Foo file</body></html>");
            fw.close();
        }
        catch (IOException ioe) {
            report(tn + "file-create", false);
            ioe.printStackTrace();
        }

        // start the instance
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));

        // check that the instance, the app, and the file are there
        report(tn + "list-instances", asadmin("list-instances"));
        report(tn + "getindex1", matchString("GlassFish Server", getURL(i1url)));
        report(tn + "getfoo", matchString("Foo file", getURL(i1url + "foo.html")));
        //report(tn + "getapp1", matchString("Hello", getURL(i1url + "helloworld1/hi.jsp")));

        // stop the instance
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));

        // undeploy
        //report(tn + "undeploy", asadmin("undeploy", "--target", cname, "helloworld1"));
        foo.delete();
        // touch the DAS domain.xml file so that synchronization still occurs
        this.getDASDomainXML().setLastModified(System.currentTimeMillis());

         // start the instance
        report(tn + "start-local-instance1a", asadmin("start-local-instance", i1name));

        // make sure the app and file are gone
        //report(tn + "get-del-app1", !matchString("Hello", getURL(i1url + "helloworld1/hi.jsp")));
        report(tn + "get-del-foo", !matchString("Foo file", getURL(i1url + "foo.html")));

        // stop the instance
        report(tn + "stop-local-instance1a", asadmin("stop-local-instance", i1name));

        // delete the instances and the cluster
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
    }

    /*
     * This is a test for requirement SYNC-003
     */
    void testStartWithDASDown() {
        final String tn = "dasdown";
        final String cname = "syncc2";
        final String i1url = "http://localhost:18080/";
        final String i1murl = "http://localhost:14848/management/domain/";
        final String i1name = "synci2";

        // create a cluster an instance
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:" +
                "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:" +
                "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));

        // deploy an application to the cluster (before instance is started)
        /*
        File webapp = new File("resources", "helloworld.war");
         report(tn + "deploy", asadmin("deploy", "--target", cname,
                "--name", "helloworld2", webapp.getAbsolutePath()));
        */

        // start the instance
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));

        // check that the instance and the app are there
        report(tn + "list-instances", asadmin("list-instances"));
        report(tn + "getindex1", matchString("GlassFish Server", getURL(i1url)));
        //report(tn + "getapp1", matchString("Hello", getURL(i1url + "helloworld2/hi.jsp")));

        // stop the instance
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));

        stopDomain();

         // start the instance
        report(tn + "start-local-instance1a", asadmin("start-local-instance", i1name));

        // make sure the instance and app are still there
        report(tn + "getindex1a", matchString("GlassFish Server", getURL(i1url)));
        //report(tn + "getapp1a", matchString("Hello", getURL(i1url + "helloworld2/hi.jsp")));

        // stop the instance
        report(tn + "stop-local-instance1a", asadmin("stop-local-instance", i1name));

        startDomain();

        // delete the instances and the cluster
        //report(tn + "undeploy", asadmin("undeploy", "--target", cname, "helloworld2"));
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
    }

    void testFullSync() {
        final String tn = "fullsync";
        final String cname = "syncc3";
        final String i1url = "http://localhost:18080/";
        final String i1murl = "http://localhost:14848/management/domain/";
        final String i1name = "synci3";

        // create a cluster and an instance
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:" +
                "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:" +
                "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));

        // create a file in docroot
        File foo = new File(getGlassFishHome(), "domains/domain1/docroot/foo.html");
        try {
            FileWriter fw = new FileWriter(foo);
            fw.write("<html><body>Foo file</body></html>");
            fw.close();
        }
        catch (IOException ioe) {
            report(tn + "file-create", false);
            ioe.printStackTrace();
        }

        // start the instance
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));

        // check that the instance and the file are there
        report(tn + "list-instances", asadmin("list-instances"));
        report(tn + "getindex1", matchString("GlassFish Server", getURL(i1url)));
        report(tn + "getfoo", matchString("Foo file", getURL(i1url + "foo.html")));

        // stop the instance
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));

        // delete the file from the instance
        File fooOnInstance = new File(instancesHome, i1name + "/docroot/foo.html");
        report(tn + "instance-file-exists", fooOnInstance.exists());
        fooOnInstance.delete();
        report(tn + "del-instance-file", !fooOnInstance.exists());

         // start the instance with --fullsync
        report(tn + "start-local-instance1a", asadmin("start-local-instance", "--syncfull=true", i1name));

        // make sure the file is back
        report(tn + "getfoo1", matchString("Foo file", getURL(i1url + "foo.html")));
        report(tn + "instance-file-exists2", fooOnInstance.exists());

        // stop the instance
        report(tn + "stop-local-instance1b", asadmin("stop-local-instance", i1name));

        // delete the instances and the cluster
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
        foo.delete();
    }

    private final String host;
    private final File instancesHome;

}
