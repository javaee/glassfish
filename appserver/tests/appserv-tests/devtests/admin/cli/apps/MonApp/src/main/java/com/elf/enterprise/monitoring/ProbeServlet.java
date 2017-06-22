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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elf.enterprise.monitoring;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.glassfish.flashlight.provider.ProbeRegistry;

/**
 * @author Byron Nevins
 */
@ProbeProvider(moduleProviderName = "fooblog", moduleName = "samples", probeProviderName = "ProbeServlet")
public class ProbeServlet extends HttpServlet {
    @Resource
    private ProbeProviderFactory probeProviderFactory;
    @Resource
    private ProbeClientMediator listenerRegistrar;
    @Resource
    private ProbeRegistry probeRegistry;
    private PrintWriter out;
    private ProbeInterface probeInterface;

    @Probe(name = "myProbe")
    public void myProbe(String s) {
        System.out.println("inside myProbeMethod called with this arg: " + s);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        myProbe("Hello #" + counter.incrementAndGet());
        Random random = new Random();
        probeInterface.myProbe2("random: " + random.nextDouble(), "random: " + random.nextInt());
        response.setContentType("text/html;charset=UTF-8");
        out = response.getWriter();
        try {
            pr("<html>");
            pr("<head>");
            pr("<title>Servlet ProbeServlet</title>");
            pr("</head>");
            pr("<body>");
            pr("<h2>All Probes</h2>");
            pr("<ul>");
            // KISS
            for (Iterator<String> it = getAllProbes().iterator(); it.hasNext();) {
                String s = it.next();
                
                if(s.startsWith("fooblog"))
                    s = "<b>" + s + "</b>";
                
                pr("<li>" + s + "</li>");
            }
            pr("</ul>");
            pr("</body>");
            pr("</html>");
        }
        finally {
            out.close();
        }
    }

    @Override
    public void init() throws ServletException {
        if (probeProviderFactory == null)
            throw new ServletException("ProbeProviderFactory was not injected.");

        if (listenerRegistrar == null)
            throw new ServletException("ProbeClientMediator was not injected.");

        try {
            // need to get the probe provider registered before the listener!
            probeProviderFactory.getProbeProvider(getClass());
            probeInterface = probeProviderFactory.getProbeProvider(ProbeInterface.class);
            listenerRegistrar.registerListener(new MyProbeListener());

            StatsProviderManager.register("cloud", PluginPoint.SERVER, "fooblog/samples/ProbeServlet", new MyProbeListener());
        }
        catch (Exception e) {
            throw new ServletException("Error initializing", e);
        }

        if (probeInterface == null)
            throw new ServletException("ProbeInterface was not instantiated as expected.");
    }

    private void pr(String s) {
        out.println(s);
    }

    private Set<String> getAllProbes() {
        Collection<FlashlightProbe> probes = probeRegistry.getAllProbes();
        NavigableSet sorted = new TreeSet<String>();

        for (FlashlightProbe flp : probes) {
            String probeString = flp.toString();
            sorted.add(probeString);
        }

        if (sorted.isEmpty()) {
            sorted.add("No Probes found!");
        }
        return sorted;
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
    private final static AtomicInteger counter = new AtomicInteger();
}
