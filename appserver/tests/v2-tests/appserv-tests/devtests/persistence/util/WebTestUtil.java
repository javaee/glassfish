/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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


