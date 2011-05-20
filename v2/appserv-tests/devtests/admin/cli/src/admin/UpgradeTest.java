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
import javax.xml.xpath.XPathConstants;

/*
 * Dev test for config upgrade from v2.1 to 3.1.
 * If you want to add more tests, you can probably make changes in
 * an existing domain.xml file in the resources/configs directory.
 *
 * Otherwise, just add another domain.xml file there and follow
 * the instructions in the runEm() method.
 *
 * @author Bobby Who Copied Everything From Other Tests So Don't Ask Him
 */
public class UpgradeTest extends AdminBaseDevTest {

    // the original domain.xml file will me moved to this
    private File movedDomXml;

    @Override
    protected String getTestDescription() {
        return "Unit tests for upgrade";
    }

    public static void main(String[] args) {
        new UpgradeTest().runEm();
    }

    private void runEm() {
        // before copying in our domain config, move the old one
        renameOriginalDomainConfig();

        /*
         * To add tests that require a different source domain.xml,
         * put them in a new method like this one. In your test method,
         * just call copyDomainConfig() and you're ready to test.
         */
        testV2Domain();

        // after all the tests have run, move back the old domain
        restoreOriginalDomainConfig();

        // print results
        stat.printSummary();
    }

    /*
     * test that uses the v2domain.xml file in resources/configs
     *
     * This mostly tests cluster/gms-related information. Feel free
     * to add more.
     */
    private void testV2Domain() {
        copyDomainConfig("v2domain.xml");

        // quick check before upgrade to see if our data is there
        String xPath =
            "//cluster[@name='testUpgradeCluster']/@heartbeat-enabled";
        Boolean heartbeatEnabled = (Boolean)
            evalXPath(xPath, getDASDomainXML(), XPathConstants.BOOLEAN);
        report("pre-upgrade-check", heartbeatEnabled);

        // now the upgrade (note: this leaves server in stopped state)
        report("run-upgrade", asadmin("start-domain", "--upgrade"));

        // non-default values should be there
        xPath = "//config" +
            "[@name='testUpgradeClusterNonDefaultGMSProperties-config']" +
            "/group-management-service" +
            "/failure-detection" +
            "/@max-missed-heartbeats";
        Double heartBeats =
            (Double) evalXPath(xPath, getDASDomainXML(), XPathConstants.NUMBER);
        report("non-default-heartbeats", heartBeats.intValue() == 4);

        // default values should not be there
        xPath = "//config" +
            "[@name='testUpgradeCluster-config']" +
            "/group-management-service" +
            "/failure-detection" +
            "/@max-missed-heartbeats";
        Object node = evalXPath(xPath, getDASDomainXML(), XPathConstants.NODE);
        report("default-heartbeats", node == null);

        // start server for 'asadmin get' tests
        report("server-start-post-upgrade", asadmin("start-domain"));

        final String attribute =
            "configs.config.testUpgradeCluster-config.group-management-service.failure-detection.max-missed-heartbeats";
        String out = asadminWithOutput("get", attribute).outAndErr;
        report("default-heartbeats-get-cmd",
            out.indexOf(attribute + "=3") > 0);

        // and bring it down
        report("server-stop-post-upgrade", asadmin("stop-domain"));
    }

    // run before any upgrade test
    private void renameOriginalDomainConfig() {
        File domXml = getDASDomainXML();
        movedDomXml = new File(domXml.getParentFile(), "domain.xml.moved");
        report("moved-domain", domXml.renameTo(movedDomXml));
    }

    // cleanup after upgrade test
    private void restoreOriginalDomainConfig() {
        getDASDomainXML().delete();
        report("restored-domain", movedDomXml.renameTo(getDASDomainXML()));
    }

    // run from each test method to copy a domain into the server
    private void copyDomainConfig(String fileName) {
        File source = new File(
            "resources" + File.separator +
            "configs" + File.separator +
             fileName);
        File target = getDASDomainXML();

        // we really want to catch this right away
        if (! source.exists()) {
            throw new RuntimeException(String.format(
                "File %s does not exist to copy to config dir",
                source.getAbsolutePath()));
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(source);
            to = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } catch (IOException ex) {
            report("file-copy", false);
            ex.printStackTrace();
            return;
        } finally {
            try {
                from.close();
                to.close();
            } catch (Exception e) {
                /* Could be IOException or NPE if there was an error
                 * above. Either way, let someone know about it....
                 */                
                System.err.println("Some problem closing file streams:");
                e.printStackTrace();
            }
        }
    }
}
