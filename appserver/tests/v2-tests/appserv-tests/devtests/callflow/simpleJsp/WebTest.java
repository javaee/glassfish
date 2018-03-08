/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {
    
    private static SimpleReporterAdapter stat
            = new SimpleReporterAdapter("appserv-tests");
    
    private static final String TEST_NAME = "callflow-simple-jsp";
    
    private String host;
    private String port;
    private String contextRoot;
    
    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        WebTest webTest = new WebTest(args);
        try {
            if (args.length == 5){
                if (args[3].equalsIgnoreCase("report")){
                    stat.addDescription("Callflow Simple JSP Test");
                    webTest.analyseResult(args[4]);
                    stat.printSummary(TEST_NAME);
                    
                }
            }else if (args.length == 4){ 
                if (args[3].equalsIgnoreCase ("clean-db")){
			webTest.cleandb ();
		}
            }else
                webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }
    
    public void doTest() throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port
                + contextRoot + "/including.jsp");
        System.out.println("Connecting to: " + url.toString());
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
        }
	conn.disconnect ();
    }
    
    public void analyseResult(String result) throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port
                +"/dbReader/dbReader?servletName=callflow-simple-jsp");
        System.out.println("Analysing Result .... Connecting to: " + url.toString());
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
	    System.out.println ("Expected Result :" + result);
            System.out.println ("Actual Result   :" + line);
	    if(result.equals (line))
            	stat.addStatus(TEST_NAME, stat.PASS);
	    else
		stat.addStatus(TEST_NAME, stat.FAIL);		

        }
	conn.disconnect ();
    }

    public void cleandb() throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port
                +"/dbReader/dbReader?cleandb=true");
        System.out.println("Cleaning DB .... Connecting to: " + url.toString());
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
	    System.out.println (line);
	    System.out.println (input.readLine());
	}
	
	conn.disconnect ();
    } 
}
