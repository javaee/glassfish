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

package org.glassfish.deployment.common;

import java.io.*;
import java.net.*;
import java.util.*;

import sun.misc.BASE64Encoder;

import com.sun.enterprise.deployment.deploy.shared.Archive;
import com.sun.enterprise.deployment.deploy.shared.MemoryMappedArchive;


public class FileUploadUtil {
    private static final String JSR88 = "jsr88";

    /*
     * Alternate upload method.  It will use UploadServlet instead of using JMX MBean.
     * Reason is to increase upload speed.
     * @param host admin hostname
     * @param port admin port
     * @param user admin username
     * @param password admin password
     * @param file upload file path
     * @param name moduleId
     * @return remote file path
    */
    public static String uploadToServlet(String host, String port, String user,
        String password, Archive module) throws Exception {

        Socket socket = null;
        BufferedInputStream bis = null;

        try {

            String fileName = null; 
            if (module instanceof MemoryMappedArchive) {
                // jsr88: module.getArchiveUri() == null
                byte[] bytes = ((MemoryMappedArchive) module).getByteArray();
                bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
                File tmpFile = File.createTempFile(JSR88, null);
                tmpFile.deleteOnExit();
                fileName = tmpFile.getName();
            } else {
                // other deployment mechanisms               
                File f = new File(module.getURI().getPath());
                bis = new BufferedInputStream(new FileInputStream(f));
                fileName = f.getName();
            }

            
            // upload servlet url
            String path = "/web1/uploadServlet?file=" + URLEncoder.encode(fileName, "UTF-8");

            socket = new Socket(host, Integer.parseInt(port));

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // write HTTP headers
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));   
            wr.write("POST " + path + " HTTP/1.0\r\n");
            wr.write("Content-Length: " + bis.available() + "\r\n");
            wr.write("Content-Type: application/octet-stream\r\n");

            // add basic authentication header
            byte[] encodedPassword = (user + ":" + password).getBytes();
            BASE64Encoder encoder = new BASE64Encoder();
            wr.write("Authorization: Basic " + encoder.encode(encodedPassword) + "\r\n");
            wr.write("\r\n");
            wr.flush();

            // write upload file data
            byte[] buffer = new byte[1024*64];
            for (int i = bis.read(buffer); i > 0; i = bis.read(buffer)) {
                out.write(buffer, 0, i);
            }
            out.flush();

            // read HTTP response
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Vector v = new Vector();
            for (int timeoutCounter = 0; timeoutCounter < 120; timeoutCounter++) {
                boolean finished = false;
                while (reader.ready()) {
                    String curLine = reader.readLine();
                    v.add(curLine);
                    if (curLine.startsWith("SUCCESS:")) {
                        finished = true;
                    }
                }
                if (finished) {
                    break;
                } else {
                    Thread.sleep(500);
                }
            }

            if (v.size() == 0) { 
                throw new Exception("Upload file failed: no server response received");    
            }   
            
            // parse HTTP response strings
            boolean isData = false;
            int i = 0;
            String responseString = null;
            for (Enumeration e = v.elements(); e.hasMoreElements(); i++) {
                String curElement = (String) e.nextElement();
                
                // check response code
                if (i == 0) {
                    String responseCode = curElement.substring(curElement.indexOf(" ") + 1);       
                    if (!responseCode.startsWith("200")) {
                        throw new Exception("HTTP connection failed: " + responseCode);                
                    }
                }
                // check start of data
                if (curElement.equals("")) {
                    isData = true;
                    continue;
                }
                // get first line of data
                if (isData) {
                    responseString = curElement;
                    break;
                }
            }

            if (responseString == null) {
                throw new Exception("Upload file failed: no server response received");
            }

            if (!responseString.startsWith("SUCCESS:")) {
                throw new Exception("Upload file failed: " + responseString);
            }

            // parse response string to get remote file path
            String remotePath = responseString.substring(8);

            return remotePath;
                
        } catch (Exception e) {
            throw e;
        } finally { 
            if (socket != null) {
                socket.close();
            }   
            if (bis != null) {
                bis.close();
            }       
        }       
            
    }
            
}        
