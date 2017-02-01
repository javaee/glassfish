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
public class Client extends ClientBase {

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare();
        } else if ("deploy".equals(args[0])) {
            (new Client()).deploy(args[1]);
        } else if ("undeploy".equals(args[0])) {
            (new Client()).undeploy(args[1]);
        } else {
            (new Client()).process(args);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for transaction recovery with MDBs";
    }

    public void prepare() {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
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
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, "jms/ejb_mdb_QCF");
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, "jms/ejb_mdb_Queue");
            asadmin("start-cluster", CLUSTER_NAME);
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

    public void verify(String appname, String instance, String operation, String servlet) {
        stat.addDescription("transaction-ee-" + operation);

        boolean res = execute(appname, instance, servlet, "RESULT:6");

        stat.addStatus("transaction-ee-mdb" + operation, ((res)? stat.PASS : stat.FAIL));
        stat.printSummary("transaction-ee-mdb" + operation);
    }

    @Override
    public void clean(String name) {
        try {
            asadmin("delete-jms-resource", "--target", CLUSTER_NAME, "jms/ejb_mdb_QCF");
            asadmin("delete-jms-resource", "--target", CLUSTER_NAME, "jms/ejb_mdb_Queue");
            asadmin("delete-jmsdest", "--target", CLUSTER_NAME, "ejb_mdb_Queue");
            System.out.println("Deleted JMS resources.");

            super.clean(null);
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

}
