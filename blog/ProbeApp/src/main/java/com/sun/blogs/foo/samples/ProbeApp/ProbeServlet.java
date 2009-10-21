/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.blogs.foo.samples.ProbeApp;

import com.sun.enterprise.util.StringUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author bnevins
 */

@ProbeProvider (moduleProviderName="fooblog", moduleName="samples", probeProviderName="ProbeServlet")

public class ProbeServlet extends HttpServlet {

    @Resource
    Habitat habitat;

    //@Resource(mappedName="ProbeProviderFactory")
    @Resource
    protected ProbeProviderFactory probeProviderFactory;

    @Resource
    ProbeClientMediator listenerRegistrar;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String msg = "";

        if(habitat == null) {
            msg = "*** NO HABITAT ****<br>";
        }
        if(probeProviderFactory == null) {
            msg += "*** No PPF *****<br><hr>";
        }
        if(ps == null) {
            try {
                if(probeProviderFactory == null)
                    msg += "<h2>PPF is null!!!</h2>";
                else
                    ps = probeProviderFactory.getProbeProvider(ProbeServlet.class);

                if(listenerRegistrar == null)
                    msg += "<h2>listener registrar screwup!!</h2>";
                else
                    listenerRegistrar.registerListener(new MyProbeListener());

                msg += "<h1>getPP() Worked!!!!!</h1>";
            }
            catch(Throwable t) {
                msg = "FAT ERROR!!!!<br>";
                msg += t.getMessage() + "<br>";
                msg += t.toString() + "<br>";
                StackTraceElement[] els = t.getStackTrace();

                msg += "<HR>";
                for(StackTraceElement el : els) {
                    msg += el.toString() + "<br>";
                }
                msg += "<HR>";
            }
        }



            try {
                Iterator<String> it = habitat.getAllContracts();
                msg += "<HR><B>";

                while(it.hasNext())
                    msg += it.next() + "<br>";
                msg += "<HR></B>";
            }
            catch(Throwable t) {
                msg += "<p>Error getting contracts!!</p>";
            }





        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        myProbeMethod1("hello");
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ProbeServlet</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ProbeServlet at " + request.getContextPath () + "</h1>");
            
            if(msg != null)
                out.println(msg);

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


        static {
        }

        static private ProbeServlet ps = null;
}
