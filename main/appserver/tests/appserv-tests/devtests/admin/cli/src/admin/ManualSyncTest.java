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

import java.io.File;

/**
 *
 * @author Jennifer Chou
 */
public class ManualSyncTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Tests Manual Sync support.";
    }

    public ManualSyncTest() {
        host = "localhost";
        glassFishHome = TestEnv.getGlassFishHome();
        dasDomainXml = TestEnv.getDomainXml();
        nodeDir = TestEnv.getNodesHome();
        instancesHome = TestEnv.getInstancesHome(DEFAULT_LOCAL_NODE);
        curDir = new File(System.getProperty("user.dir"));

        // note that it is in /gf/domains/domain1  NOT /gf/domains/domain1/server
        syncDir = new File(TestEnv.getDomainHome(), "sync");
        // it does NOT need to exist -- do not insist!
        //nodeDir = new File(glassFishHome, "nodes");
        //instancesHome = new File(nodeDir, DEFAULT_LOCAL_NODE);
    }

    public static void main(String[] args) {
        new ManualSyncTest().runTests();
    }

    private void runTests() {
        startDomain();
        testExport();
        testRetrieve();
        testExportCluster();
        testImport();
        testImportDasOffline();
        testImportNode();
        testImportNodeDir();
        testEndtoEnd();
        stopDomain();
        stat.printSummary();
    }

    private void testExport() {
        String i = "noinstance";
        report("export-sync-bundle-" + i, !asadmin("export-sync-bundle", "--target", i));

        /*
         * export default bundle
         */
        i = "iexportdefbun";
        String bundleName = i + "-sync-bundle.zip";
        File bundle = new File(syncDir, bundleName);

        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i));
        report("check-bundle-" + i, bundle.isFile());

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete());
        report("delete-syncdir-" + i, syncDir.delete());

        /*
         * export my bundle
         */
        i = "iexportmybun";
        bundleName = i + "-my-bundle.zip";
        File mybundleDir = new File(glassFishHome, "mybundles");
        bundle = new File(mybundleDir, bundleName);

        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i, bundle.getPath()));
        report("check-bundle-" + i, bundle.isFile());

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete());
        report("delete-mybundledir-" + i, mybundleDir.delete());

        /*
         * export default bundle to my dir
         */
        i = "iexportmydir";
        bundleName = i + "-sync-bundle.zip";
        mybundleDir = new File(glassFishHome, "mybundles");
        mybundleDir.mkdir(); //mybundles directory must exist, otherwise it will be exported to a file called 'mybundles'
        bundle = new File(mybundleDir, bundleName);

        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i, mybundleDir.getPath()));
        report("check-bundle-" + i, bundle.isFile());

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete());
        report("delete-mybundledir-" + i, mybundleDir.delete());

        /*
         * export default bundle to curr dir - happens to be
         * domains/domain1/config for glassfish
         */
        i = "iexportcurrdir";
        bundleName = i + "-my-bundle.zip";
        bundle = new File(TestEnv.getDomainConfigDir(), bundleName);

        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i, bundleName));
        report("check-bundle-" + i, bundle.isFile()); // KABOOM

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete()); // KABOOM

        //AsadminReturn ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        //boolean success = ret.outAndErr.indexOf("1010") >= 0;
        //report("port-set-create-domain-sysprop", success);

    }

    private void testRetrieve() {
        String i = "iretrievecurrdir";
        String bundleName = i + "-sync-bundle.zip";
        File bundle = new File(curDir, bundleName);

        /*
         * retrieve bundle in current working directory
         */
        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i, "--retrieve", "true"));
        report("check-bundle-" + i, bundle.isFile());

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete());

        /*
         * retrieve my bundle
         */
        i = "iretrievemybun";
        bundleName = i + "-my-bundle.zip";
        bundle = new File(glassFishHome, bundleName); //parent dir must exist, if it doesn't exist will put in the next parent that exists

        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i, "--retrieve", "true", bundle.getPath()));
        report("check-bundle-" + i, bundle.isFile());

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete());

        /*
         * export default bundle to my dir
         */
        i = "iretrievemydir";
        bundleName = i + "-sync-bundle.zip";
        bundle = new File(glassFishHome, bundleName);

        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i, glassFishHome.getPath()));
        report("check-bundle-" + i, bundle.isFile());

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete());

    }

    private void testExportCluster() {
        /*
         * export using cluster target
         */
        String i = "iexport1";
        String c = "cexport";
        String bundleName = c + "-sync-bundle.zip";
        File bundle = new File(syncDir, bundleName);

        report("create-cluster-" + c, asadmin("create-cluster", c));
        report("export-sync-bundle-bad" + i, !asadmin("export-sync-bundle", "--target", c));
        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, "--cluster", c, i));
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", c));
        report("check-bundle-" + i, bundle.isFile());

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-instance-" + c, asadmin("delete-cluster", c));
        report("delete-bundle-" + c, bundle.delete());
        report("delete-syncdir-" + c, syncDir.delete()); //KABOOM
    }

    private void testImport() {
        String i = "iimportnobundle";
        File bundle = new File("nosuchbundle");
        report("import-sync-bundle-" + i, !asadmin("import-sync-bundle", "--instance", i, bundle.getPath()));

        /*
         * import default bundle
         */
        i = "iimportdefbun";
        String bundleName = i + "-sync-bundle.zip";
        bundle = new File(syncDir, bundleName);

        File instDomainXml = TestEnv.getInstanceDomainXml(DEFAULT_LOCAL_NODE, i);

        report("check-dasdomainxml-" + i, dasDomainXml.exists()); // KABOOM
        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        long dasDomainXmlTS = dasDomainXml.lastModified();
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i));
        report("check-bundle-" + i, bundle.isFile());
        report("import-sync-bundle-" + i, asadmin("import-sync-bundle", "--instance", i, bundle.getPath()));
        String s = get("servers.server." + i + ".property.rendezvousOccurred");
        report("check-rendezvous-" + i, s != null && s.equals("true"));
        report("check-domainxml-" + i, instDomainXml.exists());
        long instDomainXmlTS = instDomainXml.lastModified();
        report("check-timestamp-" + i, dasDomainXmlTS == instDomainXmlTS);

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        report("delete-bundle-" + i, bundle.delete());
        report("delete-syncdir-" + i, syncDir.delete());
    }

    private void testImportDasOffline() {
        String testname = "iimportdasoff";
        String bundleName = testname + "-sync-bundle.zip";
        File bundle = new File(syncDir, bundleName);
        File instDomainXml = TestEnv.getInstanceDomainXml(DEFAULT_LOCAL_NODE, testname);

        report("check-dasdomainxml-" + testname, dasDomainXml.exists());
        report("create-instance-" + testname, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, testname));
        long dasDomainXmlTS = dasDomainXml.lastModified();
        report("export-sync-bundle-" + testname, asadmin("export-sync-bundle", "--target", testname));
        report("check-bundle-" + testname, bundle.isFile());

        /*
         * import bundle with DAS offline
         */
        stopDomain();

        report("import-sync-bundle-" + testname, asadmin("import-sync-bundle", "--instance", testname, bundle.getPath()));

        report("check-domainxml-" + testname, instDomainXml.exists());
        long instDomainXmlTS = instDomainXml.lastModified();
        report("check-timestamp-" + testname, dasDomainXmlTS == instDomainXmlTS);

        startDomain();

        String s = get("servers.server." + testname + ".property.rendezvousOccurred");
        report("check-rendezvous-" + testname, s == null);

        //cleanup
        report("delete-instance-" + testname, asadmin("delete-instance", testname));
        report("delete-bundle-" + testname, bundle.delete());
        report("delete-syncdir-" + testname, syncDir.delete());
    }

    /*
     * WBN - yes this is VERY confusing!
     * we create an instance in the default node
     * we import to a different fake node, "nodeimport"
     */
    private void testImportNode() {
        String i = "iimportnode";
        String node = "nodeimport";
        String bundleName = i + "-sync-bundle.zip";
        File bundle = new File(syncDir, bundleName);
        File instDomainXml = TestEnv.getInstanceDomainXml(node, i); // gf3/gf/nodes/nodeimport/iimportnode/config/domain.xml
        File dasFile = TestEnv.getDasPropertiesFile(node);

        report("check-dasdomainxml-" + i, dasDomainXml.exists());
        report("create-instance-" + i, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, i));
        long dasDomainXmlTS = dasDomainXml.lastModified();
        report("export-sync-bundle-" + i, asadmin("export-sync-bundle", "--target", i));
        report("check-bundle-" + i, bundle.isFile());

        report("import-sync-bundle-" + i, asadmin("import-sync-bundle", "--instance", i, "--node", node, bundle.getPath()));

        report("check-domainxml-" + i, instDomainXml.exists());
        long instDomainXmlTS = instDomainXml.lastModified();
        boolean same = (dasDomainXmlTS == instDomainXmlTS);

        if(!same) {
            // troubleshooting info
            System.out.printf("XXXXXXXXXXXXXXXXXXXXXXXXXXX\ndas timestamp: %s\ninstance timestamp: %s\nXXXXXXXXXXXXXXX\n",
                    "" + dasDomainXmlTS, "" + instDomainXmlTS);
            System.out.printf("XXXXXXXXXXXXXX domxml=%s, instxml=%s\n", dasDomainXml, instDomainXml);
        }
        report("check-timestamp-" + i, same);
        report("check-das-properties-" + i, dasFile.exists());
        String s = get("servers.server." + i + ".property.rendezvousOccurred");
        report("check-rendezvous-" + i, s.equals("true"));

        //cleanup
        report("delete-instance-" + i, asadmin("delete-instance", i));
        bundle.delete();
        syncDir.delete();

        File domainHome = TestEnv.getDomainHome();
        File nodeHome = TestEnv.getInstancesHome(node);

        if (TestEnv.isV4Layout())
            deleteDirectory(new File(domainHome, "agent"));
        else
            deleteDirectory(nodeHome);
    }

   private void testImportNodeDir() {
        String instance = "iimportnodedir";
        String node = "nodeimport";
        String bundleName = instance + "-sync-bundle.zip";
        File bundle = new File(syncDir, bundleName);
        File nodeDirParent = TestEnv.getNodesHome();
        File instDomainXml = TestEnv.getInstanceDomainXml(node, instance);
        File dasFile = TestEnv.getDasPropertiesFile(DEFAULT_LOCAL_NODE);

        report("check-dasdomainxml-" + instance, dasDomainXml.exists());
        report("create-instance-" + instance, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, instance));
        long dasDomainXmlTS = dasDomainXml.lastModified();
        report("export-sync-bundle-" + instance, asadmin("export-sync-bundle", "--target", instance));
        report("check-bundle-" + instance, bundle.isFile());

        if (TestEnv.isV4Layout())
            report("import-sync-bundle-" + instance, asadmin("import-sync-bundle", "--instance", instance, "--node", node, "--domaindir", nodeDirParent.getPath(), bundle.getPath()));
        else
            report("import-sync-bundle-" + instance, asadmin("import-sync-bundle", "--instance", instance, "--node", node, "--nodedir", nodeDirParent.getPath(), bundle.getPath()));

        report("check-domainxml-" + instance, instDomainXml.exists());
        long instDomainXmlTS = instDomainXml.lastModified();
        report("check-timestamp-" + instance, dasDomainXmlTS == instDomainXmlTS);

        report("check-das-properties-" + instance, dasFile.exists());

        String s = get("servers.server." + instance + ".property.rendezvousOccurred");
        report("check-rendezvous-" + instance, s != null && s.equals("true"));

        //cleanup
        report("delete-instance-" + instance, asadmin("delete-instance", instance));
        bundle.delete();
        syncDir.delete();
        if(TestEnv.isV3Layout())
            deleteDirectory(nodeDirParent);
    }

    private void testEndtoEnd() {
        String testname = "iendtoend";
        String bundleName = testname + "-sync-bundle.zip";
        File bundle = new File(syncDir, bundleName);
        File instDomainXml = TestEnv.getInstanceDomainXml(DEFAULT_LOCAL_NODE, testname);

        File webapp = new File("resources", "helloworld.war");
        final String i1url = "http://localhost:18080/";

        report("check-dasdomainxml-" + testname, dasDomainXml.exists());

        // byron says this create-instance call is broken in das-branch.
        // when you step over the next line in v4 trunk -- domain.xml exists
        // in the branch, the iendtoend dir exists but is totally empty...
        // the error shows up below in the check for domain.xml
        // TODO THIS IS BROKEN IN THE BRANCH WBN
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

        report("create-instance-" + testname, asadmin("create-instance", "--node", DEFAULT_LOCAL_NODE, "--systemproperties",
                "HTTP_LISTENER_PORT=18080", testname));

        report("deploy-" + testname, asadmin("deploy", "--target", testname, webapp.getAbsolutePath()));

        long dasDomainXmlTS = dasDomainXml.lastModified();
        report("export-sync-bundle-" + testname, asadmin("export-sync-bundle", "--target", testname));
        
        report("check-domainxml-" + testname, instDomainXml.exists());

        stopDomain();

        report("check-bundle-" + testname, bundle.isFile());
        report("import-sync-bundle-" + testname, asadmin("import-sync-bundle", "--instance", testname, bundle.getPath()));

        long instDomainXmlTS = instDomainXml.lastModified();
        report("check-timestamp-" + testname, dasDomainXmlTS == instDomainXmlTS);

        report("start-local-instance-" + testname, asadmin("start-local-instance", testname));

        String urlStr = getURL(i1url + "helloworld/hi.jsp");
        report("check-app-" + testname, matchString("Hello", urlStr));

        startDomain();

        String s = get("servers.server." + testname + ".property.rendezvousOccurred");
        report("check-rendezvous-" + testname, s == null);

        //cleanup
        report("undeploy-" + testname, asadmin("undeploy", "--target", testname, "helloworld"));
        report("stop-local-instance-" + testname, asadmin("stop-local-instance", testname));
        sleep(5);
        report("delete-instance-" + testname, asadmin("delete-instance", testname));
        bundle.delete();
        syncDir.delete();
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                }
                else {
                    f.delete();
                }
            }
        }
        return (path.delete());
    }

    private void sleep(int n) {
        try {
            // Give instances time to come down
            Thread.sleep(n * 1000);
        }
        catch (InterruptedException e) {
        }

    }
    private final String host;
    private final File glassFishHome;
    private final File nodeDir;
    private final File instancesHome;
    private final File syncDir;
    private final File curDir;
    private final File dasDomainXml;
    private final String DEFAULT_LOCAL_NODE = "localhost-domain1";
}
