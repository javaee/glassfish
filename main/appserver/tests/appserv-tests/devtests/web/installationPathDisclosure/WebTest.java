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
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Bugtraq 5047700 Installation Path Disclosure
 */
public class WebTest{

    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    private static URLConnection conn = null;
    private static URL url;
    
    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try{
            stat.addDescription("Basic Host/Context mapping");
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + "///BREAK");
            String originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection)conn;
                urlConnection.setDoOutput(true);

                writeOneByte(urlConnection);

                int responseCode=  urlConnection.getResponseCode();
                System.out.println("installationPathDisclosure: " + responseCode + " Expected code: 40X"); 
                if (urlConnection.getResponseCode() >= 400 && urlConnection.getResponseCode() < 500){
                    stat.addStatus("Test installationPathDisclosure", stat.PASS);
                } else {
                    stat.addStatus("Test installationPathDisclosure", stat.FAIL);
                }
            }
            url = new URL("http://" + host  + ":" + port + "/BREAK////");
            originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection)conn;
                urlConnection.setDoOutput(true);

                writeOneByte(urlConnection);

                int responseCode=  urlConnection.getResponseCode();
                System.out.println("installationPathDisclosure: " + responseCode + " Expected code: 40X"); 
                if (urlConnection.getResponseCode() >= 400 && urlConnection.getResponseCode() < 500){
                    stat.addStatus("Test installationPathDisclosure-wrongUrl", stat.PASS);
                } else {
                    stat.addStatus("Test installationPathDisclosure-wrongUrl", stat.FAIL);
                }
            }
            stat.printSummary("web/installationPathDisclosure");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void writeOneByte(HttpURLConnection urlConnection) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(urlConnection.getOutputStream());
            out.writeByte(1);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }
    }
}
