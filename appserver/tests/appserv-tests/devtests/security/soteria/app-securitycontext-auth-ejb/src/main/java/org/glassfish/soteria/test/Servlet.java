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

import org.glassfish.soteria.SecurityContextImpl;

import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.credential.CallerOnlyCredential;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import javax.inject.Inject;

import static javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import static org.glassfish.soteria.Utils.notNull;

/**
 * Test Servlet that prints out the name of the authenticated caller and whether
 * this caller is in any of the roles {foo, bar, kaz}
 */
@DeclareRoles({"foo", "bar", "kaz"})
@WebServlet("/servlet")
public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private TestEJB bean;
    @Inject
    private SecurityContext securityContext;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("This is a servlet \n");
        String name = request.getParameter("name");

        if (notNull(name)) {

            AuthenticationStatus status = securityContext.authenticate(
                    request, response,
                    withParams()
                            .credential(
                                    new CallerOnlyCredential(name)));

            response.getWriter().write("Authenticated with status: " + status.name() + "\n");
        }

        String ejbName = null;
        if (bean.getUserPrincipalFromEJBContext() != null) {
            ejbName = bean.getUserPrincipalFromEJBContext().getName();
        }

        response.getWriter().write("ejb username: " + ejbName + "\n");

        response.getWriter().write("ejb user has role \"foo\": " + bean.isCallerInRoleFromEJBContext("foo") + "\n");
        response.getWriter().write("ejb user has role \"bar\": " + bean.isCallerInRoleFromEJBContext("bar") + "\n");
        response.getWriter().write("ejb user has role \"kaz\": " + bean.isCallerInRoleFromEJBContext("kaz") + "\n");

        String contextName = null;
        if (bean.getUserPrincipalFromSecContext() != null) {
            contextName = bean.getUserPrincipalFromSecContext().getName();
        }

        response.getWriter().write("context username: " + contextName + "\n");

        response.getWriter().write("context user has role \"foo\": " + bean.isCallerInRoleFromSecContext("foo") + "\n");
        response.getWriter().write("context user has role \"bar\": " + bean.isCallerInRoleFromSecContext("bar") + "\n");
        response.getWriter().write("context user has role \"kaz\": " + bean.isCallerInRoleFromSecContext("kaz") + "\n");

        response.getWriter().write("web user has access to /protectedServlet: " + securityContext.hasAccessToWebResource("/protectedServlet") + "\n");

        Set<String> roles = bean.getAllDeclaredCallerRoles();

        response.getWriter().write("All declared roles of user " + roles + "\n");

        response.getWriter().write("all roles has role \"foo\": " + roles.contains("foo") + "\n");
        response.getWriter().write("all roles has role \"bar\": " + roles.contains("bar") + "\n");
        response.getWriter().write("all roles has role \"kaz\": " + roles.contains("kaz") + "\n");
    }


    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
