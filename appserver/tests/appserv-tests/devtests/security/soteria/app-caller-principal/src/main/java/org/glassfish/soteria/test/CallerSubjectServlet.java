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

import java.io.IOException;

import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.CallerPrincipal;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

/**
 * The Servlet which validates if for the authenticated user, both
 * container and caller principals are present in the subject
 * representing the caller.
 */
@WebServlet("/callerSubjectServlet")
@ServletSecurity(@HttpConstraint(rolesAllowed = "foo"))
public class CallerSubjectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private SecurityContext securityContext;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String containerCallerPrincipalName = null;
        String appPrincipalName = null;
        String callerPrincipalFromSecurityContextName = null;
        boolean isUserInRole = securityContext.isCallerInRole("foo");
        int callerPrincipalCount = 0;

        Principal containerCallerPrincipal = securityContext.getCallerPrincipal();
        containerCallerPrincipalName = containerCallerPrincipal.getName();

        Set<Principal> principals = securityContext.getPrincipalsByType(java.security.Principal.class);

        Optional<Principal> appCallerPrincipalOptional = principals.stream().filter((p) -> p.getClass().getName() == AppPrincipal.class.getName())
                .findAny();
        Principal appPrincipal = null;
        if (appCallerPrincipalOptional.isPresent()) {
            callerPrincipalCount++;
            appPrincipal = appCallerPrincipalOptional.get();
            appPrincipalName = appPrincipal.getName();
        }

        Optional<Principal> containerCallerPrincipalOptional = principals.stream().filter((p) -> p.getClass().getName() == CallerPrincipal
                .class.getName())
                .findAny();
        Principal callerPrincipalFromSecurityContext = null;
        if (containerCallerPrincipalOptional.isPresent()) {
            callerPrincipalCount++;
            callerPrincipalFromSecurityContext = containerCallerPrincipalOptional.get();
            callerPrincipalFromSecurityContextName = callerPrincipalFromSecurityContext.getName();
        }

        if (!containerCallerPrincipalName.isEmpty() && !appPrincipalName.isEmpty() && containerCallerPrincipalName.equals
                (appPrincipalName) && isUserInRole & callerPrincipalCount == 1) {
            response.getWriter().write(String.format("Container caller principal and application caller principal both are " +
                    "represented by same principal for user %s and is in role %s", containerCallerPrincipal.getName(), "foo"));
        } else {
            response.getWriter().write(String.format("Both %s and %s principal types are available wherein only principal of " +
                    "type %s was expected for user %s and is in role %s",AppPrincipal.class.getName(), CallerPrincipal.class
                            .getName(), AppPrincipal.class.getName(), containerCallerPrincipal.getName(),
                    "foo"));
        }
    }
}
