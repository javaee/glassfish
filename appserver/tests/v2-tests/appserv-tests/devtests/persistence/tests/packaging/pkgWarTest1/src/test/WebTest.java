package test;

import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import  util.WebTestUtil;

public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static String TEST_NAME = "test";

    private String testSuiteID;
    private String host;
    private String port;
    private String contextRoot;
    private String urlPattern;


    public WebTest(String[] args) {
        if ( args.length < 5 ) {
          System.err.println("Usage: java WebTest <TestSuiteID> <host> <port> <contextRoot> <urlPattern> " );
          return;
        }
 
        testSuiteID = args[0];
        host = args[1];
        port = args[2];
        contextRoot = args[3];
        urlPattern = args[4];
        TEST_NAME = testSuiteID;
    }
    
    public static void main(String[] args) {
        WebTest webTest = new WebTest(args);
        webTest.doTest();
    }

    public void doTest() {     
       
        try { 
            WebTestUtil wtu = new WebTestUtil ( host, port, contextRoot, urlPattern, testSuiteID, stat); 
	    stat.addDescription("Persistence Pakcage Test ");
            wtu.test("testInsert");
            wtu.test("verifyInsert");
            wtu.test("testDelete");
            wtu.test("verifyDelete");
	    stat.printSummary(TEST_NAME);
        } catch (Exception ex) {
	    stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }


}
