import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=6391
 *  ("PreDestroy not called in certain web components")
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-context-attribute-listener-annotation-predestroy";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for 6391");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {

        File inFile = new File("/tmp/mytest");
        FileInputStream fis = new FileInputStream(inFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = br.readLine();
        inFile.delete();

        if ("SUCCESS".equals(line)) {
            stat.addStatus(TEST_NAME, stat.PASS);
	} else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
