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
        new SimpleReporterAdapter("jbi");

	private static String testId = "jbi-serviceengine/rpc-literal/se_consumer_se_provider/generated_wsdl";

    public static void main (String[] args) {
        stat.addDescription(testId);
        TestClient client = new TestClient();
        client.doTest(args);
        stat.printSummary(testId);
    }

    public void doTest(String[] args) {

        String url = args[0];
        try {
            int code = invokeServlet(url);
            report(code);
	} catch (Exception e) {
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
		StringBuffer buffer = new StringBuffer();
        String line = null;
		boolean found = false;
        while ((line = input.readLine()) != null) {
			if(line.indexOf("SUCCESS") != -1) found = true;	
			buffer.append(line + "\n");
        }
        log(buffer.toString());
	    if(!found) return 505;	
        return code;
    }

    private void report(int code) {
        if(code != 200) {
            log("Incorrect return code: " + code);
            fail();
        }
        pass();
    }

    private void log(String message) {
        System.out.println("[client.TestClient]:: " + message);
    }

    private void pass() {
        stat.addStatus(testId, stat.PASS);
    }

    private void fail() {
        stat.addStatus(testId, stat.FAIL);
    }
}
