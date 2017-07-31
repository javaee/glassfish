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

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;
import org.testng.Assert;

import java.io.*;
import java.net.*;

public class WebTestNG {

    private static final String TEST_NAME =
        "jsp-resource-injection-tag-handler";

    private static final String EXPECTED_RESPONSE =
        "ds1-login-timeout=0,ds2-login-timeout=0,ds3-login-timeout=0,"
        + "ds4-login-timeout=0,ds5-login-timeout=0,ds6-login-timeout=0";

    static String result = "";


    @Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "even"} ) // test method
    public void webtest(String host, String port, String contextroot) throws Exception{

        String testurl = "http://" + host  + ":" + port + contextroot + "/jsp/test.jsp";

        URL url = new URL(testurl);
        echo("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        Assert.assertEquals(responseCode, 200);

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String lastLine = null;
        while ((line = input.readLine()) != null) {
             lastLine = line;
        }
        
        Assert.assertEquals(lastLine, EXPECTED_RESPONSE);

    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
