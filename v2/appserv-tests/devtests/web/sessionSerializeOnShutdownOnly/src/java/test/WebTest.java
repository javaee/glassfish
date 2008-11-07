package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/*
 * Unit test for
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=6447
 *  ("Avoid serializing and saving sessions to file during un- or redeployment (unless requested by user)")
 */
public class WebTest {
    private static final String TEST_ROOT_NAME = "session-serialize-on-shutdown-only";
    private static final String EXPECTED_RESPONSE = "Found map";
    private static final String JSESSIONID = "JSESSIONID";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private String testName;
    private String host;
    private String port;
    private String contextRoot;
    private String run;
    private boolean shouldFindSession;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
        shouldFindSession = args.length >= 5 && Boolean.parseBoolean(args[4]);
    }

    public static void main(String[] args) {
        new WebTest(args).run();
    }

    private void run() {
        stat.addDescription("Unit test for GlassFish Issue 6447");
        try {
            if ("first".equals(run)) {
                testName = TEST_ROOT_NAME + "-first";
                createSession();
            } else {
                testName = TEST_ROOT_NAME + "-second";
                checkForSession();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, SimpleReporterAdapter.FAIL);
        }
        stat.printSummary();
    }

    public void createSession() throws Exception {
        Socket sock = new Socket(host, new Integer(port));
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/CreateSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());
        saveSessionID(sock);
        stat.addStatus(testName, SimpleReporterAdapter.PASS);
    }

    public void checkForSession() throws Exception {
        // Read the JSESSIONID from the previous run
        String jsessionId = readSessionID();
        Socket sock = new Socket(host, new Integer(port));
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ResumeSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\r\n".getBytes());
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        boolean found = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            found |= line.contains(EXPECTED_RESPONSE);
        }
        stat.addStatus(testName, found == shouldFindSession ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
    }

    private String readSessionID() throws IOException {
        FileInputStream fis = new FileInputStream(JSESSIONID);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        return br.readLine();
    }

    private void saveSessionID(Socket sock) throws Exception {
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        // Get the JSESSIONID from the response
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Set-Cookie:")
                || line.startsWith("Set-cookie:")) {
                break;
            }
        }
        if (line == null) {
            throw new Exception("Missing Set-Cookie response header");
        }
        String jsessionId = getSessionIdFromCookie(line, JSESSIONID);
        // Store the JSESSIONID in a file
        FileOutputStream fos = new FileOutputStream(JSESSIONID);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(jsessionId);
        osw.close();
    }

    private String getSessionIdFromCookie(String cookie, String field) {
        String ret = null;
        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index, endIndex);
            } else {
                ret = cookie.substring(index);
            }
            ret = ret.trim();
        }
        return ret;
    }
}
