/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

    public static final String CLUSTER_NAME = "c1";
    public static final String INSTANCE1_NAME = "in1";
    public static final String INSTANCE2_NAME = "in2";
    public static final String DEF_RESOURCE = "jdbc/xa2"; //__default";
    public static final String XA_RESOURCE = "jdbc/xa";
    public static final String TXID = "0000000000000001_00";
    public static final String TXID_MONITOR = "transaction-service.activeids-current";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare(args[1]);
        } else if ("clean".equals(args[0])) {
            (new Client()).clean(args[1]);
        } else if ("insert_in_one_resource".equals(args[0])) {
            (new Client()).insert_in_one_resource(args[1], args[2]);
        } else if ("insert_xa_data".equals(args[0])) {
            (new Client()).insert_xa_data(args[1], args[2]);
        } else if ("rollback".equals(args[0])) {
            (new Client()).rollback();
        } else if ("recover".equals(args[0])) {
            String param = null;
            if (args.length > 1)
                param = args[1];
            (new Client()).recover(param);
        } else if ("verify_default".equals(args[0])) {
            (new Client()).verify_default(args[1], args[2], args[3]);
        } else if ("verify_xa".equals(args[0])) {
            (new Client()).verify_xa(args[1], args[2], args[3]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for transaction CLIs";
    }

    public void prepare(String path) {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);
            if (Boolean.getBoolean("enableShoalLogger")) {
                asadmin("set-log-levels", "ShoalLogger=FINER");
                asadmin("set-log-levels", "--target", CLUSTER_NAME, "ShoalLogger=FINER");
            }
            asadmin("start-cluster", CLUSTER_NAME);
            asadmin("set", "configs.config." + CLUSTER_NAME + "-config.monitoring-service.module-monitoring-levels.transaction-service=HIGH");
            //asadmin("set", "configs.config." + CLUSTER_NAME + "-config.log-service.module-log-levels.jta=FINE");
            // to force derby failure on recovery after it was killed: asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.commit-one-phase-during-recovery=true");
            //asadmin("set-log-level", "javax.enterprise.resource.jta=FINE");
            System.out.println("Started cluster. Setting up resources.");

            asadmin("create-resource-ref", "--target", CLUSTER_NAME, DEF_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rollback() {
        System.out.println("Executing rollback CLI");
        try {
            asadmin("freeze-transaction-service", "--target", INSTANCE1_NAME);
            AsadminReturn result = asadminWithOutput("get", "-m", INSTANCE1_NAME + "." + TXID_MONITOR);
            System.out.println("Executed command: " + result.out);
            String[] parts = result.out.split("\n");
            String line = null;
            for (int i = 0; i++ < (parts.length - 1); ) {
                if (parts[i].startsWith("Transaction Id")) {
                    line = parts[i + 1];
                    break;
                }
            }
            if (line != null) {
                parts = line.split(" ");
                result = asadminWithOutput("rollback-transaction", "--target", INSTANCE1_NAME, "--transaction_id", parts[0]);
                System.out.println("Executed command: " + result.out);
                if (!result.returnValue) {
                    System.out.println("CLI FAILED: " + result.err);
                }
            } else {
                System.out.println("Transaction Id not found");
            }
            asadmin("unfreeze-transaction-service", "--target", INSTANCE1_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished rollback CLI");
    }

    public void recover(String location) {
        System.out.println("Executing recover CLI");
        try {
            //asadmin("set", "configs.config." + CLUSTER_NAME + "-config.log-service.module-log-levels.resourceadapter=FINE");
            AsadminReturn result = null;
            if (location != null && location.length() > 0) {
                // Try an error first
                result = asadminWithOutput("recover-transactions", "--target", INSTANCE2_NAME, INSTANCE1_NAME);
                System.out.println("Executed command: " + result.out);
                if (result.returnValue) {
                    System.out.println("CLI DID NOT FAIL!");
                    stat.addStatus("transaction-ee-cli-recover-with-null-logdir", stat.FAIL);
                } else {
                    System.out.println("CLI failed as expected: " + result.err);
                }

                String txLog = new StringBuffer(location).append(File.separator)
                        .append("nodes").append(File.separator).append("localhost-domain1")
                        .append(File.separator).append(INSTANCE1_NAME).append(File.separator)
                        .append("logs").append(File.separator).append(INSTANCE1_NAME)
                        .append(File.separator).append("tx").toString();
                result = asadminWithOutput("recover-transactions", "--target", INSTANCE2_NAME, 
                        "--transactionlogdir", txLog, INSTANCE1_NAME); 
            } else {
                result = asadminWithOutput("recover-transactions", INSTANCE1_NAME); 
            }
            System.out.println("Executed command: " + result.out);
            if (!result.returnValue) {
                System.out.println("CLI FAILED: " + result.err);
            }
            asadmin("set", "configs.config." + CLUSTER_NAME + "-config.log-service.module-log-levels.resourceadapter=INFO");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished recover CLI");
    }

    public void insert_in_one_resource(String appname, String port) {
        execute(appname, port, "TestServlet", "true");
    }

    public void insert_xa_data(String appname, String port) {
        execute(appname, port, "TestServlet?2", "true");
    }

    public void verify_default(String appname, String port, String operation) {
        verify(appname, port, operation, "VerifyServlet");
    }

    public void verify_xa(String appname, String port, String operation) {
        verify(appname, port, operation, "VerifyServlet?xa");
    }

    public void verify(String appname, String port, String operation, String servlet) {
        stat.addDescription("transaction-ee-" + operation);

        boolean res = execute(appname, port, servlet, "RESULT:3");

        stat.addStatus("transaction-ee-" + operation, ((res)? stat.PASS : stat.FAIL));
        stat.printSummary("transaction-ee-" + operation);
    }

    public void clean(String name) {
        try {
            asadmin("undeploy", "--target", CLUSTER_NAME, name);
            System.out.println("Undeployed " + name);
            asadmin("stop-local-instance", INSTANCE1_NAME);
            asadmin("stop-local-instance", INSTANCE2_NAME);
            asadmin("stop-cluster", CLUSTER_NAME);
            asadmin("delete-local-instance", INSTANCE1_NAME);
            asadmin("delete-local-instance", INSTANCE2_NAME);
            asadmin("delete-cluster", CLUSTER_NAME);
            asadmin("set-log-levels", "ShoalLogger=CONFIG");
            System.out.println("Removed cluster");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private boolean execute(String appname, String port, String servlet, String expectedResult) {
        String connection = "http://localhost:" + port + "/" + appname + "/" + servlet;

        System.out.println("invoking webclient servlet at " + connection);
        boolean result=false;

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
                    result=true;
                    break;
                }
            }
          } catch (Exception e) {
              e.printStackTrace();
          }

          if (result) {
              System.out.println("SUCCESS");
          } else {
              System.out.println("FAILURE");
          }

          return result;
    }

}
