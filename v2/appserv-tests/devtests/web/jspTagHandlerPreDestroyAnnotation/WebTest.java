import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6646921 ("@PreDestroy method not called in jsp tag
 * handler")
 */
public class WebTest {

    private static final String TEST_NAME =
        "jsp-tag-handler-pre-destroy-annotation";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String numRun;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        numRun = args[3];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for @PreDestroy on JSP tag handler");

        WebTest test = new WebTest(args);

        try {
            if ("first".equals(test.numRun)) {
                test.runFirst();
            } else {
                test.runSecond();
            }
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void runFirst() throws Exception {     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/jsp/test.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200" +
                                ", received: " + responseCode);
        }
    }

    public void runSecond() throws Exception {
        File inFile = new File("/tmp/mytest");
        FileInputStream fis = new FileInputStream(inFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = br.readLine();
        br.close();
        inFile.delete();
        if (!"SUCCESS".equals(line)) {
            throw new Exception("File contents that were supposed to have " +
                                "been written by @PreDestroy method " +
                                "not found");
        }
    }
}
