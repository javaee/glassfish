/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import java.util.Map;
import java.net.HttpURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.sun.ejte.ccl.reporter.*;

import org.apache.catalina.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.glassfish.admingui.common.util.RestResponse;
import org.glassfish.admingui.common.util.RestUtil;

/*
 * Unit test for Issue 9309: [monitoring] request-count is incorrect
 * Unit test for Issue 8984: errorcount-count statistics is missing
 *
 */
public class WebTest {

    private static final String TEST_NAME = "monitor-web-request";
    private static final String EXPECTED = "OK";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String adminHost;
    private String adminPort;
    private String adminUser;
    private String adminPassword;
    private String instanceName;
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        adminHost = args[0];
        adminPort = args[1];
        adminUser = args[2];
        adminPassword = args[3];
        instanceName = args[4];
        host = args[5];
        port = args[6];
        contextRoot = args[7];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for issue 9309");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	    stat.printSummary();
    }

    public void doTest() {
        try {
            int webReqCount1 = getCount("web/request/requestcount", "requestcount");
            System.out.println("web request count: " + webReqCount1);
            int appReqCount1 = getCount("applications" + contextRoot + "-web/" + instanceName + "/requestcount", "requestcount");
            System.out.println("app request count: " + appReqCount1);

            String testResult = invokeURL("http://" + host + ":" + port + contextRoot + "/test");
            System.out.println(testResult);
            
            int webReqCount2 = getCount("web/request/requestcount", "requestcount");
            System.out.println("web request count: " + webReqCount2);
            int appReqCount2 = getCount("applications" + contextRoot + "-web/" + instanceName + "/requestcount", "requestcount");
            System.out.println("app request count: " + appReqCount2);

            boolean ok1 = (EXPECTED.equals(testResult) &&
                    (webReqCount1 >= 0 && webReqCount2 == (webReqCount1 + 1)) &&
                    (appReqCount1 >= 0 && appReqCount2 == (appReqCount1 + 1)));


            int webErrorCount1 = getCount("web/request/errorcount", "errorcount");
            System.out.println("web error count: " + webErrorCount1);
            int appErrorCount1 = getCount("applications" + contextRoot + "-web/" + instanceName + "/errorcount", "errorcount");
            System.out.println("app error count: " + appErrorCount1);

            invokeURL("http://" + host + ":" + port + contextRoot + "/badrequest");
            
            int webErrorCount2 = getCount("web/request/errorcount", "errorcount");
            System.out.println("web error count: " + webErrorCount2);
            int appErrorCount2 = getCount("applications" + contextRoot + "-web/" + instanceName + "/errorcount", "errorcount");
            System.out.println("app error count: " + appErrorCount2);

            boolean ok2 = (webErrorCount1 >= 0 && webErrorCount2 == (webErrorCount1 + 1)) &&
                    (appErrorCount1 >= 0 && appErrorCount2 == (appErrorCount1 + 1));

            boolean ok = ok1 && ok2;
            stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    private String invokeURL(String urlString) throws Exception {
     
        StringBuilder sb = new StringBuilder();

        URL url = new URL(urlString);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("accept", "application/xml");
        if (adminPassword != null) {
            conn.setRequestProperty("Authorization", "Basic " +
                new String(Base64.encode((adminUser + ":" + adminPassword).getBytes())));
        }
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = null;
            BufferedReader reader = null;
            try {
                is = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch(IOException ex) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch(IOException ex) {
                    }
                }
            }
        } else {
            System.out.println("Get response code: " + responseCode);
        }

        return sb.toString();
    }

    private int getCount(String monitorPath, String countName) throws Exception {
        String url = "http://" + adminHost + ":" + adminPort +
                "/monitoring/domain/server/" + monitorPath;
        String resultStr = invokeURL(url);
        System.out.println("getCount: "+resultStr);
        RestResponse response = RestUtil.get(url);
        Map<String, Object> map = response.getResponse();

        return ((Long)((Map)((Map)((Map)((Map)map.get("data")).get("extraProperties")).get(
                "entity")).get(countName)).get("count")).intValue();
    }
    
}
