/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.update.client.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.net.URL;
import java.net.Proxy;
import java.net.HttpURLConnection;

import java.io.IOException;
import java.net.MalformedURLException;
import com.sun.enterprise.update.UpdateFailureException;

/**
 * An util class for the update center module.
 *
 * @author Satish Viswanatham
 */
public class Utils {
    
    /**
     *  This method is used to check the validity of url pattern 
     *  according to the spec. It is used in the following places:
     *
     *  @param conn          http connection to the update center
     *  @param localFile the path to the destination file
     */
    public static void getCatelog(HttpURLConnection conn,String localFile)
        throws UpdateFailureException {

        InputStream in=null;
        String xml = ""; String s = "";
        try{
            conn.setRequestMethod("GET");
            conn.connect();
            in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(in));
            while ((s = reader.readLine()) != null) {
                xml = xml + s + "\n";
            }
        } catch (Exception e) {
            throw new UpdateFailureException(e);
        } finally {
            if (in != null && conn!=null) {
                try {
                    int code = conn.getResponseCode();
                    in.close();
                    conn.disconnect();
                    in = null;
                    conn = null;
                } catch(Exception e) {
                    // log a warning
                }
            }
        }
    }

    public static HttpURLConnection getConnection(String u, Proxy p) throws
    IOException, MalformedURLException {

        URL url = new URL(u);
        HttpURLConnection httpCon = null;
        if (p != null) {
            httpCon = (HttpURLConnection) url.openConnection(p);
        } else {
            httpCon = (HttpURLConnection) url.openConnection();
        }
        
        return httpCon;
    }
}
