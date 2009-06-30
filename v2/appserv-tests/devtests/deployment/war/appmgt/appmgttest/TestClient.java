/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package appmgttest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Command-line args should be:
 *   URL to use in accessing the servlet
 *   expect positive result (true or false)
 *   one or more of the two following formats:
 *     -env name(class)=value//"desc"
 *     -param name=value//"desc"
 *
 * @author tjquinn
 */
public class TestClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestClient().run(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void run(String[] args) {

        String url = args[0];
        boolean testPositive = (Boolean.valueOf(args[1])).booleanValue();
        try {
            log("Test: devtests/deployment/war/appmgt");
            final Map<String,EnvEntryInfo> envs = new HashMap<String,EnvEntryInfo>();
            final Map<String,ParamInfo> params = new HashMap<String,ParamInfo>();
            int code = invokeServlet(url, envs, params);

            /*
             * We always expect the servlet to respond.
             */
            report(code, true);

            boolean entireTestPassed = true;

            String nextTestType = null;
            for (int i = 2; i < args.length; i++) {
                final String arg = args[i];
                if (arg.startsWith("-")) {
                    nextTestType = arg;
                } else {
                    if (nextTestType.equals("-env")) {
                        EnvEntryInfo target = EnvEntryInfo.parseBrief(arg);
                        EnvEntryInfo match = envs.get(target.name());
                        entireTestPassed &= reportCheck(target, match, "env-entry", target.name());
                    } else if (nextTestType.equals("-param")) {
                        ParamInfo target = ParamInfo.parseBrief(arg);
                        ParamInfo match = params.get(target.name());
                        entireTestPassed &= reportCheck(target, match, "context-param", target.name());
                    }
                }
            }
            if (entireTestPassed == testPositive) {
                pass();
            } else {
                fail();
            }

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

    private boolean reportCheck(Object target, Object match, final String testType, final String targetName) {
        boolean result;
        if (match == null) {
            System.err.println("No matching " + testType + " for target name " + targetName);
            result = false;
        } else if ( ! match.equals(target)) {
            System.err.println("Target " + target.toString() + " != match " + match.toString());
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private int invokeServlet(final String url, final Map<String,EnvEntryInfo> envs,
            final Map<String,ParamInfo> params) throws Exception {
        log("Invoking URL = " + url);
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        if (code == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(c1.getInputStream()));
            String line;
            System.out.println("From servlet:");
            while ((line = br.readLine()) != null) {
                System.out.println("  " + line);
                if (line.startsWith("-env")) {
                    final EnvEntryInfo env = EnvEntryInfo.parseBrief(line.substring("-env".length() + 1));
                    envs.put(env.name(), env);
                } else if (line.startsWith("-param")) {
                    final ParamInfo param = ParamInfo.parseBrief(line.substring("-param".length() + 1));
                    params.put(param.name(), param);
                } else {
                    System.err.println("Unrecognized response line from servlet - continuing:");
                    System.err.println(">>" + line);
                }
            }

            System.out.println("servlet done");
            br.close();
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
            }
        } else {
            if(code != 200) { //expect return code !200
                log("Correct return code: " + code);
            } else {
                log("Incorrect return code: " + code);
                fail();
            }
        }
    }

    private void log(String message) {
        System.err.println("[war.client.Client]:: " + message);
    }

    private void pass() {
        log("PASSED: devtests/deployment/war/appmgt");
        System.exit(0);
    }

    private void fail() {
        log("FAILED: devtests/deployment/war/appmgt");
        System.exit(1);
    }
}
