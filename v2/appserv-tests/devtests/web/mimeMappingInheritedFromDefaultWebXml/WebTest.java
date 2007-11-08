import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to ensure that this MIME mapping specified in default-web.xml:
 *
 *   <mime-mapping>
 *     <extension>gif</extension>
 *     <mime-type>image/gif</mime-type>
 *   </mime-mapping>
 *
 * is inherited by this webapp and reflected in response's content-type.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME
        = "mime-mapping-inherited-from-default-web-xml";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Inherit MIME mapping from default-web.xml");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/test.gif");
        System.out.println("Invoking URL: " + url.toString());

        URLConnection conn = url.openConnection();
        String contentType = conn.getContentType();
        System.out.println("Response Content-Type: " + contentType);
        if ("image/gif".equals(contentType)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
