import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugtraq 5027440 ("Impossible for webapp to override global
 * JspServlet settings").
 *
 * Notice that for test "jsp-servlet-override-ieClassId" to work, JSP
 * precompilation must be turned off (see build.properties in this directory),
 * so that the value of the 'ieClassId' property is gotten from the JspServlet
 * (instead of from the JspC command line).
 */
public class WebTest {

    private static final String OBJECT_CLASSID = "ABCD";
    private static final String INCLUDED_RESPONSE = "This is included page";

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
        stat.addDescription("Unit test for Bugtraq 5027440");
        WebTest webTest = new WebTest(args);
        webTest.overrideIeClassId();
        webTest.jspInclude();
        stat.printSummary();
    }

    private void overrideIeClassId() {
     
        String testName = "jsp-servlet-override-ieClassId";

        BufferedReader bis = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp/overrideIeClassId.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                stat.addStatus("Wrong response code. Expected: 200"
                               + ", received: " + responseCode, stat.FAIL);
            } else {
                bis = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                String line = null;
                while ((line = bis.readLine()) != null) {
                    if (line.startsWith("<OBJECT")) {
                        break;
                    }
                }
  
                if (line != null) {
                    // Check <OBJECT> classid comment
                    System.out.println(line);
                    String classid = getAttributeValue(line, "classid");
                    if (classid != null) {
                        if (!classid.equals(OBJECT_CLASSID)) {
                            stat.addStatus("Wrong classid: " + classid
                                           + ", expected: " + OBJECT_CLASSID,
                                           stat.FAIL);
                        } else {
                            stat.addStatus(testName, stat.PASS);
                        }
                    } else {
                        stat.addStatus("Missing classid", stat.FAIL);
                    }

                } else {
                    stat.addStatus("Missing OBJECT element in response body",
                                   stat.FAIL);
                }
	    }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(testName + " test failed.");
            stat.addStatus(testName, stat.FAIL);
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

    private void jspInclude() {
     
        String testName = "jsp-servlet-override-include";

        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp/include.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                stat.addStatus("Wrong response code. Expected: 200"
                               + ", received: " + responseCode, stat.FAIL);
            } else {
                BufferedReader bis = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                String line = bis.readLine();
                if (!INCLUDED_RESPONSE.equals(line)) {
                    stat.addStatus("Wrong response. Expected: "
                                   + INCLUDED_RESPONSE
                                   + ", received: " + filter(line), stat.FAIL);
                } else {
                    stat.addStatus(testName, stat.PASS);
                }
	    }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(testName + " test failed.");
            stat.addStatus(testName, stat.FAIL);
        }
    }

    private String getAttributeValue(String element, String attribute) {

        String ret = null;

        int index = element.indexOf(attribute);
        if (index != -1) {
            int beginIndex = index + attribute.length() + 2;
            int endIndex = element.indexOf('"', beginIndex);
            if (endIndex != -1) {
                ret = element.substring(beginIndex, endIndex);
            }
        }

        return ret;
    }

    private String filter(String message) {

        if (message == null)
            return (null);

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuffer result = new StringBuffer(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }
        return (result.toString());

    }

}
