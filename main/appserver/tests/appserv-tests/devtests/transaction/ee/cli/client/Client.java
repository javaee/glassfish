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
public class Client extends ClientBase {

    private static final String XA2 = "jdbc/xa2"; //__default";
    private static final String TXID = "0000000000000001_00";
    private static final String TXID_MONITOR = "transaction-service.activeids-current";

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare(args[1]);
        } else if ("insert_in_one_resource".equals(args[0])) {
            (new Client()).insert_in_one_resource(args[1], args[2]);
        } else if ("rollback".equals(args[0])) {
            (new Client()).rollback();
        } else if ("recover".equals(args[0])) {
            String param = null;
            if (args.length > 1)
                param = args[1];
            (new Client()).recover(param);
        } else if ("verify_default".equals(args[0])) {
            (new Client()).verify_default(args[1], args[2], args[3]);
        } else {
            (new Client()).process(args);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for transaction CLIs";
    }

    private void prepare(String path) {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);
            if (Boolean.getBoolean("enableShoalLogger")) {
                asadmin("set-log-levels", "ShoalLogger=FINER");
                asadmin("set-log-levels", "--target", CLUSTER_NAME, "ShoalLogger=FINER");
            }
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA2);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("start-cluster", CLUSTER_NAME);
            asadmin("set", "configs.config." + CLUSTER_NAME + "-config.monitoring-service.module-monitoring-levels.transaction-service=HIGH");
            //asadmin("set", "configs.config." + CLUSTER_NAME + "-config.log-service.module-log-levels.jta=FINE");
            // to force derby failure on recovery after it was killed: asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.commit-one-phase-during-recovery=true");
            //asadmin("set-log-level", "javax.enterprise.resource.jta=FINE");
            System.out.println("Started cluster. Setting up resources.");

            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rollback() {
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
                if (!result.returnValue) {
                    System.out.println("CLI FAILED: " + result.err);
                    System.out.println("Cannot rollback transaction");
                } else {
                    System.out.println("Transaction Id not found");
                }
            }
            asadmin("unfreeze-transaction-service", "--target", INSTANCE1_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished rollback CLI");
    }

    private void recover(String location) {
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

    private void insert_in_one_resource(String appname, String instance) {
        execute(appname, instance, "TestServlet", "true");
    }

    private void verify_default(String appname, String instance, String operation) {
        verify(appname, instance, operation, "VerifyServlet");
    }

}
