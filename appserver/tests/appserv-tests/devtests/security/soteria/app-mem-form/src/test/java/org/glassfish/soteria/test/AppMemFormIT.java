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

package org.glassfish.soteria.test;

import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


@RunWith(Arquillian.class)
public class AppMemFormIT extends ArquillianBase {
    
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }

    @Test
    public void testAuthenticated() throws IOException {
        
        
        // 1. Initially request protected page when we're not authenticated
        
        HtmlPage loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the correct credentials
        
        HtmlForm form = loginPage.getForms().get(0);
        
        form.getInputByName("j_username")
            .setValueAttribute("reza");
        
        form.getInputByName("j_password")
            .setValueAttribute("secret1");
        
        HtmlPage page = form.getInputByValue("Submit")
                            .click();
        
        // Has to be authenticted now
        assertDefaultAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
        
        // 4. Request page again. FORM is stateful (http session bound) so
        // still has to be authenticated.
        
        page = pageFromServer("/servlet");
        
        System.out.println("+++++++++++STEP 4 +++++++++++++ (before assertDefaultAuthenticated) \n\n\n\n" + page.getWebResponse()
        .getContentAsString());
        
        assertDefaultAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
        
        // 5. Logout
        
        System.out.println("*** STEP 5 ***** (before get logout) " + page.asXml());
        
        page = page.getForms()
                   .get(0)
                   .getInputByValue("Logout")
                   .click();
        
        // Has to be logged out now (page will still be rendered, but with 
        // web username null and no roles.
        
        assertDefaultNotAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
        
        
        // 6. Request page again. Should still be logged out
        // (and will display login to continue again now)
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
        
    }
    
    @Test
    public void testNotAuthenticatedWrongName() throws IOException {
        
        // 1. Initially request protected page when we're not authenticated
        
        HtmlPage loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the correct credentials
        
        HtmlForm form = loginPage.getForms().get(0);
        
        form.getInputByName("j_username")
            .setValueAttribute("romo");
        
        form.getInputByName("j_password")
            .setValueAttribute("secret1");
        
        HtmlPage page = form.getInputByValue("Submit")
                            .click();
        
        assertTrue(
            "The error page should have been displayed, but was not",
            page.getWebResponse().getContentAsString().contains("Login failed!")
        );
        
        // Should not be authenticted now
        assertDefaultNotAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
    }
    
    @Test
    public void testNotAuthenticatedWrongPassword() throws IOException {
        
        // 1. Initially request protected page when we're not authenticated
        
        HtmlPage loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the *wrong* credentials
        
        HtmlForm form = loginPage.getForms().get(0);
        
        form.getInputByName("j_username")
            .setValueAttribute("reza");
        
        form.getInputByName("j_password")
            .setValueAttribute("wrongpassword");
        
        HtmlPage page = form.getInputByValue("Submit")
                            .click();
        
        assertTrue(
            "The error page should have been displayed, but was not",
            page.getWebResponse().getContentAsString().contains("Login failed!")
        );
        
        // Should not be authenticted now
        assertDefaultNotAuthenticated(
            page.getWebResponse()
                .getContentAsString());
       
    }
    
    @Test
    public void testNotAuthenticatedInitiallyWrongNameThenCorrect() throws IOException {
        
        // 1. Initially request protected page when we're not authenticated
        
        HtmlPage loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the correct credentials
        
        HtmlForm form = loginPage.getForms().get(0);
        
        form.getInputByName("j_username")
            .setValueAttribute("romo");
        
        form.getInputByName("j_password")
            .setValueAttribute("secret1");
        
        HtmlPage errorPage = form.getInputByValue("Submit")
                            .click();
        
        // Should not be authenticted now
        assertDefaultNotAuthenticated(
            errorPage.getWebResponse()
                     .getContentAsString());
        
        
        // 4. Request login page directly, and now submit with the correct credentials
        // (note that the initial target URL of /servlet should still be remembered)
        
        loginPage = pageFromServer("/login-servlet");
        
        form = loginPage.getForms().get(0);
        
        form.getInputByName("j_username")
            .setValueAttribute("reza");
        
        form.getInputByName("j_password")
            .setValueAttribute("secret1");
        
        HtmlPage page = form.getInputByValue("Submit")
                            .click();
        
        // Has to be authenticted now
        assertDefaultAuthenticated(
            page.getWebResponse()
                .getContentAsString());
    }

}
