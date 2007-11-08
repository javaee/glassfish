import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 * - 6535898 ("@PreDestroy in servlet is not working"), and
 * - https://glassfish.dev.java.net/issues/show_bug.cgi?id=1550
 *   ("Exception thrown when invoking
 *    InjectionManagerImpl.invokeInstancePreDestroy")
 *
 * Make sure servlet's @PreDestroy annotated method is invoked when the
 * servlet is destroyed during server shutdown.
 *
 * @PreDestroy annotated method saves some output to a file which this test
 * client reads when it is run following server restart. Test succeeds
 * if file exists and contains expected contents (the string "SUCCESS")
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-annotation-predestroy";

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

        stat.addDescription("Unit test for 6535898");
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
