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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.xpath.XPathConstants;

/**
 *
 * @author tmueller
 */
public class DomainTest extends AdminBaseDevTest {
    @Override
    protected String getTestDescription() {
        return "Tests domain functionality such as create-domain, etc.";
    }

    public static void main(String[] args) {
        new DomainTest().runTests();
    }

    private void runTests() {
        testCreateDomainTemplate();
        testDeleteDomain();
        stat.printSummary();
    }

    /*
     * This is a test for requirement INFRA-001
     */
    static final String testdom =
            "<domain log-root=\"${com.sun.aas.instanceRoot}/logs\" application-root=\"${com.sun.aas.instanceRoot}/applications\" version=\"10.0\">\n" +
            "  <servers>\n" +
            "    <server name=\"%%%SERVER_ID%%%\" config-ref=\"%%%CONFIG_MODEL_NAME%%%\"/>\n" +
            "  </servers>\n" +
            "  <configs>\n" +
            "    <config name=\"%%%CONFIG_MODEL_NAME%%%\">\n" +
            "      <http-service>\n" +
            "        <virtual-server id=\"__asadmin\" network-listeners=\"admin-listener\"/>\n" +
            "      </http-service>\n" +
            "      <network-config>\n" +
            "        <protocols>\n" +
            "          <protocol name=\"admin-listener\">\n" +
            "            <http default-virtual-server=\"__asadmin\" max-connections=\"250\">\n" +
            "              <file-cache enabled=\"false\" />\n" +
            "            </http>\n" +
            "          </protocol>\n" +
            "        </protocols>\n" +
            "        <network-listeners>\n" +
            "          <network-listener port=\"4849\" protocol=\"admin-listener\" transport=\"tcp\" name=\"admin-listener\"/>\n" +
            "        </network-listeners>\n" +
            "        <transports><transport name=\"tcp\"/></transports>\n" +
            "      </network-config>\n" +
            "    </config>\n" +
            "  </configs>\n" +
            "</domain>\n";

    void testCreateDomainTemplate() {
        final String tn = "create-domain-template-";

        // test a template from the lib/templates directory
        File t1 = new File(getGlassFishHome(), "lib/templates/t1.xml");
        try {
            FileWriter fw = new FileWriter(t1);
            fw.write(testdom);
            fw.close();
            t1.deleteOnExit();
        } catch (IOException ex) {
            report(tn + "t1write", false);
            ex.printStackTrace();
            return;
        }
        report(tn + "create-domain1", asadmin("create-domain",
                "--nopassword=true", "--checkports=false", "--adminport", "4849",
                "--template", "t1.xml", "domt1"));
        report(tn + "check1", checkDomain("domt1"));
        report(tn + "delete-domain1", asadmin("delete-domain", "domt1"));

        // test a template with an absolute pathname
        File t2 = null;
        try {
            t2 = File.createTempFile("t2dom", ".xml");
            FileWriter fw = new FileWriter(t2);
            fw.write(testdom);
            fw.close();
            t2.deleteOnExit();
        } catch (IOException ex) {
            report(tn + "t2write", false);
            ex.printStackTrace();
            return;
        }
        report(tn + "create-domain2", asadmin("create-domain",
                "--nopassword=true", "--checkports=false", "--adminport", "4849",
                "--template", t2.getAbsolutePath(), "domt2"));
        report(tn + "check2", checkDomain("domt2"));
        report(tn + "delete-domain2", asadmin("delete-domain", "domt2"));
    }

    /*
     * Check to see that the only port defined in the domain.xml is 4849. This
     * checks to see that the template we provided was used.
     */
    boolean checkDomain(String name) {
        File domxml = getDASDomainXML(name);
        String xpathExpr = "//@port";
        Object o = evalXPath(xpathExpr, domxml, XPathConstants.STRING);
        System.out.println("o=" + o.toString());
        return o instanceof String && "4849".equals((String)o);

    }

    void testDeleteDomain() {
         final String tn = "delete-domain-";
         report(tn + "create", asadmin("create-domain", "foo"));
         report(tn + "baddir", !asadmin("delete-domain", "--domainsdir", "blah", "foo"));
         report(tn + "delete", asadmin("delete-domain", "foo"));
    }
}
