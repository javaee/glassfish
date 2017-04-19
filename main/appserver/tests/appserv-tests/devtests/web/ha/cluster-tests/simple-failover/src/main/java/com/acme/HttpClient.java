/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.acme;

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;

public class HttpClient {

    private static final String ASADMIN = "/home/rajiv/Software/glassfishv3/glassfish/bin/asadmin";
    private static String appName = "SFSBDriver";
    private static String servletName = "SFSBDriverServlet";

    private String host;
    private String port;

    private String failoverPort;

    private volatile SessionStateInfo stateInfo = new SessionStateInfo();
    String cookie;


    public static void main(String args[]) {
        HttpClient client = new HttpClient(args);
        client.doTest();
    }

    public HttpClient(String[] args) {
        host = "localhost";
        port = args[0];
        failoverPort = args[1];
    }

    public void doTest() {

        try {

            String url = "http://" + host + ":" + port +
                    "/" + appName + "/" + servletName;

            System.out.println("invoking webclient servlet at " + url);

            URL u = new URL(url);

            URLConnection uc = u.openConnection();

            stateInfo = extractSessionStates(uc);
            stateInfo.setAccessCount(1);
            cookie = stateInfo.getJsessionCookie();

            System.out.println("*****************************************************************");
            System.out.println("*** StateInfo: " + stateInfo + " ***");
            System.out.println("*****************************************************************");

            for (int i = 0; i < 3; i++) {
                System.out.println("Connecting for the " + i + " time....");
                u = new URL(url);
                uc = u.openConnection();
                uc.setRequestProperty("Cookie", cookie);
                uc.connect();
                SessionStateInfo info = extractSessionStates(uc);
                info.setAccessCount(2+i);
                boolean result = compareSessionStates(stateInfo, info);
                if (result) {
                    stateInfo = info;
                    System.out.println("Passed " + stateInfo);
                } else {

                    System.out.println("Failed " + info);
                }
            }


            System.out.println("Stopping inst1...");
            Process proc = Runtime.getRuntime().exec(ASADMIN + "  stop-instance inst1");
            proc.waitFor();
            Thread.sleep(3 * 1000);
            System.out.println("Process stop-instance finished...");


            System.out.println("Redirecting traffic to " + failoverPort + "...");
            url = "http://" + host + ":" + failoverPort +
                    "/" + appName + "/" + servletName;
            for (int i = 0; i < 3; i++) {
                System.out.println("Connecting for the " + i + " time....");
                u = new URL(url);
                uc = u.openConnection();
                uc.setRequestProperty("Cookie", stateInfo.getJsessionCookie());
                uc.connect();
                SessionStateInfo info = extractSessionStates(uc);
                info.setAccessCount(5+i);
                boolean result = compareSessionStates(stateInfo, info);
                if (result) {
                    stateInfo = info;
                    System.out.println("Passed " + stateInfo);
                } else {

                    System.out.println("Failed " + info);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SessionStateInfo extractSessionStates(URLConnection uc)
            throws IOException {
        SessionStateInfo tmpSessState = new SessionStateInfo();
        String headerName = null;
        for (int i = 1; (headerName = uc.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                tmpSessState.setJsessionCookie(uc.getHeaderField(i));
                System.out.println("JUST READ COOKIE: " + uc.getHeaderField(i));
            }
        }

        if (tmpSessState.getJsessionCookie() == null) {
            tmpSessState.setJsessionCookie(cookie);    
        }
        int code = ((HttpURLConnection) uc).getResponseCode();
        InputStream is = uc.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            //System.out.println("*****>>>>>> " + line);
            if (line.startsWith("Integer attr:")) {
                String val = line.substring(line.lastIndexOf(' '));
                if (! "null".equals(val.trim())) {
                    tmpSessState.setHttpCounter(Integer.valueOf(val.trim()));
                }
            } else if (line.startsWith("<h1>From session SFSB[1] NOT NULL")) {
                StringTokenizer tok = new StringTokenizer(line, " \t\n\r\f");
                for (int i=0; i<6; i++) {
                    tok.nextToken();
                }
                Boolean retrieved = Boolean.valueOf(tok.nextToken().trim());
                tmpSessState.setEjb1StateNew(! retrieved);
            } else if (line.startsWith("<h1>From session SFSB[2] NOT NULL")) {
                StringTokenizer tok = new StringTokenizer(line, " \t\n\r\f");
                for (int i=0; i<6; i++) {
                    tok.nextToken();
                }
                Boolean retrieved = Boolean.valueOf(tok.nextToken().trim());
                tmpSessState.setEjb2StateNew(! retrieved);
            } else if (line.contains("SFSB[1]")) {
                StringTokenizer tok = new StringTokenizer(line, " \t\n\r\f");
                for (int i=0; i<4; i++) {
                    tok.nextToken();
                }
                Integer state1 = Integer.valueOf(tok.nextToken().trim());
                tmpSessState.setEjb1Counter(state1);
            } else if (line.contains("SFSB[2]")) {
                StringTokenizer tok = new StringTokenizer(line, " \t\n\r\f");
                for (int i=0; i<4; i++) {
                    tok.nextToken();
                }
                Integer state2 = Integer.valueOf(tok.nextToken().trim());
                tmpSessState.setEjb2Counter(state2);
            }
        }

        //System.out.println("** COMPLETELY READ RESPONSE. State info: " + tmpSessState);
        if (code != 200) {
            throw new RuntimeException("Incorrect return code: " + code);
        }

        return tmpSessState;
    }

    private static final boolean compareSessionStates(SessionStateInfo prev, SessionStateInfo current) {
        boolean result = false;

        if (prev.getAccessCount() == 1) {
            //First time access;
            result = true;
        } else {
            result = current.getJsessionCookie() != null;
            result = result && (current.isEjb1StateNew() == false);
            result = result && (current.isEjb2StateNew() == false);
            result = result && prev.getJsessionCookie().equals(current.getJsessionCookie());

            result = result && prev.getHttpCounter() < current.getHttpCounter();
            result = result && prev.getEjb1Counter() < current.getEjb1Counter();
            result = result && prev.getEjb2Counter() < current.getEjb2Counter();
            
        }
        return result;
    }

    private static class SessionStateInfo {
        int accessCount;

        String jsessionCookie;

        int httpCounter = -1;

        boolean ejb1StateNew;
        int ejb1Counter;

        boolean ejb2StateNew;
        int ejb2Counter;

        public int getAccessCount() {
            return accessCount;
        }

        public void setAccessCount(int accessCount) {
            this.accessCount = accessCount;
        }

        public String getJsessionCookie() {
            return jsessionCookie;
        }

        public void setJsessionCookie(String jsessionCookie) {
            this.jsessionCookie = jsessionCookie;
        }

        public int getHttpCounter() {
            return httpCounter;
        }

        public void setHttpCounter(int httpCounter) {
            this.httpCounter = httpCounter;
        }

        public int getEjb1Counter() {
            return ejb1Counter;
        }

        public void setEjb1Counter(int ejb1Counter) {
            this.ejb1Counter = ejb1Counter;
        }

        public int getEjb2Counter() {
            return ejb2Counter;
        }

        public void setEjb2Counter(int ejb2Counter) {
            this.ejb2Counter = ejb2Counter;
        }

        public boolean isEjb1StateNew() {
            return ejb1StateNew;
        }

        public void setEjb1StateNew(boolean ejb1StateNew) {
            this.ejb1StateNew = ejb1StateNew;
        }

        public boolean isEjb2StateNew() {
            return ejb2StateNew;
        }

        public void setEjb2StateNew(boolean ejb2StateNew) {
            this.ejb2StateNew = ejb2StateNew;
        }

        @Override
        public String toString() {
            return "SessionStateInfo{" +
                    "accessCount=" + accessCount +
                    ", jsessionCookie='" + jsessionCookie + '\'' +
                    ", httpCounter=" + httpCounter +
                    ", ejb1StateNew=" + ejb1StateNew +
                    ", ejb1Counter=" + ejb1Counter +
                    ", ejb2StateNew=" + ejb2StateNew +
                    ", ejb2Counter=" + ejb2Counter +
                    '}';
        }
    }


}
