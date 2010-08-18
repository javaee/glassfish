/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package versionedservlet.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Romain GRECOURT - SERLI (romain.grecourt@serli.com)
 */
public class SimpleVersionedClient {

    String url;
    String versionIdentifier;
    Boolean testPositive;

    public SimpleVersionedClient(String[] args){
        url = args[0];
        testPositive = (Boolean.valueOf(args[1])).booleanValue();
        if(args.length > 2) {
            versionIdentifier = args[2];
        } else {
            versionIdentifier = "";
        }
    }
    
    public void doTest() {        
        try {
            // this provides some usefull informations to investigate
            log("Test: devtests/deployment/versioning/simple-versioned-servlet");
            if(testPositive){
                log("this test is expected to succeed");
            } else {
                log("this test is expected to fail");
            }
            TestResponse response = invokeServlet();
            report(response);
        } catch (IOException ex) {
            if (testPositive) {
                ex.printStackTrace();
                fail();
            } else {
                log("Caught EXPECTED IOException: " + ex);
                pass();
            }
	} catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private TestResponse invokeServlet() throws Exception {
        log("Invoking URL = " + url);
        log("Expected version identifier = " + versionIdentifier);
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = input.readLine();
        return new TestResponse(code, line);
    }

    private void report(TestResponse response) {
        if (testPositive) { //expect return code 200
            if(response.getCode() != 200) {
                log("Incorrect return code: " + response.getCode());
                fail();
            } else {
                log("Correct return code: " + response.getCode());
                if(response.getIdentifier().equals(versionIdentifier)){
                    log("Correct version identifier: "+response.getIdentifier());
                    pass();
                }
                else{
                    log("Incorrect version identifier: "+response.getIdentifier());
                    fail();
                }
            }
        } else {
            if(response.getCode() != 200) { //expect return code !200
                log("Incorrect version identifier: "+response.getIdentifier());
                fail();
            } else {
                log("Correct return code: " + response.getCode());
                if(response.getIdentifier().equals(versionIdentifier)){
                    log("Icorrect version identifier: " + response.getIdentifier());
                    fail();
                } else{
                    log("Correct version identifier: "+response.getIdentifier());
                    pass();
                }
            }
        }
    }

    private void log(String message) {
        System.err.println("[versionedservlet.client.SimpleVersionedClient]:: " + message);
    }

    private void pass() {
        log("PASSED: devtests/deployment/versioning/simple-versioned-servlet");
        System.exit(0);
    }

    private void fail() {
        log("FAILED: devtests/deployment/versioning/simple-versioned-servlet");
        System.exit(1);
    }

    private class TestResponse{
        int code;
        String identifier;

        public TestResponse(int codeResponse, String identifierResponse){
            code = codeResponse;
            identifier = identifierResponse;
        }

        public int getCode() {
            return code;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    public static void main (String[] args) {
        SimpleVersionedClient client = new SimpleVersionedClient(args);
        client.doTest();
    }
}
