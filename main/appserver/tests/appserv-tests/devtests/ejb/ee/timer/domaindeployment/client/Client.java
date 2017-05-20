/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import admin.AdminBaseDevTest;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * CLI Dev test 
 * @author mvatkina
 */
public class Client extends AdminBaseDevTest {

    public static final String INSTANCE1_NAME = "in1";
    public static final String INSTANCE2_NAME = "in2";
    public static final String XA_RESOURCE = "jdbc/mypool";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare(args[1]);
        } else if ("deploy".equals(args[0])) {
            (new Client()).deploy(args[1], args[2]);
        } else if ("add-app-ref".equals(args[0])) {
            (new Client()).addAppRef(args[1], args[2]);
        } else if ("clean".equals(args[0])) {
            (new Client()).clean(args[1]);
        } else if ("redeploy".equals(args[0])) {
            (new Client()).redeploy(args[1]);
        } else if ("undeploy".equals(args[0])) {
            (new Client()).undeploy(args[1], args[2]);
        } else if ("verify".equals(args[0])) {
            (new Client()).verify(args[1], args[2], args[0]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for automatic timers deployed to a target \"domain\"";
    }

    public void prepare(String cluster_name) {
        try {
            asadmin("create-cluster", cluster_name);
            asadmin("create-local-instance", "--cluster", cluster_name, cluster_name+INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", cluster_name, cluster_name+INSTANCE2_NAME);
            asadmin("start-cluster", cluster_name);
            asadmin("create-resource-ref", "--target", cluster_name, XA_RESOURCE);
            System.out.println("Started cluster.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deploy(String target, String path) {
        try {
            asadmin("deploy", "--target", target, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redeploy(String path) {
        try {
            asadmin("deploy", "--force", "true", "--target", "domain", path);
            System.out.println("ReDeployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAppRef(String cluster_name, String name) {
        try {
            asadmin("create-application-ref", "--target", cluster_name, name);
            System.out.println("Added create-application-ref " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verify(String appname, String port, String operation) {
        stat.addDescription("ejb-ee-" + operation);

        String res = execute(appname, port, "RESULT:1");
        boolean success = "RESULT:1".equals(res);
        stat.addStatus("ejb-ee-" + operation, ((success)? stat.PASS : stat.FAIL));
        stat.printSummary("ejb-ee-" + operation);

    }

    public void undeploy(String target, String name) {
        try {
            asadmin("undeploy", "--target", target, name);
            System.out.println("Undeployed " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clean(String cluster_name) {
        try {
            asadmin("stop-cluster", cluster_name);
            asadmin("delete-local-instance", cluster_name+INSTANCE1_NAME);
            asadmin("delete-local-instance", cluster_name+INSTANCE2_NAME);
            asadmin("delete-cluster", cluster_name);
            System.out.println("Removed cluster " + cluster_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private String execute(String appname, String port, String expectedResult) {
        String connection = "http://localhost:" + port + "/" + appname + "/VerifyServlet";

        System.out.println("invoking webclient servlet at " + connection);
        String result=null;

        try {
            URL url = new URL(connection);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
  
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println("Processing line: " + line);
                if(line.indexOf(expectedResult)!=-1){
                    result=line;
                    break;
                }
            }
          } catch (Exception e) {
              e.printStackTrace();
          }

          if (result != null) {
              System.out.println("FOUND " + result);
          } else {
              System.out.println("FAILURE");
          }

          return result;
    }

}
