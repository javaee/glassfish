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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.xpath.XPathConstants;

/**
 *
 * @author tmueller
 */
public class DomainTest extends AdminBaseDevTest {

    private final String NUCLEUS_DOMAIN_TEMPLATE_NAME = "nucleus-domain.jar";
    private final String DEFAULT_DOMAIN_TEMPLATE_NAME = "default_domain_template";
    private final String BRANDING_FILE_RELATIVE_PATH = "config" + File.separator + "branding" + File.separator + "glassfish-version.properties";
    private Properties _brandingProperties;

    public DomainTest(){
        init();
    }

    private void init() {
        if (_brandingProperties == null) {
            _brandingProperties = new Properties();
            try {
                File brandingFile = new File(TestEnv.getGlassFishHome(), BRANDING_FILE_RELATIVE_PATH);
                _brandingProperties.load(new FileInputStream(brandingFile));
            } catch (IOException e) {
                System.out.println("Not able to load branding file.");
            } 
        }
    }

    private String getDefaultTemplateName() {
        return _brandingProperties != null ? _brandingProperties.getProperty(DEFAULT_DOMAIN_TEMPLATE_NAME,
                NUCLEUS_DOMAIN_TEMPLATE_NAME) : NUCLEUS_DOMAIN_TEMPLATE_NAME;
    }

    @Override
    protected String getTestDescription() {
        return "Tests domain functionality such as create-domain, etc.";
    }

    public static void main(String[] args) {
        new DomainTest().runTests();
    }

    private void runTests() {
        testCreateDomain();
        testDeleteDomain();
        stat.printSummary();
    }

    /**
     * Test domain creation.
     */
    void testCreateDomain() {
        final String tn = "create-domain-template-";
        File defaultDomainDir = TestEnv.getDefaultTemplateDir();

        // Test domain creation for the default template.
        report(tn + "create-domain1", asadmin("create-domain",
                "--nopassword=true", "--checkports=false", "domt1"));
        report(tn + "check1", checkDomain("domt1", new File(defaultDomainDir, getDefaultTemplateName()).getAbsolutePath()));
        report(tn + "delete-domain1", asadmin("delete-domain", "domt1"));

        File templateJar = new File(TestEnv.getDefaultTemplateDir(), NUCLEUS_DOMAIN_TEMPLATE_NAME);
        // Test domain creation with --template argument.
        if (templateJar.exists()) {
            report(tn + "create-domain2", asadmin("create-domain",
                    "--nopassword=true", "--checkports=false", "--template",
                    templateJar.getAbsolutePath(), "domt2"));
            report(tn + "check2", checkDomain("domt2", templateJar.getAbsolutePath()));
            report(tn + "delete-domain2", asadmin("delete-domain", "domt2"));
        }
    }

    /**
     * Check's the template used to create domain against the given template path.
     *
     * @param name Domain name.
     * @param templatePath absolute template path.
     * @return true if the given template is used for domain creation. 
     */
    boolean checkDomain(String name, String templatePath) {
        File domInfoXml = TestEnv.getDomainInfoXml(name);
        String xpathExpr = "//@location";
        Object o = evalXPath(xpathExpr, domInfoXml, XPathConstants.STRING);
        if (!(o instanceof String && templatePath.equals((String)o))) {
            return false;
        }
        File domainFile = getDASDomainXML(name);
        return domainFile.exists();
    }

    void testDeleteDomain() {
        final String tn = "delete-domain-";
        report(tn + "create", asadmin("create-domain", "foo"));
        report(tn + "baddir", !asadmin("delete-domain", "--domainsdir", "blah", "foo"));
        report(tn + "delete", asadmin("delete-domain", "foo"));
    }
}
