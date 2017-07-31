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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.glassfish.soteria.test.ArquillianBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.junit.Rule;
import org.junit.AfterClass;
import org.junit.rules.TestWatcher;

import com.gargoylesoftware.htmlunit.util.Cookie;

import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;

@RunWith(Arquillian.class)
public class AppCustomRememberMeIT extends ArquillianBase {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Security::soteria::AppCustomRememberMe");

    @AfterClass
    public static void printSummary(){
      stat.printSummary();
    }
   
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }


    @Test
    public void testHttpOnlyIsFalse() {
        readFromServer("/servlet?name=reza&password=secret1&rememberme=true");

        assertTrue(getWebClient().getCookieManager().getCookie("JREMEMBERMEID").isHttpOnly());
    }

    @Test
    public void testSecureOnlyIsFalse() {
        readFromServer("/servlet?name=reza&password=secret1&rememberme=true");

        assertFalse(getWebClient().getCookieManager().getCookie("JREMEMBERMEID").isSecure());
    }

    @Test
    public void testAuthenticated() {
        assertDefaultAuthenticated(
            readFromServer("/servlet?name=reza&password=secret1"));
    }
    
    @Test
    public void testNotAuthenticated() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
    }
    
    @Test
    public void testNotAuthenticatedWrongName() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?name=romo&password=secret1"));
    }
    
    @Test
    public void testNotAuthenticatedWrongPassword() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?name=reza&password=wrongpassword"));
    }
    
    @Test
    public void testAuthenticatedRememberMe() {
        
        // 1. Initially request page when we're not authenticated
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
        
        
        // 2. Authenticate without remember me
        
        String response = readFromServer("/servlet?name=reza&password=secret1");
        
        assertDefaultAuthenticated(
            response);
        
        // For the initial authentication, the mechanism should be called
        
        assertTrue(
            "Authentication mechanism should have been called, but wasn't", 
            response.contains("authentication mechanism called: true"));
        
        
        // 3. Request same page again within same http session, without remember me
        //    specified should NOT be authenticated
        
        response = readFromServer("/servlet");
        
        assertDefaultNotAuthenticated(
            response);
        
   
        // 4. Authenticate with remember me
        
        response = readFromServer("/servlet?name=reza&password=secret1&rememberme=true");
        
        assertDefaultAuthenticated(
            response);
        
        // For the initial authentication, the mechanism should be called again
        
        assertTrue(
            "Authentication mechanism should have been called, but wasn't", 
            response.contains("authentication mechanism called: true"));
        
        
        // 5. Request same page again within same http session, with remember me
        //    specified should be authenticated
        
        response = readFromServer("/servlet");
        
        assertDefaultAuthenticated(
            response);
        
        // For the subsequent authentication, the mechanism should not be called again
        // (the remember me interceptor takes care of this)
        
        assertTrue(
            "Authentication mechanism should not have been called, but was", 
            response.contains("authentication mechanism called: false"));
        
        
        // 6. "Expire" the session by removing all cookies except the
        //    remember me cookie
        
        for (Cookie cookie : getWebClient().getCookieManager().getCookies()) {
            if (!"JREMEMBERMEID".equals(cookie.getName())) {
                getWebClient().getCookieManager().removeCookie(cookie);
            }
        }
        
        // Request same page again
        
        response = readFromServer("/servlet");
        
        // Should still be authenticated
        
        assertDefaultAuthenticated(
            response);
        
        // For the subsequent authentication, the mechanism should not be called again
        // (the remember me interceptor takes care of this)
        
        assertTrue(
            "Authentication mechanism should not have been called, but was", 
            response.contains("authentication mechanism called: false"));

        
        // 7. Logout. Should not be authenticated anymore
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?logout=true"));
        
        
        // 8. Request same page again, should still not be authenticated
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
       
    }

}
