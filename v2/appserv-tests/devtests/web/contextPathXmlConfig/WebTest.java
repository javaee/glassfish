import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for glassfish/domains/domain1/config/web-context-path-xml-config.xml
 *
 * This unit test ensures webapp context_path.xml packaged outside of the archive.  Needs to be a separate test from contextXmlConfig since contextXmlConfig tests webapp/META-INF/context.xml packaged inside the WAR 
 * 
 */
public class WebTest {

    private static final String TEST_NAME = "context-path-xml-config";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for context_path.xml");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();

    }

    public void run() throws Exception {

        if (checkWebappContextXml()) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

    }


    /*
     * Check webapp context.xml
     */
    private boolean checkWebappContextXml() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/webapp-context-path-xml-test.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean success = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("webapp-env-value")) {
                stat.addStatus(TEST_NAME, stat.PASS);
                success = true;
                break;
            }
        }

        return success;

    }
}
