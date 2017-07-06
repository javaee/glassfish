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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import com.gargoylesoftware.htmlunit.WebResponse;

public final class Assert {
    
    public static void assertDefaultAuthenticated(String response) {
        assertAuthenticated("web", "reza", response, "foo", "bar");
    }

    public static void assertDefaultAuthenticated(WebResponse response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertAuthenticated("web", "reza", response.getContentAsString(), "foo", "bar");
    }
    
    public static void assertDefaultNotAuthenticated(String response) {
        assertNotAuthenticated("web", "reza", response, "foo", "bar");
    }

    public static void assertDefaultNotAuthenticated(WebResponse response) {
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertNotAuthenticated("web", "reza", response.getContentAsString(), "foo", "bar");
    }

    public static void assertDefaultNotAuthenticatedUnprotected(WebResponse response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotAuthenticatedUnprotected("web", "null", response.getContentAsString(), new ArrayList<String>());
    }

    public static void assertNotAuthenticatedError(WebResponse response) {
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
    }
    
    public static void assertAuthenticated(String userType, String name, String response, String... roles) {
        assertTrue(
            "Should be authenticated as user " + name + " but was not \n Response: \n" + 
            response + "\n search: " + userType + " username: " + name,
            response.contains(userType + " username: " + name));
        
        for (String role : roles) {
            assertTrue(
                "Authenticated user should have role \"" + role + "\", but did not \n Response: \n" + 
                response,
                response.contains(userType + " user has role \"" + role + "\": true"));
        }
    }
    
    public static void assertNotAuthenticated(String userType, String name, String response, String... roles) {
        assertFalse(
            "Should not be authenticated as user " + name + " but was \n Response: \n" + 
            response + "\n search: " + userType + " username: " + name,
            response.contains(userType + " username: " + name));
        
        for (String role : roles) {
            assertFalse(
                "Authenticated user should not have role \"" + role + "\", but did \n Response: \n" + 
                response,
                response.contains(userType + " user has role \"" + role + "\": true"));
        }
     }

    public static void assertAuthenticatedRoles(String userType, String response, String... roles) {
        for (String role : roles) {
            assertTrue(
                    "Authenticated user should have role \"" + role + "\", but did not \n Response: \n" +
                            response,
                    response.contains(userType + " has role \"" + role + "\": true"));
        }
    }

    public static void assertNotAuthenticatedRoles(String userType, String name, String response, String... roles) {

        for (String role : roles) {
            assertFalse(
                    "Authenticated user should not have role \"" + role + "\", but did \n Response: \n" +
                            response,
                    response.contains(userType + " has role \"" + role + "\": true"));
        }
    }
    public static void assertNotAuthenticatedUnprotected(String userType, String name, String response, List<String> roles) {
        assertTrue(
                "Should not be authenticated as user " + name + " but was \n Response: \n" +
                        response + "\n search: " + userType + " username: " + name,
                response.contains(userType + " username: " + name));

        for (String role : roles) {
            assertFalse(
                    "Authenticated user should not have role \"" + role + "\", but did \n Response: \n" +
                            response,
                    response.contains(userType + " user has role \"" + role + "\": true"));
        }
    }

    public static void assertHasAccessToResource(String userType, String name, String resource, String response) {
        assertTrue(
                "user " + name + " should have access to resource "+ resource +" but was not \n Response: \n" +
                        response,
                response.contains(userType + " user has access to " + resource + ": true"));
    }

    public static void assertNotHasAccessToResource(String userType, String name, String resource, String response) {
        assertFalse(
                "user " + name + " should have access to resource "+ resource +" but was not \n Response: \n" +
                        response,
                response.contains(userType + " user has access to " + resource + ": true"));
    }

}
