package com.sun.pamrealm.test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.auth.AuthScope;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class PamBasicAuthTest {

    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final String DEFAULT_HOST = "localhost";
    public static final String CONTEXT_ROOT = "pamrealmsimpleweb";
    public static final String testId = "SEC: PamRealm";
    public static final String EXPECTED_RESPONSE = "This is a protected page";

    public static void main(String args[]) {
        SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("PamRealm Web Authentication Test");

        HttpClient client = new HttpClient();

        String host = args[0];

        if (host == null) {
            host = DEFAULT_HOST;
        }

        String strPort = args[1];

        Integer port;
        try {
            port = Integer.valueOf(strPort);
        } catch (Exception e) {
            port = DEFAULT_HTTP_PORT;
        }

        String userName = args[2];//username
        String password = args[3];//password

        client.getState().setCredentials(new AuthScope(host, port, "pam"), new UsernamePasswordCredentials(userName, password));

        String url = "http://" + host + ":" + port + "/" + CONTEXT_ROOT;

        GetMethod get = new GetMethod(url);
        get.setDoAuthentication(true);
        String response = "";
        try {
            int status = client.executeMethod(get);
            response = get.getResponseBodyAsString();
            System.out.println("Obtained response.." + response);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testId, stat.FAIL);
        }
        if (response.trim().equals(EXPECTED_RESPONSE.trim())) {
            stat.addStatus(testId, stat.PASS);
            System.out.println("PASS");
        } else {
            stat.addStatus(testId, stat.FAIL);
            System.out.println("FAIL");

        }
        stat.printSummary();
    }
}
