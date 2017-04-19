/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2017 Sun Microsystems, Inc. All rights reserved.
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
public class Client extends ClientBase {

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare(args[1], args[2], Boolean.valueOf(args[3]));
        } else if ("recover".equals(args[0])) {
            (new Client()).recover();
        } else {
            (new Client()).process(args);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for DBLogs/Base";
    }

    public void prepare(String path, String tx_log_dir, boolean enable_delegate) {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("create-system-properties", "--target", CLUSTER_NAME, "-Dcom.sun.appserv.transaction.nofdsync");
            asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.db-logging-resource=" + NONTX_RESOURCE);
            //asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.automatic-recovery=true");
            if (enable_delegate) {
                asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.delegated-recovery=true");
            }

            //asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            AsadminReturn result = asadminWithOutput("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            System.out.println("Executed command: " + result.out);
            if (!result.returnValue) {
                System.out.println("CLI FAILED: " + result.err);
            }
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);

            asadmin("create-resource-ref", "--target", CLUSTER_NAME, DEF_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, NONTX_RESOURCE);

            if (Boolean.getBoolean("enableShoalLogger")) {
                 asadmin("set-log-levels", "ShoalLogger=FINER");
                 asadmin("set-log-levels", "--target", CLUSTER_NAME, "ShoalLogger=FINER");
            }
            asadmin("set-log-levels", "--target", CLUSTER_NAME, "javax.enterprise.system.core.transaction=FINE");
            asadmin("start-cluster", CLUSTER_NAME);
            System.out.println("Started cluster. Setting up resources.");

            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recover() {
        System.out.println("Executing recover CLI");
        try {
            AsadminReturn result = null;
            result = asadminWithOutput("recover-transactions", "--target", INSTANCE2_NAME, INSTANCE1_NAME);
            System.out.println("Executed command: " + result.out);
            if (!result.returnValue) {
                System.out.println("CLI FAILED: " + result.err);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished recover CLI");
    }

}
