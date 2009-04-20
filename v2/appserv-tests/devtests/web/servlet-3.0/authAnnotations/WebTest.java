import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for @RolesAllowed, @DenyAll, @PermitAll
 */
public class WebTest {

    private static final String TEST_NAME = "auth-annotations";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for @RolesAllowed, @DenyAll, @PermitAll");
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
        String contextPath = contextRoot + "/myurl";
        doWebMethod("GET", host, port, contextPath, false, "@PermitAll", 200, "p:Hello");
        doWebMethod("POST", host, port, contextPath, true, "@RolesAllowed", 200, "r:Hello, javaee");
        doWebMethod("TRACE", host, port, contextPath, true, "@DenyAll", 403, null);
    }

    private static void doWebMethod(String webMethod, String host, int port,
            String contextPath, boolean requireAuthenticate, String testSuffix,
            int responseCode, String expected) throws Exception {

        String urlStr = "http://" + host + ":" + port + contextPath;
        System.out.println(webMethod + " " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod(webMethod);
        if (requireAuthenticate) {
            urlConnection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        }
        urlConnection.connect();

        int code = urlConnection.getResponseCode();
        boolean ok = (code == responseCode);
        if (expected != null) {
            InputStream is = urlConnection.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            try{
                int lineNum = 1;
                while ((line = bis.readLine()) != null) {
                    System.out.println(lineNum + ":  " + line);
                    lineNum++;
                    ok = ok && expected.equals(line);
                }
            } catch( Exception ex){
                ex.printStackTrace();   
                throw new Exception("Test UNPREDICTED-FAILURE");
            }
        }

        stat.addStatus(TEST_NAME + ":" + testSuffix, ((ok)? stat.PASS : stat.FAIL));
    }
}
