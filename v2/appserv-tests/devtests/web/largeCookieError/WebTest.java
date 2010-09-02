/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for large cookie error.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "large-cookie-error";

    private static final String EXPECTED_RESPONSE = "SUCCESS";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6456553");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeWithLargeCookie();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        return;
    }

    private void invokeWithLargeCookie() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/largecookie.html");
        System.out.println(url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            buf.append('a');
        }
        String largeValue = buf.toString();
        StringBuilder cookieBuf = new StringBuilder();
        for (int j = 0; j < 9; j++) {
            if (j > 0) {
                cookieBuf.append(";");
            }
            cookieBuf.append("key" + j + "=" + largeValue);
        }

        int oneMinute = 60 * 1000;
        Date now = new Date();
        Date later = new Date(now.getTime() + 60 * oneMinute);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        TimeZone timeZone = dateFormat.getTimeZone();
        int min = timeZone.getOffset(later.getTime()) / oneMinute;
        int hour = min / 60;
        timeZone = TimeZone.getTimeZone("GMT" + (hour >= 0 ? "+" : "") + hour + ":" + min);
        dateFormat.setTimeZone(timeZone);

        cookieBuf.append("; expires " + dateFormat.format(later));
        conn.setRequestProperty("Cookie", cookieBuf.toString());
        conn.connect();
        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);
        if (responseCode == 400) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
