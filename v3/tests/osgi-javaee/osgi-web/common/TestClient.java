
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class TestClient {

    boolean failed = false;
    int passCode = 0; // See test result codes in Reporter.
    int failCode = 3; // Fail code for runtime failure.

    private static Reporter reporter;
    private static String testName; 

    public static void main (String[] args) {
        TestClient client = new TestClient();
	reporter = new Reporter(args[2]); //args[2] has testResult filename
	testName = args[1];
        client.doTest(args);
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
        String line = null;
        while ((line = input.readLine()) != null) {
            log(line);
            if(line.contains("failed"))
		failed = true;
        }
        return code;
    }

    private void report(int code) {
        if(code != 200) {
            log("Incorrect return code: " + code);
            fail();
            return; 
	}
        if(failed) {
            fail();
            return; 
	}
        pass();
    }

    private void log(String message) {
        System.out.println("[TestClient]:: " + message);
    }

    private void pass() {
        System.out.println("[TestClient]:: TestPassed");
	reporter.printStatus(testName, passCode);
    }

    private void fail() {
        System.out.println("[TestClient]:: TestFailed");
	reporter.printStatus(testName, failCode);
    }
}
