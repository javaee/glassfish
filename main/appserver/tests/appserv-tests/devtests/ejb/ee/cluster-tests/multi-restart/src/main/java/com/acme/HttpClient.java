package com.acme;


import java.net.*;
import java.io.*;
import java.util.*;

public class HttpClient {

    private static final String ASADMIN = "/space/work/v3/trunk/glassfishv3/glassfish/bin/asadmin";
    private static String appName = "SFSBDriver";
    private static String servletName = "SFSBDriverServlet";

    private String host;
    private String[] instanceNames = new String[3];
    private String[] port = new String[3];

    private volatile SessionStateInfo stateInfo = new SessionStateInfo();

    private int _accessCount = 0;

    private boolean canProceed = false;

    String jsessionIDCookie;
    List<String> responseCookies;

    public static void main(String args[]) {
        HttpClient client = new HttpClient(args);
        client.doTest();
    }

    public HttpClient(String[] args) {
        host = "localhost";
	canProceed = (args.length == 6);
	if (canProceed) {
            for (int i=0; i<3; i++) {
	        instanceNames[i] = args[2 * i];
	        port[i] = args[2 * i + 1];
            }
	}
    }

    public void doTest() {
	if (! canProceed) {
	    System.err.println("Usage: java -cp <cp> com.acme.HttpClient <inst_0_name> <port0>  <inst_1_name> <port1>  <inst_2_name> <port2> ");
        } else {
            try {
                String url = "http://" + host + ":" + port[0] +
                        "/" + appName + "/" + servletName;
    
                System.out.println("invoking webclient servlet at " + url);
    
                URL u = new URL(url);
                URLConnection uc = u.openConnection();
    
                stateInfo = extractSessionStates(uc);
                stateInfo.setAccessCount(1);
                jsessionIDCookie = stateInfo.getJsessionCookie();
    
                stopAndAccessAndStart(instanceNames[0], port[1]);
                stopAndAccessAndStart(instanceNames[1], port[2]);
                stopAndAccessAndStart(instanceNames[2], port[0]);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
	}
    }

    private void stopAndAccessAndStart(String instance, String port) {
            try { Thread.sleep(3*1000); } catch (Exception ex) {}

	    stopInstance(instance);
            String url = "http://" + host + ":" + port +
                    "/" + appName + "/" + servletName;
            System.out.println("** ACCESSING URL : " + url);
            for (int i = 0; i < 3; i++) {
                if (! accessApplication(url)) {
		   break;
		}
            }

	    startInstance(instance);
            url = "http://" + host + ":" + port +
                    "/" + appName + "/" + servletName;
            for (int i = 0; i < 3; i++) {
                if (! accessApplication(url)) {
		   break;
		}
            }
    }

    private void stopInstance(String instName) {
	try {
	    System.out.println("Executing stop-instance "  + instName);
	    Thread.sleep(3 * 1000);
	    Process proc = Runtime.getRuntime().exec(ASADMIN + "  stop-instance " + instName);
	    proc.waitFor();
	    System.out.println("Process stop-instance "  + instName + " finished...");
	} catch (Exception ex) {
	    System.err.println("Error while stopping instance " + instName);
	}
    }

    private void startInstance(String instName) {
	try {
	    System.out.println("Executing start-instance "  + instName);
	    Process proc = Runtime.getRuntime().exec(ASADMIN + "  start-instance " + instName);
	    proc.waitFor();
	    System.out.println("Process start-instance "  + instName + " finished...");
	    Thread.sleep(3 * 1000);
	} catch (Exception ex) {
	    System.err.println("Error while starting instance " + instName);
	}
    }

    private boolean accessApplication(String urlStr) {
	try {
            URL url = new URL(urlStr);
            URLConnection uc = url.openConnection();
	    for (String cookie : responseCookies) {
                uc.setRequestProperty("Cookie", cookie);
	    }
            uc.connect();
            SessionStateInfo info = extractSessionStates(uc);
            info.setAccessCount(++_accessCount);
            boolean result = compareSessionStates(stateInfo, info);
            if (result) {
                stateInfo = info;
                System.out.println("Passed " + stateInfo);
            } else {
                System.out.println("Failed " + info);
		return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private SessionStateInfo extractSessionStates(URLConnection uc)
            throws IOException {
        SessionStateInfo tmpSessState = new SessionStateInfo();
        String headerName = null;
        responseCookies = new ArrayList<String>();
        for (int i = 1; (headerName = uc.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = uc.getHeaderField(i);
                responseCookies.add(cookie);
                System.out.println("JUST READ COOKIE: " + cookie);
                if (cookie.startsWith("JSESSIONID=")) {
		    jsessionIDCookie = cookie;
                }
            }
        }

        if (tmpSessState.getJsessionCookie() == null) {
            tmpSessState.setJsessionCookie(jsessionIDCookie);    
            responseCookies.add(jsessionIDCookie);    
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
