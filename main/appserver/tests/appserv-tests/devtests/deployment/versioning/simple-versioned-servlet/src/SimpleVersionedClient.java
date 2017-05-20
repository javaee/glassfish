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
