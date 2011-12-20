/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Sun Microsystems, Inc. All rights reserved.
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
    public static final String XA_RESOURCE = "jdbc/xa";
    public static final String NONTX_RESOURCE = "jdbc/nontx";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare(args[1]);
        } else if ("clean".equals(args[0])) {
            (new Client()).clean(args[1]);
        } else if ("deploy".equals(args[0])) {
            (new Client()).deploy(args[1]);
        } else if ("undeploy".equals(args[0])) {
            (new Client()).undeploy(args[1]);
        } else if ("insert_xa_data".equals(args[0])) {
            (new Client()).insert_xa_data(args[1], args[2], args[3]);
        } else if ("verify_xa".equals(args[0])) {
            (new Client()).verify_xa(args[1], args[2], args[3]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for transaction recovery with MDBs";
    }

    public void prepare(String enable_delegate) {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.db-logging-resource=" + NONTX_RESOURCE);
            if (Boolean.valueOf(enable_delegate)) {
                asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.delegated-recovery=true");
            }
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);
            System.out.println("Creating JMS resources");
            asadmin("create-jms-resource", "--target", CLUSTER_NAME, "--restype", "javax.jms.QueueConnectionFactory", "jms/ejb_mdb_QCF");
            asadmin("create-jmsdest", "--target", CLUSTER_NAME, "--desttype", "ejb_mdb_Queue");
            asadmin("create-jms-resource", "--target", CLUSTER_NAME, "--restype", "javax.jms.Queue", "--property", "imqDestinationName=ejb_mdb_Queue", "jms/ejb_mdb_Queue");
            System.out.println("Finished creating JMS resources");

            if (Boolean.getBoolean("enableShoalLogger")) {
                asadmin("set-log-levels", "ShoalLogger=FINER");
                asadmin("set-log-levels", "--target", CLUSTER_NAME, "ShoalLogger=FINER");
            }
            asadmin("start-cluster", CLUSTER_NAME);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, NONTX_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, "jms/ejb_mdb_QCF");
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, "jms/ejb_mdb_Queue");
            System.out.println("Started cluster.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deploy(String path) {
        try {
            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert_xa_data(String appname, String port, String sendMsg) {
        if (Boolean.valueOf(sendMsg))
            execute(appname, port, "TestServlet?2", "true");
        else
            execute(appname, port, "TestServlet", "true");
    }

    public void verify_xa(String appname, String port, String operation) {
        verify(appname, port, operation, "VerifyServlet?xa");
    }

    public void verify(String appname, String port, String operation, String servlet) {
        stat.addDescription("transaction-ee-" + operation);

        boolean res = execute(appname, port, servlet, "RESULT:6");

        stat.addStatus("transaction-ee-dblogs-mdb" + operation, ((res)? stat.PASS : stat.FAIL));
        stat.printSummary("transaction-ee-dblogs-mdb" + operation);
    }

    public void clean(String name) {
        try {
            asadmin("delete-jms-resource", "--target", CLUSTER_NAME, "jms/ejb_mdb_QCF");
            asadmin("delete-jms-resource", "--target", CLUSTER_NAME, "jms/ejb_mdb_Queue");
            asadmin("delete-jmsdest", "--target", CLUSTER_NAME, "ejb_mdb_Queue");
            System.out.println("Deleted JMS resources.");

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

    public void undeploy(String name) {
        try {
            asadmin("undeploy", "--target", CLUSTER_NAME, name);
            System.out.println("Undeployed " + name);
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
