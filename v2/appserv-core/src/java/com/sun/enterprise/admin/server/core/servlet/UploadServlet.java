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

package com.sun.enterprise.admin.server.core.servlet;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.zip.*;
import java.util.logging.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 *
 * This servlet is used to upload files to application server tmp directory
 */
public class UploadServlet extends HttpServlet {

    private static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // check if fileName is valid
        String str = request.getQueryString();
        Properties p = new Properties();
        try {
            getQueryProperties(str, p);
        } catch (Exception e) {
            writeErrorResponse(response, e.getMessage());
            return;
        }
        String fileName = p.getProperty("file");

        if (fileName == null || fileName.trim().equals("")) {
            writeErrorResponse(response, "no fileName found");
            return;
        }

        // use tmp directory specified in domain.xml
        File localDir = new File(AdminService.getAdminService().getTempDirPath());
        localDir.mkdirs();

        // check pre-existing file
        File uploadFile = new File(localDir, fileName);
        if (uploadFile.exists()) {
            sLogger.log(Level.INFO, "mbean.temp_upload_file_exists", uploadFile.getCanonicalPath());
            if (!uploadFile.delete()) {
                sLogger.log(Level.INFO, "mbean.delete_temp_file_failed", uploadFile.getCanonicalPath());
                writeErrorResponse(response, "cannot delete existing file");
                return;
            }
            sLogger.log(Level.FINE, "mbean.delete_temp_file_ok", uploadFile.getCanonicalPath());
        }   
        
        sLogger.log(Level.INFO, "mbean.begin_upload", uploadFile.getCanonicalPath());
        
            
        // read data from inputstream
        FileOutputStream fos = null;
        InputStream is = null;
        
        try {
            fos = new FileOutputStream(uploadFile);
            is = request.getInputStream();
            byte[] buffer = new byte[1024*64];
            
            for (int i = is.read(buffer); i > 0; i = is.read(buffer)) {
                fos.write(buffer, 0, i);
            }
        } catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.upload_failed", uploadFile.getCanonicalPath());
            writeErrorResponse(response, "uploading file failed");
            return;
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (is != null) {
                is.close();
            }
        }

        // write remoteFilePath to HTTP response
        writeResponse(response, uploadFile.getCanonicalPath());
        return;

    }

    private void writeResponse(HttpServletResponse response, String msg)
        throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setBufferSize(8192);
        PrintWriter writer = response.getWriter();
        writer.println("SUCCESS:" + msg);
        writer.close();
    }           
            
    private void writeErrorResponse(HttpServletResponse response, String errorMsg)             
        throws IOException {
        response.setContentType("text/html");
        response.setBufferSize(8192);
        PrintWriter writer = response.getWriter();
        writer.println("FAIL:" + errorMsg);
        writer.close();
    }
        

    public void getQueryProperties(String q, Properties toAddTo) throws Exception {
        if (q == null || q.length() == 0) {
            return;
        }
        for (StringTokenizer iter = new StringTokenizer(q, "&");
            iter.hasMoreElements();/*-*/) {
            String pair = (String) iter.nextToken();
            int split = pair.indexOf('=');
            if (split <= 0) {
                throw new Exception ("Invalid pair [" + pair
                    + "] in query string [" + q + "]");
            } else {
                String key = pair.substring(0, split);
                String value = pair.substring(split + 1);
                try {
                    key = URLDecoder.decode(key, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new Exception("Invalid encoding in [" + pair
                        + "] in query string [" + q + "]" + e.getMessage());
                }
                toAddTo.setProperty(key, value);
            }
        }
    }

}
