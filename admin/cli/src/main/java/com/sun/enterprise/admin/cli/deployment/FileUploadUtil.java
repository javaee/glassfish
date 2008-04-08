/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.cli.deployment;

import java.net.*;
import java.io.*;
import java.util.*;

public class FileUploadUtil {

        /**
         * upload file content to server using http post
         * @param httpConnection - http url
         * @param fileName - file name to upload to server
         * @returns HttpURLConnection - allows the caller of this api to
         *          capture server response and return code
         * @exception Exception - exception from uploading file
         **/
    public static HttpURLConnection upload(final String httpConnection, final File fileName) throws Exception {
        HttpURLConnection urlConnection = null;
        
        try {
            URL url = new URL(httpConnection);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("User-Agent", "hk2-cli");
            urlConnection.connect();

            OutputStream out = urlConnection.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));

            // write upload file data
            byte[] buffer = new byte[1024*64];
            for (int i = bis.read(buffer); i > 0; i = bis.read(buffer)) {
                out.write(buffer, 0, i);
            }
            out.flush();
            if (bis != null) {
                bis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlConnection;  //return null for now
    }

        //main clase used for testing
    public static void main (String [] args) {
        try {
            if (args.length>2) {
                final File fileName = new File(args[2]);
                final String httpConnection = "http://"+args[0]+":"+args[1]+"/__asadmin/deploy?path="+
                                               URLEncoder.encode(fileName.getName(), "UTF-8")+
                                              "?upload="+URLEncoder.encode("true", "UTF-8");
                System.out.println("httpConnection = " + httpConnection);
                FileUploadUtil.upload(httpConnection, fileName);
            } else {
                System.out.println("usage: FileUploadUtil <host> <port> <filename>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
