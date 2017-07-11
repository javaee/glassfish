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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.junit.Rule;
import org.junit.AfterClass;
import org.junit.rules.TestWatcher;


@RunWith(Arquillian.class)
public class AppSecurityContextAuthIT extends ArquillianBase {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Security::soteria::AppSecurityContextServlet");

    @AfterClass
    public static void printSummary(){
        stat.printSummary();
    }
    
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }

    @Test
    public void testAuthenticated() {
        assertDefaultAuthenticated(
            readFromServer("/servlet?name=reza"));
    }
    
    @Test
    public void testAuthenticatedStatus() {
        assertTrue(
            readFromServer("/servlet?name=reza")
                .contains("Authenticated with status: SUCCESS"));
    }
    
    /**
     * The name "rezax" will cause the custom authentication provider
     * to throw an auth exception, which should ultimately result in
     * a SEND_FAILURE outcome from SecurityContext.authenticate.
     */
    @Test
    public void testAuthenticatedStatusException() {
        assertTrue(
            readFromServer("/servlet?name=rezax")
                .contains("Authenticated with status: SEND_FAILURE"));
    }
    
    /**
     * The name "unknown" will cause the custom authentication provider
     * to return SEND_FAILURE, which should ultimately result in
     * a SEND_FAILURE outcome from SecurityContext.authenticate as well.
     */
    @Test
    public void testAuthenticatedStatusFail() {
        assertTrue(
            readFromServer("/servlet?name=unknown")
                .contains("Authenticated with status: SEND_FAILURE"));
    }

    @Test
    public void testContextAuthenticated() {
        Assert.assertAuthenticated(
            "context",
            "reza",
            readFromServer("/servlet?name=reza"));
    }

    @Test
    public void testContextIsCallerInRole(){
        Assert.assertAuthenticated(
                "context",
                "reza",
                readFromServer("/servlet?name=reza"), "foo", "bar");
    }

    @Test
    public void testContextAllCallers(){
        Assert.assertAuthenticatedRoles(
                "all roles",
                readFromServer("/servlet?name=reza"), "foo", "bar");
    }

    @Test
    public void testContextHasAccessToResource(){
        Assert.assertHasAccessToResource(
                "web",
                "reza",
                "/protectedServlet",
                readFromServer("/servlet?name=reza"));
    }
    
    @Test
    public void testNotAuthenticated() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
    }
    
  
    

}
