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

import com.meterware.httpunit.*;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class JSFTest {

    private final String serverhost;
    private final String serverport;
    private final String servercontextroot;
    private String URL = null;
    private WebConversation sr = null;
    private SimpleReporterAdapter status = null;
    private int pass = 0;
    private int fail = 0;
    private int total = 3;
    private final int DEBUG = 0;
    private String[][] summary = 
    {{"Index Page Test   : ", ""},
     {"Menu Page Test    : ", ""},
     {"Repeater Page Test: ", ""}};
    
    public static void main( String args[] ) {

        try {
            JSFTest test = new JSFTest(args);
            test.runTest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public JSFTest(String[] args) throws Exception {
        serverhost = args[0];
        serverport = args[1];
        servercontextroot = args[2];

        if(serverhost == null || serverport == null) {
            URL = "http://localhost:8080/jsfcomponents/";
        } else {
            URL = "http://"+serverhost + ":" + serverport +
                  "/" + servercontextroot + "/";
        }
        sr = new WebConversation( );
        status = new SimpleReporterAdapter("appserv-tests");
        status.addDescription("Testing JSF...");
        
    }
    
    private void runTest() {
        HttpUnitOptions.setScriptingEnabled(false);
        testIndexPage();
        testManuPage();
        testRepeaterPage();
        printSummary();
        
    }

    private void testIndexPage()  {
        String page = "";
        String testName = "JSF-indexPage";
        
        try {
            WebResponse client = sr.getResponse(URL+page);
            // client = sr.getResponse(URL+page);
            String s = client != null ? client.getText() : null;
            
            if(s != null) {
                if((s.indexOf("Component Content") >= 0) && (s.indexOf("execute.gif") >= 0)) {
                    status.addStatus(testName, status.PASS);
                    pass++;
                    summary[0][1] = "Passed";
                } else {
                    status.addStatus(testName, status.FAIL);
                    summary[0][1] = "Failed";
                    fail++;
                }
            } else {
                status.addStatus(testName, status.FAIL);
                summary[0][1] = "Failed";
                fail++;
            }
        } catch (Exception ex) {
            status.addStatus(testName, status.FAIL);
            summary[0][1] = "Failed";
            fail++;
            ex.printStackTrace();
        }
        
    }

    private void testManuPage()  {
        String page = "menu.faces";
        String testName = "JSF-menuPage";
        
        try {
            WebResponse client = sr.getResponse(URL+page);
            // client = sr.getResponse(URL+page);
            String s = client != null ? client.getText() : null;
            
            if(s != null) {
                if((s.indexOf("menu2") >= 0) && (s.indexOf("Close 2") >= 0)) {
                    status.addStatus(testName, status.PASS);
                    pass++;
                    summary[1][1] = "Passed";
                } else {
                    status.addStatus(testName, status.FAIL);
                    summary[1][1] = "Failed";
                    fail++;
                }
            } else {
                status.addStatus(testName, status.FAIL);
                summary[1][1] = "Failed";
                fail++;
            }
        } catch (Exception ex) {
            status.addStatus(testName, status.FAIL);
            summary[1][1] = "Failed";
            fail++;
            ex.printStackTrace();
        }
        
    }
    
    private void testRepeaterPage()  {
        String page = "repeater.faces";
        String testName = "JSF-repeaterPage";
        
        try {
            WebResponse client = sr.getResponse(URL+page);
            String s = client != null ? client.getText() : null;
            
            if(s != null) {
                if((s.indexOf("update") >= 0) && (s.indexOf("Save Changes") >= 0)) {
                    status.addStatus(testName, status.PASS);
                    pass++;
                    summary[2][1] = "Passed";
                } else {
                    status.addStatus(testName, status.FAIL);
                    summary[2][1] = "Failed";
                    fail++;
                }
            } else {
                status.addStatus(testName, status.FAIL);
                summary[2][1] = "Failed";
                fail++;
            }
        } catch (Exception ex) {
            status.addStatus(testName, status.FAIL);
            summary[2][1] = "Failed";
            fail++;
            ex.printStackTrace();
        }
        
    }

    private void printSummary() {
        System.out.println("**********************************************");
        System.out.println("Total Tests :"+total);
        System.out.println("Passed      :"+pass);
        System.out.println("Failed      :"+fail);
        System.out.println("**********************************************");
        System.out.println("Details of Test Run:\n");
        for(int i=0; i<summary.length; i++) {
            System.out.print(summary[i][0]);
            System.out.println(summary[i][1]);
        }
        status.printSummary("JSFTestID");
    }
    
}
