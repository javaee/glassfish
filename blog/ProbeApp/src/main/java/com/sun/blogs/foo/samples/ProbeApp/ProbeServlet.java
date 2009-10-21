package com.sun.blogs.foo.samples.ProbeApp;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.annotation.Resource;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;

/**
 *
 * @author Byron Nevins
 */

@ProbeProvider (moduleProviderName="fooblog", moduleName="samples", probeProviderName="ProbeServlet")

public class ProbeServlet extends HttpServlet {
    @Resource
    private ProbeProviderFactory probeProviderFactory;

    @Resource
    private ProbeClientMediator listenerRegistrar;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        myProbeMethod1("hello at " + new Date());
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ProbeServlet</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Probe Name is fooblog:samples:ProbeServlet:myProbe1</h1>");
            
            out.println("</body>");
            out.println("</html>");
        } finally { 
            out.close();
        }
    } 

    @Probe(name="myProbe1")
    public void myProbeMethod1(String s) {
        System.out.println("ProbeServlet PROBE-1 here!!! " + s);
    }

    @Override
    public void init() throws ServletException {
        if(probeProviderFactory == null)
            throw new ServletException("ProbeProviderFactory was not injected.");

        if(listenerRegistrar == null)
            throw new ServletException("ProbeClientMediator was not injected.");

        try {
            probeProviderFactory.getProbeProvider(getClass());
            listenerRegistrar.registerListener(new MyProbeListener());
        }
        catch(Exception e) {
            throw new ServletException("Error initializing", e);
        }
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
