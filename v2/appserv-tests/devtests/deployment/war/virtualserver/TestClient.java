/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package virtualserver.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestClient {

    public static void main (String[] args) {
        TestClient client = new TestClient();
        client.doTest(args);
    }
    
    public void doTest(String[] args) {

        String url = args[0];
        boolean testPositive = (Boolean.valueOf(args[1])).booleanValue();
        try {
            log("Test: devtests/deployment/war/virtualserver");
            int code = invokeServlet(url);
            report(code, testPositive);
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

    private int invokeServlet(String url) throws Exception {
        log("Invoking URL = " + url);
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            log(line);
        }
        return code;
    }

    private void report(int code, boolean testPositive) {
        if (testPositive) { //expect return code 200
            if(code != 200) {
                log("Incorrect return code: " + code);
                fail();
            } else {
                log("Correct return code: " + code);
                pass();
            }
        } else {
            if(code != 200) { //expect return code !200
                log("Correct return code: " + code);
                pass();
            } else {
                log("Incorrect return code: " + code);
                fail();
            }
        }
    }

    private void log(String message) {
        System.out.println("[war.client.Client]:: " + message);
    }

    private void pass() {
        log("PASSED: devtests/deployment/war/virtualserver");
        System.exit(0);
    }

    private void fail() {
        log("FAILED: devtests/deployment/war/virtualserver");
        System.exit(-1);
    }
}
