/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package util;

import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/** WebTestUtil.java
  * This program opens HttpURLconnection,sends the request to the 
  * servlet , & receives the response from the servlet.
  * Using commandline args the user can specify for WebTestUtil
  * 1. test suite name
  * 2. host name
  * 3. port no
  * 4. context root of the servlet that is defined in web.xml
  * 5. url pattern of the servlet that is defined in web.xml
  *
  * @author      Sarada Kommalapati
  */


public class WebTestUtil {

    private SimpleReporterAdapter stat;

    private String testSuiteID;
    private String TEST_NAME;
    private String host;
    private String port;
    private String contextRoot;
    private String urlPattern;


    public WebTestUtil( String host, String port, String contextRoot , String urlPattern, String testSuiteID, SimpleReporterAdapter stat) {
        this.testSuiteID = testSuiteID;
        TEST_NAME = testSuiteID;
        this.host = host;
        this.port = port;
        this.contextRoot = contextRoot;
        this.urlPattern = urlPattern;
        this.stat = stat;
    }
    

    public void test( String c) throws Exception {
      this.test( c, "");
    }


    public void test( String c, String params) throws Exception {
        String EXPECTED_RESPONSE = c + ":pass";
        String TEST_CASE = TEST_NAME + c;
        String url = "http://" + host + ":" + port + contextRoot + "/";
        url = url + urlPattern + "?case=" + c;
        if ( (params != null) & (!params.trim().equals("")) ) {
            url = url + "&" + params.trim();
        }

        System.out.println("url="+url);

        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_CASE, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
	    String line = null;
	    while ((line = input.readLine()) != null) {
              // System.out.println("line="+line);
	      if (line.contains(EXPECTED_RESPONSE)) {
		stat.addStatus(TEST_CASE, stat.PASS);
		break;
	      }
	    }
	    
	    if (line == null) {
	      System.out.println("Unable to find " + EXPECTED_RESPONSE +
				  " in the response");
	    }
	    stat.addStatus(TEST_CASE, stat.FAIL);
        }    
    }

}


