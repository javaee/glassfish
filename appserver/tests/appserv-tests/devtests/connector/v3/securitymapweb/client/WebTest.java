/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejte.j2ee.connector.securitymapweb;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.Socket;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {

    static String host = "localhost";
    static int port = 8080;

    private static SimpleReporterAdapter status = new SimpleReporterAdapter();

    private static String driver = null;
    private static String[] dbUsers = { "dbmap1", "dbmap1", "dbmap4", "dbmap3",
        "dbmap4", "dbmap2", "dbmap2", "dbmap4", "dbmap4" };

    public static void main(String[] args) {

        driver = args[0];

        status.addDescription("SecurityMap WebTest");
        try {
            runTests();
        } finally {
            status.printSummary("Securitymap tests");
        }
    }

    public static void runTests() {
        String[] users = { "foo1", "foo2", "foo3", "bar1", "bar2", "foobar",
            "barfoo", "foofoo" , "barbar" };

        for (int i = 0; i <users.length; i++) {
            sendRequest(users[i]);

            if (query(dbUsers[i], users[i]))
                status.addStatus("connector.securitymapweb.test" + (1 + i),
                                 status.PASS);
            else
                status.addStatus("connector.securitymapweb.test" + (1 + i),
                                 status.FAIL);
        }
    }

    public static boolean query(String dbUser, String userName) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName(driver);
            String url = "jdbc:derby://localhost:1527/testdb";

            conn = DriverManager.getConnection(url, dbUser, dbUser);

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from securitymapwebdb " +
                                   "where name = '" + userName + "'");

            int count  = 0;

            while (rs.next())
                count++;

            rs.close();

            System.out.println("No of records : " + count);

            if (count == 1)
                return true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) { }

            if (conn != null)
                try {
                    conn.close();
                } catch (Exception e) { }
        }

        return false;
    }

    /**
     * Connect to host:port and issue GET with given auth info.
     * This is hardcoded to expect the output that is generated
     * by the Test.jsp used in this test case.
     *
     */
    private static void sendRequest(String userName) {
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            String userpass = encode(userName + ":" + userName);

            String auth = "Authorization: Basic " + userpass + "\n";

            socket = new Socket(host, port);
            os = socket.getOutputStream();

            os.write("GET /security-map-web/ HTTP/1.0\n".getBytes());
            os.write(auth.getBytes());
            os.write("\n".getBytes());

            System.out.println("Send request");

            is = socket.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            String line = null;

            while ((line = bis.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (Exception e) { }

            if (is != null)
                try {
                    is.close();
                } catch (Exception e) { }

            if (socket != null)
                try {
                    socket.close();
                } catch (Exception e) { }
        }
    }

    public static String encode(String userpass) {
        sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
        return new String(enc.encodeBuffer(userpass.getBytes()));
    }

}
