/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License.  You can
 *  obtain a copy of the License at
 *  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 *  or packager/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at packager/legal/LICENSE.txt.
 *
 *  GPL Classpath Exception:
 *  Oracle designates this particular file as subject to the "Classpath"
 *  exception as provided by Oracle in the GPL Version 2 section of the License
 *  file that accompanied this code.
 *
 *  Modifications:
 *  If applicable, add the following below the License Header, with the fields
 *  enclosed by brackets [] replaced by your own identifying information:
 *   "Portions Copyright [year] [name of copyright owner]"
 *
 *  Contributor(s):
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package org.glassfish.javaone;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.flashlight.client.ProbeClientMediator;

/**
 *
 * @author Byron Nevins
 */
public class HollaServlet extends HttpServlet {
    @Resource
    private ProbeClientMediator listenerRegistrar;
    private int moviesCount;
    private long started;
    private long elapsed;
    private String[] movieNames;
    private int numGetMovieCalls;
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet HollaServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet HollaServlet at " + request.getContextPath() + "</h1>");
            out.println("Listening to 19120-app");
            out.println("<h3>Number of calls to initMovies() so far: " + moviesCount + "</h3>");
            out.println("<h3>Number of calls to getMovies() so far: " + numGetMovieCalls + "</h3>");
            out.println("<h3>Latest time to run getMovies in nanoseconds: " + elapsed + "</h3>");
            out.println("<h3>Current Movie List: " + Arrays.toString(movieNames) + "</h3>");
            out.println("</body>");
            out.println("</html>");
        }
        finally {
            out.close();
        }
    }

    @ProbeListener("JavaOne:JavaOneSamples:DatabaseSingletonBean:initMovies")
    public void initMovies() {
        System.out.println("initMovies LISTENER HERE.");
        ++moviesCount;
    }

    @ProbeListener("JavaOne:JavaOneSamples:MovieSessionBean:currentMovieList")
    public void currentMovieList(String[] names) {
        movieNames = names;
    }

    @ProbeListener("JavaOne:JavaOneSamples:MovieSessionBean:getMovieStarted")
    public void getMovieStarted() {
        started = System.nanoTime();
        ++numGetMovieCalls;
    }

    @ProbeListener("JavaOne:JavaOneSamples:MovieSessionBean:getMovieFinished")
    public void getMovieFinished() {
        elapsed = System.nanoTime() - started;
    }

    @Override
    public void init() throws ServletException {
        if (listenerRegistrar == null)
            throw new ServletException("ProbeClientMediator was not injected.");

        listenerRegistrar.registerListener(this);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
