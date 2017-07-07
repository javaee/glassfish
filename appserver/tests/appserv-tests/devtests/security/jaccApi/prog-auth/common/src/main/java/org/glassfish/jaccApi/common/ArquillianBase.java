/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jaccApi.common;

import static java.lang.Boolean.getBoolean;
import static java.util.logging.Level.SEVERE;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jsoup.Jsoup.parse;
import static org.jsoup.parser.Parser.xmlParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import java.io.File;

import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 *
 * 
 */
public class ArquillianBase {

    private static final String WEBAPP_SRC = "src/main/webapp";
    private static final Logger logger = Logger.getLogger(ArquillianBase.class.getName());
    
    private WebClient webClient;
    private String response;
    
    @Rule
    public TestWatcher ruleExample = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            super.failed(e, description);
            
            logger.log(SEVERE, 
                "\n\nTest failed: " + 
                description.getClassName() + "." + description.getMethodName() +
                
                "\nMessage: " + e.getMessage() +
                
                "\nLast response: " +
                
                "\n\n"  + formatHTML(response) + "\n\n");
            
        }
    };
    
    public static String formatHTML(String html) {
        try {
            return parse(html, "", xmlParser()).toString();
        } catch (Exception e) {
            return html;
        }
    }

    public static Archive<?> defaultArchive() {
        return tryWrapEAR(defaultWebArchive());
    }
    
    public static WebArchive defaultWebArchive() {
        return 
            create(WebArchive.class, "test.war")
                .addPackages(true, "org.javaee7.jaspic")
                .deleteClass(ArquillianBase.class)
                .addAsWebInfResource(resource("web.xml"))
                .addAsWebInfResource(resource("jboss-web.xml"))
                .addAsWebInfResource(resource("glassfish-web.xml"));
    }

    public static WebArchive mavenWar() {
        return create(ZipImporter.class, System.getProperty("finalName") + ".war")
                        .importFrom(new File("target/" + System.getProperty("finalName") + ".war"))
                        .as(WebArchive.class);
    }
    
    public static Archive<?> tryWrapEAR(WebArchive webArchive) {
        if (getBoolean("useEarForJaspic")) {
            return
                // EAR archive
                create(EnterpriseArchive.class, "test.ear")
                
                    // Liberty needs to have the binding file in an ear.
                    // TODO: this is no longer the case and this code can be removed (-bnd.xml
                    // needs to be moved to correct place)
                    .addAsManifestResource(resource("ibm-application-bnd.xml"))
    
                    // Web module
                    // This is needed to prevent Arquillian generating an illegal application.xml
                    .addAsModule(
                        webArchive
                    );  
        } else {
            return webArchive;
        }
    }

    public static File resource(String name) {
        return new File(WEBAPP_SRC + "/WEB-INF", name);
    }
    
    public static File web(String name) {
        return new File(WEBAPP_SRC, name);
    }

    @ArquillianResource
    private URL base;

    @Before
    public void setUp() {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    @After
    public void tearDown() {
        webClient.getCookieManager().clearCookies();
        webClient.close();
    }
    
    

    protected WebClient getWebClient() {
        return webClient;
    }

    protected URL getBase() {
        return base;
    }

    /**
     * Gets content from the path that's relative to the base URL on which the Arquillian test
     * archive is deployed.
     * 
     * @param path the path relative to the URL on which the Arquillian test is deployed
     * @return the raw content as a string as returned by the server
     */
    protected String getFromServerPath(final String path) {
        response = null;
        for (int i=0; i<=3; i++) {
            try {
                response = webClient.getPage(base + path).getWebResponse().getContentAsString();
                if (!response.contains("The response wrapper must wrap the response obtained from getResponse()")) {
                    return response;
                }
            } catch (FailingHttpStatusCodeException | IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        return response;
    }

}
