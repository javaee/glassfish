/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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
