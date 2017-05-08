/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import java.io.*;
import java.util.Properties;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebTest{


    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 3;
    private static boolean firstConnection = true;
    private static String requestedSessionId="";
    private static String requestUri = "" ;
    private static String sessionFalseId = "";

    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String trustStorePath = args[3];

        stat.addDescription("Cookie under SSL");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);

            HttpsURLConnection connection = doSSLHandshake(
                            "https://" + host  + ":" + port + "/" + contextRoot
                            + "/ServletTest;jsessionid=01A960C22480CE9F445CDE48DE333F31", ssf);

            parseResponse(connection);
            
            firstConnection = false;
            connection = doSSLHandshake(
                "https://" + host  + ":" + port + "/" + contextRoot 
                + "/ServletTest;jsessionid=" + sessionFalseId, ssf);
            parseResponse(connection);
            
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (count != EXPECTED_COUNT){
                stat.addStatus("web-sslCookie", stat.FAIL);
            }           
        }


        stat.printSummary("web/sslCookie ---> expect 3 PASS");
    }

    private static SSLSocketFactory getSSLSocketFactory(String trustStorePath)
                    throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private static HttpsURLConnection doSSLHandshake(String urlAddress,
                                                     SSLSocketFactory ssf)
                    throws Exception{

        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String rserver, SSLSession sses) {
                    return true;
                }
        });
        connection.setDoOutput(true);
        return connection;
    }

    private static int lineNum = 0;

    private static void parseResponse(HttpsURLConnection connection)
                    throws Exception{

        BufferedReader in = null;
            
        String line= ""; 
        int index = 0;
        try {
            in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            while ((line = in.readLine()) != null) {
                index = line.indexOf("::");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                    String status = line.substring(index+2);
                    String context = line.substring(0, index);
                    System.out.println("context: " + context + " status: " + status);
                    if (firstConnection){
                        if ( context.equalsIgnoreCase("getRequestSessionId") )
                            requestedSessionId = status; 
                        else if ( context.equalsIgnoreCase("getSession(false).getId")) {
                            sessionFalseId = status;
                        } else if ( context.equalsIgnoreCase("getRequestURI"))
                            requestUri = status;
                    } else {
                        if ( context.equalsIgnoreCase("getRequestSessionId") ) {
                            System.out.println(requestedSessionId + " (1) " + status);
                            if (sessionFalseId.equalsIgnoreCase(status)) {
                                stat.addStatus("web-sslCookie: " + context,
                                               stat.PASS);
                            } else {
                                stat.addStatus("web-sslCookie: getRequestSessionId", stat.FAIL);
                            }
                        } else if ( context.equalsIgnoreCase("getSession(false).getId")){
                            System.out.println(sessionFalseId + " (2) " + status);
                            if (sessionFalseId.equalsIgnoreCase(status)) {
                                stat.addStatus("web-sslCookie: " + context,
                                               stat.PASS);
                            } else {
                                stat.addStatus("web-sslCookie: getSession(false).getId", stat.FAIL);
                            }
                        } else if ( context.equalsIgnoreCase("getRequestURI")) {
                            System.out.println(requestUri + " (3) " + status);
                            if (requestUri.equalsIgnoreCase(status)) {
                                stat.addStatus("web-sslCookie: " + context,
                                               stat.PASS);
                            } else {
                                stat.addStatus("web-sslCookie: getRequestURI", stat.FAIL);
                            }
                        }
                        count++;
                    } 
                }
                lineNum++;
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }

        in.close();
    }

    private static TrustManager[] getTrustManagers(String path)
                    throws Exception {

        TrustManager[] tms = null;
        InputStream istream = null;

        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            istream = new FileInputStream(path);
            trustStore.load(istream, null);
            istream.close();
            istream = null;
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            tms = tmf.getTrustManagers();

        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }

        return tms;
    }

}
