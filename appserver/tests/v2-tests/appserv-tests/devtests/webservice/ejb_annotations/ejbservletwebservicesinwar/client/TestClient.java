package client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestClient {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String testDescription = "ejbservletwebservices-war";
    public boolean found1 = false;
    public boolean found2 = false;
    public boolean found3 = false;

    public static void main (String[] args) {
        stat.addDescription("");
        TestClient client = new TestClient();
        client.doTest(args);
        stat.printSummary(testDescription);
    }

    public void doTest(String[] args) {

        String url = args[0];
        try {
            int code = invokeServlet(url);
            report(code);
	} catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private int invokeServlet(String url) throws Exception {
        log("Invoking url = " + url);
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            log(line);
            if (line.indexOf("So the RESULT OF HELLO SERVICE IS") != -1)
                found1 = true;
            if (line.indexOf("EJB WS:All") != -1)
                found2 = true;
            if (line.indexOf("Servlet WS:All") != -1)
                found3 = true;
        }
        return code;
    }

    private void report(int code) {
        if(code != 200) {
            log("Incorrect return code: " + code);
            fail();
        }
        if(found1 && found2 && found3) {
            pass();
        } else {
            fail();
        }
    }

    private void log(String message) {
        System.out.println("[client.TestClient]: " + message);
    }

    private void pass() {
        stat.addStatus(testDescription, stat.PASS);
    }

    private void fail() {
        stat.addStatus(testDescription, stat.FAIL);
    }
}
