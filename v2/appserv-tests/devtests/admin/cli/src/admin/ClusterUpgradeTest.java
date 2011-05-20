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

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/*
 * Test attempts to mimic cluster upgrade example, as in:
 * http://wikis.sun.com/display/GlassFish/V3.1ClusterUpgradeExample
 * (though with a different app or set of apps in the domain).
 *
 * The v2.1.1 domain is in a zip file: resources/updomain.zip.
 * This test will unzip the domain (called "updomain") in the domains
 * dir. Then it can upgrade the domain, start the server, create the
 * instances, start them, and check to see that it gets a proper response
 * when connecting to the web app deployed in the instances.
 *
 * The domain is 'updomain' (has all the same ports as default domain,
 * so domain1 needs to be stopped for this to run)
 * The node agent is '007'
 * The cluster is 'upcluster'
 * The instances are 'upin1' and 'upin2' (http ports 38080 and 38081)
 *
 * In the domain, references to the original host name (hostname.us.oracle.com)
 * have been replaced with 'localhost' so that the instances can be
 * started by the DAS. If this doesn't work, we can go with the
 * original version of the domain and perform some string substitution
 * after grabbing the current host name.
 *
 * If you don't want updomain removed after the test (e.g., to look through
 * the DAS logs), comment out the call to removeUpDomain() in the
 * finally block in runEm().
 *  
 */
public class ClusterUpgradeTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Pile of tests for cluster upgrade";
    }

    public static void main(String[] args) {
        // uncomment this for output to the console
        //System.setProperty("verbose", "true");

        new ClusterUpgradeTest().runEm();
    }

    private void runEm() {
        
        // unzip updomain to the domains dir
        unzipUpDomain();

        String oldProps = "";

        try {
            // perform upgrade
            report("upgrade-updomain", asadmin("start-domain",
                "--upgrade", "updomain"));

            // point to different password file that has v2 passwords
            oldProps = System.getProperty("as.props");
            System.setProperty("as.props", oldProps.replaceFirst(
                "config/adminpassword.txt",
                "devtests/admin/cli/resources/uppassword.txt"));
            System.out.println(String.format("Changed as.props to '%s'",
                System.getProperty("as.props")));


            // start domain
            startDomain("updomain");

            // get-health (this really just checks that we can make asadmin calls)
            report("get-health-check-1", asadmin("get-health", "upcluster"));

            // create instances
            report("create-instance-1", asadmin("create-local-instance",
                "--node", "007", "--cluster", "upcluster", "upin1"));
            report("create-instance-2", asadmin("create-local-instance",
                "--node", "007", "--cluster", "upcluster", "upin2"));

            // start cluster
            report("start-upcluster", asadmin("start-cluster", "upcluster"));

            // get-health (actually useful in this case)
            report("get-health-check-2", asadmin("get-health", "upcluster"));

            // check that application is deployed in both instances
            final String expectedResponse = "you_win_this_time";
            String response =
                getURL("http://localhost:38080/SApp-war/test").trim();
            System.out.println(String.format("Response from instance 1: '%s'",
                response));
            report("expected-response-instance-1",
                expectedResponse.equals(response));
            response = getURL("http://localhost:38081/SApp-war/test").trim();
            System.out.println(String.format("Response from instance 2: '%s'",
                response));
            report("expected-response-instance-2",
                expectedResponse.equals(response));

            // stop cluster
            boolean stopRightThere = asadmin("stop-cluster", "upcluster");
            report("stop-upcluster", stopRightThere);
            if (!stopRightThere) {
                System.err.println("Could not stop cluster. You may need to " +
                    "kill some Java processes.");
            }

            // test for issue 14719
            report("create-new-instance-issue-14719",
	            asadmin("create-local-instance", "--node", "007",
		        "--cluster", "upcluster", "upin_new_guy"));
            report("delete-new-instance-issue-14719",
                asadmin("delete-local-instance", "upin_new_guy"));

            // delete instances
            report("delete-instance-1", asadmin("delete-local-instance",
                "upin1"));
            report("delete-instance-2", asadmin("delete-local-instance",
                "upin2"));

            // shut down
            stopDomain("updomain");

            // remove nodes dir
            report("cleanup-nodes-dir", deleteDirectory(
                new File(getGlassFishHome(), "nodes")));

        } finally {
            removeUpDomain();
            System.setProperty("as.props", oldProps);
            System.out.println(String.format("Changed as.props to '%s'",
                System.getProperty("as.props")));
        }

        // print results
        stat.printSummary();
    }

    private void unzipUpDomain() {
        File domainsDir = new File(getGlassFishHome(), "domains");
        final String domains = domainsDir.getAbsolutePath();

        try {
            ZipFile domainZip = new ZipFile("resources/updomain.zip");
            Enumeration<? extends ZipEntry> e = domainZip.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = e.nextElement();
                String entryName = ze.getName();
                if (isDirectory(entryName)) {
                    File f = new File(domains,
                        entryName.substring(0, entryName.length() - 1));
                    f.mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(
                    domainZip.getInputStream(ze));
                int size;
                byte[] buffer = new byte[2048];

                FileOutputStream fos = new FileOutputStream(
                    domains + File.separator + entryName);
                BufferedOutputStream bos =
                    new BufferedOutputStream(fos, buffer.length);

                while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }

                bos.flush();
                bos.close();
                fos.close();
                bis.close();
            }

        } catch (IOException ioe) {
            throw new RuntimeException(
                "Could not unzip updomain.zip file", ioe);
        }
    }

    // remove updomain if it's there
    public void removeUpDomain() {
        File deleteMe = new File(
            new File(getGlassFishHome(), "domains"), "updomain");
        if (deleteMe.exists()) {
            report("delete-updomain", asadmin("delete-domain", "updomain"));
        }
    }
    
    // used in the unzip code
    private boolean isDirectory(String s) {
        char c = s.charAt(s.length() - 1);		
        return c== '/' || c == '\\';
    }

}
