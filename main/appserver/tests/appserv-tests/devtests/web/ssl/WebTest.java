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

    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String trustStorePath = args[3];

        stat.addDescription("Testing Filter chain under SSL");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);
            HttpsURLConnection connection = doSSLHandshake(
                            "https://" + host  + ":" + port + "/", ssf);
            checkStatus(connection); 

            connection = doSSLHandshake(
                "https://" + host  + ":" + port + "/" + contextRoot 
                + "/ServletTest", ssf);
            
            parseResponse(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("ssl", stat.FAIL);
        }           

        stat.printSummary("web/ssl ---> expect 4 PASS");
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

    private static void checkStatus(HttpsURLConnection connection)
                    throws Exception{

        int responseCode=  connection.getResponseCode();
        System.out.println("Response code: " + responseCode + " Expected code: 200"); 
        if (connection.getResponseCode() != 200){
            stat.addStatus("ssl-responseCode", stat.FAIL);
        } else {
            stat.addStatus("ssl-responseCode", stat.PASS);
        }
    }

    private static void parseResponse(HttpsURLConnection connection)
                    throws Exception{

        BufferedReader in = null;
            
        String line = "";
        int index;
        try {
            String header = connection.getHeaderField("Set-Cookie");
            System.out.println(header);
            if (!header.contains("Secure")) {
                stat.addStatus("ssl-FILTER", stat.FAIL);
                return;
            }

            in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

            while ((line = in.readLine()) != null) {
                index = line.indexOf("FILTER");
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);

                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("ssl-" + line.substring(0,index),
                                       stat.PASS);
                    } else {
                        stat.addStatus("ssl-FILTER", stat.FAIL);
                    }
                    count++;
                } 
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
