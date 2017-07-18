/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package client;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.Map;
import java.util.Iterator;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author dochez
 */

public class Client {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");    

    public static void main(String[] args) {
        stat.addDescription("webservices-filter-url-handling");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-filter-url-handling");        
    }

    public void doTest(String[] args) {
        try {
            String webURL = args[0];            
            URL url = new URL(args[0] + "//");
            System.out.println("Invoking " + url.toExternalForm());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            System.out.println("Response code " + connection.getResponseCode());
            if (HttpURLConnection.HTTP_NOT_FOUND == responseCode) {
                // passed
                System.out.println("Passed, page is not found");
                stat.addStatus("webservice filter url handling", stat.PASS);
                return;
            }
            if (HttpURLConnection.HTTP_INTERNAL_ERROR == responseCode) {
                // server not started
                System.out.println("Error, server is down ?");
                stat.addStatus("webservice filter url handling", stat.FAIL);        
                return;
            }
            if (HttpURLConnection.HTTP_OK == responseCode) {
               // failed
                System.out.println("Got a page back, check it is a valid one...");
                int length = connection.getContentLength();
                System.out.println("Content-length " + length);
                if (length==-1) {
                    stat.addStatus("webservice filter url handling", stat.FAIL);                                        
                } else {
                    stat.addStatus("webservice filter url handling", stat.PASS);                                        
                }
                stat.addStatus("webservice filter url handling", stat.FAIL);                        
            } else {
                System.out.println("ERROR - Unknow return code " + responseCode);
                Map map = connection.getHeaderFields();
                for (Iterator itr=map.keySet().iterator();itr.hasNext();) {
                    String header = (String) itr.next();
                    System.out.println("Header " + header + "-->" + map.get(header));
                }   
                stat.addStatus("webservice filter url handling", stat.FAIL);        
            }
        } catch(Exception e) {
            System.out.println("Errror - exception " + e.getMessage());
            stat.addStatus("webservice filter url handling", stat.FAIL);        
        }
    }    
}
