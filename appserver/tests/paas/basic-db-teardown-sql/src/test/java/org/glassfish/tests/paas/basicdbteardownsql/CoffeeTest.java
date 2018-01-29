/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.paas.basicdbteardownsql;

import java.util.ArrayList;
import junit.framework.Assert;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.Deployer;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Shalini M
 */

public class CoffeeTest {

    @Test
    public void test() throws Exception {

        // 1. Bootstrap GlassFish DAS in embedded mode.
        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setInstanceRoot(System.getenv("S1AS_HOME")
                + "/domains/domain1");
        glassFishProperties.setConfigFileReadOnly(false);
        GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish(
                glassFishProperties);
        PrintStream sysout = System.out;
        glassfish.start();
        System.setOut(sysout);

        // 2. Deploy the PaaS application.
        File archive = new File(System.getProperty("basedir")
                + "/target/basic_db_teardown_sql.war");
        // TODO :: use mvn apis to get the archive location.
        Assert.assertTrue(archive.exists());

        Deployer deployer = null;
        String appName = null;
        List dbConnectionDetails = null;
        CommandRunner commandRunner = glassfish.getCommandRunner();
        try {

            //2.1. Create the shared DB service
            CommandResult createSharedServiceResult = commandRunner.run(
                    "create-shared-service", "--characteristics", "service-type=Database",
                    "--configuration", "database.name=foobar", "--servicetype",
                    "Database", "coffee-service");
            System.out.println("\ncreate-shared-service command output [ " +
                    createSharedServiceResult.getOutput() + "]");

            //2.2. List services to check for the shared service
            CommandResult listSharedServicesResult = commandRunner.run(
                    "list-services", "--scope", "shared", "--output", "service-name, state");
            System.out.println("\nlist-services command output [ "
                    + listSharedServicesResult.getOutput() + "]");

            //2.3. Deploy app
            deployer = glassfish.getDeployer();
            appName = deployer.deploy(archive);

            System.err.println("Deployed [" + appName + "]");
            Assert.assertNotNull(appName);


            CommandResult result = commandRunner.run("list-services");
            System.out.println("\nlist-services command output [ "
                    + result.getOutput() + "]");

            // 3. Access the app to make sure PaaS app is correctly provisioned.
            String HTTP_PORT = (System.getProperty("http.port") != null) ? System
                    .getProperty("http.port") : "28080";

            String instanceIP = getLBIPAddress(glassfish);
            get("http://" + instanceIP + ":" + HTTP_PORT
                    + "/basic_db_teardown_sql/CoffeeServlet",
                    "Coffee ID");

            dbConnectionDetails = getDbConnectionDetails("http://" + instanceIP + ":" + HTTP_PORT +
                    "/basic_db_teardown_sql/DbConnectionDetailsServlet");
            // 4. Undeploy the PaaS application .
        } finally {
            if (appName != null) {
                deployer.undeploy(appName);
                System.err.println("Undeployed [" + appName + "]");
		        testTearDownSql(dbConnectionDetails);
                System.out.println("Destroying the resources created");
                //4.1. Delete Shared DB Service.
                CommandResult deleteResult = commandRunner.run(
                        "delete-shared-service", "coffee-service");
                System.out.println("\ndelete-shared-service command output [" +
                    deleteResult.getOutput() + "]");
            }
        }

    }

    private void testTearDownSql(List<String> dbConnectionDetails) throws SQLException {
        boolean notFound = false;
        Connection con = null;
        Statement stmt = null;
        System.out.println("DB Connection Details = " + dbConnectionDetails);
        Assert.assertEquals(dbConnectionDetails.size(), 3);
        try {
            con = DriverManager.getConnection(dbConnectionDetails.get(0) ,
                    dbConnectionDetails.get(1), dbConnectionDetails.get(2));
            System.out.println("Autocommit : " + con.getAutoCommit());
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("Select * from coffee");
            System.out.println("Result set empty : " + rs.next());
        } catch (SQLSyntaxErrorException ex) {
            //Expected.
            notFound = true;
        } finally {
            if(con != null) {
                con.close();
            }
            if(stmt != null) {
                stmt.close();
            }
        }
        Assert.assertTrue(notFound);
        System.out.println("\n***** SUCCESS **** Tear Down SQL Successful.*****\n");
    }

    private List<String> getDbConnectionDetails(String urlStr) throws Exception {
        List dbConnectionDetails = new ArrayList<String>();
        URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        System.out.println("\nURLConnection [" + yc + "] : ");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            dbConnectionDetails.add(line);
        }
        return dbConnectionDetails;
    }


    private void get(String urlStr, String result) throws Exception {
        URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        System.out.println("\nURLConnection [" + yc + "] : ");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String line = null;
        boolean found = false;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.indexOf(result) != -1) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        System.out.println("\n***** SUCCESS **** Found [" + result
                + "] in the response.*****\n");
    }

    private String getLBIPAddress(GlassFish glassfish) {
        String lbIP = null;
        String IPAddressPattern = "IP-ADDRESS\\s*\n*(.*)\\s*\n(([01]?\\d*|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([0-9]?\\d\\d?|2[0-4]\\d|25[0-5]))";
        try {
            CommandRunner commandRunner = glassfish.getCommandRunner();
            String result = commandRunner
                    .run("list-services", "--type", "LB",
                            "--output", "IP-ADDRESS").getOutput().toString();
            if (result.contains("Nothing to list.")) {
                result = commandRunner
                        .run("list-services", "--type", "JavaEE", "--output",
                                "IP-ADDRESS").getOutput().toString();

                Pattern p = Pattern.compile(IPAddressPattern);
                Matcher m = p.matcher(result);
                if (m.find()) {
                    lbIP = m.group(2);
                } else {
                    lbIP = "localhost";
                }
            } else {
                Pattern p = Pattern.compile(IPAddressPattern);
                Matcher m = p.matcher(result);
                if (m.find()) {
                    lbIP = m.group(2);
                } else {
                    lbIP = "localhost";
                }

            }

        } catch (Exception e) {
            System.out.println("Regex has thrown an exception "
                    + e.getMessage());
            return "localhost";
        }
        return lbIP;
    }
}
