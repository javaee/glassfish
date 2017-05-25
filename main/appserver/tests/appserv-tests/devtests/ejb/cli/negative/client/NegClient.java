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

package com.acme;

import admin.AdminBaseDevTest;
import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.*;
import java.net.*;
import java.util.*;

public class NegClient extends AdminBaseDevTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static String expectedErr = "it does not declare a remote interface";

    public static void main(String[] args) {

        if ("deploy".equals(args[0])) {
            (new NegClient()).deploy(args[1]);
        } else if ("undeploy".equals(args[0])) {
            (new NegClient()).undeploy(args[1]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for failed deployment when wrong ejb-ref declared";
    }

    public void deploy(String path) {
        try {
            stat.addDescription("ejb-cli-negative-deploy");
            AsadminReturn ret = asadminWithOutput("deploy", path);
            if (!ret.returnValue && ret.err.contains(expectedErr)) {
                stat.addStatus("ejb-cli-negative-deploy", stat.PASS);
            } else {
            	  stat.addStatus("ejb-cli-negative-deploy", stat.FAIL);
            }
            	
            //System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stat.printSummary("ejb-cli-negative-deploy");
    }

    public void undeploy(String name) {
        try {
            asadmin("undeploy", name);
            System.out.println("Undeployed " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
