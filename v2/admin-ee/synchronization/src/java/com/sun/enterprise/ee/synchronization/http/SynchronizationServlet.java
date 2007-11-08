/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.ee.synchronization.http;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.ee.synchronization.Synchronization;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.processor.RequestContext;
import com.sun.enterprise.ee.synchronization.processor.ServletProcessor;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import java.io.FileNotFoundException;
import javax.servlet.ServletException;
import com.sun.enterprise.config.ConfigException;
import java.io.UnsupportedEncodingException;


/**
 * Synchronization servlet. This runs on DAS and responds to synchronization
 * requests over HTTP.
 *
 * @author Nazrul Islam
 */
public class SynchronizationServlet extends HttpServlet {
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and 
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter(SynchronizationRequest.ACTION);
        if (SynchronizationRequest.SYNCHRONIZE.equals(action)) {
            synchronize(request, response);
        } else if (SynchronizationRequest.GET.equals(action)) {
            get(request, response);
        } else {
            ping(request, response);
        }
    }

    /**
     * Handles a ping request.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void ping(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("ALIVE");
        out.close();
    }


    /**
     * Streams all files from given directory. It expects a "file" parameter
     * that points to the target file or directory.
     * 
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void get(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Content-Type", "application/zip");
        String file = request.getParameter("file");
        File target = new File(file);
        
        ServletOutputStream sout = response.getOutputStream();
        ZipOutputStream out = new ZipOutputStream(sout);

        addToZip(target, out);
        out.close();
    }

    /**
     * Adds the given file or directory content to the zip output stream.
     *
     * @param  target  file or directory to be added to the zip
     * @param  out     zip output stream from the zip
     */
    private void addToZip(File target, ZipOutputStream out) throws IOException {

        if (target.isDirectory()) {
            File[] list = target.listFiles();

            for (int i=0; i<list.length; i++) {
                if (list[i].isDirectory()) {
                    addToZip(list[i], out);
                } else {
                    addFiletoZip(list[i], out);
                }
            }
        } else {
            addFiletoZip(target, out);
        }
    }

    /**
     * Handles a synchronization request.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void synchronize(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {

        // config context
        ConfigContext configCtx = AdminService.getAdminService().
                            getAdminContext().getAdminConfigContext();

        // synchronization request from HTTP request
        SynchronizationRequest[] sreq = null;
        try {
            sreq = new SynchronizationRequest[] {
                    new SynchronizationRequest(request) }; 
        } catch (UnsupportedEncodingException enEx) {
            throw new ServletException(enEx);
        }

        // synchronization request context
        RequestContext rCtx = new RequestContext(configCtx, sreq);
        rCtx.setTimeDelta(Synchronization._delta);

        try {
            // processor responds to this request
            ServletProcessor processor = new ServletProcessor(rCtx, response);
            processor.process();
        } catch (ConfigException ce) {
            throw new ServletException(ce);
        }
    }

    /**
     * Streams the given file to HTTP response. The method that calls this 
     * method should add proper content type.
     *
     * @param  file  file to be sent out
     * @param  out   servlet output stream
     */
    private void streamFile(String file, ServletOutputStream out) 
            throws FileNotFoundException, IOException {

        FileInputStream in = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(in);
        
        byte[] buf = new byte[BUFFER_SIZE];
        int read = bin.read(buf, 0, BUFFER_SIZE);
        while (read != -1) {
            out.write(buf, 0, read);
            read = bin.read(buf, 0, BUFFER_SIZE);
        }
        in.close();
    }
    
    /**
     * Adds a file to the zip as a zip entry.
     *
     * @param  file  file to be added to the zip
     * @param  out   output stream of the zip 
     */
    private void addFiletoZip(File file, ZipOutputStream out) 
            throws FileNotFoundException, IOException {

        ZipEntry entry = null;
        BufferedInputStream origin = null;
        byte data[] = new byte[BUFFER_SIZE];
        int count;

        try {
            origin = new BufferedInputStream(new FileInputStream(file),
                                            BUFFER_SIZE);
            entry = new ZipEntry(file.getName());
            entry.setTime(file.lastModified());
            out.putNextEntry(entry);

            try {
                while ((count=origin.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
                origin = null;
            } finally {
                out.closeEntry();
            }
        } finally {
            if (origin != null) {
                try {
                    origin.close();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
    }
    
    /** 
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** 
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Synchronization Servlet";
    }
    
    // ---- VARIABLES - PRIVATE ------------------------------------
    private static final int BUFFER_SIZE = 131072; // 128 kb
    private static final StringManager _localStrMgr = 
            StringManager.getManager(SynchronizationServlet.class);
    private static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);
}
