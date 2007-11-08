import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=2439
 * ("Add support for context.xml"):
 *
 * This unit test ensures the following context.xml configuration.
 * 
 * 1. default virtual-server context.xml using virtual-server property 
 * "contextXmlDefault" 
 * 
 * 2. deployed webapps's context.xml (META-INF/context.xml)
 *
 * The global context.xml and virtual-server context.xml will be copied to config
 * and removed after this unit test.
 * 
 * The "contextXml" property of the virtual-server "server" is added (before
 * the webapp's deployment) and removed (after the webapp's undeployment)
 * 
 */
public class WebTest {

    private static final String TEST_NAME = "context-xml-config";

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

        stat.addDescription("Unit test for GlassFish Issue 2439");
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

        if (checkVirtualServerContextDefaultXml() &&
            checkWebappContextXml()) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

    }


    /*
     * Check virtual-server default context.xml
     */
    private boolean checkVirtualServerContextDefaultXml() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/default-context-xml-test.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean success = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("virtual-server-env-value")) {
                success = true;
                break;
            }
        }

        return success;

    }


    /*
     * Check webapp context.xml
     */
    private boolean checkWebappContextXml() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/webapp-context-xml-test.jsp" + " HTTP/1.0\n";
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
