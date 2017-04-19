/**
 */
package com.acme;


import java.net.*;
import java.io.*;
import java.util.StringTokenizer;

public class HttpClient {

    private static final String ASADMIN = "/space/work/v3/trunk/glassfishv3/glassfish/bin/asadmin";
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
            result = result && prev.getJsessionCookie().equals(current.getJsessionCookie());

            result = result && prev.getHttpCounter() < current.getHttpCounter();
            
        }
        return result;
    }

    private static class SessionStateInfo {
        int accessCount;

        String jsessionCookie;

        int httpCounter = -1;

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

        @Override
        public String toString() {
            return "SessionStateInfo{" +
                    "accessCount=" + accessCount +
                    ", jsessionCookie='" + jsessionCookie + '\'' +
                    ", httpCounter=" + httpCounter +
                    '}';
        }
    }


}
