
package util;

import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

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


